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
            versionName = "0.6.2",
            versionCode = 19,
            releaseLabel = "Phase 4",
            title = "Final Polish & Turkish Localization",
            highlights = listOf(
                "Full Turkish UI localization (Event Guide, Onboarding, Goal Detail, Home)",
                "Search Assistant promoted to a primary Home card",
                "Nundo Finder moved to More Tools",
                "Public Event Guide notes with cache and safe bundled fallback"
            ),
            safetyNotes = listOf(
                "INTERNET is used only for public Event Guide notes with cache and safe fallback",
                "No login, no tracking, no ads, no analytics",
                "No Pokémon GO account access",
                "Turkish search tokens remain beta — verify results in Pokémon GO"
            ),
            testerNotes = listOf(
                "Verify Turkish localization applies to all visible UI",
                "Check Event Guide refresh and fallback notes",
                "Verify Search Assistant accepts Turkish input (e.g. 'parlak')"
            ),
            isCurrent = true
        ),
        ChangelogEntry(
            versionName = "0.6.1",
            versionCode = 18,
            releaseLabel = "Phase 2",
            title = "Workflows, Surface & Context",
            highlights = listOf(
                "Personal Presets from favorites and history",
                "Cleaning Journal notes (local-only)",
                "Practice Mode with fake inventory sandbox",
                "Android App Shortcuts",
                "Quick Access home screen widget",
                "Offline manual Event Guide context"
            ),
            safetyNotes = listOf(
                "No login, no tracking, no ads, no analytics",
                "No Pokémon GO account access",
                "No INTERNET permission, no CAMERA permission",
                "Journal is user-entered memory only — the app never knows what changed in-game",
                "Practice Mode uses a fake sandbox; it never reads your real inventory"
            ),
            testerNotes = listOf(
                "Save a favorite as a Personal Preset",
                "Add a journal note from History",
                "Open Practice Mode and inspect matches/exclusions",
                "Long-press the app icon for shortcuts",
                "Add the Quick Access widget"
            ),
            isCurrent = false
        ),
        ChangelogEntry(
            versionName = "0.6.0",
            versionCode = 17,
            releaseLabel = "Phase 1",
            title = "Trust & Education",
            highlights = listOf("Risk explanations", "Common misconceptions", "Changelog screen", "Inventory size context"),
            safetyNotes = listOf("No login", "No tracking", "No ads", "No analytics", "No Pokémon GO account access"),
            testerNotes = listOf("Check Why this risk?", "Try Common Misconceptions", "Change inventory size context"),
            isCurrent = false
        ),
        ChangelogEntry("0.5.5", 16, "Hardening", "Audit hardening", listOf("Density consumption", "Turkish safety fallback", "Locale regression guard"), listOf("Count/background/Ultra Beast protections hardened"), listOf("Regression-test Turkish fallback"), false),
        ChangelogEntry("0.5.4", 15, "Polish", "Onboarding and layout polish", listOf("Onboarding fixes", "Layout fixes", "Radio polish"), listOf("No safety model changes"), listOf("Verify settings selections"), false),
        ChangelogEntry("0.5.3", 14, "Motion", "Motion polish", listOf("Subtle screen motion", "Reduced primitive transitions"), listOf("No tracking or analytics added"), listOf("Check back navigation"), false),
        ChangelogEntry("0.5.2", 13, "Localization foundation", "App Language black-screen fix", listOf("Locale black-screen fix", "Language foundation labels"), listOf("No OS LocaleManager recreation path"), listOf("Switch App Language safely"), false)
    )
}
