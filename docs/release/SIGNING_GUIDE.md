# Signing Guide

This document outlines the safe release signing practices for the PokeQuery project.

## Architecture

The `app/build.gradle.kts` configuration dynamically detects the presence of a local credentials file:
- If `keystore.properties` is found, the `release` build type applies the signing configuration securely.
- If it is not found, the build will proceed without failing, but the resulting artifacts will be **unsigned**.

## Local Development
- **No secrets in source control:** `.gitignore` blocks `*.jks`, `*.keystore`, and `keystore.properties`.
- **No hardcoded credentials:** The build script explicitly extracts credentials out of the properties file.

## Generation
See [KEYSTORE_LOCAL_SETUP.md](KEYSTORE_LOCAL_SETUP.md) for precise instructions on generating the key pair locally using `keytool`.

## Verification
You can manually verify a generated and signed AAB by running:
```powershell
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```
