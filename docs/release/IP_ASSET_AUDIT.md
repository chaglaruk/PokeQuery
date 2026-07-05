# IP & Asset Audit

**Status**: RUNTIME SPRITE IP RISK ACCEPTED BY USER REQUEST

## v0.3.2 update

- Runtime UI now uses IP-safe Compose/vector mascot silhouettes, map lines, shield, magnifier, cards, and glow effects.
- Historical visual reference folders were removed during the v0.6.8 cleanup; final visual evidence lives under `docs/screenshots/v068_clean_ui_repair_after_rollback/`.
- No official Pokémon character art, Poké Ball art, Niantic/Nintendo/Game Freak logos, or screenshot crops are loaded at runtime.
- User-supplied official/lookalike character art would be prototype-only unless separately licensed and accepted for release risk.

## v0.3.3 art pack update

- Added user-provided v033 runtime art under `app/src/main/res/drawable-nodpi/`.
- Historical approved/reference/contact-sheet images were repo-cleanup artifacts only and are no longer required for runtime or release.
- The runtime asset guard allowlists only the named v033 art files and still blocks screenshot/mockup/contact/full-phone assets.
- Codex did not add official Pokémon/Niantic/Nintendo/Game Freak assets. Final Play Store release still depends on the user having rights to the supplied v033 art pack.

## v0.6.9 Event Guide sprite update

- Added Event Guide Pokémon character sprites from PokeAPI sprites raw GitHub URLs because the user explicitly requested real Pokémon visuals despite release/IP risk.
- Runtime files: `event_unown.png`, `event_kangaskhan.png`, `event_mr_mime.png`, `event_heracross.png`, `event_corsola.png`, `event_pikachu.png`, `event_necrozma.png`, `event_eevee.png`, `event_gimmighoul.png`, `event_zeraora.png`, `event_wurmple.png`.
- Sources are `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/{id}.png` for IDs 201, 115, 122, 214, 222, 25, 800, 133, 999, 807, and 265.
- These are Pokémon character sprites and may carry Pokémon/Nintendo/Game Freak/The Pokémon Company IP risk.

## v0.7.1 Event Guide Mewtwo update

- Added clean Event Guide Mewtwo sprite asset: `event_mewtwo.png`.
- Source is PokeAPI Sprite #150 (`https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/150.png`).
- Checkerboard background was verified and removed completely; the sprite is stored as a clean transparent `PNG (96, 96) RGBA` asset.
- This is a Pokémon character sprite and carries official Pokémon character IP risk, which is accepted by the user for internal/corrective test purposes.

## Audit Checklist

- [ ] **No official Pokémon character assets**: Not true for v0.6.9. Event Guide includes Pokémon character sprites by explicit user request.
- [x] **No Poké Ball assets**: Verified. No branded spheres exist in `drawable-nodpi`.
- [x] **No Niantic/Nintendo/Game Freak logos**: Verified.
- [x] **No full mockup crops used at runtime**: Verified. Runtime UI loads only resources under `app/src/main/res/`.
- [x] **Generated assets are original/helper-style**: Verified. `onboarding_hero.png` and headers are stylized abstract UI graphics.
- [x] **App Launcher Icon**: Verified. `ic_launcher` uses generic Magnifier + Grid + Shield elements and does not resemble official Pokémon icons.

## Recommendation

Do not treat the v0.6.9 Event Guide sprite build as release-safe without separate legal approval or replacing those sprites with app-owned fallback visuals.
