package com.pokecharge.data.model

enum class AnimationQuality(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    /** Scales particle counts in the effect engine. */
    val particleMultiplier: Float
        get() = when (this) {
            LOW -> 0.45f
            MEDIUM -> 0.75f
            HIGH -> 1.0f
        }
}

/**
 * User-facing preferences, persisted via DataStore (see
 * [com.pokecharge.data.repository.SettingsRepository]). Defaults are chosen
 * to be battery-conscious and non-intrusive out of the box.
 */
data class AppSettings(
    val chargingExperienceEnabled: Boolean = true,
    val automaticActivation: Boolean = true,
    val selectedDexId: Int = 25, // Pikachu
    val animationQuality: AnimationQuality = AnimationQuality.MEDIUM,
    val hapticsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = false,
    val fullChargeNotificationEnabled: Boolean = true,
    val animationBrightness: Float = 0.85f,
    val darkMode: Boolean = true,
    val batteryAnimationIntensity: Float = 1.0f,
)
