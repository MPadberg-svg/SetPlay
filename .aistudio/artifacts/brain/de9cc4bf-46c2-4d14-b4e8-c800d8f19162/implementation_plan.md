# SetPlay — Icon Monogram, Section Design Identities & Security Hardening Plan

## 1. SetPlay Launcher Icon (`S` + Play-Prism `P` Monogram)
- **Visual Concept**:
  - Generate a custom adaptive launcher icon foreground (`ic_setplay_monogram_fg`) on a solid **Dark Graphite (`#141619`)** background featuring a 3D metallic **Amber Gold (`#D4A238`)** letter **`S`** interlocking with a sculpted letter **`P`** whose upper loop transforms into a glowing **Pale Gold (`#F6E8C8`)** Play prism (`▶`) radiating subtle broadcast signal waves.
- **Adaptive & Legacy Launcher Integration**:
  - Pin `#141619` in `app/src/main/res/drawable/ic_launcher_background.xml` and center the monogram inside the 66dp safe zone in `app/src/main/res/drawable/ic_launcher_foreground.xml`.
  - Generate crisp PNG assets across all five mipmap densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) for both `ic_launcher.png` and circularly masked `ic_launcher_round.png`.
  - Update the top app bar wordmark in `MainActivity.kt` so the **`P`** in `SetPlay` integrates a custom Play-prism silhouette matching the new launcher icon.

---

## 2. Section-Specific Colors & Tailored Layouts (Respecting the Royal Gold Brand Kit)
All sections retain the core **Royal Gold** foundation (`DarkGraphite #141619` background, `MetalCharcoal #25282E` surfaces, `AmberGold #D4A238` primary accents, `PaleGold #F6E8C8` titles, `Champagne #DFC69E` secondary text) while introducing distinct section identities:

### A. Live TV — **Crimson Gold Broadcast Studio**
- **Signature Palette**:
  - `LiveCrimson` (`#E63946`), `LiveCrimsonGlow` (`#3B171E`), `LiveCoralAccent` (`#FF8A80`), paired with `AmberGold` (`#D4A238`).
- **Tailored Design Touches (`LiveTvScreen.kt`)**:
  - **Broadcast Studio Header Banner**: Displays a pulsing Crimson-Gold `"LIVE ON AIR"` status pill, total active channels counter, and a subtle crimson-gold radial aura.
  - **Category Cards**: Left crimson-gold vertical accent bar, broadcast wave watermark badge, and crimson-gold channel count pill.
  - **Channel Rows**: Pulsing crimson dot inside a `"LIVE • RAW"` badge, dual-tone Crimson-to-Gold border on hovered/active channels, and an Amber Gold play button with a crimson outer ring.

### B. Movies — **Ruby Cinema Gold Premiere**
- **Signature Palette**:
  - `CinemaRuby` (`#C1121F`), `CinemaVelvetSurface` (`#33151B`), `CinemaMarqueeGold` (`#E5A93C`), `CinemaSpotlight` (`#FFD6A5`).
- **Tailored Design Touches (`VodScreen.kt` when `isSeries = false`)**:
  - **Cinema Premiere Header**: Features a `"CINEMA PREMIERE"` marquee badge, widescreen spotlight gradient header card, and Ruby-Gold filter chips.
  - **Movie Poster Cards**: Framed with a subtle Ruby-Gold border, a top-left `"4K CINEMA"` velvet-ruby ribbon, a star rating pill in Marquee Gold, and a bottom `"WATCH MOVIE"` action bar on the poster scrim.

### C. Series — **Emerald Royal Gold Binge Vault**
- **Signature Palette**:
  - `SeriesEmerald` (`#1B998B`), `SeriesJadeAccent` (`#2EC4B6`), `SeriesDeepVault` (`#132C2A`), paired with `AmberGold` (`#D4A238`).
- **Tailored Design Touches (`VodScreen.kt` when `isSeries = true`)**:
  - **Binge Series Header**: Features an `"EPISODIC VAULT"` Emerald-Gold header banner and Emerald-rimmed category chips.
  - **Layered Series Cards**: Visual stacked-deck top edge indicating multi-season depth, an Emerald-Gold `"SERIES • MULTI-SEASON"` badge, and an `"EXPLORE SEASONS"` action bar.
  - **Series Detail View**: Emerald-Gold accented horizontal Season selector chips (`Season 1`, `Season 2`, ...) above Episode cards with Emerald-Gold episode number badges and play triggers.

### D. Adaptive Navigation Bar Accents (`MainActivity.kt`)
- The Bottom Navigation Bar subtly shifts its active indicator border and icon glow to match the selected section (`AmberGold` for Home & Favorites, `LiveCrimson + AmberGold` for Live TV, `CinemaRuby + MarqueeGold` for Movies, and `SeriesEmerald + AmberGold` for Series).

---

## 3. Security Review & Code Hardening

### A. Android Keystore AES-GCM Credential Encryption & Salted PIN Hashing (`SecurityManager.kt`)
- **IPTV Password Encryption at Rest**:
  - Create `com.example.util.SecurityManager` backed by Android Keystore (`AES/GCM/NoPadding`, 256-bit key with ` fallback` support for local JVM/Robolectric unit tests).
  - Encrypt `PlaylistEntity.password` with an `"ENC:v1:"` prefix before saving to Room in `IptvRepository`, and transparently decrypt only in memory when authenticating or constructing playback URLs. Automatically migrate any existing plaintext password rows on read/sync.
- **Parental PIN Salted Hashing & Brute-Force Protection**:
  - Hash Parental Control PINs using salted `SHA-256` (`"HASH:v1:<salt>:<digest>"`) with constant-time comparison (`MessageDigest.isEqual`).
  - Enforce a 5-attempt rate limiter with a 30-second cooldown timer to prevent PIN brute-forcing.

### B. Strict URL Sanitization, HTTPS-First Fallback & Log Redaction (`XtreamClient.kt`)
- **`HttpUrl.Builder` Construction**:
  - Replace all raw string interpolations (`"$cleanUrl/player_api.php?username=$username&password=$password"`) with OkHttp `HttpUrl.Builder` (`addQueryParameter` and `addPathSegment`) so special characters in credentials or stream parameters cannot cause query/path injection.
- **Input & Extension Validation**:
  - Restrict server URLs strictly to `http://` and `https://` schemes, rejecting `file://`, `javascript:`, or malformed hosts.
  - Sanitize `containerExtension` against path traversal by allowing only safe alphanumeric media extensions (`ts`, `mp4`, `mkv`, `m3u8`, `avi`, `flv`).
  - When a user enters a bare hostname without a scheme, attempt `https://` first during authentication and fall back cleanly to `http://` if the IPTV provider only supports HTTP.
- **Credential Redaction in Logs & Errors**:
  - Add `SecurityManager.redactSensitiveText()` to strip `username`, `password`, and credentialed path segments from all Logcat warnings and user-facing error messages.

### C. Backup & Data Extraction Protection (`AndroidManifest.xml`, `backup_rules.xml`, `data_extraction_rules.xml`)
- Set `android:allowBackup="false"` in `AndroidManifest.xml` and configure `<exclude domain="database" path="streamvault_database.db"/>` in both `backup_rules.xml` and `data_extraction_rules.xml` so encrypted databases and keys are never exported to cloud backups or ADB transfers.
