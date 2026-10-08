# PokeQuery Roadmap

**Owner:** PokeQuery · **Package:** `com.caglar.pokequery`
**Current product baseline:** Android v0.7.7 / code 27 (published); Web/PWA v0.7.3 (independently versioned). Updated 2026-10-08. See [current execution status](release/PRERELEASE_EXECUTION_2026-10-08.md).

This document distinguishes current product boundaries and already-shipped milestones from **future candidates**. Proposals are not commitments and have no implied release date. Current code, tests and release evidence outrank any roadmap statement.

## Product invariants (every candidate must respect these)

These never change, regardless of feature:

- **Offline-first, with the documented Event Guide exception.** Android declares INTERNET for the public PokeQuery event feed over HTTPS, not for Pokémon GO account access or unrelated networking. No analytics, tracking, ads, or login.
- **Text only.** The app generates search strings. It never connects to Pokémon GO, never reads
  a Pokémon collection, never performs OCR/scanning/automation.
- **No unrelated permissions.** Android's current manifest includes the documented Event Guide INTERNET permission; `allowBackup=false` remains enforced.
- **Two-layer localization independence.** App Language (Layer A, UI) and Search String Language (Layer B, generated strings) remain independent. `Auto` follows a supported device locale with English fallback; `Match App Language` follows the explicitly chosen App Language or supported device locale under System Default. Explicit search-language choices override both. Official localized Help Center evidence is BETA; independent live localized-client confirmation is required before VERIFIED.
- **Safety-first risk model.** Inspection-only goals may be Info; action-adjacent
  cleanup/trade workflows are Medium and route through Risk Warning with mandatory protections.
- **Original visual assets.** No official Pokémon/Niantic/Nintendo artwork, sprites, fonts, logos, characters or Poké Ball imagery. Follow the current runtime-asset/IP policy.
- **No fake verification.** Turkish tokens are never marked VERIFIED without a recorded live
  confirmation in `turkish_verification_matrix.md`.

## Already delivered since the original roadmap

- **Changelog / What's New** ships with the Android app as locally bundled content and is reachable from Settings.
- **Event Guide** uses the documented public-feed HTTPS exception, caching and an offline bundled fallback.
- Android **Share Search** preserves Medium/High Risk Warning gates and shipped in v0.7.7.
- Android **Favorites → personal Presets** already ships (introduced in v0.6.1); current Favorites/PersonalPreset/MyPresets code and tests preserve local storage and safety review.
- The **Search Assistant** already ships deterministic local interpretation. Generative/cloud AI is not a shipped feature.
- PR #41/#42 Event Guide/UI corrections are merged in source but await the next authorized Android release; they are not in Play 0.7.7.
- Search-language semantics and token confidence rules have changed since v0.5.5. Current code/tests and `docs/localization/` outrank this historical plan.

## Future candidates (not scheduled)

The following ideas originated in earlier audits and remain proposals, not shipped capabilities.
Each carries the privacy/safety constraints that would govern a future build.

### 1. Offline localized-token verification records

**Idea.** Let trusted testers record live Pokémon GO Turkish-client confirmations of token
candidates (e.g. the contesting `count` candidates `toplam`/`sayı`/`sayısı`, and the compound
tokens' spacing variants) directly into a structured form, feeding
`SearchTokenRegistry` + `turkish_verification_matrix.md`.

**Value.** The verification matrix remains a manually reviewed record; a structured capture path would let
verification progress from `untested` → `works` faster and more reliably, which is the gating
factor for graduating Turkish output out of beta.

**Privacy/safety constraints.**
- Must remain **offline / local-only**. No crowd-sourced upload, no server, no account. A
  verification record is stored locally on the tester's device.
- Must **never auto-promote** a token to VERIFIED. A human still reviews and flips the status in
  code + the matrix together (the existing rule).
- Must respect **honesty**: a candidate stays UNTESTED/RISKY/BETA until a real live-client confirmation is recorded with date, device and source notes. No AI guesses or automatic VERIFIED promotion.
- Search String Language independence and the current `Auto` / `Match App Language` resolution rules must be preserved.

### 2. Personalized scope breadth

**Idea.** Today scope breadth (Very Narrow / Narrow / Moderate / Broad / Very Broad) is a pure
function of the query. A personalized layer could let a tester tag their own context (e.g.
"collecting", "raiding", "trading season") to influence defaults or surfacing.

**Value.** Could reduce friction for a tester's most common workflows without changing the
generated strings' safety.

**Privacy/safety constraints.**
- Must remain **local-only preference**. No profile sync, no account, no inferred behavior.
- Must **never weaken the risk model or mandatory protections.** Personalization affects
  defaults/surfacing only; it cannot downgrade an action-adjacent goal from Medium to Info or
  remove `COUNT_MANDATORY_PROTECTIONS` / the `!traded` invariant.
- Inspection-only vs action-adjacent intent (Fix 5) must be preserved.

### 3. Favorites / personal Presets refinements

**Current truth.** The Android favorite-to-personal-preset operation already ships. The old proposal to build the bridge is complete, not an active missing feature.

**Future scope only.** Validate a specific editing or Web-parity gap before scheduling work. Value is reducing repeated filter entry; no demand or delivery date is implied.

**Acceptance.** Local-only round-trip preservation of customized query and risk metadata, normal engine/linter routing, mandatory count protections and Medium/High Risk Warning. No cloud/community preset feed or fake controls.

---

Items graduate out of this document into a release plan only after a dedicated design + safety
review. Until then these are **not active features**. Do not add fake functional controls or non-working settings to advertise them.
