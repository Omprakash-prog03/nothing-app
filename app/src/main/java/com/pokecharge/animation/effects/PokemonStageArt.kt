package com.pokecharge.animation.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import com.pokecharge.animation.engine.AnimationStageEvent
import com.pokecharge.data.model.PokemonType
import kotlin.math.sin
import kotlin.math.PI

/**
 * Draws the selected Pokémon's artwork with a small set of universal,
 * physically-plausible motions (gentle idle "breathing" scale, a full-charge
 * pop, a disconnect fade) plus one type-specific quirk - Ghost types flicker
 * in and out, matching the brief's "occasionally disappears and reappears"
 * direction - layered on top of [TypeEffectCanvas] rather than duplicating
 * particle logic here.
 */
@Composable
fun PokemonStageArt(
    artwork: ImageBitmap?,
    type: PokemonType,
    intensity: Float,
    event: AnimationStageEvent,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "pc-breathe")
    val breathe by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "pc-breathe-anim",
    )
    val breatheScale = 1f + sin(breathe * 2 * PI).toFloat() * 0.025f * (0.4f + intensity)

    val pop = remember { Animatable(1f) }
    LaunchedEffect(event) {
        if (event == AnimationStageEvent.FULL_CHARGE) {
            pop.animateTo(1.14f, tween(220))
            pop.animateTo(1f, tween(260))
        }
    }

    val fade = remember { Animatable(1f) }
    LaunchedEffect(event) {
        if (event == AnimationStageEvent.DISCONNECT) {
            fade.animateTo(0.55f, tween(450))
        } else if (event == AnimationStageEvent.CHARGE_START || event == AnimationStageEvent.CHARGING) {
            fade.animateTo(1f, tween(350))
        }
    }

    // Ghost-only: brief, restrained flicker/disappear - never a jarring strobe.
    val ghostAlpha = if (type == PokemonType.GHOST) {
        val cycle = (breathe * 1.4f) % 1f
        if (cycle > 0.85f) 0.35f else 1f
    } else 1f

    Box(modifier = modifier) {
        if (artwork != null) {
            Image(
                bitmap = artwork,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.graphicsLayer {
                    scaleX = breatheScale * pop.value
                    scaleY = breatheScale * pop.value
                    alpha = fade.value * ghostAlpha
                },
            )
        }
    }
}
