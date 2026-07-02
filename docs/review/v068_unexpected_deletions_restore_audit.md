# v0.6.8 unexpected deletions restore audit

Date: 2026-07-02

Branch: `feature/v068-release-polish-i18n-events`

Scope: restore tracked deletions that were outside the approved cleanup allowlist. No staging, commit, push, merge, reset, stash, broad restore, or clean was performed.

## Baseline

- Initial deleted tracked files: 257
- Approved deletion allowlist matches: 203
- Unexpected deleted tracked files restored: 54
- Restore method: targeted `git restore -- <path>` for each restored path.

Two requested audit docs were not present in the workspace as files:

- `docs/review/v068_safe_cleanup_audit.md`
- `docs/review/v068_tracked_bloat_cleanup_audit.md`

The final workspace audit was present and confirmed historical docs/screenshots were still user-decision items, not approved deletion items.

## Approved deletion groups left untouched

- Root dev/reference artifacts:
  - `window_dump.xml`
  - `ChatGPT Image 20 Haz 2026 01_53_00 (1).png`
  - `ChatGPT Image 20 Haz 2026 01_53_00 (2).png`
  - `reference_full_mockup.png`
- Old generated asset packs:
  - `pokequery_gemini_visual_asset_pack/**`
  - `pokequery_ui_refinement_asset_pack/**`
- Obsolete root helper scripts:
  - `create_ui_handoff.py`
  - `fix_icons_transparent.py`
  - `make_comparison.py`
- Obsolete version-specific contact-sheet scripts:
  - `scripts/make_v051_contact_sheet.py`
  - `scripts/v053_contact_sheet.py`
  - `scripts/v054_contact_sheet.py`
  - `scripts/v055_contact_sheet.py`
  - `scripts/v060_contact_sheet.py`
  - `scripts/v061_contact_sheet.py`
- Old design/reference folders:
  - `design_targets/**`
  - `docs/design/**`
  - `docs/design_targets/**`
- Old screenshot evidence folders:
  - `docs/screenshots/v051_ui_polish/**`
  - `docs/screenshots/v052_language_hotfix/**`
  - `docs/screenshots/v053_motion/**`
  - `docs/screenshots/v054_motion_ui_fixes/**`
  - `docs/screenshots/v055_audit_hardening/**`
  - `docs/screenshots/v060_trust_education/**`
  - `docs/screenshots/v061_workflows_surface_context/**`
  - `docs/screenshots/v067_codex_recovery_final_review/**`
- Obsolete visual docs:
  - `docs/visual_comparison.md`
  - `docs/visual_gap_audit.md`
  - `docs/VISUAL_ACCEPTANCE_REPORT.md`
  - `docs/UI_ASSET_CONTRACT.md`
- Obsolete helper scripts:
  - `scripts/convert_png_to_webp.py`
  - `scripts/make_onboarding_hero_wide.py`
  - `scripts/make_readme_showcase.py`
  - `scripts/render_v052_launcher.py`

## Restored unexpected deleted tracked paths

- `SECURITY_AUDIT_REPORT.md`
- `docs/ASSET_NOTES.md`
- `docs/DESIGN_LOCK.md`
- `docs/ROADMAP.md`
- `docs/SCREEN_VISUAL_SPEC.md`
- `docs/ai/AI_ASSISTANT_ROADMAP.md`
- `docs/ai/AI_FEASIBILITY.md`
- `docs/audit/BUG_REPORT.md`
- `docs/audit/FEATURE_GAP_ROADMAP.md`
- `docs/audit/PLAY_COMPLIANCE_AUDIT.md`
- `docs/audit/POKEQUERY_FULL_AUDIT.md`
- `docs/audit/PRIVACY_SECURITY_AUDIT.md`
- `docs/audit/TEST_GAP_ANALYSIS.md`
- `docs/design_system.md`
- `docs/release/v0.4.1_release_audit.md`
- `docs/review/v067_cleanup_audit.md`
- `docs/review/v067_codex_recovery_final_review.md`
- `docs/screenshots/1_onboarding_step_1.png`
- `docs/screenshots/2_onboarding_step_2.png`
- `docs/screenshots/3_onboarding_step_3.png`
- `docs/screenshots/4_home.png`
- `docs/screenshots/5_safe_cleanup_detail.png`
- `docs/screenshots/6_candy_prep_detail.png`
- `docs/screenshots/7_trade_fodder_detail.png`
- `docs/screenshots/8_nundo_detail.png`
- `docs/screenshots/9_pvp_detail.png`
- `docs/screenshots/10_lucky_trade_detail.png`
- `docs/screenshots/11_popular_presets.png`
- `docs/screenshots/12_knowledge_search.png`
- `docs/screenshots/13_knowledge_expanded.png`
- `docs/screenshots/14_favorites.png`
- `docs/screenshots/15_history.png`
- `docs/screenshots/16_settings.png`
- `docs/screenshots/contact_sheet.png`
- `docs/screenshots/manual/candy_prep.png`
- `docs/screenshots/manual/home.png`
- `docs/screenshots/manual/onboarding.png`
- `docs/screenshots/manual/safe_cleanup.png`
- `docs/search_language_mapping.md`
- `docs/spec_summary.md`
- `docs/v0.1.0_internal_mvp.md`
- `docs/v0.1.1_ui_polish.md`
- `docs/v0.1.2_real_world_validation.md`
- `docs/v0.2.0_manual_validation.md`
- `docs/v0.2.0_power_user_pack.md`
- `docs/v0.3.0_ux_rebuild_plan.md`
- `docs/v0.3.1_icon_verification.md`
- `docs/v0.3.1_ux_hardening_report.md`
- `docs/v0.3.2_icon_report.md`
- `docs/v0.3.2_visual_rebuild_plan.md`
- `docs/v0.3.2_visual_rebuild_report.md`
- `docs/v0.3.3_art_pack_apply_report.md`
- `docs/v0.3.4_fix_report.md`
- `pokemon_go_arama_dizesi_app_plani 2.md`

## Post-restore verification

- Remaining deleted tracked files: 203
- Unexpected deletions remaining: 0
- Required restored docs exist:
  - `docs/review/v067_cleanup_audit.md`
  - `docs/SCREEN_VISUAL_SPEC.md`
  - `docs/ROADMAP.md`
  - `docs/spec_summary.md`
  - `docs/design_system.md`
  - `docs/search_language_mapping.md`
- Final v0.6.8 screenshot evidence exists:
  - `docs/screenshots/v068_clean_ui_repair_after_rollback/contact_sheet.png`

## Staging/commit status

- No files staged.
- No commit created.
- No push performed.
