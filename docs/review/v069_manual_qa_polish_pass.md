# v0.6.9 Manual QA Polish Pass

## Phase 1: Localization + Copy Polish
- [ ] TODO: Localize risk bar labels (Low Risk, Medium Risk, etc.) in all languages.
- [ ] TODO: Localize "About count" bar/label in all languages.
- [ ] TODO: Localize "What does this do?" bar/label in all languages.
- [ ] TODO: Rewrite "Why this risk?" text in ELI5 style (more human, less technical, natural in Turkish).
- [ ] TODO: Review and improve "Search String" section text and default string presentation (IP-safe, no `|`, no broken logic).
- [ ] TODO: Change "Ayrıştır" label/button text to a more natural term in Turkish and equivalent in other languages.

## Phase 2: Search Assistant Visual / Empty-State Improvement
- [ ] TODO: Add integrated visual treatment below the action button (full-bleed/soft background, IP-safe, no boxed image).

## Phase 3: Event Guide UI Polish
- [ ] TODO: Add integrated full-screen/background visual treatment.
- [ ] TODO: Make Refresh button smaller and subtler.
- [ ] TODO: Replace two-card structure with one stronger main event card (current or nearest upcoming).
- [ ] TODO: Add featured Pokémon name, bonuses, short ELI5 explanation, suggested search string, copy action, event notes, source label, last checked state.
- [ ] TODO: Ensure lower "event notes" area belongs to the displayed main event.
- [ ] TODO: Rewrite copy to be human and ELI5 style.

## Phase 4: Real Event Information
- [ ] TODO: Update Event Guide to show real useful event info (via `docs/event-feed/pokequery-events.json` and `event_context_fixture.json`).
- [ ] TODO: Ensure human-friendly content, not just a debug panel.

## Phase 5: Tests
- [ ] TODO: Update/Add tests for localized labels, copy changes, Event Guide behavior, locale parity, no MissingTranslation, no ignored tests.
- [ ] TODO: Ensure runtime asset guard passes and no official assets introduced.

## Phase 6: Screenshots
- [ ] TODO: Install debug APK on real phone (192.168.1.126:5555).
- [ ] TODO: Capture required screenshots and create contact sheet.

## Phase 7: Validation
- [ ] TODO: Run git diff, gradlew test, assemble, lint, bundleRelease, and asset check.
