package com.caglar.pokequery.onboarding

/**
 * Package 3 — pure onboarding content model.
 *
 * The OnboardingScreen composable renders its pages from this model, so the new
 * "How to paste into Pokémon GO" flow and the risk-color legend are unit-testable
 * without Compose UI tests. No navigation/skip/finish behavior changes.
 *
 * Content rules: manual copy/paste only; never claims automation, login, or account
 * access; Turkish is consistently labelled beta.
 */
object OnboardingContent {

    /** The full manual copy/paste workflow shown on the "How to use" page. */
    val pasteFlowSteps: List<String> = listOf(
        "1. Choose a goal in PokeQuery (e.g. Safe Cleanup).",
        "2. Review the risk and protected-category notes.",
        "3. Copy the generated search string.",
        "4. Open Pokémon GO manually on your device.",
        "5. Open your Pokémon storage and tap the search bar.",
        "6. Paste the string into the search bar.",
        "7. Review the results carefully before any transfer or trade."
    )

    /** Risk-color legend shown on the safety page. */
    val riskLegend: List<String> = listOf(
        "Info / Low — safer inspection or a narrow query. Still review matches.",
        "Medium / High — review carefully before acting. Valuable Pokémon may appear.",
        "Turkish — beta. Verify results in Pokémon GO before transferring or trading."
    )

    data class Page(
        val title: String,
        /** Rendered as a list under the title when non-empty (paste steps / risk legend). */
        val bullets: List<String> = emptyList(),
        val description: String = ""
    )

    val pages: List<Page> = listOf(
        Page(
            title = "Build safer Pokémon GO search strings",
            description = "Safe defaults for cleanup, candy prep, trading, PvP checks, Hundos and Nundos."
        ),
        Page(
            title = "How it works",
            description = "PokeQuery only creates text. You copy it and paste it into Pokémon GO yourself. No login, no scraping, no connection to the game."
        )
    )

    val pageCount: Int get() = pages.size
}
