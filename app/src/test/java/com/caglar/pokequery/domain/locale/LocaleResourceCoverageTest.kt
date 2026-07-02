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
            assertFalse("$dir contains [EN] fallback text", text.contains("[EN]"))
            listOf("Ãƒ", "Ã…", "Ã¢â‚¬", "Ã‚Â¿", "\u0080", "\u009F", "\uFFFD").forEach { marker ->
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

