package com.pokecharge.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokecharge.data.model.Pokemon
import com.pokecharge.data.repository.PokemonRepository
import com.pokecharge.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val pokemon: Pokemon? = null,
    val accentColorHex: String = "#FFFFFF",
    val isSelected: Boolean = false,
)

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    private val pokemonRepository = PokemonRepository.getInstance(application)
    private val settingsRepository = SettingsRepository.getInstance(application)

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(dexId: Int) {
        val mon = pokemonRepository.getPokemonByDexId(dexId) ?: return
        val accent = pokemonRepository.getTypeProfile(mon.profileType)?.accentColorHex ?: "#FFFFFF"
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.value = DetailUiState(mon, accent, settings.selectedDexId == dexId)
            }
        }
    }

    fun selectThisPokemon() {
        val dexId = _uiState.value.pokemon?.dexId ?: return
        viewModelScope.launch {
            settingsRepository.setSelectedDexId(dexId)
            com.pokecharge.widget.WidgetUpdater.requestUpdate(getApplication())
        }
    }
}
