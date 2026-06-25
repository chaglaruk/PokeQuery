# PokeQuery Roadmap

**Owner:** PokeQuery · **Package:** `com.caglar.pokequery`
**Applies from:** v0.5.5

This document tracks direction. Items here are **not built yet** and are not a commitment —
they are candidates evaluated against PokeQuery's hard privacy/safety constraints. Nothing here
implies a release date.

## Product invariants (every candidate must respect these)

These never change, regardless of feature:

- **Offline-first.** No INTERNET permission, no network calls, no analytics/tracking/ads/login.
- **Text only.** The app generates search strings. It never connects to Pokémon GO, never reads
  a Pokémon collection, never performs OCR/scanning/automation.
- **Zero-permission manifest**, `allowBackup=false`.
- **Two-layer localization independence.** App Language (Layer A, UI) never forces Search
  String Language (Layer B, generated strings). Turkish output stays an explicit, beta, opt-in
  choice; Auto (Safe) always resolves to English.
- **Safety-first risk model.** Inspection-only goals may be Info; action-adjacent
  cleanup/trade workflows are Medium and route through Risk Warning with mandatory protections.
- **No copyrighted assets.** No Pokémon/Niantic/Nintendo fonts, logos, colors, creatures, or
  Poké Ball art. Original artwork only.
- **No fake verification.** Turkish tokens are never marked VERIFIED without a recorded live
  confirmation in `turkish_verification_matrix.md`.

## Future / Post closed-testing candidates

The following ideas came out of the v0.5.5 audit. They are documented here, not implemented.
Each carries the privacy/safety constraints that would govern a future build.

### 1. Community Turkish token verification mode

**Idea.** Let trusted testers record live Pokémon GO Turkish-client confirmations of token
candidates (e.g. the contesting `count` candidates `toplam`/`sayı`/`sayısı`, and the compound
tokens' spacing variants) directly into a structured form, feeding
`SearchTokenRegistry` + `turkish_verification_matrix.md`.

**Value.** Today the matrix is a manual markdown table; a structured capture path would let
verification progress from `untested` → `works` faster and more reliably, which is the gating
factor for graduating Turkish output out of beta.

**Privacy/safety constraints.**
- Must remain **offline / local-only**. No crowd-sourced upload, no server, no account. A
  verification record is stored locally on the tester's device.
- Must **never auto-promote** a token to VERIFIED. A human still reviews and flips the status in
  code + the matrix together (the existing rule).
- Must respect **honesty**: a candidate stays `untested`/`risky` until a real live confirmation
  is recorded, with date/tester/device. No "AI guesses".
- Search String Language independence and Auto→English must be preserved.

### 2. Changelog screen

**Idea.** An in-app "What's new" / changelog screen surfaced after an update (and reachable
from Settings), summarizing the per-release changes.

**Value.** Closed-testing testers currently rely on external notes; an in-app changelog makes
each release's safety/feature deltas visible at the moment they matter.

**Privacy/safety constraints.**
- **No network.** The changelog content must ship with the app (a local resource / structured
  model), not be fetched online. No version-check server call.
- **No tracking.** No analytics on views, no "did the user read it" telemetry.
- **Honest wording.** Must never claim automation, Pokémon GO connectivity, or verified
  localization that does not exist. Turkish content follows the same Foundation/coming-later
  honesty rule as App Language (Fix 2).

### 3. Personalized scope breadth

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

### 4. Favorites ↔ Presets bridge

**Idea.** Let a tester promote a customized Favorite (a saved generated string) into a personal
"preset" they can re-run, and/or seed Presets from Favorites — blurring the current Favorites
(read-only saved strings) and Popular Presets (hand-authored) separation.

**Value.** Reduces re-entry for a tester's tuned queries.

**Privacy/safety constraints.**
- **Local-only.** No sharing, no cloud, no community preset packs networked in (that stays a
  separate, also-unbuilt Coming Later item).
- **Safety path unchanged.** A Favorite-turned-preset must still go through the exact same
  `StringBuilderEngine.buildString` path, risk routing, and `PopularPresetsSafetyTest`-style
  mandatory-protection enforcement. It cannot bypass Risk Warning for Medium/High goals.
- A user-built preset bearing a count/IV-band token must still receive the mandatory exclusions.

---

Items graduate out of this document into a release plan only after a dedicated design + safety
review. Until then they are explicitly **not active** and must not appear as functional UI
beyond the existing disabled "Coming Later" cards.
