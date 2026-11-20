package com.pokecharge.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Subtle, infrequent haptics only (brief §15): a short tick when charging
 * starts or an animation stage advances, and one slightly more pronounced
 * pattern when the battery hits 100%. Never continuous, never used for
 * every particle tick.
 */
object Haptics {

    private fun vibrator(context: Context): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    fun chargeStart(context: Context) = pulse(context, 40)

    fun stageAdvance(context: Context) = pulse(context, 18)

    fun fullCharge(context: Context) {
        val v = vibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 60, 60), -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 35, 60, 60), -1)
        }
    }

    private fun pulse(context: Context, durationMs: Long) {
        val v = vibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }
}
