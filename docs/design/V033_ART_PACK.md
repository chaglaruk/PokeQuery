# PokeQuery v0.3.3 art pack

## Docs-only references
- `docs/design/reference_target_pokequery_approved.png`
- `docs/design/reference_target_pokequery_generated_showcase.png`
- `docs/design/current_contact_sheet_before_art_pack.png`

These files are visual references only and are not loaded by the runtime app.

## Runtime mapping
| Source asset | Runtime resource | Used by |
| --- | --- | --- |
| `assets/art_onboarding_hero.png` | `app/src/main/res/drawable-nodpi/v033_onboarding_hero.png` | Onboarding step 1 |
| `assets/art_home_header_bg.png` | `app/src/main/res/drawable-nodpi/v033_home_header_bg.png` | Home/header map background |
| `assets/art_safe_cleanup_header.png` | `app/src/main/res/drawable-nodpi/v033_safe_cleanup_header.png` | Safe Cleanup cards/detail |
| `assets/art_candy_prep_header.png` | `app/src/main/res/drawable-nodpi/v033_candy_prep_header.png` | 2x Candy cards/detail/warnings |
| `assets/art_trade_fodder_header.png` | `app/src/main/res/drawable-nodpi/v033_trade_fodder_header.png` | Trade Fodder cards/detail |
| `assets/art_lucky_trade_header.png` | `app/src/main/res/drawable-nodpi/v033_lucky_trade_header.png` | Lucky Trade cards/detail |
| `assets/art_pvp_header.png` | `app/src/main/res/drawable-nodpi/v033_pvp_header.png` | PvP cards/detail |
| `assets/art_nundo_header.png` | `app/src/main/res/drawable-nodpi/v033_nundo_header.png` | Hundo/Nundo cards/detail |
| `assets/art_app_icon_source.png` | `app/src/main/res/drawable-nodpi/v033_app_icon_source.png` | Adaptive launcher icon foreground |

## Runtime guard
- `scripts/check_runtime_assets.py` allows only the named v033 runtime resources above.
- Screenshot, mockup, contact sheet, and full phone showcase images remain blocked as runtime assets.

## IP/release note
- The v033 art is treated as user-provided app art for this release candidate.
- No official Pokémon/Niantic/Nintendo/Game Freak assets were added by Codex.
- If any source art is later found to be unlicensed or too close to protected franchise art, replace the matching runtime resource before Play Store release.
