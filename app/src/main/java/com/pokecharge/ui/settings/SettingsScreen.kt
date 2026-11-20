package com.pokecharge.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokecharge.data.model.AnimationQuality
import com.pokecharge.ui.components.DotMatrixLabel
import com.pokecharge.ui.components.NothingCard
import com.pokecharge.ui.theme.PcAccentRed
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PcMutedSurface
import com.pokecharge.ui.theme.PcSecondaryText
import com.pokecharge.ui.theme.PcWhite

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(PcBlack).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PcWhite) }
            Text("SETTINGS", style = MaterialTheme.typography.titleLarge, color = PcWhite)
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            SectionLabel("CHARGING EXPERIENCE")
            NothingCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ToggleRow("Enable charging experience", "Show the Pokémon screen while charging", settings.chargingExperienceEnabled, viewModel::setChargingExperienceEnabled)
                    ToggleRow("Automatic activation", "Launch automatically when a charger connects", settings.automaticActivation, viewModel::setAutomaticActivation)
                    ToggleRow("Full-charge notification", "Notify when the battery reaches 100%", settings.fullChargeNotificationEnabled, viewModel::setFullChargeNotificationEnabled)
                }
            }

            SectionLabel("ANIMATION")
            NothingCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        DotMatrixLabel("ANIMATION QUALITY")
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnimationQuality.entries.forEach { quality ->
                                QualityChip(quality.label, quality == settings.animationQuality) { viewModel.setAnimationQuality(quality) }
                            }
                        }
                    }
                    SliderRow("Animation brightness", settings.animationBrightness, viewModel::setAnimationBrightness)
                    SliderRow("Battery animation intensity", settings.batteryAnimationIntensity, viewModel::setBatteryAnimationIntensity)
                }
            }

            SectionLabel("FEEDBACK")
            NothingCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ToggleRow("Haptic feedback", "Subtle vibration on charge start and full charge", settings.hapticsEnabled, viewModel::setHapticsEnabled)
                    ToggleRow("Sound effects", "Off by default. No bundled audio yet — see README", settings.soundEffectsEnabled, viewModel::setSoundEffectsEnabled)
                }
            }

            SectionLabel("GENERAL")
            NothingCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ToggleRow("Dark mode", "POKÉCHARGE is always dark, Nothing-OS style", settings.darkMode, viewModel::setDarkMode, enabled = false)
                    TextButton(onClick = viewModel::resetToDefaults) {
                        Text("RESET SETTINGS TO DEFAULTS", color = PcAccentRed, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    DotMatrixLabel(text, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = PcWhite)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = PcSecondaryText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = PcAccentRed, checkedThumbColor = PcWhite),
        )
    }
}

@Composable
private fun SliderRow(title: String, value: Float, onChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = PcWhite)
            DotMatrixLabel("${(value * 100).toInt()}%")
        }
        Slider(
            value = value,
            onValueChange = onChange,
            colors = SliderDefaults.colors(thumbColor = PcAccentRed, activeTrackColor = PcAccentRed, inactiveTrackColor = PcMutedSurface),
        )
    }
}

@Composable
private fun QualityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) PcAccentRed else PcMutedSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label.uppercase(), color = if (selected) PcWhite else PcSecondaryText, style = MaterialTheme.typography.labelMedium)
    }
}
