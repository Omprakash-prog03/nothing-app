package com.pokecharge.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pokecharge.animation.assets.PokemonAssetProvider
import com.pokecharge.animation.effects.PokemonStageArt
import com.pokecharge.animation.effects.TypeEffectCanvas
import com.pokecharge.animation.effects.TypeEffectLibrary
import com.pokecharge.animation.engine.AnimationStageEvent
import com.pokecharge.data.model.Pokemon

/**
 * The single shared "stage" composable used by the home preview, the
 * Pokémon detail preview, the animation preview screen and the dedicated
 * charging screen. Keeping it in one place guarantees the selected
 * Pokémon's animation looks identical wherever it's shown, and that the
 * charging screen is genuinely showing "the same thing" the user picked,
 * not a re-implementation.
 */
@Composable
fun AnimationStage(
    pokemon: Pokemon,
    intensity: Float,
    event: AnimationStageEvent,
    brightness: Float = 0.85f,
    highRes: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var artwork by remember(pokemon.dexId) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(pokemon.dexId, highRes) {
        artwork = if (highRes) {
            PokemonAssetProvider.loadFull(context, pokemon.artworkAssetPath)
        } else {
            PokemonAssetProvider.loadThumbnail(context, pokemon.artworkAssetPath)
        }
    }

    val spec = TypeEffectLibrary.forType(pokemon.profileType)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        TypeEffectCanvas(
            spec = spec,
            intensity = intensity,
            event = event,
            brightness = brightness,
            modifier = Modifier.fillMaxSize(),
        )
        PokemonStageArt(
            artwork = artwork,
            type = pokemon.profileType,
            intensity = intensity,
            event = event,
            contentDescription = pokemon.name,
            modifier = Modifier.fillMaxWidth(0.62f).padding(8.dp),
        )
    }
}
