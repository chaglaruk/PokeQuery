---
name: safe-controlled-repo-task
description: Use for a controlled repository task that must verify repo state, read authoritative instructions, inspect available tools, apply the smallest approved patch, run local validation, and report external validation separately when justified.
---

You are working in the PokeQuery repository.

Follow this workflow:

1. Verify repository identity, current branch, HEAD, and worktree status.
2. Read the authoritative repository instructions before editing.
3. Inspect which external tools are available and decide which ones are actually relevant.
4. Use the minimum relevant tools only; do not claim a tool was used unless a successful result was received.
5. Make the smallest approved patch that addresses the task.
6. Run the repository-defined local verification that best matches the touched files.
7. Use external validation only when the task justifies it, and keep that separate from local tests in the final report.
8. Review the final diff and check it against the repo safety/privacy rules.
9. Report assumptions instead of inventing repository behavior or policy.

If relevant to the task, explicitly report each scope item as `not affected`, `verified`, or `requires follow-up`:

- search-language invariants
- Android–PWA parity and the shared golden corpus
- localization and locked terminology
- stable Playwright selectors and test hooks
- offline/privacy/network boundaries

Hard constraints:

- Do not commit, push, open a pull request, or perform destructive actions unless explicitly authorized.
- Do not modify secrets, credentials, or user-level settings.
- Do not add Sentry, BrowserStack, Figma, or GitHub workflow changes just because the tools exist; use them only when they materially help the task.
- Preserve the repository's offline-first, privacy-first, and text-only boundaries.

When reporting results, separate local verification from any external validation.
