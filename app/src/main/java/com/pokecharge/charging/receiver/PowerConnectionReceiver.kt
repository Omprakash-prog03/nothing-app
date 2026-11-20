package com.pokecharge.charging.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.pokecharge.charging.manager.ChargingExperienceManager
import com.pokecharge.charging.service.ChargingForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manifest-registered receiver for the three system broadcasts the brief
 * calls out explicitly: `ACTION_POWER_CONNECTED`, `ACTION_POWER_DISCONNECTED`
 * and `BOOT_COMPLETED`. These three are listed as *protected broadcasts* in
 * the Android platform manifest, so they remain deliverable to a manifest
 * (not just a runtime-registered) receiver even after the Android 8
 * implicit-broadcast restrictions - no other app can forge them.
 *
 * This receiver does the absolute minimum: decide whether the charging
 * experience should turn on, then hand off to [ChargingForegroundService]
 * via `startForegroundService`, which is the Android-compliant way to
 * launch work from a background broadcast receiver. It never tries to
 * launch an Activity directly from here - see the service + notification
 * flow for why, and the lock-screen caveats in the README.
 */
class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                handle(context, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handle(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                if (ChargingExperienceManager.shouldAutoActivate(context)) {
                    startService(context, ChargingForegroundService.ACTION_START)
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                // Always deliver the disconnect signal even if the service
                // isn't running (no-op in that case) so a running session is
                // always given the chance to play its disconnect animation.
                startService(context, ChargingForegroundService.ACTION_DISCONNECT)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // A charger attached before boot finished never fires
                // ACTION_POWER_CONNECTED, so re-check the current sticky
                // battery state once after boot (still event-driven, not a
                // poll: we read the one sticky broadcast, not a loop).
                val sticky = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val plugged = sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
                if (plugged != 0 && ChargingExperienceManager.shouldAutoActivate(context)) {
                    startService(context, ChargingForegroundService.ACTION_START)
                }
            }
        }
    }

    private fun startService(context: Context, action: String) {
        val serviceIntent = Intent(context, ChargingForegroundService::class.java).setAction(action)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
