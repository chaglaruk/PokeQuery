package com.caglar.pokequery.domain.locale

import android.content.Context
import android.content.res.Resources
import java.util.Locale

/**
 * App Language — Layer A of the two-layer localization architecture.
 *
 * Records the user's chosen UI language and applies it as an in-process resource-lookup hint
 * only. This affects only this app's interface resources; it does NOT touch the system locale,
 * the Pokémon GO app, or generated search strings (those are Layer B — Search String Language).
 *
 * We deliberately do NOT call Android LocaleManager#setApplicationLocales here. An earlier
 * implementation recreated MainActivity from composition and could enter a recreation loop on
 * Samsung devices. The stable approach remains recreation-free: a localized configuration
 * context is supplied by MainActivity and Locale.setDefault is only used as the in-process
 * default for non-resource helpers.
 *
 * System Default must stay live. Never cache the device locale at object initialization: Android
 * can change the system language while this process remains alive. [deviceLocale] therefore reads
 * Resources.getSystem() on every resolution and MainActivity keys locale application to the
 * current configuration language tag.
 */
object AppLocaleController {
    const val SYSTEM_DEFAULT = "System Default"
    const val ENGLISH = "English"
    const val TURKISH = "Türkçe"
    const val DEUTSCH = "Deutsch"
    const val FRANCAIS = "Français"
    const val ESPANOL = "Español"
    const val ITALIANO = "Italiano"

    /** Previous process default used only as a compatibility fallback for low-level null restores. */
    private var processLocaleBeforeExplicitOverride: Locale? = null

    /** The valid, selectable App Language labels in display order. */
    val OPTIONS: List<String> = listOf(
        SYSTEM_DEFAULT,
        ENGLISH,
        DEUTSCH,
        ESPANOL,
        FRANCAIS,
        ITALIANO,
        TURKISH
    )

    /** Maps the stored preference label to a language tag, or null for System Default. */
    fun localeTagFor(appLanguage: String): String? = when (appLanguage.trim()) {
        ENGLISH -> "en"
        TURKISH, "Turkish", "Türkçe" -> "tr"
        DEUTSCH -> "de"
        FRANCAIS, "Français" -> "fr"
        ESPANOL, "Español" -> "es"
        ITALIANO -> "it"
        else -> null // System Default / unknown
    }

    fun supportedTagFor(locale: Locale): String = when (locale.language.lowercase()) {
        "tr" -> "tr"
        "de" -> "de"
        "es" -> "es"
        "fr" -> "fr"
        "it" -> "it"
        else -> "en"
    }

    /**
     * Reads the device locale dynamically rather than keeping a process-start snapshot.
     * Resources.getSystem() is intentionally used so an explicit PokeQuery app language cannot
     * overwrite the value we later need when the user switches back to System Default.
     */
    fun deviceLocale(): Locale {
        val locales = Resources.getSystem().configuration.locales
        return if (!locales.isEmpty) locales[0] else Locale.getDefault()
    }

    fun resolvedLocaleTagFor(
        appLanguage: String,
        currentDeviceLocale: Locale? = null
    ): String {
        localeTagFor(appLanguage)?.let { return it }
        return supportedTagFor(currentDeviceLocale ?: deviceLocale())
    }

    /**
     * Applies the App Language preference as an in-process, recreation-free locale hint.
     * [currentDeviceLocale] is injectable for deterministic tests and is supplied from the
     * current Activity configuration by MainActivity. System Default deliberately passes a null
     * explicit tag so [applyProcessLocale] also clears any remembered manual override.
     */
    fun apply(
        @Suppress("UNUSED_PARAMETER") context: Context,
        appLanguage: String,
        currentDeviceLocale: Locale? = null
    ) {
        val device = currentDeviceLocale ?: deviceLocale()
        applyProcessLocale(localeTagFor(appLanguage), device)
    }

    /**
     * For an explicit tag, no Android lookup is needed. Production System Default callers pass
     * the current device locale explicitly. The remembered pre-override process locale exists
     * only so legacy low-level callers of applyProcessLocale(null) remain deterministic without
     * reintroducing a process-start device-locale snapshot.
     */
    fun applyProcessLocale(tag: String?, currentDeviceLocale: Locale? = null) {
        if (tag.isNullOrEmpty()) {
            val target = currentDeviceLocale ?: processLocaleBeforeExplicitOverride ?: Locale.getDefault()
            Locale.setDefault(target)
            processLocaleBeforeExplicitOverride = null
            return
        }
        if (processLocaleBeforeExplicitOverride == null) {
            processLocaleBeforeExplicitOverride = currentDeviceLocale ?: Locale.getDefault()
        }
        Locale.setDefault(Locale.forLanguageTag(tag))
    }

    fun localeFor(
        appLanguage: String,
        currentDeviceLocale: Locale? = null
    ): Locale = Locale.forLanguageTag(resolvedLocaleTagFor(appLanguage, currentDeviceLocale))

    /** Reads back the currently applied in-process App Language label. */
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

/** Pure label/tag helpers used by Settings display and unit tests. */
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
