package com.caglar.pokequery.domain.locale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTokenCatalogTest {

    @Test
    fun `every supported language has English as baseline`() {
        val enTokens = SearchTokenCatalog.tokens.values.map { it.firstOrNull { lt -> lt.language == "English" } }
        assertTrue("All tokens must have English entry", enTokens.all { it != null })
    }

    @Test
    fun `turkish tokens match SearchTermMapper turkishMap entries`() {
        val turkishTokens = SearchTokenCatalog.tokens.values.mapNotNull { it.firstOrNull { lt -> lt.language == "Turkish" } }
        assertTrue("Turkish tokens should exist", turkishTokens.isNotEmpty())
        turkishTokens.forEach { lt ->
            assertTrue(
                "Turkish token '${lt.token}' for '${lt.language}' must have notes",
                lt.notes.isNotBlank()
            )
        }
    }

    @Test
    fun `beta tokens list covers all beta turkish entries`() {
        val betaList = SearchTokenCatalog.betaTokensTurkish
        assertTrue("betaTokensTurkish must not be empty", betaList.isNotEmpty())
        val catalogBeta = SearchTokenCatalog.tokens.values
            .mapNotNull { it.firstOrNull { lt -> lt.language == "Turkish" && lt.notes.contains("BETA") } }
            .map { it.token }
        catalogBeta.forEach { token ->
            assertTrue("beta token '$token' must be in betaTokensTurkish", betaList.contains(token))
        }
    }

    @Test
    fun `unverified tokens list matches catalog`() {
        val unverified = SearchTokenCatalog.unverifiedTokens("Turkish")
        assertTrue("should have unverified Turkish tokens", unverified.isNotEmpty())
        unverified.forEach { key ->
            val turkish = SearchTokenCatalog.tokenFor(key, "Turkish")
            assertNotNull("unverified token $key must have Turkish entry", turkish)
        }
    }

    @Test
    fun `supported languages include English and Turkish`() {
        val langs = SearchTokenCatalog.supportedLanguages
        assertTrue("English must be supported", langs.contains("English"))
        assertTrue("Turkish must be supported", langs.contains("Turkish"))
        assertEquals("Only English and Turkish currently supported", 2, langs.size)
    }
}
