# Visual target contract

## Runtime visual slots
- App icon foreground/background: adaptive vector resources in `res/drawable`.
- Onboarding full hero: Compose/vector art backed by `onboarding_hero.png` where useful.
- Home full map background/header: `home_header_bg.png` plus Compose map overlays.
- Goal card art slots:
  - safe cleanup
  - 2x candy
  - trade fodder
  - hundo/nundo
  - PvP
  - lucky trade
  - expert builder
  - presets/knowledge support
- Detail side art slots:
  - safe cleanup detail
  - candy/count warning detail
  - trade/warning detail
  - generic creature/mascot silhouette

## Rules
- Large card art should render at roughly 70-140dp.
- Tiny 24dp icons may support text, but cannot be the primary visual language.
- Runtime assets must be IP-safe originals: no official Pokémon, Poké Ball, Niantic, Nintendo, Game Freak, or screenshot crops.
- `docs/design/*` and `docs/design_targets/*` are reference-only and must not be loaded at runtime.
- Search-string generation remains text-only and copy-only.

## Replacement contract
- Final commissioned art can replace the Compose/vector slots by matching slot purpose and size.
- New bitmap runtime assets must be added to `scripts/check_runtime_assets.py` allowlist before use.
