# SetPlay

SetPlay is an Android IPTV player for phones. It supports Xtream Codes accounts,
multiple saved accounts, Live TV, movies, series, favorites, EPG listings, search,
parental controls, and Media3 playback.

## Download the APK

The latest APK is published automatically with each version tag:

[**Download the latest SetPlay APK**](https://github.com/MPadberg-svg/SetPlay/releases/latest/download/setplay-debug.apk)

This is a debug-signed APK intended for direct installation and testing. Android
may require enabling installation from unknown sources for the browser or file
manager used to open it.

## Requirements

- Android 7.0 (API 24) or newer
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

GitHub Actions builds the debug APK on every `v*` tag and attaches it to a
GitHub Release as `setplay-debug.apk`. It can also be run manually from the
Actions tab to verify that the project builds.
