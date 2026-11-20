package com.pokecharge.ui.preview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pokecharge.data.model.Pokemon
import com.pokecharge.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Backs the Animation Preview screen (brief §8F). This screen must never
 * touch real battery state - [simulatedPercent] is a purely local,
 * in-memory value the user drives with buttons, fed straight into the same
 * [com.pokecharge.animation.engine.AnimationEngine] / [com.pokecharge.animation.effects.TypeEffectCanvas]
 * the real charging screen uses, so what you preview is exactly what you'll see.
 */
class PreviewViewModel(application: Application) : AndroidViewModel(application) {
    private val pokemonRepository = PokemonRepository.getInstance(application)

    private val _pokemon = MutableStateFlow<Pokemon?>(null)
    val pokemon: StateFlow<Pokemon?> = _pokemon.asStateFlow()

    private val _simulatedPercent = MutableStateFlow(50)
    val simulatedPercent: StateFlow<Int> = _simulatedPercent.asStateFlow()

    private val _isSimulatingCharging = MutableStateFlow(true)
    val isSimulatingCharging: StateFlow<Boolean> = _isSimulatingCharging.asStateFlow()

    fun load(dexId: Int) {
        if (_pokemon.value?.dexId == dexId) return
        _pokemon.value = pokemonRepository.getPokemonByDexId(dexId)
    }

    fun setSimulatedPercent(percent: Int) {
        _simulatedPercent.value = percent.coerceIn(0, 100)
    }

    fun toggleCharging() {
        _isSimulatingCharging.value = !_isSimulatingCharging.value
    }
}
