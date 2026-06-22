package com.caglar.pokequery.theme.density

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * v0.5.2 (Fix 6): Visual Density — a pure, testable spacing/typography model.
 *
 * Previously the Settings "Visual Density" switch was stored in DataStore but never read
 * by any screen, so toggling it changed nothing visible. This object turns the stored
 * string ("Comfortable" / "Compact") into a concrete set of spacing/sizing tokens, and
 * [LocalDensityTokens] exposes the resolved tokens to the whole composition.
 *
 * The two modes scale consistently:
 *   - Comfortable: the v0.5.0/v0.5.1 spacing — generous, premium feel.
 *   - Compact: tighter card padding, chip spacing, section gaps and list gaps, and a very
 *     small (safe) font-scale nudge on body text. Title/hero sizes are NOT scaled — they
 *     are brand language and shrinking them would hurt identity and readability.
 *
 * Design notes:
 *   - Touch targets are never reduced below Material's 48dp/44dp guidance. Only the
 *     *padding around* controls and the *gaps between* elements change.
 *   - Only body/surface text scales (e.g. PqStringBox, descriptions) and only slightly.
 *     This keeps the app from feeling cramped while making Compact visibly denser.
 */
data class DensityTokens(
    val mode: String,
    val cardPadding: Dp,
    val glowCardPadding: Dp,
    val sectionGap: Dp,
    val listGap: Dp,
    val chipSpacing: Dp,
    val chipPaddingVertical: Dp,
    val chipPaddingHorizontal: Dp,
    val innerElementGap: Dp,
    val bodyTextScale: Float
) {
    /** Scaled body font size, clamped to a safe readable range (never below 11sp). */
    fun bodySize(baseSp: Float): TextUnit =
        (baseSp * bodyTextScale).coerceIn(11f, 22f).sp

    companion object {
        /** Sentinel stored in DataStore. Kept here as the single source of truth. */
        const val MODE_COMFORTABLE = "Comfortable"
        const val MODE_COMPACT = "Compact"

        val Comfortable = DensityTokens(
            mode = MODE_COMFORTABLE,
            cardPadding = 16.dp,
            glowCardPadding = 16.dp,
            sectionGap = 18.dp,
            listGap = 12.dp,
            chipSpacing = 8.dp,
            chipPaddingVertical = 7.dp,
            chipPaddingHorizontal = 12.dp,
            innerElementGap = 10.dp,
            bodyTextScale = 1.00f
        )

        val Compact = DensityTokens(
            mode = MODE_COMPACT,
            cardPadding = 11.dp,
            glowCardPadding = 12.dp,
            sectionGap = 11.dp,
            listGap = 7.dp,
            chipSpacing = 6.dp,
            chipPaddingVertical = 5.dp,
            chipPaddingHorizontal = 10.dp,
            innerElementGap = 7.dp,
            bodyTextScale = 0.96f
        )

        /**
         * Pure resolver used both by the UI and by unit tests. Unknown/blank values fall
         * back to Comfortable (the long-standing default) — never to Compact, so a corrupt
         * preference cannot accidentally cram the UI.
         */
        fun resolve(storedValue: String?): DensityTokens = when (storedValue?.trim()) {
            MODE_COMPACT -> Compact
            else -> Comfortable
        }
    }
}

/**
 * Composition-local carrying the resolved [DensityTokens]. Defaulted to Comfortable so
 * previews and un-wrapped compositions keep the existing look.
 */
val LocalDensityTokens = compositionLocalOf { DensityTokens.Comfortable }

@Composable
@ReadOnlyComposable
fun currentDensity(): DensityTokens = LocalDensityTokens.current
