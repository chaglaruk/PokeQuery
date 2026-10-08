package com.caglar.pokequery.domain.lint

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeCopyPolicyTest {
    @Test
    fun `reference templates and pipe cannot enter clipboard`() {
        listOf("cp[N]", "attack[0-4]", "#[tag]", "@[type]", "|", "shiny|legendary", "cp<N>", "", " ")
            .forEach { assertFalse(it, KnowledgeCopyPolicy.canCopy(it)) }
    }

    @Test
    fun `concrete tokens remain copyable without rewriting their syntax`() {
        listOf("shiny", "!favorite", "cp1500", "count2-", "@fire", "#keep", "&", ",")
            .forEach { assertTrue(it, KnowledgeCopyPolicy.canCopy(it)) }
    }
}
