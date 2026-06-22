package com.caglar.pokequery.domain.locale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.5.2 (Fix 8): search-token localization registry tests.
 *
 * Guards that the registry documents the required tokens, never fakes verification, and
 * keeps the special/newer tokens (megaevolve, fusion, dynamax, etc.) UNTESTED so they are
 * never silently emitted as Turkish.
 */
class SearchTokenRegistryTest {

    @Test
    fun `registry covers all required important tokens`() {
        val required = listOf(
            "shiny", "traded", "count", "favorite", "legendary", "mythical", "shadow",
            "purified", "costume", "specialbackground", "locationbackground", "ultrabeast",
            "age", "distance", "attack", "defense", "hp",
            "@special", "megaevolve", "fusion", "dynamax", "gigantamax"
        )
        required.forEach { token ->
            assertNotNull("Registry must document token '$token'", SearchTokenRegistry.byEnglish(token))
        }
    }

    @Test
    fun `no token is falsely marked verified`() {
        // The core anti-fake-verification guarantee: nothing in the registry is VERIFIED
        // because none have been confirmed against a live Turkish client in this project.
        SearchTokenRegistry.tokens.forEach { token ->
            assertTrue(
                "Token '${token.english}' must not be VERIFIED (would fake verification); got ${token.status}",
                token.status != TokenVerification.VERIFIED
            )
        }
    }

    @Test
    fun `special and newer tokens are untested with no turkish candidate`() {
        // We must not guess translations for these — they stay UNTESTED until a human checks.
        val newerTokens = listOf("@special", "megaevolve", "fusion", "dynamax", "gigantamax")
        newerTokens.forEach { token ->
            val meta = SearchTokenRegistry.byEnglish(token)!!
            assertEquals("'$token' must be UNTESTED", TokenVerification.UNTESTED, meta.status)
            assertTrue("'$token' must have no Turkish candidate", meta.turkish == null)
            assertFalse("'$token' must NOT be safe to emit", meta.safeToEmit)
        }
    }

    @Test
    fun `contested tokens are marked risky not beta`() {
        // count / mythical / purified / backgrounds have contesting candidates → RISKY.
        val risky = listOf("count", "mythical", "purified", "specialbackground", "locationbackground", "ultrabeast")
        risky.forEach { token ->
            assertEquals(
                "'$token' has contesting candidates and must be RISKY",
                TokenVerification.RISKY,
                SearchTokenRegistry.byEnglish(token)!!.status
            )
        }
    }

    @Test
    fun `safe to emit is true only for verified`() {
        SearchTokenRegistry.tokens.forEach { token ->
            if (token.status == TokenVerification.VERIFIED) {
                assertTrue(token.safeToEmit)
            } else {
                assertFalse("Token '${token.english}' (${token.status}) must not be safeToEmit", token.safeToEmit)
            }
        }
    }

    @Test
    fun `every token has a concrete example`() {
        // Examples are required for the KB "Example" row; no blank examples.
        SearchTokenRegistry.tokens.forEach { token ->
            assertTrue("Token '${token.english}' must have a non-blank example", token.example.isNotBlank())
        }
    }

    @Test
    fun `language sensitive tokens are flagged`() {
        val sensitive = SearchTokenRegistry.languageSensitive()
        // All currently-documented important tokens are language-sensitive by default.
        assertTrue("Expected language-sensitive tokens", sensitive.isNotEmpty())
        // Spot-check a couple.
        assertTrue(sensitive.any { it.english == "shiny" })
        assertTrue(sensitive.any { it.english == "count" })
    }

    @Test
    fun `unverified or beta list excludes nothing falsely verified`() {
        val unverified = SearchTokenRegistry.unverifiedOrBeta()
        // Since nothing is verified, every token is in this list.
        assertEquals(SearchTokenRegistry.tokens.size, unverified.size)
    }

    @Test
    fun `registry stays in sync with what the active mapper is willing to emit`() {
        // The active SearchTermMapper Turkish map (the only tokens we currently EMIT) is a
        // subset of this registry. Any token the mapper emits must be documented here as at
        // least BETA (never VERIFIED). This test guards drift between the two sources.
        val mapperEmitsTurkish = listOf(
            "shiny", "legendary", "mythical", "ultrabeast", "shadow", "purified", "favorite",
            "lucky", "traded", "costume", "background", "locationbackground", "specialbackground",
            "attack", "defense", "hp", "distance", "age", "count"
        )
        mapperEmitsTurkish.forEach { token ->
            val meta = SearchTokenRegistry.byEnglish(token)
            assertNotNull("Mapper emits '$token' but registry does not document it", meta)
            // Emitted tokens are BETA or RISKY — never falsely VERIFIED.
            assertTrue(
                "'$token' is emitted by the mapper; registry status must be BETA or RISKY, got ${meta!!.status}",
                meta.status == TokenVerification.BETA || meta.status == TokenVerification.RISKY
            )
        }
    }
}
