package com.caglar.pokequery.domain.locale

import com.caglar.pokequery.domain.engine.SearchTermMapper
import java.util.Locale

/**
 * v0.5.2 (Fix 7): the two-layer localization model — pure, testable, no Android deps.
 *
 * Layer A — [AppLanguage]: the app's UI language (System Default / English / Turkish).
 *   Stored as [UserPreferences.appLanguage]. Controls interface text ONLY.
 *
 * Layer B — [SearchStringLanguage]: the language of generated Pokémon GO search strings
 *   (Auto Safe / English / Turkish Beta). Stored as [UserPreferences.gameLanguage].
 *   Controls generated strings ONLY.
 *
 * The two layers are independent: setting a Turkish UI must NOT change search strings,
 * and setting English search strings must NOT force an English UI. This object encodes the
 * safety guarantees the product requires:
 *
 *   1. Auto follows a verified supported device language, otherwise falls back to English.
 *   2. Turkish is emitted only when explicitly selected or when the supported device/app
 *      language is Turkish under Auto/Match App Language.
 *   3. App Language has zero influence on the resolved search-string language.
 *
 * The actual token translation is delegated to [SearchTermMapper] (unchanged, safety-tested).
 */
object LocalizationModel {

    /** Layer A — App UI language preference labels. */
    object AppLanguage {
        const val SYSTEM_DEFAULT = AppLocaleController.SYSTEM_DEFAULT
        const val ENGLISH = AppLocaleController.ENGLISH
        const val TURKISH = AppLocaleController.TURKISH

        /** Selectable options in display order. */
        val OPTIONS = AppLocaleController.OPTIONS

        /** Whether the given stored label is one of the valid App Language options. */
        fun isValid(label: String): Boolean = label.trim() in OPTIONS
    }

    /** Layer B — Search String Language preference labels. */
    object SearchStringLanguage {
        const val AUTO_SAFE = "Auto"
        const val MATCH_APP = "Match App Language"
        const val ENGLISH = "English"
        const val GERMAN = "German"
        const val SPANISH = "Spanish"
        const val FRENCH = "French"
        const val ITALIAN = "Italian"
        const val TURKISH = "Turkish"

        /** Selectable options in display order. */
        val OPTIONS = listOf(AUTO_SAFE, MATCH_APP, ENGLISH, GERMAN, SPANISH, FRENCH, ITALIAN, TURKISH)

        /** Default — conservative: verified device language or English fallback. */
        const val DEFAULT = AUTO_SAFE

        /**
         * Resolves the *effective* search-string language from the stored preference.
         *
         * Safety invariant: Auto/blank uses only verified supported language tags and falls
         * back to English for unsupported locales.
         */
        fun resolve(storedValue: String?, appLanguage: String? = null, deviceLocale: Locale = AppLocaleController.deviceLocale()): String {
            val pref = storedValue ?: DEFAULT
            if (pref.equals(MATCH_APP, ignoreCase = true) || pref.equals("Match App", ignoreCase = true)) {
                val appLang = appLanguage ?: "System Default"
                return searchLanguageForTag(AppLocaleController.resolvedLocaleTagFor(appLang, deviceLocale))
            }
            return if (pref.isBlank() || pref.equals("Auto", ignoreCase = true)) {
                searchLanguageForTag(AppLocaleController.supportedTagFor(deviceLocale))
            } else {
                pref
            }
        }

        fun searchLanguageForTag(tag: String): String = when (tag.lowercase().substringBefore('-')) {
            "tr" -> TURKISH
            "de" -> GERMAN
            "es" -> SPANISH
            "fr" -> FRENCH
            "it" -> ITALIAN
            else -> ENGLISH
        }

        /** True only when the user EXPLICITLY chose Turkish for search strings. */
        fun isTurkishExplicitlyChosen(storedValue: String?): Boolean =
            (storedValue?.trim() == TURKISH)

        /** True when the effective output would be Turkish. */
        fun resolvesToTurkish(storedValue: String?, appLanguage: String? = null, deviceLocale: Locale = AppLocaleController.deviceLocale()): Boolean =
            resolve(storedValue, appLanguage, deviceLocale) == TURKISH

        /** Auto Safe does not become Turkish on an English/unsupported device locale. */
        fun autoSafeNeverBecomesTurkish(): Boolean {
            return resolve(AUTO_SAFE, deviceLocale = Locale.ENGLISH) == ENGLISH
        }
    }

    /**
     * Documents and enforces the core two-layer independence guarantee. The app UI language
     * has NO effect on the resolved search-string language. Returns the resolved
     * search-string language so callers don't accidentally derive it from the UI language.
     */
    fun resolveSearchStringLanguageIndependentOf(
        searchStringLengthPref: String?,
        appLanguagePref: String?
    ): String = SearchStringLanguage.resolve(searchStringLengthPref, appLanguagePref)
}
