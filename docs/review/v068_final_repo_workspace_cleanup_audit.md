# v0.6.8 final repo and workspace cleanup audit

Date: 2026-07-02

Branch: `feature/v068-release-polish-i18n-events`

Scope: final bloat cleanup before commit. No staging, commit, push, merge, reset, stash, `git clean`, broad checkout, or broad restore.

## Classification before deletion

### SAFE_DELETE_REPO_TRACKED

Tracked files/folders safe to delete because they are old visual/reference evidence, obsolete generated assets, or one-off helper output. They are not runtime resources, tests, release docs, Event Guide docs, or final v0.6.8 evidence.

- `design_targets/**`
- `docs/design/**`
- `docs/design_targets/**`
- `docs/screenshots/v051_ui_polish/**`
- `docs/screenshots/v052_language_hotfix/**`
- `docs/screenshots/v053_motion/**`
- `docs/screenshots/v054_motion_ui_fixes/**`
- `docs/screenshots/v055_audit_hardening/**`
- `docs/screenshots/v060_trust_education/**`
- `docs/screenshots/v061_workflows_surface_context/**`
- `docs/screenshots/v067_codex_recovery_final_review/**`
- `docs/visual_comparison.md` if present
- `docs/visual_gap_audit.md`
- `docs/VISUAL_ACCEPTANCE_REPORT.md`
- `docs/UI_ASSET_CONTRACT.md`
- `scripts/convert_png_to_webp.py`
- `scripts/make_onboarding_hero_wide.py`
- `scripts/make_readme_showcase.py`
- `scripts/render_v052_launcher.py`

Previously classified tracked deletions remain intentional:

- root dev/reference artifacts
- old generated asset packs
- obsolete root helper scripts
- obsolete version-specific contact-sheet scripts

### SAFE_DELETE_LOCAL_UNTRACKED

- `.agent-reference/Claude-BugHunter/` — ignored local reference/scratch folder, not tracked, not referenced by runtime/docs/tests/build except the `.gitignore` ignore rule.
- `.agent-reference/` — deleted after its child folder was removed.
- `.agents/` — empty local scratch folder.
- `.codex/` — empty local scratch folder.
- `app/build/`, `build/`, `.gradle/`, `.kotlin/` — generated local build/cache folders; deleted only after validation passed.

### KEEP_REQUIRED

- `app/src/main/**`
- `app/src/test/**`
- `docs/event-feed/**`
- `docs/release/**`
- `docs/screenshots/v068_clean_ui_repair_after_rollback/**`
- `docs/review/v068_clean_ui_repair_after_rollback_checklist.md`
- Intermediate untracked cleanup audits were consolidated into this final cleanup audit and are not required for the commit.
- `scripts/check_runtime_assets.py`
- `scripts/create_contact_sheet.py`
- `capture_screenshots.ps1`
- `capture_screenshots.sh`
- Gradle wrapper/build files.
- Sensitive local development/signing files, if untracked and ignored.
- `scripts/make_readme_banner.py` — referenced by `docs/readme/BANNER_SPEC.md`.
- `scripts/process_ui_assets.py` — retained as current asset pipeline helper unless separately retired.
- `scripts/update_kb.py` — retained as knowledgebase maintenance helper.

### NEEDS_USER_DECISION

- Small historical product docs under `docs/v0.1.*`, `docs/v0.2.*`, and `docs/v0.3.*`.
- `docs/SCREEN_VISUAL_SPEC.md`
- older base screenshots under `docs/screenshots/*.png`
- `docs/screenshots/manual/**`
- archived audit/review docs for earlier milestones.

## A. Initial repo state

- Branch: `feature/v068-release-polish-i18n-events`.
- Existing functional v0.6.8 work was present.
- Existing tracked deletions from the previous tracked bloat cleanup were present and intentional.
- Initial `git diff --check`: PASS with CRLF warnings only.
- Initial `git log --oneline --decorate -5` started at `2deda24`.

## B. `.agent-reference` / `Claude-BugHunter` decision

- `git ls-files -- .agent-reference/Claude-BugHunter`: no tracked files.
- `git ls-files --others --ignored --exclude-standard -- .agent-reference/Claude-BugHunter`: ignored local folder found.
- Reference scan found only the `.gitignore` ignore rule.
- Decision: delete as local-only scratch/reference bloat.

## C. Docs/screenshots/design archive cleanup

Deleted old image-heavy tracked archive evidence:

- `design_targets/**`
- `docs/design/**`
- `docs/design_targets/**`
- `docs/screenshots/v051_ui_polish/**`
- `docs/screenshots/v052_language_hotfix/**`
- `docs/screenshots/v053_motion/**`
- `docs/screenshots/v054_motion_ui_fixes/**`
- `docs/screenshots/v055_audit_hardening/**`
- `docs/screenshots/v060_trust_education/**`
- `docs/screenshots/v061_workflows_surface_context/**`
- `docs/screenshots/v067_codex_recovery_final_review/**`

Deleted obsolete visual docs:

- `docs/visual_gap_audit.md`
- `docs/VISUAL_ACCEPTANCE_REPORT.md`
- `docs/UI_ASSET_CONTRACT.md`

`docs/visual_comparison.md` was already absent.

Updated `docs/release/IP_ASSET_AUDIT.md` so release documentation no longer points at removed design-reference folders.

## D. Helper script cleanup

Deleted obsolete tracked helper scripts:

- `scripts/convert_png_to_webp.py`
- `scripts/make_onboarding_hero_wide.py`
- `scripts/make_readme_showcase.py`
- `scripts/render_v052_launcher.py`

Kept:

- `scripts/check_runtime_assets.py`
- `scripts/create_contact_sheet.py`
- `scripts/make_readme_banner.py`
- `scripts/process_ui_assets.py`
- `scripts/update_kb.py`

## E. Sensitive/local file audit result

No secret contents were printed.

- `.env`: untracked and ignored.
- `local.properties`: untracked and ignored.
- `keystore.properties`: untracked and ignored.
- `release-keystore.jks`: untracked and ignored.

No sensitive/signing local file is tracked.

## F. Tracked files/folders deleted

- Previous tracked bloat cleanup deletions remain.
- This pass additionally deletes old design/reference folders, old screenshot archives, obsolete visual docs, and obsolete helper scripts listed in sections C and D.

## G. Untracked local folders/files deleted

- `.agent-reference/Claude-BugHunter/`
- `.agent-reference/`
- `.agents/`
- `.codex/`

## H. Local build/cache folders deleted or intentionally kept

Deleted after validation passed:

- `app/build/`
- `build/`
- `.gradle/`
- `.kotlin/`

No Gradle command was run after deleting these folders. The next Gradle build will recreate them and regenerate APK/AAB outputs.

## I. Files still needing user decision

- `docs/v0.1.*`, `docs/v0.2.*`, `docs/v0.3.*`
- `docs/SCREEN_VISUAL_SPEC.md`
- base screenshots under `docs/screenshots/*.png`
- `docs/screenshots/manual/**`
- archived audit/review docs for earlier milestones

## J. Validation results

All validation passed before local build/cache deletion:

- `git diff --check` — PASS with CRLF warnings only.
- `.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain` — PASS.
- `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain` — PASS.
- `.\gradlew.bat :app:lintDebug --no-daemon --console=plain` — PASS.
- `.\gradlew.bat :app:bundleRelease --no-daemon --console=plain` — PASS.

Post-cache-deletion:

- `git diff --check` — PASS with CRLF warnings only.

## K. Runtime asset check result

- `python scripts\check_runtime_assets.py` — PASS.
- Found 22 allowed runtime image assets.
- Pillow deprecation warning only.

## L. Locale/test safety result

- No `MissingTranslation`, `@Ignore`, or `@Disabled` matches under `app/src/main` or `app/src/test`.
- Placeholder/mojibake scan passed for app resources/code and release docs; allowed French `ÂGE / DISTANCE` label only.
- Locale parity remains covered by `LocaleResourceCoverageTest` inside `:app:testDebugUnitTest`.
- Event Guide files remain:
  - `app/src/main/java/com/caglar/pokequery/domain/events/EventFeed.kt`
  - `app/src/main/res/raw/event_context_fixture.json`
  - `docs/event-feed/pokequery-events.json`

## M. Final screenshot evidence path

- `docs/screenshots/v068_clean_ui_repair_after_rollback/contact_sheet.png`

## N. Final git status --short

Summary:

- Existing functional v0.6.8 tracked modifications remain.
- Intentional tracked deletions now include old visual/design/screenshot/helper bloat.
- Required untracked v0.6.8 source/test/docs/screenshot evidence remains.
- No files are staged.

## O. Final git diff --stat

Latest observed before this final audit write:

```text
268 files changed, 5743 insertions(+), 3545 deletions(-)
```

## P. Final git diff --name-status --diff-filter=D

Intentional deletion groups:

- root dev/reference artifacts
- old generated visual asset packs
- old design/reference target folders
- old screenshot evidence folders through v0.6.7
- obsolete visual docs
- obsolete helper/contact-sheet scripts
- `window_dump.xml`

## Q. Final git diff --check

PASS with CRLF warnings only.

## Suggested commit message

```text
feat: finalize v0.6.8 localization, event guide, and clean UI polish
```
