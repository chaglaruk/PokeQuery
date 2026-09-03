package com.caglar.pokequery.ui.preview

import androidx.compose.runtime.Composable
import com.caglar.pokequery.theme.PokeQueryTheme
import com.caglar.pokequery.ui.screens.EventContextScreen

@Composable
private fun EventContextPreviewContent() {
    PokeQueryTheme {
        EventContextScreen(onBack = {})
    }
}

@PokeQueryPhonePreviews
@Composable
fun EventContextScreenPhonePreviews() {
    EventContextPreviewContent()
}

@PokeQueryLocalePreviews
@Composable
fun EventContextScreenLocalePreviews() {
    EventContextPreviewContent()
}
