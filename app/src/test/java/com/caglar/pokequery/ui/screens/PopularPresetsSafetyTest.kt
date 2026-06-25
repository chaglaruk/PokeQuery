package com.caglar.pokequery.ui.screens

import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.4.2 safety patch (Fix 4) — preset safety contract regression test.
 *
 * Audit finding (BUG-007): presets are built via
 *   StringBuilderEngine.buildString(baseQuery = preset.syntax, protections = emptyList(), ...)
 * which opts out of the optional DEFAULT_PROTECTIONS layer. The engine's
 * COUNT_MANDATORY_PROTECTIONS path still fires for any 'count' token, but a latent
 * foot-gun exists for any future preset that uses a risky cleanup/IV-band token without
 * count. This parameterized test pins the contract: every preset containing 'count',
 * '0*', '1*', or '2*' must, after the same build path the screen uses, include the full
 * set of required mandatory protections.
 *
 * SAFETY CONTRACT (documented for maintainers):
 *   - Presets are passed through StringBuilderEngine.buildString with emptyList() so the
 *     optional default-protection layer does NOT apply by design (presets are
 *     self-contained, hand-authored strings).
 *   - The engine STILL enforces COUNT_MANDATORY_PROTECTIONS whenever 'count' is present.
 *   - Therefore any count-bearing preset is protected by the engine's fail-closed path.
 *   - Any preset bearing an IV-band token (0-star, 1-star, or 2-star) WITHOUT 'count'
 *     must either include the mandatory protections inline, or be re-routed through
 *     buildString with DEFAULT_PROTECTIONS. The test below enforces this.
 */
class PopularPresetsSafetyTest {

    // Required protections for count-style / IV-band cleanup (per v0.4.2 spec + engine).
    // v0.5.5 (Fix 6): mirrors the strengthened engine COUNT_MANDATORY_PROTECTIONS, which now
    // also protects Ultra Beasts and the background variants from landing in a count/cleanup
    // list. Count-bearing presets get these from the engine's fail-closed path; IV-band presets
    // include them inline in their hand-authored syntax.
    private val requiredProtections = listOf(
        "shiny", "lucky", "legendary", "mythical", "shadow", "purified",
        "favorite", "traded", "costume",
        "ultrabeast", "background", "locationbackground", "specialbackground"
    )

    // Mirrors the exact call PresetsScreen makes when a preset is copied.
    private fun buildPresetOutput(syntax: String, risk: RiskLevel): String =
        StringBuilderEngine.buildString(
            baseQuery = syntax,
            protections = emptyList(),
            explanation = "",
            riskLevel = risk,
            goalId = "preset",
            title = "preset",
            language = "English"
        ).rawSyntax

    @Test
    fun `every count or IV-band preset carries all mandatory protections after build`() {
        val riskyPresets = POPULAR_PRESETS.filter { preset ->
            val s = preset.syntax
            s.contains("count") || s.contains("0*") || s.contains("1*") || s.contains("2*")
        }

        // Guard: the filter must actually select presets, otherwise the test is vacuous.
        assertTrue("Expected at least one risky preset to test", riskyPresets.isNotEmpty())

        riskyPresets.forEach { preset ->
            val output = buildPresetOutput(preset.syntax, preset.risk)
            requiredProtections.forEach { token ->
                assertTrue(
                    "Preset '${preset.title}' (syntax='${preset.syntax}') output '$output' is missing required mandatory protection '!$token'",
                    output.contains("!$token")
                )
            }
        }
    }
}
