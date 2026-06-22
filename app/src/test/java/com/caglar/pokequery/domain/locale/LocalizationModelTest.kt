package com.caglar.pokequery.domain.locale

import com.caglar.pokequery.domain.locale.LocalizationModel.AppLanguage
import com.caglar.pokequery.domain.locale.LocalizationModel.SearchStringLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.5.2 (Fix 7): two-layer localization model tests.
 *
 * Locks down the product safety guarantees:
 *  - App Language and Search String Language are independent.
 *  - Auto (Safe) never resolves to Turkish.
 *  - Turkish is emitted only when explicitly chosen for search strings.
 */
class LocalizationModelTest {

    // ---- Layer B: Search String Language ----

    @Test
    fun `auto safe resolves to english never turkish`() {
        assertEquals("English", SearchStringLanguage.resolve("Auto"))
        assertTrue(SearchStringLanguage.autoSafeNeverBecomesTurkish())
    }

    @Test
    fun `blank auto and null search language fall back to english not turkish`() {
        // The core safety guarantee: the safe/default paths never resolve to Turkish.
        assertEquals("English", SearchStringLanguage.resolve(null))
        assertEquals("English", SearchStringLanguage.resolve(""))
        assertEquals("English", SearchStringLanguage.resolve("   "))
        assertEquals("English", SearchStringLanguage.resolve("Auto"))
        // Unknown non-blank values are passed through by the mapper (existing behavior), but
        // they must NEVER resolve to Turkish — that is the invariant that matters.
        assertFalse(SearchStringLanguage.resolvesToTurkish(null))
        assertFalse(SearchStringLanguage.resolvesToTurkish(""))
        assertFalse(SearchStringLanguage.resolvesToTurkish("Auto"))
        assertFalse(SearchStringLanguage.resolvesToTurkish("spaghetti"))
    }

    @Test
    fun `turkish is only emitted when explicitly chosen`() {
        assertTrue(SearchStringLanguage.isTurkishExplicitlyChosen("Turkish"))
        assertFalse(SearchStringLanguage.isTurkishExplicitlyChosen("Auto"))
        assertFalse(SearchStringLanguage.isTurkishExplicitlyChosen("English"))
        assertFalse(SearchStringLanguage.isTurkishExplicitlyChosen(null))

        assertTrue(SearchStringLanguage.resolvesToTurkish("Turkish"))
        assertEquals("Turkish", SearchStringLanguage.resolve("Turkish"))
    }

    @Test
    fun `english resolves to english`() {
        assertEquals("English", SearchStringLanguage.resolve("English"))
    }

    // ---- Two-layer independence ----

    @Test
    fun `app language turkish does not force search strings to turkish`() {
        // Turkish UI + Auto search → search must stay English (safe).
        val resolved = LocalizationModel.resolveSearchStringLanguageIndependentOf(
            searchStringLengthPref = "Auto",
            appLanguagePref = "Turkish"
        )
        assertEquals("English", resolved)
    }

    @Test
    fun `app language english does not override an explicit turkish search choice`() {
        // Each layer is independent in BOTH directions.
        val resolved = LocalizationModel.resolveSearchStringLanguageIndependentOf(
            searchStringLengthPref = "Turkish",
            appLanguagePref = "English"
        )
        assertEquals("Turkish", resolved)
    }

    @Test
    fun `changing app language has zero effect on resolved search language`() {
        val autoWithEnglishUi = LocalizationModel.resolveSearchStringLanguageIndependentOf("Auto", "English")
        val autoWithTurkishUi = LocalizationModel.resolveSearchStringLanguageIndependentOf("Auto", "Turkish")
        val autoWithSystemUi = LocalizationModel.resolveSearchStringLanguageIndependentOf("Auto", "System Default")
        assertEquals(autoWithEnglishUi, autoWithTurkishUi)
        assertEquals(autoWithEnglishUi, autoWithSystemUi)
        assertEquals("English", autoWithEnglishUi)
    }

    // ---- Layer A: App Language vocabulary ----

    @Test
    fun `app language options include system default english and turkish`() {
        assertEquals(listOf("System Default", "English", "Turkish"), AppLanguage.OPTIONS)
        assertTrue(AppLanguage.isValid("System Default"))
        assertTrue(AppLanguage.isValid("English"))
        assertTrue(AppLanguage.isValid("Turkish"))
        assertFalse(AppLanguage.isValid("Auto"))
        assertFalse(AppLanguage.isValid(""))
    }

    @Test
    fun `app language locale tags map correctly`() {
        assertEquals("en", AppLocaleController.localeTagFor("English"))
        assertEquals("tr", AppLocaleController.localeTagFor("Turkish"))
        assertEquals(null, AppLocaleController.localeTagFor("System Default"))
        assertEquals(null, AppLocaleController.localeTagFor("unknown"))
    }
}
