# Search String Language Strategy

**Owner:** PokeQuery · **Package:** `com.caglar.pokequery`
**Applies from:** v0.5.2

This is the **Layer B** strategy — how the app decides the language of the Pokémon GO search
strings it generates. It is deliberately conservative. See `localization_architecture.md`
for the two-layer model.

## The one rule

> **The default search-string language is English. Turkish is emitted only when the user
> explicitly and knowingly chooses it.**

Everything else follows from this.

## Options (Settings → Search & Language → Search String Language)

| Option | What it does | When it produces Turkish |
|---|---|---|
| **Auto (Safe)** | Conservative default. Always resolves to English. | Never. |
| **English** | Explicit English output. | Never. |
| **Turkish (Beta)** | Emits Turkish candidates from `SearchTermMapper`. | Only when selected. |

`SearchTermMapper.resolveLanguage("Auto")` → `"English"`. This is tested and load-bearing.

## Why "Auto" is not "follow device locale"

A naive "Auto = device locale" would mean a Turkish-locale phone silently starts emitting
unverified Turkish tokens. Because most tokens are unverified (`turkish_verification_matrix.md`),
that is unsafe. So:

- **Auto (Safe)** = English, always. The word "Safe" is in the label on purpose.
- **Turkish** is a separate, explicit, beta-flagged choice.

Changing the **App Language** (Layer A) does **not** change this. A Turkish UI still emits
English strings unless the user separately picks Turkish for Search String Language.

## How a token becomes safe to emit in Turkish

1. A candidate is recorded in `SearchTokenRegistry` with a status
   (`UNTESTED` / `RISKY` / `BETA` / `VERIFIED`).
2. A human confirms the candidate in a live Pokémon GO client set to Turkish, and records it
   in `turkish_verification_matrix.md` (date, tester, device).
3. Only then is the token's status promoted to `VERIFIED` and considered `safeToEmit`.

As of v0.5.2 **no token is VERIFIED**. The tokens the mapper currently emits (legacy beta
behavior) are marked `BETA`/`RISKY`, never `VERIFIED`, and all carry the "verify before use"
warning.

> **v0.5.5 (Fix 4) — `count` is English-fallback even in Turkish output.** `count` is
> parser-sensitive numeric syntax (`countN-`) and its Turkish form is contested
> (`toplam` / `sayı` / `sayısı` across code/KB/plan). Until ONE candidate is confirmed live and
> promoted to `VERIFIED`, the mapper emits the **English `count`** even when the user explicitly
> chose Turkish. The candidates are recorded as hypotheses in `SearchTokenRegistry.COUNT_CANDIDATES`
> and `turkish_verification_matrix.md`; they are the single source of truth. This is the
> safety-first path for an unverified, parser-sensitive token.

## Risk surfacing

- The Risk Warning screen shows a Turkish-beta caution when the generated string looks
  Turkish (`SearchTermMapper.looksTurkish`).
- The Knowledge Base marks language-sensitive and beta/risky terms with badges sourced from
  the registry.
- Medium/High risk copies always route through Risk Warning regardless of language.

## What we will not do

- ❌ Auto-switch to Turkish based on locale or App Language.
- ❌ Treat machine translation as a source of truth for Pokémon GO tokens.
- ❌ Emit `VERIFIED` tokens without a recorded live confirmation.
- ❌ Allow the search-string language to be changed by anything other than an explicit user
  action in Settings.
