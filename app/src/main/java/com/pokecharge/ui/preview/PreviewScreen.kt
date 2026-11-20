package com.pokecharge.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokecharge.animation.engine.AnimationEngine
import com.pokecharge.charging.model.ChargingPhase
import com.pokecharge.ui.components.AnimationStage
import com.pokecharge.ui.components.DotMatrixLabel
import com.pokecharge.ui.theme.PcAccentRed
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PcMutedSurface
import com.pokecharge.ui.theme.PcSecondaryText
import com.pokecharge.ui.theme.PcWhite

private val SIMULATED_LEVELS = listOf(10, 25, 50, 75, 90, 100)

/**
 * Animation Preview screen (brief §8F): lets the user audition the
 * charging animation at fixed simulated battery levels without ever
 * touching real battery state - [PreviewViewModel.simulatedPercent] is a
 * purely local value, never written back to Android battery/charging APIs.
 */
@Composable
fun PreviewScreen(dexId: Int, onBack: () -> Unit, viewModel: PreviewViewModel = viewModel()) {
    LaunchedEffect(dexId) { viewModel.load(dexId) }
    val pokemon by viewModel.pokemon.collectAsState()
    val percent by viewModel.simulatedPercent.collectAsState()
    val isCharging by viewModel.isSimulatingCharging.collectAsState()
    val mon = pokemon ?: return

    val engine = remember(mon.dexId) { AnimationEngine() }
    val frame = engine.nextFrame(
        percent = percent,
        isCharging = isCharging,
        isFull = percent >= 100 && isCharging,
        phase = if (isCharging) ChargingPhase.CHARGING else ChargingPhase.NOT_CHARGING,
    )

    Column(modifier = Modifier.fillMaxSize().background(PcBlack)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PcWhite) }
            Column {
                Text("ANIMATION PREVIEW", style = MaterialTheme.typography.titleLarge, color = PcWhite)
                DotMatrixLabel("TEST ONLY · DOES NOT CHANGE REAL BATTERY", color = PcSecondaryText)
            }
        }

        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(horizontal = 16.dp).clip(RoundedCornerShape(10.dp))) {
            AnimationStage(
                pokemon = mon,
                intensity = frame.intensity,
                event = frame.event,
                highRes = true,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text("$percent%", style = MaterialTheme.typography.headlineLarge, color = PcWhite)
            DotMatrixLabel(mon.name.uppercase() + " · " + mon.profileType.displayName.uppercase())

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DotMatrixLabel("SIMULATE CHARGING")
                Spacer(Modifier.width(10.dp))
                Switch(
                    checked = isCharging,
                    onCheckedChange = { viewModel.toggleCharging() },
                    colors = SwitchDefaults.colors(checkedTrackColor = PcAccentRed, checkedThumbColor = PcWhite),
                )
            }

            Spacer(Modifier.height(14.dp))
            DotMatrixLabel("SIMULATED BATTERY LEVEL")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SIMULATED_LEVELS) { level ->
                    val selected = level == percent
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) PcAccentRed else PcMutedSurface)
                            .clickable { viewModel.setSimulatedPercent(level) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text("$level%", color = if (selected) PcWhite else PcSecondaryText, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
