<div align="center">

# ⚽ Set `▶` **Play** 🎬
### *The Luxury Dual-APK (Phone + Android TV 10-Foot) MENA Sports Match Center & High-Scale Xtream IPTV Cinema Suite*

[![Build & Release SetPlay APK](https://img.shields.io/github/actions/workflow/status/MPadberg-svg/SetPlay/android-apk-release.yml?style=for-the-badge&logo=githubactions&logoColor=white&label=Dual-APK%20Release)](https://github.com/MPadberg-svg/SetPlay/actions/workflows/android-apk-release.yml)
[![Download SetPlay-Phone.apk](https://img.shields.io/badge/📱_Phone_APK-SetPlay--Phone.apk-14919B?style=for-the-badge&logo=android&logoColor=white)](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-Phone.apk)
[![Download SetPlay-TV.apk](https://img.shields.io/badge/📺_Android_TV_APK-SetPlay--TV.apk-FFB800?style=for-the-badge&logo=androidtv&logoColor=06131C)](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-TV.apk)
[![Kotlin & Jetpack Compose](https://img.shields.io/badge/Kotlin_2.0-Jetpack_Compose_M3-F07167?style=for-the-badge&logo=jetpackcompose&logoColor=white)](#)

---

**Set ▶ Play** is a next-generation Android & Android TV streaming and live football companion engineered for extreme catalog scale (**~289,000+ Live TV, Movies & Series entries**) and **real-time MENA & Global Football Broadcast Intelligence**. Crafted with a 5-atmosphere dark luxury design system inspired by **Apple Sports**, **FotMob**, and **DAZN**, and published as two specialized editions sharing 100% of the core engine: **`SetPlay-Phone.apk`** and **`SetPlay-TV.apk`**.

[**📲 Download Phone & TV APKs**](#-download--install-setplay-phone--tv-apks) • [**📺 SetPlay-TV 10-Foot Edition & Wi-Fi Sync**](#-setplay-tv-10-foot-living-room-edition--wi-fi-sync) • [**⚽ KickOff Arena & Match Center 3.0**](#-kickoff-arena--match-center-30) • [**🎬 Cinema, Triple-Engine Subtitles & HUD**](#-cinema-triple-engine-subtitles--media3-hud) • [**⚙️ CI/CD & Manual Builds**](#️-github-actions-cicd--on-demand-builds)

</div>

---

## 📲 Download & Install SetPlay Phone & TV APKs

Download the specialized **Set ▶ Play** APK built for your device (**Android 7.0+ / API 24+**):

<div align="center">

| Edition | Target Devices & UX Shell | 🚀 Direct 1-Click Release Download | 📦 GitHub Actions Artifact |
| :--- | :--- | :---: | :---: |
| 📱 **`SetPlay-Phone.apk`**<br>*(Touch & Companion Edition)* | **Android Phones, Tablets & Foldables**<br>Top/Bottom Sculpted Glass Nav • Draggable Floating PiP • `52.dp` Wi-Fi TV Companion Bar | [**⬇️ Download `SetPlay-Phone.apk`**](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-Phone.apk) | `SetPlay-Phone-APK` |
| 📺 **`SetPlay-TV.apk`**<br>*(10-Foot Living Room Edition)* | **Android TV, Google TV & Fire TV**<br>`sensorLandscape` • Left Glass Navigation Rail • In-Player Channel Zapper Sidebar • 2-Stage Back Guard | [**⬇️ Download `SetPlay-TV.apk`**](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-TV.apk) | `SetPlay-TV-APK` |

</div>

### 🛠️ Quick Installation Steps
1. **On Android Phones & Tablets**: Download **[`SetPlay-Phone.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-Phone.apk)**, open the `.apk` file, allow installation from your browser/file manager if prompted, and tap **Install**.
2. **On Android TV / Google TV / Fire TV**: Download **[`SetPlay-TV.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-TV.apk)** using *Downloader*, *Send Files to TV*, or your TV browser, install it, and launch **SetPlay TV** directly from your TV home screen!

---

## 📺 SetPlay-TV 10-Foot Living Room Edition & Wi-Fi Sync

- **🧭 Widescreen Left Glass Navigation Rail (`TvLeftNavigationRail`)**
  - Replaces mobile top/bottom bars on `SetPlay-TV.apk` with a permanent `216.dp` left navigation rail featuring high-contrast D-Pad focus rings (`AmberGold` border + `1.04f` spring scale lift).
  - Provides instant remote access to **KickOff**, **Live TV**, **Movies**, **Series**, **Favorites**, **Search**, **Phone ↔ TV Sync**, **Playlists**, and **Parental Lock**.
- **📋 In-Player Live TV Channel Zapper Sidebar (`TvLiveChannelZapperOverlay`)**
  - Press **`D-Pad Left`** while watching any Live TV stream (or click the **`📋 Channels`** pill in the player HUD) to slide out a translucent `310.dp` channel switcher right over the live video—complete with instant search filtering, quality badges, and 1-click channel zapping without interrupting playback.
- **🛡️ 2-Stage TV Back-Button Exit Guard**
  - Pressing `Back` on your TV remote from a non-home tab returns to **KickOff**; pressing `Back` on the root screen displays a non-intrusive `"Press Back again to exit SetPlay TV"` toast banner (`2.5s` window) so accidental remote presses never close your session.
- **📱⇄📺 Local Wi-Fi Phone ↔ TV QR Companion & Sync Mode (`TvSyncManager`)**
  - **Auto-Ready on TV Boot**: `SetPlay-TV.apk` automatically starts the local Wi-Fi Sync Host (`ServerSocket` on port `8765` + `NsdManager` `_setplay._tcp` discovery) and displays its live **6-Digit Pairing PIN** right in the bottom pod of the TV Left Navigation Rail.
  - **1-Tap `Phone ⇄ TV` Handover (`max_connections = 1` Safe)**: Scan the TV QR code or enter the 6-digit PIN from `SetPlay-Phone.apk`. Browse 289,000+ channels/movies on your phone while the TV plays fullscreen undisturbed (*Silent Browse Mode*), beam any stream via **`[ 📺 Send to TV ]`**, zap Live TV channels with **`[ ⏮ ]` / `[ ⏭ ]`**, or pull the stream back to your phone with **`[ 📱 Watch Here ]`** (enforcing a `300ms` single-connection handover guard).

---

## ⚽ KickOff Arena & Match Center 3.0

- **🔄 Multi-Source Real-Time Live Score Engine (`API-Football` + `Football-Data.org` + `SportAPI.ai` + `TheSportsDB` + `ESPN`)**
  Smart quota-managed live polling (`80/100` daily cap with `15` calls reserved for 1-call Match Center Dossiers on `API-Football`, `10 req/min` sliding window for `Football-Data.org`, and `30 req/min` shared limiter for `TheSportsDB`) keeps live scores, match minutes (`HT`, `90+4'`), and red cards accurate with zero fabricated events.
- **🌍 User Country Zone & Seasonal Timezone Engine**
  Select your broadcast zone (`Morocco MA`, `Qatar QA`, `Saudi Arabia SA`, `UAE AE`, `Egypt EG`, `Algeria DZ`, `Tunisia TN`, `France FR`, `Spain ES`, `UK GB`, `Germany DE`, `Italy IT`, `USA US`) with automatic kickoff time conversion.
- **📡 Official TV Channel Resolution (`Sofascore` + `TheSportsDB` + Live Playlist Match Scanner)**
  Resolves official regional broadcasters (`beIN Sports HD 1..7`, `beIN 4K`, `Arryadia TNT HD`, `SSC 1 HD`, `AD Sports Premium 1`, `Canal+ Foot`, `Movistar Liga de Campeones`, `TNT Sports`) and cross-references your synced Live TV playlist for 1-tap `▶ Watch` launch.
- **📋 Match Center Dossier 3.0 & 2D Tactical Pitch**
  Tap any match card to open a 5-tab interactive dossier (**Summary**, **Lineups**, **Stats**, **H2H & Table**, **TV Guide**) featuring official 1-call `API-Football` lineups, real player cutout photos (`media.api-sports.io`, `TheSportsDB`, and header-authenticated `Sofascore`), and interactive **Home XI / Away XI / Full Pitch** views.

---

## 🎬 Cinema, Triple-Engine Subtitles & Media3 HUD

- **🗣️ Triple-Engine Multi-Language Subtitle Studio (`Wyzie` + `SubDL` + `OpenSubtitles v3`)**
  - Resolves Movie & Series `imdb_id` (`tt...`) and `tmdb_id` via Xtream metadata → **`TMDB API`** (`/3/search/{movie|tv}` + `/external_ids`) → **IMDb Instant Suggestion** fallback.
  - Queries **Wyzie Subs**, **SubDL** (with automatic in-memory `.zip` → `.srt` extraction), and **OpenSubtitles v3** in parallel across `🇸🇦 Arabic`, `🇬🇧 English`, `🇫🇷 Français`, `🇪🇸 Español`, and `🇹🇷 Türkçe`.
  - Auto-displays the **Subtitle Language Prompt** on VOD start with remembered **`★ Preferred`** language highlight, 12-second auto-hide countdown, live sync offset (`-0.5s` / `+0.5s`), and font scaling (`80%`–`150%`).
- **🎛️ 3-Zone Media3 Player HUD & Sports Broadcast Tools**
  - **Zone 1 (Top Glass Bar)**: Back, clean stream title, resolution badge (`4K UHD` / `1080p FHD` / `HD`), audio codec badge, Favorite star, and PiP trigger.
  - **Zone 2 (Center Canvas)**: Play/Pause, `-10s` / `+10s` seek, double-tap ripple seek, and vertical brightness/volume (up to **200% VLC Audio Boost**) swipe pills.
  - **Zone 3 (Bottom Dock)**: Scrubber bar (`VOD`) or **1-Tap Multi-Feed Quality Switcher** (`4K UHD` • `FHD` • `HD` • `SD`), **📋 Channels Zapper Drawer**, **15-Minute Halftime Explorer Timer** (`⏱ 15m HT`), and **In-Player Live Goal Alerts**.

---

## 🎨 Five Bespoke Luxury Design Worlds

| Section | Signature Atmosphere | Visual Highlights |
| :--- | :--- | :--- |
| **⚽ KickOff** | **Deep Ocean & Tropical Teal**<br>`#06141D` • `#14919B` • `#F07167` | Sticky Matchday Scrubber Deck, Featured Clash spotlight, 2-Zone Flashscore Match Cards |
| **📺 Live TV** | **Broadcast Control Room**<br>`#1F313B` • `#383852` • `#BE4039` | `36.dp` glass telemetry bar, progressive infinite-scroll (`+180` on scroll), instant channel zapper |
| **🎬 Movies** | **Sapphire & Mint Cinema 4K**<br>`#00385A` • `#01929F` • `#6AECE1` | Edge-lit IMAX poster cards, authentic parsed quality/language badges, TMDB-enriched IMAX Movie Dossier |
| **🎞️ Series** | **Cypress & Ice-Aqua Vault**<br>`#071412` • `#2EC4B6` • `#80FFDB` | Multi-season hierarchy explorer, per-episode resume progress bars, Continue Watching rail |
| **⭐ Favorites** | **Botanical Emerald & Sage Vault**<br>`#051F20` • `#163832` • `#8EB69B` | Instant filtering across saved Live TV channels, Movies, and Series |

---

## ⚙️ GitHub Actions CI/CD & On-Demand Builds

The workflow in [`.github/workflows/android-apk-release.yml`](.github/workflows/android-apk-release.yml) uses Gradle build caching (`actions/setup-java@v4` with `cache: 'gradle'`) to build and publish both **`SetPlay-Phone.apk`** (`-PSETPLAY_TARGET=PHONE`) and **`SetPlay-TV.apk`** (`-PSETPLAY_TARGET=TV`) to **GitHub Releases** on every push.

### 🚀 Running a Manual Build (`both`, `phone`, or `tv`)
You can trigger a fast on-demand build for a single edition or both editions at any time:
1. Open the repository on GitHub and click the **[Actions](https://github.com/MPadberg-svg/SetPlay/actions/workflows/android-apk-release.yml)** tab.
2. Select **Build & Release SetPlay APK** in the left sidebar.
3. Click **Run workflow** on the right and choose your target from the **`Which APK edition to build`** dropdown:
   - **`both`** *(default)* — Builds and publishes both `SetPlay-Phone.apk` and `SetPlay-TV.apk`.
   - **`phone`** — Builds only `SetPlay-Phone.apk` (`-PSETPLAY_TARGET=PHONE`) for rapid mobile testing.
   - **`tv`** — Builds only `SetPlay-TV.apk` (`-PSETPLAY_TARGET=TV`) for rapid Android TV testing.

### 🔑 Optional GitHub Repository Secrets
Both APKs compile cleanly out-of-the-box even with **zero secrets configured** (falling back to safe `YOUR_*` placeholders for Secrets Gradle Plugin 2.0.1 and free zero-key APIs like `TheSportsDB`, `Sofascore`, `ESPN`, `IMDb Suggestion`, and `OpenSubtitles v3`). To enable optional API keys in CI builds, go to **Settings → Secrets and variables → Actions** and add any of:
- `API_FOOTBALL_KEY` — `v3.football.api-sports.io` key for 1-call official lineups, `media.api-sports.io` player cutouts, and match stats.
- `FOOTBALL_DATA_KEY` — `api.football-data.org/v4` key for European & international fixtures and standings.
- `TMDB_API_KEY` — `api.themoviedb.org/3` key for instant `imdb_id` / `tmdb_id` resolution and 4K IMAX Movie Dossier enrichment.
- `WYZIE_API_KEY` — `sub.wyzie.ru` API key for multi-source subtitles.
- `SUBDL_API_KEY` — `api.subdl.com` API key for high-coverage Arabic/English/French/Spanish/Turkish `.zip` subtitle archives.
- `SPORT_API_KEY` / `SPORTAPI_AI_KEY` — Optional `SportAPI.ai` key.

---

<div align="center">

Crafted with precision for football fans & home cinema enthusiasts • **[📱 Download `SetPlay-Phone.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-Phone.apk)** • **[📺 Download `SetPlay-TV.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay-TV.apk)**

</div>
