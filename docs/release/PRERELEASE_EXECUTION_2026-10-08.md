# Current pre-release execution status — 2026-10-08

This is the active status document. Original June/September/7 October audits remain dated historical evidence; their unimplemented/pending statements do not override current code. [Release plan](V078_RELEASE_PLAN.md) and [final listing drafts](../growth/PLAY_STORE_LISTING_PACK.md) contain the later publication steps.

## DONE

- Android 0.7.7/code 27 is published from immutable source `93bf178f942048e0fee8cbb47976b23f86ecde3f`. The physical Samsung Play install was independently verified non-debuggable and Play-signed on 2026-10-08. Production data/install are preserved.
- Web/PWA is independently versioned at 0.7.3. Fetched master at preparation is `cd9778b663c777cb98c6c196f8d87a5314fd696d`; post-merge Android CI and Pages deployment are terminal SUCCESS.
- PR #41/#42 source corrections are merged. PR42 visual QA established six-language corrected Event Guide fallback rendering, German Home and event-day labels in a separate .visualqa package. These are development evidence, not Play 0.7.8 screenshots.
- Previous Play Console listing/performance audit is complete; it was not repeated. Existing latest growth gate is **24/30 PASS**, with six Event cells rejected. All 24 approved images and raw companions were checked against their manifests and remain byte-for-byte unchanged. All rejection/history evidence is retained.
- Shipped local deterministic Search Assistant, Expert Builder, Knowledge Base, event fallback/cache, Settings confirmations, privacy protections, risk-gated Copy/Share and Android favorites-to-personal-preset workflow were reconciled with old audit claims. Do not reopen these as absent features.

## READY BUT NOT PUBLISHED / READY FOR REVIEW

- Six finished locale listing drafts with independently counted title/short/full limits and a field-by-field publication checklist. No live listing was changed.
- Proposed 0.7.8/code 28 release notes, source/signature/CI/production verification and six-screenshot recapture sequence. Values remain planned only.
- Narrow new review candidates: Knowledge Base reference/pipe copy guard on both platforms; static guide social metadata/mobile accessibility. Their PRs must be reviewed and expressly authorized for merge. They are not represented as shipped. Final local handoff records exact PR heads, CI runs and visual evidence.
- Consolidated documentation corrections distinguish current evidence from original historical findings.

| Review candidate | Exact reviewed head | Terminal checks (2026-10-08) |
|---|---|---|
| [PR #43: Knowledge copy guard](https://github.com/chaglaruk/PokeQuery/pull/43) | `ad5387e7b2b5bbad4c7764218c2d0c20db634837` | [Android](https://github.com/chaglaruk/PokeQuery/actions/runs/37832335399), [Web build](https://github.com/chaglaruk/PokeQuery/actions/runs/37832335518), [Chromium smoke](https://github.com/chaglaruk/PokeQuery/actions/runs/37833414862): SUCCESS |
| [PR #44: guide metadata/accessibility](https://github.com/chaglaruk/PokeQuery/pull/44) | `1264f1c4149c6e7cf092640f3ebcd6cc298ba3a7` | [Web build](https://github.com/chaglaruk/PokeQuery/actions/runs/37833311309), [Chromium smoke](https://github.com/chaglaruk/PokeQuery/actions/runs/37833419264): SUCCESS |

Both PRs remain open and unmerged. Independent reviews inspected their changes and addressed required findings. GitHub Codex review completed on #43; its service failed on #44 without producing a finding. CodeRabbit reported skipped automatic review despite a SUCCESS status context, which is not counted as substantive review. No human approval is inferred from CI.

## BLOCKED

- Growth 30/30: Play 0.7.7 lacks the merged Event card renderer. Online feed correction cannot supply the binary UI change. Shortest resolution: separately authorized 0.7.8 release, verified Play update, then six Event replacements only.
- Private Reddit moderator replies cannot be verified without an accessible Reddit session. The current browser encounters a humanity challenge; no challenge bypass or new modmail is attempted. A public post in r/SilphRoad is not approval from r/TheSilphRoad.
- Google indexing is unverified. Static HTTP/sitemap/canonical availability and a public search with no located result do not establish index status. Read-only Search Console URL inspection requires access to the appropriate verified property. The project-level robots file cannot serve origin-root robots for the separate github.io origin repository; no unrelated repository is modified.

## DEFERRED UNTIL SEPARATE RELEASE / PUBLICATION AUTHORIZATION

- Merge/release metadata/tag/sign/AAB upload/Play rollout, live listing updates, six Event production captures and full growth ZIP replacement. Current versions and production package remain untouched.
- Any new public post, moderator request, paid promotion or listing experiment. Paid acquisition remains closed; organic feedback does not prove conversion uplift.
- Broader development-dependency updates: current npm audit reports existing advisories. Report their actual exposure, prioritize a separate toolchain upgrade with CI/e2e, and do not claim vulnerabilities fixed by unrelated SEO/UI work.

## FUTURE IDEA / UNSCHEDULED

| Candidate | Evidence/impact | Risk and acceptance criteria |
|---|---|---|
| Live localized-token records | Official localized documentation is BETA; no new live-client verification performed | Verify independently on actual localized clients; date/source/token/client evidence, preserve canonical fallback and corpus parity; never infer VERIFIED from translation |
| Accessibility/larger font scales | Previous Samsung visual profile does not cover every scale or narrow phone | Record normal/enlarged scales and narrow-width screenshots; no clipped titles/warnings; >=48dp targets and required contrast; no concealment to pass |
| Favorites/presets refinement | Android favorite-to-personal-preset already ships; further Web parity/editor improvements may be useful | Specify an actual missing operation first; round-trip local query, preserve risk/mandatory exclusions; do not rebuild the shipped bridge |
| Personalized scope | A proposal, not verified repeated demand | Defaults only, local-only, reversible; cannot broaden action-adjacent output or downgrade risk |
| Knowledge Base content localization | UI supports six languages, catalogue explanations currently EN/TR with English fallback elsewhere | Native review plus token confidence/source freshness; don't translate syntax as prose or treat references as complete searches |
| Event preparation/sharing | Real manual filters discussed publicly; no proven demand for a larger planner | Test one user workflow, preserve dates/region/source and risk gates; no game/account access or automatic execution |
| Toolchain dependencies | npm audit flags build/dev-server/test packages and router-related advisories | Upgrade narrowly under a dedicated compatibility review, Node20 baseline, all local/CI/Playwright gates; never expose dev/test servers publicly |

Cloud AI, OCR/scanning, Pokémon GO accounts, transfer automation, ads and analytics are not supported product directions under current boundaries. No speculative large feature is scheduled for 0.7.8.

## Evidence reconciliation

Original June BUG-001 traded protection, locale-confidence assumptions, Expert linter gaps, preset protections, dynamic version display, Settings confirmations, allowBackup and R8 concerns must be checked against current tests rather than replayed as open blockers. BUG-014's parameter-template clipboard issue was reproduced in current Knowledge code and is addressed by the new narrow review candidate. Heuristic scope measurement, alias edge cases and comprehensive catalogue localization need specific new reproductions before implementation.

September launch records were statements at the time, not present-day outcomes. Public evidence now locates additional IMadeThis, droidappshowcase and pokemongodev submissions. Indiehackers shows a moderator-removal notice. The GooglePlayDeveloper post's current result and private r/pokemongo/r/TheSilphRoad modmail remain unverified. See the sanitized local COMMUNITY_FEEDBACK report for sources and follow-up priorities. No absence of comments, demand or installs is inferred from an incomplete public rendering.

A read-only review check found an older 0.7.6/code 26 complaint about translation, untranslated searches and interface complexity. Current German QA generates localized terms but still includes a canonical English fallback token; this does not establish complete localized-token correctness or resolve the historical complaint. Prioritize specific language/query reproductions and native copy review before claiming improvement. No review reply or live listing change was made.
