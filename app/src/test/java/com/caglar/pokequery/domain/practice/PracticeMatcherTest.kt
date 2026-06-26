package com.caglar.pokequery.domain.practice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.6.1 — Practice Mode conceptual matcher tests.
 *
 * The matcher is a deterministic teaching tool over the synthetic [PracticeDataset]. These tests
 * pin the conceptual behavior: a `!token` exclusion marks the matching fake item PROTECTED, a
 * positive selector marks matches MATCHED, and everything else is NOT_MATCHED. It never claims to
 * emulate the real Pokémon GO parser.
 */
class PracticeMatcherTest {

    @Test
    fun `shiny exclusion protects the shiny duplicate`() {
        val results = PracticeMatcher.match("0*,1*&!shiny")
        val shiny = results.first { it.item.id == "f2" }
        assertEquals(PracticeStatus.PROTECTED, shiny.status)
        assertTrue(shiny.reasons.any { r -> r.contains("shiny", ignoreCase = true) })
    }

    @Test
    fun `legendary exclusion protects the legendary bait`() {
        val results = PracticeMatcher.match("0*,1*&!legendary")
        val legendary = results.first { it.item.id == "f3" }
        assertEquals(PracticeStatus.PROTECTED, legendary.status)
    }

    @Test
    fun `low iv positive selector matches plain duplicates`() {
        val results = PracticeMatcher.match("0*,1*&!shiny&!legendary&!favorite")
        val plain = results.first { it.item.id == "f1" } // Common duplicate A, LOW_IV
        assertEquals(PracticeStatus.MATCHED, plain.status)
    }

    @Test
    fun `favorite flag protects the buddy`() {
        val results = PracticeMatcher.match("0*,1*&!favorite")
        val buddy = results.first { it.item.id == "f9" } // favorite buddy
        assertEquals(PracticeStatus.PROTECTED, buddy.status)
    }

    @Test
    fun `hundo selector matches only the hundo`() {
        val results = PracticeMatcher.match("4*")
        val matched = results.filter { it.status == PracticeStatus.MATCHED }
        assertEquals(1, matched.size)
        assertEquals("f15", matched.single().item.id)
    }

    @Test
    fun `traded exclusion protects traded duplicates`() {
        val results = PracticeMatcher.match("0*,1*&!traded")
        val tradedDup = results.first { it.item.id == "f11" } // traded duplicate
        assertEquals(PracticeStatus.PROTECTED, tradedDup.status)
    }

    @Test
    fun `results cover every dataset item exactly once`() {
        val results = PracticeMatcher.match("0*,1*&!shiny")
        assertEquals(PracticeDataset.items.size, results.size)
        assertEquals(PracticeDataset.items.map { it.id }.toSet(), results.map { it.item.id }.toSet())
    }

    @Test
    fun `every result carries at least one reason`() {
        val results = PracticeMatcher.match("0*,1*&!shiny&!legendary&!favorite&!traded")
        results.forEach { assertTrue("expected reasons for ${it.item.id}", it.reasons.isNotEmpty()) }
    }
}
