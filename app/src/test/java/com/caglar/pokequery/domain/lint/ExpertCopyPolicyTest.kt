package com.caglar.pokequery.domain.lint

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression tests for Expert Builder safety policy. */
class ExpertCopyPolicyTest {

    @Test
    fun `pipe operator blocks copy`() {
        assertFalse(ExpertCopyPolicy.canCopy("shiny|lucky"))
    }

    @Test
    fun `unsafe bare count blocks copy`() {
        assertFalse(ExpertCopyPolicy.canCopy("count"))
    }

    @Test
    fun `empty query does not block copy`() {
        assertTrue(ExpertCopyPolicy.canCopy(""))
    }

    @Test
    fun `advisory-only warnings do not block copy`() {
        assertTrue(ExpertCopyPolicy.canCopy("0*"))
    }

    @Test
    fun `clean safe query does not block copy`() {
        assertTrue(ExpertCopyPolicy.canCopy("4*&!shiny"))
    }

    @Test
    fun `lucky and traded positive filters do not block copy`() {
        assertTrue(ExpertCopyPolicy.canCopy("lucky,traded"))
        assertTrue(ExpertCopyPolicy.canCopy("lucky&traded"))
    }

    @Test
    fun `advisory risky positive filter does not block copy`() {
        assertTrue(ExpertCopyPolicy.canCopy("shiny"))
        assertTrue(ExpertCopyPolicy.canCopy("legendary"))
    }

    @Test
    fun `true safety errors block copy`() {
        assertFalse(ExpertCopyPolicy.canCopy("shiny|lucky"))
        assertFalse(ExpertCopyPolicy.canCopy("count2-"))
        assertFalse(ExpertCopyPolicy.canCopy("count2-&shiny"))
    }
}
