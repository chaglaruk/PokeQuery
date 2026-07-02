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
 *  - Auto resolves from a supported device locale, otherwise English.
 *  - Turkish is emitted only when explicitly chosen for search strings.
 */
class LocalizationModelTest {

    // ---- Layer B: Search String Language ----

    @Test
    fun `auto safe follows verified device locale`() {
        assertEquals("English", SearchStringLanguage.resolve("Auto", deviceLocale = Locale("en")))
        assertEquals("Turkish", SearchStringLanguage.resolve("Auto", deviceLocale = Locale("tr")))
        assertEquals("German", SearchStringLanguage.resolve("Auto", deviceLocale = Locale("de")))
        assertTrue(SearchStringLanguage.autoSafeNeverBecomesTurkish())
    }

    @Test
    fun `blank auto and null search language fall back to english not turkish`() {
        // The core safety guarantee: the safe/default paths never resolve to Turkish.
        assertEquals("English", SearchStringLanguage.resolve(null, deviceLocale = Locale("ja")))
        assertEquals("English", SearchStringLanguage.resolve("", deviceLocale = Locale("ja")))
        assertEquals("English", SearchStringLanguage.resolve("   ", deviceLocale = Locale("ja")))
        assertEquals("English", SearchStringLanguage.resolve("Auto", deviceLocale = Locale("ja")))
        // Unknown non-blank values are passed through by the mapper (existing behavior), but
        // they must NEVER resolve to Turkish — that is the invariant that matters.
        assertFalse(SearchStringLanguage.resolvesToTurkish(null, deviceLocale = Locale("ja")))
        assertFalse(SearchStringLanguage.resolvesToTurkish("", deviceLocale = Locale("ja")))
        assertFalse(SearchStringLanguage.resolvesToTurkish("Auto", deviceLocale = Locale("ja")))
        assertFalse(SearchStringLanguage.resolvesToTurkish("spaghetti", deviceLocale = Locale("ja")))
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
        val resolved = SearchStringLanguage.resolve("Auto", appLanguage = "Turkish", deviceLocale = Locale("en"))
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
        val autoWithEnglishUi = SearchStringLanguage.resolve("Auto", "English", Locale("en"))
        val autoWithTurkishUi = SearchStringLanguage.resolve("Auto", "Turkish", Locale("en"))
        val autoWithSystemUi = SearchStringLanguage.resolve("Auto", "System Default", Locale("en"))
        assertEquals(autoWithEnglishUi, autoWithTurkishUi)
        assertEquals(autoWithEnglishUi, autoWithSystemUi)
        assertEquals("English", autoWithEnglishUi)
    }

    // ---- Layer A: App Language vocabulary ----

    @Test
    fun `app language options include system default english and turkish`() {
        assertEquals(listOf("System Default", "English", "Deutsch", "Español", "Français", "Italiano", "Türkçe"), AppLanguage.OPTIONS)
        assertTrue(AppLanguage.isValid("System Default"))
        assertTrue(AppLanguage.isValid("English"))
        assertTrue(AppLanguage.isValid("Türkçe"))
        assertFalse(AppLanguage.isValid("Auto"))
        assertFalse(AppLanguage.isValid(""))
    }

    @Test
    fun `app language locale tags map correctly`() {
        assertEquals("en", AppLocaleController.localeTagFor("English"))
        assertEquals("tr", AppLocaleController.localeTagFor("Türkçe"))
        assertEquals(null, AppLocaleController.localeTagFor("System Default"))
        assertEquals(null, AppLocaleController.localeTagFor("unknown"))
    }

    @Test
    fun `system default resolves to supported device locale or english`() {
        assertEquals("tr", AppLocaleController.resolvedLocaleTagFor("System Default", Locale("tr")))
        assertEquals("de", AppLocaleController.resolvedLocaleTagFor("System Default", Locale("de")))
        assertEquals("en", AppLocaleController.resolvedLocaleTagFor("System Default", Locale("ja")))
    }

    @Test
    fun `manual app language overrides device locale`() {
        assertEquals("en", AppLocaleController.resolvedLocaleTagFor("English", Locale("tr")))
        assertEquals("tr", AppLocaleController.resolvedLocaleTagFor("Türkçe", Locale("en")))
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
    fun `applyProcessLocale is idempotent and System Default restores startup locale`() {
        val before = Locale.getDefault()
        try {
            AppLocaleController.applyProcessLocale("tr")
            assertEquals("tr", Locale.getDefault().language)
            AppLocaleController.applyProcessLocale(null)
            assertEquals(before, Locale.getDefault())
            AppLocaleController.applyProcessLocale("tr")
            assertEquals("tr", Locale.getDefault().language)
            AppLocaleController.applyProcessLocale("")
            assertEquals(before, Locale.getDefault())

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

    // -------------------------------------------------------------------------
    // v0.6.8 — Search String Language: explicit per-language + Match App Language.
    //
    // Phase 4 expanded the vocabulary beyond Auto/English/Turkish to also offer German,
    // Spanish, French and Italian as direct choices, plus "Match App Language" (which
    // derives the output language from the App Language layer while keeping the two layers
    // independent at the preference level). These tests lock the token-generation language
    // resolution for the new options, including the safety guarantee that the safe paths
    // still resolve to English and never silently to Turkish.
    // -------------------------------------------------------------------------

    @Test
    fun `each explicit search string language resolves to itself`() {
        // Direct selections are passed through verbatim — the user gets the language they pick.
        assertEquals("English", SearchStringLanguage.resolve("English"))
        assertEquals("German", SearchStringLanguage.resolve("German"))
        assertEquals("Spanish", SearchStringLanguage.resolve("Spanish"))
        assertEquals("French", SearchStringLanguage.resolve("French"))
        assertEquals("Italian", SearchStringLanguage.resolve("Italian"))
        assertEquals("Turkish", SearchStringLanguage.resolve("Turkish"))
    }

    @Test
    fun `match app language follows an explicit app language for each supported language`() {
        // Match App Language must derive the output from the App Language layer, mapping the
        // display labels used by AppLocaleController.OPTIONS onto SearchStringLanguage values.
        assertEquals("English", SearchStringLanguage.resolve("Match App Language", AppLocaleController.ENGLISH))
        assertEquals("German", SearchStringLanguage.resolve("Match App Language", AppLocaleController.DEUTSCH))
        assertEquals("Spanish", SearchStringLanguage.resolve("Match App Language", AppLocaleController.ESPANOL))
        assertEquals("French", SearchStringLanguage.resolve("Match App Language", AppLocaleController.FRANCAIS))
        assertEquals("Italian", SearchStringLanguage.resolve("Match App Language", AppLocaleController.ITALIANO))
        assertEquals("Turkish", SearchStringLanguage.resolve("Match App Language", AppLocaleController.TURKISH))
    }

    @Test
    fun `match app language with system default follows the device locale`() {
        // When App Language is "System Default", Match App Language follows the supported
        // device locale, with English as the fallback for unsupported locales.
        for ((tag, expected) in listOf(
            "de" to "German",
            "es" to "Spanish",
            "fr" to "French",
            "it" to "Italian",
            "tr" to "Turkish",
            "en" to "English",
            "ja" to "English" // unsupported -> English safe default
        )) {
            assertEquals(
                "System Default + device '$tag' should resolve to '$expected'",
                expected,
                SearchStringLanguage.resolve("Match App Language", "System Default", Locale(tag))
            )
        }
    }

    @Test
    fun `match app language never silently becomes turkish on a non-turkish device`() {
        // Even under Match App Language the safe invariant holds: a device that is not Turkish
        // (here, English) must never resolve to Turkish output.
        assertFalse(SearchStringLanguage.resolvesToTurkish("Match App Language", "System Default", Locale("en")))
        assertFalse(SearchStringLanguage.resolvesToTurkish("Match App Language", "English", Locale("en")))
    }

    @Test
    fun `search string language options include the new languages and match app`() {
        // Vocabulary completeness — the selector must surface all of these, in display order.
        assertEquals(
            listOf(
                SearchStringLanguage.AUTO_SAFE,
                SearchStringLanguage.MATCH_APP,
                SearchStringLanguage.ENGLISH,
                SearchStringLanguage.GERMAN,
                SearchStringLanguage.SPANISH,
                SearchStringLanguage.FRENCH,
                SearchStringLanguage.ITALIAN,
                SearchStringLanguage.TURKISH
            ),
            SearchStringLanguage.OPTIONS
        )
    }
}
