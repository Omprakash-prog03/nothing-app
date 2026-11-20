package com.pokecharge.charging.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.pokecharge.charging.model.ChargingPhase
import com.pokecharge.charging.model.ChargingState
import com.pokecharge.charging.model.PlugSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

/**
 * Single source of truth for charging/battery state.
 *
 * Design notes (see project brief §5):
 *  - Event-driven only: we register exactly one dynamic [BroadcastReceiver]
 *    for `ACTION_BATTERY_CHANGED` (a sticky broadcast, so registering it
 *    also immediately delivers the current state - no polling loop is used
 *    anywhere in this app).
 *  - [start] is idempotent and guarded by [isRegistered] so callers (the
 *    Activity, the ViewModel, the foreground Service) can all call it
 *    without risking a duplicate-receiver leak; [stop] mirrors it.
 *  - Charging *speed* (`CHARGING_SLOW` / `CHARGING_FAST`) is only ever an
 *    estimate derived from `EXTRA_PLUGGED`, per the brief's instruction not
 *    to assume Android exposes a reliable charging-speed signal.
 */
object ChargingStateManager {

    private const val TAG = "ChargingStateManager"

    private val _state = MutableStateFlow(ChargingState())
    val state: StateFlow<ChargingState> = _state.asStateFlow()

    // Multiple independent callers (the foreground service, the charging
    // activity, the home-screen preview) may all want live updates at the
    // same time. A plain boolean guard would let the first caller's stop()
    // tear down the receiver out from under the others, so this is
    // reference-counted instead: the receiver is only ever registered once,
    // and only unregistered when the last caller lets go of it.
    private val refCount = AtomicInteger(0)
    private var receiver: BroadcastReceiver? = null
    private var connectedSinceMillis: Long? = null

    fun start(context: Context) {
        if (refCount.getAndIncrement() > 0) return
        val appContext = context.applicationContext

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val localReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                _state.value = deriveState(intent)
            }
        }
        receiver = localReceiver
        // Registering with ACTION_BATTERY_CHANGED returns the last sticky
        // broadcast synchronously, so we get an immediate, accurate value
        // with zero polling.
        val sticky = appContext.registerReceiver(localReceiver, filter)
        if (sticky != null) {
            _state.value = deriveState(sticky)
        }
        Log.d(TAG, "ChargingStateManager registered")
    }

    fun stop(context: Context) {
        val remaining = refCount.decrementAndGet()
        if (remaining > 0) return
        if (remaining < 0) {
            // Defensive: stop() called without a matching start(). Clamp
            // back to zero and bail - nothing to unregister.
            refCount.set(0)
            return
        }
        val localReceiver = receiver ?: return
        try {
            context.applicationContext.unregisterReceiver(localReceiver)
        } catch (e: IllegalArgumentException) {
            // Already unregistered - safe to ignore.
        }
        receiver = null
        Log.d(TAG, "ChargingStateManager unregistered")
    }

    private fun deriveState(intent: Intent): ChargingState {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val tenthsCelsius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val temperature = if (tenthsCelsius == Int.MIN_VALUE) null else tenthsCelsius / 10f

        val plugSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> PlugSource.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PlugSource.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PlugSource.WIRELESS
            4 -> PlugSource.DOCK // BATTERY_PLUGGED_DOCK, hidden constant on some API levels
            else -> PlugSource.UNKNOWN
        }

        val isPluggedIn = plugged != 0
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL || percent >= 100
        val isActivelyCharging = status == BatteryManager.BATTERY_STATUS_CHARGING

        if (isPluggedIn && connectedSinceMillis == null) {
            connectedSinceMillis = System.currentTimeMillis()
        } else if (!isPluggedIn) {
            connectedSinceMillis = null
        }

        val phase = when {
            !isPluggedIn -> ChargingPhase.NOT_CHARGING
            isFull -> ChargingPhase.FULLY_CHARGED
            isActivelyCharging && plugSource == PlugSource.WIRELESS -> ChargingPhase.CHARGING_WIRELESS
            isActivelyCharging && plugSource == PlugSource.AC -> ChargingPhase.CHARGING_FAST
            isActivelyCharging && plugSource == PlugSource.USB -> ChargingPhase.CHARGING_SLOW
            isActivelyCharging -> ChargingPhase.CHARGING
            else -> ChargingPhase.CHARGER_CONNECTED
        }

        val estimatedMode = when (phase) {
            ChargingPhase.CHARGING_FAST -> "FAST (estimated, AC)"
            ChargingPhase.CHARGING_SLOW -> "SLOW (estimated, USB)"
            ChargingPhase.CHARGING_WIRELESS -> "WIRELESS"
            ChargingPhase.FULLY_CHARGED -> "FULL"
            else -> "UNKNOWN"
        }

        return ChargingState(
            batteryPercentage = percent.coerceIn(0, 100),
            isCharging = isActivelyCharging,
            isFull = isFull,
            phase = phase,
            plugSource = plugSource,
            estimatedChargingMode = estimatedMode,
            connectedTimestampMillis = connectedSinceMillis,
            temperatureCelsius = temperature,
        )
    }
}
