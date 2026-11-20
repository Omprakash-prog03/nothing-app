package com.pokecharge.ui.detail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokecharge.animation.engine.AnimationEngine
import com.pokecharge.animation.engine.AnimationStageEvent
import com.pokecharge.ui.components.AnimationStage
import com.pokecharge.ui.components.DotMatrixLabel
import com.pokecharge.ui.components.TypeColorDot
import com.pokecharge.ui.components.parseHexColor
import com.pokecharge.ui.theme.PcAccentRed
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PcSecondaryText
import com.pokecharge.ui.theme.PcWhite

@Composable
fun DetailScreen(
    dexId: Int,
    onBack: () -> Unit,
    onPreview: () -> Unit,
    viewModel: DetailViewModel = viewModel(),
) {
    LaunchedEffect(dexId) { viewModel.load(dexId) }
    val state by viewModel.uiState.collectAsState()
    val pokemon = state.pokemon ?: return

    val accent = parseHexColor(state.accentColorHex)
    val engine = androidx.compose.runtime.remember(pokemon.dexId) { AnimationEngine() }
    val frame = engine.nextFrame(percent = 65, isCharging = true, isFull = false, phase = com.pokecharge.charging.model.ChargingPhase.CHARGING)

    Column(modifier = Modifier.fillMaxSize().background(PcBlack)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PcWhite) }
            Text("POKÉMON DETAIL", style = MaterialTheme.typography.titleLarge, color = PcWhite)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            AnimationStage(
                pokemon = pokemon,
                intensity = frame.intensity,
                event = AnimationStageEvent.CHARGING,
                highRes = true,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeColorDot(accent, size = 10.dp)
                Spacer(Modifier.width(8.dp))
                Text(pokemon.name.uppercase(), style = MaterialTheme.typography.headlineMedium, color = PcWhite)
            }
            Spacer(Modifier.height(6.dp))
            DotMatrixLabel("OFFICIAL TYPE: " + pokemon.officialTypes.joinToString(" / ") { it.displayName.uppercase() })
            Spacer(Modifier.height(4.dp))
            DotMatrixLabel("ANIMATION PROFILE: ${pokemon.profileType.displayName.uppercase()} · PROCEDURAL CANVAS")
            Spacer(Modifier.height(16.dp))
            Text(pokemon.description, style = MaterialTheme.typography.bodyLarge, color = PcSecondaryText)

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPreview,
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent, contentColor = PcWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PcSecondaryText),
                ) { Text("PREVIEW ANIMATION") }

                Button(
                    onClick = viewModel::selectThisPokemon,
                    enabled = !state.isSelected,
                    colors = ButtonDefaults.buttonColors(containerColor = PcAccentRed, contentColor = PcWhite),
                ) { Text(if (state.isSelected) "SELECTED" else "SELECT") }
            }
        }
    }
}

