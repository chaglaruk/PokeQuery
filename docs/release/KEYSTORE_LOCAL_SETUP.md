# Keystore Local Setup Guide

This project is configured for secure release signing. **Never commit the keystore or the password file.**

## How to Set Up Your Local Keystore

1. **Generate the Keystore:**
   Run the following PowerShell command in the repository root (`C:\Users\Caglar\Desktop\PokeQuery`) to generate `release-keystore.jks`:

   ```powershell
   keytool -genkeypair `
     -v `
     -keystore release-keystore.jks `
     -alias pokequery `
     -keyalg RSA `
     -keysize 2048 `
     -validity 10000
   ```
   *Follow the prompts and enter a strong password.*

2. **Create the Password File:**
   Create a new text file in the repository root named `keystore.properties`. 
   Add the following contents:

   ```properties
   storeFile=release-keystore.jks
   storePassword=YOUR_KEYSTORE_PASSWORD
   keyAlias=pokequery
   keyPassword=YOUR_KEY_PASSWORD
   ```
   *(Replace YOUR_KEYSTORE_PASSWORD and YOUR_KEY_PASSWORD with the passwords you set in step 1).*

3. **Verify:**
   `release-keystore.jks` and `keystore.properties` are both tracked in `.gitignore` and will never be committed. Once these files are in place, running `./gradlew bundleRelease` will automatically sign the AAB.
