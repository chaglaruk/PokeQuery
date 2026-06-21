package com.caglar.pokequery.data.repository

import com.caglar.pokequery.data.model.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Package 8 — Knowledge Base metadata parsing (instrumented, real org.json runtime).
 *
 * Mirrors the existing KnowledgeBaseRepositoryTest in androidTest because the parser
 * uses org.json, which needs a real Android runtime (unit-test stubs throw).
 */
class KnowledgeBaseMetadataTest {

    @Test
    fun parse_reads_verification_safety_and_language_metadata_when_present() {
        val json = """
            [{
              "id": "status_shiny",
              "syntax": "shiny",
              "category": "Status",
              "tier": "T1",
              "description_en": "Shiny.",
              "riskLevel": "High",
              "sourceUrl": "https://niantic.helpshift.com/",
              "lastVerified": "2026-06-21",
              "verificationStatus": "verified",
              "safetyLevel": "risky",
              "languageSensitive": true,
              "example": "shiny&4*",
              "commonMistake": "Shinies are valuable; do not include in cleanup."
            }]
        """.trimIndent()

        val term = KnowledgeBaseRepository.parse(json).single()
        assertEquals(VerificationStatus.VERIFIED, term.verificationStatus)
        assertEquals("risky", term.safetyLevel)
        assertEquals(true, term.languageSensitive)
        assertEquals("shiny&4*", term.example)
        assertEquals("Shinies are valuable; do not include in cleanup.", term.commonMistake)
    }

    @Test
    fun missing_metadata_defaults_to_needs_verification_and_nulls() {
        val json = """
            [{
              "id": "counter_count",
              "syntax": "count[N]",
              "category": "Counter",
              "tier": "T2",
              "description_en": "Count.",
              "riskLevel": "High",
              "sourceUrl": "https://pokemongohub.net/",
              "lastVerified": "2026-06-21"
            }]
        """.trimIndent()

        val term = KnowledgeBaseRepository.parse(json).single()
        assertEquals(VerificationStatus.NEEDS_VERIFICATION, term.verificationStatus)
        assertNull(term.safetyLevel)
        assertNull(term.languageSensitive)
        assertNull(term.example)
        assertNull(term.commonMistake)
    }

    @Test
    fun beta_verification_status_parses() {
        val json = """
            [{
              "id": "status_traded",
              "syntax": "traded",
              "category": "Status",
              "tier": "T1",
              "description_en": "Traded.",
              "riskLevel": "Medium",
              "sourceUrl": "x",
              "lastVerified": "2026-06-21",
              "verificationStatus": "beta"
            }]
        """.trimIndent()
        val term = KnowledgeBaseRepository.parse(json).single()
        assertEquals(VerificationStatus.BETA, term.verificationStatus)
    }

    @Test
    fun invalid_verification_status_falls_back_to_needs_verification() {
        val json = """
            [{
              "id": "x",
              "syntax": "x",
              "category": "X",
              "tier": "T1",
              "description_en": "x",
              "riskLevel": "Low",
              "sourceUrl": "x",
              "lastVerified": "2026-06-21",
              "verificationStatus": "garbage_value"
            }]
        """.trimIndent()
        val term = KnowledgeBaseRepository.parse(json).single()
        assertEquals(VerificationStatus.NEEDS_VERIFICATION, term.verificationStatus)
    }

    @Test
    fun shipped_knowledgebase_loads_with_metadata_every_term_has_a_status() {
        val result = KnowledgeBaseRepository(androidx.test.core.app.ApplicationProvider.getApplicationContext()).load()
        assertTrue(result.isSuccess)
        result.getOrThrow().forEach { term ->
            // No term may ever have a null verification status (default is always applied).
            assertTrue("null verification status for ${term.id}", term.verificationStatus != null)
        }
    }
}
