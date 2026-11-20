package com.pokecharge.data.model

/**
 * Official Pokémon elemental types. This is the fixed 18-entry taxonomy the
 * whole app is organised around (collection filters, animation profiles,
 * accent colours).
 */
enum class PokemonType(val id: String, val displayName: String) {
    NORMAL("normal", "Normal"),
    FIRE("fire", "Fire"),
    WATER("water", "Water"),
    ELECTRIC("electric", "Electric"),
    GRASS("grass", "Grass"),
    ICE("ice", "Ice"),
    FIGHTING("fighting", "Fighting"),
    POISON("poison", "Poison"),
    GROUND("ground", "Ground"),
    FLYING("flying", "Flying"),
    PSYCHIC("psychic", "Psychic"),
    BUG("bug", "Bug"),
    ROCK("rock", "Rock"),
    GHOST("ghost", "Ghost"),
    DRAGON("dragon", "Dragon"),
    DARK("dark", "Dark"),
    STEEL("steel", "Steel"),
    FAIRY("fairy", "Fairy");

    companion object {
        fun fromId(id: String): PokemonType? = entries.find { it.id == id.lowercase() }
    }
}

/** Which role a Pokémon plays within its assigned charging-animation profile. */
enum class PokemonRole { PRIMARY, SECONDARY }

/**
 * A single collectible Pokémon.
 *
 * [profileType] is the *charging animation profile* this character was
 * assigned to (data-driven, editable in [assets/pokemon_index.json]) and is
 * intentionally decoupled from [officialTypes]: many Pokémon are dual-typed
 * (e.g. Bulbasaur is Grass/Poison) or get reused across profiles, so the UI
 * must never assume a Pokémon belongs exclusively to the type whose
 * animation it was assigned to.
 */
data class Pokemon(
    val dexId: Int,
    val name: String,
    val profileType: PokemonType,
    val officialTypes: List<PokemonType>,
    val role: PokemonRole,
    val description: String,
    val artworkAssetPath: String,
    val isAvailable: Boolean = true,
) {
    val isDualType: Boolean get() = officialTypes.size > 1
}

/** A type's catalogue metadata plus the Pokémon assigned to it. */
data class PokemonTypeProfile(
    val type: PokemonType,
    val accentColorHex: String,
    val pokemon: List<Pokemon>,
) {
    val primary: Pokemon? get() = pokemon.firstOrNull { it.role == PokemonRole.PRIMARY } ?: pokemon.firstOrNull()
}
