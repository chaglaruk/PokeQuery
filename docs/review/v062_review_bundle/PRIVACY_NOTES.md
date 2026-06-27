# Privacy Notes

PokeQuery operates in a privacy-first, zero-trust environment:

- **No User Accounts:** There is no login, registration, or session tracking.
- **No Cloud Sync:** All history, favorites, and settings are saved on device in `DataStore`.
- **No Analytics:** The app contains no crash reporting or analytics SDKs.
- **No API Calls (default):** The search string engine generates queries via static mappings in local code. It never queries an external database at runtime.
- **Optional Event Feed (v0.6.2):** When enabled in Settings, the app fetches a single JSON payload once per day from a configured URL to provide daily event context. This is OFF by default. The URL contains no authentication, no API keys, and no user-identifying data.
- **No Account Linking:** The app explicitly does not connect to Niantic or the Pokémon GO app. Users must manually copy text and paste it into the game themselves.

## Data Collection

PokeQuery does **not** collect, transmit, or share any personal data, device identifiers, or usage statistics. All user data (favorites, history, settings) remains on-device.
