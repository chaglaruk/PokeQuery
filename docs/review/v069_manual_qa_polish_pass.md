# v0.6.9 Manual QA Polish Pass

## Scope

This pass continues on `test/zero-trial` and focuses on manual QA polish only:

- portrait-only runtime behavior;
- onboarding copy/layout polish;
- Goal Detail explanation order and clearer ELI5 text;
- Search Assistant copy polish;
- Event Guide date-truthful status, richer local planning fields, and deterministic debug screenshots;
- Settings simplification for Event Guide controls;
- final validation, phone install, screenshots, and contact sheet.

No commit, staging, push, merge, reset, stash, or broad clean is part of this pass.

## Status tracker

| Item | Status | Evidence |
| --- | --- | --- |
| Baseline | Passed | Branch verified as `test/zero-trial`; no staged files. |
| Portrait lock | Passed | `MainActivity` requests portrait orientation; final phone screenshots are portrait. |
| Onboarding polish | Passed | Top skip removed, CTA contrast improved, feature cards made readable in EN/TR screenshots. |
| Detail/risk ELI5 layout | Passed | “What does this do?” appears before warnings; Turkish detail screenshot captured. |
| Search Assistant polish | Passed | Description copy localized across EN/TR/DE/ES/FR/IT; EN/TR screenshots captured. |
| Event Guide date-truthful status | Passed | Date window drives Current/Upcoming/Ended status; July 11–12 GO Fest shows Coming up on July 2. |
| Event Guide UX/content | Passed | GO Fest planning card includes boosted spawns, bonuses, raids, research, search, source, and last checked. |
| Settings simplification | Passed | Event Guide settings collapsed to one public updates control in EN/TR screenshots. |
| Commit hygiene | Passed | No staging, commit, push, merge, reset, stash, or broad clean performed. |
| Static validation | Passed | `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:lintDebug`, `:app:bundleRelease`, and `python scripts\check_runtime_assets.py` passed. |
| Real phone screenshots | Passed | Installed debug APK on `192.168.1.126:5555`; captured final screenshot set and contact sheet. |
| Final report | Ready | Final response includes exact evidence and remaining limitations. |

## Screenshot set

Expected final screenshots:

- `onboarding_step_1_en.png`
- `onboarding_step_2_en.png`
- `onboarding_step_1_tr.png`
- `onboarding_step_2_tr.png`
- `home_tr.png`
- `detail_or_risk_tr.png`
- `search_assistant_en.png`
- `search_assistant_tr.png`
- `event_guide_en_before_refresh.png`
- `event_guide_en_after_refresh.png`
- `event_guide_tr_before_refresh.png`
- `event_guide_tr_after_refresh.png`
- `event_guide_scrolled_or_expanded_en.png`
- `event_guide_scrolled_or_expanded_tr.png`
- `settings_en.png`
- `settings_tr.png`
- `contact_sheet.png`

## Notes

- Debug builds use the bundled event fixture for deterministic QA screenshots.
- Release builds keep the public Event Guide provider behavior.
- Event Guide content remains informational planning guidance. Users must verify the Today view and manually review results before acting.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk` (25,996,265 bytes).
- Release AAB: `app/build/outputs/bundle/release/app-release.aab` (4,779,453 bytes).
- Contact sheet: `docs/screenshots/v069_final_user_qa_polish/contact_sheet.png` (4,161,766 bytes).
