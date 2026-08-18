# Release Readiness Status

**Android release:** 0.7.5  
**versionCode:** 25  
**Web/PWA:** 0.7.3 (independent versioning)  
**Final Android release source:** `b19c3b150468318a71da6c4763266cf4aba10cdd`  
**Google Play Closed Testing:** Published on 2026-08-18

## Completed gates

- ✅ Independent pre-AAB code audits completed.
- ✅ Android unit/lint/generator/fallback/event-feed/runtime-asset validation passed on the audited source tree.
- ✅ Web typecheck/lint/unit/build/Playwright validation passed on the audited source tree.
- ✅ Samsung Galaxy S25 / Android 16 physical validation passed for core Search Assistant, localized output, Event Guide, Privacy, caught-date composition and tester-feedback launch.
- ✅ PR #21 remediation merged to `master`; its merge tree was byte-identical to the CI-passed PR head.
- ✅ Android release metadata bumped and merged as `0.7.5` / `versionCode 25` in PR #22.
- ✅ Release-bump Android CI passed completely: unit tests, lint, golden-corpus identity, Event Guide fallback freshness, generator/fallback validators, event-feed validation and runtime-asset validation.
- ✅ Final signed release AAB built from `b19c3b150468318a71da6c4763266cf4aba10cdd` using the existing configured Play upload keystore.
- ✅ `jarsigner` verification passed.
- ✅ Official Google bundletool 1.18.3 validation passed.
- ✅ Built AAB manifest verified directly as package `com.caglar.pokequery`, versionCode `25`, versionName `0.7.5`.
- ✅ Delivery AAB copy hash matches the Gradle output hash.
- ✅ Annotated `v0.7.5` created and pushed without moving `v0.7.4`; GitHub comparison confirms `v0.7.5` is identical to release source `b19c3b150468318a71da6c4763266cf4aba10cdd`.
- ✅ The exact verified `PokeQuery-v0.7.5-code25.aab` was accepted by Google Play and published to the Closed Testing track on 2026-08-18.
- ✅ Google Play accepted `versionCode 25` and the configured upload certificate.

## Verified release artifact

- **File:** `PokeQuery-v0.7.5-code25.aab`
- **Size:** 4,933,973 bytes
- **SHA-256:** `4BEBCCA6911B8F231C11758190BD72EF9708495149BBAEC79791640385116EE1`
- **Signer certificate SHA-256:** `EB:D1:AF:BF:B1:02:8B:06:11:C5:E1:DE:2F:92:2B:60:A8:A1:22:EE:1D:86:5A:A3:BE:BB:7F:6B:A9:08:AA:8C`
- **Release tag:** `v0.7.5` → `b19c3b150468318a71da6c4763266cf4aba10cdd`
- **Google Play status:** Closed Testing release published

## Release status

The v0.7.5 / versionCode 25 Android release pipeline is complete. The verified signed AAB was accepted by Google Play and published to the Closed Testing track on 2026-08-18.

No application-code, CI, signing, artifact, tag, or Google Play upload blocker remains for this release.

## Non-blocking verification note

The previous v0.7.4 AAB was not available locally for a direct certificate-fingerprint comparison. The v0.7.5 build used the existing configured Play upload keystore, its signer certificate was verified with `jarsigner`, and Google Play subsequently accepted the upload certificate for versionCode 25.

## Device validation note

The signed release APK was not installed over the existing physical debug build because Android rejected the replacement due to the expected signing mismatch. The existing debug app was intentionally not uninstalled, preserving app data. Physical functional smoke evidence from the same release code path remains PASS; bundle integrity/package/version were verified directly from the signed AAB with bundletool.

## Known deferred issue

Samsung One UI did not list the optional PokeQuery home-screen widgets in the launcher widget picker during pre-AAB physical validation. Widget discovery/tap validation remains intentionally deferred; core app flows and the published Closed Testing release are unaffected.

## Current blocker

None for v0.7.5 Closed Testing publication.
