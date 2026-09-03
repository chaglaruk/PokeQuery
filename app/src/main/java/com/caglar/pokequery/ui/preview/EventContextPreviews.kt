package com.caglar.pokequery.ui.preview

import androidx.compose.runtime.Composable
import com.caglar.pokequery.ui.screens.EventContextScreen

@Composable
private fun EventContextPreviewContent() {
    PokeQueryPreviewFrame(currentRoute = "builder") {
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
