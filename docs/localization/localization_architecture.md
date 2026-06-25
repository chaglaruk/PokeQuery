# Localization Architecture

**Owner:** PokeQuery · **Package:** `com.caglar.pokequery`
**Applies from:** v0.5.2

PokeQuery has **two independent language layers**. They never affect each other. This is the
single most important rule of the architecture and the reason Turkish output stays opt-in.

```
┌─────────────────────────────────────────────────────────────────────┐
│                       LAYER A — App Language                         │
│   Controls: interface text ONLY (buttons, labels, screen titles).   │
│   Options:  System Default · English · Turkish                      │
│   Store:    UserPreferences.appLanguage                             │
│   Apply:    AppLocaleController (in-process Locale.setDefault,       │
│              recreation-free — see "v0.5.2.1 hotfix" below)          │
│   Effect on generated search strings: NONE                          │
└─────────────────────────────────────────────────────────────────────┘
                                ↕ independent
┌─────────────────────────────────────────────────────────────────────┐
│                  LAYER B — Search String Language                   │
│   Controls: the text you COPY and paste into Pokémon GO.            │
│   Options:  Auto (Safe) · English · Turkish (Beta)                  │
│   Store:    UserPreferences.gameLanguage                            │
│   Apply:    SearchTermMapper.translateSyntax(...)                   │
│   Effect on UI text: NONE                                           │
└─────────────────────────────────────────────────────────────────────┘
```

## Why two layers?

Pokémon GO parses search strings against a fixed token grammar. A **localized** game client
may or may not accept a translated token, and getting it wrong silently returns no results
(or the wrong results). That makes the *search-string language* a safety decision, not a
cosmetic one. By contrast, the *app's UI language* is purely cosmetic.

Coupling them (e.g. "a Turkish UI should produce Turkish search strings") would be
dangerous: most Turkish tokens are unverified (see `turkish_verification_matrix.md`). So the
two layers are decoupled on purpose.

## Guarantees (enforced by tests in `LocalizationModelTest`)

1. **Auto (Safe) never resolves to Turkish.** `SearchStringLanguage.resolve("Auto")` always
   returns `English`, regardless of App Language or device locale.
2. **Turkish output is only ever emitted when the user explicitly chooses it** for Search
   String Language (`SearchStringLanguage.isTurkishExplicitlyChosen`).
3. **App Language has zero effect on the resolved search-string language.**
   `LocalizationModel.resolveSearchStringLanguageIndependentOf(searchPref, appPref)` ignores
   `appPref` entirely.
4. **No token is ever marked VERIFIED without live confirmation.** The
   `SearchTokenRegistry` documents every important token's verification status; as of v0.5.2
   none are VERIFIED.

## Where the layers live (code map)

| Concern | Location |
|---|---|
| Two-layer pure model + safety invariants | `domain/locale/LocalizationModel.kt` |
| App UI language controller (Layer A) | `domain/locale/AppLocaleController.kt` |
| Search-token registry with metadata (Layer B docs) | `domain/locale/SearchTokenRegistry.kt` |
| Active Turkish translation map (Layer B emit) | `domain/engine/SearchTermMapper.kt` |
| App Language application point | `MainActivity.kt` (`LaunchedEffect(appLanguage)` → `AppLocaleController.apply`) |
| Settings UI for both layers | `ui/screens/MiscScreens.kt` ("Search & Language" panel) |

## What this means for the user

- Choosing **Turkish** under *App Language* is a **Foundation** preference only. As of v0.5.5
  the app ships **no `values-tr/` string resources**, so choosing Turkish does **not** actually
  translate the interface today — most of the UI stays in English. The preference is recorded
  and ready for a future localization sprint, but Settings says so honestly (the options are
  labelled "Foundation"). Crucially it **keeps generating safe English search strings**.
- Choosing **Turkish (Beta)** under *Search String Language* translates the generated search
  strings, but shows the beta warning everywhere relevant and still routes risky copies
  through Risk Warning.
- Both can be combined (Turkish UI + Turkish search strings) or crossed
  (English UI + Turkish search strings) — either is valid and intentional.

> **Honesty rule (v0.5.5, Fix 2).** App Language must never be presented as if full UI
> translation already works. Until real `values-tr/` resources exist, the section copy and
> radio labels must keep a "Foundation / coming later" framing. Search String Language
> (Layer B) is fully active and unaffected.

## Do not

- ❌ Derive the search-string language from the app UI language or device locale.
- ❌ Auto-promote `Auto` to Turkish for any reason.
- ❌ Mark a token VERIFIED without a live Pokémon GO Turkish-client confirmation recorded in
  `turkish_verification_matrix.md`.
- ❌ Machine-translate Pokémon GO tokens as a source of truth. Only `SearchTokenRegistry` +
  the verified matrix are truth.
- ❌ Apply the OS per-app locale (`LocaleManager.setApplicationLocales`) from composition. The
  v0.5.2 original did this from a `SideEffect` and, because the pref loads asynchronously,
  the null→label flip recreated the Activity every frame → a permanent black screen on
  Samsung SM-S931B / Android 16 (v0.5.2.1 hotfix). Use `AppLocaleController.apply`, which
  sets only the in-process default locale and is recreation-free.

## v0.5.2.1 hotfix note

App Language is applied via `AppLocaleController.applyProcessLocale` (`Locale.setDefault`),
invoked from `MainActivity` through `LaunchedEffect(appLanguage)`. This is intentionally a
foundation: the app ships no `values-tr/` resources yet, so Settings presents App Language
honestly as "where translations are available — more coming." Selecting English or Turkish
must never black-screen the app, and the regression tests in `LocalizationModelTest`
(`applyProcessLocale …`) lock the recreation-free behavior down.
