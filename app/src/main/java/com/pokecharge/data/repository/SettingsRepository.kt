package com.pokecharge.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pokecharge.data.model.AnimationQuality
import com.pokecharge.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pokecharge_settings")

/**
 * Settings persistence. Backed by Jetpack DataStore (Preferences) rather
 * than Room, since the data is a single flat bag of user preferences with
 * no relational structure - Room would be unnecessary complexity here.
 */
class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    private object Keys {
        val CHARGING_EXPERIENCE_ENABLED = booleanPreferencesKey("charging_experience_enabled")
        val AUTOMATIC_ACTIVATION = booleanPreferencesKey("automatic_activation")
        val SELECTED_DEX_ID = intPreferencesKey("selected_dex_id")
        val ANIMATION_QUALITY = stringPreferencesKey("animation_quality")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val FULL_CHARGE_NOTIFICATION = booleanPreferencesKey("full_charge_notification")
        val ANIMATION_BRIGHTNESS = floatPreferencesKey("animation_brightness")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val BATTERY_ANIMATION_INTENSITY = floatPreferencesKey("battery_animation_intensity")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            chargingExperienceEnabled = prefs[Keys.CHARGING_EXPERIENCE_ENABLED] ?: defaults.chargingExperienceEnabled,
            automaticActivation = prefs[Keys.AUTOMATIC_ACTIVATION] ?: defaults.automaticActivation,
            selectedDexId = prefs[Keys.SELECTED_DEX_ID] ?: defaults.selectedDexId,
            animationQuality = prefs[Keys.ANIMATION_QUALITY]?.let { raw ->
                runCatching { AnimationQuality.valueOf(raw) }.getOrNull()
            } ?: defaults.animationQuality,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: defaults.hapticsEnabled,
            soundEffectsEnabled = prefs[Keys.SOUND_EFFECTS_ENABLED] ?: defaults.soundEffectsEnabled,
            fullChargeNotificationEnabled = prefs[Keys.FULL_CHARGE_NOTIFICATION] ?: defaults.fullChargeNotificationEnabled,
            animationBrightness = prefs[Keys.ANIMATION_BRIGHTNESS] ?: defaults.animationBrightness,
            darkMode = prefs[Keys.DARK_MODE] ?: defaults.darkMode,
            batteryAnimationIntensity = prefs[Keys.BATTERY_ANIMATION_INTENSITY] ?: defaults.batteryAnimationIntensity,
        )
    }

    suspend fun setChargingExperienceEnabled(enabled: Boolean) = update { it[Keys.CHARGING_EXPERIENCE_ENABLED] = enabled }
    suspend fun setAutomaticActivation(enabled: Boolean) = update { it[Keys.AUTOMATIC_ACTIVATION] = enabled }
    suspend fun setSelectedDexId(dexId: Int) = update { it[Keys.SELECTED_DEX_ID] = dexId }
    suspend fun setAnimationQuality(quality: AnimationQuality) = update { it[Keys.ANIMATION_QUALITY] = quality.name }
    suspend fun setHapticsEnabled(enabled: Boolean) = update { it[Keys.HAPTICS_ENABLED] = enabled }
    suspend fun setSoundEffectsEnabled(enabled: Boolean) = update { it[Keys.SOUND_EFFECTS_ENABLED] = enabled }
    suspend fun setFullChargeNotificationEnabled(enabled: Boolean) = update { it[Keys.FULL_CHARGE_NOTIFICATION] = enabled }
    suspend fun setAnimationBrightness(value: Float) = update { it[Keys.ANIMATION_BRIGHTNESS] = value }
    suspend fun setDarkMode(enabled: Boolean) = update { it[Keys.DARK_MODE] = enabled }
    suspend fun setBatteryAnimationIntensity(value: Float) = update { it[Keys.BATTERY_ANIMATION_INTENSITY] = value }

    suspend fun resetToDefaults() {
        dataStore.edit { it.clear() }
    }

    private suspend fun update(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }

    companion object {
        @Volatile private var instance: SettingsRepository? = null
        fun getInstance(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}
