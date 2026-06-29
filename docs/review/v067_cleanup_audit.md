# v067 Cleanup Audit

Repo: `C:\Users\Caglar\Desktop\PokeQuery`
Branch: `feature/v062-polish-branding-settings`

Scope: cleanup-only review after the v067 recovery pass. No behavior changes, commit, push, merge, reset, rebase, stash, or broad clean.

## Phase 0 Inventory

- `git branch --show-current`: `feature/v062-polish-branding-settings`
- `git diff --stat` before cleanup: `210 files changed, 1131 insertions(+), 1277 deletions(-)`
- `git diff --check` before cleanup: PASS with line-ending warnings only.
- `git ls-files --others --exclude-standard`: untracked app resources, v067 screenshots/report, old review/screenshot artifacts, XML UI dumps, and `scratch/` scripts.

## Phase 1 Categorization

### A. KEEP - App/source/test/resource changes needed for v0.6.2/v067

These are app behavior, localization, offline-only, release-lint, tests, or accepted asset/tooling changes:

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/caglar/pokequery/MainActivity.kt`
- `app/src/main/java/com/caglar/pokequery/Navigation.kt`
- `app/src/main/java/com/caglar/pokequery/NavigationKeys.kt`
- `app/src/main/java/com/caglar/pokequery/data/repository/UserPreferencesRepository.kt`
- `app/src/main/java/com/caglar/pokequery/domain/assist/SearchIntentParser.kt`
- `app/src/main/java/com/caglar/pokequery/domain/changelog/Changelog.kt`
- `app/src/main/java/com/caglar/pokequery/domain/events/EventContext.kt`
- `app/src/main/java/com/caglar/pokequery/domain/events/EventFeedClient.kt` deleted intentionally with offline-only event context.
- `app/src/main/java/com/caglar/pokequery/domain/events/MonthlyContext.kt`
- `app/src/main/java/com/caglar/pokequery/domain/locale/AppLocaleController.kt`
- `app/src/main/java/com/caglar/pokequery/domain/risk/RiskExplanation.kt`
- `app/src/main/java/com/caglar/pokequery/onboarding/OnboardingContent.kt`
- `app/src/main/java/com/caglar/pokequery/ui/components/BottomNavBar.kt`
- `app/src/main/java/com/caglar/pokequery/ui/pq/PqComponents.kt`
- `app/src/main/java/com/caglar/pokequery/ui/screens/EventContextScreen.kt`
- `app/src/main/java/com/caglar/pokequery/ui/screens/GoalDetailScreen.kt`
- `app/src/main/java/com/caglar/pokequery/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/caglar/pokequery/ui/screens/MiscScreens.kt`
- `app/src/main/java/com/caglar/pokequery/ui/screens/OnboardingScreen.kt`
- `app/src/main/java/com/caglar/pokequery/ui/screens/RiskWarningScreen.kt`
- `app/src/main/java/com/caglar/pokequery/ui/screens/SearchAssistantScreen.kt`
- `app/src/main/res/drawable-nodpi/pokequery_wordmark.png`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-tr/strings.xml`
- `app/src/main/res/values-de/strings.xml`
- `app/src/main/res/values-es/strings.xml`
- `app/src/main/res/values-fr/strings.xml`
- `app/src/main/res/values-it/strings.xml`
- `app/src/test/java/com/caglar/pokequery/domain/assist/SearchIntentParserTest.kt`
- `app/src/test/java/com/caglar/pokequery/domain/events/EventContextTest.kt`
- `app/src/test/java/com/caglar/pokequery/domain/locale/LocalizationModelTest.kt`
- `app/src/test/java/com/caglar/pokequery/domain/risk/RiskExplanationsTest.kt`
- `app/src/test/java/com/caglar/pokequery/onboarding/OnboardingContentTest.kt`
- `app/src/test/java/com/caglar/pokequery/privacy/ManifestPrivacyRegressionTest.kt`
- `scripts/check_runtime_assets.py`

### B. KEEP - Final QA evidence/reports that should remain

- `docs/review/v067_codex_recovery_final_review.md`
- `docs/review/v067_cleanup_audit.md`
- `docs/screenshots/v067_codex_recovery_final_review/contact_sheet.png`
- `docs/screenshots/v067_codex_recovery_final_review/event_context_tr_offline_only.png`
- `docs/screenshots/v067_codex_recovery_final_review/goal_detail_safe_cleanup_tr.png`
- `docs/screenshots/v067_codex_recovery_final_review/home_more_tools_after_back_tr.png`
- `docs/screenshots/v067_codex_recovery_final_review/home_more_tools_expanded_tr.png`
- `docs/screenshots/v067_codex_recovery_final_review/home_tr_after_restart.png`
- `docs/screenshots/v067_codex_recovery_final_review/onboarding_tr.png`
- `docs/screenshots/v067_codex_recovery_final_review/search_assistant_tr_input_result.png`
- `docs/screenshots/v067_codex_recovery_final_review/settings_language_tr.png`
- `docs/screenshots/v067_codex_recovery_final_review/what_changed_tr_offline_only.png`

### C. REMOVE/RESTORE CANDIDATE - Generated scratch/temp files

These are local UI dumps, phase captures, or one-off scratch automation. They are safe cleanup candidates after this audit is written:

- `docs/_phase1_check.png`
- `docs/_phase1_ui.xml`
- `docs/_phase2_after_restart.xml`
- `docs/_phase2_after_skip.xml`
- `docs/_phase2_after_turkish.xml`
- `docs/_phase2_settings.xml`
- `docs/_phase3_back_to_home.xml`
- `docs/_phase3_event_context_tr.xml`
- `docs/_phase3_goal_detail.xml`
- `docs/_phase3_home_for_events.xml`
- `docs/_phase3_home_fresh.xml`
- `docs/_phase3_home_goal.xml`
- `docs/_phase3_home_reset.xml`
- `docs/_phase3_home_sa.xml`
- `docs/_phase3_home_scrolled.xml`
- `docs/_phase3_home_top.xml`
- `docs/_phase3_home_tr.xml`
- `docs/_phase3_more_tools_expanded.xml`
- `docs/_phase3_more_tools_expanded2.xml`
- `docs/_phase3_more_tools_expanded3.xml`
- `docs/_phase3_nundo.xml`
- `docs/_phase3_onboarding_tr.xml`
- `docs/_phase3_onboarding_tr_p2.xml`
- `docs/_phase3_sa_typed.xml`
- `docs/_phase3_sa_typed2.xml`
- `docs/_phase3_search_assistant.xml`
- `docs/ui_current.xml`
- `docs/ui_dump_home.xml`
- `docs/ui_dump_home_full.xml`
- `docs/ui_dump_home_scrolled.xml`
- `docs/ui_dump_more_tools.xml`
- `docs/ui_dump_more_tools2.xml`
- `docs/ui_dump_onboarding.xml`
- `docs/ui_dump_settings.xml`
- `docs/ui_dump_settings2.xml`
- `scratch/add_strings.py`
- `scratch/make_contact_sheet.py`
- `scratch/take_screenshots.py`
- `scratch/update_risk_explainer.py`
- `scratch/update_tr_strings_v5.py`

### D. RESTORE CANDIDATE - Accidental tracked screenshot deletions or unrelated docs

All deleted tracked files are old QA screenshot/report evidence under `docs/screenshots/**`. Repo docs and scripts still reference historical screenshot locations such as `docs/screenshots/contact_sheet.png`, `docs/screenshots/v051_ui_polish`, `docs/screenshots/v052_language_hotfix`, `docs/screenshots/v053_motion`, `docs/screenshots/v054_motion_ui_fixes`, `docs/screenshots/v055_audit_hardening`, `docs/screenshots/v060_trust_education`, `docs/screenshots/v061_workflows_surface_context`, and `docs/screenshots/v062_fix_pass`.

Recommendation: restore the whole targeted `docs/screenshots` tracked deletion set with `git checkout -- docs/screenshots`. This is not app behavior and removes accidental deletion churn.

Deleted tracked groups:

- `docs/screenshots/*.png`
- `docs/screenshots/manual/*.png`
- `docs/screenshots/v051_ui_polish/*`
- `docs/screenshots/v052_language_hotfix/*`
- `docs/screenshots/v053_motion/*`
- `docs/screenshots/v054_motion_ui_fixes/*`
- `docs/screenshots/v055_audit_hardening/*`
- `docs/screenshots/v060_trust_education/*`
- `docs/screenshots/v061_workflows_surface_context/**`
- `docs/screenshots/v062_fix_pass/*`
- `docs/screenshots/v062_home_online_ai_i18n/QA_REPORT.md`
- `docs/screenshots/v062_polish_branding_settings/zcoder_audit_device_fresh/*`
- `docs/screenshots/visual_comparison.md`

### E. REVIEW MANUALLY - Uncertain files

These are untracked older review/screenshot artifacts. They may be useful history, but they are not required by the v067 final evidence set, so this cleanup pass leaves them untouched:

- `docs/review/v062_antigravity_final_review.md`
- `docs/review/v062_zcoder_final_review.md`
- `docs/review/v063_exact_logo_i18n_event_fix.md`
- `docs/review/v063_final_review.md`
- `docs/review/v063_repair_exact_logo_real_i18n.md`
- `docs/review/v064_final_validation_report.md`
- `docs/review/v064_product_language_onboarding_fix.md`
- `docs/screenshots/v064_product_language_onboarding_fix/**`
- `docs/screenshots/v066_turkish_completion_review/**`

## Phase 2 Plan

- Remove only files listed in category C.
- Do not remove source, tests, resources, accepted logo, v067 screenshots, v067 final report, or this audit.

## Phase 3 Plan

- Restore only the targeted tracked deletion group `docs/screenshots`.
- Do not use broad `git checkout .`.

## Phase 2 Results

Removed only category C files:

- `docs/_phase1_check.png`
- `docs/_phase1_ui.xml`
- `docs/_phase2_after_restart.xml`
- `docs/_phase2_after_skip.xml`
- `docs/_phase2_after_turkish.xml`
- `docs/_phase2_settings.xml`
- `docs/_phase3_back_to_home.xml`
- `docs/_phase3_event_context_tr.xml`
- `docs/_phase3_goal_detail.xml`
- `docs/_phase3_home_for_events.xml`
- `docs/_phase3_home_fresh.xml`
- `docs/_phase3_home_goal.xml`
- `docs/_phase3_home_reset.xml`
- `docs/_phase3_home_sa.xml`
- `docs/_phase3_home_scrolled.xml`
- `docs/_phase3_home_top.xml`
- `docs/_phase3_home_tr.xml`
- `docs/_phase3_more_tools_expanded.xml`
- `docs/_phase3_more_tools_expanded2.xml`
- `docs/_phase3_more_tools_expanded3.xml`
- `docs/_phase3_nundo.xml`
- `docs/_phase3_onboarding_tr.xml`
- `docs/_phase3_onboarding_tr_p2.xml`
- `docs/_phase3_sa_typed.xml`
- `docs/_phase3_sa_typed2.xml`
- `docs/_phase3_search_assistant.xml`
- `docs/ui_current.xml`
- `docs/ui_dump_home.xml`
- `docs/ui_dump_home_full.xml`
- `docs/ui_dump_home_scrolled.xml`
- `docs/ui_dump_more_tools.xml`
- `docs/ui_dump_more_tools2.xml`
- `docs/ui_dump_onboarding.xml`
- `docs/ui_dump_settings.xml`
- `docs/ui_dump_settings2.xml`
- `scratch/add_strings.py`
- `scratch/make_contact_sheet.py`
- `scratch/take_screenshots.py`
- `scratch/update_risk_explainer.py`
- `scratch/update_tr_strings_v5.py`
- `scratch/`

## Phase 3 Results

Restored:

- `docs/screenshots/**` tracked deletion group via targeted `git checkout -- docs/screenshots`.

Result:

- `git diff --name-status -- docs/screenshots`: no tracked screenshot deletions remain.
- Tracked screenshot deletion churn gone: YES.

## Phase 4 Validation

- `.\gradlew.bat testDebugUnitTest assembleDebug bundleRelease --no-daemon --console=plain`: PASS
- `python scripts\check_runtime_assets.py`: PASS
- `git diff --check`: PASS with line-ending warnings only

## Final State

Final `git diff --stat`:

```text
32 files changed, 1131 insertions(+), 902 deletions(-)
```

Files still intentionally changed:

- Category A app/source/test/resource/tooling files listed above.
- `app/src/main/java/com/caglar/pokequery/domain/events/EventFeedClient.kt` remains intentionally deleted.

Files still untracked and why:

- `app/src/main/res/drawable-nodpi/pokequery_wordmark.png`: KEEP, accepted logo asset.
- `app/src/main/res/values-de/strings.xml`: KEEP, locale resource.
- `app/src/main/res/values-es/strings.xml`: KEEP, locale resource.
- `app/src/main/res/values-fr/strings.xml`: KEEP, locale resource.
- `app/src/main/res/values-it/strings.xml`: KEEP, locale resource.
- `docs/review/v067_cleanup_audit.md`: KEEP, this cleanup report.
- `docs/review/v067_codex_recovery_final_review.md`: KEEP, final v067 validation report.
- `docs/screenshots/v067_codex_recovery_final_review/**`: KEEP, final v067 QA evidence.
- `docs/review/v062_antigravity_final_review.md`: REVIEW MANUALLY, older untracked report.
- `docs/review/v062_zcoder_final_review.md`: REVIEW MANUALLY, older untracked report.
- `docs/review/v063_exact_logo_i18n_event_fix.md`: REVIEW MANUALLY, older untracked report.
- `docs/review/v063_final_review.md`: REVIEW MANUALLY, older untracked report.
- `docs/review/v063_repair_exact_logo_real_i18n.md`: REVIEW MANUALLY, older untracked report.
- `docs/review/v064_final_validation_report.md`: REVIEW MANUALLY, older untracked report.
- `docs/review/v064_product_language_onboarding_fix.md`: REVIEW MANUALLY, older untracked report.
- `docs/screenshots/v064_product_language_onboarding_fix/**`: REVIEW MANUALLY, older untracked QA evidence.
- `docs/screenshots/v066_turkish_completion_review/**`: REVIEW MANUALLY, older untracked QA evidence.

Commit-ready: NO. The app change set is validated and screenshot deletion churn is resolved, but older untracked review/QA artifacts still need a human include/remove decision.

Merge-ready: NO. This is still an uncommitted branch with manual-review untracked artifacts.

No commit, push, or merge performed.
