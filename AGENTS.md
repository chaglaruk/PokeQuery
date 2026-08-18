# PokeQuery — Agent Guide

This guide applies to all PokeQuery work: Android, Web/PWA, Event Guide data, localization, QA, CI, documentation and release tasks.

## 1. Scope

- This repository is PokeQuery only. Never mix it with PokeRarityScanner/PokemonRarityScanner or another project.
- PokeQuery generates and explains Pokémon GO inventory search strings for manual copy/paste. It does not authenticate to, control or read a Pokémon GO account.
- Android and Web/PWA are separate release surfaces. iOS coverage here is Safari/PWA smoke testing, not a native iOS app.

## 2. Source-of-truth hierarchy

For repository implementation and release claims, prefer:
1. current code, tests and workflows at the exact ref being worked on;
2. immutable release SHA/tag for claims about a released build;
3. current narrow canonical docs (`README.md`, `CHANGELOG.md`, `docs/localization/official_search_token_matrix.md`, `docs/DESIGN_LOCK.md`, Event Guide docs);
4. historical docs/audits only as background.

Changing external facts must be checked against current official sources. Do not silently reconcile conflicts: state them and repair stale docs when scope permits.

`master` can advance through scheduled Event Guide feed-only bot commits. Never assume moving master HEAD equals the latest Android release source.

## 3. ChatGPT-first delegation

- ChatGPT must do every task it can with available tools before delegating to Antigravity or another local agent.
- ChatGPT-owned work includes repository/GitHub inspection, commit/diff/PR analysis, official-source research, architecture/release decisions, CI/workflow inspection, safe GitHub-side edits and independent verification of agent reports.
- Local agents are for genuinely local needs: Windows checkout, unavailable SDK/Gradle/npm execution, physical ADB/device work, keystore/signing, APK/AAB generation or another local-only capability.
- Do not use the user as a relay for checks ChatGPT can perform.
- Agent reports are evidence, not authority.

## 4. Completion discipline

- `queued`, `in_progress`, launched validation or an agent saying PASS is not a PASS.
- Continue through the reachable chain: inspect -> change -> targeted validation -> full relevant validation -> CI -> diff/state hygiene -> visual/device gate -> final result.
- Only stop for a genuinely inaccessible local/manual action; delegate exactly that action with a tightly scoped prompt.
- Never require another user message merely to re-check CI that can be observed in the current task.

## 5. Product/privacy boundary

Core search generation stays local. Forbidden without explicit product re-scope:
- Pokémon GO account login/auth/credentials or private endpoints;
- gameplay automation, transfer/delete/batch actions;
- OCR/camera/screen reading of the game;
- analytics, telemetry, ads or tracking;
- unrelated remote calls or cloud user accounts.

Documented network exception: Event Guide may fetch the public PokeQuery static feed. Feed generation may research configured public event sources, but build-time enrichment must stay on the pipeline's approved HTTPS host/redirect policy. This exception does not authorize account access, telemetry or arbitrary networking.

Keep `allowBackup="false"`. Never commit keystores, passwords, tokens or signing secrets.

## 6. Search-string safety

- PokeQuery-generated strings never emit `|`, even where external Pokémon GO documentation accepts it.
- Preserve current engine delimiter behavior, exact-token protection/deduplication, Risk Warning gates and linter policy.
- Never invent unsupported syntax such as `untraded`.
- Token/parser/Search Assistant/risk/linter changes require tests and cross-platform parity review.
- Official localized Help Center evidence is BETA; VERIFIED requires independent live localized-client confirmation under the current registry model.
- Parser-sensitive or unsupported localized terms may deliberately fall back to canonical English. Never machine-translate syntax and mark it verified.

## 7. Localization

- App Language and Search String Language are separate controls.
- UI resources: EN/TR/DE/ES/FR/IT.
- Search String Language choices: Auto, Match App Language, EN/DE/ES/FR/IT/TR.
- Current semantics must be read from `LocalizationModel.kt`, `AppLocaleController.kt` and tests. Historical v0.5 assumptions are obsolete.
- Auto currently follows a supported device locale and falls back to English for unsupported locales.
- Match App Language follows an explicit App Language; when App Language is System Default it follows the supported device locale, with English fallback.
- Any visible copy change must be naturally localized across all six UI languages unless intentionally locale-specific.
- Translate meaning, not wording. Preserve safety/risk semantics and check compact-screen wrapping/overflow.
- Android and Web visible-copy parity must be maintained; do not assume one platform's locale files are generated from the other unless the current tooling proves it.

## 8. Android/Web parity

- Android core: Kotlin/Compose. Web core: React/TypeScript.
- Search engines intentionally mirror one another.
- Keep `web/src/parity/golden-corpus.json` and `app/src/test/resources/golden-corpus.json` byte-identical.
- Engine/linter/token/Search Assistant changes must update both platforms or document why behavior is intentionally platform-only.

## 9. Event Guide

- Canonical feed: `docs/event-feed/pokequery-events.json`.
- Source config: `docs/event-feed/sources.json`.
- Manual enrichment/override metadata: `docs/event-feed/event_metadata.json`.
- Discovery runs through `scripts/generate_event_feed.py`; source-page enrichment runs through `scripts/enrich_event_feed.py`.
- Prefer official Pokémon GO Live data when available; configured third-party sources are enrichment/fallback only.
- Build-time source-page fetches must use the approved HTTPS host policy and validate redirect/final destinations; do not broaden network destinations casually.
- The discovered/catalog event title is authoritative. Source-page H1 text may fill a genuinely missing title but must not silently rename an existing event entry.
- Date/status truth is mandatory. Never fabricate Pokémon, bonuses, raids, research, rewards or dates.
- CURRENT/UPCOMING gameplay entries must pass strict detail-quality validation before publication.
- The scheduled workflow generates online data, enriches with `--strict`, validates, then keeps canonical, Android bundled fallback and Web fallback synchronized.
- No official Pokémon/Niantic/Nintendo runtime art, sprites or logos.

## 10. UI/product quality

- Follow current code and `docs/DESIGN_LOCK.md`.
- Mockups/community infographics are layout inspiration only; never use baked UI screenshots or copied official/community assets at runtime.
- Use real Compose/React text/buttons/cards/chips/navigation.
- Keep copy short, human and ELI5; avoid generic AI-style filler.
- Home is a scannable choice surface; details belong on detail screens.
- No fake/no-op settings.
- Visual changes require screenshot/contact-sheet review, not only green tests.
- Accessibility baseline: >=48dp touch targets; 4.5:1 small-text contrast; 3:1 large text/graphics; meaningful semantics/content descriptions.

## 11. Git safety

- Never use `git add .`, `git add -A`, or `git add --all`. Stage explicit paths only.
- Preserve unrelated dirty changes.
- Do not force-push, retarget release tags, merge, version-bump, sign or publish a release until the relevant gate explicitly permits it.
- Before commit/push inspect `git status --short`, working diff and staged diff.
- Do not stage build outputs, temp files, QA-only screenshots, secrets or local configs accidentally.
- Release artifacts must be traceable to exact source SHA + versionName + versionCode.

## 12. Validation

Use scope-appropriate checks and treat current GitHub workflows as final authority.

### General
```bash
git diff --check
git status --short
```

### Android
```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug    # build/UI integration work
```
Release-affecting local work may additionally require `:app:bundleRelease` when signing/build context permits.

### Safety / Event Guide
```bash
python scripts/test_generator_safety.py
python -m unittest scripts.test_generator scripts.test_enrich_event_feed scripts.test_check_web_fallback_fresh
python scripts/validate_event_feed.py docs/event-feed/pokequery-events.json
python scripts/check_runtime_assets.py
```
For an intentional live feed regeneration, follow the actual workflow order: generate -> `enrich_event_feed.py ... --strict` -> validate -> synchronize both fallbacks.

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

Playwright E2E is intentionally separated from routine PR validation because it is slow. Do not block ordinary development on it. Run the manual `PWA Playwright E2E` workflow only for targeted routing/offline regressions or when an explicit release gate calls for it. Optimising/re-expanding automatic Playwright coverage is deferred work.

### Visual/device gates
- UI/copy/localization changes: screenshots/contact sheet + compact/long-locale review.
- Intent/clipboard/widget/locale lifecycle/device-specific fixes: appropriate physical-device/ADB validation.

## 13. Changing external facts

For Pokémon GO syntax/events, Google Play requirements, Android quality/policy or other current facts, verify current official sources. External official syntax does not automatically override stricter PokeQuery safety invariants.
