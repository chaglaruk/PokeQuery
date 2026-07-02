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
        assertTrue(en.contains("Build useful searches"))
        assertTrue(en.contains("Slow down on risk"))
        assertTrue(en.contains("Copy text only"))
        assertTrue(en.contains("Cleanup, candy, trades, PvP, hundos, nundos, and event prep."))
        assertTrue(en.contains("Warnings remind you to check shiny, costume, favorite, and tagged Pokémon."))
        assertTrue(en.contains("PokeQuery never touches your account. You paste the search yourself."))
        assertTrue(!en.contains("Plan the search"))
        assertTrue(!en.contains("Pick the goal before you paste"))
    }
}
