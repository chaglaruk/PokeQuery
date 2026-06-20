You are working inside the PokeQuery Android repo.

Your job is to implement a **major UI refinement pass** using the asset folder `pokequery_ui_refinement_asset_pack` in the repo root.

## Non-negotiable goal
Make the real app look much closer to `pokequery_ui_refinement_asset_pack/reference_ui_target.png`.

Important: the user does **not** want Pokémon, creature art, or fake animal/monster illustrations in the real UI. Match the reference mainly through:
- layout
- spacing
- typography hierarchy
- iconography
- chip styling
- card styling
- polished top bars
- polished bottom navigation
- better wordmark/logo treatment
- better onboarding composition

Do not leave the UI in its current “same background repeated on many cards” state.

## Assets to use
Use the files described in `pokequery_ui_refinement_asset_pack/ASSET_MAPPING.md`.
Copy the runtime PNGs into `app/src/main/res/drawable-nodpi/` and wire them into the app.
Do **not** ship `reference_ui_target.png` as a runtime asset.

## Required visual fixes

### 1) Brand / app name
- The app name must stop looking like plain oversized text.
- Create a compact, logo-like brand treatment using `logo_wordmark_source.png` as inspiration or source.
- Reduce wasted vertical space in the top area.
- Use the compact wordmark in onboarding and the home top bar.

### 2) Onboarding
Rebuild onboarding to closely match the target reference:
- visually richer first impression
- better composition and spacing
- refined indicator dots
- better CTA placement and sizing
- icon row below the hero should be styled and spaced like the reference
- text hierarchy should feel premium and compact, not stretched
- use `onboarding_hero_scene.png` as the main hero visual

### 3) Home / Builder screen
Rebuild the builder screen to look much closer to the target reference:
- add hamburger menu on the top-left
- compact brand wordmark near top
- search icon on the top-right
- use `home_header_bg.png` for the header area
- make the screen look more compact and cohesive
- reduce dead space
- improve alignment and card sizing
- the goal cards should look like the target reference in density and polish

### 4) Goal card icons
Current icons feel weak / repetitive. Fix this.
Use these distinct icons:
- Safe Cleanup -> `icon_safe_cleanup.png`
- 2x Candy Prep -> `icon_candy_prep.png`
- Trade Fodder -> `icon_trade_fodder.png`
- Hundo Check -> `icon_hundo_check.png`
- Untagged Cleanup -> `icon_untagged_cleanup.png`
- Expert Builder -> `icon_expert_builder.png`

They should be displayed prominently and crisply, not tiny and washed out. Avoid making every card look like “just another digital map panel.”

### 5) Goal detail screens
Rework the detail screens so they are closer to the target reference and require less scrolling.

Fix all of the following:
- remove the redundant small icon/text area sitting above or inside the search string block unless it serves a clear polished purpose
- bring `Search Options` higher so the user does not need so much scrolling before seeing the useful controls
- keep **one** favorite action only; do not duplicate favorite in two places
- make the copy CTA placement closer to the target reference
- style the risk header more like the target
- use `detail_header_blue.png` on low-risk or neutral detail screens
- use `detail_header_gold.png` on medium-risk screens
- preserve instant live-updating search strings when toggles change

### 6) Protected categories chips
The protected category items currently do not look polished enough.
Make them look much closer to the target reference:
- pill chips / capsules
- stronger visual hierarchy
- better spacing
- more polished icon + label feel
- cleaner wrapping behavior

### 7) Bottom navigation
The bottom navigation should look much closer to the target reference:
- better icons
- better label spacing
- tighter, more premium layout
- more polished selection state
Use or adapt vector icons if needed, but the final result must visually resemble the target reference more closely.

### 8) Knowledge Base
Keep the search and expandable cards, but improve the content quality.
The user said current explanations are too weak.
Do the following:
- expand term descriptions substantially
- explain what each search term does, when to use it, and example usage
- keep the screen tidy and readable
- do not let it feel like a pasted raw dictionary

### 9) Settings
Add / expose more meaningful settings, and make sure the screen looks finished.
At minimum expose:
- Search Language: Auto / English / Turkish
- Copy Behavior: Always Warn / Confirm Risky Copy / Direct Copy (if safe and already supported)
- Visual Density: Comfortable / Compact
- Default Duplicate Threshold: Count 2 / Count 3
- Reset onboarding
- Clear favorites
- Clear history

### 10) Search language handling
The app must safely handle game-language differences.
The user explicitly said that English terms do not work in a Turkish game client.
Implement or keep a proper mapping layer between internal canonical tokens and output strings.

Requirements:
- the engine may internally use canonical English terms
- before output/copy, map them based on selected Search Language
- support at least Auto / English / Turkish
- examples:
  - `legendary` -> `efsanevi`
  - `shiny` -> `parlak`
  - `traded` -> `takaslanan`
- numeric or structural syntax must remain correct
- verify that mapped strings remain syntactically valid

If Auto is already partially implemented, harden it. If not, implement a safe explicit setting and keep Auto conservative.

## Technical guardrails
- Do not regress the current string-generation logic.
- Do not reintroduce old multi-step preview/review flows.
- Preserve live-updating goal detail behavior.
- Keep the app private/offline/copy-only.
- Do not add Pokémon or creature art.
- Remove or stop using any stale art that conflicts with this direction.

## File / code tasks
1. Copy runtime assets from `pokequery_ui_refinement_asset_pack` into `app/src/main/res/drawable-nodpi/`.
2. Update launcher / brand presentation only if beneficial, but do not break existing package/signing setup.
3. Refactor Compose screens as needed:
   - onboarding
   - home/builder
   - goal detail
   - knowledge base
   - settings
   - bottom nav / top bars
4. Improve or add vector-based UI icons where asset PNGs are not needed.
5. Expand knowledge-base content.
6. Verify search-language mapping logic.
7. Remove stale or unused visual assets if they are no longer relevant.

## Validation steps (must run)
Run all of the following and do not stop early:
- `git status --short`
- `./gradlew clean test assembleDebug --no-daemon --console=plain`
- `python scripts/check_runtime_assets.py`
- install the APK on the connected device/emulator
- run the screenshot capture script
- regenerate the contact sheet
- inspect the screenshots for instruction-following

## Expected final report
When done, report:
- latest commit hash
- whether pushed or not
- build/test result
- asset guard result
- APK path
- contact sheet path
- exact list of screens materially improved
- exact list of settings exposed
- whether search language mapping is implemented and how
- any remaining gaps vs the reference

## Quality bar
Do not do a minimal pass.
This should be a serious visual refinement so the real app is **noticeably closer** to the supplied target reference in composition, density, polish, chip styling, navigation, and brand treatment.
