#!/usr/bin/env python3
"""
Build the SetPlay V15 Inverted Cloud static global popularity index.

Fetches top global Movies and TV Series from TMDb API v3 using Python's
standard library only (zero third-party pip dependencies), computes a
normalized 0.0..100.0 popularity/quality score per title, and writes a
compact minified JSON file to docs/popularity_index.json for GitHub Pages CDN.

Usage:
    TMDB_API_KEY="<your_tmdb_key>" python3 scripts/build_popularity_index.py
"""

from __future__ import annotations

import gzip
import json
import math
import os
import re
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
from typing import Any

TMDB_API_BASE = "https://api.themoviedb.org/3"
OUTPUT_PATH = os.path.join("docs", "popularity_index.json")
COLLECTIONS_OUTPUT_PATH = os.path.join("docs", "collections_index.json")

REQUEST_SLEEP_SECONDS = 0.05  # 50ms polite pacing between requests
REQUEST_TIMEOUT_SECONDS = 25
MAX_RETRIES = 4
BAYESIAN_PRIOR_M = 200.0
DYNAMIC_DISCOVERY_MOVIE_LIMIT = 250
MAX_COLLECTIONS = 350

# Top 80 iconic global TMDb movie franchise/collection IDs
CURATED_COLLECTION_IDS: list[int] = [
    645,      # James Bond
    87359,    # Mission: Impossible
    9485,     # The Fast and the Furious
    404609,   # John Wick
    295,      # Pirates of the Caribbean
    1241,     # Harry Potter
    10,       # Star Wars
    119,      # The Lord of the Rings
    121938,   # The Hobbit
    86311,    # The Avengers
    531241,   # Spider-Man (MCU)
    263,      # The Dark Knight
    328,      # Jurassic Park
    10194,    # Toy Story
    31562,    # The Bourne
    84,       # Indiana Jones
    2344,     # The Matrix
    8091,     # Alien
    528,      # The Terminator
    1575,     # Rocky
    1570,     # Die Hard
    8650,     # Transformers
    748,      # X-Men
    131635,   # The Hunger Games
    173710,   # Planet of the Apes (Reboot)
    313086,   # The Conjuring
    726871,   # Dune
    448150,   # Deadpool
    87096,    # Avatar
    230,      # The Godfather
    264,      # Back to the Future
    8945,     # Mad Max
    2150,     # Shrek
    86066,    # Despicable Me
    8354,     # Ice Age
    77816,    # Kung Fu Panda
    89137,    # How to Train Your Dragon
    386382,   # Frozen
    137697,   # Finding Nemo
    87118,    # Cars
    137696,   # Monsters, Inc.
    468222,   # The Incredibles
    14740,    # Madagascar
    573436,   # Spider-Man: Spider-Verse
    131292,   # Iron Man
    131295,   # Captain America
    131296,   # Thor
    284433,   # Guardians of the Galaxy
    422834,   # Ant-Man
    529892,   # Black Panther
    618529,   # Doctor Strange
    556,      # Spider-Man (Raimi)
    125574,   # The Amazing Spider-Man
    558216,   # Venom
    120794,   # Batman (1989)
    720879,   # Sonic the Hedgehog
    1022790,  # Inside Out
    399,      # Predator
    656,      # Saw
    2602,     # Scream
    91361,    # Halloween
    525634,   # A Quiet Place
    228446,   # Insidious
    256322,   # The Purge
    8864,     # Final Destination
    17255,    # Resident Evil
    2326,     # Underworld
    5039,     # Rambo
    126125,   # The Expendables
    90863,    # Rush Hour
    14890,    # Bad Boys
    86055,    # Men in Black
    2980,     # Ghostbusters
    304,      # Ocean's
    722971,   # Knives Out
    435259,   # Fantastic Beasts
    420,      # The Chronicles of Narnia
    33514,    # The Twilight
    295130,   # The Maze Runner
    523855,   # The Equalizer
]

# Endpoints, page ranges, bucket names, and optional query params for global Movies & TV Series
MOVIE_SOURCES: list[tuple[str, int, str, dict[str, str]]] = [
    ("/trending/movie/week", 15, "trending", {}),
    ("/movie/popular", 25, "popular", {}),
    ("/movie/top_rated", 20, "top_rated", {}),
    ("/discover/movie", 40, "discover_votes", {"sort_by": "vote_count.desc"}),
]

SERIES_SOURCES: list[tuple[str, int, str, dict[str, str]]] = [
    ("/trending/tv/week", 15, "trending", {}),
    ("/tv/popular", 25, "popular", {}),
    ("/tv/top_rated", 20, "top_rated", {}),
    ("/discover/tv", 40, "discover_votes", {"sort_by": "vote_count.desc"}),
]

NON_ALNUM_OR_UNDERSCORE_RE = re.compile(r"(?:[^\w]|_)+", re.UNICODE)
MULTI_SPACE_RE = re.compile(r"\s+")
YEAR_RE = re.compile(r"^(\d{4})")


def get_tmdb_api_key() -> str:
    raw = os.environ.get("TMDB_API_KEY", "").strip()
    if (
        not raw
        or raw.lower() == "null"
        or raw.upper().startswith("YOUR_")
        or raw.upper().startswith("MY_")
    ):
        print(
            "ERROR: TMDB_API_KEY environment variable is missing or holds a placeholder value.\n"
            "Set a valid TMDb API v3 key (or v4 read access token) in TMDB_API_KEY before running.",
            file=sys.stderr,
        )
        sys.exit(1)
    return raw


def is_bearer_token(api_key: str) -> bool:
    # TMDb v4 Read Access Tokens are JWTs (3 dot-separated segments, usually > 100 chars)
    return len(api_key) > 64 and api_key.count(".") == 2


def fetch_tmdb_page(
    endpoint: str,
    page: int,
    api_key: str,
    extra_params: dict[str, str] | None = None,
) -> dict[str, Any]:
    params: dict[str, str] = {
        "language": "en-US",
        "page": str(page),
    }
    if extra_params:
        params.update(extra_params)
    headers: dict[str, str] = {
        "Accept": "application/json",
        "User-Agent": "SetPlay-PopularityIndexBuilder/1.0",
    }

    if is_bearer_token(api_key):
        headers["Authorization"] = f"Bearer {api_key}"
    else:
        params["api_key"] = api_key

    url = f"{TMDB_API_BASE}{endpoint}?{urllib.parse.urlencode(params)}"

    last_error: Exception | None = None
    for attempt in range(1, MAX_RETRIES + 1):
        req = urllib.request.Request(url, headers=headers, method="GET")
        try:
            with urllib.request.urlopen(req, timeout=REQUEST_TIMEOUT_SECONDS) as resp:
                raw_bytes = resp.read()
                return json.loads(raw_bytes.decode("utf-8"))
        except urllib.error.HTTPError as err:
            last_error = err
            status = err.code
            if status == 429:
                retry_after = err.headers.get("Retry-After")
                wait_s = float(retry_after) if retry_after and retry_after.isdigit() else (1.5 * attempt)
                wait_s = max(0.5, min(wait_s, 30.0))
                print(
                    f"[429] Rate-limited on {endpoint} page={page}; sleeping {wait_s:.1f}s "
                    f"(attempt {attempt}/{MAX_RETRIES})",
                    file=sys.stderr,
                )
                time.sleep(wait_s)
                continue
            if 500 <= status < 600 and attempt < MAX_RETRIES:
                wait_s = 1.0 * attempt
                print(
                    f"[{status}] Server error on {endpoint} page={page}; retrying in {wait_s:.1f}s "
                    f"(attempt {attempt}/{MAX_RETRIES})",
                    file=sys.stderr,
                )
                time.sleep(wait_s)
                continue
            if status in (401, 403):
                print(
                    f"ERROR: TMDb rejected credentials with HTTP {status} on {endpoint}. "
                    "Verify TMDB_API_KEY secret.",
                    file=sys.stderr,
                )
                sys.exit(1)
            if status in (400, 404, 422) and page > 1:
                print(
                    f"[{status}] Reached page cap on {endpoint} at page={page}; stopping pagination.",
                    file=sys.stderr,
                )
                return {"results": [], "total_pages": page - 1}
            raise
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as err:
            last_error = err
            if attempt < MAX_RETRIES:
                wait_s = 1.0 * attempt
                print(
                    f"[NET] Error on {endpoint} page={page}: {err}; retrying in {wait_s:.1f}s "
                    f"(attempt {attempt}/{MAX_RETRIES})",
                    file=sys.stderr,
                )
                time.sleep(wait_s)
                continue
            raise

    raise RuntimeError(f"Failed fetching {endpoint} page={page}: {last_error}")


def fetch_tmdb_object(
    endpoint: str,
    api_key: str,
) -> dict[str, Any] | None:
    params: dict[str, str] = {"language": "en-US"}
    headers: dict[str, str] = {
        "Accept": "application/json",
        "User-Agent": "SetPlay-PopularityIndexBuilder/1.0",
    }

    if is_bearer_token(api_key):
        headers["Authorization"] = f"Bearer {api_key}"
    else:
        params["api_key"] = api_key

    url = f"{TMDB_API_BASE}{endpoint}?{urllib.parse.urlencode(params)}"

    for attempt in range(1, MAX_RETRIES + 1):
        req = urllib.request.Request(url, headers=headers, method="GET")
        try:
            with urllib.request.urlopen(req, timeout=REQUEST_TIMEOUT_SECONDS) as resp:
                raw_bytes = resp.read()
                parsed = json.loads(raw_bytes.decode("utf-8"))
                return parsed if isinstance(parsed, dict) else None
        except urllib.error.HTTPError as err:
            status = err.code
            if status == 429:
                retry_after = err.headers.get("Retry-After")
                wait_s = float(retry_after) if retry_after and retry_after.isdigit() else (1.5 * attempt)
                wait_s = max(0.5, min(wait_s, 30.0))
                time.sleep(wait_s)
                continue
            if 500 <= status < 600 and attempt < MAX_RETRIES:
                time.sleep(1.0 * attempt)
                continue
            if status in (401, 403):
                print(
                    f"ERROR: TMDb rejected credentials with HTTP {status} on {endpoint}. "
                    "Verify TMDB_API_KEY secret.",
                    file=sys.stderr,
                )
                sys.exit(1)
            if status in (400, 404, 422):
                return None
            if attempt < MAX_RETRIES:
                time.sleep(1.0 * attempt)
                continue
            return None
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError):
            if attempt < MAX_RETRIES:
                time.sleep(1.0 * attempt)
                continue
            return None

    return None


def extract_year(date_str: Any) -> int:
    if not isinstance(date_str, str):
        return 0
    match = YEAR_RE.match(date_str.strip())
    if not match:
        return 0
    year = int(match.group(1))
    return year if 1880 <= year <= 2100 else 0


def normalize_alnum_title(raw_title: str) -> str:
    """
    Matches SetPlay's GlobalPopularityIndexManager.lookupGlobalScore normalization:
    NFKC -> lowercase -> replace non-alphanumeric (including underscores) with space -> collapse spaces.
    """
    if not raw_title:
        return ""
    text = unicodedata.normalize("NFKC", raw_title).lower()
    text = NON_ALNUM_OR_UNDERSCORE_RE.sub(" ", text)
    text = MULTI_SPACE_RE.sub(" ", text).strip()
    return text


def ascii_fold_title(normalized_title: str) -> str:
    if not normalized_title:
        return ""
    decomposed = unicodedata.normalize("NFKD", normalized_title)
    stripped = "".join(ch for ch in decomposed if not unicodedata.combining(ch))
    stripped = NON_ALNUM_OR_UNDERSCORE_RE.sub(" ", stripped).lower()
    return MULTI_SPACE_RE.sub(" ", stripped).strip()


def collect_media_catalog(
    sources: list[tuple[str, int, str, dict[str, str]]],
    is_movie: bool,
    api_key: str,
) -> dict[int, dict[str, Any]]:
    records: dict[int, dict[str, Any]] = {}
    title_field = "title" if is_movie else "name"
    orig_title_field = "original_title" if is_movie else "original_name"
    date_field = "release_date" if is_movie else "first_air_date"

    for endpoint, max_pages, bucket, extra_params in sources:
        rank_counter = 0
        for page in range(1, max_pages + 1):
            payload = fetch_tmdb_page(endpoint, page, api_key, extra_params=extra_params)
            results = payload.get("results") or []
            if not results:
                break

            for item in results:
                if not isinstance(item, dict):
                    continue
                if item.get("adult") is True:
                    continue
                raw_id = item.get("id")
                if not isinstance(raw_id, int) or raw_id <= 0:
                    continue

                rank_counter += 1
                popularity = float(item.get("popularity") or 0.0)
                vote_average = float(item.get("vote_average") or 0.0)
                vote_count = int(item.get("vote_count") or 0)
                title = str(item.get(title_field) or "").strip()
                orig_title = str(item.get(orig_title_field) or "").strip()
                year = extract_year(item.get(date_field))

                existing = records.get(raw_id)
                if existing is None:
                    existing = {
                        "id": raw_id,
                        "title": title,
                        "original_title": orig_title,
                        "year": year,
                        "popularity": popularity,
                        "vote_average": vote_average,
                        "vote_count": vote_count,
                        "trending_rank": None,
                        "buckets": set(),
                    }
                    records[raw_id] = existing
                else:
                    if not existing["title"] and title:
                        existing["title"] = title
                    if not existing["original_title"] and orig_title:
                        existing["original_title"] = orig_title
                    if existing["year"] == 0 and year > 0:
                        existing["year"] = year
                    if popularity > existing["popularity"]:
                        existing["popularity"] = popularity
                    if vote_count > existing["vote_count"]:
                        existing["vote_count"] = vote_count
                        existing["vote_average"] = vote_average

                existing["buckets"].add(bucket)
                if bucket == "trending":
                    prev_rank = existing["trending_rank"]
                    if prev_rank is None or rank_counter < prev_rank:
                        existing["trending_rank"] = rank_counter

            total_pages = int(payload.get("total_pages") or 0)
            time.sleep(REQUEST_SLEEP_SECONDS)
            if total_pages > 0 and page >= total_pages:
                break

    return records


def compute_scores(records: dict[int, dict[str, Any]]) -> dict[int, float]:
    """
    Computes a normalized 0.0..100.0 score (rounded to 1 decimal) combining:
      1. TMDb popularity percentile (rank-based + log-normalized smoothing)
      2. Bayesian vote_average weighted by vote_count (prior m = 200)
      3. Trending rank bonus (strong boost for current weekly trending titles)
      4. Small multi-list consensus bonus (appearing across trending/popular/top_rated)
    """
    if not records:
        return {}

    items = list(records.values())
    total_items = len(items)

    # 1. Popularity percentile rank across all unique items in this media type
    sorted_by_pop = sorted(
        items,
        key=lambda r: (r["popularity"], r["vote_count"], r["vote_average"]),
    )
    pop_percentile_by_id: dict[int, float] = {}
    denom = max(1, total_items - 1)
    for idx, rec in enumerate(sorted_by_pop):
        pop_percentile_by_id[rec["id"]] = (idx / denom) * 100.0

    max_log_pop = max(
        (math.log1p(max(0.0, rec["popularity"])) for rec in items),
        default=1.0,
    )
    if max_log_pop <= 0.0:
        max_log_pop = 1.0

    # 2. Global mean rating C for Bayesian prior (m = 200)
    rated_values = [
        rec["vote_average"]
        for rec in items
        if rec["vote_count"] > 0 and rec["vote_average"] > 0.0
    ]
    prior_mean_c = (sum(rated_values) / len(rated_values)) if rated_values else 6.8

    scores: dict[int, float] = {}
    for rec in items:
        rec_id = rec["id"]
        pop_pct = pop_percentile_by_id[rec_id]
        log_pop_pct = (math.log1p(max(0.0, rec["popularity"])) / max_log_pop) * 100.0
        blended_pop = 0.72 * pop_pct + 0.28 * log_pop_pct

        v = float(max(0, rec["vote_count"]))
        r = float(max(0.0, min(10.0, rec["vote_average"])))
        bayesian_rating = (
            (v / (v + BAYESIAN_PRIOR_M)) * r
            + (BAYESIAN_PRIOR_M / (v + BAYESIAN_PRIOR_M)) * prior_mean_c
        )
        # Map Bayesian rating [4.0 .. 9.5] -> [0.0 .. 100.0]
        bayesian_100 = max(0.0, min(100.0, ((bayesian_rating - 4.0) / 5.5) * 100.0))

        trending_rank = rec["trending_rank"]
        if trending_rank is not None:
            # Rank 1 -> +18.0, rank 100 -> ~+10.5, rank 300 -> ~+0.1
            norm_pos = max(0.0, min(1.0, (trending_rank - 1) / 300.0))
            trending_bonus = 18.0 * ((1.0 - norm_pos) ** 1.35)
        else:
            trending_bonus = 0.0

        buckets = rec["buckets"]
        multi_list_bonus = 0.0
        if "popular" in buckets and "top_rated" in buckets:
            multi_list_bonus += 3.0
        if "trending" in buckets and "popular" in buckets:
            multi_list_bonus += 2.0

        composite = (
            0.50 * blended_pop
            + 0.35 * bayesian_100
            + trending_bonus
            + multi_list_bonus
        )
        final_score = round(max(0.0, min(100.0, composite)), 1)
        scores[rec_id] = final_score

    return scores


def register_title_keys(
    title_map: dict[str, float],
    prefix: str,
    rec: dict[str, Any],
    score: float,
) -> None:
    year = int(rec.get("year") or 0)
    candidates: set[str] = set()

    for raw_field in ("title", "original_title"):
        raw = str(rec.get(raw_field) or "").strip()
        if not raw:
            continue
        norm = normalize_alnum_title(raw)
        if norm:
            candidates.add(norm)
            folded = ascii_fold_title(norm)
            if folded and folded != norm:
                candidates.add(folded)

    for norm_title in candidates:
        key = f"{prefix}:{norm_title}_{year}"
        prev = title_map.get(key)
        if prev is None or score > prev:
            title_map[key] = score

        # Also index yearless key for strong global titles so year-0 IPTV items match
        if score >= 45.0:
            yearless_key = f"{prefix}:{norm_title}"
            prev_yearless = title_map.get(yearless_key)
            if prev_yearless is None or score > prev_yearless:
                title_map[yearless_key] = score


def build_index() -> dict[str, Any]:
    api_key = get_tmdb_api_key()

    print("Fetching global Movies from TMDb API v3...")
    movie_records = collect_media_catalog(MOVIE_SOURCES, is_movie=True, api_key=api_key)
    print(f"Collected {len(movie_records)} unique movies.")

    print("Fetching global TV Series from TMDb API v3...")
    series_records = collect_media_catalog(SERIES_SOURCES, is_movie=False, api_key=api_key)
    print(f"Collected {len(series_records)} unique series.")

    movie_scores = compute_scores(movie_records)
    series_scores = compute_scores(series_records)

    # Sort ID maps by descending score so output is deterministic and easy to inspect
    movie_id_map: dict[str, float] = {
        str(rec_id): score
        for rec_id, score in sorted(
            movie_scores.items(),
            key=lambda kv: (-kv[1], kv[0]),
        )
    }
    series_id_map: dict[str, float] = {
        str(rec_id): score
        for rec_id, score in sorted(
            series_scores.items(),
            key=lambda kv: (-kv[1], kv[0]),
        )
    }

    title_map: dict[str, float] = {}
    for rec_id, rec in movie_records.items():
        register_title_keys(title_map, "m", rec, movie_scores[rec_id])
    for rec_id, rec in series_records.items():
        register_title_keys(title_map, "s", rec, series_scores[rec_id])

    sorted_title_map: dict[str, float] = dict(
        sorted(title_map.items(), key=lambda kv: kv[0])
    )

    return {
        "v": 1,
        "generatedAt": int(time.time()),
        "m": movie_id_map,
        "s": series_id_map,
        "t": sorted_title_map,
    }


def normalize_release_date_sort_key(date_str: Any, movie_id: int) -> tuple[int, str, int]:
    if isinstance(date_str, str):
        cleaned = date_str.strip()
        if re.match(r"^\d{4}-\d{2}-\d{2}$", cleaned):
            return (0, cleaned, movie_id)
        year = extract_year(cleaned)
        if year > 0:
            return (0, f"{year:04d}-12-31", movie_id)
    return (1, "9999-99-99", movie_id)


def build_collections_index(
    movie_id_map: dict[str, float],
    api_key: str,
) -> dict[str, Any]:
    collection_ids: list[int] = []
    seen_collections: set[int] = set()

    for seed_id in CURATED_COLLECTION_IDS:
        if seed_id > 0 and seed_id not in seen_collections:
            seen_collections.add(seed_id)
            collection_ids.append(seed_id)

    top_movie_ids = list(movie_id_map.keys())[:DYNAMIC_DISCOVERY_MOVIE_LIMIT]
    print(
        f"Discovering dynamic movie collections from top {len(top_movie_ids)} movies "
        f"(seeded with {len(collection_ids)} curated franchises)..."
    )
    for movie_id_str in top_movie_ids:
        if len(collection_ids) >= MAX_COLLECTIONS:
            break
        details = fetch_tmdb_object(f"/movie/{movie_id_str}", api_key)
        time.sleep(REQUEST_SLEEP_SECONDS)
        if not details:
            continue
        btc = details.get("belongs_to_collection")
        if isinstance(btc, dict):
            col_id = btc.get("id")
            if isinstance(col_id, int) and col_id > 0 and col_id not in seen_collections:
                seen_collections.add(col_id)
                collection_ids.append(col_id)

    collection_ids = collection_ids[:MAX_COLLECTIONS]
    print(f"Fetching official TMDb metadata for {len(collection_ids)} unique collections...")

    collections_meta: dict[str, dict[str, str]] = {}
    movie_to_collection: dict[str, list[Any]] = {}
    title_to_collection: dict[str, list[Any]] = {}

    for col_id in collection_ids:
        payload = fetch_tmdb_object(f"/collection/{col_id}", api_key)
        time.sleep(REQUEST_SLEEP_SECONDS)
        if not payload:
            continue

        col_name = str(payload.get("name") or "").strip()
        parts_raw = payload.get("parts") or []
        if not col_name or not isinstance(parts_raw, list):
            continue

        valid_parts: list[dict[str, Any]] = []
        for part in parts_raw:
            if not isinstance(part, dict):
                continue
            if part.get("adult") is True:
                continue
            part_id = part.get("id")
            if not isinstance(part_id, int) or part_id <= 0:
                continue
            valid_parts.append(part)

        if not valid_parts:
            continue

        valid_parts.sort(
            key=lambda p: normalize_release_date_sort_key(p.get("release_date"), int(p["id"]))
        )

        col_id_str = str(col_id)
        poster_path = str(payload.get("poster_path") or "").strip()
        collections_meta[col_id_str] = {
            "n": col_name,
            "p": poster_path,
        }

        for order_idx, part in enumerate(valid_parts, start=1):
            part_id_str = str(int(part["id"]))
            entry = [col_id_str, order_idx]
            movie_to_collection[part_id_str] = entry

            year = extract_year(part.get("release_date"))
            if year <= 0:
                continue

            candidates: set[str] = set()
            for field in ("title", "original_title"):
                raw_t = str(part.get(field) or "").strip()
                if not raw_t:
                    continue
                norm = normalize_alnum_title(raw_t)
                if norm:
                    candidates.add(norm)
                    folded = ascii_fold_title(norm)
                    if folded and folded != norm:
                        candidates.add(folded)

            for norm_title in candidates:
                t_key = f"{norm_title}_{year}"
                if t_key not in title_to_collection:
                    title_to_collection[t_key] = entry

    sorted_collections_meta = dict(sorted(collections_meta.items(), key=lambda kv: int(kv[0])))
    sorted_movie_map = dict(sorted(movie_to_collection.items(), key=lambda kv: int(kv[0])))
    sorted_title_map = dict(sorted(title_to_collection.items(), key=lambda kv: kv[0]))

    return {
        "v": 1,
        "generatedAt": int(time.time()),
        "c": sorted_collections_meta,
        "m": sorted_movie_map,
        "t": sorted_title_map,
    }


def write_output(payload: dict[str, Any], output_path: str = OUTPUT_PATH) -> tuple[int, int]:
    parent_dir = os.path.dirname(output_path)
    if parent_dir:
        os.makedirs(parent_dir, exist_ok=True)

    serialized = json.dumps(payload, ensure_ascii=False, separators=(",", ":"))
    raw_bytes = serialized.encode("utf-8")

    tmp_path = f"{output_path}.tmp"
    with open(tmp_path, "wb") as handle:
        handle.write(raw_bytes)
    os.replace(tmp_path, output_path)

    gz_bytes = gzip.compress(raw_bytes, compresslevel=9)
    return len(raw_bytes), len(gz_bytes)


def main() -> None:
    started = time.time()
    api_key = get_tmdb_api_key()
    payload = build_index()
    raw_size, gz_size = write_output(payload, OUTPUT_PATH)
    pop_elapsed = time.time() - started

    print(
        f"Wrote {OUTPUT_PATH} in {pop_elapsed:.1f}s | "
        f"unique movies: {len(payload['m'])}, "
        f"unique series: {len(payload['s'])}, "
        f"title keys: {len(payload['t'])}, "
        f"size: {raw_size / 1024.0:.1f} KB raw ({gz_size / 1024.0:.1f} KB gzip)"
    )

    col_started = time.time()
    collections_payload = build_collections_index(payload["m"], api_key)
    col_raw_size, col_gz_size = write_output(collections_payload, COLLECTIONS_OUTPUT_PATH)
    col_elapsed = time.time() - col_started

    print(
        f"Wrote {COLLECTIONS_OUTPUT_PATH} in {col_elapsed:.1f}s | "
        f"collections: {len(collections_payload['c'])}, "
        f"movie mappings: {len(collections_payload['m'])}, "
        f"title_year keys: {len(collections_payload['t'])}, "
        f"size: {col_raw_size / 1024.0:.1f} KB raw ({col_gz_size / 1024.0:.1f} KB gzip)"
    )


if __name__ == "__main__":
    main()
