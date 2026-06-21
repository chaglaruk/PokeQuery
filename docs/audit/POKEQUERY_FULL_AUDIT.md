# PokeQuery — Full Repository Audit

**Date:** 2026-06-21
**Mode:** ANALYSIS ONLY. No source files were modified. No commit, no push.
**Scope:** v0.4.1 (versionCode 8), package `com.caglar.pokequery`.

---

## 1. Repository Map

Single-module Android app, Kotlin + Jetpack Compose, offline-first Pokémon GO search-string generator.

### Architecture / layering

| Layer | Path | Responsibility |
|---|---|---|
| Domain engine | `domain/engine/StringBuilderEngine.kt` | Builds goal-based queries, appends protections, raises warnings, computes scope breadth. |
| Domain linter | `domain/lint/Linter.kt` | Static checks on raw expert queries (pipe, count, reserved tags, etc.). |
| Domain mapper | `domain/engine/SearchTermMapper.kt` | English→Turkish token translation. |
| Data models | `data/model/` | `GeneratedString`, `SavedTemplate`, `Term`, `RiskLevel`. |
| Data repos | `data/repository/` | `KnowledgeBaseRepository` (asset JSON parse), `UserPreferencesRepository` (DataStore + `SavedTemplateCodec`). |
| UI | `ui/screens/`, `ui/components/` | Compose screens and reusable premium components. |
| Theme | `theme/` | Forced dark Material3 scheme, color/type tokens. |
| Nav | `Navigation.kt`, `NavigationKeys.kt` | Navigation3 (back-stack of `NavKey` data objects). |
| Entry | `MainActivity.kt` | Reads `start_route` intent extra (screenshot/test deep-linking). |

### UI screens & navigation flow

- **Onboarding** (3-page pager) → sets `firstUseSeen`, goes Home. Hidden bottom bar.
- **Home** → grid of 10 goal cards → each routes to `GoalDetail(goalId)`, except `expert`→`ExpertBuilder` and `presets`→`Presets`.
- **GoalDetail** → builds query from engine + options panel → copy or `RiskWarning` flow.
- **ExpertBuilder** → raw text field + linter warnings → generates custom string.
- **Presets** → categorized static preset list → copy/risk.
- **Favorites / History** → DataStore-backed lists.
- **KnowledgeBase** → searchable/filterable term list from `knowledgebase.json`.
- **Settings** → language, copy behavior, density, data reset, about.
- **RiskWarning** → interstitial confirmation before Medium/High copy.
- **Bottom nav:** Builder / Favorites / History / Knowledge / Settings.

### Search-string engine behavior

`buildGoal(goalId, config, language)` returns a `GoalSpec` (query, explanation, risk, title, protections). `buildString(...)` then:
1. Replaces `|` → `,` (fail-closed pipe rule) with a warning.
2. Appends `!token` protections joined by `&`.
3. If `count` present, appends `COUNT_MANDATORY_PROTECTIONS` and bumps risk to Medium.
4. `trade_fodder` gets an extra trade-eligibility disclaimer.
5. Translates syntax via `SearchTermMapper` for Turkish.

### Settings / persistence

- DataStore (`user_prefs`) via `preferencesDataStore`.
- Keys: `first_use_seen`, `saved_templates_v1`, `recent_history_v1`, `warning_behavior`, `duplicate_threshold`, `safety_style`, `copy_behavior`, `game_language`, `visual_density`.
- `SavedTemplateCodec` uses a `length:value` framing (robust, round-trip tested).
- Legacy `favorites_set` migrated and removed.

### Favorites / history

- Favorites dedup by `rawSyntax`; persisted as encoded set; sorted by `createdAt` desc.
- History capped at 25, dedup by `rawSyntax`.
- `resetSettings()` clears prefs but preserves favorites/history/firstUse.

### Knowledge base

- `app/src/main/assets/knowledgebase.json` — 60+ entries (operators, numeric, IV, status, tags, size, encounter, evolution, move, max).
- Parsed defensively (`runCatching`, `optString` for optional fields).
- `lastVerified` uniformly `2026-06-20`.

### Play / release docs

`docs/release/` holds 18 docs: AAB build report, app config audit, Data Safety draft, internal testing checklists, IP audit, keystore setup, manifest audit, Play upload checklist, privacy notes, release readiness, screenshot plan, signing guide, store listing, third-party notices, v0.4.1 audit.

### Scripts / assets / screenshot pipeline

- `scripts/`: `check_runtime_assets.py` (allowlist guard), `create_contact_sheet.py`, `process_ui_assets.py`, `update_kb.py`.
- `capture_screenshots.ps1` / `.sh`, `create_ui_handoff.py`, `make_comparison.py`, `fix_icons_transparent.py`.
- Screenshots in `docs/screenshots/` (16 numbered + contact sheet).

### Tests

- Unit (`src/test`): `StringBuilderEngineTest` (engine + linter, 18 cases), `SavedTemplateCodecTest` (2 cases), `NavigationTest` (4 cases).
- Instrumented (`src/androidTest`): `KnowledgeBaseRepositoryTest` (2 cases — load + malformed JSON).
- **No Compose UI tests, no screenshot tests, no localization tests, no manifest/permission tests, no Linter unit tests in isolation beyond engine tests.**

---

## 2. Build & Test Health

| Check | Command | Result |
|---|---|---|
| Clean build | `./gradlew clean` | ✅ Success |
| Unit tests | `./gradlew :app:test --rerun-tasks` | ✅ **BUILD SUCCESSFUL**, 24 tasks executed |
| Debug assemble | `assembleDebug` | ✅ Success (~1m17s) |
| Asset guard | `python scripts/check_runtime_assets.py` | ✅ **PASS** (20 runtime assets, all allowlisted) |

### Warnings (compiler)
- `LocalClipboardManager` deprecated → use `LocalClipboard` (Navigation.kt:30, GoalDetailScreen.kt:48, MiscScreens.kt:45).
- `statusBarColor` deprecated (Theme.kt:42).
- SDK XML version 4 encountered (tooling/CLI version skew) — harmless.

### Notes
- No flaky/slow tests observed (suite is small and fast).
- Release build has `isMinifyEnabled = false` → larger APK/AAB, no obfuscation/optimization (see Play/privacy sections).
- AAB reported ~34 MB — unusually large; root cause is 20 uncompressed PNGs (~22 MB total) in `drawable-nodpi` (see privacy/security).

---

## 3. Search-String Correctness Audit

### What is correct
- **Pipe fail-closed:** `buildString` replaces `|`→`,` and warns. ✅ Compliant with AGENTS.md.
- **`count` mandatory protections:** auto-appended (`shiny,lucky,legendary,mythical,shadow,purified,favorite,traded,costume`) and risk bumped to Medium. ✅
- **Trade disclaimer** on `trade_fodder`. ✅
- **Hundo/Nundo/PvP** return bare exact strings with `RiskLevel.Info` and no cleanup protections — correct (these are inspection strings).
- **Linter** independently catches pipe, unsafe count, 0★-band caveat, risky category inclusion, reserved-tag collisions, shortcuts, non-ASCII.
- **`SavedTemplateCodec`** is framing-safe and round-trip tested with delimiter chars.

### Bugs / unsafe strings (see BUG_REPORT.md for full detail)

**BUG-001 (High) — `pvp_candidates` protections are silently dropped AND scope/query malformed.**
`StringBuilderEngine.buildGoal("pvp_candidates")` returns the bare string, but `GoalDetailScreen` (lines 86–98) skips re-wrapping for `pvp_candidates`, `hundo_check`, `nundo_finder`. For PvP this means the user can never add protections even though the options panel is hidden for it — acceptable. **However**, the `lucky_trade` branch is *not* in that skip-list, so `lucky_trade` flows through the `baseGoal.rawSyntax.split("&").firstOrNull { !it.startsWith("!") }` path. That split takes only the **first non-! token** and discards `!traded`. Net effect: `age365-&!traded` + default exclusions becomes `age365-&<defaults>` **without `!traded`** unless the user toggles "Exclude Traded" — and even then the option only re-adds `traded`, not the other lucky-trade nuance. The GoalDetail "must be untraded" default state is `excludeTraded=true`, so the default is safe, but the engine's own `trade_fodder`/`lucky_trade` `!traded` guarantee is bypassed in the screen layer.

**BUG-002 (High) — `SearchTermMapper` Turkish regex can corrupt tokens.**
Regex `(?<=^|[&!,])(key)(?=[0-9\-,]|$)` with `key=hp` matches inside `0hp`, but `hp` is also a key. `favorite`/`favourite` mismatch: only `favorite` is mapped; `favourite` is not (KB lists both). `traded`→`takaslanan` in code but `takaslanmış` in the localization plan and KB (`status_tr` field shows inconsistencies — e.g., `status_traded` has `description_tr: "Takas edilmiş"` while mapper emits `takaslanan`). Turkish is **unverified against the live game client** per `turkish_localization_plan.md` ("Do not enable Turkish runtime mode in this phase") — yet it is **enabled by default** in Settings (`Auto` resolves to Turkish on tr-TR devices).

**BUG-003 (High) — Turkish `count` token mismatch.**
Mapper: `count→toplam`. Localization plan matrix: `count2-→sayı2-`. KB `counter_count` description_tr: "Tür kopya sayısı". Three different candidate words for the same token (`toplam` vs `sayı` vs `sayısı`). Unverified → risky.

**BUG-004 (Medium) — `lucky_trade` does not exclude shiny/legendary by default.**
`GoalSpec.protections = emptyList()` with an explicit comment "We do not exclude shiny/legendary by default." This is a deliberate choice but **dangerous**: lucky-trade prep surfaces valuable shinies/legendaries, and a user who treats the result as "trade fodder" could lose them. The explanation warns, but no risk-interstitial nuance differentiates "this list contains valuables" from cleanup.

**BUG-005 (Medium) — Expert builder ignores linter errors.**
`ExpertBuilderScreen` computes `Linter.lint(rawQuery)` for display but `onGenerate` always passes the raw query to `buildGoal("expert")`. Linter `isError=true` warnings (pipe, unsafe count, risky category) do **not** block copy. The engine still fail-closes `|` and adds count protections, so it isn't catastrophic, but the UI implies a guard that isn't enforced.

**BUG-006 (Low) — `calculateScopeBreadth` is heuristic and can mislabel.**
E.g. `0*,1*&!shiny...` (a Preset) is "Broad" by intent but the heuristic only special-cases a few literals. Cosmetic, but the "Result Breadth" hint could mislead.

**BUG-007 (Low) — Presets double-protect / over-claim.**
Presets like "Low IV Cleanup Candidate" hardcode the full protection list, then `PresetsScreen` calls `buildString(..., protections = emptyList())` — so preset syntax is used verbatim. Good. But "Untraded Duplicates" = `count2-&!traded` is built with `protections = emptyList()`, so it ships **without** the count-mandatory protections (no `!shiny`, `!legendary`, etc.) that the engine would otherwise enforce. This directly contradicts the engine's fail-closed count rule. A user copying "Untraded Duplicates" gets an unsafe count string.

### Localization risks (summary)
- Turkish output is enabled but unverified against the live client.
- `favourite` (UK) not mapped.
- `hp` translation contested (`can` per user requirement; KB says variable).
- Non-ASCII warning in Linter is correct but only advisory.

### Stale KB entries / assumptions
- All `lastVerified = 2026-06-20` (bulk-set, not individually re-verified).
- `counter_count` risk High; `count[N]` semantics ("Pokédex species number") correct and well-flagged.
- `move_special @special` description lumps Legacy/Elite TM/Frustration/Return — acceptable but broad.

---

## 4. UX / UI Audit

### Premium-dark quality assessment
Visually the app targets a premium dark utility aesthetic (navy `#040A18`, teal `#00E5FF`, glowing borders, gradient cards, Canvas-drawn mascot/shield art). Execution is consistent and polished for a solo project. Below are concrete issues, not a redesign.

### High-priority UX bugs
- **U1 — Settings "About" shows `v0.3.4`** (MiscScreens.kt:246) while the app is `0.4.1`. Stale version string shown to users/testers.
- **U2 — Home hero "Menu" and "Search" icon buttons are no-ops** (HomeScreen.kt:121–131, `onClick = {}`). Dead affordances.
- **U3 — `RiskWarningScreen` body text uses `Color.Gray` on `BackgroundDark`** — low contrast, fails WCAG AA for body text (RiskWarningScreen.kt:37,46). Same for "Go Back".
- **U4 — `Settings` "Reset all settings" and clear actions are plain `Text` with `clickable`** (MiscScreens.kt:236–238), no confirmation dialog. Destructive, one-tap, no undo. Same for clear favorites/clear history.
- **U5 — Expert builder "Copy Custom String" copies even when linter shows red errors** (see BUG-005). No disabled state.
- **U6 — Onboarding "Skip" is one-tap to Home with no confirmation**, and there is no way back to onboarding except toggling the (un-labeled-as-such) "First-use guide seen" switch in Settings.
- **U7 — `goalAccent` for `pvp_candidates` returns `BlueCTA`, but Home card uses `Color(0xFF4FC3F7)`** — minor inconsistency between Home tint and detail accent.
- **U8 — `WarningPanel` always uses the `candy_prep` header image** (GoalDetailScreen.kt:227) regardless of which goal produced the warning — misleading imagery.

### Polish / wording
- "What do you want to find?" hero text is good; goal subtitles are concise.
- "PvP IVs" card subtitle "Great & Ultra" is terse but clear.
- "Special Trade Review" preset = `shiny,legendary,mythical` labeled RiskLevel.Info — but selecting shinies/legendaries for trade is high-stakes; Info understates it.
- Knowledge Base "Copy token" copies raw syntax (e.g. `cp[N]`) including brackets — a user pasting `cp[N]` literally into GO gets nothing. Should copy an example or note the placeholder.

### Missing affordances
- No "select-all / share" on copied strings.
- No haptic/stronger feedback than a Toast on copy.
- No empty-state illustration differentiation (Favorites/History both reuse safe_cleanup art).
- No back-handler integration with predictive back beyond NavDisplay defaults.

### Accessibility
- Many `contentDescription = null` on decorative art — acceptable.
- Touch targets: SwitchRow/RadioRow rows are tall enough (≥44dp). Bottom nav items fine.
- RiskWarning confirm button is default-height (good).
- Color-only state communication (risk badges) — should pair with text (it does: badge shows risk name). OK.
- No `testTag`/semantics for screen-reader navigation of the goal grid.

### Layout robustness
- Home grid is `chunked(2)` with `weight(1f)` — survives small phones; long subtitles `maxLines=2`.
- Detail screen is a single `verticalScroll` Column — fine.
- Presets uses nested `LazyColumn` inside a `Column` with fixed top bar — OK because the outer is not scrollable, but the `ScreenTitleBar` is outside the LazyColumn so it scrolls away. Minor.
- Rotation not specifically handled (Compose handles it, but onboarding pager/dimens are dp-fixed). Acceptable for a phone utility.

---

## 5. Google Play Readiness

### Config consistency
- `applicationId = namespace = "com.caglar.pokequery"` ✅ consistent.
- `versionCode=8`, `versionName="0.4.1"` ✅ matches release docs.
- `minSdk=24`, `targetSdk=36`, `compileSdk=36` ✅ current.
- Signing: release config loaded from gitignored `keystore.properties` only if present ✅ safe pattern.

### Doc contradictions (must reconcile before production)
- **`app_config_audit.md` is stale:** says versionName `0.1.3`, versionCode `2`, and contains a self-contradictory "BLOCKER: placeholder package name … must be changed to `com.caglar.pokequery`" while already stating the package *is* `com.caglar.pokequery`. This doc predates the rename commit `c55c689`.
- **Settings "About" hardcoded `v0.3.4`** (see U1) — user-visible mismatch with 0.4.1.

### Permissions / privacy claims
- Manifest requests **zero permissions**, no INTERNET ✅. Matches `MANIFEST_PERMISSION_AUDIT.md` and `PRIVACY_NOTES.md`.
- Data Safety draft ("zero data collected", "no network traffic") is **accurate** given the code (no analytics/ads/network SDKs). ✅
- Store listing "100% offline. Zero permissions. No ads." ✅ accurate.
- Store listing "Supports both English and Turkish string output natively." ⚠️ **Overstatement** — Turkish is unverified (see BUG-002/003) and the localization plan says not to enable it this phase. Risk: a Turkish-language Play reviewer or user files a defect.

### IP / trademark risk
- Runtime assets are abstract (Canvas mascot, shield, map lines) per `IP_ASSET_AUDIT.md` ✅.
- ⚠️ **However**, `docs/design/` and `docs/design_targets/` contain tracked reference/mockup PNGs and `reference_target_pokequery*.png`. If any depict official Pokémon/Niantic art, they are in the repo (not the APK) — still a reputational/legal hygiene issue. **Needs verification** of those images' provenance. They do **not** ship in the APK.
- App name "PokeQuery: Safe Search Strings" and copy reference "Pokémon GO" descriptively. Descriptive/nominative use is generally lower risk but **needs a clear disclaimer** of non-affiliation. `PRIVACY_NOTES.md` says "does not connect to Niantic or the Pokémon GO app" — good, but the **store listing and in-app About lack an explicit "not affiliated/endorsed by Nintendo/Niantic/The Pokémon Company" disclaimer.** Recommend adding.

### Content rating / target audience
- No violence, no user content, no online features → "Everyone" is defensible. Questionnaire still needs completing (PLAY_UPLOAD_CHECKLIST checkboxes are unchecked).

### Closed-testing readiness
- AAB builds and signs. Internal testing checklist exists. The 16 numbered screenshots exist.
- ⚠️ Screenshots are auto-generated via `start_route` deep-links — good for determinism, but they should be re-captured at 0.4.1 to avoid stale store imagery (commit `3a03f03` "update fresh screenshots" suggests recent, verify currency).

---

## 6. Privacy & Security

### Permissions / network / analytics
- Zero permissions, zero network code, zero analytics/ads/crash SDKs. ✅ Excellent.
- No exported components except launcher `MainActivity`. ✅
- `allowBackup=true` with empty backup/extraction rules ⚠️ — favorites/history (DataStore) become eligible for auto-cloud-backup and device-to-device transfer. For a "privacy-first" app this is a minor mismatch; consider `allowBackup=false` or explicit `<exclude>` of `user_prefs`. **Not a Play blocker.**

### Secrets
- `keystore.properties` and `release-keystore.jks` are **gitignored and NOT tracked** ✅ (verified via `git ls-files`).
- Plaintext store/key passwords exist **locally** in `keystore.properties` — acceptable for a local dev setup, but the file must never be committed and the password should be rotated if the machine is shared. **No secret values are reproduced in this report.**
- `docs/release/KEYSTORE_LOCAL_SETUP.md` is tracked but contains only placeholders (`YOUR_KEYSTORE_PASSWORD`) ✅.
- No API keys/tokens anywhere in code (no network layer exists).

### Generated/accidental files
- `jarsigner_verify.txt` (226 KB) is **untracked** (only in working tree) ✅ but should be added to `.gitignore` to prevent accidental commit.
- `local.properties` is gitignored ✅.
- `window_dump.xml`, `reference_full_mockup.png`, two `ChatGPT Image *.png` are **tracked at repo root** ⚠️ — dev artifacts that bloat the repo and may contain UI dump data. Not in APK. Housekeeping item.

### Dependencies
- All AndroidX/Compose/Kotlin official; versions are very recent (AGP 9.0.1, Kotlin 2.3.20, Compose BOM 2026.03.01). No third-party analytics/ad networks. ✅
- `material-icons-extended` pulled as full dependency (not in version catalog) — increases method/size footprint; acceptable.

### Debug flags / release behavior
- `isMinifyEnabled = false` on release ⚠️: no shrinking/obfuscation. For an offline text app the security impact is low, but it inflates the package and ships all debug-friendly symbol names. Recommend enabling R8 + the existing `proguard-android-optimize.txt` before production.
- No `debuggable` override; release inherits false ✅.

### Clipboard
- Uses `LocalClipboardManager.setText` (deprecated API) to write the generated string. Reads nothing from clipboard. ✅ Privacy-safe.

### Local storage
- DataStore only. No SQLCipher, no files outside app sandbox. ✅

---

## 7. Top Issues (summary — full lists in companion reports)

### Top 10 issues
1. **Turkish output enabled but unverified; mapper word disagrees with plan/KB** (BUG-002/003) — High.
2. **Preset "Untraded Duplicates" ships unsafe `count2-&!traded` without mandatory count protections** (BUG-007) — High.
3. **`lucky_trade`/`trade_fodder` engine `!traded` guarantee bypassed by GoalDetail screen-layer split** (BUG-001) — High.
4. **Expert builder copies despite linter errors** (BUG-005/U5) — Medium.
5. **Settings About shows v0.3.4 (stale); `app_config_audit.md` stale and self-contradictory** (U1, Play) — Medium.
6. **No confirmation dialogs for destructive Settings actions (reset/clear)** (U4) — Medium.
7. **RiskWarning screen low-contrast text** (U3) — Medium (a11y).
8. **`allowBackup=true` + empty rules vs "privacy-first" positioning** (Privacy) — Low/Medium.
9. **No non-affiliation disclaimer in store listing / in-app** (Play IP) — Medium.
10. **~22 MB of uncompressed PNGs; release minify off → 34 MB AAB** (Privacy/size) — Low/Medium.

### Top 10 recommended improvements
1. Gate Turkish behind an explicit "beta" toggle + on-screen unverified notice (or disable Auto→Turkish) until the spot-check matrix is cleared.
2. Route Presets through the same `buildString` count-protection path as goals (or pre-bake full protections into preset syntax).
3. Make GoalDetail wrap `lucky_trade`/`trade_fodder` through the engine unchanged instead of re-splitting on `&`.
4. Block Expert "Copy" when any `Linter.lint(...).isError` is true.
5. Fix version strings (Settings About → 0.4.1; delete or rewrite `app_config_audit.md`).
6. Add confirmation dialogs for clear/reset; convert to proper `Button`/`TextButton`.
7. Raise RiskWarning text contrast (use `TextSecondary` minimum).
8. Convert PNGs to WebP and enable R8 minify to cut AAB size dramatically.
9. Add a "Not affiliated with Nintendo/Niantic/The Pokémon Company" disclaimer to store listing + About.
10. Add `.gitignore` entries for `jarsigner_verify.txt` and stop tracking root dev PNGs/`window_dump.xml`.

### Play Console blockers
- **None hard blockers.** App builds, signs, requests no permissions, and matches its Data Safety form.
- **Soft blockers / strong recommendations before production access request:** reconcile stale config docs; add non-affiliation disclaimer; resolve Turkish "native support" claim; (recommended) enable minify + WebP.

### Privacy/security blockers
- **None.** No tracked secrets, no network, no analytics, no exported surfaces, signing secrets correctly gitignored.
- Recommendations only: `allowBackup` policy, R8, rotate password if shared machine.

---

## 8. Exact Next Recommended Action

**Fix the three High search-safety bugs before the next closed-testing build**, in this order:

1. **BUG-007** — In `PresetsScreen`, either (a) pass preset syntax through `StringBuilderEngine.buildString` so count-mandatory protections apply, or (b) bake the full mandatory-protection suffix into the "Untraded Duplicates" / "Older Untraded" / "Distance Trade Candidates" preset strings. Add a regression test.
2. **BUG-001** — In `GoalDetailScreen`, stop re-deriving the base query by `split("&").firstOrNull`; for `lucky_trade`/`trade_fodder` use `baseGoal.rawSyntax` directly so `!traded` and engine protections survive.
3. **BUG-002/003** — Either disable Turkish (set default gameLanguage to English and remove Auto→Turkish resolution) until the spot-check matrix in `turkish_localization_plan.md` is cleared, **or** add a visible "Turkish terms are community-sourced and unverified" notice on the Settings language row and in copy output.

Each is a small, localized change with an existing test scaffold (`StringBuilderEngineTest`), so they can be validated immediately without touching signing, package, or UI redesign.

---

*Companion reports in this directory: `BUG_REPORT.md`, `FEATURE_GAP_ROADMAP.md`, `PLAY_COMPLIANCE_AUDIT.md`, `PRIVACY_SECURITY_AUDIT.md`, `TEST_GAP_ANALYSIS.md`.*
