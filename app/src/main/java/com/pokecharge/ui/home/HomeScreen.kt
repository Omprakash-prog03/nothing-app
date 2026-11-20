package com.pokecharge.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokecharge.animation.engine.AnimationEngine
import com.pokecharge.animation.engine.AnimationStageEvent
import com.pokecharge.charging.model.ChargingPhase
import com.pokecharge.ui.components.AnimationStage
import com.pokecharge.ui.components.DotMatrixDecoration
import com.pokecharge.ui.components.DotMatrixLabel
import com.pokecharge.ui.components.NothingCard
import com.pokecharge.ui.components.TypeColorDot
import com.pokecharge.ui.components.parseHexColor
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PcWhite

@Composable
fun HomeScreen(
    onOpenCollection: () -> Unit,
    onOpenTypes: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPreview: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val pokemon = state.selectedPokemon

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PcBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("POKÉCHARGE", style = MaterialTheme.typography.titleLarge, color = PcWhite)
                DotMatrixDecoration(dotCount = 4, modifier = Modifier.padding(top = 4.dp))
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = PcWhite)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (pokemon != null) {
            val accent = parseHexColor(state.accentColorHex)
            val isCharging = state.chargingState.isCharging || state.chargingState.phase == ChargingPhase.FULLY_CHARGED
            val engine = remember(pokemon.dexId) { AnimationEngine() }

            val frame = engine.nextFrame(
                percent = if (isCharging) state.chargingState.batteryPercentage else 42,
                isCharging = isCharging,
                isFull = state.chargingState.isFull,
                phase = state.chargingState.phase,
            )

            NothingCard(modifier = Modifier.fillMaxWidth().clickable { onOpenPreview(pokemon.dexId) }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.3f)) {
                        AnimationStage(
                            pokemon = pokemon,
                            intensity = if (isCharging) frame.intensity else 0.3f,
                            event = AnimationStageEvent.CHARGING,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TypeColorDot(accent)
                        Spacer(Modifier.size(8.dp))
                        Column {
                            Text(pokemon.name.uppercase(), style = MaterialTheme.typography.titleLarge, color = PcWhite)
                            DotMatrixLabel("TYPE: ${pokemon.profileType.displayName}")
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${state.chargingState.batteryPercentage}%",
                            style = MaterialTheme.typography.headlineLarge,
                            color = PcWhite,
                        )
                        Spacer(Modifier.size(8.dp))
                        DotMatrixLabel(
                            text = phaseLabel(state.chargingState.phase),
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        DotMatrixLabel("QUICK ACCESS")
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuickAction(Icons.Filled.GridView, "Collection", Modifier.weight(1f), onOpenCollection)
            QuickAction(Icons.Filled.Category, "Types", Modifier.weight(1f), onOpenTypes)
            QuickAction(Icons.Filled.PlayArrow, "Preview", Modifier.weight(1f)) {
                pokemon?.let { onOpenPreview(it.dexId) }
            }
        }
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    NothingCard(modifier = modifier.clickable(onClick = onClick)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = label, tint = PcWhite)
            Spacer(Modifier.height(6.dp))
            DotMatrixLabel(label)
        }
    }
}

private fun phaseLabel(phase: ChargingPhase): String = when (phase) {
    ChargingPhase.NOT_CHARGING -> "Not charging"
    ChargingPhase.CHARGER_CONNECTED -> "Connected"
    ChargingPhase.CHARGING -> "Charging"
    ChargingPhase.CHARGING_SLOW -> "Charging slowly"
    ChargingPhase.CHARGING_FAST -> "Charging fast"
    ChargingPhase.CHARGING_WIRELESS -> "Charging wirelessly"
    ChargingPhase.FULLY_CHARGED -> "Fully charged"
    ChargingPhase.CHARGER_DISCONNECTED -> "Disconnected"
}

