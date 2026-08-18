package com.caglar.pokequery.domain.expert

import com.caglar.pokequery.domain.engine.StringBuilderEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpertOfficialCoverageTest {

    @Test
    fun `values inside one official family use OR`() {
        val model = ExpertQueryModel()
            .toggleFilter("region", "kanto")
            .toggleFilter("region", "johto")
        assertEquals("johto,kanto", model.buildRawQuery())
    }

    @Test
    fun `different official families use AND`() {
        val model = ExpertQueryModel(positiveTokens = setOf("shiny"))
            .toggleFilter("region", "kanto")
            .toggleFilter("size", "xxl")
        assertEquals("shiny&kanto&xxl", model.buildRawQuery())
    }

    @Test
    fun `expert shiny intent is not silently contradicted by cleanup protections`() {
        val generated = StringBuilderEngine.buildGoal("expert", customQuery = "shiny", language = "English")
        assertEquals("shiny", generated.rawSyntax)
        assertFalse(generated.rawSyntax.contains("!shiny"))
    }

    @Test
    fun `expert count query still receives mandatory destructive-action protections`() {
        val generated = StringBuilderEngine.buildGoal("expert", customQuery = "count2-", language = "English")
        assertTrue(generated.rawSyntax.contains("count2-"))
        assertTrue(generated.rawSyntax.contains("!shiny"))
        assertTrue(generated.rawSyntax.contains("!traded"))
        assertTrue(generated.rawSyntax.contains("!background"))
    }
}
