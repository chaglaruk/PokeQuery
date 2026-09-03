package com.caglar.pokequery.ui.preview

import androidx.compose.runtime.Composable
import com.caglar.pokequery.theme.PokeQueryTheme
import com.caglar.pokequery.ui.screens.SearchAssistantScreen

@Composable
private fun SearchAssistantPreviewContent() {
    PokeQueryTheme {
        SearchAssistantScreen(
            onBack = {},
            onCopyRaw = {},
            onExplain = {}
        )
    }
}

@PokeQueryPhonePreviews
@Composable
fun SearchAssistantScreenPhonePreviews() {
    SearchAssistantPreviewContent()
}

@PokeQueryLocalePreviews
@Composable
fun SearchAssistantScreenLocalePreviews() {
    SearchAssistantPreviewContent()
}
