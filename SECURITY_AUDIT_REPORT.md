# Defensive Privacy and Android Hardening Report

**Date:** 2026-06-22
**Scope:** Local PokeQuery repository (`com.caglar.pokequery`, v0.5.2, versionCode 13)
**Reviewer:** Automated defensive review (Antigravity IDE)
**Type:** Code-quality, privacy, manifest, storage, logging, and release-readiness review

---

## How `.agent-reference/Claude-BugHunter` Was Used

The Claude-BugHunter reference material was read **as documentation only** for:
- Structured review methodology and evidence hygiene principles
- False-positive avoidance guidance
- Clear severity grouping (Critical / High / Medium / Low / Info)
- Reproducible local findings approach
- Safe remediation planning

**Not used:** External target testing, exploitation steps, payloads, red-team workflows, offensive tooling, Burp/MCP, credential attacks, or network probing.

---

## Repo Map

| Area | Path(s) | Description |
|------|---------|-------------|
| **App Type** | Android / Jetpack Compose (Kotlin) | Offline-first Pokémon GO search string generator |
| **Build System** | Gradle KTS + Version Catalog | compileSdk 36, minSdk 24, targetSdk 36 |
| **Application ID** | `com.caglar.pokequery` | Single-module app |
| **Manifest** | `app/src/main/AndroidManifest.xml` | Single activity, no services/receivers/providers |
| **Permissions** | **None declared** | Zero permissions — matches Play Data Safety claims |
| **Activities** | `MainActivity` (exported=true, launcher) | Only exported component |
| **FileProvider** | Not present | No content:// sharing |
| **Network Config** | Not present (not needed) | App makes zero network requests |
| **WebView** | Not present | No web content rendering |
| **Logging** | **None** | No `Log.*`, `println`, or `System.out` in main source |
| **Storage** | DataStore preferences only | `user_prefs` via Jetpack DataStore |
| **Screenshots/OCR** | Not present | No MediaProjection, screenshot capture, or OCR |
| **Data Models** | `data/model/` | `Term`, `GeneratedString`, `SavedTemplate`, `RiskLevel` |
| **Domain Logic** | `domain/engine/`, `domain/expert/`, `domain/risk/`, `domain/lint/`, `domain/locale/` | String builder engine, risk assessment, localization |
| **Feedback** | `feedback/FeedbackBuilder.kt` | mailto-based, no network, user-initiated only |
| **Tests** | `app/src/test/` | Unit tests for navigation, privacy, presets, density, locale, feedback, onboarding |
| **Proguard** | `app/proguard-rules.pro` | Conservative keeps for serialization and NavKeys |
| **Signing** | `keystore.properties` + `release-keystore.jks` | Both in `.gitignore` — NOT tracked |

---

## Reviewed Areas

1. ✅ Android permissions — zero declared, regression test exists
2. ✅ Exported components — only launcher `MainActivity`, regression test exists
3. ✅ Intent handling — `start_route` extra handled safely with fallback
4. ✅ FileProvider — not present, not needed
5. ✅ WebView — not present
6. ✅ Network config — no INTERNET permission, no HTTP calls, no cleartext traffic
7. ✅ Logging — zero `Log.*`, `println`, or `System.out` in production code
8. ✅ Storage — DataStore only (app-private), no external storage, no databases, no temp files
9. ✅ Screenshots/OCR — not present
10. ✅ Secrets — `keystore.properties`, `.jks`, `.pem`, `.key` all in `.gitignore`; confirmed not tracked
11. ✅ Debug/Release — R8 minification + shrinking enabled for release; `buildConfig = true` for version display only
12. ✅ Backup/extraction — `allowBackup="false"` set (**fixed**: rules XMLs now wired)
13. ✅ Clipboard — Compose `LocalClipboardManager` used for search string copy only; appropriate
14. ✅ Feedback — mailto-based, no auto-send, user reviews before sending
15. ✅ Gradle/dependencies — standard AndroidX/Compose BOM; no risky third-party dependencies
16. ✅ ProGuard — conservative keeps; no reflection-heavy libraries
17. ✅ Tests — existing privacy regression tests for manifest, BuildConfig, presets, locale

---

## Findings

### Critical
None.

### High
None.

### Medium

#### M-1: Backup/extraction rules XMLs not wired in manifest
- **File:** `app/src/main/AndroidManifest.xml`
- **Finding:** `backup_rules.xml` and `data_extraction_rules.xml` existed in `res/xml/` but were not referenced via `android:fullBackupContent` or `android:dataExtractionRules` in the `<application>` tag. While `allowBackup="false"` already disables auto-backup, the explicit rules provide defense-in-depth and are required for Play Store compliance on API 31+.
- **Status:** ✅ **Fixed**

#### M-2: Backup/extraction rules were template stubs
- **File:** `app/src/main/res/xml/backup_rules.xml`, `app/src/main/res/xml/data_extraction_rules.xml`
- **Finding:** Both files contained only commented-out template content with TODOs. Since the app stores user preferences (favorites, history, settings) in DataStore, explicit exclusion rules should be configured.
- **Status:** ✅ **Fixed** — both files now explicitly exclude `sharedpref`, `database`, and `file` domains.

### Low

#### L-1: Missing `enableOnBackInvokedCallback` for predictive back
- **File:** `app/src/main/AndroidManifest.xml`
- **Finding:** Android 14+ supports predictive back gestures. Without `android:enableOnBackInvokedCallback="true"`, the app opts out of the new back navigation UX.
- **Status:** ✅ **Fixed**

### Info

#### I-1: `start_route` intent extra from external callers
- **File:** `app/src/main/java/com/caglar/pokequery/MainActivity.kt` (line 33)
- **Finding:** `MainActivity` reads `intent.getStringExtra("start_route")` to determine the initial screen. Since the activity is exported (launcher), any app could send an intent with an arbitrary `start_route` value. Current handling is safe: `startDestination()` maps unknown values to `Home`, and no value leads to privileged actions.
- **Status:** No fix needed — current code is safe-by-default.

#### I-2: `.gitignore` already properly configured
- **Finding:** `keystore.properties`, `*.jks`, `*.keystore`, `*.pem`, `*.key`, `*.p12`, `local.properties`, `.agent-reference/` are all listed. Verified none of these are tracked in Git.
- **Status:** No fix needed.

#### I-3: No `android.permission.INTERNET` — fully offline
- **Finding:** The app declares zero permissions and makes no network requests. This is a strong privacy posture.
- **Status:** No fix needed — already excellent.

#### I-4: No logging in production code
- **Finding:** Zero instances of `android.util.Log`, `Log.*`, `println`, or `System.out`/`System.err` in `app/src/main/`. This eliminates log-based data leakage.
- **Status:** No fix needed — already excellent.

#### I-5: Clipboard usage is appropriate
- **Finding:** `LocalClipboardManager` is used to copy generated Pokémon GO search strings. The data copied is non-sensitive (game search syntax). Usage is user-initiated only.
- **Status:** No fix needed.

---

## Changes Made

| File | Change |
|------|--------|
| `app/src/main/AndroidManifest.xml` | Added `android:dataExtractionRules`, `android:fullBackupContent`, `android:enableOnBackInvokedCallback` |
| `app/src/main/res/xml/data_extraction_rules.xml` | Replaced template with explicit exclude rules for all data domains |
| `app/src/main/res/xml/backup_rules.xml` | Replaced template with explicit exclude rules for all data domains |
| `app/src/test/.../privacy/ManifestPrivacyRegressionTest.kt` | Added 2 regression tests: `dataExtractionRules is linked in manifest`, `fullBackupContent is linked in manifest` |

---

## Issues Intentionally Not Changed

| Area | Reason |
|------|--------|
| `start_route` intent extra | Current handling is safe — unknown values fall through to `Home`. Adding explicit validation would be defensive but changes behavior. |
| Email in `FeedbackBuilder.kt` | Hardcoded recipient email (`caglar@caglardinc.com`) is intentional for closed-test feedback. Not a secret. |
| `android:supportsRtl="true"` | Standard for internationalized apps. Not a security concern. |

---

## Tests & Build Commands Run

| Command | Result |
|---------|--------|
| `gradlew.bat :app:testDebugUnitTest --no-daemon` | ✅ **BUILD SUCCESSFUL** (26 tasks, all pass) |
| `gradlew.bat :app:assembleDebug --no-daemon` | ✅ **BUILD SUCCESSFUL** (43 tasks) |

**Note:** The SDK version warning (`SDK XML versions up to 3 but version 4 encountered`) is a pre-existing environment mismatch between Android Studio and command-line tools. It does not affect the build.

---

## Remaining Risks

| Risk | Severity | Notes |
|------|----------|-------|
| No network security config XML | Info | Not needed — app has no INTERNET permission and makes zero network calls. If networking is ever added, a `network_security_config.xml` restricting cleartext should be created. |
| No `lint` run completed | Info | `lintDebug` was not run in this audit. Consider running `gradlew.bat :app:lintDebug` as a follow-up. |
| SDK version warning | Info | Pre-existing environment mismatch. Update command-line tools to match Android Studio. |

---

## Recommended Next Phase

1. **Run `gradlew.bat :app:lintDebug`** to catch any Android-specific lint warnings.
2. **Add a CI/CD step** that runs the privacy regression tests on every PR.
3. **Consider `android:localeConfig`** for API 33+ automatic per-app language selection.
4. **Monitor dependencies** — run `gradlew.bat dependencyUpdates` periodically to check for security patches.
5. **If networking is ever added:** create `res/xml/network_security_config.xml` with `cleartextTrafficPermitted="false"` and add `android:networkSecurityConfig` to the manifest.
