package com.pokecharge.ui.splash

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.pokecharge.ui.theme.PcAccentRed
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PcBorder
import com.pokecharge.ui.theme.PcWhite
import kotlinx.coroutines.delay

/**
 * Minimal splash screen (brief §8A): black background, wordmark, a short
 * animated charging-progress indicator, then a smooth hand-off into Home.
 * The platform's native SplashScreen API (see themes.xml) covers the very
 * first instant before Compose is ready; this composable is the "real"
 * animated splash the user actually perceives.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(1100))
        delay(250)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text = "POKÉCHARGE",
                style = MaterialTheme.typography.headlineLarge,
                color = PcWhite,
            )
            Canvas(modifier = Modifier.width(120.dp).height(4.dp)) {
                drawLine(
                    color = PcBorder,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = PcAccentRed,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width * progress.value, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
    // Background colour is already black via the activity theme; this Box
    // deliberately has no own background so the splash never flashes a
    // different colour during the handoff from the system splash screen.
}
