# PokeQuery Validation Matrix

> **2026-10-08 scope update:** PR42 visual acceptance and post-merge Android/PWA CI are complete development evidence. Current production is still 0.7.7/code 27. Do not rerun the historical Play audit or 30 captures; preserve 24 approved cells and defer six Event replacements. New narrow UI/SEO changes get their own relevant tests, visual evidence and exact-head CI. [Current status](release/PRERELEASE_EXECUTION_2026-10-08.md).

Use this as a minimum-evidence matrix. Current GitHub workflows and exact-ref code/tests remain the final authority; re-read them before relying on captured workflow facts.

| Change area | Minimum local/CI evidence | Additional gate |
|---|---|---|
| Docs only | `git diff --check`, link/path sanity | Verify no stale release/version/workflow claims |
| Android domain/search | `:app:testDebugUnitTest`, `:app:lintDebug` | generator safety / golden corpus when relevant |
| Android UI/copy | unit + lint + `:app:assembleDebug` | screenshots/contact sheet, six-locale overflow review |
| Search tokens/localization | Android tests + golden corpus + mapper/registry/official-syntax tests | current official FAQ evidence; live localized client before VERIFIED |
| Web engine | `check:golden-corpus`, typecheck, lint, unit, build | Android parity review |
| Web UI/routing/offline | typecheck, lint, unit, build | manual Playwright E2E only for targeted routing/offline regressions or explicit release gate; visual/WebKit review where relevant |
| Event feed/generator | generator safety/tests + enrichment tests + `validate_event_feed.py` | source/date/status/content sanity, approved HTTPS/redirect policy, event-title identity, strict CURRENT/UPCOMING detail quality, fallback freshness |
| Runtime assets | `check_runtime_assets.py` | IP/original-art review + visual QA |
| Android intents/widgets/locale/device bug | Android tests/lint/build | physical device/ADB validation |
| Release metadata | full relevant Android gate + exact versionName/versionCode | exact source SHA, AAB/signing gate, tag immutability |
| PWA deployment | fast deploy workflow green | deployed-site smoke; run manual Playwright only when the release/risk scope calls for it |

## Current workflow facts — captured 2026-10-01

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

Routine fast PR/deploy gate checks:
- `npm ci`;
- golden corpus;
- Web fallback freshness;
- typecheck;
- lint;
- unit tests;
- production build.

Playwright browser installation, E2E, and screenshot capture are intentionally absent from the routine PR gate. Pages deployment occurs only for the master ref after the fast build job passes.

### `pwa-e2e.yml`

Playwright is separated from routine PWA validation and runs only by manual `workflow_dispatch`.

Manual modes:
- `smoke`: Chromium-mobile only, covering base-path/routing, Event Guide network/cache/offline behavior, goal/search safety gates, navigation/overflow, PWA/service-worker behavior, and language persistence;
- `full`: the complete configured Chromium + WebKit project matrix, split across two Playwright shards for lower wall-clock release-gate latency;
- `visual`: deterministic Chromium-mobile screenshot capture.

There is no scheduled Playwright run and no automatic PR Playwright blocker. Historical successful PR runs measured the fast non-Playwright checks at roughly 25–30 seconds, Playwright browser installation at roughly 36–125 seconds, and the full multi-project E2E execution at roughly 3.4–4.2 minutes. That makes browser setup + E2E the dominant routine latency, so they remain explicit targeted/release gates.

### `update-event-feed.yml`

Runs every 12 hours and via manual dispatch.

Current post-bug-hunt pipeline:
1. enrichment tests, including approved-source/redirect and event-title identity regressions;
2. online event discovery/generation;
3. source-page enrichment with `--strict` under the approved HTTPS destination policy;
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

### Web/PWA fast gate
```bash
cd web
npm ci
npm run check:golden-corpus
npm run typecheck
npm run lint
npm test
npm run build
```

### Manual Playwright gate
Run GitHub Actions workflow `PWA Playwright E2E` only when specifically required. Prefer `smoke` for a targeted regression, `full` for a Web/PWA release gate, and `visual` when screenshot evidence is part of the task. Routine development remains on the fast deploy workflow.

## Completion rule

Queued/in-progress/launched is not PASS. Reach terminal CI and required visual/device gates before declaring completion. A Playwright run is only part of the required chain when the current change/release scope explicitly calls for the manual E2E gate.
