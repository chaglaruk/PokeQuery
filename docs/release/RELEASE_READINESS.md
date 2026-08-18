# Release Readiness Status

**Android release candidate:** 0.7.6  
**versionCode:** 26  
**Web/PWA:** 0.7.3 (independent versioning)  
**Release branch base:** `a4f45be9ebdfba19006ef86e3bf6ab69db86dc2e`  
**Previous published Android release:** v0.7.5 / code 25  
**Immutable v0.7.5 release source:** `b19c3b150468318a71da6c4763266cf4aba10cdd`

## Candidate scope

v0.7.6 is the Android Closed Testing candidate for the post-v0.7.5 correctness and hardening work already reviewed on master.

Included release-facing areas:
- Search Assistant lexical-boundary, negation, alias, caught-date and pipe-input correctness;
- audited finite official search-filter coverage in Expert Builder while preserving PokeQuery's stricter no-pipe generated-output policy;
- live System Default app-language handling and explicit override -> System Default restore;
- Event Guide date/status/category/runtime validation and source-page enrichment quality/security hardening;
- repeated widget/app-shortcut routing hardening;
- Android/Web parity and regression coverage for shared search behavior.

Web/PWA remains independently versioned at 0.7.3. The Android version bump does not itself create a new Web/PWA release.

## Completed pre-release gates

- ✅ Published v0.7.5 / code 25 remains anchored to immutable source SHA `b19c3b150468318a71da6c4763266cf4aba10cdd`.
- ✅ Post-release bug-hunt PR #26 merged after Android/Python validation and physical-device regression testing.
- ✅ Material CodeRabbit findings from the bug-hunt review were independently verified and fixed; final bot re-review was rate-limited and is not claimed as a PASS.
- ✅ Samsung Galaxy S25 (`SM-S931B`) / Android 16 / SDK 36 / One UI 8.5 passed the final Android runtime-affecting device gate for System Default locale lifecycle, Expert Builder localization, Search Assistant regressions, raw pipe safety, Event Guide, repeated routes and logcat.
- ✅ The only post-device functional commits before merge were build-time Python Event Guide enrichment hardening/tests; subsequent PR #27 changed workflows only and PR #29 changed documentation only.
- ✅ PR #27 separated Playwright from the routine PWA gate; the current fast gate is install + golden corpus + fallback freshness + typecheck + lint + unit + production build.
- ✅ PR #29 refreshed repository truth, validation and Event Guide source-policy documentation.
- ✅ Release branch `release/v0.7.6` was created from exact master `a4f45be9ebdfba19006ef86e3bf6ab69db86dc2e` and Android metadata is being bumped to `0.7.6` / `versionCode 26`.

## Required before merge / release-source freeze

- [ ] Release-branch Android CI passes completely on the version-bumped candidate.
- [ ] Verify release-branch diff contains only intentional version/changelog/readiness changes.
- [ ] Resolve any real review/CI findings before merge.
- [ ] Squash-merge the release PR only after the release-branch gate is green.
- [ ] Record the exact resulting master merge SHA as the immutable v0.7.6 release source candidate.

## Required after merge — local signing/artifact gate

These steps require the local Windows/Android signing environment and must be performed from the exact final release-source SHA, not a moving master assumption.

- [ ] Checkout/fetch the exact final v0.7.6 release-source SHA in the local PokeQuery repository.
- [ ] Confirm clean worktree and exact `versionName=0.7.6`, `versionCode=26`, package `com.caglar.pokequery`.
- [ ] Run full relevant local Android validation, including `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, and release-relevant `:app:bundleRelease`.
- [ ] Build the signed AAB using the existing configured Play upload keystore.
- [ ] Verify AAB signature/certificate with `jarsigner`.
- [ ] Validate the AAB with the official bundletool and inspect package/version metadata directly from the bundle.
- [ ] Copy the exact verified artifact to the delivery filename `PokeQuery-v0.7.6-code26.aab` and record size + SHA-256.
- [ ] Confirm the delivery-copy hash exactly matches the Gradle output artifact hash.
- [ ] Perform only the necessary final physical-device smoke if the release-source delta after the previous device gate is device-sensitive; a pure version/changelog change does not by itself require repeating the full device matrix.

## Publication gate

- [ ] Upload the exact verified `PokeQuery-v0.7.6-code26.aab` to Google Play Closed Testing.
- [ ] Confirm Google Play accepts versionCode 26 and the configured upload certificate.
- [ ] Create/push annotated `v0.7.6` only at the exact verified release-source SHA; never move/retarget `v0.7.5` or older tags.
- [ ] Update this document from candidate state to published state only after the AAB is accepted/published.
- [ ] Update public release-facing version references (for example the README badge) only when the new Android build is actually published; until then v0.7.5 remains the accurate shipped version.

## Playwright policy for this Android release

The manual `PWA Playwright E2E` workflow is not a routine blocker for this Android-only version bump. Run it only if a targeted Web routing/offline risk is introduced or the release scope explicitly expands to require a Web/PWA E2E gate.

## Known deferred issue

Samsung One UI previously did not list the optional PokeQuery home-screen widgets in the launcher widget picker. Widget discovery remains a known deferred optional issue; repeated widget/app-shortcut routing itself was validated where accessible and does not block the core Android Closed Testing candidate.

## Current blocker

The release candidate is not yet releasable until the version-bumped release-branch CI is terminal green, the release PR is merged, and the exact merged source is used for signed AAB generation/verification.
