# App Config Audit

**App Name**: PokeQuery

**Package ID / applicationId / namespace**: `com.caglar.pokequery`

> Note: An older revision of this file described the package as a "placeholder" and
> recommended renaming it to `com.caglar.pokequery`. The rename (commit `c55c689`) was
> completed long ago, so that historical blocker no longer applies. The package is
> `com.caglar.pokequery` across `build.gradle.kts`, the manifest, and all release docs.
> The string `poke.cglr` does not appear as a current package anywhere in the project.

**Versions** (v0.4.2 safety patch):
- `versionName`: 0.4.2
- `versionCode`: 9

**SDK Versions**:
- `minSdk`: 24
- `targetSdk`: 36
- `compileSdk`: 36

**Build Configs**:
- `debug`: Configured (no shrinking)
- `release`: Configured (minify disabled; ProGuard rules defined). Signing loaded from
  gitignored `keystore.properties` when present.
- `buildConfig`: enabled (v0.4.2) so `BuildConfig.VERSION_NAME` drives the in-app About
  version via `AppVersion` (Fix 5, audit BUG-008/015) — the displayed version no longer
  drifts from the built version.
