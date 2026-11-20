package com.pokecharge.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokecharge.data.model.AnimationQuality
import com.pokecharge.data.model.AppSettings
import com.pokecharge.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SettingsRepository.getInstance(application)

    val settings: StateFlow<AppSettings> = repo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setChargingExperienceEnabled(v: Boolean) = viewModelScope.launch { repo.setChargingExperienceEnabled(v) }
    fun setAutomaticActivation(v: Boolean) = viewModelScope.launch { repo.setAutomaticActivation(v) }
    fun setAnimationQuality(v: AnimationQuality) = viewModelScope.launch { repo.setAnimationQuality(v) }
    fun setHapticsEnabled(v: Boolean) = viewModelScope.launch { repo.setHapticsEnabled(v) }
    fun setSoundEffectsEnabled(v: Boolean) = viewModelScope.launch { repo.setSoundEffectsEnabled(v) }
    fun setFullChargeNotificationEnabled(v: Boolean) = viewModelScope.launch { repo.setFullChargeNotificationEnabled(v) }
    fun setAnimationBrightness(v: Float) = viewModelScope.launch { repo.setAnimationBrightness(v) }
    fun setDarkMode(v: Boolean) = viewModelScope.launch { repo.setDarkMode(v) }
    fun setBatteryAnimationIntensity(v: Float) = viewModelScope.launch { repo.setBatteryAnimationIntensity(v) }
    fun resetToDefaults() = viewModelScope.launch { repo.resetToDefaults() }
}
