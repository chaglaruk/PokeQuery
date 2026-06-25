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
    // v0.5.5 safety hotfix — compound parser-sensitive PROTECTION tokens fall back to English.
    //
    // The multi-word Turkish candidates (background / locationbackground / specialbackground /
    // ultrabeast) are unverified against a live Turkish client. A wrong multi-word form silently
    // breaks a PROTECTION token — the kind that must work to keep a valuable Pokémon out of a
    // cleanup/transfer/trade list. So generated Turkish strings keep these tokens in English,
    // exactly like `count` already does. Candidate forms stay visible in the KB/registry for
    // verification, not as emitted tokens.
    // -------------------------------------------------------------------------

    @Test
    fun `Turkish count cleanup keeps compound protection tokens in English fallback`() {
        // A Safe Cleanup / count-cleanup string builds the base in English (with mandatory
        // protections incl. the background variants + !traded), then translates. The compound
        // parser-sensitive protections MUST stay English; only verified-ish tokens translate.
        val base = "count2-&!shiny&!lucky&!legendary&!mythical&!shadow&!purified&!favorite&" +
            "!traded&!costume&!ultrabeast&!background&!locationbackground&!specialbackground&!#&!4*"
        val result = SearchTermMapper.translateSyntax(base, "Turkish")

        // count2- stays English (existing fallback).
        assertTrue("count must stay English (fallback): $result", result.contains("count2-"))
        assertFalse("must not emit 'toplam': $result", result.contains("toplam"))

        // !traded invariant stays present (Turkish: takaslanan is a BETA single-word candidate).
        assertTrue("!traded invariant (takaslanan) must remain: $result", result.contains("!takaslanan"))

        // The OTHER mapped tokens still translate (sanity that translation still runs).
        assertTrue("shiny→parlak expected: $result", result.contains("!parlak"))
        assertTrue("legendary→efsanevi expected: $result", result.contains("!efsanevi"))

        // The compound PARSER-SENSITIVE protections MUST stay English (fallback).
        assertTrue("!ultrabeast must stay English (fallback): $result", result.contains("!ultrabeast"))
        assertTrue("!background must stay English (fallback): $result", result.contains("!background"))
        assertTrue("!locationbackground must stay English (fallback): $result", result.contains("!locationbackground"))
        assertTrue("!specialbackground must stay English (fallback): $result", result.contains("!specialbackground"))

        // And MUST NOT emit the unverified multi-word candidate forms.
        assertFalse("must not emit 'ultra canavar' (unverified multi-word): $result", result.contains("ultra canavar"))
        assertFalse("must not emit 'arka planlı' (unverified multi-word): $result", result.contains("arka planlı"))
        assertFalse("must not emit 'konum arka planlı' (unverified multi-word): $result", result.contains("konum arka planlı"))
        assertFalse("must not emit 'özel arka planlı' (unverified multi-word): $result", result.contains("özel arka planlı"))
    }

    @Test
    fun `Turkish safe cleanup string keeps valuable-item exclusions in English fallback`() {
        // safe_cleanup base = "1*" + DEFAULT_PROTECTIONS, which include the background variants.
        val base = "1*&!shiny&!legendary&!mythical&!ultrabeast&!costume&!background&" +
            "!locationbackground&!specialbackground&!shadow&!purified&!favorite&!lucky&!#&!traded&!4*"
        val result = SearchTermMapper.translateSyntax(base, "Turkish")

        // Compound valuable-item exclusions stay English (fallback) — not the multi-word candidates.
        assertTrue("!ultrabeast stays English: $result", result.contains("!ultrabeast"))
        assertTrue("!background stays English: $result", result.contains("!background"))
        assertTrue("!locationbackground stays English: $result", result.contains("!locationbackground"))
        assertTrue("!specialbackground stays English: $result", result.contains("!specialbackground"))
        assertFalse("no 'arka planlı' candidate in generated string: $result", result.contains("arka planlı"))
        assertFalse("no 'ultra canavar' candidate in generated string: $result", result.contains("ultra canavar"))

        // The rest still translates (the cleanup is visibly Turkish beta, not silently English).
        assertTrue("shiny→parlak expected: $result", result.contains("!parlak"))
        assertTrue("costume→kostümlü expected: $result", result.contains("!kostümlü"))
    }

    @Test
    fun `Turkish generated string never outputs unverified multi-word protection tokens`() {
        // Exhaustive guard: NONE of the unverified multi-word candidate forms may appear in any
        // translated output, regardless of which English protection token produced them.
        val candidateForms = listOf("arka planlı", "konum arka planlı", "özel arka planlı", "ultra canavar")
        val bases = listOf(
            "count2-&!ultrabeast&!background&!locationbackground&!specialbackground",
            "1*&!ultrabeast&!background&!locationbackground&!specialbackground",
            "age365-&!ultrabeast&!background&!locationbackground&!specialbackground"
        )
        bases.forEach { base ->
            val result = SearchTermMapper.translateSyntax(base, "Turkish")
            candidateForms.forEach { candidate ->
                assertFalse(
                    "Unverified multi-word candidate '$candidate' must NEVER be emitted (base=$base, result=$result)",
                    result.contains(candidate)
                )
            }
        }
    }
}
