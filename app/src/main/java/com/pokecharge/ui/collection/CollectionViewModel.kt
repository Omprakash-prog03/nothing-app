package com.pokecharge.ui.collection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokecharge.data.model.Pokemon
import com.pokecharge.data.model.PokemonType
import com.pokecharge.data.repository.PokemonRepository
import com.pokecharge.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CollectionUiState(
    val query: String = "",
    val typeFilter: PokemonType? = null,
    val results: List<Pokemon> = emptyList(),
    val selectedDexId: Int = -1,
)

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    private val pokemonRepository = PokemonRepository.getInstance(application)
    private val settingsRepository = SettingsRepository.getInstance(application)

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(selectedDexId = settings.selectedDexId)
                refresh()
            }
        }
    }

    fun setInitialType(typeId: String?) {
        val type = typeId?.let { PokemonType.fromId(it) }
        if (type != null && _uiState.value.typeFilter == null) {
            _uiState.value = _uiState.value.copy(typeFilter = type)
            refresh()
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        refresh()
    }

    fun onTypeFilterChange(type: PokemonType?) {
        _uiState.value = _uiState.value.copy(typeFilter = type)
        refresh()
    }

    fun selectPokemon(dexId: Int) {
        viewModelScope.launch {
            settingsRepository.setSelectedDexId(dexId)
            com.pokecharge.widget.WidgetUpdater.requestUpdate(getApplication())
        }
    }

    private fun refresh() {
        val state = _uiState.value
        val results = pokemonRepository.searchPokemon(state.query, state.typeFilter)
        _uiState.value = state.copy(results = results)
    }
}
