package com.caglar.pokequery.domain.locale

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.5.5 (Fix 3): regression guard against the v0.5.2 black-screen bug.
 *
 * ## The bug this guards
 *
 * The original v0.5.2 implementation applied the per-app locale through Android's
 * `LocaleManager` — reached via the `"locale"` system service and **reflection** (to dodge AGP
 * BTA trimming of the API-33 symbol), calling `LocaleManager#setApplicationLocales`. On API 33+
 * that call **recreates the Activity** whenever the applied locale changes. It was wired from a
 * `SideEffect` in `MainActivity` (per-frame) while the preference loaded asynchronously, so the
 * null→label flip recreated the Activity every frame → an infinite recreation loop. On Samsung
 * SM-S931B / Android 16 the window got stuck `WindowStopped=true`: a permanent **black screen**,
 * recoverable only by `pm clear`. `runCatching` swallowed every exception, so nothing was logged.
 * English and Turkish both reproduced; System Default alone was stable.
 *
 * The v0.5.2.1 hotfix replaced all of that with an in-memory `Locale.setDefault(...)` only —
 * no Activity recreation, no OS locale service. `MainActivity` now invokes it per-change via
 * `LaunchedEffect(appLanguage)` instead of per-frame.
 *
 * ## What this test does
 *
 * It reads the production source (`AppLocaleController.kt` + `MainActivity.kt`), **strips Kotlin
 * comments and string literals first** (the controller's kdoc deliberately mentions the banned
 * symbols to explain the history, so a naive whole-file scan would false-positive), and FAILS if
 * any of the device-bricking primitives appear in actual executable code. The behavioral
 * recreation-free guarantee is covered separately by `LocalizationModelTest`
 * (`applyProcessLocale …`); this guard protects the *source* so a future "improvement" cannot
 * silently bring the OS locale path back.
 *
 * If you legitimately need per-app OS locales in the future, you MUST also change how the
 * preference is applied (never from a per-frame `SideEffect`, and never in a way that can loop
 * on async-load null→label flips). Update this test as part of that work — do not just delete it.
 */
class AppLocaleControllerBlackScreenGuardTest {

    private val controllerSource = File("src/main/java/com/caglar/pokequery/domain/locale/AppLocaleController.kt")
    private val mainActivitySource = File("src/main/java/com/caglar/pokequery/MainActivity.kt")

    private fun requireExists(file: File) {
        assertTrue("Source file missing: ${file.path} (did the package move?)", file.exists())
    }

    /**
     * Strip // line comments, /* */ block comments, and "..." string literals so the banned
     * symbol names that legitimately appear in the explanatory kdoc/strings are not matched.
     * Only actual executable code tokens remain.
     */
    private fun codeOnly(file: File): String {
        var src = file.readText()
        // Remove block comments (non-greedy, DOTALL).
        src = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(src, " ")
        // Remove line comments.
        src = src.lineSequence().joinToString("\n") { line ->
            val idx = line.indexOf("//")
            if (idx >= 0) line.substring(0, idx) else line
        }
        // Remove double-quoted string literals (simple, sufficient for this guard).
        src = Regex("\"[^\"]*\"").replace(src, "\" \"")
        return src
    }

    @Test
    fun `app locale controller does not call LocaleManager`() {
        requireExists(controllerSource)
        val src = codeOnly(controllerSource)
        assertFalse(
            "AppLocaleController must NOT reference LocaleManager in executable code — it drove " +
                "the v0.5.2 black-screen recreation loop. Use Locale.setDefault (in-process) instead.",
            src.contains("LocaleManager")
        )
    }

    @Test
    fun `app locale controller does not call setApplicationLocales or getApplicationLocales`() {
        requireExists(controllerSource)
        val src = codeOnly(controllerSource)
        assertFalse(
            "AppLocaleController must NOT call setApplicationLocales/getApplicationLocales — these " +
                "recreate the Activity and drove the v0.5.2 black-screen loop.",
            src.contains("setApplicationLocales") || src.contains("getApplicationLocales")
        )
    }

    @Test
    fun `app locale controller does not fetch the locale system service`() {
        requireExists(controllerSource)
        val src = codeOnly(controllerSource)
        assertFalse(
            "AppLocaleController must NOT obtain the \"locale\" system service (getSystemService) — " +
                "the LocaleManager handle it returns is the black-screen path.",
            src.contains("getSystemService")
        )
    }

    @Test
    fun `app locale controller does not use reflection to reach locale apis`() {
        requireExists(controllerSource)
        val src = codeOnly(controllerSource)
        // Reflection was used to dodge AGP BTA trimming of the API-33 LocaleManager symbol.
        // Any reflection on the locale path is a strong signal the OS locale path is back.
        assertFalse(
            "AppLocaleController must NOT use reflection (java.lang.reflect / Class.forName / " +
                "getDeclaredMethod / invoke) to reach locale APIs — that was the v0.5.2 dodge used " +
                "to call the recreating LocaleManager path.",
            src.contains("java.lang.reflect") || src.contains("Class.forName") ||
                src.contains(".getDeclaredMethod(") || src.contains(".invoke(")
        )
    }

    @Test
    fun `app locale controller uses the recreation-free Locale setDefault path`() {
        requireExists(controllerSource)
        val src = codeOnly(controllerSource)
        assertTrue(
            "AppLocaleController must apply the locale via Locale.setDefault (the recreation-free, " +
                "in-process path that replaced the black-screen LocaleManager call).",
            src.contains("Locale.setDefault")
        )
    }

    @Test
    fun `main activity applies app language per-change not per-frame`() {
        // The bug's amplifier: a per-frame SideEffect re-ran the (recreating) call every frame
        // while the async pref flipped null→label. Even with the in-process path, the cadence
        // must be per-change (LaunchedEffect(appLanguage)), never per-frame (SideEffect).
        requireExists(mainActivitySource)
        val src = codeOnly(mainActivitySource)
        assertTrue(
            "MainActivity must apply App Language via LaunchedEffect(appLanguage) (per-change), " +
                "not a per-frame SideEffect — the per-frame cadence was the black-screen amplifier.",
            src.contains("LaunchedEffect")
        )
        assertFalse(
            "MainActivity must NOT apply App Language from a per-frame SideEffect — that cadence " +
                "amplified the v0.5.2 null→label flip into a recreation loop.",
            src.contains("SideEffect") && src.contains("AppLocaleController")
        )
    }
}
