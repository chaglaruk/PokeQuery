package com.caglar.pokequery.domain.locale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialSearchSyntaxCoverageTest {

    @Test
    fun `all Help Center search families audited on 2026-08-18 are represented`() {
        val expected = setOf(
            "pokemon-name", "nickname", "pokedex-number", "pokemon-type", "region",
            "evolution-family", "cp", "hp", "distance", "dynamax", "fusion", "gigantamax",
            "size", "mega-evolvable", "mega-level", "move-name", "move-type", "special-move",
            "move-slot", "age", "buddy-level", "can-evolve", "favorite", "gym-defender",
            "hyper-training", "item-evolution", "new-evolution", "evolution-quest",
            "trade-evolution", "weather-boosted-move", "year", "appraisal",
            "attack-appraisal", "defense-appraisal", "hp-appraisal", "egg-exclusive", "hatched",
            "lucky", "legendary", "mythical", "background", "location-background", "purified",
            "shadow", "shiny", "costume", "tag", "traded", "ultra-beast",
            "and", "or", "not", "maximum", "minimum", "range", "punctuation"
        )
        assertEquals(expected, OfficialSearchSyntax.familyIds)
    }

    @Test
    fun `finite official value families are complete`() {
        assertEquals(listOf("0*", "1*", "2*", "3*", "4*"), OfficialSearchSyntax.appraisalValues)
        assertEquals(listOf("xxs", "xs", "xl", "xxl"), OfficialSearchSyntax.sizeValues)
        assertEquals((0..5).map { "buddy$it" }, OfficialSearchSyntax.buddyValues)
        assertEquals((1..3).map { "mega$it" }, OfficialSearchSyntax.megaLevelValues)
        assertEquals(10, OfficialSearchSyntax.regions.size)
        assertEquals(18, OfficialSearchSyntax.pokemonTypes.size)
        assertEquals(
            listOf("ultrabeast", "ultra beasts"),
            OfficialSearchSyntax.byId("ultra-beast")!!.finiteValues
        )
    }

    @Test
    fun `current official operator and bounds syntax are represented exactly`() {
        assertEquals("& or |", OfficialSearchSyntax.byId("and")!!.pattern)
        assertEquals("cp-300", OfficialSearchSyntax.byId("maximum")!!.example)
        assertEquals("cp300-", OfficialSearchSyntax.byId("minimum")!!.example)
        assertEquals("cp200-300", OfficialSearchSyntax.byId("range")!!.example)
        assertEquals("Mr. Mime", OfficialSearchSyntax.byId("punctuation")!!.example)
    }

    @Test
    fun `every family has an example and source metadata is pinned`() {
        OfficialSearchSyntax.families.forEach { family ->
            assertTrue("Missing example for ${family.id}", family.example.isNotBlank())
            assertTrue("Missing pattern for ${family.id}", family.pattern.isNotBlank())
        }
        assertTrue(OfficialSearchSyntax.SOURCE_URL.contains("faq/1486"))
        assertEquals("2026-08-18", OfficialSearchSyntax.AUDITED_DATE)
    }

    @Test
    fun `all selectable canonical status filters are known`() {
        val expected = listOf(
            "shiny", "legendary", "mythical", "ultrabeast", "shadow", "purified", "costume",
            "lucky", "traded", "favorite", "defender", "dynamax", "gigantamax", "fusion",
            "megaevolve", "evolve", "hypertraining", "item", "evolvenew", "evolvequest",
            "tradeevolve", "eggsonly", "hatched", "background", "locationbackground", "@special",
            "@weather"
        )
        assertEquals(expected, OfficialSearchSyntax.statusValues)
        expected.filterNot { it in setOf("@special", "@weather") }.forEach { token ->
            assertNotNull("Localization registry should document canonical token $token", SearchTokenRegistry.byEnglish(token))
        }
    }
}
