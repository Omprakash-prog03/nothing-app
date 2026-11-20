package com.pokecharge.charging.model

/**
 * High-level charging state machine. [CHARGING_SLOW] / [CHARGING_FAST] are
 * *estimated* from `BatteryManager.EXTRA_PLUGGED` (AC vs USB) and the
 * charge-rate delta observed between consecutive `ACTION_BATTERY_CHANGED`
 * broadcasts - Android does not expose a reliable single "charging speed"
 * value, so these two states are treated as best-effort hints only, never
 * as certainties the UI should rely on for anything functional.
 */
enum class ChargingPhase {
    NOT_CHARGING,
    CHARGER_CONNECTED,
    CHARGING,
    CHARGING_SLOW,
    CHARGING_FAST,
    CHARGING_WIRELESS,
    FULLY_CHARGED,
    CHARGER_DISCONNECTED,
}

enum class PlugSource { AC, USB, WIRELESS, DOCK, UNKNOWN }

/**
 * Immutable snapshot of device charging/battery state, as produced by
 * [com.pokecharge.charging.manager.ChargingStateManager].
 */
data class ChargingState(
    val batteryPercentage: Int = 0,
    val isCharging: Boolean = false,
    val isFull: Boolean = false,
    val phase: ChargingPhase = ChargingPhase.NOT_CHARGING,
    val plugSource: PlugSource = PlugSource.UNKNOWN,
    val estimatedChargingMode: String = "UNKNOWN",
    val connectedTimestampMillis: Long? = null,
    val temperatureCelsius: Float? = null,
)

/**
 * Animation-intensity stage derived from battery percentage. These are
 * *intensity* buckets for smooth interpolation, not hard behavioural
 * thresholds - see [com.pokecharge.animation.engine.AnimationEngine].
 */
enum class BatteryStage(val range: IntRange, val label: String) {
    WAKING(0..20, "Waking up"),
    ACTIVE(21..40, "Active"),
    MAIN(41..60, "Main animation"),
    ENERGETIC(61..80, "Energetic"),
    PREPARING(81..99, "Preparing full charge"),
    FULL(100..100, "Fully charged");

    companion object {
        fun fromPercentage(percent: Int): BatteryStage =
            entries.find { percent in it.range } ?: if (percent >= 100) FULL else WAKING
    }
}
