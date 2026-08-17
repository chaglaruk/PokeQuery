package com.caglar.pokequery.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for search-string language selection and localized token emission.
 */
class SearchTermMapperTest {

    @Test
    fun `English is the safe default`() {
        assertEquals("English", SearchTermMapper.resolveLanguage("English"))
    }

    @Test
    fun `Auto resolves to English regardless of device locale`() {
        assertEquals("English", SearchTermMapper.resolveLanguage("Auto"))
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
    fun `explicit Turkish selection translates according to the official Niantic map`() {
        val result = SearchTermMapper.translateSyntax("count2-&!shiny&!traded", "Turkish")
        assertTrue("count must stay English (fallback) in Turkish output: $result", result.contains("count2-"))
        assertFalse("count must NOT be translated to 'toplam': $result", result.contains("toplam"))
        assertTrue("Expected Turkish shiny token in: $result", result.contains("!parlak"))
        assertTrue("Expected Turkish traded token in: $result", result.contains("!takas edilen"))
    }

    @Test
    fun `Auto no longer emits Turkish tokens even when a token is translatable`() {
        val result = SearchTermMapper.translateSyntax("shiny&!traded", "Auto")
        assertFalse("Auto must not translate 'shiny' to 'parlak': $result", result.contains("parlak"))
        assertEquals("shiny&!traded", result)
    }

    @Test
    fun `looksTurkish accurately detects Turkish output with token boundaries and avoids false positives`() {
        assertFalse("!specialbackground must not match Turkish hp (sp)", SearchTermMapper.looksTurkish("!specialbackground"))
        assertFalse("!shiny must not be detected as Turkish", SearchTermMapper.looksTurkish("!shiny"))
        assertFalse("distance100- must not be detected as Turkish", SearchTermMapper.looksTurkish("distance100-"))
        assertTrue("sp150 must be detected as Turkish", SearchTermMapper.looksTurkish("sp150"))
        assertTrue("dg300 must be detected as Turkish", SearchTermMapper.looksTurkish("dg300"))
        assertTrue("!şanslı must be detected as Turkish", SearchTermMapper.looksTurkish("!şanslı"))
        assertTrue("!takas edilen must be detected as Turkish", SearchTermMapper.looksTurkish("!takas edilen"))
        assertTrue("!gölge must be detected as Turkish", SearchTermMapper.looksTurkish("!gölge"))
        assertFalse("4* must not be detected as Turkish", SearchTermMapper.looksTurkish("4*"))
    }

    @Test
    fun `Turkish count cleanup translates only officially documented safe tokens`() {
        val base = "count2-&!shiny&!lucky&!legendary&!mythical&!shadow&!purified&!favorite&" +
            "!traded&!costume&!ultrabeast&!background&!locationbackground&!specialbackground&!#&!4*"
        val result = SearchTermMapper.translateSyntax(base, "Turkish")

        assertTrue("count must stay English (fallback): $result", result.contains("count2-"))
        assertFalse("must not emit 'toplam': $result", result.contains("toplam"))
        assertTrue("!traded translates to !takas edilen: $result", result.contains("!takas edilen"))
        assertTrue("!specialbackground must stay English fallback: $result", result.contains("!specialbackground"))
        assertTrue("shiny -> parlak expected: $result", result.contains("!parlak"))
        assertTrue("legendary -> efsanevi expected: $result", result.contains("!efsanevi"))
        assertTrue("costume -> kostüm expected: $result", result.contains("!kostüm"))
        assertTrue("ultrabeast -> ultracanavar expected: $result", result.contains("!ultracanavar"))
        assertTrue("background -> arkaplan expected: $result", result.contains("!arkaplan"))
        assertTrue("locationbackground -> konumarkaplanı expected: $result", result.contains("!konumarkaplanı"))
    }

    @Test
    fun `Turkish safe cleanup keeps only unresolved protection terms in English fallback`() {
        val base = "1*&!shiny&!legendary&!mythical&!ultrabeast&!costume&!background&" +
            "!locationbackground&!specialbackground&!shadow&!purified&!favorite&!lucky&!#&!traded&!4*"
        val result = SearchTermMapper.translateSyntax(base, "Turkish")

        assertTrue("!specialbackground stays English: $result", result.contains("!specialbackground"))
        assertTrue("!traded translates to !takas edilen: $result", result.contains("!takas edilen"))
        assertTrue("shiny -> parlak expected: $result", result.contains("!parlak"))
        assertTrue("costume -> kostüm expected: $result", result.contains("!kostüm"))
        assertTrue("background -> arkaplan expected: $result", result.contains("!arkaplan"))
    }

    @Test
    fun `operators and custom separators remain canonical while tokens translate`() {
        val result = SearchTermMapper.translateSyntax("shiny,!legendary&cp1500-", "Turkish")
        assertEquals("parlak,!efsanevi&dg1500-", result)

        val semicolonResult = SearchTermMapper.translateSyntax("shiny;legendary", "Turkish")
        assertEquals("parlak;efsanevi", semicolonResult)

        val colonResult = SearchTermMapper.translateSyntax("shiny:legendary", "Turkish")
        assertEquals("parlak:efsanevi", colonResult)
    }

    @Test
    fun `unverified tokens are reported for localized output`() {
        // Turkish traded is now verified in official Help Center; count and specialbackground remain unverified.
        val unverified = SearchTermMapper.findUnverifiedTokens("count2-&!traded&!specialbackground", "Turkish")
        assertEquals(listOf("count", "specialbackground"), unverified)
    }

    @Test
    fun `German official search tokens match official Niantic FAQ`() {
        assertEquals("glücks", SearchTermMapper.translateSyntax("lucky", "German"))
        assertEquals("!glücks", SearchTermMapper.translateSyntax("!lucky", "German"))
        assertEquals("nurausEiern", SearchTermMapper.translateSyntax("eggsonly", "German"))
        assertEquals("kumpel", SearchTermMapper.translateSyntax("buddy", "German"))
        assertEquals("tauschentwicklung", SearchTermMapper.translateSyntax("tradeevolve", "German"))
        assertEquals("!tauschentwicklung", SearchTermMapper.translateSyntax("!tradeevolve", "German"))
    }

    @Test
    fun `Spanish official search tokens match official Niantic FAQ`() {
        assertEquals("con suerte", SearchTermMapper.translateSyntax("lucky", "Spanish"))
        assertEquals("!con suerte", SearchTermMapper.translateSyntax("!lucky", "Spanish"))
        assertEquals("intercambiados", SearchTermMapper.translateSyntax("traded", "Spanish"))
        assertEquals("!intercambiados", SearchTermMapper.translateSyntax("!traded", "Spanish"))
        assertEquals("entrenamiento extremo", SearchTermMapper.translateSyntax("hypertraining", "Spanish"))
    }

    @Test
    fun `French official search tokens handle numeric suffix properly`() {
        assertEquals("méga1", SearchTermMapper.translateSyntax("mega1", "French"))
        assertEquals("copain", SearchTermMapper.translateSyntax("buddy", "French"))
    }

    @Test
    fun `Italian official search tokens translate evolve to fai evolvere`() {
        assertEquals("fai evolvere", SearchTermMapper.translateSyntax("evolve", "Italian"))
    }

    @Test
    fun `Turkish official CP and HP abbreviations remain DG and SP`() {
        assertEquals("dg300&sp150", SearchTermMapper.translateSyntax("cp300&hp150", "Turkish"))
    }
}
