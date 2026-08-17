package com.caglar.pokequery.domain.locale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the Turkish search token metadata registry and confidence tracking.
 */
class SearchTokenRegistryTest {

    @Test
    fun `every canonical token is present in the registry`() {
        val expected = listOf(
            "cp", "hp", "distance", "attack", "defense", "age", "year",
            "shiny", "legendary", "mythical", "shadow", "purified", "favorite",
            "lucky", "costume", "traded", "defender", "background", "locationbackground",
            "specialbackground", "ultrabeast", "dynamax", "gigantamax", "fusion",
            "mega", "megaevolve", "buddy", "evolve", "hypertraining", "item",
            "evolvenew", "evolvequest", "tradeevolve", "@special", "@weather",
            "eggsonly", "hatched", "count"
        )
        expected.forEach { key ->
            assertNotNull("Missing canonical token: '$key'", SearchTokenRegistry.byEnglish(key))
        }
    }

    @Test
    fun `official Niantic Help Center tokens are documented as BETA`() {
        val betaTokens = listOf("shiny", "lucky", "shadow", "purified", "costume", "legendary", "mythical", "traded")
        betaTokens.forEach { token ->
            val meta = SearchTokenRegistry.byEnglish(token)
            assertNotNull("Token '$token' should exist in registry", meta)
            assertEquals("Token '$token' should be BETA", TokenVerification.BETA, meta!!.status)
            assertFalse("'$token' must NOT be isLiveVerified until live client confirmed", meta.isLiveVerified)
        }
    }

    @Test
    fun `unsupported tokens remain UNTESTED with null candidate`() {
        val untested = listOf("count", "specialbackground")
        untested.forEach { token ->
            val meta = SearchTokenRegistry.byEnglish(token)
            assertNotNull("Token '$token' should exist", meta)
            assertEquals("Token '$token' must be UNTESTED", TokenVerification.UNTESTED, meta!!.status)
            assertNull("Token '$token' must have null Turkish candidate (English fallback)", meta.turkish)
            assertFalse("'$token' must NOT be isLiveVerified", meta.isLiveVerified)
        }
    }

    @Test
    fun `every token has a concrete example`() {
        SearchTokenRegistry.tokens.forEach { token ->
            assertTrue("Token '${token.english}' must have a non-blank example", token.example.isNotBlank())
        }
    }

    @Test
    fun `language sensitive tokens are flagged`() {
        val sensitive = SearchTokenRegistry.languageSensitive()
        assertTrue("Expected language-sensitive tokens", sensitive.isNotEmpty())
        assertTrue(sensitive.any { it.english == "shiny" })
        assertTrue(sensitive.any { it.english == "count" })
    }

    @Test
    fun `unverified or beta list excludes nothing falsely verified`() {
        val unverified = SearchTokenRegistry.unverifiedOrBeta()
        assertEquals(SearchTokenRegistry.tokens.size, unverified.size)
    }

    @Test
    fun `registry stays in sync with what the active mapper is willing to emit`() {
        val mapperEmitsTurkish = listOf(
            "cp", "hp", "distance", "attack", "defense", "age", "year",
            "shiny", "legendary", "mythical", "shadow", "purified", "favorite",
            "lucky", "costume", "traded", "defender", "background", "locationbackground",
            "ultrabeast", "dynamax", "gigantamax", "fusion", "mega", "megaevolve",
            "buddy", "evolve", "hypertraining", "item", "evolvenew", "evolvequest",
            "tradeevolve", "@special", "@weather", "eggsonly", "hatched"
        )
        mapperEmitsTurkish.forEach { token ->
            val meta = SearchTokenRegistry.byEnglish(token)
            assertNotNull("Mapper emits '$token' but registry does not document it", meta)
            assertTrue(
                "'$token' is emitted by the mapper; registry status must be BETA or RISKY, got ${meta!!.status}",
                meta.status == TokenVerification.BETA || meta.status == TokenVerification.RISKY
            )
        }
    }

    @Test
    fun `compound parser-sensitive tokens are tracked and not live verified`() {
        assertEquals(
            listOf("specialbackground", "locationbackground", "ultrabeast", "background"),
            SearchTokenRegistry.compoundTokens.map { it.english }
        )
        SearchTokenRegistry.compoundTokens.forEach { token ->
            assertFalse("'${token.english}' must not be isLiveVerified", token.isLiveVerified)
        }
    }

    @Test
    fun `compound protection token candidates are centralized`() {
        assertEquals(
            mapOf(
                "background" to "arkaplan",
                "locationbackground" to "konumarkaplanı",
                "ultrabeast" to "ultracanavar"
            ),
            SearchTokenRegistry.compoundCandidates
        )
        SearchTokenRegistry.compoundCandidates.forEach { (english, candidate) ->
            val meta = SearchTokenRegistry.byEnglish(english)
            assertNotNull("Candidate '$candidate' maps to unknown token '$english'", meta)
        }
    }
}
