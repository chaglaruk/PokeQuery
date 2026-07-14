# PokeQuery PWA — Agent Guide

See `web/README.md` for full PWA documentation.

## Commands

```bash
cd web
npm run typecheck    # tsc --noEmit
npm run lint         # eslint .
npm test             # vitest run (111 tests)
npm run build        # tsc -b && vite build (produces dist/ with PWA SW)
npm run dev          # vite dev server
npm run test:e2e     # playwright e2e tests (20 scenarios, requires build first)
```

## Key Files

- `web/src/engine/` — TypeScript port of Android search engine (1:1 parity)
- `web/src/__tests__/` — Parity tests + golden corpus + event feed tests
- `web/src/parity/golden-corpus.json` — Shared parity corpus (44 cases)
- `app/src/test/resources/golden-corpus.json` — Copy for Android JUnit
- `app/src/test/java/.../GoldenCorpusParityTest.kt` — Android parity test
- `web/e2e/` — Playwright E2E tests (20 scenarios)
- `web/src/i18n/` — I18n context + 6 locale files
- `web/src/ui/screens/` — React screens (Home, GoalDetail, Events, Explain, Settings, Onboarding, Changelog)
- `.github/workflows/deploy-pwa.yml` — Pages deploy workflow
