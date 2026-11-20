package com.pokecharge.ui.types

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pokecharge.data.repository.PokemonRepository
import com.pokecharge.ui.components.DotMatrixLabel
import com.pokecharge.ui.components.NothingCard
import com.pokecharge.ui.components.TypeColorDot
import com.pokecharge.ui.components.parseHexColor
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PcSecondaryText
import com.pokecharge.ui.theme.PcWhite

/** Grid/list of all 18 types (brief §8E): colour, pokemon count, tap to open its collection filter. */
@Composable
fun TypeSelectionScreen(onBack: () -> Unit, onTypeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val repo = remember { PokemonRepository.getInstance(context) }
    val profiles = remember { repo.getAllTypes() }

    Column(modifier = Modifier.fillMaxSize().background(PcBlack).padding(top = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PcWhite) }
            Text("TYPES", style = MaterialTheme.typography.titleLarge, color = PcWhite)
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(profiles, key = { it.type.id }) { profile ->
                val accent = parseHexColor(profile.accentColorHex)
                NothingCard(
                    modifier = Modifier.fillMaxWidth().clickable { onTypeSelected(profile.type.id) },
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TypeColorDot(accent, size = 14.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.type.displayName.uppercase(), style = MaterialTheme.typography.titleMedium, color = PcWhite)
                            DotMatrixLabel("${profile.pokemon.size} AVAILABLE", color = PcSecondaryText)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = PcSecondaryText)
                    }
                }
            }
        }
    }
}

