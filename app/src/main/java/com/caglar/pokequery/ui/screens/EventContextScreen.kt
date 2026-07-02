package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.events.ContextFeedState
import com.caglar.pokequery.domain.events.EventContextRepository
import com.caglar.pokequery.domain.events.EventFeedLoader
import com.caglar.pokequery.domain.events.MonthlyContextRepository
import com.caglar.pokequery.domain.events.MonthlyContextView
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.BorderSubtle
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CardPremium
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.PurpleIV
import com.caglar.pokequery.theme.SlateBlack
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EventContextScreen(
    onBack: () -> Unit
) {
    val density = currentDensity()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    var feedState by remember { mutableStateOf<ContextFeedState>(ContextFeedState.Loading()) }
    var refreshing by remember { mutableStateOf(false) }
    var lastChecked by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        refreshing = true
        feedState = ContextFeedState.Loading(feedState.monthly, feedState.events)
        scope.launch {
            feedState = withContext(Dispatchers.IO) {
                if (userPrefs?.eventGuideUpdatesEnabled == false) {
                    EventContextRepository.combined()
                } else {
                    EventFeedLoader.load(
                        context.applicationContext,
                        preferCachedOnFailure = userPrefs?.eventGuidePreferSavedOffline ?: true
                    )
                }
            }
            lastChecked = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            refreshing = false
        }
    }
    androidx.compose.runtime.LaunchedEffect(userPrefs?.eventGuideRefreshOnOpen, userPrefs?.eventGuideUpdatesEnabled) {
        if (userPrefs == null) return@LaunchedEffect
        if (userPrefs?.eventGuideRefreshOnOpen == false) {
            feedState = EventContextRepository.combined()
        } else {
            refresh()
        }
    }

    PqStaggeredEntrance { visible ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(density.listGap),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pqStaggeredItem(visible, 0)
                        .padding(bottom = 4.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.events_title),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                FeedStatusBanner(feedState, refreshing, lastChecked, onRefresh = { refresh() }, Modifier.pqStaggeredItem(visible, 1))
            }

            if (userPrefs?.eventGuideShowPlanningHints != false) {
                val eventSourceLabelRes = when (feedState) {
                    is ContextFeedState.Online -> R.string.event_status_live_feed
                    is ContextFeedState.StaleCache -> R.string.event_status_saved_guide
                    else -> R.string.event_status_bundled_fallback
                }
                item {
                    Text(
                        text = stringResource(R.string.event_general_notes),
                        color = TealPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .pqStaggeredItem(visible, 2)
                    )
                }
                items(feedState.events, key = { it.id }) { event ->
                    EventNoteCard(event, eventSourceLabelRes, Modifier.pqStaggeredItem(visible, 3))
                }
            }

            item {
                Text(
                    text = stringResource(EventContextRepository.disclaimerRes()),
                    color = TextTertiary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .pqStaggeredItem(visible, 4)
                )
            }
        }
    }
}

@Composable
private fun FeedStatusBanner(
    feedState: ContextFeedState,
    refreshing: Boolean,
    lastChecked: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CyanGlow.copy(alpha = 0.08f))
            .border(1.dp, CyanGlow.copy(alpha = 0.35f), shape)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(8.dp)
                    .offset(y = 4.dp)
                    .background(CyanGlow, CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = when (feedState) {
                    is ContextFeedState.Loading -> stringResource(R.string.event_context_loading)
                    is ContextFeedState.Online -> stringResource(R.string.event_context_online_banner, feedState.lastUpdated)
                    is ContextFeedState.StaleCache -> stringResource(R.string.event_context_cached_banner, feedState.lastUpdated)
                    is ContextFeedState.Invalid -> stringResource(R.string.event_context_invalid_banner)
                    is ContextFeedState.OfflineOnly -> stringResource(R.string.event_context_offline_banner)
                },
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        lastChecked?.let {
            Text(
                text = stringResource(R.string.event_context_last_checked, it),
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = onRefresh,
            enabled = !refreshing,
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = SlateBlack),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (refreshing) {
                CircularProgressIndicator(color = SlateBlack, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = stringResource(R.string.events_refresh),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun MonthlyNoteCardInner(view: MonthlyContextView?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    if (view == null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(CardDark)
                .border(1.dp, BorderSubtle, shape)
                .padding(14.dp)
        ) {
            Text(
                text = stringResource(R.string.event_context_community_day),
                color = TealPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(MonthlyContextRepository.noNoteMessageRes()),
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        return
    }
    val note = view.note
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(CardPremium, CardDark)))
            .border(1.dp, TealPrimary.copy(alpha = 0.25f), shape)
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.event_context_community_day),
                color = TealPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            val tone = if (view.isStale) AmberWarning else CyanGlow
            val label = if (view.isStale) {
                stringResource(R.string.event_context_may_be_outdated)
            } else if (note.confidence.name == "MANUAL") {
                stringResource(R.string.event_context_manual_note)
            } else {
                stringResource(R.string.event_context_online_note)
            }
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(tone.copy(alpha = 0.16f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(label, color = tone, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        val feedStr = stringResource(R.string.event_context_from_feed)
        val sourceLabel = if (note.lastUpdatedInAppVersion == "feed") feedStr else "v${note.lastUpdatedInAppVersion}"
        Text(
            text = "${note.month}/${note.year} · $sourceLabel",
            color = TextSecondary,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(note.noteRes),
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(view.disclaimerRes),
            color = AmberWarning,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun EventNoteCard(
    event: com.caglar.pokequery.domain.events.EventContext,
    sourceLabelRes: Int,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val clipboard = LocalClipboardManager.current
    val useTurkish = Locale.getDefault().language == "tr"
    fun choose(en: String?, tr: String?): String = if (useTurkish && !tr.isNullOrBlank()) tr else en.orEmpty()
    val timingLabel = when (event.status) {
        com.caglar.pokequery.domain.events.EventStatus.UPCOMING -> stringResource(R.string.event_label_upcoming)
        com.caglar.pokequery.domain.events.EventStatus.CURRENT -> stringResource(R.string.event_label_current)
    }
    val tone = when (event.themeKey) {
        "candy_bonus" -> AmberWarning
        "trade_bonus" -> PurpleIV
        "raid" -> GoldCaution
        "spotlight_hour" -> CyanGlow
        "community_day" -> TealPrimary
        else -> CyanGlow
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(tone.copy(alpha = 0.14f), CardDark)))
            .border(1.dp, tone.copy(alpha = 0.25f), shape)
            .padding(14.dp)
    ) {
        val title = event.titleText ?: event.titleRes?.let { stringResource(it) }.orEmpty()
        val displayTitle = if (useTurkish && !event.titleTextTr.isNullOrBlank()) event.titleTextTr else title
        val note = choose(event.noteText ?: event.noteRes?.let { stringResource(it) }, event.noteTextTr)
        val summary = choose(event.summaryText, event.summaryTextTr)
        val prep = choose(event.prepText, event.prepTextTr)
        val eventNotes = choose(event.eventNotesText, event.eventNotesTextTr)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EventThemeMark(event.themeKey, tone, Modifier.size(44.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = timingLabel,
                        color = tone,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = stringResource(sourceLabelRes),
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = event.themeKey.replace('_', ' '),
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(CardPremium.copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = displayTitle,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(6.dp))
        val dateText = when {
            !event.startText.isNullOrBlank() && !event.endText.isNullOrBlank() -> stringResource(R.string.event_date_range, event.startText, event.endText)
            !event.startText.isNullOrBlank() -> stringResource(R.string.event_date_starts, event.startText)
            !event.endText.isNullOrBlank() -> stringResource(R.string.event_date_ends, event.endText)
            event.month != null && event.year != null -> stringResource(R.string.event_month_year, event.month, event.year)
            else -> null
        }
        if (dateText != null) {
            Text(
                text = dateText,
                color = TextTertiary,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(note, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.event_card_summary),
            color = TealPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(summary, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.event_prep_guidance),
            color = TealPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(prep, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        val search = event.suggestedSearch.orEmpty()
        if (search.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.event_card_search),
                color = TealPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateBlack.copy(alpha = 0.55f))
                    .border(1.dp, tone.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(search, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { clipboard.setText(AnnotatedString(search)) },
                    colors = ButtonDefaults.buttonColors(containerColor = tone, contentColor = SlateBlack),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.event_card_copy_search), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.event_card_notes),
            color = TealPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(eventNotes, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.event_safety_note),
            color = AmberWarning,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun EventThemeMark(themeKey: String, tone: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(14.dp)).background(tone.copy(alpha = 0.12f))) {
        val stroke = Stroke(width = size.minDimension * 0.08f)
        val w = size.width
        val h = size.height
        when (themeKey) {
            "community_day" -> {
                drawCircle(tone.copy(alpha = 0.28f), radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawLine(tone, Offset(w * 0.5f, h * 0.18f), Offset(w * 0.5f, h * 0.82f), strokeWidth = w * 0.07f)
                drawLine(tone, Offset(w * 0.18f, h * 0.5f), Offset(w * 0.82f, h * 0.5f), strokeWidth = w * 0.07f)
            }
            "candy_bonus" -> {
                drawCircle(tone.copy(alpha = 0.35f), radius = w * 0.20f, center = Offset(w * 0.42f, h * 0.5f))
                drawCircle(tone.copy(alpha = 0.18f), radius = w * 0.20f, center = Offset(w * 0.60f, h * 0.5f), style = stroke)
                drawLine(tone, Offset(w * 0.18f, h * 0.35f), Offset(w * 0.30f, h * 0.65f), strokeWidth = w * 0.06f)
                drawLine(tone, Offset(w * 0.82f, h * 0.35f), Offset(w * 0.70f, h * 0.65f), strokeWidth = w * 0.06f)
            }
            "trade_bonus" -> {
                drawLine(tone, Offset(w * 0.22f, h * 0.38f), Offset(w * 0.78f, h * 0.38f), strokeWidth = w * 0.07f)
                drawLine(tone, Offset(w * 0.78f, h * 0.38f), Offset(w * 0.62f, h * 0.24f), strokeWidth = w * 0.07f)
                drawLine(tone, Offset(w * 0.22f, h * 0.62f), Offset(w * 0.78f, h * 0.62f), strokeWidth = w * 0.07f)
                drawLine(tone, Offset(w * 0.22f, h * 0.62f), Offset(w * 0.38f, h * 0.76f), strokeWidth = w * 0.07f)
            }
            else -> {
                drawCircle(tone.copy(alpha = 0.20f), radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(tone, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.5f))
            }
        }
    }
}
