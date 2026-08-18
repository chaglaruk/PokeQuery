<p align="center">
  <img src="docs/readme/pokequery_repo_banner.png" alt="PokeQuery - Build safer Pokémon GO search strings" width="100%" />
</p>

<p align="center">
  <strong>Build safer Pokémon GO search strings.</strong><br/>
  An offline-first Android and Web/PWA utility that turns goals into copy-ready, safety-checked search strings.
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android%20%7C%20Web%2FPWA-3DDC84?logo=android&logoColor=white" />
  <img alt="Language" src="https://img.shields.io/badge/language-Kotlin%20%7C%20TypeScript-7F52FF?logo=kotlin&logoColor=white" />
  <img alt="UI" src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20React-4285F4?logo=jetpackcompose&logoColor=white" />
  <img alt="Offline-first" src="https://img.shields.io/badge/offline-first-0099E5" />
  <img alt="No tracking" src="https://img.shields.io/badge/tracking-none-success" />
  <img alt="Version" src="https://img.shields.io/badge/Android%20v0.7.5-Web%20v0.7.3-00E0FF" />
  <img alt="Status" src="https://img.shields.io/badge/status-closed%20testing-FFB400" />
</p>

<p align="center">
  <em>Unofficial fan-made utility. Not affiliated with, endorsed by, or sponsored by Niantic, The Pokémon Company, or Nintendo.</em>
</p>

---

## What is PokeQuery?

PokeQuery is an **offline-first Android and Web/PWA companion utility** that helps Pokémon GO players organize their storage. You pick a goal — *Safe Cleanup*, *2x Candy Prep*, *Trade Fodder*, *Event Guide* — and PokeQuery builds a carefully constructed **search string** from a tested rule engine. You then **manually copy** that string and **paste it into Pokémon GO** yourself.

It is **not** an IV checker, scanner, bot, automation tool, or Pokémon GO account tool. It generates text, and you decide what to do with it. Every risky action is gated behind an explicit review step, and protected categories (shiny, legendary, mythical, shadow, lucky, traded, favorite, high-IV) are excluded by default.

> Why "safer"? Because a badly-built search string can surface the wrong Pokémon and lead to an accidental transfer. PokeQuery's engine is conservative: when in doubt, it protects rather than exposes.

---

## Screenshots

<p align="center">
  <img src="docs/readme/pokequery_readme_showcase.png" alt="PokeQuery screens - Home, Onboarding, Safe Cleanup, Popular Presets, Knowledge Base, Settings" width="100%" />
</p>

<details>
<summary><strong>📸 Full screen gallery</strong></summary>

| | | |
|:---:|:---:|:---:|
| <img src="docs/readme/screenshots/01_home.png" width="240" alt="Home"/> | <img src="docs/readme/screenshots/02_onboarding.png" width="240" alt="Onboarding"/> | <img src="docs/readme/screenshots/03_safe_cleanup_risk_warning.png" width="240" alt="Safe Cleanup with Risk Warning"/> |
| **Home** | **Onboarding** | **Safe Cleanup · Risk Warning** |
| <img src="docs/readme/screenshots/04_popular_presets.png" width="240" alt="Popular Presets"/> | <img src="docs/readme/screenshots/05_knowledge_base.png" width="240" alt="Knowledge Base"/> | <img src="docs/readme/screenshots/06_settings.png" width="240" alt="Settings"/> |
| **Popular Presets** | **Knowledge Base** | **Settings** |

</details>

---

## What can it do?

| Goal | What it does |
|---|---|
| 🧹 **Safe Cleanup** | Excludes high-IV (4★), shiny, legendary, mythical, ultra beast, costume, shadow, purified, favorite, lucky, and traded Pokémon so you can safely review low-IV catches for transfer. |
| 🍬 **2x Candy Prep** | Surfaces candidates worth transferring during a Candy bonus event — duplicate families, low-IV commons — while protecting rare and special forms. |
| 🔁 **Trade Fodder** | Lists commons ideal for re-rolling via trading, with notes on distance and trade economics. |
| ✨ **Lucky Trade** | Highlights older Pokémon more likely to go Lucky when traded. |
| 💯 **Hundo / Nundo** | Find perfect (4★) or worst (0★) IV entries for collection review. |
| ⚔️ **PvP Candidates** | League-specific (Great / Ultra) candidate filters for competitive review. |
| 📅 **Event Guide** | Surfaces active and upcoming in-game events with summaries, storage prep tips, and suggested search strings. Uses read-only public feed data with local caching and offline bundled fallback. |
| 🔧 **Expert Builder** | Compose your own query from grouped chips (status, tags, IV, age, distance…). Live preview + a linter that blocks unsafe combinations. |
| 📖 **Knowledge Base** | Researched entries on Pokémon GO search syntax, with tier and risk badges — sourced from official Niantic help docs, stored locally. |
| ⚡ **Popular Presets** | Curated, safety-reviewed one-tap strings for common tasks. |
| ⭐ **Favorites & History** | Save useful strings and revisit recent ones. Stored on-device only. |
| 🌐 **Language Safety** | Two independent language layers (see below) so the search-string language never silently changes. |

---

## Safety-first by design

PokeQuery treats a search string as a **suggestion, not a command**. The app itself never modifies your Pokémon GO storage — it only produces text you choose to act on.

- **Risk Warning gate** — every Medium/High-risk copy route (e.g. Safe Cleanup) opens an explicit *Accept & Copy / Review Query* screen before the string reaches your clipboard.
- **Protected by default** — shiny, legendary, mythical, ultra beast, costume, shadow, purified, favorite, lucky, traded, and high-IV (4★) Pokémon are excluded by default.
- **Conservative `Auto (Safe)`** — when unsure, the engine resolves to English and protects rather than exposes.
- **No automation** — there is no "transfer", "delete", or batch-action button. PokeQuery cannot perform any action in Pokémon GO.
- **No account connection** — the app never talks to Pokémon GO, Niantic, or private game servers. Copy/paste is 100% manual.
- **Linter** — the Expert Builder refuses to emit strings the engine considers unsafe (e.g. contradictory or empty-result queries).

---

## Privacy

PokeQuery is privacy-first by design. Read the authoritative policy at [`privacy.html`](https://chaglaruk.github.io/PokeQuery/privacy.html).

| | |
|---|---|
| 👤 Accounts | None. No login, no registration, no sessions. |
| 🌐 Network | Read-only HTTPS access to the public GitHub-hosted Event Guide feed only. Cached locally for offline use. |
| 📊 Analytics | None. No crash SDKs, no analytics, no telemetry. |
| 🚫 Ads | None. |
| 💾 Storage | All favorites, history, presets, and settings live locally on the device (Android Jetpack DataStore / Web LocalStorage). |
| ☁️ Cloud sync | None. There is no remote user database. |

---

## Language support

PokeQuery keeps two language layers **fully independent**. Changing one never affects the other — this is enforced by unit tests in `LocalizationModelTest`.

| Layer | Controls | Options |
|---|---|---|
| **App Language** | Interface text only (labels, buttons) | System Default · English · Türkçe · Deutsch · Español · Français · Italiano |
| **Search String Language** | The text you copy & paste into Pokémon GO | Auto · Match App Language · English · German · Spanish · French · Italian · Turkish (Beta) |

- **`Auto` resolves safely to English** unless an explicit localized search string language is selected.
- **Search token mappings** are documented from official Niantic Help Center materials (FAQ 1486). See [`docs/localization/official_search_token_matrix.md`](docs/localization/official_search_token_matrix.md) for the complete reference matrix and verification tiers (`BETA` vs `VERIFIED`).
- See [`docs/localization/localization_architecture.md`](docs/localization/localization_architecture.md) for the architecture and invariants.

---

## Tech stack

- **Android App**: Kotlin + Jetpack Compose (Material 3), Navigation, ViewModel, DataStore Preferences (`minSdk 24`, `targetSdk 36`, `compileSdk 36`).
- **Web / PWA**: React 18, TypeScript, Vite, Workbox PWA service worker with offline caching.
- **Cross-Platform Engine**: Deterministic `StringBuilderEngine`, `SearchTermMapper`, `ExpertCopyPolicy`, validated by a synchronized cross-platform `golden-corpus.json`.

---

## Build from source

> Android build requires JDK 17 and Android Studio. Web build requires Node.js 20+.

### Android:
```bash
# Clone
git clone https://github.com/chaglaruk/PokeQuery.git
cd PokeQuery

# Run Android unit tests
./gradlew :app:testDebugUnitTest

# Build debug APK
./gradlew :app:assembleDebug
```

### Web / PWA:
```bash
cd web
npm ci
npm run typecheck
npm test
npm run build
```

---

## Testing & quality

| Check | What it guards |
|---|---|
| `./gradlew :app:testDebugUnitTest` | Android domain engine, expert linter, risk messages, localization invariants, popular-preset safety, saved-template codec, golden corpus parity. |
| `npm test` | Web engine parity, search intent parser, event lifecycle, golden corpus parity, UI language independence. |
| `npx playwright test` | Multi-browser PWA end-to-end suite across desktop, mobile Chrome, and WebKit (iPhone SE, 13, 14 Pro Max). |
| `python scripts/test_generator_safety.py` | Guards against pipe (`|`) generation in search strings and protects feed safety invariants. |
| `python scripts/check_runtime_assets.py` | Fails if a runtime image asset is missing from allowlist, non-square, or invalid. |

---

## Closed testing

PokeQuery is currently in **Google Play Closed Testing**.

- 👥 **Google Group:** <https://groups.google.com/g/pokequery>
- 🧪 **Opt-in (testers):** <https://play.google.com/apps/testing/com.caglar.pokequery>
- 📱 **Play Store:** <https://play.google.com/store/apps/details?id=com.caglar.pokequery>

---

## Roadmap

These are realistic, in-scope directions:

- 🔬 Independent live-client confirmation of localized tokens (`BETA` -> `VERIFIED`)
- 🛡️ Safer, refined preset library additions
- ♿ Accessibility enhancements across Android and Web
- 📖 Expanded Knowledge Base entries

---

## Disclaimer

PokeQuery is an **independent fan-made utility** and is **not affiliated with, endorsed by, or sponsored by Niantic, The Pokémon Company, or Nintendo**. Pokémon GO and all related marks, names, and characters are trademarks of their respective owners. PokeQuery does not access Pokémon GO accounts, automate gameplay, or use any official assets.

All artwork in this repository (app icon, wordmark, banners) is **original** and contains no Pokémon creatures, Poké Ball imagery, or official logos. See [`docs/release/IP_ASSET_AUDIT.md`](docs/release/IP_ASSET_AUDIT.md).

---

## License

Released under the **MIT License**. See [`LICENSE`](LICENSE).

```
MIT License - Copyright (c) 2026 PokeQuery contributors
```

> Pokémon GO is a trademark of Niantic, Inc. / The Pokémon Company. This project
> neither uses nor redistributes any of their assets.
