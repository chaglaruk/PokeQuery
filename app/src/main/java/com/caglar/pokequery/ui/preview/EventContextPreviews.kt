package com.caglar.pokequery.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.caglar.pokequery.R
import com.caglar.pokequery.domain.events.ContextFeedState
import com.caglar.pokequery.domain.events.EventFeedParser
import com.caglar.pokequery.domain.events.MonthlyContextRepository
import com.caglar.pokequery.ui.screens.EventContextScreen

@Composable
private fun EventContextPreviewContent() {
    val context = LocalContext.current
    val previewFeedState = remember {
        val json = context.resources.openRawResource(R.raw.event_context_fallback)
            .bufferedReader()
            .use { it.readText() }
        val feed = EventFeedParser.parse(json).getOrThrow()
        ContextFeedState.OfflineOnly(
            monthly = MonthlyContextRepository.current,
            events = feed.events
        )
    }

    PokeQueryPreviewFrame(currentRoute = "builder") {
        EventContextScreen(
            onBack = {},
            initialFeedState = previewFeedState,
            autoRefresh = false
        )
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
