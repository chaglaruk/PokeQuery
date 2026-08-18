# AAB Build Report

**Target Version:** 0.7.5  
**versionCode:** 25  
**Application ID:** `com.caglar.pokequery`  
**Release source commit:** `b19c3b150468318a71da6c4763266cf4aba10cdd`

## Output Summary

- **Result:** Success
- **Gradle task:** `clean :app:bundleRelease`
- **Source artifact:** `app/build/outputs/bundle/release/app-release.aab`
- **Delivery artifact:** `C:\Users\Caglar\Desktop\PokeQuery-Releases\0.7.5\PokeQuery-v0.7.5-code25.aab`
- **File size:** 4,933,973 bytes
- **SHA-256:** `4BEBCCA6911B8F231C11758190BD72EF9708495149BBAEC79791640385116EE1`
- **Source/delivery hashes:** Match

## Signing Verification

- **`jarsigner -verify -certs`:** PASS
- **Signer subject:** `CN=PokeQuery Upload, OU=Development, O=Caglar Dinc, L=London, ST=London, C=GB`
- **Signer issuer:** `CN=PokeQuery Upload, OU=Development, O=Caglar Dinc, L=London, ST=London, C=GB`
- **Signer certificate SHA-256:** `EB:D1:AF:BF:B1:02:8B:06:11:C5:E1:DE:2F:92:2B:60:A8:A1:22:EE:1D:86:5A:A3:BE:BB:7F:6B:A9:08:AA:8C`
- **Certificate validity:** 2026-08-14 02:43:26 BST through 2053-12-30 01:43:26 GMT
- **Previous v0.7.4 AAB certificate comparison:** Previous artifact not available locally; the build used the existing configured Play upload keystore.

## Bundletool Verification

- **Tool:** Google `bundletool-all-1.18.3.jar`
- **Tool SHA-256:** `A099CFA1543F55593BC2ED16A70A7C67FE54B1747BB7301F37FDFD6D91028E29`
- **Version:** 1.18.3
- **`bundletool validate`:** PASS
- **Built AAB package:** `com.caglar.pokequery`
- **Built AAB versionCode:** 25
- **Built AAB versionName:** `0.7.5`

## Device / Smoke Validation

- **Device:** Samsung Galaxy S25 (`SM-S931B`), Android 16 / API 36
- **Signed release APK install:** Not performed because the already-installed debug build has a different signature; the existing app was intentionally not uninstalled to preserve app data.
- **Existing physical debug smoke evidence:** PASS
  - caught-date + shiny composition
  - caught-date + hundo + `!shiny`
  - tester-feedback chooser without crash
  - About version 0.7.5 / code 25
  - Search String Language independence
  - no generated `|`

## Integrity

- Local `master`, `origin/master`, and release source SHA were all `b19c3b150468318a71da6c4763266cf4aba10cdd` before the build.
- Source worktree remained clean after build/validation.
- No source changes, commits, pushes, tags, or Play uploads were performed during artifact generation.

## Release Artifact to Upload

Upload exactly:

`PokeQuery-v0.7.5-code25.aab`

with SHA-256:

`4BEBCCA6911B8F231C11758190BD72EF9708495149BBAEC79791640385116EE1`
