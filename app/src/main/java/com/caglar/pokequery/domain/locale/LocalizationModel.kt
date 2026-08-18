package com.caglar.pokequery.domain.locale

import com.caglar.pokequery.domain.engine.SearchTermMapper
import java.util.Locale

/**
 * v0.5.2 (Fix 7): the two-layer localization model — pure, testable, no Android deps until a
 * caller actually asks Auto/System Default to consult the device locale.
 *
 * Layer A — [AppLanguage]: the app's UI language.
 * Layer B — [SearchStringLanguage]: the language of generated Pokémon GO search strings.
 *
 * The two layers are independent. Explicit search-language choices never consult Android. Auto
 * and Match App + System Default resolve from an injected device locale when available, otherwise
 * they consult the live system locale on Android and safely fall back to English in plain JVM
 * environments.
 */
object LocalizationModel {

    object AppLanguage {
        const val SYSTEM_DEFAULT = AppLocaleController.SYSTEM_DEFAULT
        const val ENGLISH = AppLocaleController.ENGLISH
        const val TURKISH = AppLocaleController.TURKISH
        val OPTIONS = AppLocaleController.OPTIONS
        fun isValid(label: String): Boolean = label.trim() in OPTIONS
    }

    object SearchStringLanguage {
        const val AUTO_SAFE = "Auto"
        const val MATCH_APP = "Match App Language"
        const val ENGLISH = "English"
        const val GERMAN = "German"
        const val SPANISH = "Spanish"
        const val FRENCH = "French"
        const val ITALIAN = "Italian"
        const val TURKISH = "Turkish"

        val OPTIONS = listOf(AUTO_SAFE, MATCH_APP, ENGLISH, GERMAN, SPANISH, FRENCH, ITALIAN, TURKISH)
        const val DEFAULT = AUTO_SAFE

        private fun currentDeviceLocaleOrEnglish(deviceLocale: Locale?): Locale {
            if (deviceLocale != null) return deviceLocale
            return runCatching { AppLocaleController.deviceLocale() }.getOrDefault(Locale.ENGLISH)
        }

        fun resolve(
            storedValue: String?,
            appLanguage: String? = null,
            deviceLocale: Locale? = null
        ): String {
            val pref = storedValue ?: DEFAULT

            // Explicit search-string language is fully independent from device/UI language and
            // must remain JVM-testable without touching Android Resources.
            if (pref.isNotBlank() &&
                !pref.equals(AUTO_SAFE, ignoreCase = true) &&
                !pref.equals(MATCH_APP, ignoreCase = true) &&
                !pref.equals("Match App", ignoreCase = true)
            ) {
                return pref
            }

            if (pref.equals(MATCH_APP, ignoreCase = true) || pref.equals("Match App", ignoreCase = true)) {
                val appLang = appLanguage ?: AppLanguage.SYSTEM_DEFAULT
                val explicitAppTag = AppLocaleController.localeTagFor(appLang)
                val tag = explicitAppTag ?: AppLocaleController.supportedTagFor(currentDeviceLocaleOrEnglish(deviceLocale))
                return searchLanguageForTag(tag)
            }

            val currentDevice = currentDeviceLocaleOrEnglish(deviceLocale)
            return searchLanguageForTag(AppLocaleController.supportedTagFor(currentDevice))
        }

        fun searchLanguageForTag(tag: String): String = when (tag.lowercase().substringBefore('-')) {
            "tr" -> TURKISH
            "de" -> GERMAN
            "es" -> SPANISH
            "fr" -> FRENCH
            "it" -> ITALIAN
            else -> ENGLISH
        }

        fun isTurkishExplicitlyChosen(storedValue: String?): Boolean =
            storedValue?.trim() == TURKISH

        fun resolvesToTurkish(
            storedValue: String?,
            appLanguage: String? = null,
            deviceLocale: Locale? = null
        ): Boolean = resolve(storedValue, appLanguage, deviceLocale) == TURKISH

        fun autoSafeNeverBecomesTurkish(): Boolean =
            resolve(AUTO_SAFE, deviceLocale = Locale.ENGLISH) == ENGLISH
    }

    fun resolveSearchStringLanguageIndependentOf(
        searchStringLengthPref: String?,
        appLanguagePref: String?
    ): String = SearchStringLanguage.resolve(searchStringLengthPref, appLanguagePref)
}
