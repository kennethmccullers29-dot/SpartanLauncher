# Spartan Launcher

A **Windows 10 Mobile / Windows Phone 8.1 recreation for Android**: a home screen launcher with Metro live tiles, the W10M Start experience and its look and feel.

> Fan project. Not affiliated with, endorsed by, or sponsored by Microsoft. Windows, Windows Phone and Windows 10 Mobile are trademarks of Microsoft Corporation.

## Features

- **Start screen in two styles**: Modern (Windows 10 Mobile) and Classic (Windows Phone 8.1), with small, medium, wide and large tiles.
- **Live tiles**, including a **People tile** with the classic mosaic (and an orbiting small tile), plus tile notification counts and content from app notifications.
- **All apps list** with jump list (letter grid) and search.
- **Icon pack support**: use any common Android icon pack for tile and app icons.
- **W10M-style Settings pages**: personalization (accent color, theme, wallpaper / Bing wallpaper), Start, weather, icon packs and more.
- **SpartanOS boot screen**: a Windows 10 style boot screen with the spinning-dots loader on cold start.
- **Lock screen settings** for the companion **SpartanLock** app.
- Works together with [SpartanBar](https://github.com/kennethmccullers29-dot/SpartanBar) (W10M status bar and action center).
- W10M-style motion: tile flips, Start "dive" transitions, Metro page turns.

## Install

1. Go to **[Releases](https://github.com/kennethmccullers29-dot/SpartanLauncher/releases)** and download `SpartanLauncher-v1.1.apk`.
2. Open it on your phone and allow installing from unknown sources if asked.
3. Press Home and choose **Spartan Launcher**, and set it as default (Settings → Apps → Default apps → Home app).

Requires Android 8.0 (API 26) or newer.

**Package name:** `com.ken1dot.spartanlauncher`. v1.1 moved from `com.example.*` to this package, so it installs as a new app. Uninstall v1.0 first so you don't end up with two copies.

## Permissions and setup

The launcher asks for these when you use the related feature:

- **Notification access**: live tile counts and notification content on tiles.
- **Contacts**: the People tile.
- **Calendar**: the Calendar tile.
- **Photos (media images)**: the Photos tile and custom wallpaper.
- **Location**: the weather tile.
- **Query all packages**: listing your apps and icon packs.

## Build from source

```bash
git clone https://github.com/kennethmccullers29-dot/SpartanLauncher.git
cd SpartanLauncher
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

You need JDK 17+ (Android Studio's bundled JBR works) and the Android SDK. Opening the project in Android Studio also works.

## Release signing

To build a signed release, create `../keys/keystore.properties` next to the repo folder. It holds `storeFile`, `storePassword`, `keyAlias` and `keyPassword`. Then run `./gradlew assembleRelease`. Without that file, release builds are unsigned.

## Credits

- Fonts: [Selawik](https://github.com/microsoft/Selawik) (SIL Open Font License), an open-source Segoe UI fallback.
- Inspired by Windows Phone 8.1 and Windows 10 Mobile.
