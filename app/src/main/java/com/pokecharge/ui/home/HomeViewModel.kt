package com.pokecharge.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokecharge.charging.manager.ChargingStateManager
import com.pokecharge.charging.model.ChargingState
import com.pokecharge.data.model.AppSettings
import com.pokecharge.data.model.Pokemon
import com.pokecharge.data.repository.PokemonRepository
import com.pokecharge.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val selectedPokemon: Pokemon? = null,
    val accentColorHex: String = "#FFFFFF",
    val chargingState: ChargingState = ChargingState(),
    val settings: AppSettings = AppSettings(),
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val pokemonRepository = PokemonRepository.getInstance(application)
    private val settingsRepository = SettingsRepository.getInstance(application)

    init {
        // The home screen keeps a live battery readout while visible. This
        // is a ref-counted registration (see ChargingStateManager), so it
        // composes safely with the foreground service doing the same thing
        // during an actual charging session.
        ChargingStateManager.start(application)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settingsFlow,
        ChargingStateManager.state,
    ) { settings, chargingState ->
        val selected = pokemonRepository.getPokemonByDexId(settings.selectedDexId)
            ?: pokemonRepository.getAllPokemon().firstOrNull()
        val accent = selected?.let { pokemonRepository.getTypeProfile(it.profileType)?.accentColorHex } ?: "#FFFFFF"
        HomeUiState(
            selectedPokemon = selected,
            accentColorHex = accent,
            chargingState = chargingState,
            settings = settings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    override fun onCleared() {
        ChargingStateManager.stop(getApplication())
        super.onCleared()
    }
}
