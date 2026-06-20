# Visual target contract

## Runtime visual slots
- App icon foreground/background: adaptive resources; v0.3.3 uses `v033_app_icon_source.png` as the launcher foreground.
- Onboarding full hero: v0.3.3 uses `v033_onboarding_hero.png`.
- Home full map background/header: v0.3.3 uses `v033_home_header_bg.png` plus Compose overlays.
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
- Final commissioned art can replace the v033 slots by matching slot purpose and size.
- New bitmap runtime assets must be added to `scripts/check_runtime_assets.py` allowlist before use.
