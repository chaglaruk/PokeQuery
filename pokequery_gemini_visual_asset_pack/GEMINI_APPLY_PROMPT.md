# PokeQuery v0.3.4 visual + UX refinement prompt for Gemini/Codex

You are working inside the `PokeQuery` Android repo. Continue from the current repo state and implement a focused visual/UX refinement pass.

## Ground truth and constraints
- Use `docs/design/reference_ui_target.png` as the visual north star.
- Also use the shipped asset files in `docs/design/` / provided handoff pack.
- Important: the app should look close to the target screenshot in overall polish and layout feel.
- Do **not** add Pokémon, fake Pokémon, mascot creatures, animal silhouettes, or creature illustrations.
- Do **not** introduce copyrighted franchise artwork.
- Keep the art abstract and premium: night map scenes, glowing location pins, shields, routes, soft stars, search motifs, clean geometry, collector/search atmosphere.
- Keep the app text-only/copy-only behavior intact.

## Asset application
Take the provided files and wire them into runtime usage. If needed, copy/rename them into `app/src/main/res/drawable-nodpi/` using these names:
- onboarding_hero.png
- home_header_bg.png
- safe_cleanup_header.png
- candy_prep_header.png
- trade_fodder_header.png
- lucky_trade_header.png
- pvp_header.png
- nundo_header.png
- app_icon_source.png

Also update the adaptive launcher icon to use `app_icon_source.png` cleanly.

## Required UI changes
1. **Match the target style more closely**
   - Stronger premium dark-blue visual language.
   - More faithful spacing, card proportions, header treatment, and typography hierarchy.
   - Remove any childish / repetitive card art feel.
   - Make all goal cards feel more distinct through composition and icon treatment, not through random creatures.

2. **Goal detail screens: reduce scrolling**
   - Remove unnecessary mini text/icon blocks sitting directly on top of the search string unless they are genuinely high-value.
   - Keep risk header, string area, copy action, warnings, and “what does this do?” but rebalance the order.
   - Move **Search Options** higher so users do not need excessive scrolling.
   - Keep the generated string visible and copyable without extra steps.
   - Unify the favorite action: do not keep duplicate favorite controls in two different places. One clear favorite location only.

3. **Knowledge Base improvements**
   - Current structure is decent, but explanations are too short.
   - Expand every important operator/term with a plain-English explanation: what it is, what it does, and a simple example.
   - Add better explanatory copy so users understand why a string returns results.
   - Keep search and collapsible cards.

4. **Settings improvements**
   - Add more meaningful settings where safe and appropriate.
   - At minimum include settings for:
     - first-use guide reset
     - copy behavior
     - duplicate-threshold preference if still relevant
     - visual density / compact mode if useful
     - language/search-term mode handling (see next section)
   - Do not add dangerous settings that reduce safety by default.

5. **Game language handling**
   The in-game search terms depend on Pokémon GO language. Example: in Turkish client `legendary` may not work while `efsanevi` does.
   Implement a safe handling strategy:
   - Add a user-facing setting for **Search Term Language** or **Game Language**.
   - Start with at least English and Turkish.
   - Introduce a small internal mapping layer for the most important terms used by the app.
   - Generated strings should use the selected language automatically.
   - Document any limitations clearly in settings/help.
   - Do not guess silently if a term is uncertain.
   - Add tests for the mapping layer.

6. **Home / onboarding polish**
   - Keep the onboarding visually impressive like the target.
   - Keep home cards compact, balanced, and visually organized.
   - No unused awkward empty areas.
   - Preserve clarity about what the app does.

7. **Back navigation animation**
   - Ensure back transitions remain smooth and not shrinky/janky.
   - If any old scale-out behavior survives anywhere, remove it.

## Implementation expectations
- Inspect current screens and refactor as needed; do not paper over problems.
- Update screenshots and contact sheet deterministically.
- Keep runtime asset checks passing.
- Add/update tests if you introduce a search-term language mapping layer.
- Bump version to `0.3.4` if appropriate.

## Required output workflow
1. Apply the art assets.
2. Implement the UX refinements above.
3. Run:
   - `./gradlew clean test assembleDebug --no-daemon --console=plain`
   - `python scripts/check_runtime_assets.py`
   - install APK on device/emulator
   - screenshot capture flow
   - `python scripts/create_contact_sheet.py`
4. Commit and push.

## Final report format
Return:
- latest commit hash
- whether push succeeded
- build/test result
- asset guard result
- APK path
- contact sheet path
- concise list of files changed
- explicit note on how language-specific search-term handling was implemented
- any remaining limitations
