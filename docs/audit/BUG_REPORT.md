# PokeQuery — Bug Report

**Date:** 2026-06-21 · **Version audited:** 0.4.1 (versionCode 8) · **Mode:** Analysis only (no fixes applied).

Severity scale: **Blocker / High / Medium / Low**. "Blocker" = must fix before any further closed-testing distribution. None found at Blocker level.

---

## BUG-001 — GoalDetail screen layer drops engine's `!traded` guarantee for trade goals
**Severity:** High
**Affected files:** `app/src/main/java/com/caglar/pokequery/ui/screens/GoalDetailScreen.kt` (lines 86–98), `domain/engine/StringBuilderEngine.kt` (`trade_fodder`, `lucky_trade`).
**Evidence:** For `hundo_check`, `nundo_finder`, `pvp_candidates` the screen returns `baseGoal` unchanged. For all other goals it does:
```
baseGoal.rawSyntax.split("&").firstOrNull { !it.startsWith("!") }
```
For `trade_fodder` the engine produces `count2-&!traded` (+ defaults). The split takes only `count2-` and discards `!traded`; `!traded` only returns if the user's `excludeTraded` toggle is on (default true for lucky_trade, but trade_fodder's option row is hidden, so it relies on the default state of `excludeTraded=true`).
**User impact:** A user who toggles off "Exclude Traded" (or a future refactor changing defaults) gets a trade-fodder string that includes already-traded Pokémon — which cannot be traded again, undermining the goal. The engine's own contract is silently bypassed.
**Suggested fix:** Use `baseGoal.rawSyntax` directly for trade/lucky goals instead of re-splitting; let the options panel only *add* optional protections.
**Test needed:** `GoalDetailViewModel`/engine-level test asserting `trade_fodder` final string always contains `!traded` regardless of toggles.

---

## BUG-002 — Turkish mapping enabled by default but unverified; mapper word disagrees with plan & KB
**Severity:** High
**Affected files:** `domain/engine/SearchTermMapper.kt`; `data/repository/UserPreferencesRepository.kt` (default `gameLanguage = "English"`, but `Auto` resolves to Turkish on tr-TR); `docs/research/turkish_localization_plan.md`; `docs/search_language_mapping.md`.
**Evidence:**
- `turkish_localization_plan.md`: *"Do not enable Turkish runtime mode in this phase."* All spot-check rows are "Pending".
- `SearchTermMapper.turkishMap`: `traded → "takaslanan"`.
- `knowledgebase.json status_traded.description_tr`: `"Takas edilmiş"`.
- Plan matrix: `count2- → sayı2-`; mapper: `count → toplam`; KB: `"Tür kopya sayısı"`.
- `Linter.kt:69` flags non-ASCII as "unverified" (advisory only).
**User impact:** On a Turkish-locale device with `Auto`, the app emits strings using contested Turkish tokens that have never been validated against the live Pokémon GO client. A cleanup string with a wrong token silently fails to protect (e.g., `!parlak` vs actual localized shiny term) → potential transfer of valuables.
**Suggested fix:** Until the spot-check matrix is cleared, default `gameLanguage` to `English`, disable `Auto→Turkish`, and show an explicit "Turkish terms are community-sourced and unverified" notice. Reconcile `count`/`traded`/`hp` to one verified word each.
**Test needed:** Localization test asserting Turkish output matches a vetted golden table; unit test that English is the default.

---

## BUG-003 — Turkish `count` token mismatch (`toplam` vs `sayı` vs `sayısı`)
**Severity:** High (subset of BUG-002, called out because count strings are the highest-risk output)
**Affected files:** `SearchTermMapper.kt` (`"count" to "toplam"`); `knowledgebase.json counter_count`; `turkish_localization_plan.md`.
**Evidence:** Three different candidate words across code/KB/plan for the same operator. The engine promotes `count` strings to Medium risk and warns about Pokédex-number semantics; an *additionally wrong* localized token compounds the risk.
**User impact:** `count2-` → `toplam2-` may return zero matches in a Turkish client if the real term is `sayı`, giving a false "no duplicates" result, or match the wrong set.
**Suggested fix:** Verify in a Turkish client; lock to one token; add to the golden-table test.
**Test needed:** See BUG-002.

---

## BUG-004 — `lucky_trade` does not exclude shiny/legendary by default
**Severity:** Medium
**Affected files:** `StringBuilderEngine.kt` (`lucky_trade` GoalSpec `protections = emptyList()` with comment "We do not exclude shiny/legendary by default").
**Evidence:** Lucky-trade prep surfaces valuable shinies/legendaries with only an explanation warning and a Medium risk interstitial.
**User impact:** A user who treats the lucky-trade list as disposable could trade away shinies/legendaries (special trades are limited and irreversible).
**Suggested fix:** Either keep as-is but upgrade the RiskWarning copy for `lucky_trade` to explicitly say "This list may contain shinies and legendaries — review every entry," or add an opt-in "Exclude shinies/legendaries" toggle defaulting on.
**Test needed:** Assert `lucky_trade` warnings include the valuable-Pokémon caveat.

---

## BUG-005 — Expert Builder copies despite linter errors
**Severity:** Medium
**Affected files:** `ui/screens/ExpertBuilderScreen.kt` (lines 39–63).
**Evidence:** `Linter.lint(rawQuery)` is computed for display only; the "Copy Custom String" button's `onClick = { onGenerate(rawQuery) }` always fires. `Linter` returns `LintWarning(isError = true)` for pipe, unsafe count, and risky-category inclusion.
**User impact:** The UI implies a safety guard (red error text) that is not enforced. The engine still fail-closes `|` and adds count protections, so it is not catastrophic, but the perceived safety is false.
**Suggested fix:** Disable the copy button (or route through `RiskWarning`) when `linterWarnings.any { it.isError }`.
**Test needed:** UI/assertion test that expert copy is blocked on error-level linter warnings.

---

## BUG-006 — `calculateScopeBreadth` heuristic can mislabel breadth
**Severity:** Low
**Affected files:** `StringBuilderEngine.kt` (`calculateScopeBreadth`).
**Evidence:** Only a handful of literals are special-cased; multi-token cleanup strings (e.g. presets) fall through to "Moderate" or "Very Broad" inconsistently.
**User impact:** The "Result Breadth" hint (where shown) may over/under-state scope, mildly misleading the user.
**Suggested fix:** Either remove the scope meter until it's rule-accurate, or formalize the rules (e.g., breadth from protection count + presence of count/age/distance).
**Test needed:** Unit tests covering each breadth branch.

---

## BUG-007 — Preset "Untraded Duplicates" (and siblings) bypass count-mandatory protections
**Severity:** High
**Affected files:** `ui/screens/PresetsScreen.kt` (lines 38–49 presets, 108–116 build call with `protections = emptyList()`).
**Evidence:**
```
Preset("Untraded Duplicates", ..., "count2-&!traded", RiskLevel.Medium, ...)
...
StringBuilderEngine.buildString(baseQuery = preset.syntax, protections = emptyList(), ...)
```
`buildString` *does* still apply `COUNT_MANDATORY_PROTECTIONS` when `count` is present (so `!shiny`, `!legendary`, etc. are added). **However**, because the preset string already contains `!traded`, and the engine's `protectionsToAdd = protections.filter { !baseQuery.contains("!$it") }` runs against an empty list, the *optional* default protections are intentionally suppressed for presets. Re-verifying: the mandatory count path still fires, so this specific preset is actually **safe** at the engine level. The residual issue is **inconsistency**: presets opt out of the default-protection layer entirely, so any future preset using a non-count risky token (e.g., a bare `0*` preset) would ship without `!shiny`/`!4*`. This is a latent foot-gun, not a current data-loss bug.
**User impact:** Low today (count presets are protected by the mandatory path); Medium as a maintenance hazard.
**Suggested fix:** Document the preset contract explicitly; add a test that every preset containing a count or IV-band token includes the mandatory protections after `buildString`.
**Test needed:** Parameterized test over `POPULAR_PRESETS` asserting mandatory protections present for risky tokens.

> Correction note: an earlier draft flagged this as a live data-loss bug; re-tracing the engine shows the mandatory count path still applies. Downgraded to High-severity *latent* hazard with a hardening test.

---

## BUG-008 — Settings "About" shows stale version `v0.3.4`
**Severity:** Low (user-facing correctness) / Medium (tester confusion)
**Affected files:** `ui/screens/MiscScreens.kt` (line 246, hardcoded `"PokeQuery v0.3.4"`).
**Evidence:** App is `0.4.1`; About panel displays `0.3.4`.
**Suggested fix:** Read version from `BuildConfig`/`PackageManager` instead of hardcoding. (Note: `buildConfig = false` currently; enabling it or querying `PackageInfo` is needed.)
**Test needed:** UI test asserting About version matches the manifest versionName.

---

## BUG-009 — Home hero "Menu" and "Search" buttons are no-ops
**Severity:** Low
**Affected files:** `ui/screens/HomeScreen.kt` (lines 121–131, `onClick = {}`).
**Evidence:** Two `IconButton`s with empty click handlers.
**User impact:** Tappable controls that do nothing — erodes trust and fails accessibility expectations (announced as buttons).
**Suggested fix:** Wire to a real action (e.g., Menu → Settings/Knowledge, Search → Knowledge search) or remove.
**Test needed:** UI test asserting the buttons either navigate or are absent.

---

## BUG-010 — RiskWarning screen body text fails contrast
**Severity:** Medium (accessibility)
**Affected files:** `ui/screens/RiskWarningScreen.kt` (lines 37, 46, `Color.Gray` on `BackgroundDark #040A18`).
**Evidence:** `Color.Gray` (~#888) on near-black is below WCAG AA 4.5:1 for body text.
**User impact:** Low-vision users struggle to read the very warning meant to protect them.
**Suggested fix:** Use `TextSecondary` (#8F9FB5) minimum; ideally `TextPrimary` for the confirmation paragraph.
**Test needed:** Screenshot/contrast assertion (can be manual in the screenshot pipeline).

---

## BUG-011 — Destructive Settings actions have no confirmation
**Severity:** Medium
**Affected files:** `ui/screens/MiscScreens.kt` (lines 236–238, plain `Text` + `clickable` for clear favorites / clear history / reset all settings).
**Evidence:** One-tap, no dialog, no undo.
**User impact:** Accidental data loss of favorites/history.
**Suggested fix:** Wrap each in an `AlertDialog` confirm.
**Test needed:** UI test asserting a dialog appears before the destructive action.

---

## BUG-012 — `allowBackup=true` with empty rules vs "privacy-first" positioning
**Severity:** Low/Medium
**Affected files:** `AndroidManifest.xml` (`android:allowBackup="true"`); `res/xml/backup_rules.xml`, `data_extraction_rules.xml` (both effectively empty/TODO).
**Evidence:** DataStore (`user_prefs`) holding favorites/history is eligible for auto-cloud-backup and device transfer.
**User impact:** Minor; contradicts the "private/offline" messaging if a user inspects backup behavior. Not a Play blocker.
**Suggested fix:** Set `allowBackup="false"`, or add `<exclude domain="sharedpref" path="user_prefs.xml"/>`-style rules; remove the TODO stubs.
**Test needed:** Manifest assertion test (TEST_GAP).

---

## BUG-013 — `WarningPanel` always shows the candy_prep header image
**Severity:** Low (polish)
**Affected files:** `ui/screens/GoalDetailScreen.kt` (line 227, `goalHeaderRes("candy_prep")`).
**Evidence:** The count-warning panel uses a fixed candy_prep thumbnail regardless of the originating goal.
**Suggested fix:** Use the current `goalId` or a neutral warning icon.
**Test needed:** Screenshot test.

---

## BUG-014 — Knowledge Base "Copy token" copies placeholder syntax verbatim
**Severity:** Low
**Affected files:** `ui/screens/MiscScreens.kt` (`KnowledgeTermRow`, copies `term.syntax` e.g. `cp[N]`, `@[type]`).
**Evidence:** Pasting `cp[N]` into Pokémon GO yields nothing.
**Suggested fix:** Copy an example (e.g. `cp1500`) or append a hint, or disable copy for template-only entries.
**Test needed:** Behavioral test.

---

## BUG-015 — Stale / self-contradictory release doc `app_config_audit.md`
**Severity:** Low (doc hygiene) / Medium (could mislead a reviewer)
**Affected files:** `docs/release/app_config_audit.md` (versionName 0.1.3, versionCode 2; "BLOCKER: placeholder package name" while package is already `com.caglar.pokequery`).
**Evidence:** Predates rename commit `c55c689`.
**Suggested fix:** Delete or rewrite to match 0.4.1 / `com.caglar.pokequery`.
**Test needed:** None (doc).

---

## BUG-016 — `favourite` (UK spelling) not mapped in Turkish mapper
**Severity:** Low
**Affected files:** `SearchTermMapper.kt` (only `favorite` mapped); `Linter.reservedTerms` includes both.
**Evidence:** KB has both `status_favorite` and `status_favourite`; mapper only translates `favorite`.
**Suggested fix:** Add `"favourite" to "favori"` (same Turkish word).
**Test needed:** Mapper unit test.

---

## BUG-017 — Release minify disabled; uncompressed PNGs inflate AAB (~34 MB)
**Severity:** Low/Medium (size, store hygiene)
**Affected files:** `app/build.gradle.kts` (`isMinifyEnabled = false`); `app/src/main/res/drawable-nodpi/*.png` (~22 MB across 20 files).
**Evidence:** `du` on drawable-nodpi PNGs; AAB size reported in `docs/release/AAB_BUILD_REPORT.md`.
**Suggested fix:** Enable R8 with `proguard-android-optimize.txt`; convert large PNGs to WebP (lossless/quality 80).
**Test needed:** Build-size assertion in CI.
