# PokeQuery — Google Play Production Access Evidence

**Package:** `com.caglar.pokequery` · **Target version:** 0.4.3 (versionCode 10)
**Purpose:** Evidence pack to support the Google Play production access request after closed testing.

---

## 1. What PokeQuery does

PokeQuery is an **offline, copy-only Android utility** that helps Pokémon GO players generate safe, review-ready **search strings** (plain text) for storage management goals: Safe Cleanup, 2x Candy Prep, Trade Fodder, Lucky Trade, PvP candidates, Hundo/Nundo inspection, Untagged cleanup, an Expert Builder, and Popular Presets.

The app **creates text only**. The user manually copies a generated string and pastes it into Pokémon GO themselves. It performs no gameplay action.

## 2. Why it is safe

- **No permissions.** The manifest requests zero permissions (asserted by automated regression tests; see §10).
- **No network.** No INTERNET permission, no HTTP clients, no analytics/ads/crash SDKs.
- **No account access.** PokeQuery never logs into, connects to, or reads from Pokémon GO or any Niantic service.
- **Manual copy/paste only.** No automation of taps, transfers, trades, or any in-game action.
- **Fail-closed string safety.** The engine never emits the `|` operator, auto-applies mandatory exclusions for `count`-based strings, preserves `!traded` invariants, and blocks Expert Builder copy on linter errors.
- **Risk interstitials.** Medium/High-risk copies require explicit confirmation with goal-specific explanations.

## 3. Closed testing plan

- Distribution via Google Play **closed testing** track with a Google Group of invited testers.
- Minimum 14-day closed-testing window (see `tester_tracking_template.md`) before requesting production access, per Google's 2024+ personal-account requirements.
- Testers opt in via the closed-testing opt-in URL; no public listing until production.

## 4. Tester flow

1. Accept the Google Group invite / opt-in link.
2. Install PokeQuery from Play (closed track).
3. Complete onboarding, which now (v0.4.3) explains the manual paste flow and the risk-color legend.
4. Pick a goal, review risk/protections, copy the string.
5. Paste into Pokémon GO and review results.
6. Send feedback via **Settings → Send tester feedback** (a `mailto:` intent to `caglar@caglardinc.com` — no backend).

## 5. How feedback is collected

- **In-app mailto** (Package 2): Settings → "Send tester feedback" opens the tester's own email app with a pre-filled subject and body template (app version, Android version, device, selected language, structured prompts). Nothing is sent automatically; the user reviews and sends manually.
- **Direct messages** via the recruitment channel (see `closed_testing_message_templates.md`).
- No crash SDK, no in-app analytics, no automatic reporting.

## 6. What v0.4.2 fixed (prior safety patch)

- Preserved the engine-mandated `!traded` term on trade/lucky goals via a tested `GoalStringBuilder` helper.
- Blocked Expert Builder copy when the linter reports error-level warnings.
- Gated Turkish output: "Auto" no longer silently switches to Turkish from device locale; English is the safe default. Turkish remains an explicit, beta-labeled manual option.
- Added a parameterized preset safety regression test.
- Made the About version dynamic (no more stale `v0.3.4`).
- Added an in-app non-affiliation disclaimer.
- Improved RiskWarning text contrast and added confirmation dialogs for destructive Settings actions.

## 7. What v0.4.3 adds (this pack)

- **Package 2** — Tester feedback via mailto (no network).
- **Package 3** — Onboarding now teaches the full manual copy/paste flow and the risk-color legend.
- **Package 4** — Per-goal risk explanations (RiskWarning copy is now goal-aware, not generic).
- **Package 6** — R8 minify + WebP assets reduce AAB size (see build report).
- **Package 7** — Automated manifest/privacy/build-config regression tests.
- **Package 8** — Knowledge Base verification/safety/language badges (honest: no token is marked verified unless confirmed live).
- **Package 9** — Turkish verification matrix + spot-check protocol (Turkish remains beta).

## 8. Privacy summary

- **Data collected:** None.
- **Data transmitted:** None (no network code exists).
- **Local data:** Favorites and copy history only, in DataStore, never exported.
- **Backup:** `android:allowBackup="false"` — local data is excluded from cloud backup (Package 7).

## 9. Data Safety summary (Play Console form)

- "Does your app collect or share any of the required user data types?" → **No.**
- "Is your app's data collection required?" → **Not applicable (no data collected).**
- "Does your app encrypt data in transit?" → **Not applicable (no network traffic).**
- Advertising ID: **not present** (no ads SDK).
- Family/children: utility app, not targeted at children.

## 10. Non-affiliation disclaimer

> PokeQuery is an independent helper app and is not affiliated with, endorsed by, or sponsored by Niantic, The Pokémon Company, or Nintendo.

This appears in Settings/About and in the store listing. The app uses "Pokémon GO" descriptively only and contains no official Pokémon/Niantic/Nintendo artwork, logos, sprites, or balls at runtime.

## 11. Risks acknowledged and mitigated

| Risk | Mitigation |
|---|---|
| User over-trusts a search string and transfers valuables | Risk interstitials; goal-specific explanations; mandatory `count` exclusions; fail-closed engine rules |
| Turkish terms wrong on a Turkish client | Turkish is beta, never auto-selected; contested tokens documented; verification matrix + protocol in place |
| Privacy claim drift (permissions/network added) | Automated manifest/privacy regression tests (Package 7) |
| Tester can't figure out how to use a string | Onboarding paste flow + risk legend (Package 3) |
| Feedback friction | In-app mailto feedback (Package 2) |
| Size/quality concern | R8 + WebP (Package 6) |

## 12. What changed based on tester/audit feedback

- Audit (v0.4.2): `!traded` invariant, linter enforcement, Turkish gating, dynamic version, disclaimer, contrast, destructive-action confirmations.
- v0.4.3: onboarding clarity (paste flow), goal-specific risk copy, KB trust badges, production evidence, feedback channel, size reduction.
