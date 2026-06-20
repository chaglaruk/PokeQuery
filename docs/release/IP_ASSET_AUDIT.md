# IP & Asset Audit

**Status**: ALL CLEAR (with recommendations)

## v0.3.2 update
- Runtime UI now uses IP-safe Compose/vector mascot silhouettes, map lines, shield, magnifier, cards, and glow effects.
- The approved visual reference in `docs/design/reference_target_pokequery.png` remains documentation-only.
- No official Pokémon character art, Poké Ball art, Niantic/Nintendo/Game Freak logos, or screenshot crops are loaded at runtime.
- User-supplied official/lookalike character art would be prototype-only unless separately licensed and accepted for release risk.

## v0.3.3 art pack update
- Added user-provided v033 runtime art under `app/src/main/res/drawable-nodpi/`.
- Added approved/reference/contact-sheet images under `docs/design/`; these are documentation-only and not runtime resources.
- The runtime asset guard allowlists only the named v033 art files and still blocks screenshot/mockup/contact/full-phone assets.
- Codex did not add official Pokémon/Niantic/Nintendo/Game Freak assets. Final Play Store release still depends on the user having rights to the supplied v033 art pack.

## Audit Checklist
- [x] **No official Pokémon character assets**: Verified. All icons are abstract generic shapes (stars, candies, tags, cleanup brushes).
- [x] **No Poké Ball assets**: Verified. No branded spheres exist in `drawable-nodpi`.
- [x] **No Niantic/Nintendo/Game Freak logos**: Verified.
- [x] **No full mockup crops used at runtime**: Verified. `docs/design_targets` holds the reference mockups securely away from the runtime `src`.
- [x] **Generated assets are original/helper-style**: Verified. `onboarding_hero.png` and headers are stylized abstract UI graphics.
- [x] **App Launcher Icon**: Verified. `ic_launcher` uses generic Magnifier + Grid + Shield elements and does not resemble official Pokémon icons.

## Recommendation
Double-check any specific icon designs before final release to ensure no vector shapes are accidentally identical to registered in-game UI shapes, but visually the current abstract assets appear generic and legally distinct as helper utilities.
