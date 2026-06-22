package com.caglar.pokequery.domain.locale

import com.caglar.pokequery.domain.locale.LocalizationModel.AppLanguage
import com.caglar.pokequery.domain.locale.LocalizationModel.SearchStringLanguage
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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

    // ---- v0.5.2.1 hotfix: recreation-free, in-process locale apply ----

    @Test
    fun `applyProcessLocale sets english locale without going through LocaleManager`() {
        val before = Locale.getDefault()
        try {
            AppLocaleController.applyProcessLocale("en")
            assertEquals("en", Locale.getDefault().language)
        } finally {
            Locale.setDefault(before)
        }
    }

    @Test
    fun `applyProcessLocale sets turkish locale without going through LocaleManager`() {
        // Selecting Turkish must never black-screen the app: this is the regression that
        // reproduces the SM-S931B / Android 16 blocker. It only touches the process default
        // locale (JDK), so it cannot recreate the Activity.
        val before = Locale.getDefault()
        try {
            AppLocaleController.applyProcessLocale("tr")
            assertEquals("tr", Locale.getDefault().language)
        } finally {
            Locale.setDefault(before)
        }
    }

    @Test
    fun `applyProcessLocale is idempotent and System Default is a no-op`() {
        // System Default must not fight the device locale, and applying the same locale twice
        // must be stable (no loop). We capture the current default, set "en" twice, then
        // restore — the key assertion is the call returns normally both times with the same
        // resulting locale.
        val before = Locale.getDefault()
        try {
            // System Default (null/blank) must not change the current locale at all.
            AppLocaleController.applyProcessLocale(null)
            assertEquals(before, Locale.getDefault())
            AppLocaleController.applyProcessLocale("")
            assertEquals(before, Locale.getDefault())

            // English twice is stable and equal.
            AppLocaleController.applyProcessLocale("en")
            val first = Locale.getDefault()
            AppLocaleController.applyProcessLocale("en")
            assertEquals(first, Locale.getDefault())
            assertEquals("en", Locale.getDefault().language)
        } finally {
            Locale.setDefault(before)
        }
    }

    @Test
    fun `applyProcessLocale switching back and forth is stable`() {
        // The original bug was the flip between System Default (null) and a real tag causing a
        // recreation loop. The hotfix path is recreation-free; this asserts repeated toggling
        // converges to the last applied value with no exception.
        val before = Locale.getDefault()
        try {
            AppLocaleController.applyProcessLocale("en")
            assertEquals("en", Locale.getDefault().language)
            AppLocaleController.applyProcessLocale("tr")
            assertEquals("tr", Locale.getDefault().language)
            AppLocaleController.applyProcessLocale("en")
            assertEquals("en", Locale.getDefault().language)
            assertNotEquals("tr", Locale.getDefault().language)
        } finally {
            Locale.setDefault(before)
        }
    }
}
