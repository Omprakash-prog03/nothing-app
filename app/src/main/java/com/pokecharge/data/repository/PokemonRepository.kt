package com.pokecharge.data.repository

import android.content.Context
import com.pokecharge.data.model.Pokemon
import com.pokecharge.data.model.PokemonRole
import com.pokecharge.data.model.PokemonType
import com.pokecharge.data.model.PokemonTypeProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader

/**
 * Loads the Pokémon catalogue from `assets/pokemon_index.json` - the single
 * hand-maintained, data-driven source of truth described in the asset
 * pipeline (see `tools/generate_pokemon_index.py` and
 * `assets/ASSET_SOURCES.md`). Nothing about the roster is hardcoded in the
 * UI layer; screens only ever read from this repository.
 *
 * Parsing uses `org.json` (built into Android) rather than an extra
 * serialization dependency, keeping the dependency graph small.
 */
class PokemonRepository(context: Context) {

    private val appContext = context.applicationContext

    private val typeProfiles: List<PokemonTypeProfile> by lazy { loadCatalogue() }

    fun getAllTypes(): List<PokemonTypeProfile> = typeProfiles

    fun getTypeProfile(type: PokemonType): PokemonTypeProfile? =
        typeProfiles.find { it.type == type }

    fun getAllPokemon(): List<Pokemon> = typeProfiles.flatMap { it.pokemon }

    fun getPokemonByDexId(dexId: Int): Pokemon? =
        getAllPokemon().find { it.dexId == dexId }

    fun searchPokemon(query: String, typeFilter: PokemonType? = null): List<Pokemon> {
        val normalizedQuery = query.trim().lowercase()
        return getAllPokemon().filter { mon ->
            (typeFilter == null || mon.profileType == typeFilter) &&
                (normalizedQuery.isEmpty() || mon.name.lowercase().contains(normalizedQuery))
        }
    }

    suspend fun getAllPokemonAsync(): List<Pokemon> = withContext(Dispatchers.Default) { getAllPokemon() }

    private fun loadCatalogue(): List<PokemonTypeProfile> {
        val json = readAssetFile("pokemon_index.json")
        val root = JSONObject(json)

        val typesJson: JSONArray = root.getJSONArray("types")
        val typeColors = HashMap<String, String>()
        for (i in 0 until typesJson.length()) {
            val t = typesJson.getJSONObject(i)
            typeColors[t.getString("id")] = t.getString("colorHex")
        }

        val pokemonJson: JSONArray = root.getJSONArray("pokemon")
        val byType = LinkedHashMap<PokemonType, MutableList<Pokemon>>()
        // Preserve the canonical 18-type ordering regardless of JSON order.
        PokemonType.entries.forEach { byType[it] = mutableListOf() }

        for (i in 0 until pokemonJson.length()) {
            val obj = pokemonJson.getJSONObject(i)
            val profileType = PokemonType.fromId(obj.getString("profileType")) ?: continue
            val officialTypesJson = obj.getJSONArray("officialTypes")
            val officialTypes = (0 until officialTypesJson.length()).mapNotNull {
                PokemonType.fromId(officialTypesJson.getString(it))
            }
            val name = obj.getString("name")
            val slug = name.lowercase()
            val artworkPath = "pokemon/${profileType.id}/$slug/artwork.webp"

            val mon = Pokemon(
                dexId = obj.getInt("dexId"),
                name = name,
                profileType = profileType,
                officialTypes = officialTypes.ifEmpty { listOf(profileType) },
                role = if (obj.getString("role") == "primary") PokemonRole.PRIMARY else PokemonRole.SECONDARY,
                description = obj.getString("description"),
                artworkAssetPath = artworkPath,
                isAvailable = assetExists(artworkPath),
            )
            byType.getValue(profileType).add(mon)
        }

        return PokemonType.entries.map { type ->
            PokemonTypeProfile(
                type = type,
                accentColorHex = typeColors[type.id] ?: "#FFFFFF",
                pokemon = byType.getValue(type),
            )
        }
    }

    private fun assetExists(relativePath: String): Boolean = try {
        appContext.assets.open(relativePath).close()
        true
    } catch (e: Exception) {
        false
    }

    private fun readAssetFile(name: String): String {
        appContext.assets.open(name).use { input ->
            return input.bufferedReader().use(BufferedReader::readText)
        }
    }

    companion object {
        @Volatile private var instance: PokemonRepository? = null

        fun getInstance(context: Context): PokemonRepository =
            instance ?: synchronized(this) {
                instance ?: PokemonRepository(context).also { instance = it }
            }
    }
}
