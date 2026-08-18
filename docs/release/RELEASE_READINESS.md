# Release Readiness Status

**Android release candidate:** 0.7.5
**versionCode:** 25
**Web/PWA:** 0.7.3 (independent versioning)

## Completed gates

- ✅ Independent pre-AAB code audits completed.
- ✅ Android unit/lint/generator/fallback/event-feed/runtime-asset validation passed on the audited source tree.
- ✅ Web typecheck/lint/unit/build/Playwright validation passed on the audited source tree.
- ✅ Samsung Galaxy S25 / Android 16 physical validation passed for core Search Assistant, localized output, Event Guide, Privacy, caught-date composition and tester-feedback launch.
- ✅ PR #21 remediation merged to `master`; the merge tree is byte-identical to the CI-passed PR head.
- ✅ Android release metadata bumped to `0.7.5` / `versionCode 25` on `release/v0.7.5`.

## Required before Closed Testing upload

- [ ] Release-branch Android CI passes with the version bump.
- [ ] Build the release AAB from the final release commit using the existing Play upload keystore.
- [ ] Verify AAB signature/certificate with `jarsigner`.
- [ ] Validate/install the bundle on the physical device with `bundletool` where feasible.
- [ ] Confirm final app version/package on the installed build.
- [ ] Upload the exact verified AAB to Google Play Closed Testing.
- [ ] Create annotated `v0.7.5` only at the final release merge commit; never move `v0.7.4`.

## Known deferred issue

- Samsung One UI did not list the optional PokeQuery home-screen widgets in the launcher widget picker during pre-AAB physical validation. Widget discovery/tap validation is intentionally deferred; core app flows are unaffected.

## Current blocker

The application code has no known release-blocking defect. The remaining gates are release-build/signing/device/upload verification listed above; do not claim the AAB is generated or uploaded until those steps are completed.
