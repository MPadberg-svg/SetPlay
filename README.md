# SetPlay

## Download

**[Download SetPlay.apk](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/SetPlay.apk)**

Or browse all versions on the [Releases page](https://github.com/MPadberg-svg/SetPlay/releases).

1. Download `SetPlay.apk` to your Android phone.
2. Open the downloaded file and allow your browser or file manager to install
   apps from unknown sources if Android asks.
3. Install and open SetPlay.

SetPlay is an Android IPTV player for phones. It supports Xtream Codes accounts,
multiple saved accounts, Live TV, movies, series, favorites, EPG listings, search,
parental controls, and Media3 playback.

The downloadable APK is debug-signed and intended for direct installation and
testing. It supports Android 7.0 (API 24) and newer.

## Requirements

- An Xtream Codes-compatible IPTV account and server
- Internet access

SetPlay does not provide IPTV channels or subscriptions. Only use services and
content you are authorized to access.

## Build locally

The project includes the Gradle wrapper and can be built from the repository root:

```bash
./gradlew assembleDebug
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

GitHub Actions builds the APK on every `v*` tag and attaches it to a GitHub
Release as `SetPlay.apk`. The stable download URL above always points to
the newest published release.
