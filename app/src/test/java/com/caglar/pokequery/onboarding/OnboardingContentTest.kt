package com.caglar.pokequery.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Package 3 — onboarding content model (paste flow + risk-color legend).
 *
 * The composable renders from this pure model so the new content is unit-testable
 * without Compose UI tests. Existing navigation/skip/finish behavior is unchanged.
 */
class OnboardingContentTest {

    @Test
    fun `pages include the new simplified hero and how-it-works pages`() {
        val pageTitles = OnboardingContent.pages.map { it.title }
        assertTrue(pageTitles.any { it.contains("Build", ignoreCase = true) })
        assertTrue(pageTitles.any { it.contains("How it works", ignoreCase = true) })
    }

    @Test
    fun `page count matches the pages list`() {
        // Renders must use this count so the pager and the model stay in sync.
        assertEquals(OnboardingContent.pages.size, OnboardingContent.pageCount)
        assertEquals(2, OnboardingContent.pageCount)
    }

    @Test
    fun `onboarding feature cards explain actual product value`() {
        val en = File("src/main/res/values/strings.xml").readText(Charsets.UTF_8)
        assertTrue(en.contains("Build useful Pokémon GO searches"))
        assertTrue(en.contains("Avoid risky transfers"))
        assertTrue(en.contains("Plan around events"))
        assertTrue(en.contains("Cleanup, candy, trades, PvP, hundos, nundos, events."))
        assertTrue(en.contains("Warnings help protect favorites, shinies, costumes, legendaries."))
        assertTrue(en.contains("Prep notes help you tag, trade, evolve, keep, or review."))
        assertTrue(!en.contains("Plan the search"))
        assertTrue(!en.contains("Pick the goal before you paste"))
    }
}
