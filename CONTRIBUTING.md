# Contributing to PokeQuery

Thanks for contributing to PokeQuery. This repository contains the Android app, Web/PWA, Event Guide feed pipeline, localization data and their shared safety/parity tests.

Before changing code, read `AGENTS.md` and the hard constraints below.

## Hard constraints

PokeQuery is privacy-first and text-only. Contributions must not add:

- Pokémon GO account login, credentials, private/hidden endpoints or account access;
- gameplay automation, scripting, transfer/delete/batch actions;
- OCR, camera or screen reading of Pokémon GO;
- analytics, telemetry, ads, tracking or crash-reporting SDKs;
- cloud user accounts or unrelated remote calls;
- official Pokémon/Niantic/Nintendo/The Pokémon Company runtime artwork, sprites, logos, Poké Ball imagery or trademarked fonts;
- `|` in PokeQuery-generated search strings.

### Documented Event Guide network exception

Android declares `android.permission.INTERNET` only for the documented public Event Guide feed path. The Event Guide generator may fetch configured public event sources to build the static feed.

This exception does **not** authorize arbitrary HTTP clients, telemetry, account access, remote config or unrelated cloud dependencies. Runtime app networking must remain limited to explicitly documented public-data features.

If a proposal requires changing these product boundaries, discuss the product re-scope before implementation.

## Safety and privacy checklist

Every relevant PR should confirm:

- [ ] Android permissions remain limited to the documented requirements; no new sensitive permission was added.
- [ ] `allowBackup="false"` remains unchanged.
- [ ] No new ads/analytics/tracking/account-access dependency was added.
- [ ] Generated search strings never emit `|`.
- [ ] Risk Warning and linter gates remain intact for action-adjacent/risky searches.
- [ ] Android/Web engine changes preserve parity and the golden-corpus copies remain byte-identical.
- [ ] Event Guide networking remains within the documented public-feed/source pipeline.
- [ ] New runtime art/assets pass `scripts/check_runtime_assets.py` and IP review.

## Development setup

Requires JDK 17 and the Android toolchain used by the current project.

```bash
git clone https://github.com/chaglaruk/PokeQuery.git
cd PokeQuery
./gradlew :app:testDebugUnitTest --console=plain
./gradlew :app:assembleDebug --console=plain
```

Release signing uses local signing material that is not committed. It is not required for normal debug/test work.

## Validation

Choose checks based on the changed area; `AGENTS.md`, `docs/VALIDATION_MATRIX.md` and current GitHub workflows are authoritative.

### Android
```bash
./gradlew :app:testDebugUnitTest --console=plain
./gradlew :app:lintDebug --console=plain
./gradlew :app:assembleDebug --console=plain
```

### Search / Event Guide / runtime assets
```bash
python scripts/test_generator_safety.py
python -m unittest scripts.test_generator scripts.test_enrich_event_feed scripts.test_check_web_fallback_fresh
python scripts/validate_event_feed.py docs/event-feed/pokequery-events.json
python scripts/check_runtime_assets.py
```

### Web/PWA
```bash
cd web
npm ci
npm run check:golden-corpus
npm run typecheck
npm run lint
npm test
npm run build
npm run test:e2e
```

UI/copy/localization work also requires visual review; device-specific Android behavior requires appropriate physical-device/ADB validation.

## Coding style

- Kotlin/Compose for Android; React/TypeScript for Web/PWA.
- Keep domain/search logic deterministic and testable.
- New behavior needs focused regression coverage.
- Avoid comment-driven architecture drift: update stale docs/comments when behavior deliberately changes.
- Preserve exact-token behavior; do not replace token parsing with unsafe substring shortcuts.

## Search tokens and localization

Current search-language architecture is documented in:

- `app/src/main/java/com/caglar/pokequery/domain/locale/OfficialSearchSyntax.kt`
- `app/src/main/java/com/caglar/pokequery/domain/locale/SearchTokenRegistry.kt`
- `app/src/main/java/com/caglar/pokequery/domain/engine/SearchTermMapper.kt`
- `docs/localization/official_search_token_matrix.md`
- `docs/localization/localization_architecture.md`

Rules:

- App Language and Search String Language are separate controls.
- UI languages: EN/TR/DE/ES/FR/IT.
- Search String Language includes Auto, Match App Language and EN/DE/ES/FR/IT/TR.
- Official localized Help Center evidence is BETA in the current confidence model.
- VERIFIED requires independent confirmation in a live localized Pokémon GO client.
- Never machine-translate a syntax token and call it verified.
- Parser-sensitive or unsupported localized terms may intentionally fall back to English.
- PokeQuery's no-pipe policy remains stricter than external game documentation.

## Event Guide contributions

Canonical feed: `docs/event-feed/pokequery-events.json`.

The scheduled pipeline performs online discovery, source-page enrichment with a strict quality gate, validation and synchronization of Android/Web fallbacks. Do not hand-invent missing dates, Pokémon, bonuses, raids or research to make an event look complete.

Prefer official Pokémon GO Live information when available; configured third-party sources are enrichment/fallback only.

## Git and branching

- Work on focused branches.
- Never use `git add .`, `git add -A` or `git add --all`; stage explicit paths.
- Preserve unrelated dirty work.
- Do not force-push or retarget an existing release tag.
- Do not merge, version-bump, sign or publish a release without the relevant gate.
- A moving `master` may include Event Guide feed-only bot commits and must not be treated as an immutable Android release source.

## Reporting bugs

Include:
- PokeQuery version from Settings → About;
- Android/browser version and device where relevant;
- steps to reproduce;
- expected vs actual behavior;
- screenshot/log evidence if useful.

Do not include Pokémon GO credentials, session data or private collection information.
