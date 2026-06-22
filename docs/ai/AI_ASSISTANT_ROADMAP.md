# AI Assistant Roadmap

**Owner:** PokeQuery · **Package:** `com.caglar.pokequery`
**Status as of v0.5.2:** Not started. The "AI Assistant — Coming later" UI entry point is
**disabled and non-functional** on purpose. This roadmap records what a *safe* future
assistant would and would not do.

This document is conditional and exploratory. Nothing here is committed or scheduled.

---

## Guiding principle

Any future AI Assistant must be an **optional convenience that helps the user understand or
draft safer search strings**. It must never make gameplay decisions on the user's behalf and
must never weaken the offline-first, no-key, no-network privacy contract. See
`AI_FEASIBILITY.md` for why only on-device generative AI is even a candidate, and why it is
gated by severe device-coverage limits.

---

## Safe future use cases (assistive, advisory only)

These are the only directions worth pursuing. All are *advisory* — the user always decides.

1. **Explain this query** — Given a generated search string, produce a plain-language
   explanation of what it matches and its caveats. On-device; can also be done
   deterministically from the existing engine without any generative model.
2. **Safer rewrite suggestion** — Suggest an equivalent query that adds protective exclusions
   (e.g. reminding to exclude `shiny`, `lucky`, `#`, `traded`). This is the engine's existing
   safety contract, surfaced as a suggestion.
3. **Why no results?** — Help the user reason about why a search returned nothing (e.g. an
   unverified localized token). Advisory only; never claims the game is broken.
4. **Natural language → draft query** — Turn a plain intent ("find duplicates I can trade")
   into a *draft* query that still passes through the existing `Linter` / `ExpertCopyPolicy`
   / `RiskMessageBuilder` safety pipeline. Output is always a suggestion, never auto-copied.

---

## Features to avoid (hard exclusions)

These are explicitly out of scope and must never be built:

- ❌ **Automatic transfer / trade decisions.** The app generates text only; the user decides.
- ❌ **"Safe to delete" / "safe to trade" absolute claims.** Real eligibility depends on
  friendship level, value, and in-game state the app cannot see. AI must never assert safety.
- ❌ **Pokémon GO account access** of any kind.
- ❌ **Gameplay automation** (auto-tapping, scripted actions).
- ❌ **Screen scanning / OCR** of the game.
- ❌ Any feature requiring user API keys or steady-state network access.

---

## Gating conditions (all must be true before any feature ships)

1. **On-device only.** Inference via AICore / Gemini Nano or a bundled model — never a cloud
   call. The app keeps zero steady-state network dependencies.
2. **Capability detection.** Runtime check that the device actually has the model; the app
   degrades silently and fully to today's behavior where it does not.
3. **No permission regressions.** No new permissions in the manifest that break the
   zero-permission guarantee for the non-AI path.
4. **Same safety pipeline.** Any AI-produced string still flows through `StringBuilderEngine`
   → `Linter` → `ExpertCopyPolicy` → `RiskMessageBuilder`. AI never bypasses safety.
5. **Advisory framing.** All output is phrased as a suggestion with the existing manual-review
   reminders intact.

---

## What ships in v0.5.2

A single, clearly-disabled entry point (Settings → "Coming Later" → "AI Assistant") that:

- States **"Coming later"** and **"Not available yet"**.
- States **"The offline-first app remains unchanged."**
- Is non-interactive (no model is loaded, no network is made, no key is requested).

This sets expectations honestly without faking capability.
