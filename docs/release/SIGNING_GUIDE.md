# Signing Guide

## Uploading to Play Console

The current `app-debug.apk` generated during active development is **NOT** meant for Google Play release. You must sign the app with a release keystore.

### 1. Keystore Creation & Security
- See [KEYSTORE_LOCAL_SETUP.md](KEYSTORE_LOCAL_SETUP.md) for the exact command to generate a keystore using `keytool`.
- Store the keystore **outside** of this git repository (e.g., `~/.android/`).
- **NEVER** commit your keystore, alias names, or passwords into version control.

### 2. Gradle Signing Config
The `app/build.gradle.kts` is configured to look for a `keystore.properties` file in the project root.
If it finds the file, it will configure the `release` build type to sign the artifacts.

**Expected `keystore.properties` content:**
```properties
storeFile=C:\\Users\\Caglar\\.android\\pokequery-upload-key.jks
storePassword=DO_NOT_COMMIT
keyAlias=pokequery
keyPassword=DO_NOT_COMMIT
```
*(Replace `DO_NOT_COMMIT` with your real passwords locally)*

### 3. Generate Release AAB
When ready, run:
`./gradlew bundleRelease --no-daemon --console=plain`
to generate an Android App Bundle (`.aab`) which is the required format for modern Play Store uploads.
