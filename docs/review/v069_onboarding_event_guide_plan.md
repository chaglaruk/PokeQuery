# v0.6.9 onboarding and Event Guide plan

Date: 2026-07-02

Branch: `feature/v069-next`

## Phase 0 — Baseline

- PASS: Current branch is `feature/v069-next`.
- PASS: Working tree was clean before changes.
- PASS: HEAD is based on merged v0.6.8 commit `cee2a7f`.

## Phase 1 — Onboarding copy

- PASS: Replaced vague onboarding feature-card copy with product-specific value copy.
- PASS: Updated EN/TR/DE/ES/FR/IT.
- PASS: EN/TR real-phone screenshots show readable cards without visible overflow.

## Phase 2 — Event Guide data model

- PASS: Extended existing Event Guide feed model instead of adding a second architecture.
- PASS: Supports event id, title, status, dates, source state, summary, prep recommendation, suggested search, notes, and generic theme key.

## Phase 3 — Feed/fallback behavior

- PASS: Preserved live public feed, saved feed/cache, bundled fallback behavior.
- PASS: Fallback cards are useful and honestly labeled.

## Phase 4 — Public feed strategy

- PASS: Kept bundled fallback in `app/src/main/res/raw/event_context_fixture.json`.
- PASS: Kept repo feed sample in `docs/event-feed/pokequery-events.json`.
- PASS: Avoided scraping, private APIs, login, analytics, and hidden endpoints.

## Phase 5 — Event Guide UI

- PASS: Renders full event-style cards with source/status, summary, suggested search, copy button, and related prep notes.
- PASS: Uses generic Compose visuals only.

## Phase 6 — Tests

- PASS: Updated feed parsing and fallback tests.
- PASS: Added fixture/content assertions.
- PASS: Preserved locale and privacy regression coverage.

## Phase 7 — Screenshots

- PASS: Captured required real-phone screenshots under `docs/screenshots/v069_onboarding_event_guide/`.
- PASS: Created `contact_sheet.png`.

## Phase 8 — Validation

- PASS: Ran Gradle test/build/lint/bundle and runtime asset guard.
- PASS: Ran static checks for locale/test/safety/IP constraints.
