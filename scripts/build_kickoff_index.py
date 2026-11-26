#!/usr/bin/env python3
"""
SetPlay Va2 — 6-Hourly Static Kickoff Schedule Window Builder.

Builds an 8-day UTC schedule window (Yesterday -1 through +6 Days) across
SetPlay's 13 core competitions using ONLY the Python 3.11 standard library.

Sources:
  1. Football-Data.org v4 (optional, when FOOTBALL_DATA_KEY env var is set) — single
     window request covering European Top-5 & UEFA Champions League.
  2. API-Football v3 (optional, when API_FOOTBALL_KEY env var is set) — Today &
     Tomorrow only (max 2 req/run = 8 req/day, strictly respecting the 100 req/day tier).
  3. ESPN public soccer scoreboards (zero-key fallback & MENA/CAF/global authority).

Outputs:
  docs/kickoff/schedule_window.json (written atomically ONLY when total matches > 0).
"""

from __future__ import annotations

import datetime as dt
import json
import os
import re
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any

SCHEMA_VERSION = 1
OUTPUT_PATH = Path("docs/kickoff/schedule_window.json")
USER_AGENT = "SetPlay-Kickoff-IndexBot/Va2 (+https://github.com/MPadberg-svg/SetPlay)"
HTTP_TIMEOUT_SECONDS = 20

# SetPlay's 13 core Tier-1 competitions (strictly aligned with FootballRepository.ALL_TOURNAMENTS)
COMPETITIONS: list[dict[str, Any]] = [
    {
        "tournamentId": "uefa_cl",
        "tournamentName": "UEFA Champions League",
        "tournamentRegion": "Europe",
        "espnSlug": "uefa.champions",
        "fdCode": "CL",
        "apiFootballIds": [2],
    },
    {
        "tournamentId": "uefa_el",
        "tournamentName": "UEFA Europa League",
        "tournamentRegion": "Europe",
        "espnSlug": "uefa.europa",
        "fdCode": "",
        "apiFootballIds": [3],
    },
    {
        "tournamentId": "premier_league",
        "tournamentName": "Premier League",
        "tournamentRegion": "England",
        "espnSlug": "eng.1",
        "fdCode": "PL",
        "apiFootballIds": [39],
    },
    {
        "tournamentId": "laliga",
        "tournamentName": "LaLiga EA Sports",
        "tournamentRegion": "Spain",
        "espnSlug": "esp.1",
        "fdCode": "PD",
        "apiFootballIds": [140],
    },
    {
        "tournamentId": "serie_a",
        "tournamentName": "Serie A",
        "tournamentRegion": "Italy",
        "espnSlug": "ita.1",
        "fdCode": "SA",
        "apiFootballIds": [135],
    },
    {
        "tournamentId": "bundesliga",
        "tournamentName": "Bundesliga",
        "tournamentRegion": "Germany",
        "espnSlug": "ger.1",
        "fdCode": "BL1",
        "apiFootballIds": [78],
    },
    {
        "tournamentId": "ligue_1",
        "tournamentName": "Ligue 1 McDonald's",
        "tournamentRegion": "France",
        "espnSlug": "fra.1",
        "fdCode": "FL1",
        "apiFootballIds": [61],
    },
    {
        "tournamentId": "botola_pro",
        "tournamentName": "Botola Pro Inwi",
        "tournamentRegion": "Morocco",
        "espnSlug": "mar.1",
        "fdCode": "",
        "apiFootballIds": [200],
    },
    {
        "tournamentId": "saudi_pro",
        "tournamentName": "Roshn Saudi League",
        "tournamentRegion": "Saudi Arabia",
        "espnSlug": "ksa.1",
        "fdCode": "",
        "apiFootballIds": [307],
    },
    {
        "tournamentId": "caf_cl",
        "tournamentName": "CAF Champions League",
        "tournamentRegion": "Africa",
        "espnSlug": "caf.champions",
        "fdCode": "",
        "apiFootballIds": [12],
    },
    {
        "tournamentId": "caf_confed",
        "tournamentName": "CAF Confederation Cup",
        "tournamentRegion": "Africa",
        "espnSlug": "caf.confed",
        "fdCode": "",
        "apiFootballIds": [20],
    },
    {
        "tournamentId": "intl_official",
        "tournamentName": "International Official Fixtures",
        "tournamentRegion": "International",
        "espnSlug": "fifa.worldq.caf",
        "fdCode": "WC",
        "apiFootballIds": [1, 4, 5, 6, 7, 9, 15, 29, 30, 31, 32, 34, 36],
    },
    {
        "tournamentId": "intl_friendly",
        "tournamentName": "International Friendlies",
        "tournamentRegion": "International",
        "espnSlug": "fifa.friendly",
        "fdCode": "",
        "apiFootballIds": [10],
    },
]

COMP_BY_ID = {c["tournamentId"]: c for c in COMPETITIONS}
COMP_BY_FD_CODE: dict[str, dict[str, Any]] = {
    "CL": COMP_BY_ID["uefa_cl"],
    "PL": COMP_BY_ID["premier_league"],
    "PD": COMP_BY_ID["laliga"],
    "SA": COMP_BY_ID["serie_a"],
    "BL1": COMP_BY_ID["bundesliga"],
    "FL1": COMP_BY_ID["ligue_1"],
    "WC": COMP_BY_ID["intl_official"],
    "EC": COMP_BY_ID["intl_official"],
}
COMP_BY_API_FOOTBALL_ID: dict[int, dict[str, Any]] = {}
for _comp in COMPETITIONS:
    for _lid in _comp["apiFootballIds"]:
        COMP_BY_API_FOOTBALL_ID[_lid] = _comp

# Club transliteration & alias canonicalization (mirrors FootballRepository.normalizeTeamNameForMerge)
CLUB_CANONICAL_ALIASES: dict[str, str] = {
    # Saudi Pro League / MENA transliterations
    "al qadsiyah": "al qadsiah",
    "al qadisiya": "al qadsiah",
    "qadsiyah": "al qadsiah",
    "qadisiya": "al qadsiah",
    "qadsiah": "al qadsiah",
    "al feiha": "al fayha",
    "feiha": "al fayha",
    "fayha": "al fayha",
    "al ittihad jeddah": "al ittihad",
    "ittihad jeddah": "al ittihad",
    "ittihad": "al ittihad",
    "al hilal riyadh": "al hilal",
    "al hilal saudi": "al hilal",
    "hilal": "al hilal",
    "al nassr riyadh": "al nassr",
    "nassr": "al nassr",
    "al ahli jeddah": "al ahli saudi",
    "al ahli saudi": "al ahli saudi",
    "al ettifaq": "al ettifaq",
    "ettifaq": "al ettifaq",
    "al shabab riyadh": "al shabab",
    "al taawoun": "al taawon",
    "al taawon": "al taawon",
    "al okhdood": "al akhdoud",
    "al akhdoud": "al akhdoud",
    "al orobah": "al orobah",
    "al kholood": "al kholood",
    "damac": "damac",
    "raja casablanca": "raja casablanca",
    "raja": "raja casablanca",
    "wydad casablanca": "wydad casablanca",
    "wydad": "wydad casablanca",
    "far rabat": "as far",
    "as far rabat": "as far",
    "rs berkane": "berkane",
    "renaissance berkane": "berkane",
    # European giants & city-suffixed variants
    "psg": "paris saint germain",
    "paris sg": "paris saint germain",
    "paris saint germain": "paris saint germain",
    "internazionale": "inter milan",
    "internazionale milano": "inter milan",
    "inter milano": "inter milan",
    "inter": "inter milan",
    "milan": "ac milan",
    "man city": "manchester city",
    "man united": "manchester united",
    "man utd": "manchester united",
    "spurs": "tottenham hotspur",
    "tottenham": "tottenham hotspur",
    "wolves": "wolverhampton wanderers",
    "wolverhampton": "wolverhampton wanderers",
    "newcastle": "newcastle united",
    "west ham": "west ham united",
    "nottingham": "nottingham forest",
    "nottm forest": "nottingham forest",
    "brighton": "brighton hove albion",
    "brighton and hove albion": "brighton hove albion",
    "bayern": "bayern munich",
    "bayern munchen": "bayern munich",
    "borussia dortmund": "dortmund",
    "bvb": "dortmund",
    "bayer leverkusen": "leverkusen",
    "bayer 04 leverkusen": "leverkusen",
    "rb leipzig": "leipzig",
    "atletico madrid": "atletico madrid",
    "atletico de madrid": "atletico madrid",
    "athletic bilbao": "athletic club",
    "real betis balompie": "real betis",
    "betis": "real betis",
    "barca": "barcelona",
    "feyenoord rotterdam": "feyenoord",
    "ajax amsterdam": "ajax",
    "psv eindhoven": "psv",
    "sporting cp": "sporting lisbon",
    "sporting clube de portugal": "sporting lisbon",
    "olympique marseille": "marseille",
    "olympique de marseille": "marseille",
    "olympique lyonnais": "lyon",
    "olympique lyon": "lyon",
}

STRIP_TOKENS = {
    "fc", "cf", "sc", "ac", "as", "ca", "cd", "ud", "rc", "rcd", "sd",
    "fk", "sk", "bk", "sv", "tsv", "vfb", "vfl", "fsv", "bsc", "ssc",
    "us", "ss", "afc", "cfc", "club", "de", "futbol", "football", "calcio",
    "losc", "ogc", "sl", "rc", "sd", "spvgg", "1", "04", "05", "1899",
}

STATUS_PRIORITY = {
    "LIVE": 5,
    "HALFTIME": 5,
    "FINISHED": 4,
    "POSTPONED": 3,
    "CANCELLED": 3,
    "UPCOMING": 1,
}


def is_usable_secret(raw: str | None) -> bool:
    if not raw:
        return False
    val = raw.strip()
    if not val:
        return False
    upper = val.upper()
    if upper == "NULL" or upper.startswith("YOUR_") or upper.startswith("MY_"):
        return False
    return True


def strip_diacritics(text: str) -> str:
    decomposed = unicodedata.normalize("NFD", text)
    return "".join(ch for ch in decomposed if unicodedata.category(ch) != "Mn")


def normalize_team_name(raw_name: str) -> str:
    if not raw_name:
        return ""
    cleaned = strip_diacritics(raw_name).lower()
    cleaned = cleaned.replace("&", " and ")
    cleaned = re.sub(r"[^a-z0-9]+", " ", cleaned).strip()
    cleaned = re.sub(r"\s+", " ", cleaned)
    if cleaned in CLUB_CANONICAL_ALIASES:
        return CLUB_CANONICAL_ALIASES[cleaned]

    tokens = [tok for tok in cleaned.split(" ") if tok and tok not in STRIP_TOKENS]
    reduced = " ".join(tokens).strip() or cleaned
    if reduced in CLUB_CANONICAL_ALIASES:
        return CLUB_CANONICAL_ALIASES[reduced]

    # Handle hyphen/prefix variants like "al-qadsiyah" -> "al qadsiah"
    if reduced.startswith("al ") and reduced[3:] in CLUB_CANONICAL_ALIASES:
        return CLUB_CANONICAL_ALIASES[reduced[3:]]
    return reduced


def build_canonical_id(
    tournament_id: str,
    iso_date: str,
    home_team_name: str,
    away_team_name: str,
) -> str:
    t_id = tournament_id.strip().lower()
    d_str = iso_date.strip()
    h_norm = normalize_team_name(home_team_name)
    a_norm = normalize_team_name(away_team_name)
    return f"{t_id}|{d_str}|{h_norm}|{a_norm}"


def parse_iso_utc_to_epoch_ms(raw_iso: str) -> int:
    if not raw_iso:
        return 0
    val = raw_iso.strip()
    if val.endswith("Z"):
        val = val[:-1] + "+00:00"
    try:
        parsed = dt.datetime.fromisoformat(val)
        if parsed.tzinfo is None:
            parsed = parsed.replace(tzinfo=dt.timezone.utc)
        return int(parsed.timestamp() * 1000)
    except ValueError:
        return 0


def epoch_ms_to_utc_date(epoch_ms: int, fallback_date: str) -> str:
    if epoch_ms <= 0:
        return fallback_date
    try:
        return dt.datetime.fromtimestamp(epoch_ms / 1000.0, tz=dt.timezone.utc).strftime("%Y-%m-%d")
    except (OverflowError, OSError, ValueError):
        return fallback_date


def fetch_json(url: str, headers: dict[str, str] | None = None) -> Any | None:
    req_headers = {
        "User-Agent": USER_AGENT,
        "Accept": "application/json",
    }
    if headers:
        req_headers.update(headers)
    req = urllib.request.Request(url, headers=req_headers, method="GET")
    try:
        with urllib.request.urlopen(req, timeout=HTTP_TIMEOUT_SECONDS) as resp:
            if resp.status < 200 or resp.status >= 300:
                return None
            raw = resp.read().decode("utf-8", errors="replace")
            return json.loads(raw)
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, json.JSONDecodeError) as exc:
        print(f"[warn] GET {url} failed: {exc}", file=sys.stderr)
        return None


def map_fd_status(raw_status: str) -> tuple[str, str]:
    s = (raw_status or "").upper()
    if s in {"IN_PLAY", "LIVE"}:
        return "LIVE", "LIVE"
    if s == "PAUSED":
        return "HALFTIME", "HT"
    if s in {"FINISHED", "AWARDED"}:
        return "FINISHED", "FT"
    if s == "POSTPONED":
        return "POSTPONED", "PPD"
    if s in {"CANCELLED", "SUSPENDED"}:
        return "CANCELLED", "CANC"
    return "UPCOMING", ""


def fetch_football_data_window(
    api_key: str,
    window_start: str,
    window_end: str,
) -> list[tuple[str, dict[str, Any]]]:
    url = (
        "https://api.football-data.org/v4/matches"
        f"?dateFrom={urllib.parse.quote(window_start)}&dateTo={urllib.parse.quote(window_end)}"
    )
    payload = fetch_json(url, headers={"X-Auth-Token": api_key})
    if not isinstance(payload, dict):
        return []
    matches = payload.get("matches")
    if not isinstance(matches, list):
        return []

    results: list[tuple[str, dict[str, Any]]] = []
    for item in matches:
        if not isinstance(item, dict):
            continue
        comp_code = str((item.get("competition") or {}).get("code") or "").upper()
        comp = COMP_BY_FD_CODE.get(comp_code)
        if not comp:
            continue

        utc_date = str(item.get("utcDate") or "")
        kickoff_ms = parse_iso_utc_to_epoch_ms(utc_date)
        iso_date = epoch_ms_to_utc_date(kickoff_ms, utc_date[:10])
        if not iso_date:
            continue

        home = item.get("homeTeam") or {}
        away = item.get("awayTeam") or {}
        home_name = str(home.get("shortName") or home.get("name") or "").strip()
        away_name = str(away.get("shortName") or away.get("name") or "").strip()
        if not home_name or not away_name:
            continue

        score_ft = (item.get("score") or {}).get("fullTime") or {}
        home_score = score_ft.get("home")
        away_score = score_ft.get("away")
        status, badge = map_fd_status(str(item.get("status") or ""))
        if status == "UPCOMING":
            home_score = None
            away_score = None

        fixture = {
            "canonicalId": build_canonical_id(comp["tournamentId"], iso_date, home_name, away_name),
            "tournamentId": comp["tournamentId"],
            "tournamentName": comp["tournamentName"],
            "tournamentRegion": comp["tournamentRegion"],
            "kickoffEpochMs": kickoff_ms,
            "status": status,
            "liveMinuteOrBadge": badge,
            "homeTeamId": str(home.get("id") or ""),
            "homeTeamName": home_name,
            "homeTeamBadge": str(home.get("crest") or ""),
            "homeScore": int(home_score) if isinstance(home_score, int) else None,
            "awayTeamId": str(away.get("id") or ""),
            "awayTeamName": away_name,
            "awayTeamBadge": str(away.get("crest") or ""),
            "awayScore": int(away_score) if isinstance(away_score, int) else None,
            "stadiumName": str(item.get("venue") or ""),
            "espnEventId": "",
            "sourceProvider": "football_data",
        }
        results.append((iso_date, fixture))
    return results


def map_api_football_status(short_code: str, elapsed: Any) -> tuple[str, str]:
    s = (short_code or "").upper()
    if s in {"1H", "2H", "ET", "P", "BT", "LIVE"}:
        minute_label = f"{elapsed}'" if isinstance(elapsed, int) and elapsed > 0 else s
        return "LIVE", minute_label
    if s == "HT":
        return "HALFTIME", "HT"
    if s in {"FT", "AET", "PEN"}:
        return "FINISHED", s
    if s in {"PST", "SUSP", "INT"}:
        return "POSTPONED", "PPD"
    if s in {"CANC", "ABD", "AWD", "WO"}:
        return "CANCELLED", "CANC"
    return "UPCOMING", ""


def fetch_api_football_day(api_key: str, iso_date: str) -> list[tuple[str, dict[str, Any]]]:
    url = f"https://v3.football.api-sports.io/fixtures?date={urllib.parse.quote(iso_date)}"
    payload = fetch_json(url, headers={"x-apisports-key": api_key})
    if not isinstance(payload, dict):
        return []
    entries = payload.get("response")
    if not isinstance(entries, list):
        return []

    results: list[tuple[str, dict[str, Any]]] = []
    for item in entries:
        if not isinstance(item, dict):
            continue
        league_obj = item.get("league") or {}
        league_id = league_obj.get("id")
        if not isinstance(league_id, int):
            continue
        comp = COMP_BY_API_FOOTBALL_ID.get(league_id)
        if not comp:
            continue

        fixture_obj = item.get("fixture") or {}
        teams_obj = item.get("teams") or {}
        goals_obj = item.get("goals") or {}
        home = teams_obj.get("home") or {}
        away = teams_obj.get("away") or {}

        home_name = str(home.get("name") or "").strip()
        away_name = str(away.get("name") or "").strip()
        if not home_name or not away_name:
            continue

        kickoff_ms = parse_iso_utc_to_epoch_ms(str(fixture_obj.get("date") or ""))
        if kickoff_ms <= 0 and isinstance(fixture_obj.get("timestamp"), int):
            kickoff_ms = int(fixture_obj["timestamp"]) * 1000
        match_date = epoch_ms_to_utc_date(kickoff_ms, iso_date)

        status_obj = fixture_obj.get("status") or {}
        status, badge = map_api_football_status(
            str(status_obj.get("short") or ""),
            status_obj.get("elapsed"),
        )
        home_score = goals_obj.get("home")
        away_score = goals_obj.get("away")
        if status == "UPCOMING":
            home_score = None
            away_score = None

        venue_obj = fixture_obj.get("venue") or {}
        fixture = {
            "canonicalId": build_canonical_id(comp["tournamentId"], match_date, home_name, away_name),
            "tournamentId": comp["tournamentId"],
            "tournamentName": comp["tournamentName"],
            "tournamentRegion": comp["tournamentRegion"],
            "kickoffEpochMs": kickoff_ms,
            "status": status,
            "liveMinuteOrBadge": badge,
            "homeTeamId": str(home.get("id") or ""),
            "homeTeamName": home_name,
            "homeTeamBadge": str(home.get("logo") or ""),
            "homeScore": int(home_score) if isinstance(home_score, int) else None,
            "awayTeamId": str(away.get("id") or ""),
            "awayTeamName": away_name,
            "awayTeamBadge": str(away.get("logo") or ""),
            "awayScore": int(away_score) if isinstance(away_score, int) else None,
            "stadiumName": str(venue_obj.get("name") or ""),
            "espnEventId": "",
            "sourceProvider": "api_football",
        }
        results.append((match_date, fixture))
    return results


def map_espn_status(status_obj: dict[str, Any]) -> tuple[str, str]:
    status_type = status_obj.get("type") or {}
    state = str(status_type.get("state") or "").lower()
    name = str(status_type.get("name") or "").upper()
    short_detail = str(status_type.get("shortDetail") or "").strip()
    display_clock = str(status_obj.get("displayClock") or "").strip()

    if "POSTPONED" in name:
        return "POSTPONED", "PPD"
    if "CANCEL" in name or "ABANDON" in name:
        return "CANCELLED", "CANC"
    if "HALFTIME" in name or short_detail.upper() == "HT":
        return "HALFTIME", "HT"
    if state == "in":
        return "LIVE", display_clock or short_detail or "LIVE"
    if state == "post" or bool(status_type.get("completed")):
        return "FINISHED", "FT"
    return "UPCOMING", ""


def safe_int_score(raw: Any) -> int | None:
    if isinstance(raw, int):
        return raw
    if isinstance(raw, str) and raw.strip().isdigit():
        return int(raw.strip())
    return None


def fetch_espn_league_window(
    comp: dict[str, Any],
    start_compact: str,
    end_compact: str,
) -> list[tuple[str, dict[str, Any]]]:
    slug = comp.get("espnSlug") or ""
    if not slug:
        return []
    url = (
        f"https://site.api.espn.com/apis/site/v2/sports/soccer/{slug}/scoreboard"
        f"?dates={start_compact}-{end_compact}"
    )
    payload = fetch_json(url)
    if not isinstance(payload, dict):
        return []
    events = payload.get("events")
    if not isinstance(events, list):
        return []

    results: list[tuple[str, dict[str, Any]]] = []
    for event in events:
        if not isinstance(event, dict):
            continue
        competitions = event.get("competitions") or []
        if not competitions or not isinstance(competitions[0], dict):
            continue
        c0 = competitions[0]
        competitors = c0.get("competitors") or []
        home_entry = next((c for c in competitors if c.get("homeAway") == "home"), None)
        away_entry = next((c for c in competitors if c.get("homeAway") == "away"), None)
        if not home_entry or not away_entry:
            continue

        home_team = home_entry.get("team") or {}
        away_team = away_entry.get("team") or {}
        home_name = str(home_team.get("shortDisplayName") or home_team.get("displayName") or home_team.get("name") or "").strip()
        away_name = str(away_team.get("shortDisplayName") or away_team.get("displayName") or away_team.get("name") or "").strip()
        if not home_name or not away_name:
            continue

        raw_date = str(c0.get("date") or event.get("date") or "")
        kickoff_ms = parse_iso_utc_to_epoch_ms(raw_date)
        iso_date = epoch_ms_to_utc_date(kickoff_ms, raw_date[:10])
        if not iso_date:
            continue

        status_obj = c0.get("status") or event.get("status") or {}
        status, badge = map_espn_status(status_obj)
        home_score = safe_int_score(home_entry.get("score"))
        away_score = safe_int_score(away_entry.get("score"))
        if status == "UPCOMING":
            home_score = None
            away_score = None

        venue_obj = c0.get("venue") or {}
        fixture = {
            "canonicalId": build_canonical_id(comp["tournamentId"], iso_date, home_name, away_name),
            "tournamentId": comp["tournamentId"],
            "tournamentName": comp["tournamentName"],
            "tournamentRegion": comp["tournamentRegion"],
            "kickoffEpochMs": kickoff_ms,
            "status": status,
            "liveMinuteOrBadge": badge,
            "homeTeamId": str(home_team.get("id") or ""),
            "homeTeamName": home_name,
            "homeTeamBadge": str(home_team.get("logo") or ""),
            "homeScore": home_score,
            "awayTeamId": str(away_team.get("id") or ""),
            "awayTeamName": away_name,
            "awayTeamBadge": str(away_team.get("logo") or ""),
            "awayScore": away_score,
            "stadiumName": str(venue_obj.get("fullName") or ""),
            "espnEventId": str(event.get("id") or ""),
            "sourceProvider": "espn_soccer",
        }
        results.append((iso_date, fixture))
    return results


def merge_fixtures(existing: dict[str, Any], incoming: dict[str, Any]) -> dict[str, Any]:
    existing_prio = STATUS_PRIORITY.get(str(existing.get("status") or "UPCOMING"), 0)
    incoming_prio = STATUS_PRIORITY.get(str(incoming.get("status") or "UPCOMING"), 0)
    prefer_incoming = incoming_prio > existing_prio

    primary = incoming if prefer_incoming else existing
    secondary = existing if prefer_incoming else incoming

    merged = dict(primary)
    if merged.get("kickoffEpochMs", 0) <= 0:
        merged["kickoffEpochMs"] = secondary.get("kickoffEpochMs", 0)
    if not merged.get("liveMinuteOrBadge"):
        merged["liveMinuteOrBadge"] = secondary.get("liveMinuteOrBadge", "")
    if merged.get("homeScore") is None and secondary.get("homeScore") is not None:
        merged["homeScore"] = secondary.get("homeScore")
    if merged.get("awayScore") is None and secondary.get("awayScore") is not None:
        merged["awayScore"] = secondary.get("awayScore")
    if not merged.get("homeTeamBadge"):
        merged["homeTeamBadge"] = secondary.get("homeTeamBadge", "")
    if not merged.get("awayTeamBadge"):
        merged["awayTeamBadge"] = secondary.get("awayTeamBadge", "")
    if not merged.get("stadiumName"):
        merged["stadiumName"] = secondary.get("stadiumName", "")
    if not merged.get("espnEventId"):
        merged["espnEventId"] = secondary.get("espnEventId", "")
    if not merged.get("homeTeamId"):
        merged["homeTeamId"] = secondary.get("homeTeamId", "")
    if not merged.get("awayTeamId"):
        merged["awayTeamId"] = secondary.get("awayTeamId", "")
    return merged


def build_schedule_window() -> dict[str, Any]:
    now_utc = dt.datetime.now(tz=dt.timezone.utc)
    today_date = now_utc.date()
    start_date = today_date - dt.timedelta(days=1)
    end_date = today_date + dt.timedelta(days=6)
    window_dates = {
        (start_date + dt.timedelta(days=offset)).isoformat()
        for offset in range(8)
    }

    window_start = start_date.isoformat()
    window_end = end_date.isoformat()
    start_compact = start_date.strftime("%Y%m%d")
    end_compact = end_date.strftime("%Y%m%d")

    days_map: dict[str, dict[str, dict[str, Any]]] = {d: {} for d in sorted(window_dates)}

    def ingest(records: list[tuple[str, dict[str, Any]]]) -> None:
        for iso_date, fixture in records:
            if iso_date not in days_map:
                continue
            cid = fixture["canonicalId"]
            existing = days_map[iso_date].get(cid)
            if existing is None:
                days_map[iso_date][cid] = fixture
            else:
                days_map[iso_date][cid] = merge_fixtures(existing, fixture)

    # 1. Football-Data.org v4 (1 single request for the entire 8-day European window)
    fd_key = os.environ.get("FOOTBALL_DATA_KEY", "")
    if is_usable_secret(fd_key):
        fd_records = fetch_football_data_window(fd_key.strip(), window_start, window_end)
        print(f"[info] Football-Data.org yielded {len(fd_records)} fixtures")
        ingest(fd_records)

    # 2. API-Football v3 (optional, strictly capped at Today + Tomorrow = 2 req/run)
    af_key = os.environ.get("API_FOOTBALL_KEY", "")
    if is_usable_secret(af_key):
        for offset in (0, 1):
            target_day = (today_date + dt.timedelta(days=offset)).isoformat()
            af_records = fetch_api_football_day(af_key.strip(), target_day)
            print(f"[info] API-Football ({target_day}) yielded {len(af_records)} fixtures")
            ingest(af_records)

    # 3. ESPN public league scoreboards (zero-key global + MENA/CAF coverage)
    espn_total = 0
    for comp in COMPETITIONS:
        records = fetch_espn_league_window(comp, start_compact, end_compact)
        espn_total += len(records)
        ingest(records)
        time.sleep(0.15)
    print(f"[info] ESPN public scoreboards yielded {espn_total} fixtures")

    # Sort each day's fixtures by kickoffEpochMs then tournamentId
    ordered_days: dict[str, list[dict[str, Any]]] = {}
    for iso_date in sorted(days_map.keys()):
        day_fixtures = list(days_map[iso_date].values())
        day_fixtures.sort(key=lambda m: (m.get("kickoffEpochMs", 0), m.get("tournamentId", ""), m.get("canonicalId", "")))
        ordered_days[iso_date] = day_fixtures

    return {
        "schemaVersion": SCHEMA_VERSION,
        "generatedAtEpochMs": int(now_utc.timestamp() * 1000),
        "windowStartDate": window_start,
        "windowEndDate": window_end,
        "days": ordered_days,
    }


def main() -> int:
    payload = build_schedule_window()
    total_matches = sum(len(v) for v in payload["days"].values())
    print(
        f"[info] Built Kickoff schedule window {payload['windowStartDate']}..{payload['windowEndDate']} "
        f"with {total_matches} deduplicated matches across {len(payload['days'])} days."
    )

    if total_matches <= 0:
        print(
            "[warn] Zero matches fetched across all upstreams; refusing to overwrite existing snapshot.",
            file=sys.stderr,
        )
        return 0

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    tmp_path = OUTPUT_PATH.with_suffix(".json.tmp")
    serialized = json.dumps(payload, ensure_ascii=False, separators=(",", ":"))
    tmp_path.write_text(serialized + "\n", encoding="utf-8")
    os.replace(tmp_path, OUTPUT_PATH)
    print(f"[info] Atomically wrote {OUTPUT_PATH} ({len(serialized)} bytes).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
