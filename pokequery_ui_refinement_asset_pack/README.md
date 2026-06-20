# PokeQuery UI Refinement Asset Pack

This pack contains:
- production-usable PNG assets
- a docs-only UI reference target
- an asset mapping file
- a Gemini implementation prompt

## Recommended placement
Put the whole `pokequery_ui_refinement_asset_pack` folder in the **repo root** of your PokeQuery project.

Example:
- `C:\Users\Caglar\Desktop\PokeQuery\pokequery_ui_refinement_asset_pack\...`

Then give Gemini the prompt in `GEMINI_APPLY_PROMPT.md` and tell it to use the assets from that folder.

Gemini should:
1. copy the runtime PNGs into `app/src/main/res/drawable-nodpi/`
2. update the Compose UI to use them
3. leave `reference_ui_target.png` as docs-only
4. rebuild screenshots and contact sheet
