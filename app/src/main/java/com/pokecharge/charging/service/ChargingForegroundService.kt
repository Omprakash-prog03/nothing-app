package com.pokecharge.charging.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pokecharge.R
import com.pokecharge.charging.manager.ChargingExperienceManager
import com.pokecharge.charging.manager.ChargingStateManager
import com.pokecharge.charging.model.ChargingPhase
import com.pokecharge.data.model.Pokemon
import com.pokecharge.ui.charging.ChargingActivity
import kotlinx.coroutines.launch

/**
 * The only background component allowed to *keep running* while a charger
 * is attached. Its single job is to own the charging-session notification
 * (with a tap target and, where the platform allows it, a full-screen
 * intent) and to stop itself the moment the charger is disconnected.
 *
 * Why a foreground service at all, and not just a plain notification posted
 * from the BroadcastReceiver: Android does not allow a background receiver
 * to keep listening for every subsequent `ACTION_BATTERY_CHANGED` update by
 * itself once `onReceive` returns - something has to stay alive to update
 * the live battery percentage in the notification/activity for the
 * duration of the charging session. A foreground service is the
 * platform-sanctioned way to do that, declared here with the
 * `specialUse` foreground service type and a human-readable justification
 * property (required on Android 14+), per the brief's instruction to only
 * use one "when genuinely justified and permitted".
 */
class ChargingForegroundService : LifecycleService() {

    private var sessionStarted = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_DISCONNECT, ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> if (!sessionStarted) {
                sessionStarted = true
                beginSession()
            }
        }
        return START_NOT_STICKY
    }

    private fun beginSession() {
        ensureChannels()
        // Must call startForeground() within the 5s window after the
        // process was woken by startForegroundService().
        startForeground(NOTIFICATION_ID, buildNotification(pokemonName = "…", percent = 0, phase = ChargingPhase.CHARGER_CONNECTED))

        ChargingStateManager.start(this)
        lifecycleScope.launch {
            val pokemon = ChargingExperienceManager.selectedPokemon(this@ChargingForegroundService)
            var lastFullNotified = false
            ChargingStateManager.state.collect { state ->
                if (state.phase == ChargingPhase.NOT_CHARGING) {
                    // Charger already gone before we could react to the
                    // broadcast (e.g. a very fast plug/unplug) - just stop.
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collect
                }
                updateNotification(pokemon, state.batteryPercentage, state.phase)
                com.pokecharge.widget.WidgetUpdater.requestUpdate(this@ChargingForegroundService)

                val settings = ChargingExperienceManager.currentSettings(this@ChargingForegroundService)
                if (state.isFull && settings.fullChargeNotificationEnabled && !lastFullNotified) {
                    lastFullNotified = true
                    postFullChargeNotification(pokemon)
                } else if (!state.isFull) {
                    lastFullNotified = false
                }
            }
        }
    }

    override fun onDestroy() {
        ChargingStateManager.stop(this)
        super.onDestroy()
    }

    private fun updateNotification(pokemon: Pokemon, percent: Int, phase: ChargingPhase) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(pokemon.name, percent, phase))
    }

    private fun buildNotification(pokemonName: String, percent: Int, phase: ChargingPhase): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, ChargingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_CHARGING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$pokemonName is charging — $percent%")
            .setContentText(phaseLabel(phase))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(contentIntent)

        // Full-screen intent launch is only requested when the platform
        // currently permits it for this app. We never claim it bypasses the
        // Android 14+ restriction (calling/alarm-style apps only, user can
        // revoke it in Settings) - if denied, the user still has the
        // ordinary tap-to-open notification above as a graceful fallback.
        if (canUseFullScreenIntent()) {
            builder.setFullScreenIntent(contentIntent, true)
        }

        return builder.build()
    }

    private fun canUseFullScreenIntent(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
        } else {
            true // Permission was effectively always granted pre-API 34.
        }
    }

    private fun postFullChargeNotification(pokemon: Pokemon) {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, CHANNEL_COMPLETE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Fully charged")
            .setContentText("${pokemon.name} is ready to go.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID_COMPLETE, notification)
    }

    private fun phaseLabel(phase: ChargingPhase): String = when (phase) {
        ChargingPhase.CHARGING_FAST -> "Charging fast"
        ChargingPhase.CHARGING_SLOW -> "Charging slowly"
        ChargingPhase.CHARGING_WIRELESS -> "Charging wirelessly"
        ChargingPhase.FULLY_CHARGED -> "Fully charged"
        ChargingPhase.CHARGER_CONNECTED -> "Charger connected"
        else -> "Charging"
    }

    private fun ensureChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_CHARGING, getString(R.string.notification_channel_charging), NotificationManager.IMPORTANCE_HIGH).apply {
                description = getString(R.string.notification_channel_charging_desc)
                setShowBadge(false)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_COMPLETE, getString(R.string.notification_channel_complete), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.notification_channel_complete_desc)
            }
        )
    }

    companion object {
        const val ACTION_START = "com.pokecharge.action.START"
        const val ACTION_DISCONNECT = "com.pokecharge.action.DISCONNECT"
        const val ACTION_STOP = "com.pokecharge.action.STOP"

        private const val CHANNEL_CHARGING = "charging_session"
        private const val CHANNEL_COMPLETE = "charging_complete"
        private const val NOTIFICATION_ID = 42
        private const val NOTIFICATION_ID_COMPLETE = 43

        fun stopIntent(context: Context) =
            Intent(context, ChargingForegroundService::class.java).setAction(ACTION_STOP)
    }
}
