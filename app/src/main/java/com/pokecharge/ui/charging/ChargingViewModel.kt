package com.pokecharge.ui.charging

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokecharge.animation.engine.AnimationEngine
import com.pokecharge.animation.engine.AnimationFrame
import com.pokecharge.animation.engine.AnimationStageEvent
import com.pokecharge.charging.manager.ChargingExperienceManager
import com.pokecharge.charging.manager.ChargingStateManager
import com.pokecharge.charging.model.ChargingPhase
import com.pokecharge.charging.model.ChargingState
import com.pokecharge.data.model.AppSettings
import com.pokecharge.data.model.Pokemon
import com.pokecharge.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ChargingUiState(
    val pokemon: Pokemon? = null,
    val accentColorHex: String = "#FFFFFF",
    val chargingState: ChargingState = ChargingState(),
    val frame: AnimationFrame? = null,
    val settings: AppSettings = AppSettings(),
    /** True once we've observed the charger is gone - the Activity should finish shortly after. */
    val readyToClose: Boolean = false,
)

class ChargingViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository.getInstance(application)
    private val pokemonRepository = com.pokecharge.data.repository.PokemonRepository.getInstance(application)

    private var engine: AnimationEngine? = null
    private var hadChargerAtLeastOnce = false

    private val _uiState = MutableStateFlow(ChargingUiState())
    val uiState: StateFlow<ChargingUiState> = _uiState.asStateFlow()

    init {
        ChargingStateManager.start(application)
        viewModelScope.launch {
            val selected = ChargingExperienceManager.selectedPokemon(application)
            val accent = pokemonRepository.getTypeProfile(selected.profileType)?.accentColorHex ?: "#FFFFFF"
            _uiState.value = _uiState.value.copy(pokemon = selected, accentColorHex = accent)

            combine(ChargingStateManager.state, settingsRepository.settingsFlow) { charging, settings ->
                charging to settings
            }.collect { (charging, settings) ->
                val pluggedIn = charging.phase != ChargingPhase.NOT_CHARGING && charging.phase != ChargingPhase.CHARGER_DISCONNECTED
                if (pluggedIn) hadChargerAtLeastOnce = true

                val localEngine = engine ?: AnimationEngine(
                    qualityMultiplier = settings.animationQuality.particleMultiplier,
                    userIntensity = settings.batteryAnimationIntensity,
                ).also { engine = it }

                val frame = localEngine.nextFrame(
                    percent = charging.batteryPercentage,
                    isCharging = pluggedIn,
                    isFull = charging.isFull,
                    phase = charging.phase,
                )

                val shouldClose = hadChargerAtLeastOnce && !pluggedIn

                _uiState.value = _uiState.value.copy(
                    chargingState = charging,
                    frame = frame,
                    settings = settings,
                    readyToClose = shouldClose,
                )
            }
        }
    }

    override fun onCleared() {
        ChargingStateManager.stop(getApplication())
        super.onCleared()
    }
}
