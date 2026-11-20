package com.pokecharge.utils

import android.content.Context

/**
 * Placeholder for optional sound effects (brief §15). No audio assets are
 * bundled - `freesound.org` and `opengameart.org`, the sources suggested
 * for charge/spark/splash/complete sound effects, were not reachable from
 * the build sandbox (see assets/ASSET_SOURCES.md §4). The Settings screen's
 * "Sound effects" toggle is wired to this controller so adding real clips
 * later is a one-file change, but today every call here is a documented
 * no-op - it will never silently play nothing while claiming to play
 * something, and it never autoplays or overrides silent mode.
 */
object SoundController {
    enum class Cue { CHARGE_START, FULL_CHARGE, STAGE_ADVANCE }

    fun play(context: Context, cue: Cue, enabled: Boolean) {
        if (!enabled) return
        // Intentionally not implemented yet - see class doc.
    }
}
