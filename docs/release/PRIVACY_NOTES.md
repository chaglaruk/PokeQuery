# Privacy Notes

PokeQuery operates in a privacy-first, zero-trust environment:

- **No User Accounts:** There is no login, registration, or session tracking.
- **No Cloud Sync:** All history, favorites, and settings are saved on device in `DataStore`.
- **No Analytics:** The app contains no crash reporting or analytics SDKs.
- **No API Calls:** The search string engine generates queries via static mappings in local code. It never queries an external database at runtime.
- **No Account Linking:** The app explicitly does not connect to Niantic or the Pokémon GO app. Users must manually copy text and paste it into the game themselves.
