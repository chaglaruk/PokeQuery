# PokeQuery PWA — Agent Guide

See `web/README.md` for full PWA documentation.

## Project Working Rules

These rules apply to all future PokeQuery work, not only to one chat/session.

### ChatGPT-first delegation

- ChatGPT must do every task it can perform with its own available tools before delegating work to Antigravity or another local agent.
- ChatGPT-owned work includes repository/GitHub inspection, commit/diff/PR analysis, web and official-source research, architecture/release decisions, CI and workflow inspection, GitHub-side edits that are safe to perform remotely, and independent verification of agent reports.
- Antigravity/local agents should receive only work that genuinely requires the user's local Windows environment, Android SDK/Gradle/npm execution unavailable to ChatGPT, physical-device/ADB interaction, local keystore/signing, APK/AAB generation, or another capability ChatGPT cannot access.
- Do not use the user as a relay for checks ChatGPT can perform itself.
- Reports from Antigravity or other agents are evidence, not authority: independently verify GitHub-visible commits, diffs, PR state, CI, and release metadata before accepting them.

### Completion and response discipline

- Do not end the user-facing task while a check, CI run, build, test, or other gate that ChatGPT can continue observing or resolving is still pending.
- `queued`, `in_progress`, or merely launched validation is not a PASS.
- Continue through the full reachable chain: change -> targeted validation -> full relevant validation -> CI -> diff/state verification -> required physical/local validation -> final result.
- Only stop early when a genuinely inaccessible local/manual action is required; in that case delegate exactly that action to Antigravity with a tightly scoped prompt.
- Never ask the user to send another message merely so ChatGPT can re-check a test or CI run that ChatGPT can check itself in the same task.

### Git safety

- Never use `git add .`, `git add -A`, or `git add --all` in agent instructions. Stage explicit paths only.
- Do not force-push, retarget existing release tags, merge, version-bump, or sign/build a release artifact until the relevant gate explicitly permits it.

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
