#!/usr/bin/env python3
"""
POKECHARGE asset pipeline helper.

Reads app/src/main/assets/pokemon_index.json (the single hand-maintained
source of truth for Pokemon metadata) and:

  1. Verifies that every referenced artwork/animation path actually exists
     under app/src/main/assets/pokemon/<type>/<name>/...
  2. Writes a per-Pokemon metadata.json next to its artwork, so the asset
     folder is self-describing even outside the app.

Run from the repository root:
    python3 tools/generate_pokemon_index.py
"""
import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app" / "src" / "main" / "assets"
INDEX_PATH = ASSETS / "pokemon_index.json"
POKEMON_DIR = ASSETS / "pokemon"
STAGES = ["idle", "charging", "full_charge", "disconnect"]


def main() -> int:
    index = json.loads(INDEX_PATH.read_text())
    missing = []
    written = 0

    for mon in index["pokemon"]:
        type_slug = mon["profileType"]
        name_slug = mon["name"].lower()
        pdir = POKEMON_DIR / type_slug / name_slug
        artwork = pdir / "artwork.webp"

        if not artwork.exists():
            missing.append(str(artwork.relative_to(ASSETS)))
            continue

        anim_paths = {}
        for stage in STAGES:
            stage_dir = pdir / stage
            stage_dir.mkdir(parents=True, exist_ok=True)
            # No external animation files were obtainable for any Pokemon
            # (see assets/ASSET_SOURCES.md) - these stages are rendered
            # procedurally at runtime by animation.effects.TypeEffectEngine,
            # driven by the profileType + battery stage. The folders are
            # kept so a future asset drop (Lottie/sprite sheets) can be
            # placed here without any code change beyond AssetProvider.
            anim_paths[stage] = f"pokemon/{type_slug}/{name_slug}/{stage}/"

        meta = {
            "dexId": mon["dexId"],
            "name": mon["name"],
            "profileType": type_slug,
            "officialTypes": mon["officialTypes"],
            "role": mon["role"],
            "description": mon["description"],
            "artwork": f"pokemon/{type_slug}/{name_slug}/artwork.webp",
            "animations": anim_paths,
            "animationRenderer": "procedural-canvas",
        }
        (pdir / "metadata.json").write_text(json.dumps(meta, indent=2) + "\n")
        written += 1

    print(f"Wrote {written} metadata.json files.")
    if missing:
        print(f"MISSING artwork for {len(missing)} entries:")
        for m in missing:
            print(" -", m)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
