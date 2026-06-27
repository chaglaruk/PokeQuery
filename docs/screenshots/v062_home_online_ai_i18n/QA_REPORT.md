# QA Report — v0.6.2 Home / Online Events / Assistant / i18n

**Date:** 2026-06-26
**Branch:** feature/v062-home-online-ai-i18n
**Build:** `app-debug.apk` (versionCode 19, versionName 0.6.2)
**Device:** N/A (no connected device/emulator — unit tests only)

## Test Results

All 219 unit tests pass:

```
BUILD SUCCESSFUL in 14s
26 actionable tasks: 5 executed, 21 up-to-date
```

## Verification Checklist

| Check | Status | Notes |
|-------|--------|-------|
| Unit tests | PASS | 219/219 |
| Compile (debug) | PASS | No errors |
| Lint | PASS | Warnings only (deprecated LocalClipboardManager, MenuBook icon) |
| Home screen redesign | ✓ | 8 primary + 7 tools in collapsible More Tools |
| Event feed client | ✓ | HttpURLConnection, daily cache, offline fallback |
| INTERNET permission | ✓ | Documented, opt-in, single permission |
| SearchIntentParser | ✓ | 18 patterns, synonyms, limitations |
| RemoteAiProvider | ✓ | Interface + NoOp default, disabled by default |
| Explain mode | ✓ | Token breakdown, precision labels, clipboard detection |
| SearchTokenCatalog | ✓ | EN/TR entries for 20 tokens |
| Turkish strings | ✓ | values-tr/strings.xml for shortcuts and widget |
| Changelog | ✓ | v0.6.2 entry added |
| Privacy docs | ✓ | Updated for INTERNET permission rationale |
| Manifest audit | ✓ | INTERNET documented, no other permissions |

## Known Issues

1. `LocalClipboardManager` deprecated — should migrate to `LocalClipboard` (suspend API). Pre-existing in v0.6.1 codebase, not introduced in this branch.
2. `Icons.Filled.MenuBook` deprecated — should use `Icons.AutoMirrored.Filled.MenuBook`. Pre-existing.
3. No screenshot capture performed (no connected device/emulator).
4. Runtime testing on physical device pending.

## APK Location

`app/build/outputs/apk/debug/app-debug.apk`
