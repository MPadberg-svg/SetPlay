# SetPlay

![Build APK](https://github.com/MPadberg-svg/SetPlay/actions/workflows/release-apk.yml/badge.svg)
[![Latest release](https://img.shields.io/github/v/release/MPadberg-svg/SetPlay?display_name=tag)](https://github.com/MPadberg-svg/SetPlay/releases/latest)

SetPlay is an Android IPTV player for phones with Xtream Codes account support,
Live TV, movies, series, favorites, EPG listings, search, parental controls,
and Media3 playback.

## Download

### One-click download

**[Download SetPlay.apk](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay.apk)**

The link always points to the newest published APK. You can also browse all
versions on the [Releases page](https://github.com/MPadberg-svg/SetPlay/releases).

### Install on Android

1. Download `SetPlay.apk` to your phone.
2. Open the file from your Downloads folder.
3. If prompted, allow your browser or file manager to install unknown apps.
4. Confirm **Install**, then open SetPlay.

The downloadable APK is debug-signed and intended for direct installation and
testing. It supports Android 7.0 (API 24) and newer. SetPlay does not provide
IPTV channels or subscriptions; only use services and content you are
authorized to access.

### Verify the download (optional)

Download the matching checksum file and compare it with the APK:

```bash
sha256sum -c SetPlay.apk.sha256
```

## Requirements

- Android 7.0 (API 24) or newer
- An Xtream Codes-compatible IPTV account and server
- Internet access

## Build locally

The project includes the Gradle wrapper and can be built from the repository root:

```bash
./gradlew assembleDebug --no-daemon
```

The resulting APK is located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To install it on a connected Android device with ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Automated releases

GitHub Actions builds the APK on every `v*` tag, validates the output, generates
a SHA-256 checksum, and attaches both files to a GitHub Release:

- `SetPlay.apk`
- `SetPlay.apk.sha256`

Running the workflow manually from the **Actions** tab creates a downloadable
workflow artifact without publishing a release.
