package com.pokecharge.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.pokecharge.MainActivity
import com.pokecharge.R
import com.pokecharge.charging.manager.ChargingExperienceManager
import com.pokecharge.charging.model.ChargingPhase
import com.pokecharge.charging.model.ChargingState
import com.pokecharge.data.repository.PokemonRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A deliberately small classic `RemoteViews` home-screen widget (brief
 * §10): selected Pokémon, battery percentage, charging state, a
 * type-coloured dot, and a tap target that opens the app. Built on plain
 * `AppWidgetProvider`/`RemoteViews` rather than Jetpack Glance to avoid an
 * extra dependency for something this simple, and because it is easy to
 * verify correct without a running emulator.
 *
 * Android limits `updatePeriodMillis` to roughly 30 minutes, so
 * [WidgetUpdater.requestUpdate] is also called directly from the charging
 * foreground service whenever battery state changes, keeping the widget
 * live during an actual charging session without any extra polling loop
 * of its own.
 */
class PokeChargeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.Default).launch {
            val settings = ChargingExperienceManager.currentSettings(context)
            val pokemon = PokemonRepository.getInstance(context).getPokemonByDexId(settings.selectedDexId)
            val accentHex = pokemon?.let { PokemonRepository.getInstance(context).getTypeProfile(it.profileType)?.accentColorHex }
            val charging = readChargingSnapshot(context)

            appWidgetIds.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_pokecharge)
                views.setTextViewText(R.id.widget_pokemon_name, (pokemon?.name ?: "—").uppercase())
                views.setTextViewText(R.id.widget_battery_percent, "${charging.batteryPercentage}%")
                views.setTextViewText(R.id.widget_status, statusLabel(charging))
                accentHex?.let {
                    views.setInt(R.id.widget_type_dot, "setBackgroundColor", android.graphics.Color.parseColor(it))
                }

                val openAppIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                views.setOnClickPendingIntent(R.id.widget_pokemon_name, pendingIntent)

                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }

    private fun readChargingSnapshot(context: Context): ChargingState {
        val sticky = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return ChargingState()
        val level = sticky.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
        val scale = sticky.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else 0
        val status = sticky.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, 0)
        val plugged = sticky.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, 0)
        val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING
        val phase = when {
            plugged == 0 -> ChargingPhase.NOT_CHARGING
            percent >= 100 -> ChargingPhase.FULLY_CHARGED
            isCharging -> ChargingPhase.CHARGING
            else -> ChargingPhase.CHARGER_CONNECTED
        }
        return ChargingState(batteryPercentage = percent, isCharging = isCharging, isFull = percent >= 100, phase = phase)
    }

    private fun statusLabel(state: ChargingState): String = when (state.phase) {
        ChargingPhase.NOT_CHARGING -> "NOT CHARGING"
        ChargingPhase.FULLY_CHARGED -> "FULLY CHARGED"
        ChargingPhase.CHARGING -> "CHARGING"
        ChargingPhase.CHARGER_CONNECTED -> "CONNECTED"
        else -> "CHARGING"
    }
}
