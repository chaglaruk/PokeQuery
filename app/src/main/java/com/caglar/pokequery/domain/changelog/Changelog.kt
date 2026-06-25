package com.caglar.pokequery.domain.changelog

data class ChangelogEntry(
    val versionName: String,
    val versionCode: Int,
    val releaseLabel: String,
    val title: String,
    val highlights: List<String>,
    val safetyNotes: List<String>,
    val testerNotes: List<String>,
    val isCurrent: Boolean
)

object Changelog {
    val entries = listOf(
        ChangelogEntry(
            versionName = "0.6.0",
            versionCode = 17,
            releaseLabel = "Phase 1",
            title = "Trust & Education",
            highlights = listOf("Risk explanations", "Common misconceptions", "Changelog screen", "Inventory size context"),
            safetyNotes = listOf("No login", "No tracking", "No ads", "No analytics", "No Pokémon GO account access"),
            testerNotes = listOf("Check Why this risk?", "Try Common Misconceptions", "Change inventory size context"),
            isCurrent = true
        ),
        ChangelogEntry("0.5.5", 16, "Hardening", "Audit hardening", listOf("Density consumption", "Turkish safety fallback", "Locale regression guard"), listOf("Count/background/Ultra Beast protections hardened"), listOf("Regression-test Turkish fallback"), false),
        ChangelogEntry("0.5.4", 15, "Polish", "Onboarding and layout polish", listOf("Onboarding fixes", "Layout fixes", "Radio polish"), listOf("No safety model changes"), listOf("Verify settings selections"), false),
        ChangelogEntry("0.5.3", 14, "Motion", "Motion polish", listOf("Subtle screen motion", "Reduced primitive transitions"), listOf("No tracking or analytics added"), listOf("Check back navigation"), false),
        ChangelogEntry("0.5.2", 13, "Localization foundation", "App Language black-screen fix", listOf("Locale black-screen fix", "Language foundation labels"), listOf("No OS LocaleManager recreation path"), listOf("Switch App Language safely"), false)
    )
}
