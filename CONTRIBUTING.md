# Contributing to PokeQuery

Thanks for your interest in PokeQuery! This project is a small, safety-first Android
utility, and contributions are welcome — especially in these areas:

- 🌐 verified language mappings (Turkish tokens confirmed against a live client)
- 📚 Knowledge Base entries
- ♿ accessibility and Compose test tags
- 🧪 tests and bug fixes

Before contributing, please read the **hard constraints** below — they are
non-negotiable because they define what makes PokeQuery trustworthy.

---

## Hard constraints (read first)

PokeQuery is **offline-first and privacy-first by design**. The following are
forbidden in any contribution:

- ❌ Any `<uses-permission>` that grants network, phone, SMS, contacts, location,
  camera, or microphone access. The manifest must stay permission-free.
- ❌ Any network call, HTTP client, remote config, or cloud dependency.
- ❌ Ads, analytics, telemetry, or crash-reporting SDKs of any kind.
- ❌ Login, accounts, authentication, or Pokémon GO account access.
- ❌ Gameplay automation, scripting, or any "transfer/delete/batch" action against
  the game. PokeQuery generates **text only**; the user copies it manually.
- ❌ Scanning, OCR, or screen-reading of the Pokémon GO app.
- ❌ Scraping of any data source. The knowledge base is a **local** file.
- ❌ Official Pokémon, Niantic, Nintendo, or The Pokémon Company assets — including
  creatures, Poké Ball imagery, the official logo, or trademarked fonts.
- ❌ The `|` operator in generated search strings (Pokémon GO uses `,` for OR).

If your change would require breaking any of these, please **open an issue first**
to discuss it before writing code.

---

## Safety & privacy review checklist

Every PR that touches app code is expected to keep these green:

- [ ] `app/src/main/AndroidManifest.xml` still declares **zero** `<uses-permission>`.
- [ ] `allowBackup="false"` is unchanged.
- [ ] No new network/ads/analytics dependencies in `app/build.gradle.kts`.
- [ ] The Risk Warning gate still routes every Medium/High-risk copy.
- [ ] `Auto (Safe)` still resolves to English (never Turkish).
- [ ] No new binary asset is added without updating `scripts/check_runtime_assets.py`.
- [ ] All artwork is original — no third-party or copyrighted images.

---

## Getting set up

Requires **JDK 17** and **Android Studio** with AGP 9.x support.

```bash
git clone https://github.com/chaglaruk/PokeQuery.git
cd PokeQuery
./gradlew test --console=plain        # unit tests must pass
./gradlew assembleDebug --console=plain
```

Release signing uses a local `keystore.properties` + `release-keystore.jks` that are
**not** in the repo. You do not need them to build or test.

---

## Running the checks

Before opening a PR, run:

```bash
git status --short                         # nothing unexpected staged
./gradlew test --console=plain             # all unit tests pass
python scripts/check_runtime_assets.py     # asset guard passes
./gradlew assembleDebug --console=plain    # debug build succeeds
```

If you touched runtime assets, also run `./gradlew bundleRelease --console=plain`
and confirm the AAB still builds.

---

## Coding style

- **Kotlin**, idiomatic, following the surrounding code.
- **Jetpack Compose** for all UI — no XML views.
- Domain logic (engine, mapper, linter, risk messages) stays **pure Kotlin** with no
  Android imports, so it stays unit-testable.
- Match the existing comment density and naming. Prefer descriptive names over
  comments where possible.
- New behavior needs a test. The domain engine in particular is regression-tested;
  don't weaken an existing invariant without an explicit discussion.

---

## Adding or changing search tokens

The truth for Pokémon GO search syntax lives in:

- `app/src/main/assets/knowledgebase.json` (researched entries, sourced from
  official Niantic help docs)
- `app/src/main/java/com/caglar/pokequery/domain/locale/SearchTokenRegistry.kt`
  (verification status metadata)
- `docs/localization/turkish_verification_matrix.md` (Turkish token verification)

**Never** machine-translate a token and mark it `VERIFIED`. A token is `VERIFIED`
only after live confirmation against a real localized Pokémon GO client, recorded
in the matrix.

---

## Commit messages

Use concise, lowercase imperative messages, e.g.:

- `add great league preset`
- `fix expert builder chip wrapping`
- `improve repo presentation and README`

---

## Branching

- Work on a feature branch, e.g. `feature/<short-description>`.
- Do **not** push to or merge into `master` without explicit sign-off.
- Keep PRs focused; one logical change per PR is ideal.

---

## Reporting bugs

Open an issue with:

- PokeQuery version (shown in **Settings → About**)
- Android version and device
- Steps to reproduce
- Expected vs. actual behavior
- A screenshot if it helps

Please **do not** include any Pokémon GO account information, screenshots of your
Pokémon collection, or other private data.

---

Thank you for helping make PokeQuery safer and more useful. 💙
