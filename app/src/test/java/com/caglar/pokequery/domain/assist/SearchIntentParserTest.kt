package com.caglar.pokequery.domain.assist

import com.caglar.pokequery.domain.lint.ExpertCopyPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for SearchIntentParser — the natural-language search assistant.
 *
 * Covers all required patterns, edge cases, combined intents,
 * and the canBuild / explanation contract.
 */
class SearchIntentParserTest {

    @Test
    fun testUntagged() {
        val result = SearchIntentParser.parse("untagged")
        assertEquals("!#", result.rawQuery)
        assertTrue(result.canBuild)
        assertTrue(result.tokens.isEmpty())
        assertEquals(listOf("#"), result.exclusions)
    }

    @Test
    fun testCleanup() {
        val result = SearchIntentParser.parse("cleanup")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("1*"))
    }

    @Test
    fun testTrade() {
        val result = SearchIntentParser.parse("trade")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("count2-"))
        assertTrue(result.exclusions.contains("traded"))
    }

    @Test
    fun testPvp() {
        val result = SearchIntentParser.parse("pvp")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("0-1attack"))
        assertTrue(result.tokens.contains("3-4defense"))
        assertTrue(result.tokens.contains("3-4hp"))
    }

    @Test
    fun testGreatLeaguePvp() {
        val result = SearchIntentParser.parse("great league pvp")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("0-1attack"))
        assertTrue(result.tokens.contains("3-4defense"))
        assertTrue(result.tokens.contains("3-4hp"))
        assertTrue(result.tokens.contains("cp-1500"))
    }

    @Test
    fun testUltraLeaguePvp() {
        val result = SearchIntentParser.parse("ultra league pvp")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("0-1attack"))
        assertTrue(result.tokens.contains("3-4defense"))
        assertTrue(result.tokens.contains("3-4hp"))
        assertTrue(result.tokens.contains("cp-2500"))
    }

    @Test
    fun testLuckyTrade() {
        val result = SearchIntentParser.parse("lucky trade")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("age365-"))
        assertTrue(result.exclusions.contains("traded"))
    }

    @Test
    fun testTurkishIntentPhrases() {
        assertTrue(SearchIntentParser.parse("parlak pokemon bul").tokens.contains("shiny"))
        assertTrue(SearchIntentParser.parse("güçlü pokemon bul").tokens.contains("4*"))
        assertTrue(SearchIntentParser.parse("şanslı takas").tokens.contains("age365-"))
        assertTrue(SearchIntentParser.parse("etiketlenmemiş pokemon").exclusions.contains("#"))
        assertTrue(SearchIntentParser.parse("pvp adayı").tokens.contains("0-1attack"))
    }

    @Test
    fun testShinyLegendary() {
        val result = SearchIntentParser.parse("shiny legendary")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("shiny"))
        assertTrue(result.tokens.contains("legendary"))
    }

    @Test
    fun testCandyPrep() {
        val result = SearchIntentParser.parse("candy prep")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("count2-"))
    }

    @Test
    fun testNundo() {
        val result = SearchIntentParser.parse("nundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("0attack"))
        assertTrue(result.tokens.contains("0defense"))
        assertTrue(result.tokens.contains("0hp"))
    }

    @Test
    fun testHundo() {
        val result = SearchIntentParser.parse("hundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        // Check explanation/limitations contain exact or 4*
        assertTrue(result.explanation.contains("exact 100%") || result.explanation.contains("perfect 15/15/15"))
    }

    @Test
    fun testAllPokemon() {
        val result = SearchIntentParser.parse("all pokemon")
        assertFalse(result.canBuild)
    }

    @Test
    fun testHideShinyAndFavourites() {
        val result = SearchIntentParser.parse("hide shiny and favourites")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertTrue(result.exclusions.contains("favorite"))
        assertFalse(result.tokens.contains("shiny"))
        assertFalse(result.tokens.contains("favorite"))
        assertEquals("!shiny&!favorite", result.rawQuery)
    }

    @Test
    fun testShadowPokemonForTrade() {
        val result = SearchIntentParser.parse("shadow pokemon for trade")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("shadow"))
        assertTrue(result.limitations.any { it.contains("cannot be traded", ignoreCase = true) })
    }

    @Test
    fun testPurifiedGuaranteedLuckyGreatLeague() {
        val result = SearchIntentParser.parse("purified pokemon that may be good for guaranteed lucky trade and have Great League IVs")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("purified"))
        assertTrue(result.tokens.contains("age365-"))
        assertTrue(result.tokens.contains("0-1attack"))
        assertTrue(result.tokens.contains("cp-1500"))
        assertTrue(result.exclusions.contains("traded"))
        assertTrue(result.limitations.isNotEmpty())
    }

    @Test
    fun testNoRawQueryContainsInvalidFormatting() {
        val inputs = listOf(
            "untagged", "cleanup", "trade", "pvp", "shiny legendary", "hundo",
            "nundo", "candy prep", "lucky trade", "old", "distance", "shadow",
            "purified", "lucky", "costume", "favorite", "mythical", "legendary",
            "great league pvp", "ultra league pvp"
        )
        for (input in inputs) {
            val result = SearchIntentParser.parse(input)
            if (result.canBuild) {
                assertFalse("'$input' rawQuery should not contain '!&!#': ${result.rawQuery}",
                    result.rawQuery.contains("!&!#"))
                assertFalse("'$input' rawQuery should not contain '&&': ${result.rawQuery}",
                    result.rawQuery.contains("&&"))
                assertFalse("'$input' rawQuery should not start with '&': ${result.rawQuery}",
                    result.rawQuery.startsWith("&"))
                assertFalse("'$input' rawQuery should not end with '&': ${result.rawQuery}",
                    result.rawQuery.endsWith("&"))
            }
        }
    }

    @Test
    fun testFindHundosAndExcludeShinies() {
        val result = SearchIntentParser.parse("Find hundos and exclude shinies")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testExcludeShiniesAndFindHundos() {
        val result = SearchIntentParser.parse("exclude shinies and find hundos")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    // PQ-ZAI-FINAL-01: Negated controls
    @Test
    fun testDontHideShiny() {
        val result = SearchIntentParser.parse("don't hide shiny")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("shiny"))
        assertFalse(result.exclusions.contains("shiny"))
        assertEquals("shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testDontHideShinyVariants() {
        val r1 = SearchIntentParser.parse("dont hide shiny")
        assertTrue(r1.canBuild)
        assertTrue(r1.tokens.contains("shiny"))
        assertFalse(r1.exclusions.contains("shiny"))
        assertEquals("shiny", r1.rawQuery)

        val r2 = SearchIntentParser.parse("do not hide shiny")
        assertTrue(r2.canBuild)
        assertTrue(r2.tokens.contains("shiny"))
        assertFalse(r2.exclusions.contains("shiny"))
        assertEquals("shiny", r2.rawQuery)

        val r3 = SearchIntentParser.parse("don't exclude shiny")
        assertTrue(r3.canBuild)
        assertTrue(r3.tokens.contains("shiny"))
        assertFalse(r3.exclusions.contains("shiny"))
        assertEquals("shiny", r3.rawQuery)
    }

    @Test
    fun testDontIncludeShiny() {
        val result = SearchIntentParser.parse("don't include shiny")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.tokens.contains("shiny"))
        assertEquals("!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))

        val r2 = SearchIntentParser.parse("don't show shiny")
        assertTrue(r2.canBuild)
        assertTrue(r2.exclusions.contains("shiny"))
        assertFalse(r2.tokens.contains("shiny"))
        assertEquals("!shiny", r2.rawQuery)
    }

    @Test
    fun testNotShiny() {
        val result = SearchIntentParser.parse("not shiny")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.tokens.contains("shiny"))
        assertEquals("!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    // PQ-ZAI-FINAL-02: Contrast with explicit positive control
    @Test
    fun testWithoutShinyButWithHundo() {
        val result = SearchIntentParser.parse("without shiny but with hundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    // PQ-ZAI-FINAL-03: Contrast polarity inheritance
    @Test
    fun testShowAllButHundos() {
        val result = SearchIntentParser.parse("show all but hundos")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("4*"))
        assertFalse(result.tokens.contains("4*"))
        assertEquals("!4*", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testFindAllButShiny() {
        val result = SearchIntentParser.parse("find all but shiny")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.tokens.contains("shiny"))
        assertEquals("!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testEverythingButShinyAndLegendary() {
        val result = SearchIntentParser.parse("everything but shiny and legendary")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertTrue(result.exclusions.contains("legendary"))
        assertTrue(result.tokens.isEmpty())
        assertEquals("!shiny&!legendary", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testShowAllButShinyAndHundos() {
        val result = SearchIntentParser.parse("show all but shiny and hundos")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertTrue(result.exclusions.contains("4*"))
        assertTrue(result.tokens.isEmpty())
        assertTrue(result.rawQuery == "!4*&!shiny" || result.rawQuery == "!shiny&!4*")
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testHideShinyButHundo() {
        val result = SearchIntentParser.parse("hide shiny but hundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testFindHundoButExcludeShiny() {
        val result = SearchIntentParser.parse("find hundo but exclude shiny")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testExcludeShinyButFindHundo() {
        val result = SearchIntentParser.parse("exclude shiny but find hundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    // ==========================================
    // CAUGHT DATE INTENT TESTS (Fixed today: 2026-08-18)
    // ==========================================
    private val fixedToday = LocalDate.of(2026, 8, 18)

    @Test
    fun testCaughtInApril2025_English() {
        val r1 = SearchIntentParser.parse("find pokemon caught in April 2025", fixedToday)
        assertTrue(r1.canBuild)
        assertEquals("year2025&age475-504", r1.rawQuery)
        assertFalse(r1.rawQuery.contains("|"))
        assertEquals(listOf("year2025", "age475-504"), r1.tokens)
        assertTrue(r1.limitations.any { it.contains("rolling 24-hour windows", ignoreCase = true) })

        val r2 = SearchIntentParser.parse("caught in April 2025", fixedToday)
        assertTrue(r2.canBuild)
        assertEquals("year2025&age475-504", r2.rawQuery)
    }

    @Test
    fun testCaughtInApril2025_Turkish() {
        val r1 = SearchIntentParser.parse("nisan 2025te yakalanan pokemonları bul", fixedToday)
        assertTrue(r1.canBuild)
        assertEquals("year2025&age475-504", r1.rawQuery)
        assertFalse(r1.rawQuery.contains("|"))

        val r2 = SearchIntentParser.parse("nisan 2025'te yakalanan pokemonları bul", fixedToday)
        assertTrue(r2.canBuild)
        assertEquals("year2025&age475-504", r2.rawQuery)
    }

    @Test
    fun testCaughtInApril_MonthOnly() {
        val rEn = SearchIntentParser.parse("caught in April", fixedToday)
        assertTrue(rEn.canBuild)
        assertEquals("year2026&age110-139", rEn.rawQuery)
        assertFalse(rEn.rawQuery.contains("|"))

        val rTr1 = SearchIntentParser.parse("nisanda yakalanan pokemonları bul", fixedToday)
        assertTrue(rTr1.canBuild)
        assertEquals("year2026&age110-139", rTr1.rawQuery)

        val rTr2 = SearchIntentParser.parse("nisan ayında yakalanan pokemonları bul", fixedToday)
        assertTrue(rTr2.canBuild)
        assertEquals("year2026&age110-139", rTr2.rawQuery)
    }

    @Test
    fun testCaughtIn2025_YearOnly() {
        val rEn = SearchIntentParser.parse("caught in 2025", fixedToday)
        assertTrue(rEn.canBuild)
        assertEquals("year2025", rEn.rawQuery)
        assertFalse(rEn.rawQuery.contains("|"))

        val rTr1 = SearchIntentParser.parse("2025te yakalanan pokemonları bul", fixedToday)
        assertTrue(rTr1.canBuild)
        assertEquals("year2025", rTr1.rawQuery)

        val rTr2 = SearchIntentParser.parse("2025'te yakalanan pokemonları bul", fixedToday)
        assertTrue(rTr2.canBuild)
        assertEquals("year2025", rTr2.rawQuery)
    }

    @Test
    fun testBareCaughtRequest() {
        val rEn = SearchIntentParser.parse("find caught pokemon", fixedToday)
        assertFalse(rEn.canBuild)
        assertEquals("", rEn.rawQuery)
        assertTrue(rEn.explanation.contains("caught in April 2025", ignoreCase = true) || rEn.explanation.contains("Specify a month", ignoreCase = true))

        val rTr = SearchIntentParser.parse("yakalanan pokemonları bul", fixedToday)
        assertFalse(rTr.canBuild)
        assertEquals("", rTr.rawQuery)
        assertTrue(rTr.explanation.contains("Nisan 2025'te yakalanan", ignoreCase = true) || rTr.explanation.contains("Bir ay veya yıl belirtin", ignoreCase = true))
    }

    @Test
    fun testCaughtDateComposition() {
        // 1. find shiny pokemon caught in April 2025
        val rShinyDate = SearchIntentParser.parse("find shiny pokemon caught in April 2025", fixedToday)
        assertTrue(rShinyDate.canBuild)
        assertEquals("year2025&age475-504&shiny", rShinyDate.rawQuery)
        assertFalse(rShinyDate.rawQuery.contains("|"))

        // 2. find hundos caught in 2025
        val rHundoDate = SearchIntentParser.parse("find hundos caught in 2025", fixedToday)
        assertTrue(rHundoDate.canBuild)
        assertEquals("year2025&4*", rHundoDate.rawQuery)
        assertFalse(rHundoDate.rawQuery.contains("|"))

        // 3. find legendary pokemon caught in April
        val rLegDate = SearchIntentParser.parse("find legendary pokemon caught in April", fixedToday)
        assertTrue(rLegDate.canBuild)
        assertEquals("year2026&age110-139&legendary", rLegDate.rawQuery)
        assertFalse(rLegDate.rawQuery.contains("|"))

        // 4. Turkish: nisan 2025te yakalanan parlak pokemonları bul
        val rTrShiny = SearchIntentParser.parse("nisan 2025te yakalanan parlak pokemonları bul", fixedToday)
        assertTrue(rTrShiny.canBuild)
        assertEquals("year2025&age475-504&shiny", rTrShiny.rawQuery)
        assertFalse(rTrShiny.rawQuery.contains("|"))

        // 5. exclude shiny pokemon caught in 2025
        val rExclShiny = SearchIntentParser.parse("exclude shiny pokemon caught in 2025", fixedToday)
        assertTrue(rExclShiny.canBuild)
        assertEquals("year2025&!shiny", rExclShiny.rawQuery)
        assertFalse(rExclShiny.rawQuery.contains("|"))

        // 6. find hundos caught in April 2025 and exclude shiny
        val rHundoExclShiny = SearchIntentParser.parse("find hundos caught in April 2025 and exclude shiny", fixedToday)
        assertTrue(rHundoExclShiny.canBuild)
        assertEquals("year2025&age475-504&4*&!shiny", rHundoExclShiny.rawQuery)
        assertFalse(rHundoExclShiny.rawQuery.contains("|"))

        // 7. find shadow pokemon caught in 2025
        val rShadowDate = SearchIntentParser.parse("find shadow pokemon caught in 2025", fixedToday)
        assertTrue(rShadowDate.canBuild)
        assertEquals("year2025&shadow", rShadowDate.rawQuery)
        assertFalse(rShadowDate.rawQuery.contains("|"))

        // 8. 2018 collision: find shiny pokemon caught in 2018 must NOT contain age365-
        val r2018 = SearchIntentParser.parse("find shiny pokemon caught in 2018", fixedToday)
        assertTrue(r2018.canBuild)
        assertEquals("year2018&shiny", r2018.rawQuery)
        assertFalse(r2018.rawQuery.contains("age365-"))
        assertFalse(r2018.rawQuery.contains("|"))

        // 9. find old shiny pokemon MUST contain age365-
        val rOldShiny = SearchIntentParser.parse("find old shiny pokemon", fixedToday)
        assertTrue(rOldShiny.canBuild)
        assertEquals("age365-&shiny", rOldShiny.rawQuery)
    }

    @Test
    fun testCaughtDateEdgeCases() {
        // Current month: August 2026 (clamped to today: Aug 18)
        val rCurrent = SearchIntentParser.parse("caught in August 2026", fixedToday)
        assertTrue(rCurrent.canBuild)
        assertEquals("year2026&age0-17", rCurrent.rawQuery)

        // Month-only December (inferred past year: Dec 2025)
        val rDec = SearchIntentParser.parse("caught in December", fixedToday)
        assertTrue(rDec.canBuild)
        assertEquals("year2025&age230-260", rDec.rawQuery)

        // Future year
        val rFuture = SearchIntentParser.parse("caught in 2030", fixedToday)
        assertFalse(rFuture.canBuild)
        assertEquals("", rFuture.rawQuery)

        // Leap year: Feb 2024 (29 days)
        val rLeap = SearchIntentParser.parse("caught in February 2024", fixedToday)
        assertTrue(rLeap.canBuild)
        assertEquals("year2024&age901-929", rLeap.rawQuery)
    }

    @Test
    fun testAll12MonthsRecognition() {
        val enMonths = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        for (m in enMonths) {
            val res = SearchIntentParser.parse("caught in $m 2024", fixedToday)
            assertTrue("Expected buildable for $m 2024", res.canBuild)
            assertTrue("Expected year2024 for $m 2024", res.rawQuery.startsWith("year2024&age"))
            assertFalse(res.rawQuery.contains("|"))
        }

        val trMonths = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
        for (m in trMonths) {
            val res = SearchIntentParser.parse("$m 2024'te yakalanan", fixedToday)
            assertTrue("Expected buildable for Turkish $m 2024", res.canBuild)
            assertTrue("Expected year2024 for Turkish $m 2024", res.rawQuery.startsWith("year2024&age"))
            assertFalse(res.rawQuery.contains("|"))
        }
    }

    @Test
    fun testSearchTermMapperLanguageTranslationsForCaughtDate() {
        val query = "year2025&age475-504&shiny"
        assertEquals("yıl2025&yaş475-504&parlak", com.caglar.pokequery.domain.engine.SearchTermMapper.translateSyntax(query, "Turkish"))
        assertEquals("jahr2025&alter475-504&schillernd", com.caglar.pokequery.domain.engine.SearchTermMapper.translateSyntax(query, "German"))
        assertEquals("año2025&edad475-504&variocolor", com.caglar.pokequery.domain.engine.SearchTermMapper.translateSyntax(query, "Spanish"))
        assertEquals("année2025&âge475-504&chromatique", com.caglar.pokequery.domain.engine.SearchTermMapper.translateSyntax(query, "French"))
        assertEquals("anno2025&età475-504&cromatico", com.caglar.pokequery.domain.engine.SearchTermMapper.translateSyntax(query, "Italian"))
    }
}