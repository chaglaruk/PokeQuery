# Signing Guide

## Uploading to Play Console

The current `app-debug.apk` generated during active development is **NOT** meant for Google Play release. You must sign the app with a release keystore.

### 1. Create an Upload Keystore
Use Android Studio (Build > Generate Signed Bundle / APK) or the `keytool` command-line utility to generate a new `.jks` or `.keystore` file.

### 2. Secure the Keystore
- Store the keystore **outside** of this git repository.
- **NEVER** commit your keystore, alias names, or passwords into version control.

### 3. Gradle Signing Config
Once created, configure your `release` build type in `app/build.gradle.kts` to reference your secure keystore paths using local properties or environment variables.

### 4. Generate Release AAB
When ready, run:
`./gradlew bundleRelease`
to generate an Android App Bundle (`.aab`) which is the required format for modern Play Store uploads.
