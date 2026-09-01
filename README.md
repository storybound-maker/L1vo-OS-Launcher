# L1vo OS Launcher

The first launcher layer of the future L1vo ecosystem.

## What is in this first build

- Simple L1vo home screen inspired by the supplied tree/wilderness concept.
- Four quick-access tiles: Home is fixed; Settings, Gallery and Calls can be replaced.
- Long-press a non-Home tile to choose any installed launcher app.
- App Hub with installed apps in a clean icon grid.
- Wallpaper picker using Android's system image picker.
- Dedicated Leau eyes button that tries `com.liv.ol1viapa.OPEN_ASSISTANT` first, then opens the Leau Assistant app normally.
- Launcher/Home intent filter so Android can offer L1vo OS Launcher as the default Home app.

## Build

Open this repository as an Android Studio project and sync Gradle. The project uses Kotlin + Jetpack Compose.

## Ecosystem connection

L1vo OS Launcher and the Leau phone assistant are intentionally separate applications. The launcher only knows how to request the assistant; the assistant remains its own app and can evolve independently.
