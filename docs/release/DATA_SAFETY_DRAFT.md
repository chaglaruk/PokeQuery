# Data Safety Draft

PokeQuery does not intentionally collect or share any Google Play Data Safety user-data category.

When filling out the Google Play Data Safety form, use the current implementation as the source of truth:

- **Does your app collect or share any of the required user data types?** No.
- **Is your app's data collection required?** Not applicable; no required user-data category is intentionally collected.
- **Does your app encrypt data in transit?** Yes. The Android app uses HTTPS/TLS to retrieve the public, read-only Event Guide JSON feed hosted on GitHub.

## Network behavior

Core search-string generation, favorites, history, presets, settings, and other personal app state remain local on the device. The Android `INTERNET` permission is used for the public Event Guide feed only; PokeQuery does not log in to Pokémon GO, access a Pokémon GO account, or transmit Pokémon GO inventory/account data.

Standard HTTP connection metadata such as IP address and User-Agent may be processed by GitHub and network providers when serving the public feed. PokeQuery has no advertising, analytics, tracking, or crash-reporting SDK.

## Local data

Favorites, search history, presets, and settings are stored locally using Android platform storage/DataStore. `android:allowBackup="false"` is set, with backup/data-extraction exclusion rules as defense in depth. Users can delete this local data by clearing PokeQuery app storage or uninstalling the app.
