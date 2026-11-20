# POKÉCHARGE — Asset Sources & Licensing

This document records provenance for every non-code asset shipped in
`app/src/main/assets/`, as required by the project brief. It is generated
and maintained manually alongside `tools/generate_pokemon_index.py`.

## 0. Sandbox network constraints (why this list is shorter than the brief's wishlist)

The brief asks for assets from PokéAPI's REST API, LottieFiles, OpenGameArt,
Freesound, and Google Fonts. The development sandbox this project was built
in only has outbound network access to: `github.com`, `codeload.github.com`,
`api.github.com`, `registry.npmjs.org`, `pypi.org`, `files.pythonhosted.org`.

- `pokeapi.co`, `lottiefiles.com`, `opengameart.org`, `freesound.org`,
  `fonts.google.com` were **not reachable**. No files were downloaded from
  them, and none are claimed to be.
- `github.com` / `api.github.com` **were** reachable, so real official
  Pokémon artwork was retrieved from the public `PokeAPI/sprites` GitHub
  repository (which mirrors Pokémon Company/Game Freak artwork for
  non-commercial developer use, the same data source PokéAPI itself serves).
- Every animation effect in this app (fire, electricity, water, frost,
  shadows, ribbons, etc.) is **procedurally generated at runtime** with
  Jetpack Compose `Canvas`, not downloaded. This is explicitly called out
  below and in code comments — it is never presented as a downloaded asset.
- No audio assets are bundled. Sound effects are a documented placeholder
  (see §4) because freesound.org/opengameart.org were unreachable; the
  Settings screen's "Sound effects" toggle has no audio behind it yet.
- No custom font files are bundled. The UI uses the system default
  sans-serif plus `FontFamily.Monospace` for dot-matrix/technical text,
  which satisfies the visual brief without requiring a font download.

## 1. Pokémon artwork

| Field | Value |
|---|---|
| Source | `https://github.com/PokeAPI/sprites` (branch `master`), path `sprites/pokemon/other/official-artwork/<dex-id>.png` |
| Retrieval method | GitHub Contents API (`api.github.com/repos/PokeAPI/sprites/contents/...`), base64-decoded, one file per request |
| Retrieval date | 2026-11-20 |
| Original format | PNG, RGBA, ~475×475 |
| Shipped format | WebP, resized to 512×512 max, `convert -strip` (ImageMagick), transparency preserved |
| Shipped path | `app/src/main/assets/pokemon/<type>/<name>/artwork.webp` |
| License / terms | The `PokeAPI/sprites` repository is distributed under a 3-Clause BSD-style license for the repository's own code/structure, **but the sprite and artwork image files themselves are explicitly called out by PokéAPI as the property of Nintendo / Game Freak / The Pokémon Company and are provided for fan, non-commercial, informational use only.** PokéAPI's own usage guidelines state the artwork must not be used to imply official affiliation and should not be the basis of a commercial product. |
| Attribution | "Pokémon and Pokémon character names are trademarks of Nintendo." Artwork via PokéAPI (pokeapi.co) / PokeAPI/sprites contributors. |
| ⚠️ Commercial use flag | **Not cleared for commercial/Play Store distribution as-is.** This app must either (a) stay a personal/non-commercial build, (b) obtain a license from The Pokémon Company, or (c) replace this artwork with fully original character designs before any commercial release. Treat every file below as a development/placeholder-grade asset until that decision is made. |

Full list of the 36 artwork files retrieved (primary + one secondary
Pokémon per type), by National Pokédex number:

| # | Pokémon | Profile type | Role | Official type(s) |
|---|---|---|---|---|
| 1 | Bulbasaur | grass | primary | Grass / Poison |
| 4 | Charmander | fire | primary | Fire |
| 7 | Squirtle | water | primary | Water |
| 12 | Butterfree | bug | primary | Bug / Flying |
| 18 | Pidgeot | flying | primary | Normal / Flying |
| 25 | Pikachu | electric | primary | Electric |
| 27 | Sandshrew | ground | primary | Ground |
| 36 | Clefable | fairy | secondary | Fairy |
| 59 | Arcanine | fire | secondary | Fire |
| 65 | Alakazam | psychic | secondary | Psychic |
| 68 | Machamp | fighting | secondary | Fighting |
| 94 | Gengar | poison | primary | Ghost / Poison |
| 95 | Onix | rock | primary | Rock / Ground |
| 131 | Lapras | ice | secondary | Water / Ice |
| 133 | Eevee | normal | primary | Normal |
| 134 | Vaporeon | water | secondary | Water |
| 135 | Jolteon | electric | secondary | Electric |
| 143 | Snorlax | normal | secondary | Normal |
| 149 | Dragonite | dragon | primary | Dragon / Flying |
| 150 | Mewtwo | psychic | primary | Psychic |
| 169 | Crobat | poison | secondary | Poison / Flying |
| 197 | Umbreon | dark | primary | Dark |
| 200 | Misdreavus | ghost | primary | Ghost |
| 212 | Scizor | bug | secondary | Bug / Steel |
| 248 | Tyranitar | rock | secondary | Rock / Dark |
| 306 | Aggron | steel | secondary | Steel / Rock |
| 354 | Banette | ghost | secondary | Ghost |
| 359 | Absol | dark | secondary | Dark |
| 373 | Salamence | dragon | secondary | Dragon / Flying |
| 376 | Metagross | steel | primary | Steel / Psychic |
| 445 | Garchomp | ground | secondary | Dragon / Ground |
| 448 | Lucario | fighting | primary | Fighting / Steel |
| 470 | Leafeon | grass | secondary | Grass |
| 471 | Glaceon | ice | primary | Ice |
| 700 | Sylveon | fairy | primary | Fairy |
| 715 | Noivern | flying | secondary | Flying / Dragon |

Per-Pokémon `metadata.json` files (generated by
`tools/generate_pokemon_index.py` from `assets/pokemon_index.json`) sit next
to each `artwork.webp` and repeat this provenance in machine-readable form.

## 2. Elemental charging animations — procedurally generated, not downloaded

Every idle / charging / full-charge / disconnect animation is rendered live
by `animation/effects/TypeEffectEngine.kt` using Jetpack Compose `Canvas`.
No Lottie, Rive, GIF, or sprite-sheet animation files are bundled, because
the only source suggested for them (LottieFiles, OpenGameArt) was not
reachable from the build environment.

This is a deliberate, documented fallback per the project brief ("If no
suitable animation exists, create a custom animation... never claim an
animation was retrieved if it was actually generated procedurally"). Each
of the 18 types has its own particle shape, palette, and motion curve (see
`TypeEffectSpec` table) so effects are not simple recolors of one another.

Original work, MIT-equivalent / public-domain-equivalent (no third-party
asset license applies since nothing was downloaded).

## 3. Fonts

No font files are bundled. UI text uses the platform default sans-serif
(`FontFamily.Default`) for general copy and `FontFamily.Monospace` for
dot-matrix/technical labels and the big battery counter, which are both
available on-device with zero licensing concerns. `fonts.google.com` was
unreachable from the sandbox, so no Google Font (e.g. a true dot-matrix
face) was downloaded — revisit this once network access allows it.

## 4. Sound effects

Not bundled. `freesound.org` and `opengameart.org` were unreachable. The
Settings screen exposes a "Sound effects" toggle and `SoundController`
stub wired for future `SoundPool` playback, but it is a no-op today. This
is intentional and documented rather than silently faked — see
`utils/SoundController.kt`.

## 5. App icon / launcher assets

The launcher icon is a simple procedurally-drawn vector (`ic_launcher`
adaptive icon built from Compose/XML vector drawables), not a downloaded
image, to stay consistent with the Nothing OS dot-matrix identity without
any licensing question.
