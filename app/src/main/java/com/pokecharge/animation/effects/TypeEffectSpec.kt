package com.pokecharge.animation.effects

import androidx.compose.ui.graphics.Color
import com.pokecharge.data.model.PokemonType

/** The silhouette each type's particles are drawn as. Deliberately one shape per type. */
enum class ParticleShape {
    BOLT, FLAME, DROPLET, LEAF, SNOWFLAKE, IMPACT_STREAK, BUBBLE, DUST_MOTE,
    FEATHER, ORB, WING_GLINT, SHARD, SMOKE_WISP, AURA_RIBBON, SHADOW_MOTE,
    METAL_GLINT, RIBBON, STAR,
}

/** How a type's particles move. Deliberately paired so no two types share both shape+motion. */
enum class MotionPattern {
    JAGGED_STRIKE, RISE_FLICKER, FALL_AND_RIPPLE, FLUTTER_DOWN, DRIFT_SETTLE,
    IMPACT_RADIATE, BUBBLE_UP, SWIRL_LOW, GLIDE_SIDEWAYS, ORBIT_CENTER,
    ZIGZAG_DART, TUMBLE_FALL, FLICKER_GLITCH, SWEEP_TRAIL, PULSE_RING,
    SWEEP_SHINE, WEAVE, TWINKLE,
}

/**
 * Everything the generic [TypeEffectCanvas] needs to render a visually
 * distinct charging effect for one Pokémon type: colour, particle
 * silhouette, motion pattern, density and a background tint used behind
 * the character artwork. No two types share the same (shape, motion) pair.
 */
data class TypeEffectSpec(
    val type: PokemonType,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundTint: Color,
    val shape: ParticleShape,
    val motion: MotionPattern,
    val baseParticleCount: Int,
    val periodMillis: Int,
)

object TypeEffectLibrary {

    val specs: Map<PokemonType, TypeEffectSpec> = listOf(
        TypeEffectSpec(PokemonType.NORMAL, Color(0xFFD7D3C8), Color(0xFFFFFFFF), Color(0xFF2A2A26), ParticleShape.ORB, MotionPattern.DRIFT_SETTLE, 10, 5200),
        TypeEffectSpec(PokemonType.FIRE, Color(0xFFF08030), Color(0xFFFFC178), Color(0xFF3A1706), ParticleShape.FLAME, MotionPattern.RISE_FLICKER, 16, 2200),
        TypeEffectSpec(PokemonType.WATER, Color(0xFF6890F0), Color(0xFFBFD7FF), Color(0xFF071A3A), ParticleShape.DROPLET, MotionPattern.FALL_AND_RIPPLE, 14, 2600),
        TypeEffectSpec(PokemonType.ELECTRIC, Color(0xFFF8D030), Color(0xFFFFF3B0), Color(0xFF332B04), ParticleShape.BOLT, MotionPattern.JAGGED_STRIKE, 9, 1400),
        TypeEffectSpec(PokemonType.GRASS, Color(0xFF78C850), Color(0xFFBCE8A0), Color(0xFF122B08), ParticleShape.LEAF, MotionPattern.FLUTTER_DOWN, 14, 3400),
        TypeEffectSpec(PokemonType.ICE, Color(0xFF98D8D8), Color(0xFFE6FBFB), Color(0xFF0C2B2B), ParticleShape.SNOWFLAKE, MotionPattern.DRIFT_SETTLE, 16, 4200),
        TypeEffectSpec(PokemonType.FIGHTING, Color(0xFFC03028), Color(0xFFFF9A8E), Color(0xFF2C0A08), ParticleShape.IMPACT_STREAK, MotionPattern.IMPACT_RADIATE, 8, 1100),
        TypeEffectSpec(PokemonType.POISON, Color(0xFFA040A0), Color(0xFFE2A6E2), Color(0xFF26082A), ParticleShape.BUBBLE, MotionPattern.BUBBLE_UP, 12, 3000),
        TypeEffectSpec(PokemonType.GROUND, Color(0xFFE0C068), Color(0xFFF2E0A8), Color(0xFF2E2409), ParticleShape.DUST_MOTE, MotionPattern.SWIRL_LOW, 14, 3600),
        TypeEffectSpec(PokemonType.FLYING, Color(0xFFA890F0), Color(0xFFDDD2FF), Color(0xFF1E1836), ParticleShape.FEATHER, MotionPattern.GLIDE_SIDEWAYS, 11, 3800),
        TypeEffectSpec(PokemonType.PSYCHIC, Color(0xFFF85888), Color(0xFFFFC2D7), Color(0xFF330A1A), ParticleShape.ORB, MotionPattern.ORBIT_CENTER, 10, 3000),
        TypeEffectSpec(PokemonType.BUG, Color(0xFFA8B820), Color(0xFFE0ED8F), Color(0xFF232A04), ParticleShape.WING_GLINT, MotionPattern.ZIGZAG_DART, 13, 1900),
        TypeEffectSpec(PokemonType.ROCK, Color(0xFFB8A038), Color(0xFFE7D9A0), Color(0xFF2A2309), ParticleShape.SHARD, MotionPattern.TUMBLE_FALL, 11, 2600),
        TypeEffectSpec(PokemonType.GHOST, Color(0xFF705898), Color(0xFFC6B7E8), Color(0xFF150F22), ParticleShape.SMOKE_WISP, MotionPattern.FLICKER_GLITCH, 10, 2400),
        TypeEffectSpec(PokemonType.DRAGON, Color(0xFF7038F8), Color(0xFFC3ABFF), Color(0xFF190C3A), ParticleShape.AURA_RIBBON, MotionPattern.SWEEP_TRAIL, 9, 3200),
        TypeEffectSpec(PokemonType.DARK, Color(0xFFE53935), Color(0xFFFFFFFF), Color(0xFF060606), ParticleShape.SHADOW_MOTE, MotionPattern.PULSE_RING, 8, 3600),
        TypeEffectSpec(PokemonType.STEEL, Color(0xFFB8B8D0), Color(0xFFFFFFFF), Color(0xFF212126), ParticleShape.METAL_GLINT, MotionPattern.SWEEP_SHINE, 10, 2600),
        TypeEffectSpec(PokemonType.FAIRY, Color(0xFFEE99AC), Color(0xFFFFE1EA), Color(0xFF301620), ParticleShape.RIBBON, MotionPattern.WEAVE, 12, 3400),
    ).associateBy { it.type } + mapOf(
        // STAR/TWINKLE reserved as a secondary accent used only inside the
        // fairy full-charge burst (see TypeEffectCanvas), not a top-level type.
    )

    fun forType(type: PokemonType): TypeEffectSpec = specs.getValue(type)
}
