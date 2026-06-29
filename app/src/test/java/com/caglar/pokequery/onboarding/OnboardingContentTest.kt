package com.caglar.pokequery.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
