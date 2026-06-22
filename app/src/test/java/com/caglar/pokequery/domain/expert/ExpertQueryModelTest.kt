package com.caglar.pokequery.domain.expert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Package v0.5.0 Expert Builder — modular query model.
 *
 * The chip-based builder composes a raw query from positive tokens, IV floors, numeric
 * ranges, and exclusions. The produced string is fed to the EXISTING Linter /
 * ExpertCopyPolicy / StringBuilderEngine — safety behavior is unchanged.
 */
class ExpertQueryModelTest {

    @Test
    fun `empty model produces empty query`() {
        assertEquals("", ExpertQueryModel().buildRawQuery())
    }

    @Test
    fun `adding a shiny token produces shiny`() {
        val model = ExpertQueryModel(positiveTokens = setOf("shiny"))
        assertEquals("shiny", model.buildRawQuery())
    }

    @Test
    fun `multiple positive tokens joined by comma (OR)`() {
        val model = ExpertQueryModel(positiveTokens = setOf("shiny", "legendary"))
        // OR within a group; order is stable via sorted() for determinism.
        assertEquals("legendary,shiny", model.buildRawQuery())
    }

    @Test
    fun `exclusions are appended with and and bang`() {
        val model = ExpertQueryModel(positiveTokens = setOf("4*"), exclusions = setOf("shiny", "traded"))
        assertEquals("4*&!shiny&!traded", model.buildRawQuery())
    }

    @Test
    fun `iv floor attack 0 produces 0attack`() {
        val model = ExpertQueryModel(ivAttackFloor = 0)
        assertEquals("0attack", model.buildRawQuery())
    }

    @Test
    fun `iv floors combine with ampersand`() {
        val model = ExpertQueryModel(ivAttackFloor = 0, ivDefenseFloor = 15)
        assertEquals("0attack&15defense", model.buildRawQuery())
    }

    @Test
    fun `count floor produces countN minus`() {
        val model = ExpertQueryModel(countFloor = 2)
        assertEquals("count2-", model.buildRawQuery())
    }

    @Test
    fun `complex model combines tokens iv floors count and exclusions`() {
        val model = ExpertQueryModel(
            positiveTokens = setOf("shiny"),
            ivAttackFloor = 0,
            countFloor = 2,
            exclusions = setOf("traded")
        )
        // shiny & 0attack & count2- & !traded
        val q = model.buildRawQuery()
        assertTrue(q.contains("shiny"))
        assertTrue(q.contains("0attack"))
        assertTrue(q.contains("count2-"))
        assertTrue(q.contains("!traded"))
    }

    @Test
    fun `toggle exclusion adds and removes`() {
        var model = ExpertQueryModel(positiveTokens = setOf("4*"))
        model = model.toggleExclusion("shiny")
        assertTrue(model.exclusions.contains("shiny"))
        model = model.toggleExclusion("shiny")
        assertFalse(model.exclusions.contains("shiny"))
    }

    @Test
    fun `toggle positive token adds and removes`() {
        var model = ExpertQueryModel()
        model = model.togglePositive("shiny")
        assertTrue(model.positiveTokens.contains("shiny"))
        model = model.togglePositive("shiny")
        assertFalse(model.positiveTokens.contains("shiny"))
    }

    // v0.5.1 (Fix 6/7): deterministic preview string for the richer grouped option set.
    // New fields (age, distance, IV defense/hp) join the output in a stable order so the
    // live preview is reproducible.

    @Test
    fun `iv defense and hp floors appear in deterministic order`() {
        val model = ExpertQueryModel(ivAttackFloor = 0, ivDefenseFloor = 15, ivHpFloor = 15)
        assertEquals("0attack&15defense&15hp", model.buildRawQuery())
    }

    @Test
    fun `age and distance filters are appended after count`() {
        val model = ExpertQueryModel(countFloor = 2, age365 = true, distance100 = true)
        assertEquals("count2-&age365-&distance100-", model.buildRawQuery())
    }

    @Test
    fun `setters toggle age and distance`() {
        val on = ExpertQueryModel().setAge(true).setDistance(true)
        assertEquals("age365-&distance100-", on.buildRawQuery())
        val off = on.setAge(false).setDistance(false)
        assertEquals("", off.buildRawQuery())
    }

    @Test
    fun `full grouped selection produces deterministic preview`() {
        // Positives (sorted) -> iv floors -> count -> age -> distance -> exclusions (sorted).
        val model = ExpertQueryModel(
            positiveTokens = setOf("lucky", "shiny"),
            ivAttackFloor = 1,
            ivDefenseFloor = 3,
            ivHpFloor = 3,
            countFloor = 2,
            age365 = true,
            exclusions = setOf("traded")
        )
        assertEquals("lucky,shiny&1attack&3defense&3hp&count2-&age365-&!traded", model.buildRawQuery())
    }
}
