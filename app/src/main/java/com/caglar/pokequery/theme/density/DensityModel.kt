package com.caglar.pokequery.theme.density

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Fixed density tokens — always Comfortable spacing. The user-facing Visual Density
 * toggle was removed in v0.6.2 polish because it added complexity for minimal utility.
 * All screens now use the same generous, premium spacing.
 */
data class DensityTokens(
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
    fun bodySize(baseSp: Float): TextUnit =
        (baseSp * bodyTextScale).coerceIn(11f, 22f).sp

    companion object {
        val Default = DensityTokens(
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
    }
}

@Composable
@ReadOnlyComposable
fun currentDensity(): DensityTokens = DensityTokens.Default
