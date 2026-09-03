package com.caglar.pokequery.ui.preview

import androidx.compose.runtime.Composable
import com.caglar.pokequery.theme.PokeQueryTheme
import com.caglar.pokequery.ui.screens.SettingsScreen

@Composable
private fun SettingsPreviewContent() {
    PokeQueryTheme {
        SettingsScreen(
            onBack = {},
            onOpenChangelog = {}
        )
    }
}

@PokeQueryPhonePreviews
@Composable
fun SettingsScreenPhonePreviews() {
    SettingsPreviewContent()
}

@PokeQueryLocalePreviews
@Composable
fun SettingsScreenLocalePreviews() {
    SettingsPreviewContent()
}
