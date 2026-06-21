# Turkish Search Term Verification Matrix

**Owner:** PokeQuery · **Package:** `com.caglar.pokequery`
**Status:** Turkish output is **BETA — not fully verified.** Do not mark any token `works` until it has been confirmed inside a live Pokémon GO client whose language is set to Turkish.

This matrix exists so we never over-claim that Turkish terms are verified. The app ships Turkish as an explicit, manually-selected, beta option only (never auto-selected from device locale).

## Candidate sources

- Community sources (e.g. the MIT-licensed `pogosearch` project, used only as conceptual reference per `docs/release/THIRD_PARTY_NOTICES.md`).
- The current app map in `app/src/main/java/com/caglar/pokequery/domain/engine/SearchTermMapper.kt`.
- The Knowledge Base `description_tr` fields.

> Multiple candidates exist for several tokens (e.g. `count` → `toplam` / `sayı` / `sayısı`; `traded` → `takaslanan` / `takas edilmiş`). Do **not** pick one and call it verified. Test it first.

## Matrix

Legend — `untested` = default; `works` = confirmed live in Turkish client; `fails` = confirmed broken; `ambiguous` = behaves differently than expected.

| English token | Current Turkish candidate | Alternative candidates | Query example | Expected behavior in Pokémon GO | Status | Date tested | Tester / device | Notes |
|---|---|---|---|---|---|---|---|---|
| `shiny` | `parlak` | — | `parlak` | Returns all shiny Pokémon | untested | — | — | Likely correct but must be confirmed live. |
| `traded` | `takaslanan` | `takas edilmiş` | `!takaslanan` | Excludes all traded Pokémon | untested | — | — | Contested. KB uses "Takas edilmiş"; code emits "takaslanan". |
| `count` | `toplam` | `sayı`, `sayısı` | `toplam2-` | Returns species with 2+ owned | untested | — | — | Most contested. Three candidates across code/KB/plan. |
| `favorite` | `favori` | — | `!favori` | Excludes favorites | untested | — | — | Likely correct; confirm live. |
| `favourite` | `favori` | — | `!favourite` | UK spelling variant | untested | — | — | English-only token; Turkish map reuses "favori". |
| `legendary` | `efsanevi` | — | `efsanevi` | Returns legendaries | untested | — | — | Likely correct; confirm live. |
| `mythical` | `mistik` | — | `mistik` | Returns mythicals | untested | — | — | "Mistik" is the candidate; verify. |
| `shadow` | `gölge` | — | `!gölge` | Excludes shadows | untested | — | — | Likely correct; confirm live. |
| `purified` | `arıtılmış` | `arındırılmış` | `purified` | Returns purified | untested | — | — | Contested ("arıtılmış" vs "arındırılmış"). |
| `costume` | `kostümlü` | — | `!kostümlü` | Excludes costumes | untested | — | — | Likely correct; confirm live. |
| `specialbackground` | `özel arka planlı` | — | `özel arka planlı` | Returns special backgrounds | untested | — | — | Compound token; verify exact form. |
| `locationbackground` | `konum arka planlı` | — | `konum arka planlı` | Returns location backgrounds | untested | — | — | Compound token; verify exact form. |
| `ultrabeast` | `ultra canavar` | — | `ultra canavar` | Returns Ultra Beasts | untested | — | — | Compound token; verify exact form. |
| `age` | `yaş` | — | `yaş365-` | Pokémon caught 365+ days ago | untested | — | — | Verify the localized prefix works. |
| `distance` | `mesafe` | — | `mesafe100-` | Pokémon caught 100km+ away | untested | — | — | Verify the localized prefix works. |
| `attack` | `saldırı` | — | `0saldırı` | Exact 0 Attack IV | untested | — | — | Verify suffix form. |
| `defense` | `savunma` | — | `0savunma` | Exact 0 Defense IV | untested | — | — | Verify suffix form. |
| `hp` | `can` | `sg` | `0can` | Exact 0 HP IV | untested | — | — | Contested; KB notes variable localization. |
| `0attack` | `0saldırı` | — | `0saldırı` | Exact 0 Attack IV | untested | — | — | Compound; test exactly as written. |
| `0defense` | `0savunma` | — | `0savunma` | Exact 0 Defense IV | untested | — | — | Compound; test exactly as written. |
| `0hp` | `0can` | `0sg` | `0can` | Exact 0 HP IV | untested | — | — | Contested; verify both candidates. |
| `@special` | `@special` | — | `@special` | Legacy/Elite/Frustration/Return moves | untested | — | — | Likely language-invariant (`@` prefix). Verify. |
| `megaevolve` | `megaevolve` | — | `megaevolve` | Mega-evolvable | untested | — | — | Verify whether it localizes. |
| `fusion` | (none mapped) | — | n/a | n/a | untested | — | — | Not present in current KB/map; add if supported. |
| `dynamax` | `dynamax` | — | `dynamax1-` | Dynamax Pokémon | untested | — | — | Verify whether it localizes. |
| `gigantamax` | `gigantamax` | — | `gigantamax1-` | Gigantamax Pokémon | untested | — | — | Verify whether it localizes. |

## Rules for updating this matrix

1. A token may only move from `untested` to `works` after live Pokémon GO Turkish-client confirmation.
2. Record the date, tester, and device for each confirmation.
3. If a token `fails`, record the failure and the corrected candidate (and update `SearchTermMapper` + this matrix together).
4. Never mark a token `works` based on a community source alone — it must be re-checked live.
5. When all tokens a goal uses are `works`, that goal's Turkish output may graduate from beta (separate decision, not automatic).
