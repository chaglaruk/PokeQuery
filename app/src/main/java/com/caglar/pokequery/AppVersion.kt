package com.caglar.pokequery

/**
 * Single source of truth for the user-facing app version (Fix 5, audit BUG-008/015).
 *
 * Previously Settings/About hardcoded "v0.3.4", which drifted from the real release.
 * Reading from BuildConfig.VERSION_NAME (generated from defaultConfig.versionName in
 * build.gradle.kts) guarantees the displayed version always matches the built version.
 */
object AppVersion {
    val versionName: String = BuildConfig.VERSION_NAME

    val aboutDisplayString: String
        get() = "PokeQuery v$versionName"
}
