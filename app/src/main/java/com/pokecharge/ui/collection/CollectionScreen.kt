package com.pokecharge.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokecharge.animation.assets.PokemonAssetProvider
import com.pokecharge.data.model.Pokemon
import com.pokecharge.data.model.PokemonType
import com.pokecharge.ui.components.DotMatrixLabel
import com.pokecharge.ui.components.TypeColorDot
import com.pokecharge.ui.components.parseHexColor
import com.pokecharge.ui.theme.PcAccentRed
import com.pokecharge.ui.theme.PcBlack
import com.pokecharge.ui.theme.PcBorder
import com.pokecharge.ui.theme.PcMutedSurface
import com.pokecharge.ui.theme.PcSecondaryText
import com.pokecharge.ui.theme.PcWhite
import com.pokecharge.data.repository.PokemonRepository

@Composable
fun CollectionScreen(
    initialTypeFilter: String?,
    onBack: () -> Unit,
    onOpenDetail: (Int) -> Unit,
    viewModel: CollectionViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val repo = remember_(context)

    LaunchedEffect(initialTypeFilter) { viewModel.setInitialType(initialTypeFilter) }

    Column(modifier = Modifier.fillMaxSize().background(PcBlack).padding(top = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PcWhite) }
            Text("COLLECTION", style = MaterialTheme.typography.titleLarge, color = PcWhite)
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Search Pokémon", color = PcSecondaryText) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PcAccentRed,
                    unfocusedBorderColor = PcBorder,
                    focusedTextColor = PcWhite,
                    unfocusedTextColor = PcWhite,
                    cursorColor = PcAccentRed,
                ),
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { TypeChip("ALL", selected = state.typeFilter == null, color = PcWhite) { viewModel.onTypeFilterChange(null) } }
                items(PokemonType.entries.toList(), key = { it.id }) { type ->
                    val accent = parseHexColor(repo.getTypeProfile(type)?.accentColorHex ?: "#FFFFFF")
                    TypeChip(type.displayName.take(3).uppercase(), selected = state.typeFilter == type, color = accent) {
                        viewModel.onTypeFilterChange(if (state.typeFilter == type) null else type)
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            gridItems(state.results, key = { it.dexId }) { mon ->
                PokemonGridTile(
                    pokemon = mon,
                    accentColorHex = repo.getTypeProfile(mon.profileType)?.accentColorHex ?: "#FFFFFF",
                    isSelected = mon.dexId == state.selectedDexId,
                    onClick = { onOpenDetail(mon.dexId) },
                )
            }
        }
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) color.copy(alpha = 0.25f) else PcMutedSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        DotMatrixLabel(label, color = if (selected) color else PcSecondaryText)
    }
}

@Composable
private fun PokemonGridTile(pokemon: Pokemon, accentColorHex: String, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    var artwork by androidx.compose.runtime.remember(pokemon.dexId) { androidx.compose.runtime.mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(pokemon.dexId) { artwork = PokemonAssetProvider.loadThumbnail(context, pokemon.artworkAssetPath) }
    val accent = parseHexColor(accentColorHex)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PcMutedSurface)
            .then(if (isSelected) Modifier.background(accent.copy(alpha = 0.12f)) else Modifier)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
            artwork?.let {
                androidx.compose.foundation.Image(
                    bitmap = it,
                    contentDescription = pokemon.name,
                    modifier = Modifier.fillMaxSize(0.9f),
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    DotMatrixLabel("IN USE", color = PcBlack)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TypeColorDot(accent, size = 6.dp)
        }
        Text(pokemon.name.uppercase(), style = MaterialTheme.typography.labelMedium, color = PcWhite)
    }
}

@Composable
private fun remember_(context: android.content.Context): PokemonRepository =
    androidx.compose.runtime.remember { PokemonRepository.getInstance(context) }
