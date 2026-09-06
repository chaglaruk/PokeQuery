# Release Readiness Status

**Android release candidate:** 0.7.7  
**versionCode:** 27  
**Web/PWA:** 0.7.3 (independent versioning)  
**Release branch:** `release/v0.7.7`  
**Release branch base:** `fb00bcdbeee1d8694684835375af89a6aece0221`  
**Previous published Android release:** v0.7.6 / code 26  

> Historical note: the repository does not currently provide a verified immutable `v0.7.6` tag/release source record. Do not infer one from moving `master` or from the version-bump commit alone. v0.7.7 must establish its own exact release-source SHA after the release PR is merged and the signed AAB is verified.

## Candidate scope

v0.7.7 is the Android production-update candidate for the user-facing growth/safety work merged in PR #32.

Included release-facing areas:
- Android Goal Detail **Share Search** via the system share chooser;
- Medium/High-risk Share actions preserve the Risk Warning gate before the chooser opens;
- Search Assistant copy risk parity: canonical Info/Low output may copy directly, while Medium/High output must be reviewed before the clipboard changes;
- localized Search Assistant output still follows the selected Search String Language after confirmation;
- local-only Play Store rating prompt with no analytics, telemetry, attribution or review SDK;
- EN/TR/DE/ES/FR/IT Share/rating/risk copy and the rating-dialog locale fallback fix;
- Android share-chooser launch fix for the localized configuration context;
- v0.7.7 Changelog current-entry copy localized for EN/TR/DE/ES/FR/IT and bound to dedicated `what_changed_v077_*` resources after PR review caught the stale v0.6.6 binding;
- the current master Event Guide fallback/assets bundled at the release branch base.

Web/PWA remains independently versioned at 0.7.3. The Android version bump does not itself create a new Web/PWA release.

## Completed pre-release gates inherited from the exact release base

- ✅ Growth foundation PR #32 was squash-merged to exact master SHA `fb00bcdbeee1d8694684835375af89a6aece0221`.
- ✅ PR #32 final Android CI passed unit tests, lint, `assembleDebug`, golden-corpus identity, Event Guide fallback freshness, generator/enrichment/fallback validation, event-feed validation and runtime-asset validation.
- ✅ PR #32 final PWA CI passed typecheck, lint, unit tests, production build, Chromium/WebKit Playwright E2E and mobile visual QA.
- ✅ Physical Android validation covered low/Info Share, Medium-risk Share gating, Search Assistant `hundo -> 4*`, Medium-risk `shiny` clipboard protection, German localized `schillernd` copy after canonical Medium review, and EN/TR/DE/ES/FR/IT changed growth surfaces.
- ✅ The first physical pass found and fixed the Android chooser launch defect; the follow-up pass confirmed the real system chooser opens after the risk gate.
- ✅ The follow-up locale pass found and fixed the rating-dialog English fallback; the committed final file content matched the device-tested patched worktree.
- ✅ CodeRabbit actionable findings on PR #32 were resolved before merge.
- ✅ `release/v0.7.7` was created from exact master `fb00bcdbeee1d8694684835375af89a6aece0221` before any subsequent moving-master Event Guide bot commit could be mistaken for the release base.

## Intentional release-branch delta

The release branch contains the release/version metadata set plus the narrow PR-review hardening required to make the v0.7.7 Changelog screen truthful. Relative to exact base `fb00bcd...`, the intentional delta is these 14 files:
- `app/build.gradle.kts`
- `CHANGELOG.md`
- `app/src/main/java/com/caglar/pokequery/domain/changelog/Changelog.kt`
- `app/src/main/java/com/caglar/pokequery/ui/screens/MiscScreens.kt`
- `app/src/main/res/values/changelog_v077.xml`
- `app/src/main/res/values-tr/changelog_v077.xml`
- `app/src/main/res/values-de/changelog_v077.xml`
- `app/src/main/res/values-es/changelog_v077.xml`
- `app/src/main/res/values-fr/changelog_v077.xml`
- `app/src/main/res/values-it/changelog_v077.xml`
- `app/src/test/java/com/caglar/pokequery/AppVersionTest.kt`
- `app/src/test/java/com/caglar/pokequery/data/model/PersonalPresetTest.kt`
- `app/src/test/java/com/caglar/pokequery/privacy/BuildConfigRegressionTest.kt`
- `docs/release/RELEASE_READINESS.md`

The UI implementation delta is limited to rebinding the current Changelog row from stale `what_changed_v066_*` resources to the dedicated localized `what_changed_v077_*` resources. No search-engine behavior, Event Guide feed data, signing configuration, keystore material, tag or release artifact is changed by the release-prep branch.

## Required before merge / release-source freeze

- [ ] Release-branch Android CI passes completely on **0.7.7 / code 27** after the review-hardening commits.
- [x] Confirm release-branch diff is the intentional 14-file version/changelog/readiness + review-hardening set above.
- [ ] Confirm the new v0.7.7 Changelog current-entry copy is readable without clipping/overlap in EN/TR/DE/ES/FR/IT on the physical Android validation device.
- [ ] Resolve the two verified PR #33 review findings after their fixes are validated.
- [ ] Confirm `versionName=0.7.7`, `versionCode=27`, package `com.caglar.pokequery` from the release branch itself.
- [ ] Squash-merge the release PR only after the release-branch gate is terminal green.
- [ ] Record the exact resulting master merge SHA as the v0.7.7 release-source candidate.

## Required after merge — local signing/artifact gate

These steps require the local Windows/Android signing environment and must be performed from the **exact merged v0.7.7 release-source SHA**, not from a moving `master` assumption.

- [ ] Fetch/checkout the exact merged release-source SHA in the local PokeQuery repository or an isolated clean worktree.
- [ ] Confirm clean worktree and exact `versionName=0.7.7`, `versionCode=27`, package `com.caglar.pokequery`.
- [ ] Run `:app:testDebugUnitTest`.
- [ ] Run `:app:lintDebug`.
- [ ] Run `:app:assembleDebug`.
- [ ] Run release-relevant `:app:bundleRelease` using the existing configured Play upload keystore.
- [ ] Verify the signed AAB with `jarsigner` and record the upload-certificate fingerprint.
- [ ] Validate the AAB with the official bundletool and inspect package/version metadata directly from the bundle.
- [ ] Copy the verified artifact to `PokeQuery-v0.7.7-code27.aab` and record file size + SHA-256.
- [ ] Confirm the delivery-copy SHA-256 exactly matches the Gradle output artifact.
- [ ] Perform a final physical-device smoke only if the exact release-source delta after the already-tested growth base becomes device-sensitive beyond the Changelog copy check above.

## Publication gate

- [ ] Upload the exact verified `PokeQuery-v0.7.7-code27.aab` to the intended Google Play production update track only after explicit publication approval.
- [ ] Confirm Google Play accepts versionCode 27 and the configured upload certificate.
- [ ] Do not assume rollout/publication from upload alone; verify the Play Console release state explicitly.
- [ ] Create/push annotated `v0.7.7` only at the exact verified release-source SHA after artifact verification; never retarget older tags.
- [ ] Update this document from candidate state to published state only after Google Play acceptance/publication is confirmed.
- [ ] Update public release-facing version references such as README only after v0.7.7 is actually published; until then v0.7.6 remains the shipped Android version.

## Playwright policy for this Android release

PR #32 already passed full PWA Playwright and visual QA before the Android-only version bump. The v0.7.7 release branch changes no Web/PWA source, so another PWA E2E run is not a release blocker.

## Current blocker

The candidate is not yet releasable. The immediate gates are terminal-green Android CI on the final review-hardened release head, six-locale physical Changelog copy/overflow validation, resolution of the verified PR #33 review threads, and confirmation of the release metadata. Merge, signing, AAB generation, tagging and Google Play publication remain separate gated operations.
