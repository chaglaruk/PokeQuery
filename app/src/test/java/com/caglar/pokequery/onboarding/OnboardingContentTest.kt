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
    fun `paste flow has the full manual copy sequence`() {
        val steps = OnboardingContent.pasteFlowSteps
        assertEquals(7, steps.size)
        // Must cover the end-to-end manual workflow requested.
        assertTrue(steps.any { it.contains("Choose a goal", ignoreCase = true) })
        assertTrue(steps.any { it.contains("Copy", ignoreCase = true) })
        assertTrue(steps.any { it.contains("Open Pokémon GO", ignoreCase = true) })
        assertTrue(steps.any { it.contains("search", ignoreCase = true) })
        assertTrue(steps.any { it.contains("Paste", ignoreCase = true) })
        assertTrue(steps.any { it.contains("Review", ignoreCase = true) })
    }

    @Test
    fun `paste flow never claims automation or account access`() {
        OnboardingContent.pasteFlowSteps.forEach { step ->
            assertTrue("Step must not claim automation: $step", !step.contains("automatic", ignoreCase = true))
            assertTrue("Step must not claim account access: $step", !step.contains("login", ignoreCase = true))
        }
    }

    @Test
    fun `risk legend explains each band`() {
        val legend = OnboardingContent.riskLegend
        // Info/Low = safer inspection; Medium/High = review carefully; Turkish = beta.
        assertTrue(legend.any { it.contains("Info") && it.contains("Low", ignoreCase = true) })
        assertTrue(legend.any { it.contains("Medium") && it.contains("High", ignoreCase = true) })
        assertTrue(legend.any { it.contains("Turkish", ignoreCase = true) && it.contains("beta", ignoreCase = true) })
    }

    @Test
    fun `pages include the new paste flow and risk legend pages`() {
        val pageTitles = OnboardingContent.pages.map { it.title }
        assertTrue(pageTitles.any { it.contains("paste", ignoreCase = true) || it.contains("How to use", ignoreCase = true) })
        assertTrue(pageTitles.any { it.contains("risk", ignoreCase = true) || it.contains("Safety", ignoreCase = true) })
    }

    @Test
    fun `page count matches the pages list`() {
        // Renders must use this count so the pager and the model stay in sync.
        assertEquals(OnboardingContent.pages.size, OnboardingContent.pageCount)
    }
}
