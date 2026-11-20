package com.pokecharge.ui.charging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokecharge.animation.engine.AnimationStageEvent
import com.pokecharge.charging.model.ChargingPhase
import com.pokecharge.ui.components.AnimationStage
import com.pokecharge.ui.components.DotMatrixDecoration
import com.pokecharge.ui.components.DotMatrixLabel
import com.pokecharge.ui.components.parseHexColor
import com.pokecharge.ui.theme.PcAccentRed
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PcBatteryNumberStyle
import com.pokecharge.ui.theme.PcWhite
import com.pokecharge.utils.Haptics

/**
 * The dedicated, visually-separate charging interface (brief §9): full
 * black background, the selected Pokémon's animation centred, a large
 * battery readout, and nothing else - no buttons, no dashboard chrome.
 */
@Composable
fun ChargingScreen(onReadyToClose: () -> Unit, viewModel: ChargingViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val pokemon = state.pokemon

    LaunchedEffect(state.frame?.event) {
        val event = state.frame?.event ?: return@LaunchedEffect
        if (!state.settings.hapticsEnabled) return@LaunchedEffect
        when (event) {
            AnimationStageEvent.CHARGE_START -> Haptics.chargeStart(context)
            AnimationStageEvent.FULL_CHARGE -> Haptics.fullCharge(context)
            else -> Unit
        }
    }

    LaunchedEffect(state.readyToClose) {
        if (state.readyToClose) {
            kotlinx.coroutines.delay(1700)
            onReadyToClose()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PcBlack)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("POKÉCHARGE", style = MaterialTheme.typography.titleMedium, color = PcWhite)
            Spacer(Modifier.height(6.dp))
            DotMatrixDecoration(dotCount = 5)

            Spacer(Modifier.height(8.dp))

            if (pokemon != null) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    AnimationStage(
                        pokemon = pokemon,
                        intensity = state.frame?.intensity ?: 0.2f,
                        event = state.frame?.event ?: AnimationStageEvent.ENTER_IDLE,
                        brightness = state.settings.animationBrightness,
                        highRes = true,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Text(
                    text = "${state.chargingState.batteryPercentage}%",
                    style = PcBatteryNumberStyle,
                    color = PcWhite,
                )

                Spacer(Modifier.height(6.dp))
                DotMatrixLabel(phaseCopy(state.chargingState.phase), color = PcAccentRed)
                Spacer(Modifier.height(4.dp))
                DotMatrixLabel("TYPE: ${pokemon.profileType.displayName.uppercase()} · ${pokemon.name.uppercase()}")
            }
        }
    }
}

private fun phaseCopy(phase: ChargingPhase): String = when (phase) {
    ChargingPhase.NOT_CHARGING -> "DISCONNECTED"
    ChargingPhase.CHARGER_CONNECTED -> "CHARGER CONNECTED"
    ChargingPhase.CHARGING -> "CHARGING..."
    ChargingPhase.CHARGING_SLOW -> "CHARGING SLOWLY..."
    ChargingPhase.CHARGING_FAST -> "CHARGING FAST..."
    ChargingPhase.CHARGING_WIRELESS -> "CHARGING WIRELESSLY..."
    ChargingPhase.FULLY_CHARGED -> "FULLY CHARGED"
    ChargingPhase.CHARGER_DISCONNECTED -> "DISCONNECTED"
}
