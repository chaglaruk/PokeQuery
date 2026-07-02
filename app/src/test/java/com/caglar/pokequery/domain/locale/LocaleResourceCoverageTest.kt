package com.caglar.pokequery.domain.locale

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocaleResourceCoverageTest {
    private val stringName = Regex("""<string name="([^"]+)">""")

    
    @Test
    fun `supported locales define every default string key`() {
        val defaultKeys = keys("src/main/res/values/strings.xml")
        listOf("values-tr", "values-de", "values-es", "values-fr", "values-it").forEach { dir ->
            assertEquals("$dir must match default strings", defaultKeys, keys("src/main/res/$dir/strings.xml"))
        }
    }

    
    @Test
    fun `non english locale files have no obvious mixed or broken text`() {
        listOf("values-tr", "values-de", "values-es", "values-fr", "values-it").forEach { dir ->
            val text = File("src/main/res/$dir/strings.xml").readText(Charsets.UTF_8)
            listOf(
                "Ãœbersetzt:",
                "Traducido:",
                "Traduit :",
                "Tradotto:",
                "Sprache ?ndern",
                "App Sprache applies",
                "applies to this app nur",
                "During Community Day",
                "During Candy transfer",
                "manual app note",
                "\\?",
                "Coming later",
                "Turco search tokens are BETA",
                "T\\?rkisch search tokens are BETA"
            ).forEach { marker ->
                assertFalse("$dir contains broken marker $marker", text.contains(marker))
            }
            listOf(
                "Describe what",
                "Phase 5 â€” Localization",
                "permission",
                "offline event context",
                "login, tracking",
                "ads, analytics",
                "Count ",
                "Open the search",
                "Search String",
                "System Default",
                "only creates text",
                "solo creates text",
                "seulement creates text",
                "Turco search",
                "Turc search",
                "Tester notes"
            ).forEach { marker ->
                assertFalse("$dir contains mixed-language marker $marker", text.contains(marker))
            }
            val englishFallbackMarker = "[" + "EN" + "]"
            assertFalse("$dir contains English fallback marker text", text.contains(englishFallbackMarker))
            listOf("\u00C3\u0192", "\u00C3\u2026", "\u00C3\u00A2\u00E2\u201A\u00AC", "\u00C3\u201A\u00C2\u00BF", "\u0080", "\u009F", "\uFFFD").forEach { marker ->
                assertFalse("$dir contains mojibake marker $marker", text.contains(marker))
            }
            assertFalse(
                "$dir contains replacement-question text",
                Regex("""\?[\p{L}]|[\p{L}]\?[\p{L}]""").containsMatchIn(
                    text.lineSequence().filterNot { it.startsWith("<?xml") }.joinToString("\n")
                )
            )
        }
    }

    
    @Test
    fun `v069 event guide main card strings exist in all locales`() {
        val eventGuideKeys = listOf(
            "event_main_card_title",
            "event_featured_pokemon",
            "event_boosted_pokemon",
            "event_bonuses",
            "event_raids",
            "event_research",
            "event_whats_happening",
            "event_why_care",
            "event_what_to_do",
            "event_keep_review",
            "event_avoid_transfer",
            "event_check_before",
            "event_no_events_title",
            "event_no_events_desc",
            "event_main_card_live_now",
            "event_main_card_coming_up",
            "event_main_card_ended",
            "event_suggested_for_event"
        )
        listOf("values", "values-tr", "values-de", "values-es", "values-fr", "values-it").forEach { dir ->
            val localeKeys = keys("src/main/res/$dir/strings.xml")
            eventGuideKeys.forEach { key ->
                assertTrue("$dir missing key $key", localeKeys.contains(key))
            }
        }
    }

    @Test
    fun `v069 localized risk labels exist in all locales`() {
        val riskKeys = listOf(
            "risk_low_display",
            "risk_medium_display",
            "risk_info_display",
            "risk_high_display",
            "risk_low_subtitle",
            "risk_medium_subtitle",
            "risk_info_subtitle",
            "risk_high_subtitle",
            "goal_detail_about_count",
            "goal_detail_what_does_this_do"
        )
        listOf("values", "values-tr", "values-de", "values-es", "values-fr", "values-it").forEach { dir ->
            val localeKeys = keys("src/main/res/$dir/strings.xml")
            riskKeys.forEach { key ->
                assertTrue("$dir missing key $key", localeKeys.contains(key))
            }
        }
    }

    @Test
    fun `knowledge tier risk accepts string tier value`() {
        listOf("values", "values-tr", "values-de", "values-es", "values-fr", "values-it").forEach { dir ->
            val text = File("src/main/res/$dir/strings.xml").readText(Charsets.UTF_8)
            val match = Regex("""<string name="knowledge_tier_risk">([^<]+)</string>""").find(text)
            assertTrue("$dir missing knowledge_tier_risk", match != null)
            assertFalse("$dir must not format tier as integer", match!!.groupValues[1].contains("%2\$d"))
            assertTrue("$dir must format tier as string", match.groupValues[1].contains("%2\$s"))
        }
    }

    private fun keys(path: String): Set<String> =
        stringName.findAll(File(path).readText(Charsets.UTF_8)).map { it.groupValues[1] }.toSet()
}

