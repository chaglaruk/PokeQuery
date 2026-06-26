package com.caglar.pokequery.data.repository

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class KnowledgeBaseMisconceptionsTest {

    private val json by lazy {
        listOf(
            File("app/src/main/assets/knowledgebase.json"),
            File("src/main/assets/knowledgebase.json")
        ).first { it.exists() }.readText()
    }

    @Test
    fun `common misconceptions category and required entries exist`() {
        assertTrue(json.contains("\"category\": \"Common Misconceptions\""))
        listOf(
            "misconception_pipe_operator",
            "misconception_or_separator",
            "misconception_count_caveats",
            "misconception_turkish_beta",
            "misconception_background_ultrabeast",
            "misconception_inspection_vs_action"
        ).forEach { id ->
            assertTrue("Missing $id", json.contains("\"id\": \"$id\""))
            val entryStart = json.indexOf("\"id\": \"$id\"")
            val entryEnd = json.indexOf("\n  }", entryStart).takeIf { it > entryStart } ?: json.length
            val entry = json.substring(entryStart, entryEnd)
            assertTrue("$id missing title", entry.contains("\"title\":"))
            assertTrue("$id missing body", entry.contains("\"description_en\":"))
            assertTrue("$id missing category", entry.contains("\"category\": \"Common Misconceptions\""))
        }
    }

    @Test
    fun `misconception copy matches engine safety stance`() {
        assertTrue(json.contains("does not treat `|` as a safe OR operator"))
        assertTrue(json.contains("supported OR separator is the comma"))
        assertTrue(json.contains("Turkish `count` stays English"))
        assertTrue(json.contains("Search String Language: Turkish is beta"))
        assertTrue(json.contains("may remain English in generated strings for safety"))
        assertTrue(json.contains("RiskWarning is required"))
    }
}
