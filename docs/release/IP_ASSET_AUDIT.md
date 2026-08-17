# IP & Asset Audit

**Status:** RELEASE-SAFE GENERIC RUNTIME ASSETS

## Current runtime policy

PokeQuery runtime UI uses app-owned/generic helper artwork only: abstract cards, shields, magnifiers, map/event glyphs, gradients, lines, and other utility-style UI graphics.

The project must not ship official Pokémon character artwork, Poké Ball artwork, or Niantic/Nintendo/Game Freak/The Pokémon Company logos as runtime UI assets unless separately licensed.

## Historical Event Guide sprite note

Earlier internal/corrective builds used 12 Pokémon character sprites sourced from the public PokeAPI sprite repository under these resource names:

- `event_unown.png`
- `event_kangaskhan.png`
- `event_mr_mime.png`
- `event_heracross.png`
- `event_corsola.png`
- `event_gimmighoul.png`
- `event_pikachu.png`
- `event_necrozma.png`
- `event_eevee.png`
- `event_zeraora.png`
- `event_wurmple.png`
- `event_mewtwo.png`

Those character images were explicitly treated as an internal/testing IP risk and were not suitable for the next production candidate.

## Pre-AAB remediation

Before the next Android AAB, all 12 resource paths above are replaced with the same app-owned neutral Event Guide glyph: a generic dark utility tile with a search ring/handle and abstract event spark. The replacement contains no Pokémon character silhouette, Poké Ball, franchise logo, or copied official artwork.

Resource filenames are intentionally preserved so existing widget/Compose references remain stable while the underlying runtime artwork becomes generic and release-safe.

## Audit checklist

- [x] **No official Pokémon character art at runtime** — historical Event Guide character sprite bytes replaced with original generic PokeQuery artwork.
- [x] **No Poké Ball art** — no branded sphere imagery is used.
- [x] **No Niantic/Nintendo/Game Freak/The Pokémon Company logos**.
- [x] **No screenshot/mockup/contact-sheet crops used as runtime resources**.
- [x] **Launcher icon** — generic magnifier/grid/shield utility design.
- [x] **Event Guide placeholder/glyph** — app-owned abstract event/search artwork.

## Release rule

If a future change reintroduces recognizable Pokémon character art or other franchise-owned visual assets, it must be treated as a separate legal/release decision and must not silently bypass the runtime asset audit.
