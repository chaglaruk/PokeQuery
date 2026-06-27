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
            releaseLabel = "Phase 3",
            title = "Home Redesign, Events, Assistant & Explain",
            highlights = listOf(
                "Redesigned Home: 8 primary goals + collapsible More Tools",
                "Favorites removed from Home (accessible via bottom nav)",
                "Daily online event context with offline fallback",
                "Local NL search-string assistant (SearchIntentParser)",
                "Optional remote AI provider interface (disabled by default)",
                "Search String Explain mode with token-by-token breakdown",
                "Clipboard import detection in Explain",
                "Risk precision labels: Exact / Shortlist / Approximate / NeedsVerification",
                "SearchTokenCatalog for multi-language token research",
                "Turkish string resources for shortcuts and widget"
            ),
            safetyNotes = listOf(
                "INTERNET permission declared for optional daily event feed (opt-in via Settings)",
                "No login, no tracking, no ads, no analytics",
                "No Pokémon GO account access",
                "AI provider is an interface only — no key shipped, disabled by default",
                "Local parser output validated through Linter before copy",
                "Turkish search terms remain BETA"
            ),
            testerNotes = listOf(
                "Verify Home screen: 8 primary + More Tools collapse/expand",
                "Enable Online Event Updates in Settings, verify cache/refresh flow",
                "Open Search Assistant and type 'show me shiny legendaries'",
                "Open Explain and paste a search string from clipboard",
                "Check precision labels (Exact/Shortlist/Approximate) in Explain",
                "Verify Turkish strings appear for shortcuts"
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
                "Offline manual event context"
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
