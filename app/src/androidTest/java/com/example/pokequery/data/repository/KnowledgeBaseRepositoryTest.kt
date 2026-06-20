package com.example.pokequery.data.repository

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeBaseRepositoryTest {
    @Test
    fun expandedKnowledgeBaseLoadsSafely() {
        val result = KnowledgeBaseRepository(ApplicationProvider.getApplicationContext()).load()
        assertTrue(result.isSuccess)
        val syntax = result.getOrThrow().map { it.syntax }.toSet()
        listOf(
            "&", ",", "!", "cp[N]", "0*", "4*", "costume", "!#", "#[tag]",
            "remoteraid", "tradeevolve", "@special", "adventureeffect", "gigantamax", "maxspirit[N]"
        ).forEach { assertTrue("Missing $it", it in syntax) }
    }

    @Test
    fun brokenJsonReturnsFailureInsteadOfCrashingUi() {
        assertTrue(runCatching { KnowledgeBaseRepository.parse("{broken") }.isFailure)
    }
}
