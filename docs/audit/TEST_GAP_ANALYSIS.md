# PokeQuery — Test Gap Analysis

**Date:** 2026-06-21 · **Version:** 0.4.1 · **Current state:** Unit tests pass (24 tasks), 1 instrumented test class. No UI/screenshot/localization/manifest tests.

**Existing coverage:**
- `StringBuilderEngineTest` — engine + linter, 18 cases (pipe, count protections, goal specs, linter bypass rules).
- `SavedTemplateCodecTest` — encode/decode round-trip + malformed input.
- `NavigationTest` — bottom-tab routing, home-goal routing, screenshot routes, risk-warning predicate.
- `KnowledgeBaseRepositoryTest` (instrumented) — KB loads, malformed JSON fails safely.

---

## Gap 1 — Unit tests

### U-T1: `SearchTermMapper` translation table
**Assert:** Each key in `turkishMap` round-trips correctly for representative queries (`shiny`, `count2-&!traded`, `0attack&0defense&0hp`, `distance100-`, `age365-`). English pass-through. Auto resolution uses `Locale.getDefault()`.
**Why:** Turkish is the highest-risk unverified path (BUG-002/003). A golden-table test locks behavior and surfaces regressions.

### U-T2: `Linter` isolated coverage
**Assert:** Each `LintWarning` branch fires independently: pipe error, unsafe-count error + advisory, 0★ band caveat (and nundo bypass), risky-category inclusion in cleanup/count/trade (and PvP/trade-prep bypass), reserved-tag collision, shortcut expansions, non-ASCII advisory.
**Why:** Linter is currently only exercised incidentally via `StringBuilderEngineTest`. Isolated tests prevent silent relaxation of safety rules.

### U-T3: `StringBuilderEngine.calculateScopeBreadth`
**Assert:** Each branch ("Very Narrow", "Narrow", "Broad", "Moderate", "Very Broad") returns for canonical inputs.
**Why:** BUG-006; the meter is user-facing.

### U-T4: `SavedTemplateCodec` adversarial inputs
**Assert:** Negative length, truncated payload, non-numeric length field, payload longer than declared, and `offset != value.length` all return `null` (not throw). `RiskLevel` decode failure returns null.
**Why:** Codec parses persisted untrusted-shaped data; corruption must never crash the app.

### U-T5: `KnowledgeBaseRepository.parse` field handling
**Assert:** Missing optional fields (`knownQuirks`, `description_tr`) parse to null/empty without error; `riskLevel` invalid enum throws (caught by `runCatching`).
**Why:** Defensive parse of the shipped JSON.

### U-T6: Preset safety contract (parameterized over `POPULAR_PRESETS`)
**Assert:** Every preset whose syntax contains `count` or an IV-band token (`0*`,`1*`,`2*`) yields, after `StringBuilderEngine.buildString`, a string containing all `COUNT_MANDATORY_PROTECTIONS` / default protections.
**Why:** BUG-007 latent hazard; prevents a future preset from shipping an unsafe string.

---

## Gap 2 — Compose UI tests

### UI-T1: Onboarding → Home navigation
**Assert:** Tapping "Skip" or completing page 3 routes to Home and hides bottom bar during onboarding.
**Why:** First-run flow is the entry point for every new tester.

### UI-T2: GoalDetail copy & risk interstitial routing
**Assert:** For a Medium/High goal, "Copy" routes to `RiskWarning` instead of copying; for Info/Low it copies directly and adds history.
**Why:** Core safety gate (BUG-005 adjacent).

### UI-T3: Expert builder blocks copy on linter error
**Assert:** With `|` or unsafe `count` in the field, the copy button is disabled (post-fix) and no copy fires.
**Why:** BUG-005 regression guard.

### UI-T4: Favorites add/remove/clear and History cap
**Assert:** Saving a favorite dedups by `rawSyntax`; removing works; clear-all shows confirmation (post-fix); history caps at 25 and dedups.
**Why:** Data-loss paths (BUG-011).

### UI-T5: Settings destructive-action confirmation
**Assert:** Each of clear-favorites / clear-history / reset-settings shows an `AlertDialog` before acting.
**Why:** BUG-011.

### UI-T6: Settings language applies to generated string
**Assert:** Selecting Turkish changes the next generated string's tokens (and English pass-through).
**Why:** Localization wiring.

---

## Gap 3 — Screenshot tests

### S-T1: Deterministic screenshot per `start_route`
**Assert:** Snapshot for each of the 16 routes (`onboarding_step_1..3`, `home`, `detail_*`, `knowledge`, `presets`, `favorites`, `history`, `settings`) matches golden.
**Why:** The screenshot pipeline already supports `start_route`; a paparazzi/Roboroco capture test locks visual regressions and replaces the manual `capture_screenshots.*` flow.

### S-T2: RiskWarning contrast/render
**Assert:** Snapshot shows readable text (post contrast fix).
**Why:** BUG-010.

---

## Gap 4 — Search-string engine edge cases

### E-T1: Pipe fail-closed preserves semantics
**Assert:** `a|b,c` → `a,b,c` with a warning; never contains `|`.
**Why:** AGENTS.md hard rule.

### E-T2: Double-protection dedup
**Assert:** If `baseQuery` already contains `!shiny`, the engine does not append a second `!shiny`.
**Why:** Prevents malformed duplicate tokens.

### E-T3: Empty/blank query handling
**Assert:** `buildString("")` produces only the protection suffix (no leading `&`).
**Why:** Robustness.

### E-T4: Trade-fodder `!traded` invariant through the screen layer
**Assert:** End-to-end (engine + GoalDetail re-wrap) the final string contains `!traded`.
**Why:** BUG-001.

---

## Gap 5 — Localization tests

### L-T1: Golden Turkish output table
**Assert:** For each goal, Turkish output equals the vetted golden string (once verified).
**Why:** BUG-002/003.

### L-T2: Locale fallback
**Assert:** Non-tr, non-en locales fall back to English; `Auto` resolves correctly.
**Why:** Prevents silent wrong-language output.

### L-T3: KB `description_tr` presence/shape
**Assert:** Every KB entry has a non-empty `description_tr` (or documented absence).
**Why:** Data completeness for the tr UI.

---

## Gap 6 — Release config / privacy / manifest tests

### R-T1: Manifest permission assertion
**Assert:** Built manifest declares zero permissions; `MainActivity` is the only exported component.
**Why:** Guards the "zero permissions" Data Safety claim.

### R-T2: No network dependencies
**Assert:** Release dependency tree contains no `INTERNET`-using artifacts (e.g., via manifest-merge report).
**Why:** Privacy claim.

### R-T3: `allowBackup` / backup rules
**Assert:** `allowBackup=false` or DataStore excluded (post-fix).
**Why:** BUG-012.

### R-T4: Version consistency
**Assert:** `versionName` in gradle == in-app About string == release docs.
**Why:** BUG-008/015.

### R-T5: Signing config present only when properties exist
**Assert:** Release build is unsigned/debug-signed when `keystore.properties` absent; signed when present.
**Why:** Prevent CI mis-release.

---

## Gap 7 — Asset guard tests

### A-T1: `check_runtime_assets.py` runs in CI
**Assert:** Script exits 0 on current assets; fails on an added `*mockup*`/`*screenshot*` filename.
**Why:** Prevent IP/size regressions.

### A-T2: PNG→WebP size budget
**Assert:** Total `drawable-nodpi` size under N MB after conversion.
**Why:** BUG-017.

---

## Priority summary

| Tier | Tests | Why first |
|---|---|---|
| **P0 (before next closed-testing build)** | U-T1, U-T2, U-T6, E-T4 | Lock the highest-risk search-safety behavior (Turkish, linter, presets, trade `!traded`). |
| **P1 (before production request)** | UI-T2, UI-T3, UI-T5, R-T1, R-T4, A-T1 | Safety gates, privacy claims, version/asset integrity. |
| **P2 (post-production)** | S-T1/T2, L-T1/T2, the rest | Polish, golden screenshots, full localization. |
