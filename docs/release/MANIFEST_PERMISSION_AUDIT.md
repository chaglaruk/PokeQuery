# Manifest Permission Audit

**App Label:** PokeQuery
**Application ID / Namespace:** `com.caglar.pokequery`

## Requested Permissions

**INTERNET** (v0.6.8, Event Guide feed only)

The `AndroidManifest.xml` declares exactly one permission: `android.permission.INTERNET`.
It is used only for the Event Guide JSON feed:

- The app fetches only when the Event Guide screen is opened or refreshed.
- The feed is schema-validated JSON documented at `docs/event-feed/pokequery-events.json`.
- Failed, invalid, or unavailable feeds are ignored in favor of cached/stale/manual fallback.
- No login, tracking, analytics, scraping, Pokemon GO account access, or user API key is used.

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

PokeQuery operates primarily offline. `INTERNET` is documented solely for Event Guide feed updates. No dangerous permissions are introduced.
