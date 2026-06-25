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
        // v0.5.5 (Fix 4): count is no longer RISKY — it is UNTESTED (English fallback, see
        // the dedicated count-centralization test).
        // v0.5.5 safety hotfix: the compound protection tokens (backgrounds / UB) are ALSO no
        // longer RISKY — they are UNTESTED (English fallback, see the dedicated compound test).
        // Only mythical / purified still have contesting single-word candidates → RISKY.
        val risky = listOf("mythical", "purified")
        risky.forEach { token ->
            assertEquals(
                "'$token' has contesting/risky candidates and must be RISKY",
                TokenVerification.RISKY,
                SearchTokenRegistry.byEnglish(token)!!.status
            )
        }
    }

    @Test
    fun `count token truth is centralized and not emitted as turkish`() {
        // v0.5.5 (Fix 4): the count Turkish candidate is contested across sources
        // (toplam/sayı/sayısı) AND the token is parser-sensitive numeric syntax, so:
        //  - the registry metadata has NO turkish candidate (English fallback is emitted);
        //  - its status is UNTESTED (not RISKY/BETA/VERIFIED);
        //  - the contesting candidates are captured in COUNT_CANDIDATES for the matrix.
        val count = SearchTokenRegistry.byEnglish("count")!!
        assertEquals(TokenVerification.UNTESTED, count.status)
        assertTrue("count must have no emitted Turkish candidate", count.turkish == null)
        assertEquals(SearchTokenRegistry.COUNT_CANDIDATES, listOf("toplam", "sayı", "sayısı"))
        assertEquals(count, SearchTokenRegistry.countMeta)
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
        // v0.5.5 (Fix 4): 'count' is NO LONGER emitted (English fallback) so it is removed here.
        // v0.5.5 safety hotfix: the compound protection tokens (background, locationbackground,
        // specialbackground, ultrabeast) are ALSO NO LONGER emitted (English fallback) so they
        // are removed here. Their candidates live in SearchTokenRegistry.compoundCandidates.
        val mapperEmitsTurkish = listOf(
            "shiny", "legendary", "mythical", "shadow", "purified", "favorite",
            "lucky", "traded", "costume", "attack", "defense", "hp", "distance", "age"
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

    @Test
    fun `compound parser-sensitive tokens are tracked and never verified`() {
        // v0.5.5 (Fix 4): the multi-word compound candidates (backgrounds, ultra beast) are the
        // riskiest for the Pokémon GO parser because their exact spacing/form is unverified. They
        // must appear in compoundTokens, be RISKY (not verified/beta), and stay non-safeToEmit.
        //
        // v0.5.5 safety hotfix: these are now UNTESTED (was RISKY) AND not emitted (English
        // fallback) because a broken PROTECTION token is dangerous — it must work to exclude a
        // valuable Pokémon from cleanup/trade lists. Their candidate forms live in
        // compoundCandidates (NOT the active mapper), kept as hypotheses to verify live.
        assertEquals(
            listOf("specialbackground", "locationbackground", "ultrabeast", "background"),
            SearchTokenRegistry.compoundTokens.map { it.english }
        )
        SearchTokenRegistry.compoundTokens.forEach { token ->
            assertEquals("'${token.english}' compound must be UNTESTED (English fallback)", TokenVerification.UNTESTED, token.status)
            assertTrue("'${token.english}' must have no emitted Turkish candidate (fallback)", token.turkish == null)
            assertFalse("'${token.english}' must not be safe to emit", token.safeToEmit)
        }
    }

    @Test
    fun `compound protection token candidates are centralized and not emitted`() {
        // v0.5.5 safety hotfix: the multi-word Turkish candidates for the parser-sensitive
        // PROTECTION tokens are gathered in ONE place (compoundCandidates), mirroring
        // COUNT_CANDIDATES. They are NOT emitted — generated protection strings keep the English
        // token. This is the single source of truth the mapper, KB and docs agree on.
        assertEquals(
            mapOf(
                "background" to "arka planlı",
                "locationbackground" to "konum arka planlı",
                "specialbackground" to "özel arka planlı",
                "ultrabeast" to "ultra canavar"
            ),
            SearchTokenRegistry.compoundCandidates
        )
        // Every candidate maps to a token that exists and is NOT emitted (English fallback).
        SearchTokenRegistry.compoundCandidates.forEach { (english, candidate) ->
            val meta = SearchTokenRegistry.byEnglish(english)
            assertNotNull("Candidate '$candidate' maps to unknown token '$english'", meta)
            assertTrue("'$english' must be UNTESTED (English fallback)", meta!!.status == TokenVerification.UNTESTED)
            assertTrue("'$english' must have no emitted Turkish candidate", meta.turkish == null)
        }
    }
}
