package com.pokecharge.animation.engine

import com.pokecharge.charging.model.BatteryStage
import com.pokecharge.charging.model.ChargingPhase

/** A single discrete transition worth reacting to with a one-shot animation. */
enum class AnimationStageEvent { ENTER_IDLE, CHARGE_START, CHARGING, FULL_CHARGE, DISCONNECT }

/** One computed animation instruction for the current tick. */
data class AnimationFrame(
    val stage: BatteryStage,
    val intensity: Float,
    val event: AnimationStageEvent,
)

/**
 * Pure (no Android/Compose dependency, easy to unit test) state machine that
 * turns raw charging state into an animation instruction: a smoothly-scaled
 * 0..1 [AnimationFrame.intensity] for continuous effects, plus a one-shot
 * [AnimationStageEvent] for discrete moments (charger attached, battery hit
 * 100%, charger removed).
 *
 * Smoothing of percent-driven intensity against UI-visible jitter is done
 * one layer up, in Compose, via `animateFloatAsState` - this class only
 * decides the *target* value so battery fluctuations never cause an abrupt
 * jump in the target itself (bucketed by [BatteryStage], not raw percent).
 */
class AnimationEngine(
    private val qualityMultiplier: Float = 1f,
    private val userIntensity: Float = 1f,
) {
    private var lastPhase: ChargingPhase? = null
    private var lastWasCharging = false

    fun nextFrame(percent: Int, isCharging: Boolean, isFull: Boolean, phase: ChargingPhase): AnimationFrame {
        val stage = BatteryStage.fromPercentage(percent)
        val baseIntensity = when (stage) {
            BatteryStage.WAKING -> 0.18f
            BatteryStage.ACTIVE -> 0.38f
            BatteryStage.MAIN -> 0.58f
            BatteryStage.ENERGETIC -> 0.78f
            BatteryStage.PREPARING -> 0.92f
            BatteryStage.FULL -> 1f
        }
        val intensity = (baseIntensity * qualityMultiplier * userIntensity).coerceIn(0.08f, 1f)

        val event = when {
            isFull && lastPhase != ChargingPhase.FULLY_CHARGED -> AnimationStageEvent.FULL_CHARGE
            isCharging && !lastWasCharging -> AnimationStageEvent.CHARGE_START
            !isCharging && lastWasCharging -> AnimationStageEvent.DISCONNECT
            isCharging -> AnimationStageEvent.CHARGING
            else -> AnimationStageEvent.ENTER_IDLE
        }

        lastPhase = phase
        lastWasCharging = isCharging
        return AnimationFrame(stage, intensity, event)
    }
}
