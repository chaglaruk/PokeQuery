# PokeQuery Copilot Instructions

PokeQuery is an offline-first Android app with a companion web PWA. The repository rules below are authoritative and must be preserved:

- [AGENTS.md](../AGENTS.md)
- [README.md](../README.md)
- [CONTRIBUTING.md](../CONTRIBUTING.md)
- [docs/ROADMAP.md](../docs/ROADMAP.md)
- [docs/DESIGN_LOCK.md](../docs/DESIGN_LOCK.md)
- [docs/spec_summary.md](../docs/spec_summary.md)
- [docs/localization/localization_architecture.md](../docs/localization/localization_architecture.md)
- [docs/release/RELEASE_READINESS.md](../docs/release/RELEASE_READINESS.md)
- [web/README.md](../web/README.md)
- [.github/workflows/deploy-pwa.yml](./workflows/deploy-pwa.yml)

If a task-specific doc conflicts with a broader one, follow the most specific and explicit rule, and report the contradiction instead of inventing a new policy.

## PokeQuery invariants

- The app remains offline-first. Do not add login, user accounts, analytics, tracking, scraping, or unnecessary network behaviour.
- Preserve the Android manifest and privacy/security constraints documented by the repository, including zero-permission expectations and `allowBackup=false` where those are currently authoritative. Do not weaken privacy, backup, export, or local-data protections.
- PokeQuery generates Pokémon GO search text only; do not add Pokémon GO login, gameplay automation, OCR, scraping, or account integration.
- Preserve the repository’s localization architecture and fallback behaviour. Do not hard-code user-facing text when the localization system should be used.
- Preserve locked terminology and prohibited wording defined by the authoritative localization and design documents, including currently authoritative Turkish terminology rules such as `Link Charges` → `Bağlantı Şarjı` and `Fusion Energy` → `Füzyon Enerjisi`.
- Do not invent translations or silently normalize locked copy.
- Preserve the constraints in `docs/DESIGN_LOCK.md`. Do not redesign the UI unless the task explicitly authorizes design changes.
- Figma context must not override locked design, application behaviour, accessibility, or localization rules.
- Preserve stable Playwright selectors and existing test hooks unless a task explicitly requires a coordinated selector/test migration.
- Do not bake UI text or inappropriate UI elements into runtime assets where the design lock prohibits it.
- Android and PWA search-engine behaviour must remain in parity. Treat the shared golden corpus as authoritative parity evidence, and do not claim parity from compilation alone.
- Preserve search-language invariants from the authoritative documents. The `|` operator is prohibited, `!traded` must remain exactly once, and search syntax, query ordering, escaping, and generation semantics must not change without updating and passing the shared parity/golden-corpus tests.

When changing Android or PWA engine or corpus files, verify both sides and keep the corpus copies synchronized. For web/PWA work, use the existing repository commands as applicable: `cd web`, `npm run check:golden-corpus`, `npm run typecheck`, `npm run lint`, `npm test`, `npm run build`, and `npm run test:e2e`. For Android work, use the currently documented Gradle and repository validation commands from `AGENTS.md` and the project docs. Select the narrowest relevant subset first; do not run every suite mechanically for documentation-only tasks.

## General safety

- External tools are optional capabilities, not mandatory steps.
- At the start of a task, inspect which tools are available and use the minimum relevant set.
- Do not claim a tool was used unless a successful tool result was received.
- If an external tool is unavailable, continue with the strongest appropriate local verification and report the missing external validation.
- Never place credentials, tokens, secrets, screenshots with private telemetry, or other sensitive data in repository files.
- Do not commit, push, open pull requests, or perform destructive actions unless the task explicitly authorizes them.

## GitHub MCP

- Use GitHub MCP for repository metadata, issues, pull requests, reviews, and CI context when that information is useful to the task.
- Do not create, modify, merge, or close GitHub resources unless explicitly authorized.

## Sentry MCP

- Use Sentry only for existing runtime errors, crashes, exceptions, regressions, traces, or performance evidence.
- Do not add a Sentry SDK just because MCP access exists.
- Do not modify Sentry issues, projects, alerts, or settings unless explicitly authorized.
- Do not expose personal data, screenshots, raw OCR text, secret values, tokens, or private telemetry payloads.

## BrowserStack MCP

- Use BrowserStack only for real-device, responsive, mobile-browser, cross-browser, or platform-specific validation when the task justifies it.
- Prefer local tests first unless the task is specifically about BrowserStack or remote-device validation.
- Prefer a minimal smoke matrix before any broader device matrix.
- Do not add BrowserStack CI or workflow integration merely because MCP access exists.

## Figma MCP

- Use Figma only when the task includes a Figma URL, frame, selected node, component, or explicitly approved design work.
- Treat Figma as design context, not authority over application logic, accessibility, localization, architecture, or tests.
- Do not redesign screens merely because Figma access is available.

## JetBrains

- JetBrains IDEs may exist as manual development environments, but they are not MCP tools.
- Do not claim to have used Android Studio, IntelliJ, or WebStorm-only functionality unless that was actually performed or its output was supplied.

## Repo workflow

- Keep patches as small as possible and avoid unrelated refactors.
- Preserve existing repo-specific rules, comments, test expectations, and safety/privacy boundaries.
- For Android changes, prefer the narrowest relevant verification first, such as `./gradlew :app:testDebugUnitTest`, `./gradlew :app:lintDebug`, and `python scripts/check_runtime_assets.py` when runtime assets change.
- For web changes, prefer the narrowest relevant verification first, such as `cd web && npm run check:golden-corpus` when parity changes, plus `npm run typecheck`, `npm run lint`, `npm test`, `npm run build`, and `npm run test:e2e` when the touched area justifies them.
- Review the final diff before finishing and call out any residual risk or missing external validation.
