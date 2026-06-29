package com.caglar.pokequery.domain.locale

import android.content.Context
import java.util.Locale

/**
 * v0.5.2 (Fix 7) → v0.5.2.1 hotfix: App Language — Layer A of the two-layer localization
 * architecture.
 *
 * Records the user's chosen UI language and applies it as an **in-process** resource-lookup
 * hint only. This affects only this app's interface resources; it does NOT touch the system
 * locale, the Pokémon GO app, or the generated search strings (those are Layer B — Search
 * String Language).
 *
 *   System Default → leave the process default locale untouched (follow the device locale).
 *   English        → Locale("en")
 *   Turkish        → Locale("tr")
 *
 * ## v0.5.2.1 hotfix — why no OS LocaleManager
 *
 * The original v0.5.2 implementation applied the per-app locale via Android's
 * `LocaleManager#setApplicationLocales` (reached through the "locale" system service +
 * reflection, to dodge the AGP BTA trimming of the API-33 symbol). On API 33+ that call
 * **recreates the Activity** whenever the applied locale actually changes. It was wired from
 * a `SideEffect` in `MainActivity`, which runs on every composition. Because the preference
 * is loaded asynchronously (`collectAsState(initial = null)`), the effective value flipped
 * between "System Default" (null) and the persisted label on every recreation — so each
 * `setApplicationLocales` call changed the locale, which recreated the Activity, which ran
 * the SideEffect again... an infinite recreation loop. On Samsung SM-S931B / Android 16 the
 * window got stuck with `WindowStopped=true` and rendered a permanent black screen,
 * recoverable only by `pm clear`. The `runCatching` swallowed every exception, so nothing
 * was logged. English and Turkish both reproduced; System Default alone was stable.
 *
 * The fix prioritizes stability over "true" OS per-app language for v0.5.2:
 *   - We do NOT call `LocaleManager` / `setApplicationLocales` / any reflection.
 *   - `apply()` sets only the process-default `Locale` via `Locale.setDefault(...)`, which is
 *     an in-memory field set: it does NOT recreate the Activity and is safe to call from
 *     composition.
 *   - `MainActivity` now applies it via `LaunchedEffect(appLanguage)` (per-change) instead of
 *     `SideEffect` (per-frame), so even the invocation cadence cannot loop.
 *
 * This has no functional downside today because the app ships no localized resources yet
 * (no `values-tr/`): the OS locale call was only ever a foundation, and Settings now says so
 * honestly. When real translated string resources are added in a future release, the
 * process-locale set here makes them resolve correctly within the run.
 */
object AppLocaleController {

    const val SYSTEM_DEFAULT = "System Default"
    const val ENGLISH = "English"
    const val TURKISH = "Türkçe"
    const val DEUTSCH = "Deutsch"
    const val FRANCAIS = "Français"
    const val ESPANOL = "Español"
    const val ITALIANO = "Italiano"

    /** The valid, selectable App Language labels in display order. */
    val OPTIONS: List<String> = listOf(SYSTEM_DEFAULT, ENGLISH, DEUTSCH, ESPANOL, FRANCAIS, ITALIANO, TURKISH)

    /** Maps the stored preference label to a language tag, or null for System Default. */
    fun localeTagFor(appLanguage: String): String? = when (appLanguage.trim()) {
        ENGLISH -> "en"
        TURKISH, "Turkish" -> "tr"
        DEUTSCH -> "de"
        FRANCAIS -> "fr"
        ESPANOL -> "es"
        ITALIANO -> "it"
        else -> null // System Default / unknown
    }

    /**
     * Applies the App Language preference as an in-process, **recreation-free** locale hint.
     *
     * Idempotent and safe to call from composition (see [MainActivity]). It deliberately does
     * NOT call `LocaleManager` / `setApplicationLocales` — see the class kdoc for why. The
     * [context] is accepted for API stability but is not required by the current
     * implementation.
     */
    fun apply(context: Context, appLanguage: String) {
        val tag = localeTagFor(appLanguage)
        // Set the process locale (fallback)
        applyProcessLocale(tag)


    }

    fun applyProcessLocale(tag: String?) {
        if (tag.isNullOrEmpty()) return
        val parts = tag.split("-")
        val locale = if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
        Locale.setDefault(locale)
    }

    /**
     * Reads back the currently applied App Language label (System Default if unset).
     *
     * Reads the process default locale only — no `LocaleManager` read, consistent with the
     * v0.5.2.1 hotfix. The [context] is accepted for API stability.
     */
    fun currentLabel(@Suppress("UNUSED_PARAMETER") context: Context): String =
        when (Locale.getDefault().language) {
            "en" -> ENGLISH
            "tr" -> TURKISH
            "de" -> DEUTSCH
            "fr" -> FRANCAIS
            "es" -> ESPANOL
            "it" -> ITALIANO
            else -> SYSTEM_DEFAULT
        }
}

/**
 * Pure label/tag helpers (no Android context) used by Settings display and unit tests.
 * Kept here so the locale domain has one source of truth for the preference vocabulary.
 */
object AppLocaleLabels {
    fun labelForLanguageTag(tag: String?): String = when (tag?.lowercase()?.substringBefore('-')) {
        "en" -> AppLocaleController.ENGLISH
        "tr" -> AppLocaleController.TURKISH
        "de" -> AppLocaleController.DEUTSCH
        "fr" -> AppLocaleController.FRANCAIS
        "es" -> AppLocaleController.ESPANOL
        "it" -> AppLocaleController.ITALIANO
        else -> AppLocaleController.SYSTEM_DEFAULT
    }
}
