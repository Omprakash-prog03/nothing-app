package com.pokecharge.charging.manager

import android.content.Context
import com.pokecharge.data.model.AppSettings
import com.pokecharge.data.repository.PokemonRepository
import com.pokecharge.data.repository.SettingsRepository
import com.pokecharge.data.model.Pokemon
import kotlinx.coroutines.flow.first

/**
 * Coordinates "should the charging experience activate right now?" and
 * "which Pokémon should it show?" - the small piece of glue between
 * [SettingsRepository], [PokemonRepository] and the charging lifecycle
 * (receiver / foreground service / activity).
 */
object ChargingExperienceManager {

    suspend fun currentSettings(context: Context): AppSettings =
        SettingsRepository.getInstance(context).settingsFlow.first()

    suspend fun shouldAutoActivate(context: Context): Boolean {
        val settings = currentSettings(context)
        return settings.chargingExperienceEnabled && settings.automaticActivation
    }

    suspend fun selectedPokemon(context: Context): Pokemon {
        val settings = currentSettings(context)
        val repo = PokemonRepository.getInstance(context)
        return repo.getPokemonByDexId(settings.selectedDexId)
            ?: repo.getAllPokemon().firstOrNull { it.isAvailable }
            ?: repo.getAllPokemon().first()
    }
}
