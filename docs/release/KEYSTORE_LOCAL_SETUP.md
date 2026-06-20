# Keystore Local Setup Guide

If you are seeing an error that `bundleRelease` is failing due to a missing keystore configuration, follow these steps to securely set up your signing environment locally.

## 1. Create the Keystore

Open PowerShell and generate a new keystore. Do not use this exact password, choose a strong one.

```powershell
keytool -genkeypair -v -keystore "$env:USERPROFILE\.android\pokequery-upload-key.jks" -alias pokequery -keyalg RSA -keysize 2048 -validity 10000
```

* **CRITICAL**: Save your password in a password manager.
* **CRITICAL**: Never lose this keystore file (`.jks`). If you lose it, you will have to request an app signing key reset from Google Play support.

## 2. Configure Local Properties

Create a file named `keystore.properties` in the root folder of the project (`C:\Users\Caglar\Desktop\PokeQuery\keystore.properties`).

Add the following contents:

```properties
storeFile=C:\\Users\\Caglar\\.android\\pokequery-upload-key.jks
storePassword=YOUR_STRONG_PASSWORD
keyAlias=pokequery
keyPassword=YOUR_STRONG_PASSWORD
```

* **CRITICAL**: Never commit `keystore.properties` into Git. It is already added to `.gitignore`.
* **CRITICAL**: Never commit the `.jks` file into Git.

## 3. Generate Release AAB

Once the `keystore.properties` file is in place, you can build the signed Release AAB:

```powershell
./gradlew bundleRelease --no-daemon --console=plain
```

The output will be located at:
`app/build/outputs/bundle/release/app-release.aab`
