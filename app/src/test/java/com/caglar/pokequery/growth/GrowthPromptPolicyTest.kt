package com.caglar.pokequery.growth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GrowthPromptPolicyTest {
    @Test
    fun firstPromptRequiresThreeSuccessfulCopies() {
        assertFalse(GrowthPromptPolicy.shouldPrompt(1, 0, false))
        assertFalse(GrowthPromptPolicy.shouldPrompt(2, 0, false))
        assertTrue(GrowthPromptPolicy.shouldPrompt(3, 0, false))
    }

    @Test
    fun dismissedPromptWaitsTenMoreCopies() {
        assertFalse(GrowthPromptPolicy.shouldPrompt(12, 3, false))
        assertTrue(GrowthPromptPolicy.shouldPrompt(13, 3, false))
    }

    @Test
    fun openingPlayStoreDisablesFuturePrompts() {
        assertFalse(GrowthPromptPolicy.shouldPrompt(100, 3, true))
    }
}
