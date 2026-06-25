# Turkish Search Term Verification Matrix

**Owner:** PokeQuery · **Package:** `com.caglar.pokequery`
**Status:** Turkish output is **BETA — not fully verified.** Do not mark any token `works` until it has been confirmed inside a live Pokémon GO client whose language is set to Turkish.

This matrix exists so we never over-claim that Turkish terms are verified. The app ships Turkish as an explicit, manually-selected, beta option only (never auto-selected from device locale).

## Candidate sources

- Community sources (e.g. the MIT-licensed `pogosearch` project, used only as conceptual reference per `docs/release/THIRD_PARTY_NOTICES.md`).
- The current app map in `app/src/main/java/com/caglar/pokequery/domain/engine/SearchTermMapper.kt`.
- The Knowledge Base `description_tr` fields.

> Multiple candidates exist for several tokens (e.g. `count` → `toplam` / `sayı` / `sayısı`; `traded` → `takaslanan` / `takas edilmiş`). Do **not** pick one and call it verified. Test it first.
>
> **v0.5.5 (Fix 4) — `count` is English-fallback.** The `count` token is parser-sensitive
> numeric syntax (`countN-`) AND its Turkish form is contested across code/KB/plan, so as of
> v0.5.5 PokeQuery emits the **English `count`** even in Turkish output. The three candidates
> below (`toplam` / `sayı` / `sayısı`) remain in this matrix as hypotheses to test live. When
> one is confirmed, promote it to `works` here AND add it to `SearchTermMapper` +
> `SearchTokenRegistry.countMeta` together. The single source of truth for the candidates is
> `SearchTokenRegistry.COUNT_CANDIDATES`.
>
> **v0.5.5 safety hotfix — compound protection tokens are English-fallback (EMITTED), not just
> an alternative.** Multi-word candidates (`background`, `specialbackground`,
> `locationbackground`, `ultrabeast`) are parser-sensitive PROTECTION/exclusion tokens: a wrong
> form silently breaks an exclusion that must keep a valuable Pokémon out of a cleanup/transfer/
> trade list. Because their exact Turkish spacing/form is unverified on a live Turkish Pokémon GO
> client, PokeQuery now **emits the English token** in the generated string even when Search
> String Language is Turkish — mirroring the `count` policy. The Turkish candidate phrases below
> remain in this matrix (and in `SearchTokenRegistry.compoundCandidates`) as **hypotheses to test
> live**, NOT proof of support. UI labels and KB `description_tr` wording may still show the
> candidate phrase; only the generated query is locked to English until verified. When one is
> confirmed live, promote it to `works` here AND add it back to `SearchTermMapper.turkishMap` +
> `SearchTokenRegistry` together. The single source of truth for the candidates is
> `SearchTokenRegistry.compoundCandidates`.

## Matrix

Legend — `untested` = default; `works` = confirmed live in Turkish client; `fails` = confirmed broken; `ambiguous` = behaves differently than expected.

| English token | Current Turkish candidate | Alternative candidates | Query example | Expected behavior in Pokémon GO | Status | Date tested | Tester / device | Notes |
|---|---|---|---|---|---|---|---|---|
| `shiny` | `parlak` | — | `parlak` | Returns all shiny Pokémon | untested | — | — | Likely correct but must be confirmed live. |
| `traded` | `takaslanan` | `takas edilmiş` | `!takaslanan` | Excludes all traded Pokémon | untested | — | — | Contested. KB uses "Takas edilmiş"; code emits "takaslanan". |
| `count` | **(English fallback: `count`)** | `toplam`, `sayı`, `sayısı` | `count2-` (emitted) / `toplam2-`,`sayı2-`,`sayısı2-` (to test) | Returns species with 2+ owned | untested | — | — | v0.5.5: English `count` emitted even in TR output. Contesting candidates `toplam`/`sayı`/`sayısı` are NOT emitted — test all three live, then promote ONE. Source of truth: `SearchTokenRegistry.COUNT_CANDIDATES`. |
| `favorite` | `favori` | — | `!favori` | Excludes favorites | untested | — | — | Likely correct; confirm live. |
| `favourite` | `favori` | — | `!favourite` | UK spelling variant | untested | — | — | English-only token; Turkish map reuses "favori". |
| `legendary` | `efsanevi` | — | `efsanevi` | Returns legendaries | untested | — | — | Likely correct; confirm live. |
| `mythical` | `mistik` | — | `mistik` | Returns mythicals | untested | — | — | "Mistik" is the candidate; verify. |
| `shadow` | `gölge` | — | `!gölge` | Excludes shadows | untested | — | — | Likely correct; confirm live. |
| `purified` | `arıtılmış` | `arındırılmış` | `purified` | Returns purified | untested | — | — | Contested ("arıtılmış" vs "arındırılmış"). |
| `costume` | `kostümlü` | — | `!kostümlü` | Excludes costumes | untested | — | — | Likely correct; confirm live. |
| `background` | **(English fallback: `background`)** | `arka planlı` (phrase), `arkaplanlı` (no-space) | `!background` (emitted) / `!arka planlı`,`!arkaplanlı` (to test) | Excludes backgrounded Pokémon | untested | — | — | v0.5.5 hotfix: English `background` EMITTED even in TR output (protection token). Candidates `arka planlı`/`arkaplanlı` are NOT emitted — test live, then promote ONE. Source of truth: `SearchTokenRegistry.compoundCandidates`. |
| `specialbackground` | **(English fallback: `specialbackground`)** | `özel arka planlı` (phrase), `özelarkaplanlı` (no-space) | `!specialbackground` (emitted) / `!özel arka planlı`,`!özelarkaplanlı` (to test) | Returns special backgrounds | untested | — | — | v0.5.5 hotfix: English `specialbackground` EMITTED even in TR output (protection token). Compound candidates NOT emitted — test live, then promote ONE. |
| `locationbackground` | **(English fallback: `locationbackground`)** | `konum arka planlı` (phrase), `konumarkaplanlı` (no-space) | `!locationbackground` (emitted) / `!konum arka planlı`,`!konumarkaplanlı` (to test) | Returns location backgrounds | untested | — | — | v0.5.5 hotfix: English `locationbackground` EMITTED even in TR output (protection token). Compound candidates NOT emitted — test live, then promote ONE. |
| `ultrabeast` | **(English fallback: `ultrabeast`)** | `ultra canavar` (phrase), `ultracanavar` (no-space) | `!ultrabeast` (emitted) / `!ultra canavar`,`!ultracanavar` (to test) | Returns Ultra Beasts | untested | — | — | v0.5.5 hotfix: English `ultrabeast` EMITTED even in TR output (protection token). Compound candidates NOT emitted — test live, then promote ONE. |
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
