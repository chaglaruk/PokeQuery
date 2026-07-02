# Privacy Notes

PokeQuery operates in a privacy-first, zero-trust environment:

- **No User Accounts:** There is no login, registration, or session tracking.
- **No Cloud Sync:** History, favorites, personal presets, journal notes, and settings are saved on device.
- **No Analytics:** The app contains no crash reporting or analytics SDKs.
- **No Game Connection:** PokeQuery generates search strings only. It never connects to Pokemon GO or a Pokemon GO account.
- **Event Feed:** When Event Guide is opened or refreshed, the app fetches one public schema-validated JSON payload from the configured feed URL. The feed has no authentication, no API keys, and no user-identifying data. Invalid or unavailable feeds fall back to cached/manual notes.

## Data Collection

PokeQuery does not collect, transmit, or share personal data, device identifiers, or usage statistics. User-created data remains on device unless the user explicitly sends feedback by email.
