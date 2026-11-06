<div align="center">

# ⚽ Set `▶` **Play** 🎬
### *The Luxury Dual-Mode (Phone + Android TV) MENA Sports Match Center & High-Scale Xtream IPTV Cinema Suite*

[![Build & Release SetPlay APK](https://img.shields.io/github/actions/workflow/status/MPadberg-svg/SetPlay/android-apk-release.yml?style=for-the-badge&logo=githubactions&logoColor=white&label=APK%20Release)](https://github.com/MPadberg-svg/SetPlay/actions/workflows/android-apk-release.yml)
[![Download SetPlay APK](https://img.shields.io/badge/📲_Download_APK-SetPlay.apk-14919B?style=for-the-badge&logo=android&logoColor=white)](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay.apk)
[![Android 7.0+ Phone & TV](https://img.shields.io/badge/Android_Phone_%26_TV-API_24%2B-80EE98?style=for-the-badge&logo=androidtv&logoColor=06131C)](#)
[![Kotlin & Jetpack Compose](https://img.shields.io/badge/Kotlin_2.0-Jetpack_Compose_M3-F07167?style=for-the-badge&logo=jetpackcompose&logoColor=white)](#)

---

**Set ▶ Play** is a next-generation Android & Android TV streaming and live football companion engineered for extreme catalog scale (**~289,000+ Live TV, Movies & Series entries**) and **real-time MENA & Global Football Broadcast Intelligence**. Crafted with a 5-atmosphere dark luxury design system inspired by **Apple Sports**, **FotMob**, and **DAZN**.

[**📲 Download & Install APK**](#-download--install-setplay-apk) • [**⚽ KickOff Arena & Match Center 2.0**](#-kickoff-arena--match-center-20) • [**🎬 Cinema, Subtitles & Media3 HUD**](#-cinema-subtitles--media3-hud) • [**📺 Android TV & Security**](#-dual-mode-android-tv--security) • [**⚙️ CI/CD & Secrets**](#️-github-actions-cicd--secrets)

</div>

---

## 📲 Download & Install SetPlay APK

Download and install **Set ▶ Play** onto any Android phone, tablet, foldable, or Android TV / Google TV box:

<div align="center">

| 🚀 **Option 1: GitHub Release APK (Recommended)** | 📦 **Option 2: GitHub Actions Artifact** |
| :---: | :---: |
| [**⬇️ Download `SetPlay.apk` (Latest Release)**](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay.apk) | [**⚙️ Download `SetPlay-APK` from Actions**](https://github.com/MPadberg-svg/SetPlay/actions/workflows/android-apk-release.yml) |
| *Automatically built & published on every push to `main` or `v*` tag* | *Download `SetPlay-APK` artifact from any workflow run* |

</div>

### 🛠️ Quick Installation Steps on Android & Android TV
1. Download **[`SetPlay.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay.apk)** on your Android phone, tablet, or Android TV box.
2. Open the downloaded **`.apk`** file from your browser notification or file manager.
3. If prompted *"For your security, your phone currently isn't allowed to install unknown apps from this source"*, tap **Settings** and toggle **Allow from this source** ON.
4. Tap **Install** and launch **Set ▶ Play** from your phone home screen or Android TV Leanback launcher!

---

## ⚽ KickOff Arena & Match Center 2.0

- **🔄 Multi-Source Real-Time Live Score Engine (`SportAPI.ai` + `TheSportsDB` + `ESPN` + `API-Football`)**
  Background coroutine sync automatically updates live scores, halftime transitions (`HT`), stoppage time (`90+4'`), red cards, and goal scorers with a **30 req/min rate limiter** for TheSportsDB v1 and tabular numeral (`tnum`) score alignment.
- **🌍 User Country Zone & Seasonal Timezone Engine**
  Select your broadcast zone (`Morocco MA`, `Qatar QA`, `Saudi Arabia SA`, `UAE AE`, `Egypt EG`, `Algeria DZ`, `Tunisia TN`, `France FR`, `Spain ES`, `UK GB`, `Germany DE`, `Italy IT`, `USA US`) with automatic Winter/Summer (`GMT+0` / `GMT+1` / `CET` / `CEST`) kickoff time conversion.
- **📡 Official TV Channel Resolution (`Sofascore` + `TheSportsDB` + Live Playlist Match Scanner)**
  Resolves official regional broadcasters (`beIN Sports HD 1..7`, `beIN 4K`, `Arryadia TNT HD`, `SSC 1 HD`, `AD Sports Premium 1`, `Canal+ Foot`, `Movistar Liga de Campeones`, `TNT Sports`) and cross-references your synced Live TV playlist for channels explicitly naming the fixture.
- **🏟️ 2-Zone Flashscore Match Cards & Featured Clash Hero**
  Clean 2-zone match cards with `28.dp` unclipped club crests, tabular scores, and a single bottom row pairing the official broadcaster pill with a 1-tap `▶ Watch` CTA.
- **📋 Match Center Dossier 2.0 & 2D Tactical Pitch**
  Tap any match card to open a 5-tab interactive dossier (**Summary**, **Lineups**, **Stats**, **H2H & Table**, **TV Guide**):
  - **Real Player Cutout Photos**: Resolves player cutouts from `TheSportsDB` (`lookup_all_players.php` + `searchplayers.php`) and `Sofascore` (`api.sofascore.com/api/v1/player/{id}/image`) with crisp jersey-number fallback badges.
  - **Home XI / Away XI / Full Pitch Toggle**: Inspect 11v11 full-pitch formations or switch to spacious single-team half-pitch tactical views (`Home XI` / `Away XI`) with zero name truncation.

---

## 🎬 Cinema, Subtitles & Media3 HUD

- **🗣️ Automatic Multi-Language Subtitle Studio (`Wyzie Subs` + `OpenSubtitles v3`)**
  - Automatically prompts **"Subtitles — Choose Your Language"** (`🇸🇦 Arabic`, `🇬🇧 English`, `🇫🇷 Français`, `🇪🇸 Español`, `🇹🇷 Türkçe`, `🚫 Off`) when starting any Movie or Series.
  - Combines **Wyzie Subs** (`sub.wyzie.ru` / `sub.wyzie.io`) with a zero-key **OpenSubtitles v3** fallback (`opensubtitles-v3.strem.io`) and Xtream `tmdb_id` resolution so subtitles work even without a third-party API key.
  - Includes live subtitle sync offset controls (`-0.5s` / `+0.5s` / `Reset`) and font-size scaling (`80%`–`150%`).
- **🎛️ 3-Zone Media3 Player HUD & Sports Broadcast Tools**
  - **Zone 1 (Top Glass Bar)**: Back, clean stream title, resolution badge (`4K UHD` / `1080p FHD` / `HD`), audio codec badge (`AAC`, `EAC3+`, `AC4`, `DTS`), Favorite star, and PiP trigger.
  - **Zone 2 (Center Canvas)**: Play/Pause, `-10s` / `+10s` seek, double-tap ripple seek, and vertical brightness/volume (up to **200% VLC Audio Boost**) swipe pills.
  - **Zone 3 (Bottom Dock)**: Scrubber bar (`VOD`) or **1-Tap Multi-Feed Quality Switcher** (`4K UHD` • `FHD` • `HD` • `SD` sibling feeds), **15-Minute Halftime Explorer Timer** (`⏱ 15m HT` with automatic 2nd-half return banner), and **In-Player Live Goal Alerts** with 1-tap `"Watch Goal"`.
- **🎞️ IMAX Movie Dossier & Per-Episode Series Resume**
  - Tap any movie poster to open the **IMAX Movie Dossier** (`get_vod_info` plot synopsis, cast, director, release year, runtime, rating, and `▶ Watch Now`).
  - Independent per-episode Series resume tracking keyed by `(playlistId, episodeId)` with episode progress bars, `"Resume • Xm left"` badges, and a root **"Continue Watching"** rail.

---

## 🎨 Five Bespoke Luxury Design Worlds

| Section | Signature Atmosphere | Visual Highlights |
| :--- | :--- | :--- |
| **⚽ KickOff** | **Deep Ocean & Tropical Teal**<br>`#06141D` • `#14919B` • `#F07167` | Sticky Matchday Scrubber Deck, Featured Clash spotlight, 2-Zone Flashscore Match Cards |
| **📺 Live TV** | **Broadcast Control Room**<br>`#1F313B` • `#383852` • `#BE4039` | `36.dp` glass telemetry bar, progressive infinite-scroll (`+180` on scroll), instant channel zapper |
| **🎬 Movies** | **Sapphire & Mint Cinema 4K**<br>`#00385A` • `#01929F` • `#6AECE1` | Edge-lit IMAX poster cards, authentic parsed quality/language badges, IMAX Movie Dossier |
| **🎞️ Series** | **Cypress & Ice-Aqua Vault**<br>`#071412` • `#2EC4B6` • `#80FFDB` | Multi-season hierarchy explorer, per-episode resume progress bars, Continue Watching rail |
| **⭐ Favorites** | **Botanical Emerald & Sage Vault**<br>`#051F20` • `#163832` • `#8EB69B` | Instant filtering across saved Live TV channels, Movies, and Series |

---

## 📺 Dual-Mode Android TV & Security

- **📺 Single-APK Phone + Android TV (10-Foot Leanback UI)**
  - Native `LEANBACK_LAUNCHER` declaration in `AndroidManifest.xml` with optional touchscreen/leanback hardware features.
  - Reusable `Modifier.luxuryFocusableCard` D-Pad focus choreography (`1.04f` spring scale lift + `2.dp` illuminated `AmberGold` border) and full TV remote keybinding support (`DPAD_CENTER`, `CHANNEL_UP/DOWN`, `MEDIA_PLAY_PAUSE`, `MEDIA_REWIND/FAST_FORWARD`).
- **🔐 Hardware-Backed AES-256-GCM & Salted SHA-256 Parental Controls**
  - Encrypts Xtream server credentials at rest using Android Keystore (`AES/GCM/NoPadding`).
  - Protects **Live TV**, **Movies**, and **Series** categories behind a salted SHA-256 4-digit PIN gate with brute-force lockout protection and automatic lock restoration across playlist re-syncs.
- **⚡ Shared Singleton Repository & 289K Progressive Paging**
  - Thread-safe `IptvRepository.getInstance(context)` singleton backed by a shared HTTP/2 `OkHttpClient` connection pool (`10` idle connections) and deterministic `ORDER BY id ASC LIMIT :limit` infinite-scroll Room queries.

---

## ⚙️ GitHub Actions CI/CD & Secrets

The workflow in [`.github/workflows/android-apk-release.yml`](.github/workflows/android-apk-release.yml) automatically builds `SetPlay.apk` and publishes it to **GitHub Releases** on every push.

### Optional GitHub Repository Secrets
The APK compiles cleanly out-of-the-box even with **zero secrets configured** (falling back to safe placeholder constants in `BuildConfig` and free zero-key APIs like `TheSportsDB`, `Sofascore`, `ESPN`, and `OpenSubtitles v3`). To enable optional premium APIs in your GitHub Actions builds, go to **Settings → Secrets and variables → Actions** and add any of:
- `SPORT_API_KEY` (or `SPORTAPI_AI_KEY`) — Optional `SportAPI.ai` key for supplementary live score polling.
- `WYZIE_API_KEY` — Optional `Wyzie Subs` API key (automatically falls back to `OpenSubtitles v3` when omitted).
- `API_FOOTBALL_KEY` — Optional `API-Football` key.

---

<div align="center">

Crafted with precision for football fans & home cinema enthusiasts • **[Download `SetPlay.apk`](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay.apk)**

</div>
