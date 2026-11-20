# POKÉCHARGE — Testing status

The brief (§16) lists 16 scenarios to verify, explicitly on a real CMF
Phone 2 Pro, not just an emulator. **This development environment has
neither** (no Android SDK, no emulator, no physical device, no ADB). None
of the scenarios below have been executed. This document records, for each
one, what the code is designed to do and exactly how to verify it once a
real device/Android Studio is available — so testing is a checklist, not a
guessing game.

| # | Scenario | Design intent | How to verify |
|---|---|---|---|
| 1 | App opens normally | Splash → Home via `PokeChargeNavHost` | Launch the app, confirm splash → home transition, no crash. |
| 2 | Charger connected while app open | `HomeViewModel`/`ChargingViewModel` observe `ChargingStateManager.state` live | Open the app, plug in charger, watch the Home card's battery % update without restarting the app. |
| 3 | Charger connected while app in background | `PowerConnectionReceiver` → `ChargingForegroundService` → notification | Background the app, plug in, confirm the charging notification appears within ~1s. |
| 4 | Charger connected while screen locked | Full-screen intent path, gated by `canUseFullScreenIntent()` | Lock the phone, plug in, observe whether the charging screen auto-appears or only the notification does (both are valid depending on platform/Play Protect state — see README's "Lock screen & full-screen intent limitations"). |
| 5 | Charger disconnected | `ACTION_POWER_DISCONNECTED` → service plays disconnect animation → `ChargingActivity.finish()` after ~1.7s | Unplug while the charging screen is open; confirm the disconnect fade/animation plays, then the screen closes (not an abrupt black screen). |
| 6 | Battery reaches 100% | `AnimationStageEvent.FULL_CHARGE` one-shot burst + full-charge notification | Charge (or use a device/emulator battery override) to 100%, confirm the burst animation, haptic, and "Fully charged" notification (if enabled in Settings). |
| 7 | Battery already at 100% when connected | `ChargingStateManager` derives `FULLY_CHARGED` from the very first sticky broadcast | Plug in an already-full device; confirm it shows "FULLY CHARGED" immediately, not "0%" or a crash. |
| 8 | Charging state changes (e.g. AC ↔ USB) | `PlugSource`/`ChargingPhase` recomputed on every `ACTION_BATTERY_CHANGED` | Swap chargers/cables mid-session; confirm the status label updates smoothly (not abruptly) per `AnimationEngine`'s stage bucketing. |
| 9 | Device restarted while charging | `BOOT_COMPLETED` receiver re-checks the sticky battery broadcast | Reboot while plugged in; confirm the charging notification/experience resumes without the user re-plugging. |
| 10 | User changes selected Pokémon | `SettingsRepository.setSelectedDexId` via Collection/Detail screens | Select a different Pokémon in Collection, confirm Home/Charging screen reflect it next session. |
| 11 | User changes Pokémon type | Type Selection → Collection filtered by type | Open Types, tap a type, confirm Collection opens pre-filtered. |
| 12 | User disables automatic activation | `Settings.automaticActivation` gates `PowerConnectionReceiver` | Turn off "Automatic activation", plug in, confirm no notification/service starts (charging state still updates inside the app if opened manually). |
| 13 | Animation assets unavailable | `Pokemon.isAvailable` computed from `assetExists()`; `PokemonStageArt` handles a null bitmap gracefully (renders nothing, no crash) | Temporarily rename an artwork file, confirm the app doesn't crash, just shows the particle effect without character art. |
| 14 | Notification permission denied | `MainActivity` requests `POST_NOTIFICATIONS`; app still functions without it, just can't show the charging notification | Deny the permission prompt, plug in, confirm the app doesn't crash (no notification appears, which is expected and should be surfaced to the user — currently only documented here, not yet surfaced as an in-app banner). |
| 15 | App process killed | `ChargingStateManager`/`ChargingForegroundService` re-derive all state from sticky broadcasts and DataStore on next start — no in-memory-only state that would be silently lost | Force-stop the app while charging, re-open it, confirm correct battery %/phase immediately. |
| 16 | Battery saver mode | No app-specific handling implemented | Enable battery saver, confirm the app's own background footprint stays minimal (single event-driven receiver, no polling) — note: Android may independently throttle animations/vibration system-wide in this mode, which is expected OS behaviour, not a bug in this app. |

## Known gaps to close once a device is available

- Scenario 4 needs to be checked specifically on the CMF Phone 2 Pro's
  Nothing OS build, since OEM skins can differ from stock AOSP in how
  aggressively they restrict full-screen intents.
- Scenario 14 should grow a small in-app banner ("notifications are off —
  the charging screen will only open when you tap the app") rather than
  silently doing nothing; not implemented yet.
- No automated instrumentation/unit tests are included yet. `AnimationEngine`
  (pure Kotlin, no Android dependency) is the best first candidate for a
  JUnit test suite once a JDK + Kotlin test runner is available in the
  build environment.
