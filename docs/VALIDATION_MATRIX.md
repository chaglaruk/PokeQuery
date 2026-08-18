# PokeQuery Validation Matrix

Use this as a minimum-evidence matrix. Current GitHub workflows and exact-ref code/tests remain the final authority; re-read them before relying on captured workflow facts.

| Change area | Minimum local/CI evidence | Additional gate |
|---|---|---|
| Docs only | `git diff --check`, link/path sanity | Verify no stale release/version/workflow claims |
| Android domain/search | `:app:testDebugUnitTest`, `:app:lintDebug` | generator safety / golden corpus when relevant |
| Android UI/copy | unit + lint + `:app:assembleDebug` | screenshots/contact sheet, six-locale overflow review |
| Search tokens/localization | Android tests + golden corpus + mapper/registry/official-syntax tests | current official FAQ evidence; live localized client before VERIFIED |
| Web engine | `check:golden-corpus`, typecheck, lint, unit, build | Android parity review |
| Web UI/routing/offline | typecheck, lint, unit, build, Playwright E2E | mobile/WebKit screenshots where visual behavior matters |
| Event feed/generator | generator safety/tests + enrichment tests + `validate_event_feed.py` | source/date/status/content sanity, strict CURRENT/UPCOMING detail quality, fallback freshness |
| Runtime assets | `check_runtime_assets.py` | IP/original-art review + visual QA |
| Android intents/widgets/locale/device bug | Android tests/lint/build | physical device/ADB validation |
| Release metadata | full relevant Android gate + exact versionName/versionCode | exact source SHA, AAB/signing gate, tag immutability |
| PWA deployment | deploy workflow green | deployed-site smoke; iOS Simulator Safari/PWA smoke when relevant |

## Current workflow facts — captured 2026-08-18

### `android-validate.yml`

Checks:
- Android/Web golden-corpus byte identity;
- Android bundled Event Guide fallback identity;
- Android unit tests;
- Android lint;
- generator safety;
- generator + enrichment + fallback-validator tests;
- canonical Event Guide validation;
- runtime asset validation.

### `deploy-pwa.yml`

Checks:
- `npm ci`;
- golden corpus;
- Web fallback freshness;
- typecheck;
- lint;
- unit tests;
- production build;
- full Playwright E2E with Chromium + WebKit.

Pages deployment occurs only for the master ref after the build job passes.

### `update-event-feed.yml`

Runs every 12 hours and via manual dispatch.

Current post-bug-hunt pipeline:
1. enrichment tests;
2. online event discovery/generation;
3. source-page enrichment with `--strict`;
4. feed validation;
5. synchronize canonical + Android fallback + Web fallback;
6. stage exactly those three feed files;
7. commit/push only when content changed.

A bot feed commit is not a new Android release source.

### `ios-simulator-smoke.yml`

Manual Safari/PWA smoke validation only. There is no native iOS app in this repository.

## Default commands

### Hygiene
```bash
git diff --check
git status --short
```

### Android
```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
```

### Event/search safety
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

## Completion rule

Queued/in-progress/launched is not PASS. Reach terminal CI and required visual/device gates before declaring completion.
