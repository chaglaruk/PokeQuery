package com.caglar.pokequery.domain.locale

import com.caglar.pokequery.domain.engine.SearchTermMapper

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
 *   1. Auto (Safe) never resolves to Turkish — it is always conservative (English).
 *   2. Turkish is only ever emitted when the user EXPLICITLY chooses it for search strings.
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
        const val ENGLISH = "English"
        const val TURKISH = "Turkish"

        /** Selectable options in display order. */
        val OPTIONS = listOf(AUTO_SAFE, ENGLISH, TURKISH)

        /** Default — conservative. Never Turkish unless explicitly chosen. */
        const val DEFAULT = AUTO_SAFE

        /**
         * Resolves the *effective* search-string language from the stored preference.
         *
         * Safety invariant: Auto/blank/unknown → English (never Turkish). This is the
         * delegation point to the existing, safety-tested [SearchTermMapper.resolveLanguage],
         * kept here so the two-layer contract has one documented home and one test target.
         */
        fun resolve(storedValue: String?): String = SearchTermMapper.resolveLanguage(storedValue ?: DEFAULT)

        /** True only when the user EXPLICITLY chose Turkish for search strings. */
        fun isTurkishExplicitlyChosen(storedValue: String?): Boolean =
            (storedValue?.trim() == TURKISH)

        /** True when the effective output would be Turkish. */
        fun resolvesToTurkish(storedValue: String?): Boolean =
            resolve(storedValue) == TURKISH

        /** Auto Safe never becomes Turkish, regardless of UI language or device locale. */
        fun autoSafeNeverBecomesTurkish(): Boolean {
            // Auto must resolve to English, no matter what. Guarded by a test.
            return resolve(AUTO_SAFE) == ENGLISH
        }
    }

    /**
     * Documents and enforces the core two-layer independence guarantee. The app UI language
     * has NO effect on the resolved search-string language. Returns the resolved
     * search-string language so callers don't accidentally derive it from the UI language.
     */
    fun resolveSearchStringLanguageIndependentOf(
        searchStringLengthPref: String?,
        @Suppress("UNUSED_PARAMETER") appLanguagePref: String?
    ): String = SearchStringLanguage.resolve(searchStringLengthPref)
}
