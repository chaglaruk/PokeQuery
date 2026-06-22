# Repo Banner Specification

This file documents the requirements and current implementation of the GitHub
social-preview banner for the PokeQuery repository. The banner is generated
programmatically from `scripts/make_readme_banner.py`, so it contains **only
original artwork** — no third-party or copyrighted assets.

## Output

- File: `docs/readme/pokequery_repo_banner.png`
- Size: 1280 × 640 (GitHub social preview ratio)
- Format: PNG (optimized)

## Visual requirements (locked)

- **Background:** dark-navy vertical gradient (`#081020` → `#0c1c38`) with a faint
  cyan grid overlay (search/query motif).
- **Wordmark:** "Poke" in white, "Query" in cyan (`#00e0ff`), with a soft cyan glow.
  Arial Bold (or equivalent geometric sans). It is the **PokeQuery** wordmark — not
  the Pokémon logo.
- **Tagline:** "Build safer Pokémon GO search strings." in muted blue-grey, centered
  under the wordmark, with a short cyan underline accent.
- **Motif:** an abstract geometric shield containing a stylized search-query glyph
  (`{ }` + dot) — original geometric form.
- **Footer chips:** `OFFLINE-FIRST · NO TRACKING · NO LOGIN · NO ADS` with small
  cyan bullets.
- **Accent sparks:** small four-point stars in cyan / amber / white, placed subtly.

## Hard exclusions (IP safety)

The banner **must never** contain:

- ❌ Poké Ball, Great/Ultra/Master Ball imagery
- ❌ Pokémon creatures, silhouettes, or stylizations
- ❌ The official Pokémon logo or wordmark
- ❌ Niantic / Nintendo / The Pokémon Company logos
- ❌ The official Pokémon yellow/red color pairing or game UI chrome
- ❌ Any font designed to imitate the Pokémon title font

## Regeneration

```
python scripts/make_readme_banner.py
```

Requires Pillow. Output path is fixed; re-running overwrites safely.

## Future tweaks

- Swap Arial Bold for a licensed display sans once chosen (update `FONT_BOLD`).
- Add a translucent "v0.5.2" version chip in a corner if a versioned banner is
  ever needed for release notes.
- Keep the shield motif as the brand anchor — do not replace it with a ball shape.
