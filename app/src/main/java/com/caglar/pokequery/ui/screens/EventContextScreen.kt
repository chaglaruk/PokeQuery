package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.domain.events.ContextFeedState
import com.caglar.pokequery.domain.events.EventContextRepository

import com.caglar.pokequery.domain.events.MonthlyContextRepository
import com.caglar.pokequery.domain.events.MonthlyContextView
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.BorderSubtle
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.PurpleIV
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.TextTertiary
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.components.ScreenTitleBar
import com.caglar.pokequery.ui.motion.PqStaggeredEntrance
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EventContextScreen(
    onBack: () -> Unit
) {
    val density = currentDensity()
    val feedState = EventContextRepository.combined()

    PqStaggeredEntrance { visible ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(density.listGap),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ScreenTitleBar(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.events_title), onBack, Modifier.pqStaggeredItem(visible, 0).padding(bottom = 4.dp))
            }

            item {
                OfflineBanner(Modifier.pqStaggeredItem(visible, 1))
            }

            val monthly = feedState.monthly
            val view = monthly?.let { MonthlyContextView(it, it.isManual) }
            item {
                MonthlyNoteCardInner(view, Modifier.pqStaggeredItem(visible, 2))
            }
            item {
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_general_notes), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            items(EventContextRepository.all(), key = { it.id }) { event ->
                EventNoteCard(event)
            }

            item {
                Text(
                    androidx.compose.ui.res.stringResource(EventContextRepository.disclaimerRes()),
                    color = TextTertiary, fontSize = 11.sp, lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(CyanGlow.copy(alpha = 0.08f))
            .border(1.dp, CyanGlow.copy(alpha = 0.35f), shape).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(6.dp).background(CyanGlow, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(
            androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_context_offline_banner),
            color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp
        )
    }
}

@Composable
private fun MonthlyNoteCardInner(view: MonthlyContextView?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    if (view == null) {
        Column(Modifier.fillMaxWidth().then(modifier).clip(shape).background(CardDark).border(1.dp, BorderSubtle, shape).padding(12.dp)) {
            Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_context_community_day), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(androidx.compose.ui.res.stringResource(MonthlyContextRepository.noNoteMessageRes()), color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
        return
    }
    val note = view.note
    Column(Modifier.fillMaxWidth().then(modifier).clip(shape).background(CardDark).border(1.dp, BorderSubtle, shape).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_context_community_day), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            val tone = if (view.isStale) AmberWarning else CyanGlow
            val label = if (view.isStale) androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_context_may_be_outdated) else if (note.confidence.name == "MANUAL") androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_context_manual_note) else androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_context_online_note)
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(tone.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)
            ) { Text(label, color = tone, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(6.dp))
        val feedStr = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_context_from_feed)
        val sourceLabel = if (note.lastUpdatedInAppVersion == "feed") feedStr else "v${note.lastUpdatedInAppVersion}"
        Text("${note.month}/${note.year} · $sourceLabel", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        Text(androidx.compose.ui.res.stringResource(note.noteRes), color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(8.dp))
        Text(androidx.compose.ui.res.stringResource(view.disclaimerRes), color = AmberWarning, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun EventNoteCard(event: com.caglar.pokequery.domain.events.EventContext) {
    val shape = RoundedCornerShape(14.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(CardDark).border(1.dp, BorderSubtle, shape).padding(12.dp)) {
        Text(androidx.compose.ui.res.stringResource(event.titleRes), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Text(androidx.compose.ui.res.stringResource(event.noteRes), color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        if (!event.isManual) {
            Spacer(Modifier.height(4.dp))
            Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.event_context_online_feed_note), color = PurpleIV, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
