package com.caglar.pokequery.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the v0.4.2 safety patch (Fix 3).
 *
 * Audit finding (BUG-002/003): the mapper auto-switched to Turkish whenever the device
 * locale was tr-TR, even though Turkish tokens are unverified vs the live Pokémon GO
 * client and the localization plan says "Do not enable Turkish runtime mode in this
 * phase." Auto must resolve to the safe default (English); Turkish remains available
 * only as an explicit manual selection.
 */
class SearchTermMapperTest {

    @Test
    fun `English is the safe default`() {
        assertEquals("English", SearchTermMapper.resolveLanguage("English"))
    }

    @Test
    fun `Auto resolves to English regardless of device locale`() {
        // Even on a tr-TR device, Auto must not silently switch to Turkish.
        assertEquals("English", SearchTermMapper.resolveLanguage("Auto"))
        // Blank settings also fall back to the safe English default.
        assertEquals("English", SearchTermMapper.resolveLanguage(""))
        assertEquals("English", SearchTermMapper.resolveLanguage("   "))
    }

    @Test
    fun `explicit Turkish selection is preserved`() {
        assertEquals("Turkish", SearchTermMapper.resolveLanguage("Turkish"))
    }

    @Test
    fun `English pass-through does not translate tokens`() {
        val result = SearchTermMapper.translateSyntax("count2-&!shiny&!traded", "English")
        assertEquals("count2-&!shiny&!traded", result)
    }

    @Test
    fun `explicit Turkish selection still translates according to the current map`() {
        // Golden behavior for currently-mapped tokens only. This locks the *existing* map;
        // it does NOT assert the tokens are verified against the live game client.
        val result = SearchTermMapper.translateSyntax("count2-&!shiny", "Turkish")
        // v0.5.5 (Fix 4): 'count' is NO LONGER translated — its Turkish candidate is contested
        // (toplam/sayı/sayısı) and parser-sensitive, so the English 'count' is emitted even in
        // Turkish output. Other mapped tokens (shiny→parlak) still translate.
        assertTrue("count must stay English (fallback) in Turkish output: $result", result.contains("count2-"))
        assertFalse("count must NOT be translated to 'toplam' (unverified candidate): $result", result.contains("toplam"))
        assertTrue("Expected Turkish shiny token in: $result", result.contains("parlak"))
    }

    @Test
    fun `Auto no longer emits Turkish tokens even when a token is translatable`() {
        // Sanity: Auto must produce the English base string, not a translated one.
        val result = SearchTermMapper.translateSyntax("shiny&!traded", "Auto")
        assertFalse("Auto must not translate 'shiny' to 'parlak': $result", result.contains("parlak"))
        assertEquals("shiny&!traded", result)
    }

    @Test
    fun `looksTurkish detects translated output for the risk-warning beta notice`() {
        // Used by the RiskWarning screen to decide whether to show the Turkish-beta caution.
        // v0.5.5 (Fix 4): count stays English even in Turkish output, so the detector must rely
        // on the OTHER translated tokens (parlak/gölge) and Turkish-specific letters — not count.
        assertTrue(SearchTermMapper.looksTurkish("count2-&!parlak"))
        assertTrue(SearchTermMapper.looksTurkish("count2-&!gölge"))
        assertFalse(SearchTermMapper.looksTurkish("count2-&!shiny"))
        assertFalse(SearchTermMapper.looksTurkish("4*"))
    }

    // -------------------------------------------------------------------------
    // Official Help Center-backed tokens translate; unsafe or unverified tokens fall back.
    // -------------------------------------------------------------------------

    @Test
    fun `Turkish count cleanup translates only officially verified safe tokens`() {
        // A Safe Cleanup / count-cleanup string builds the base in English (with mandatory
        // protections incl. the background variants + !traded), then translates.
        val base = "count2-&!shiny&!lucky&!legendary&!mythical&!shadow&!purified&!favorite&" +
            "!traded&!costume&!ultrabeast&!background&!locationbackground&!specialbackground&!#&!4*"
        val result = SearchTermMapper.translateSyntax(base, "Turkish")

        // count2- stays English (existing fallback).
        assertTrue("count must stay English (fallback): $result", result.contains("count2-"))
        assertFalse("must not emit 'toplam': $result", result.contains("toplam"))

        // Multi-word/uncertain safety terms stay English.
        assertTrue("!traded must stay English fallback: $result", result.contains("!traded"))
        assertTrue("!specialbackground must stay English fallback: $result", result.contains("!specialbackground"))

        // Official single-token Turkish terms translate.
        assertTrue("shiny -> parlak expected: $result", result.contains("!parlak"))
        assertTrue("legendary -> efsanevi expected: $result", result.contains("!efsanevi"))
        assertTrue("costume -> kostüm expected: $result", result.contains("!kostüm"))
        assertTrue("ultrabeast -> ultracanavar expected: $result", result.contains("!ultracanavar"))
        assertTrue("background -> arkaplan expected: $result", result.contains("!arkaplan"))
        assertTrue("locationbackground -> konumarkaplanı expected: $result", result.contains("!konumarkaplanı"))
    }

    @Test
    fun `Turkish safe cleanup string keeps only unverified valuable-item exclusions in English fallback`() {
        // safe_cleanup base = "1*" + DEFAULT_PROTECTIONS, which include the background variants.
        val base = "1*&!shiny&!legendary&!mythical&!ultrabeast&!costume&!background&" +
            "!locationbackground&!specialbackground&!shadow&!purified&!favorite&!lucky&!#&!traded&!4*"
        val result = SearchTermMapper.translateSyntax(base, "Turkish")

        assertTrue("!specialbackground stays English: $result", result.contains("!specialbackground"))
        assertTrue("!traded stays English: $result", result.contains("!traded"))

        // The rest still translates using official single-token forms.
        assertTrue("shiny -> parlak expected: $result", result.contains("!parlak"))
        assertTrue("costume -> kostüm expected: $result", result.contains("!kostüm"))
        assertTrue("background -> arkaplan expected: $result", result.contains("!arkaplan"))
    }

    @Test
    fun `operators remain canonical while tokens translate`() {
        val result = SearchTermMapper.translateSyntax("shiny,!legendary&cp1500-", "Turkish")
        assertEquals("parlak,!efsanevi&dg1500-", result)
    }

    @Test
    fun `unverified tokens are reported for localized output`() {
        val unverified = SearchTermMapper.findUnverifiedTokens("count2-&!traded&!specialbackground", "Turkish")
        assertEquals(listOf("count", "traded", "specialbackground"), unverified)
    }
}
