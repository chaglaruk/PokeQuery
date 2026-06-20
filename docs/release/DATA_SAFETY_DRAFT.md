# Data Safety Draft

Based on current app architecture (v0.1.3), here are the likely answers to the Google Play Data Safety form:

- **Does your app collect or share any of the required user data types?** 
  - No.
- **Data Collection**: 
  - No personal data collected.
  - No location collection.
  - No contacts/files/photos access.
  - No gameplay data extracted from the device.
- **Data Sharing**: 
  - No data shared with third parties.
- **Data Storage**: 
  - Favorites and settings are stored locally on the device via Android DataStore.
- **Account Creation**: 
  - No account creation required.
- **Network Dependency**: 
  - Fully offline-first. No network calls are made.
