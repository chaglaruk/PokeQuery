package com.caglar.pokequery.domain.locale

import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * v0.5.2 (Fix 7): App Language — Layer A of the two-layer localization architecture.
 *
 * Applies the user's chosen UI language via Android's per-app language APIs. This affects
 * only this app's interface resources; it does NOT touch the system locale, the Pokémon GO
 * app, or the generated search strings (those are Layer B — Search String Language).
 *
 *   System Default → empty locale list → the OS uses the device/system locale.
 *   English        → Locale("en")
 *   Turkish        → Locale("tr")
 *
 * On API 33+ we use the platform per-app language service via its string service name
 * ("locale") and reflection. We avoid a compile-time reference to android.os.LocaleManager
 * because the AGP 9.x Build-Tools-API (BTA) compiler trims that API-33 symbol from its
 * resolved surface even under @RequiresApi. The string-service + reflection path is
 * reflection-free of any third-party dependency, survives the BTA trimming, and targets the
 * exact same platform LocaleManager.setApplicationLocales/getApplicationLocales contract.
 *
 * On older devices we fall back to setting the process [Locale] default so resource lookups
 * localize within the current run. We deliberately do NOT add an AppCompat dependency.
 *
 * SCOPE NOTE: the app's UI strings are currently English-only. Wiring the per-app locale
 * here is the *foundation* so that when Turkish string resources (values-tr) are added
 * later they work automatically. Settings makes clear App Language is independent of the
 * generated search strings.
 */
object AppLocaleController {

    const val SYSTEM_DEFAULT = "System Default"
    const val ENGLISH = "English"
    const val TURKISH = "Turkish"

    /** The valid, selectable App Language labels in display order. */
    val OPTIONS: List<String> = listOf(SYSTEM_DEFAULT, ENGLISH, TURKISH)

    /** Maps the stored preference label to a language tag, or null for System Default. */
    fun localeTagFor(appLanguage: String): String? = when (appLanguage.trim()) {
        ENGLISH -> "en"
        TURKISH -> "tr"
        else -> null // System Default / unknown
    }

    /**
     * Applies the per-app locale. Idempotent; safe to call repeatedly.
     */
    fun apply(context: Context, appLanguage: String) {
        val tag = localeTagFor(appLanguage)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applyApi33(context, tag)
        } else if (!tag.isNullOrEmpty()) {
            // API < 33: best-effort process locale so resource lookups localize within this run.
            val parts = tag.split("-")
            val locale = if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
            Locale.setDefault(locale)
        }
    }

    /** Reads back the currently applied per-app locale label (System Default if unset). */
    fun currentLabel(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return when (Locale.getDefault().language) {
                "en" -> ENGLISH
                "tr" -> TURKISH
                else -> SYSTEM_DEFAULT
            }
        }
        val tags = currentTagsApi33(context)
        return when (tags.substringBefore("-").lowercase()) {
            "en" -> ENGLISH
            "tr" -> TURKISH
            else -> SYSTEM_DEFAULT
        }
    }

    // ---- API 33+ implementation via the platform "locale" service + reflection. ----
    // Targets android.os.LocaleManager#setApplicationLocales(LocaleList).

    private const val LOCALE_SERVICE = "locale"

    private fun applyApi33(context: Context, tag: String?) {
        runCatching {
            val lm = context.getSystemService(LOCALE_SERVICE) ?: return@runCatching
            val list = if (tag.isNullOrEmpty()) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
            lm.javaClass.getMethod("setApplicationLocales", LocaleList::class.java).invoke(lm, list)
        }
    }

    private fun currentTagsApi33(context: Context): String =
        runCatching {
            val lm = context.getSystemService(LOCALE_SERVICE) ?: return@runCatching ""
            val list = lm.javaClass.getMethod("getApplicationLocales").invoke(lm) as? LocaleList
            list?.toLanguageTags().orEmpty()
        }.getOrDefault("")
}

/**
 * Pure label/tag helpers (no Android context) used by Settings display and unit tests.
 * Kept here so the locale domain has one source of truth for the preference vocabulary.
 */
object AppLocaleLabels {
    fun labelForLanguageTag(tag: String?): String = when (tag?.lowercase()?.substringBefore('-')) {
        "en" -> AppLocaleController.ENGLISH
        "tr" -> AppLocaleController.TURKISH
        else -> AppLocaleController.SYSTEM_DEFAULT
    }
}

