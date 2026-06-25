# Turkish Localization Plan

## Current Status
Turkish Pokémon GO search terms are available from community and third-party sources (e.g., the pogosearch repository and community forums).

> **v0.5.5 (Fix 4): single source of truth.** Token truth now lives in code first —
> `SearchTokenRegistry` (`domain/locale/SearchTokenRegistry.kt`) — and this doc plus the KB and
> the verification matrix (`docs/localization/turkish_verification_matrix.md`) must stay aligned
> with it. If the candidates below disagree with the registry, **the registry is authoritative**.
> In particular `count` is **English-fallback** (emitted as `count` even in Turkish output) until
> one of its contesting candidates (`toplam` / `sayı` / `sayısı` — see
> `SearchTokenRegistry.COUNT_CANDIDATES`) is confirmed live.

## Beta Phase Constraint
**Do not enable Turkish runtime mode in this phase.**

All sourced Turkish terms must be treated as `beta/community-sourced` until they are manually spot-checked inside the live Pokémon GO Turkish client. Unverified translations can cause catastrophic string failures if Niantic changes a localized term.

## Spot-Check Matrix for Future Release

To clear the beta constraint, a tester with a Turkish Pokémon GO client must execute the following searches and confirm they yield correct results:

| Category | Expected English | Community Turkish candidate(s) | Status |
| :--- | :--- | :--- | :--- |
| Shiny | `shiny` | `parlak` | Pending |
| Legendary | `legendary` | `efsanevi` | Pending |
| Traded | `traded` | `takaslanan` (emitted) / `takaslanmış`, `takas edilmiş` (alternatives) | Pending |
| Count limit | `count2-` | **English fallback emitted.** Test `toplam2-`, `sayı2-`, `sayısı2-` | Pending |
| IV - Attack | `0attack` | `0saldırı` | Pending |
| IV - Defense | `0defense` | `0savunma` | Pending |
| IV - HP | `0hp` | `0sg` / `0hp` | Pending |
| Age | `age365-` | `yaş365-` | Pending |
| Distance | `distance100-` | `mesafe100-` | Pending |
| Types | `water` | `su` | Pending |

## Implementation Path
Once the matrix is cleared, we will implement a `LanguageProvider` layer in the `StringBuilderEngine` to dynamically map our base English logic tokens to the verified Turkish strings before rendering the final output.

