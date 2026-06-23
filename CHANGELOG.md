# Changelog

Notable changes to PokeQuery. Dates are grouped by release; the current shipped
version is **0.5.2** (versionCode 13).

The format is loosely based on [Keep a Changelog](https://keepachangelog.com/).

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

[0.5.2]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.5.2
[0.5.1]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.5.1
[0.5.0]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.5.0
[0.4.3]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.4.3
[0.4.2]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.4.2
[0.4.1]: https://github.com/chaglaruk/PokeQuery/releases/tag/v0.4.1
