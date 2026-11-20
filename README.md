# POKÉCHARGE

A Pokémon-themed charging companion for Android, built for the **CMF Phone 2
Pro** (Nothing OS), in Kotlin + Jetpack Compose.

Plug in the phone and a Pokémon tied to your charging profile comes alive
with a procedurally-animated, type-specific elemental effect that responds
to battery percentage and charging state — rendered natively with Compose
`Canvas`, no WebView, no GIFs.

---

## ⚠️ Read this first: what you're looking at

This project was produced in a sandboxed development environment with:

- **No Android SDK, no JDK, no Gradle, no emulator, and no device.**
- **No network access** to Google's Maven repository or Maven Central
  (only `github.com`/`codeload.github.com`/`api.github.com`, `npmjs.org`
  and `pypi.org` were reachable).

That means every `.kt`/Gradle/XML file here was written and carefully
reviewed by hand, but **it has not been compiled, run, or tested on a
device or emulator in this environment.** Treat it as a complete,
from-scratch, review-ready Android Studio project — not as a build you can
assume is green. The very first thing to do with it is:

```bash
# In Android Studio: File → Open → select this folder, let Gradle sync,
# then Run on a device/emulator. Or from a machine with the Android SDK:
./gradlew assembleDebug
```

If `gradlew` complains about a missing wrapper jar, run `gradle wrapper`
once (with any locally installed Gradle 8.x) to regenerate
`gradle/wrapper/gradle-wrapper.jar`, since that binary couldn't be
downloaded in the sandbox either (see "Known limitations" below).

Fix whatever Gradle/Kotlin/Compose version-compat nits surface on first
sync (version numbers in `build.gradle.kts` were chosen to be mutually
compatible as of late 2024/2025, but AGP/Compose/Kotlin release trains move
fast — bump `app/build.gradle.kts` + root `build.gradle.kts` together if
Android Studio asks).

---

## What's implemented

| Area | Status |
|---|---|
| Project foundation, Gradle/Kotlin/Compose setup | ✅ |
| MVVM architecture (`data` / `charging` / `animation` / `ui` / `widget`) | ✅ |
| Data-driven Pokémon catalogue, 18 types × 2 Pokémon each (36 total) | ✅ |
| Real official artwork for all 36 Pokémon (see `assets/ASSET_SOURCES.md`) | ✅ |
| Event-driven charging/battery state engine (no polling) | ✅ |
| Procedural, type-distinct Compose Canvas animation for all 18 types | ✅ |
| Home / Collection / Type selection / Detail / Preview / Settings screens | ✅ |
| Dedicated charging screen, foreground service, notification + full-screen-intent flow | ✅ |
| Settings persistence via DataStore | ✅ |
| Haptics on key transitions | ✅ |
| Home-screen widget (classic RemoteViews) | ✅ |
| Sound effects | ❌ Stubbed, disabled by default — no reachable audio source (see below) |
| Verified build / APK | ❌ Not produced — no SDK/toolchain in this environment |
| On-device testing on a CMF Phone 2 Pro | ❌ Not possible in this environment — see `TESTING.md` |

---

## Architecture

```
com.pokecharge
  data/model          Pokemon, PokemonType, AppSettings, etc.
  data/repository      PokemonRepository (reads assets/pokemon_index.json), SettingsRepository (DataStore)
  charging/model        ChargingState, ChargingPhase, BatteryStage
  charging/manager      ChargingStateManager (event-driven, ref-counted), ChargingExperienceManager
  charging/receiver     PowerConnectionReceiver (manifest-registered)
  charging/service      ChargingForegroundService
  animation/engine      AnimationEngine (battery % → stage/intensity/event state machine)
  animation/effects     TypeEffectSpec/Library, TypeEffectCanvas (procedural per-type effects), PokemonStageArt
  animation/assets      PokemonAssetProvider (decodes artwork from assets/, LRU-bounded)
  ui/*                  One package per screen, each with its own ViewModel
  widget                 Classic AppWidgetProvider + RemoteViews
  utils                 Haptics, SoundController (stub)
```

No Hilt, no Room: the dependency graph is small enough that
`getInstance(context)` singletons + DataStore cover it without extra build
complexity (see brief §12 — "use Hilt only if it improves maintainability").

### Why the animations are procedural, not downloaded

The brief's suggested animation sources (LottieFiles, OpenGameArt) were not
reachable from the sandbox. Per the brief's own fallback instructions
("create a custom animation... never claim an animation was retrieved if it
was actually generated procedurally"), every elemental effect — fire,
electricity, water, frost, shadows, ribbons, and so on for all 18 types —
is rendered live by `animation/effects/TypeEffectCanvas.kt` using Compose
`Canvas`. Each type has its own particle silhouette + motion curve + colour
+ density (see `TypeEffectSpec.kt`), so no two types are a simple recolor
of one another. This is documented, not hidden.

### Why the Pokémon artwork is real but the licensing is flagged

`PokeAPI/sprites` on GitHub *was* reachable (GitHub is an allowed host), so
real official artwork was fetched for all 36 Pokémon via the GitHub
Contents API and converted to WebP. See `assets/ASSET_SOURCES.md` for the
full per-file provenance table and an explicit, unambiguous flag that this
artwork is Nintendo/Game Freak/Pokémon Company property and is **not**
cleared for commercial redistribution — treat it as placeholder/dev-grade
art until that's resolved (license it, or replace with original designs).

---

## Permissions

| Permission | Why |
|---|---|
| `POST_NOTIFICATIONS` | Android 13+ requires explicit consent to show the charging-session notification. |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps the charging-session notification/battery updates alive while a charger is attached — see `ChargingForegroundService`. |
| `USE_FULL_SCREEN_INTENT` | Requests permission to auto-launch the charging screen as a full-screen notification. See limitations below — this is requested, never assumed to work. |
| `RECEIVE_BOOT_COMPLETED` | Re-checks "already charging?" once after a reboot (a charger attached before boot never fires `ACTION_POWER_CONNECTED`). |
| `VIBRATE` | Subtle haptics on charge-start / full-charge. |

## Lock screen & full-screen intent limitations

Android does **not** let an ordinary app freely draw an Activity over the
lock screen just because it feels like it. This app uses the fully
documented, compliant path:

1. A manifest-registered `BroadcastReceiver` reacts to the protected
   `ACTION_POWER_CONNECTED` / `ACTION_POWER_DISCONNECTED` broadcasts.
2. It starts a foreground service (`startForegroundService`), which posts a
   high-priority notification with a `PendingIntent` to `ChargingActivity`.
3. If, and only if, `NotificationManager.canUseFullScreenIntent()` returns
   true (Android 14+) or the app targets an older OS (where the permission
   was implicitly available), the notification also carries
   `setFullScreenIntent(...)`, which *may* let the system launch the
   Activity automatically, including over the keyguard, via
   `showWhenLocked`/`turnScreenOn` declared on `ChargingActivity`.
4. If the platform does not currently trust this app with that capability
   (Android 14+ restricts full-screen intents to calling/alarm-style apps
   by policy, and a user can always revoke it in Settings → Notifications),
   the **fallback is simply the ordinary tappable notification** — the
   charging screen is one tap away, same as any other notification. The
   app never claims to have bypassed this restriction.

This also means: **on a secured CMF Phone 2 Pro, if the full-screen intent
is not granted, the Pokémon screen will not pop up automatically while the
phone is locked** — the user sees the notification instead. This should be
verified empirically on the actual device per `TESTING.md` once it's
available, not assumed either way.

---

## Build & run

Requirements: Android Studio (Koala/2024.1 or newer recommended), JDK 17,
Android SDK Platform 34.

```bash
git clone <this repo>
cd nothing-app
./gradlew assembleDebug      # or open in Android Studio and hit Run
```

Target device: CMF Phone 2 Pro (Nothing OS). `minSdk` is 26 for broad
testability; `targetSdk` is 34 to match current Play/OS expectations.

---

## Known limitations (be upfront, per the brief's own rules)

- **Unverified build.** See the big warning at the top of this file.
- **No audio.** `freesound.org`/`opengameart.org` were unreachable; the
  Settings "Sound effects" toggle is wired to a documented no-op
  (`utils/SoundController.kt`), off by default.
- **No bundled Lottie/Rive/sprite-sheet animation files.** All charging
  animations are procedural Compose `Canvas` drawing, by design (see
  above) — not a corner cut silently.
- **No custom dot-matrix font file.** Uses `FontFamily.Monospace` as a
  stand-in; `fonts.google.com` was unreachable.
- **Pokémon artwork licensing is unresolved for commercial release** — see
  `assets/ASSET_SOURCES.md`.
- **Charging-speed detection (`CHARGING_SLOW`/`CHARGING_FAST`) is a
  best-effort estimate** from `EXTRA_PLUGGED` (AC vs USB vs wireless), not
  a guaranteed-accurate wattage read — Android doesn't expose one
  consistently across OEMs.
- **Full-screen auto-launch on the lock screen is not guaranteed** — see
  the dedicated section above.
- **The widget** uses classic `RemoteViews` (not Jetpack Glance) for
  reliability without an extra dependency; it refreshes live during an
  active charging session and otherwise on Android's ~30-minute minimum
  widget update cadence.

## Project docs

- `assets/ASSET_SOURCES.md` — full asset provenance & licensing.
- `TESTING.md` — the brief's 16 test scenarios, with honest status for each.
- `tools/generate_pokemon_index.py` — regenerates per-Pokémon
  `metadata.json` files from `assets/pokemon_index.json` and verifies every
  referenced asset actually exists.
