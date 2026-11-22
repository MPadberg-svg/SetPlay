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

REQUEST_SLEEP_SECONDS = 0.05  # 50ms polite pacing between requests
REQUEST_TIMEOUT_SECONDS = 25
MAX_RETRIES = 4
BAYESIAN_PRIOR_M = 200.0

# Endpoints and page ranges for global Movies & TV Series
MOVIE_SOURCES: list[tuple[str, int, str]] = [
    ("/trending/movie/week", 15, "trending"),
    ("/movie/popular", 25, "popular"),
    ("/movie/top_rated", 20, "top_rated"),
]

SERIES_SOURCES: list[tuple[str, int, str]] = [
    ("/trending/tv/week", 15, "trending"),
    ("/tv/popular", 25, "popular"),
    ("/tv/top_rated", 20, "top_rated"),
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


def fetch_tmdb_page(endpoint: str, page: int, api_key: str) -> dict[str, Any]:
    params: dict[str, str] = {
        "language": "en-US",
        "page": str(page),
    }
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
    sources: list[tuple[str, int, str]],
    is_movie: bool,
    api_key: str,
) -> dict[int, dict[str, Any]]:
    records: dict[int, dict[str, Any]] = {}
    title_field = "title" if is_movie else "name"
    orig_title_field = "original_title" if is_movie else "original_name"
    date_field = "release_date" if is_movie else "first_air_date"

    for endpoint, max_pages, bucket in sources:
        rank_counter = 0
        for page in range(1, max_pages + 1):
            payload = fetch_tmdb_page(endpoint, page, api_key)
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
    payload = build_index()
    raw_size, gz_size = write_output(payload, OUTPUT_PATH)
    elapsed = time.time() - started

    print(
        f"Wrote {OUTPUT_PATH} in {elapsed:.1f}s | "
        f"unique movies: {len(payload['m'])}, "
        f"unique series: {len(payload['s'])}, "
        f"title keys: {len(payload['t'])}, "
        f"size: {raw_size / 1024.0:.1f} KB raw ({gz_size / 1024.0:.1f} KB gzip)"
    )


if __name__ == "__main__":
    main()
