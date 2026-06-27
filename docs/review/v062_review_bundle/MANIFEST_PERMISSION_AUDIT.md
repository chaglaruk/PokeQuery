# Manifest Permission Audit

**App Label:** PokeQuery (from strings.xml)
**Application ID / Namespace:** `com.caglar.pokequery`

## Requested Permissions

**INTERNET** (v0.6.2 — optional, opt-in only)

The `AndroidManifest.xml` declares exactly one permission: `android.permission.INTERNET`.
This is used exclusively for the **optional daily event feed**:
- The user must opt in via Settings > Event Context.
- Fetches a single JSON payload from a configured URL once per day.
- No other network access. Offline manual fallback preserved.
- OFF by default.

## Not Requested
- No ACCESS_FINE_LOCATION.
- No ACCESS_COARSE_LOCATION.
- No READ_EXTERNAL_STORAGE.
- No CAMERA.
- No RECORD_AUDIO.
- No BLUETOOTH_CONNECT.
- No POST_NOTIFICATIONS.

## Exported Components
- **Activities:** `MainActivity` (launcher).
- **Receivers:** `QuickAccessWidgetProvider` (AppWidget contract, exported by system requirement).

## Privacy Verification
PokeQuery operates primarily offline. The INTERNET permission is used solely for the optional event feed, which is opt-in and documentable in Play Data Safety.

## Action Taken
INTERNET permission added for v0.6.2 with documented rationale and opt-in toggle. No other permissions introduced.
