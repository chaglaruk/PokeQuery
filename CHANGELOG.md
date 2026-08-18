# Changelog

Notable changes to PokeQuery. Dates are grouped by release.

The current Android release candidate is **0.7.6** (`versionCode 26`).
The Web/PWA version remains **0.7.3** and is versioned independently.

The format is loosely based on [Keep a Changelog](https://keepachangelog.com/).

---

## [0.7.6] — search correctness, locale lifecycle, Event Guide hardening

### Added
- **Audited official search-family coverage** for finite Pokémon GO inventory filters in Android Expert Builder, backed by canonical registry tests and Android/Web parity checks.
- **Event Guide source-page enrichment** with strict detail-quality checks for CURRENT/UPCOMING gameplay events.
- **Approved-source network policy** for build-time Event Guide enrichment: HTTPS-only allowlisted hosts, redirect-target validation, final-response URL validation, and rejection of credentials/non-standard ports.

### Fixed
- **Search Assistant substring collisions** such as `traded`/trade and `storage`/age, plus hundo/nundo plural handling and smart-apostrophe negation normalization.
- **Search Assistant pipe safety** so pipe-delimited input is rejected instead of silently becoming a different AND query; PokeQuery-generated/copied output remains pipe-free.
- **Caught-date `May` disambiguation** so normal modal-English “may” does not accidentally become a month filter while real May date requests still work.
- **Localized intent and alias regressions**, including Turkish negation handling and canonical lucky/purified output.
- **Tag intent regression** where affirmative tag/tagged requests could become `!#`.
- **System Default App Language** now follows the live device locale while the process remains running, including explicit-language → System Default reset without recreation loops.
- **Event Guide date/status/category parity**, Web runtime feed validation, DST end-of-day handling, title normalization, and protection against source-page H1 renaming an existing catalog event.
- **Repeated widget/app-shortcut routing** when the running task is reused.

### Validation
- Android CI passed unit tests, lint, golden-corpus identity, Event Guide fallback freshness, generator/enrichment/fallback tests, event-feed validation and runtime-asset validation on the post-release hardening branch.
- PWA fast gate passed golden corpus, fallback freshness, typecheck, lint, unit and production build; Playwright is now a separate manual workflow and is not a routine PR blocker.
- Samsung Galaxy S25 / Android 16 / One UI 8.5 physical validation passed System Default locale lifecycle, Expert Builder localization, Search Assistant regressions, raw pipe safety, Event Guide checks, repeated widget routes and clean logcat on the final Android runtime-affecting code.

---

## [0.7.5] — pre-AAB safety, Search Assistant date intent, feedback stability

### Added
- **Caught-date Search Assistant intents** for English and Turkish natural language.
  Queries such as `caught in April 2025`, `caught in 2025`,
  `nisan 2025'te yakalanan` and month-only requests now compile to official
  `yearYYYY` + rolling `ageN-M` syntax.
- **Caught-date composition** with existing Search Assistant intents, so date
  filters can be combined with shiny, hundo, legendary, shadow and exclusions
  without dropping either side of the request.
- Deterministic month/year tests, including leap-year, future-date, DST-safe Web
  date math, all 12 English/Turkish month names, and localized numeric
  `year`/`age` token translation.
- Real Chromium offline PWA coverage for precached Event Guide and Knowledge data.

### Fixed
- **Tester feedback crash** when Android resolved the `mailto:` intent to the app
  itself. Feedback launch is now failure-safe; physical-device validation opens
  the system chooser without a fatal exception.
- **Search Assistant polarity** for mixed positive/negative intent, contractions,
  contrast clauses and list inheritance (`don't hide`, `without ... but with ...`,
  `show all but ...`).
- **Caught-date combined-intent regression** that initially caused date matching to
  short-circuit generic filters such as shiny/hundo/exclusions.
- **2016/2017/2018 collision** where an explicit caught year could incorrectly add
  the legacy `age365-` intent.
- **App Language / Search String Language independence** in Android Search
  Assistant, including localized output under a different UI language.
- **Exact-token safety checks** for localized protection deduplication and count
  protections; unsupported `untraded` is blocked.
- **No-pipe invariant hardening** across generators, linter paths and regression
  tests. PokeQuery-generated output never intentionally emits `|`.
- **Event Guide freshness**: Android runtime fallback is aligned with the canonical
  feed and widget/event paths no longer rely on stale July event entries.
- **PWA offline/update behavior**, unknown-route fallback and dynamic i18n fallback.
- **Privacy/intent hardening** for browser failure handling, widget PendingIntent
  identity and the non-exported widget copy path.
- Historical runtime Pokémon character sprite bytes replaced with an original
  neutral PokeQuery event glyph.

### Validation
- Android unit, lint, generator/fallback, asset and event-feed CI passed on the
  audited pre-release source.
- Web typecheck, lint, unit, build and multi-browser Playwright CI passed.
- Samsung Galaxy S25 / Android 16 physical validation covered Search Assistant
  polarity, caught-date composition, localized copied output, Event Guide, Privacy
  and tester-feedback launch.
- Launcher widget discovery on Samsung One UI remains a known deferred optional
  issue; it does not block the core Closed Testing release.

---

## [0.5.2] — polish, localization foundation, AI foundation, app-language hotfix

### Added
- **Launcher icon** refresh with a refined original design.
- **Onboarding** polish and clearer first-run flow.
- **Home PokeQuery wordmark/logo** treatment (original brand artwork).
- **Knowledge Base** entry point directly from Home.
- **Popular Presets** with expanded previews.
- **Visual Density** setting (Comfortable / Compact) for Refine sections.
- **Search token registry** with verification metadata.
- **Turkish guardrails** — `Auto (Safe)` never resolves to Turkish; Turkish output
  stays explicit beta only, with verification matrix.
- **"AI Coming Later"** skeleton in Settings (foundation only — no active AI).
- Two independent language layers documented in
  `docs/localization/localization_architecture.md`.

### Fixed
- **App Language black screen (v0.5.2.1 hotfix)** — selecting English or Turkish
  could render a permanent black screen on Samsung SM-S931B / Android 16. Root cause
  was the OS per-app locale call firing from a Compose `SideEffect` on every
  recomposition, producing an Activity-recreation loop. Removed the
  `LocaleManager`/reflection path; App Language now applies a recreation-free
  in-process `Locale.setDefault` via `LaunchedEffect`. Regression tests added.

---

## [0.5.1] — UI polish and copy flow fixes

### Fixed
- **Risk Warning** now opens correctly from Safe Cleanup Copy (and all Medium/High
  goals) before the string reaches the clipboard.
- **Refine section text overlap** resolved across all goal detail screens.
- **Copy button no longer covers** the generated search string — clear hierarchy of
  risk badge → string box → copy button.
- **Trade Fodder notes** card layout fixed (no clipping/overlap).
- **PvP Candidates** now offers league-specific strings (Great / Ultra) via a
  segmented control.
- **Expert Builder** options wrap (no horizontal-scroll dependency) and are grouped
  into status / tags / IV / age / distance sections.
- **Expert Builder copy policy** — Copy is disabled only for true linter errors;
  advisory/risky warnings keep Copy enabled with a visible warning.
- **Home wordmark** replaced with a real brand logo treatment (original artwork).
- Typography polish for major headings.

---

## [0.5.0] — Stitch UI overhaul

### Changed
- Full premium dark "Stitch" UI direction implemented across all screens.
- Compose layout rebuilt around a consistent dark navy + cyan brand language.

### Added
- Onboarding, Home, detail screens, Knowledge Base, Favorites, History, Presets,
  Settings — all redesigned under the new direction.

---

## [0.4.3] — production readiness pack

### Added
- Manifest/privacy hardening and release-config lockdown.
- Signing configuration gated on local `keystore.properties`.
- Release readiness and upload checklists under `docs/release/`.

---

## [0.4.2] — safety patch

### Fixed
- Version consistency — displayed version derives from a single source of truth
  (`BuildConfig.VERSION_NAME`), with a regression test guarding against stale
  hardcoded values.
- Additional privacy and safety hardening across the domain engine.

---

## [0.4.1] — internal testing release

### Added
- First internal-testing build with the v0.4 UI layout.
- Fresh screenshot set and contact sheets.

---

## [0.4.0] — UI layout polish

### Changed
- UI layout refined to match the approved reference direction.

---

[0.7.6]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.7.6
[0.7.5]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.7.5
[0.5.2]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.5.2
[0.5.1]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.5.1
[0.5.0]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.5.0
[0.4.3]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.4.3
[0.4.2]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.4.2
[0.4.1]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.4.1
