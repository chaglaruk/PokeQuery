# Release Readiness Status

**Android published release:** v0.7.7  
**versionCode:** 27  
**Web/PWA:** v0.7.3 (independent versioning)  
**Immutable Android release source SHA:** `93bf178f942048e0fee8cbb47976b23f86ecde3f`  
**Release tag:** `v0.7.7`  
**Google Play publication date:** 2026-10-01  
**Previous published Android release:** v0.7.6 / code 26  

> Release identity is anchored to the exact immutable source SHA above, not moving `master`. The scheduled Event Guide workflow may create feed-only commits after the binary release.

## Published scope

v0.7.7 is the published Android production update for the growth and safety work originally merged through PR #32 and finalized by PR #33.

Release-facing areas include:
- Android Goal Detail **Share Search** through the system share chooser;
- Medium/High-risk Share actions preserve the Risk Warning gate before the chooser opens;
- Search Assistant copy risk parity: canonical Info/Low output may copy directly, while Medium/High output must be reviewed before the clipboard changes;
- localized Search Assistant output continues to follow the selected Search String Language after confirmation;
- local-only Play Store rating prompt with no analytics, telemetry, attribution or review SDK;
- EN/TR/DE/ES/FR/IT Share/rating/risk copy and rating-dialog locale fallback fix;
- Android share-chooser launch fix for localized configuration context;
- v0.7.7 Changelog current-entry copy localized for EN/TR/DE/ES/FR/IT and bound to dedicated `what_changed_v077_*` resources.

Web/PWA remains independently versioned at **v0.7.3**. The Android publication does not imply a Web/PWA release.

## Source and cloud validation

PR #33 was merged as exact source commit:

`93bf178f942048e0fee8cbb47976b23f86ecde3f`

The release branch was originally created from exact post-growth master SHA:

`fb00bcdbeee1d8694684835375af89a6aece0221`

Final Android CI on the release branch passed:
- cross-platform golden-corpus identity;
- Android bundled Event Guide fallback freshness;
- Android unit tests;
- Android lint;
- debug APK assembly;
- generator safety invariants;
- generator/enrichment/fallback validators;
- event-feed validation;
- runtime-asset validation.

The v0.7.7 Changelog was physically checked in EN/TR/DE/ES/FR/IT before merge for content, wrapping, clipping, overlap, horizontal overflow, scrolling and navigation obstruction.

## Local signing and artifact verification

The exact release source `93bf178f942048e0fee8cbb47976b23f86ecde3f` was checked out in an isolated clean worktree and independently validated before upload.

Identity verified from the source and built bundle:
- package: `com.caglar.pokequery`;
- versionName: `0.7.7`;
- versionCode: `27`.

Required Gradle gates passed:
- `:app:testDebugUnitTest`;
- `:app:lintDebug`;
- `:app:assembleDebug`;
- `:app:bundleRelease`.

The signed AAB was verified with `jarsigner` and official bundletool 1.18.3.

Verified Play upload certificate SHA-1:

`28:7D:20:73:0E:F2:02:C1:49:FB:80:67:A1:42:9B:50:1D:1D:01:78`

Verified release AAB:
- file name: `PokeQuery-v0.7.7-code27.aab`;
- size: **4,966,207 bytes**;
- SHA-256: `a4f54fcfb699e1048f313bb6346ba85ce3d0ac45f7b84bbd7f73e7d1afc33e56`;
- bundletool manifest: package `com.caglar.pokequery`, versionCode `27`, versionName `0.7.7`;
- delivery copy was byte-identical to the Gradle output artifact.

No keystore, password, signing property, build output or other secret material was committed.

## Google Play publication

The exact verified `PokeQuery-v0.7.7-code27.aab` was accepted by Google Play for Production with:
- versionCode 27;
- versionName 0.7.7;
- target SDK 36;
- six of six localized release-note languages;
- 100% production rollout.

The upload-key reset completed before this release. Google Play accepted the replacement upload certificate matching the SHA-1 recorded above.

Publication was confirmed on **2026-10-01**.

## Post-publication physical smoke

The installed Play Store build reported **0.7.7**, while the Play Store offered **Open / Uninstall** rather than Update.

Post-publication smoke passed on the installed Play build:
- existing state/settings remained intact;
- low-risk Share Search opened the Android share flow correctly;
- Medium-risk Share preserved the Risk Warning gate;
- cancelling the warning did not open the share chooser;
- Search Assistant `hundo` produced `4*` directly;
- `shiny` preserved the review/risk gate;
- Event Guide opened normally;
- no blocking crash, locale regression or layout issue was observed.

**Production smoke: PASS.**

## Release closure checklist

- [x] Release PR merged.
- [x] Exact immutable source SHA recorded.
- [x] Android CI passed.
- [x] Six-locale physical Changelog visual gate passed.
- [x] Exact source rebuilt locally.
- [x] Unit tests, lint, debug assembly and release bundle passed.
- [x] Signed AAB certificate verified.
- [x] bundletool package/version metadata verified.
- [x] Artifact size and SHA-256 recorded.
- [x] Delivery copy matched the Gradle artifact byte-for-byte.
- [x] Google Play accepted versionCode 27 and the reset upload certificate.
- [x] Production publication confirmed.
- [x] Play Store physical smoke passed.
- [x] Immutable `v0.7.7` tag points to exact release source SHA.
- [x] Public release-facing version references refreshed.

## Ongoing rule

Do not use moving `master` as evidence of a shipped Android binary. Event Guide automation can legitimately advance `master` after release without changing the installed Android package. Future release claims must continue to use the exact immutable release tag/source SHA and the verified release artifact.
