# SetPlay

Dual-Edition Android Phone & Android TV IPTV Cinema & Live Football Companion engineered for 250K+ Xtream catalogs.

[![Dual-APK Release](https://img.shields.io/github/actions/workflow/status/MPadberg-svg/SetPlay/android-apk-release.yml?label=Dual-APK%20Release&logo=githubactions&logoColor=white)](https://github.com/MPadberg-svg/SetPlay/actions/workflows/android-apk-release.yml)
[![Daily Cloud Index](https://img.shields.io/github/actions/workflow/status/MPadberg-svg/SetPlay/update-popularity-index.yml?label=Daily%20Cloud%20Index&logo=githubactions&logoColor=white)](https://github.com/MPadberg-svg/SetPlay/actions/workflows/update-popularity-index.yml)
[![Download SetPlay-Phone.apk](https://img.shields.io/badge/Download-SetPlay--Phone.apk-14919B?logo=android&logoColor=white)](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-Phone.apk)
[![Download SetPlay-TV.apk](https://img.shields.io/badge/Download-SetPlay--TV.apk-FFB800?logo=androidtv&logoColor=black)](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-TV.apk)

![Kotlin 2.2.10](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09.00-4285F4?logo=jetpackcompose&logoColor=white)
![Room 2.7.0](https://img.shields.io/badge/Room-2.7.0%20(v9)-3DDC84?logo=android&logoColor=black)
![Media3 1.5.1](https://img.shields.io/badge/Media3-1.5.1-0F9D58?logo=googleplay&logoColor=white)
![WorkManager 2.10.0](https://img.shields.io/badge/WorkManager-2.10.0-4285F4?logo=android&logoColor=white)
![Android SDK](https://img.shields.io/badge/Android%20SDK-Min%2024%20%7C%20Target%2036-34A853?logo=android&logoColor=white)

---

## Download & Dual-Edition Architecture

SetPlay compiles from a single codebase into two target-specific APKs selected at build time via `-PSETPLAY_TARGET` and exposed to runtime UI shells through `BuildConfig.DEVICE_EDITION` (`EditionResolver`).

| Capability | `SetPlay-Phone.apk` (`-PSETPLAY_TARGET=PHONE`) | `SetPlay-TV.apk` (`-PSETPLAY_TARGET=TV`) |
| :--- | :--- | :--- |
| **Target Devices** | Android Phones, Tablets & Foldables (API 24+) | Android TV, Google TV & Fire TV (API 24+) |
| **Manifest Config** | Launcher label `SetPlay`, orientation `unspecified` | Launcher label `SetPlay TV`, orientation `sensorLandscape` |
| **Navigation Shell** | Touch-first UI with floating "Liquid Prism Beam" morphing pill dock | 10-foot Collapsible Prism Glass Rail (`74dp <-> 236dp` expand-on-focus) with D-Pad focus rings |
| **Version Picker** | Modal bottom sheet for switching audio/language/quality editions | Right-side D-Pad-navigable Version Drawer |
| **Playback UX** | Draggable Picture-in-Picture (PiP) & touch swipe gestures | In-player Live TV Channel Zapper overlay & 2-stage Back exit guard |
| **Phone ↔ TV Sync** | Wi-Fi TV Companion controller (browse, cast, zap, pull back to phone) | Auto-started local Wi-Fi Sync Host (`ServerSocket` port `8765`, NSD `_setplay._tcp`) |
| **Direct Release** | [Download `SetPlay-Phone.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-Phone.apk) | [Download `SetPlay-TV.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-TV.apk) |

### Installation

- **Phone & Tablet**: Download [`SetPlay-Phone.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-Phone.apk), enable installation from your browser or file manager if prompted, and install.
- **Android TV / Google TV / Fire TV**: Transfer [`SetPlay-TV.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-TV.apk) via *Downloader*, *Send Files to TV*, or `adb install SetPlay-TV.apk`, then launch **SetPlay TV** from the TV home screen.

---

## Enterprise Catalog Engine, Sorting & Smart Sync (V17)

### 3-Level Movie Organization

1. **Level 1 — Lossless Duplicate Versions**: An O(N) Union-Find grouping pass bridges duplicate provider feeds using TMDb ID, a zero-regex TMDb poster hash parser, and normalized `(cleanTitle, year)` keys. 100% of provider `streamId` entries are preserved; only multi-version groups are materialized into the `vod_group_members` join table. Representative selection uses pre-compiled language regexes with deterministic priority (`English > Arabic > French > Multi-Sub > Unmarked Latin`), while the Version Picker organizes all editions by Language, Quality, and Container.
2. **Level 2 — Official Movie Franchises & Collections**: Independent installments (e.g., *James Bond*, *Mission: Impossible*, *Harry Potter*, *Lord of the Rings*) remain distinct catalog titles linked via `vod_collections`. Collections support **Release Order**, **Newest**, and **Top Rated** sorting plus an in-dossier franchise rail backed by a built-in offline seed of 25 iconic franchises and daily cloud collections from `docs/collections_index.json`.
3. **Level 3 — Standalone Movies**: Unmatched single-edition movies stay ungrouped with zero false-positive merging.

### Inverted Cloud Ranking ($0/month, Privacy-First)

- A daily GitHub Actions job runs `scripts/build_popularity_index.py` to publish `docs/popularity_index.json` and `docs/collections_index.json` via GitHub Pages.
- `GlobalPopularityIndexManager` and `GlobalCollectionsIndexManager` fetch these static indices with 24-hour ETag conditional caching and match them locally on-device against the user's synced catalog.
- No user playlists, M3U URLs, or Xtream credentials ever leave the device.

### Playback-Safe Single-Queue Smart Sync

- **Serialized Execution**: `CatalogSyncScheduler` coordinates all `LIVE`, `SERIES`, and `MOVIE` refreshes through a single serialized WorkManager queue (`CatalogSyncWorker`) and a global `catalogProcessingMutex`, preventing overlapping syncs from competing for CPU or SQLite locks.
- **Zero-Regex Fast Path**: `XtreamClient` computes a streaming FNV-1a `metadataHash` for every catalog entry. Unchanged items short-circuit before regex normalization or SQLite writes.
- **Non-Destructive Incremental Diffs**: `CatalogDiffEngine` applies in-place metadata updates so user favorites, watch history, and playback resume positions remain intact, enforces a 50% safe-removal guard against truncated upstream payloads, and persists checkpoint progress in `SyncSectionStateEntity` for crash recovery.
- **Background Update Checks**: `CatalogUpdateCheckWorker` runs a configurable 12-hour lightweight check before scheduling full diffs.
- **Network & Resource Isolation**: Dedicated `playbackOkHttpClient` and `syncOkHttpClient` pools paired with `PlaybackResourceGovernor` throttle background sync work whenever ExoPlayer is actively streaming.

### High-Scale Storage (Room v9)

- SQLite runs in WAL mode with `PRAGMA busy_timeout` configured and non-destructive schema migrations (`v3 → v9`).
- Full-text search uses Room FTS4 virtual tables; catalog shelves and "See All →" views use keyset cursor pagination for constant-time deep scrolling across 250K+ entries.
- Stage 2 catalog diffs commit atomically in a single Room transaction to prevent partial UI states.

---

## Core Product Modules

- **KickOff Arena & Match Center**: Multi-source live fixture and score aggregation (`API-Football`, `Football-Data.org`, `SportAPI.ai`, `TheSportsDB`, `ESPN`) with quota guards (`KickoffSourceMonitor`), regional broadcast zone and timezone mapping (`UserCountryZone`), automatic Live TV playlist channel matching, and a 2D tactical pitch for lineups and match stats.
- **Media3 Player HUD & Triple-Engine Subtitles**: ExoPlayer (`Media3 1.5.1`) HUD with 1-tap multi-feed quality switching (`4K UHD`, `FHD`, `HD`, `SD`), 15-minute halftime timer, audio boost, and parallel subtitle resolution across **Wyzie**, **SubDL** (in-memory `.zip` → `.srt`), and **OpenSubtitles v3** with live offset and font-scale controls.
- **Security & Privacy**: AES-GCM encryption at rest for saved Xtream credentials (`SecurityManager`), sensitive credential/token redaction in diagnostic logs, multi-layer adult content filtering, and Parental PIN lock.

---

## Architecture & Project Structure

For mandatory concurrency, finite-state-machine, priority, and lease/heartbeat invariants governing catalog synchronization, see **[`docs/TASK_ORCHESTRATION_RULES.md`](docs/TASK_ORCHESTRATION_RULES.md)**.

```text
SetPlay/
├── app/
│   ├── build.gradle.kts                     # Dual-edition (-PSETPLAY_TARGET=PHONE|TV), BuildConfig, Secrets plugin
│   └── src/
│       ├── main/java/com/example/
│       │   ├── MainActivity.kt              # Single-activity Compose host & edition shell router
│       │   ├── data/
│       │   │   ├── local/                   # StreamVaultDatabase (Room v9, WAL, FTS4), StreamVaultDao
│       │   │   ├── model/                   # Entities.kt, VodGroupingNormalizer, FTS & sync entities
│       │   │   ├── remote/                  # XtreamClient, XtreamDto, GlobalPopularityIndexManager,
│       │   │   │                            # GlobalCollectionsIndexManager, WyzieSubtitleService
│       │   │   ├── repository/              # IptvRepository, FootballRepository, MatchCenterModels,
│       │   │   │                            # MatchClockEngine, InternationalRankingResolver,
│       │   │   │                            # ProviderAuthorityEngine, KickoffSourceMonitor, UserCountryZone
│       │   │   └── sync/                    # CatalogTaskOrchestrator, CatalogTaskStateMachine,
│       │   │                                # CatalogSyncScheduler, CatalogSyncWorker, CatalogDiffEngine,
│       │   │                                # CatalogUpdateCheckWorker, PlaybackResourceGovernor,
│       │   │                                # SyncModels, TvSyncManager (port 8765 / _setplay._tcp)
│       │   ├── ui/
│       │   │   ├── components/              # Shared Prism glass components, Phone dock & TV rail
│       │   │   ├── favorites/               # Saved Live TV, Movies & Series vault
│       │   │   ├── home/                    # KickOff Arena & Match Center UI
│       │   │   ├── livetv/                  # Live TV browser, channel zapper & EPG views
│       │   │   ├── parental/                # Parental PIN lock & content filter controls
│       │   │   ├── player/                  # Media3 ExoPlayer HUD, subtitles & quality switcher
│       │   │   ├── playlists/               # Xtream & M3U playlist onboarding/management
│       │   │   ├── search/                  # FTS4 unified catalog search
│       │   │   ├── sync/                    # Phone ↔ TV Wi-Fi pairing & catalog sync status UI
│       │   │   ├── theme/                   # Material 3 themes, typography & atmosphere tokens
│       │   │   ├── viewmodel/               # Shared app & playback ViewModels
│       │   │   └── vod/                     # Movies/Series shelves, collections, Version Picker & TV UX
│       │   └── util/                        # EditionResolver, SecurityManager, StreamMetadataParser
│       └── test/java/com/example/           # ExampleRobolectricTest.kt, ExampleUnitTest.kt (89 tests)
├── docs/
│   ├── TASK_ORCHESTRATION_RULES.md          # Mandatory Room SSOT, FSM, priority & lease orchestration rules
│   ├── popularity_index.json                # Daily GitHub Pages global popularity index
│   └── collections_index.json               # Daily GitHub Pages official movie collections index
├── gradle/
│   └── libs.versions.toml                   # Centralized Gradle version catalog
├── scripts/
│   └── build_popularity_index.py            # TMDb index generator for popularity & collections JSON
└── .github/workflows/
    ├── android-apk-release.yml              # Unit tests + Phone/TV APK build & GitHub Release publish
    └── update-popularity-index.yml          # Daily 04:00 UTC cloud index builder
```

---

## Local Development, Testing & CI/CD

### Prerequisites

- **JDK 17** (project compiles targeting `JavaVersion.VERSION_11`)
- **Android SDK** (`compileSdk 36.1`, `minSdk 24`, `targetSdk 36`)

### Build & Test Commands

```bash
# Run the full 89-test JUnit + Robolectric suite (Room, Union-Find, DiffEngine, Orchestrator, WorkManager, UI)
./gradlew :app:testDebugUnitTest

# Build the Phone & Tablet edition (default)
./gradlew assembleDebug -PSETPLAY_TARGET=PHONE

# Build the Android TV / Google TV / Fire TV 10-foot edition
./gradlew assembleDebug -PSETPLAY_TARGET=TV
```

### GitHub Actions Workflows

| Workflow | File | Triggers | Responsibility |
| :--- | :--- | :--- | :--- |
| **Build & Release SetPlay APK** | `.github/workflows/android-apk-release.yml` | Push (`**`, tags `v*`), PR (`main`, `master`), manual `workflow_dispatch` (`both` / `phone` / `tv`) | Runs `:app:testDebugUnitTest`, builds `SetPlay-Phone.apk` and `SetPlay-TV.apk`, uploads 90-day artifacts, and publishes to GitHub Releases. |
| **Update Global Popularity Index** | `.github/workflows/update-popularity-index.yml` | Daily cron (`0 4 * * *` UTC), manual `workflow_dispatch` | Runs `python3 scripts/build_popularity_index.py` and commits updated `docs/popularity_index.json` and `docs/collections_index.json` to `main`. |

### Optional Secrets

Both APKs build and run cleanly with **zero secrets configured** — unset keys fall back to safe placeholders via the Secrets Gradle Plugin (`2.0.1`), and core IPTV playback, KickOff free sources, and OpenSubtitles v3 remain functional. For optional enrichment in local `.env` or GitHub Repository Secrets:

| Secret | Purpose |
| :--- | :--- |
| `TMDB_API_KEY` | TMDb movie/series metadata enrichment, external ID lookup, and `update-popularity-index.yml` index generation |
| `API_FOOTBALL_KEY` | Official lineups, player cutouts, and match statistics (`v3.football.api-sports.io`) |
| `FOOTBALL_DATA_KEY` | European and international fixtures and standings (`api.football-data.org/v4`) |
| `WYZIE_API_KEY` | Wyzie multi-source subtitle search (`sub.wyzie.ru`) |
| `SUBDL_API_KEY` | SubDL archive subtitle search (`api.subdl.com`) |
| `SPORTAPI_AI_KEY` | Optional SportAPI.ai football data source (`SPORT_API_KEY` alias supported) |
