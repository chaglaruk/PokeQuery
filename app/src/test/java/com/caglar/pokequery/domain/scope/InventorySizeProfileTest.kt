package com.caglar.pokequery.domain.scope

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InventorySizeProfileTest {

    @Test
    fun `default stored profile is not set`() {
        assertEquals(InventorySizeProfile.NOT_SET, InventorySizeProfile.fromStored(null))
        assertEquals(InventorySizeProfile.NOT_SET, InventorySizeProfile.fromStored("garbage"))
    }

    @Test
    fun `each profile changes explanation text`() {
        val explanations = InventorySizeProfile.entries.associateWith {
            ScopeBreadthExplainer.explain("Broad", it)
        }
        InventorySizeProfile.entries.forEach { profile ->
            assertTrue(explanations.getValue(profile).contains("PokeQuery cannot see your Pok\u00e9mon GO inventory"))
            assertTrue(explanations.getValue(profile).contains("educational", ignoreCase = true))
        }
        assertNotEquals(explanations.getValue(InventorySizeProfile.SMALL), explanations.getValue(InventorySizeProfile.VERY_LARGE))
    }

    @Test
    fun `scope explanations avoid fake exact match claims`() {
        InventorySizeProfile.entries.forEach { profile ->
            val text = ScopeBreadthExplainer.explain("Very Broad", profile)
            assertFalse(text.contains("will match", ignoreCase = true))
            assertFalse(text.contains("your account", ignoreCase = true))
        }
    }

    @Test
    fun `profile descriptions are approximate ranges only`() {
        assertTrue(InventorySizeProfile.SMALL.description.contains("Approx."))
        assertTrue(InventorySizeProfile.MEDIUM.description.contains("Approx."))
        assertTrue(InventorySizeProfile.LARGE.description.contains("Approx."))
        assertTrue(InventorySizeProfile.VERY_LARGE.description.contains("Approx."))
    }
}
