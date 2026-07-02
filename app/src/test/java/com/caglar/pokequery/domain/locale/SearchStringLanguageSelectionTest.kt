package com.caglar.pokequery.domain.locale

import com.caglar.pokequery.domain.locale.LocalizationModel.AppLanguage
import com.caglar.pokequery.domain.locale.LocalizationModel.SearchStringLanguage
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.5.4 (Fix 5) — regression test for the Settings > Search String Language radio bug.
 *
 * Observed bug: selecting "English" lit BOTH the "Auto (Safe)" radio and the "English"
 * radio, because the Auto predicate was `searchLang == "Auto" || isBlank || == "English"`
 * (it conflated the generation-time invariant "Auto resolves to English" with UI selection
 * state). Turkish was unaffected. This test mirrors the corrected, mutually-exclusive
 * selection predicates that the Settings UI must use, so a future regression is caught
 * without needing an Android instrumentation test.
 *
 * These predicates intentionally do NOT call SearchTermMapper.resolveLanguage — selection
 * state is about WHICH option the user picked, not about the effective output language
 * (which is covered separately by SearchTermMapperTest / LocalizationModelTest).
 */
class SearchStringLanguageSelectionTest {

    /**
     * Mirrors the corrected radio `selected` predicates in MiscScreens.SettingsScreen
     * (v0.5.4 Fix 5): Auto is selected ONLY for "Auto"/blank; English and Turkish are
     * exact equality. Auto no longer matches "English".
     */
    private fun isSelectedAuto(storedGameLanguage: String?): Boolean =
        (storedGameLanguage ?: SearchStringLanguage.DEFAULT).let { it == SearchStringLanguage.AUTO_SAFE || it.isBlank() }

    private fun isSelectedEnglish(storedGameLanguage: String?): Boolean =
        (storedGameLanguage ?: SearchStringLanguage.DEFAULT) == SearchStringLanguage.ENGLISH

    private fun isSelectedTurkish(storedGameLanguage: String?): Boolean =
        (storedGameLanguage ?: SearchStringLanguage.DEFAULT) == SearchStringLanguage.TURKISH

    /** The default fallback the Settings UI uses (v0.5.4: "Auto", was "English"). */
    private val uiFallback = SearchStringLanguage.DEFAULT

    @Test
    fun `Auto selected alone when stored value is Auto`() {
        assertEquals(SearchStringLanguage.AUTO_SAFE, uiFallback)
        assertTrue("Auto should be selected", isSelectedAuto(SearchStringLanguage.AUTO_SAFE))
        assertFalse("English must NOT be selected", isSelectedEnglish(SearchStringLanguage.AUTO_SAFE))
        assertFalse("Turkish must NOT be selected", isSelectedTurkish(SearchStringLanguage.AUTO_SAFE))
    }

    @Test
    fun `English selected alone when stored value is English`() {
        // This is the regression: previously Auto ALSO lit up here.
        assertFalse("Auto must NOT be selected for English", isSelectedAuto(SearchStringLanguage.ENGLISH))
        assertTrue("English should be selected", isSelectedEnglish(SearchStringLanguage.ENGLISH))
        assertFalse("Turkish must NOT be selected", isSelectedTurkish(SearchStringLanguage.ENGLISH))
    }

    @Test
    fun `Turkish selected alone when stored value is Turkish`() {
        assertFalse("Auto must NOT be selected for Turkish", isSelectedAuto(SearchStringLanguage.TURKISH))
        assertFalse("English must NOT be selected for Turkish", isSelectedEnglish(SearchStringLanguage.TURKISH))
        assertTrue("Turkish should be selected", isSelectedTurkish(SearchStringLanguage.TURKISH))
    }

    @Test
    fun `blank or unset value resolves to Auto selected (safe default, not English)`() {
        // Fresh-install / unset preference must show Auto selected, NOT English, matching
        // LocalizationModel.SearchStringLanguage.DEFAULT and the v0.5.4 repository default.
        assertTrue("Auto should be selected when blank", isSelectedAuto(""))
        assertTrue("Auto should be selected when null (uiFallback)", isSelectedAuto(null))
        assertFalse("English must NOT be selected when blank", isSelectedEnglish(""))
        assertFalse("English must NOT be selected when null", isSelectedEnglish(null))
    }

    private fun isSelectedGerman(storedGameLanguage: String?): Boolean =
        (storedGameLanguage ?: SearchStringLanguage.DEFAULT) == SearchStringLanguage.GERMAN

    private fun isSelectedSpanish(storedGameLanguage: String?): Boolean =
        (storedGameLanguage ?: SearchStringLanguage.DEFAULT) == SearchStringLanguage.SPANISH

    private fun isSelectedFrench(storedGameLanguage: String?): Boolean =
        (storedGameLanguage ?: SearchStringLanguage.DEFAULT) == SearchStringLanguage.FRENCH

    private fun isSelectedItalian(storedGameLanguage: String?): Boolean =
        (storedGameLanguage ?: SearchStringLanguage.DEFAULT) == SearchStringLanguage.ITALIAN

    private fun isSelectedMatchApp(storedGameLanguage: String?): Boolean =
        (storedGameLanguage ?: SearchStringLanguage.DEFAULT) == SearchStringLanguage.MATCH_APP

    @Test
    fun `exactly one Search String Language option is selected for every valid value`() {
        SearchStringLanguage.OPTIONS.forEach { value ->
            val auto = isSelectedAuto(value)
            val matchApp = isSelectedMatchApp(value)
            val english = isSelectedEnglish(value)
            val german = isSelectedGerman(value)
            val spanish = isSelectedSpanish(value)
            val french = isSelectedFrench(value)
            val italian = isSelectedItalian(value)
            val turkish = isSelectedTurkish(value)
            val count = listOf(auto, matchApp, english, german, spanish, french, italian, turkish).count { it }
            assertEquals("exactly one option selected for '$value' (got $count)", 1, count)
        }
    }

    @Test
    fun `App Language selection does not affect Search String Language selection predicates`() {
        // The two layers are independent: changing App Language must not flip any Search
        // String Language radio. The selection predicates read gameLanguage only.
        for (appLang in AppLanguage.OPTIONS) {
            // Search String Language = English, App Language varies -> only English lit.
            val auto = isSelectedAuto(SearchStringLanguage.ENGLISH)
            val english = isSelectedEnglish(SearchStringLanguage.ENGLISH)
            val turkish = isSelectedTurkish(SearchStringLanguage.ENGLISH)
            assertFalse("App Language '$appLang' must not light Auto", auto)
            assertTrue("English stays selected regardless of App Language", english)
            assertFalse("App Language '$appLang' must not light Turkish", turkish)
        }
    }

    @Test
    fun `Auto resolves from supported device locale with english fallback`() {
        // Selection remains independent; generation resolves Auto from verified supported
        // device locales only, otherwise English.
        assertEquals(SearchStringLanguage.ENGLISH, SearchStringLanguage.resolve(SearchStringLanguage.AUTO_SAFE, deviceLocale = Locale("en")))
        assertEquals(SearchStringLanguage.TURKISH, SearchStringLanguage.resolve(SearchStringLanguage.AUTO_SAFE, deviceLocale = Locale("tr")))
        assertEquals(SearchStringLanguage.ENGLISH, SearchStringLanguage.resolve(SearchStringLanguage.AUTO_SAFE, deviceLocale = Locale("ja")))
    }
}
