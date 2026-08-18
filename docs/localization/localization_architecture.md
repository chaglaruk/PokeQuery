# PokeQuery Localization Architecture

**Current architecture reference:** read current `LocalizationModel.kt`, `AppLocaleController.kt`, mapper/registry code and tests before changing behavior. Historical v0.5 assumptions are not authoritative.

PokeQuery has two language controls with different responsibilities:

1. **App Language** — controls PokeQuery UI resources.
2. **Search String Language** — controls generated Pokémon GO search syntax.

They are stored separately. `Match App Language` is an explicit user-selected bridge between them; otherwise changing App Language does not silently change an explicit Search String Language choice.

## Layer A — App Language

Current selectable values:

- System Default
- English
- Deutsch
- Español
- Français
- Italiano
- Türkçe

Current UI resources exist for:

- English (`values/`)
- Turkish (`values-tr/`)
- German (`values-de/`)
- Spanish (`values-es/`)
- French (`values-fr/`)
- Italian (`values-it/`)

### Resolution

`AppLocaleController` maps explicit choices to language tags. `System Default` reads the live device/system locale and resolves supported languages to EN/TR/DE/ES/FR/IT; unsupported device locales fall back to English.

System Default must remain **live** while the process is running. Do not cache a process-start device locale and treat it as permanent.

### Application model

PokeQuery deliberately does not call `LocaleManager#setApplicationLocales` from Compose. An earlier implementation could trigger a recreation loop/black screen on Samsung Android 16 devices.

The current design is recreation-free:

- `MainActivity` supplies a localized configuration context;
- `AppLocaleController` resolves the current UI locale;
- `Locale.setDefault` is used only as an in-process default for helpers that do not use Android resources directly;
- System Default re-resolves the current system locale instead of restoring a stale startup snapshot.

## Layer B — Search String Language

Current selectable values:

- Auto
- Match App Language
- English
- German
- Spanish
- French
- Italian
- Turkish

### Explicit language choices

English/German/Spanish/French/Italian/Turkish resolve directly to the chosen output language and do not depend on App Language or device locale.

### Auto

Current `Auto` behavior follows the **supported device locale**:

- en -> English
- de -> German
- es -> Spanish
- fr -> French
- it -> Italian
- tr -> Turkish
- unsupported locale -> English fallback

Auto does not follow an explicit App Language override; it follows device locale.

### Match App Language

`Match App Language` is the explicit opt-in bridge:

- when App Language is an explicit supported language, search output follows it;
- when App Language is System Default, search output follows the supported device locale;
- unsupported system locales fall back to English.

This does not collapse the two preferences into one control: the user must explicitly select Match App Language.

## Search-token confidence

Localized search syntax is a correctness/safety feature, not ordinary UI translation.

Current confidence model:

- `UNTESTED` — no sufficiently reliable localized form;
- `RISKY` — ambiguity/parser concern/conflicting evidence;
- `BETA` — current official localized Help Center evidence exists, but the term has not been independently confirmed in a live localized Pokémon GO client;
- `VERIFIED` — independently confirmed against a live localized client.

Official localized Help Center wording does **not** become VERIFIED automatically.

Canonical sources:

- `OfficialSearchSyntax.kt` — audited official syntax families/capabilities;
- `SearchTokenRegistry.kt` — token confidence metadata;
- `SearchTermMapper.kt` — active output mapping;
- `docs/localization/official_search_token_matrix.md` — documented localized mappings/evidence.

Never machine-translate Pokémon GO syntax and call it verified.

## Intentional English fallbacks

Parser-sensitive or unsupported localized terms may deliberately remain canonical English even when another Search String Language is selected.

Current documented examples include:

- `count` — numeric `countN-` syntax remains conservative English fallback until live localized confirmation;
- `specialbackground` — no documented localized mapping in the current matrix, so English fallback remains.

Always check the current registry/mapper/matrix before adding or removing a fallback.

## Generated-output safety

PokeQuery-generated strings **never emit `|`**. This is intentionally stricter than external Pokémon GO documentation.

Do not change this merely because an official Help Center page documents pipe syntax. Any product-policy change would require deliberate cross-platform engine/linter/copy-policy/tests/docs updates.

## Android/Web parity

Android and Web/PWA maintain mirrored search behavior. Token/language changes must preserve:

- Android/Web engine parity;
- byte-identical golden corpus copies;
- equivalent BETA/VERIFIED semantics;
- equivalent no-pipe safety;
- equivalent visible language choices unless a platform-only reason is explicitly documented.

Do not assume Web locale files are mechanically generated from Android resources unless current tooling explicitly proves that for the file being changed.

## UI localization rules

Any visible copy change must be applied naturally across EN/TR/DE/ES/FR/IT unless intentionally locale-specific.

- Translate meaning, not individual English words.
- Preserve risk severity and action meaning.
- Avoid AI-like/marketing filler.
- Check compact layouts, wrapping and overflow in longer locales.

## Historical appendix — v0.5 locale incident

Older v0.5 documentation described only English/Turkish foundations, claimed no `values-tr/` resources existed, and treated Auto as English-only. Those statements are historical and must not be used as current behavior.

A v0.5-era attempt to apply OS per-app locales from composition caused repeated Activity recreation/black-screen behavior on Samsung SM-S931B / Android 16. The project therefore retained a recreation-free in-process/configuration-context approach. Preserve that safety property while fixing current System Default behavior.
