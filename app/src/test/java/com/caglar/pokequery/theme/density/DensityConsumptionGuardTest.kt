package com.caglar.pokequery.theme.density

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * v0.5.5 (Fix 1): regression guard that Visual Density is actually consumed.
 *
 * The original audit finding: `LocalDensityTokens` was provided in `Navigation.kt`, but the
 * token fields that drive the dominant visual rhythm (`sectionGap`, `listGap`,
 * `innerElementGap`) were defined and not consumed by any screen — so toggling Comfortable vs
 * Compact produced little visible change. PqCard/PqGlowCard/PqChip/PqStringBox already consumed
 * the card/chip/text tokens; v0.5.5 wires the list/section/inner tokens into the LazyColumn
 * arrangements and section Spacers across every screen.
 *
 * This test reads the production source and fails if:
 *   - `LocalDensityTokens`/`currentDensity()` is referenced ONLY in the definition/provider
 *     files (DensityModel.kt + Navigation.kt), i.e. the dead-toggle regression returned; OR
 *   - any of the three list/section tokens (`sectionGap`, `listGap`, `innerElementGap`) is
 *     referenced nowhere outside the model.
 *
 * It is a source-level guard (not a behavioural one) because the whole point is that the UI
 * *reads* the composition local; a pure model test cannot observe that.
 */
class DensityConsumptionGuardTest {

    private val mainSourceDir = File("src/main/java/com/caglar/pokequery")

    private fun productionKtFiles(): List<File> =
        mainSourceDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            // Exclude the model file itself (it defines the tokens, so it trivially references
            // them). Navigation.kt is a CONSUMER (provider) so it counts.
            .filter { !it.absolutePath.replace('\\', '/').endsWith("/theme/density/DensityModel.kt") }
            .toList()

    @Test
    fun `density is consumed by real ui files not only its definition and provider`() {
        // `currentDensity()` is the idiomatic read accessor (ReadOnlyComposable). Counting it
        // across the UI/screens/pq trees proves the composition local is actually read.
        val consumers = productionKtFiles().filter { file ->
            file.readText().contains("currentDensity()") || file.readText().contains("LocalDensityTokens.current")
        }
        // Expect several distinct consumers (screens + pq primitives), not just one or two.
        assertTrue(
            "Visual Density must be consumed by real UI files. Found ${consumers.size} consumers: " +
                consumers.joinToString { it.name },
            consumers.size >= 4
        )
    }

    @Test
    fun `section gap token is consumed outside the density model`() {
        val consumers = productionKtFiles().filter { it.readText().contains(".sectionGap") }
        assertFalse(
            "density.sectionGap is defined but never consumed — the Settings/Goal Detail section " +
                "rhythm would not respond to Compact. Consumers: ${consumers.map { it.name }}",
            consumers.isEmpty()
        )
    }

    @Test
    fun `list gap token is consumed outside the density model`() {
        val consumers = productionKtFiles().filter { it.readText().contains(".listGap") }
        assertFalse(
            "density.listGap is defined but never consumed — list screens (KB, Favorites, History, " +
                "Presets) would not respond to Compact. Consumers: ${consumers.map { it.name }}",
            consumers.isEmpty()
        )
    }

    @Test
    fun `inner element gap token is consumed outside the density model`() {
        val consumers = productionKtFiles().filter { it.readText().contains(".innerElementGap") }
        assertFalse(
            "density.innerElementGap is defined but never consumed — intra-card spacing would not " +
                "respond to Compact. Consumers: ${consumers.map { it.name }}",
            consumers.isEmpty()
        )
    }
}
