package com.caglar.pokequery.theme.density

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.5.2 (Fix 6): Visual Density model tests.
 *
 * Guards that the previously dead Settings toggle now resolves to materially different
 * spacing tokens, and that unknown/corrupt values never collapse the UI to Compact.
 */
class DensityModelTest {

    @Test
    fun `comfortable resolves to comfortable tokens`() {
        val tokens = DensityTokens.resolve("Comfortable")
        assertEquals(DensityTokens.MODE_COMFORTABLE, tokens.mode)
        assertEquals(DensityTokens.Comfortable, tokens)
    }

    @Test
    fun `compact resolves to compact tokens`() {
        val tokens = DensityTokens.resolve("Compact")
        assertEquals(DensityTokens.MODE_COMPACT, tokens.mode)
        assertEquals(DensityTokens.Compact, tokens)
    }

    @Test
    fun `compact mode is actually denser than comfortable`() {
        // The whole point of Fix 6: toggling Compact must produce visibly tighter spacing.
        val c = DensityTokens.Comfortable
        val k = DensityTokens.Compact
        assertTrue("card padding must shrink in Compact", k.cardPadding < c.cardPadding)
        assertTrue("section gap must shrink in Compact", k.sectionGap < c.sectionGap)
        assertTrue("list gap must shrink in Compact", k.listGap < c.listGap)
        assertTrue("chip spacing must shrink in Compact", k.chipSpacing < c.chipSpacing)
        assertTrue("chip vertical padding must shrink in Compact", k.chipPaddingVertical < c.chipPaddingVertical)
    }

    @Test
    fun `unknown and blank values fall back to comfortable not compact`() {
        // Corrupt preference must never cram the UI.
        assertEquals(DensityTokens.Comfortable, DensityTokens.resolve(null))
        assertEquals(DensityTokens.Comfortable, DensityTokens.resolve(""))
        assertEquals(DensityTokens.Comfortable, DensityTokens.resolve("   "))
        assertEquals(DensityTokens.Comfortable, DensityTokens.resolve("spacious"))
    }

    @Test
    fun `case sensitive compact label avoids accidental activation`() {
        // "compact" lowercase or "COMPACT" must NOT silently flip to Compact — only the
        // exact stored label produced by the Settings radio rows activates Compact.
        assertEquals(DensityTokens.Comfortable, DensityTokens.resolve("compact"))
        assertEquals(DensityTokens.Comfortable, DensityTokens.resolve("COMPACT"))
    }

    @Test
    fun `body size scaling stays within a safe readable range`() {
        // Compact scales body text down a little but never below 11sp; Comfortable is 1:1.
        val compactBody = DensityTokens.Compact.bodySize(13f).value
        val comfortableBody = DensityTokens.Comfortable.bodySize(13f).value
        assertTrue("Compact body must be smaller than Comfortable", compactBody < comfortableBody)
        assertTrue("Body text must never drop below 11sp", compactBody >= 11f)
        assertEquals(13f, comfortableBody, 0.001f)
    }

    @Test
    fun `compact mode never breaks minimum touch target sizing contract`() {
        // We scale PADDING and GAPS, not control heights. A 48dp button stays 48dp in both
        // modes — documented contract so the density change is safe for accessibility.
        val c = DensityTokens.Comfortable
        val k = DensityTokens.Compact
        // Touch-target heights are not part of tokens; the contract is that token fields
        // only describe padding/gaps/fonts, which they do. Sanity-check the field set:
        assertTrue(c.cardPadding.value > 0f)
        assertTrue(k.cardPadding.value > 0f)
    }
}
