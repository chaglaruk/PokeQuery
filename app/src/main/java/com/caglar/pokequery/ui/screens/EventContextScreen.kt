package com.caglar.pokequery.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.caglar.pokequery.domain.events.EventContext
import com.caglar.pokequery.domain.events.EventContextRepository
import com.caglar.pokequery.domain.events.EventFeedLoader
import com.caglar.pokequery.domain.events.EventStatus
import com.caglar.pokequery.domain.events.selectMainEvent
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
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
import com.caglar.pokequery.ui.motion.PqStaggeredEntrance
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.motion.rememberReducedMotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * v0.6.9 Event Guide — human-friendly single main event card.
 *
 * The screen features ONE main event (current, or the nearest upcoming) instead of a technical
 * multi-card debug panel. A soft, code-drawn scan/ribbon visual sits behind the content (IP-safe,
 * no official art). The refresh control is a compact icon-first button integrated into the header.
 */
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

    val mainEvent = selectMainEvent(feedState.events)
    val sourceLabelRes = when (feedState) {
        is ContextFeedState.Online -> R.string.event_status_live_feed
        is ContextFeedState.StaleCache -> R.string.event_status_saved_guide
        else -> R.string.event_status_bundled_fallback
    }
    val isLoading = feedState is ContextFeedState.Loading && mainEvent == null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Integrated, full-bleed background visual — soft scan/ribbon behind content.
        EventGuideBackground(
            active = !rememberReducedMotion(),
            modifier = Modifier.matchParentSize()
        )

        PqStaggeredEntrance { visible ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(density.listGap),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp)
            ) {
                // Header: back, title, compact refresh.
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pqStaggeredItem(visible, 0)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        CompactRefreshButton(
                            refreshing = refreshing,
                            onRefresh = { refresh() },
                            modifier = Modifier.pqStaggeredItem(visible, 1)
                        )
                    }
                }

                // Compact source / last-checked line.
                item {
                    SourceStatusLine(
                        feedState = feedState,
                        sourceLabelRes = sourceLabelRes,
                        lastChecked = lastChecked,
                        modifier = Modifier.pqStaggeredItem(visible, 1)
                    )
                }

                when {
                    isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = TealPrimary, strokeWidth = 3.dp)
                            }
                        }
                    }
                    mainEvent != null -> {
                        item {
                            EventMainCard(
                                event = mainEvent,
                                sourceLabelRes = sourceLabelRes,
                                modifier = Modifier.pqStaggeredItem(visible, 2)
                            )
                        }
                    }
                    else -> {
                        item {
                            EventEmptyState(
                                modifier = Modifier.pqStaggeredItem(visible, 2),
                                onRefresh = { refresh() }
                            )
                        }
                    }
                }

                // Honest disclaimer — always present.
                item {
                    Text(
                        text = stringResource(EventContextRepository.disclaimerRes()),
                        color = TextTertiary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .pqStaggeredItem(visible, 3)
                    )
                }
            }
        }
    }
}

/**
 * Compact, icon-first refresh button integrated into the header. Subtle circular tinted background,
 * shows a spinner while refreshing. Discoverable but no longer a large text-heavy box.
 */
@Composable
private fun CompactRefreshButton(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onRefresh,
        enabled = !refreshing,
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(TealPrimary.copy(alpha = 0.16f))
            .border(1.dp, TealPrimary.copy(alpha = 0.3f), CircleShape)
    ) {
        if (refreshing) {
            CircularProgressIndicator(
                color = TealPrimary,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.events_refresh),
                tint = TealPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * One-line source + last-checked status. Replaces the old large status banner so the screen leads
 * with the event itself, not a status panel.
 */
@Composable
private fun SourceStatusLine(
    feedState: ContextFeedState,
    sourceLabelRes: Int,
    lastChecked: String?,
    modifier: Modifier = Modifier
) {
    val statusText = when (feedState) {
        is ContextFeedState.Online -> stringResource(R.string.event_context_online_banner, feedState.lastUpdated)
        is ContextFeedState.StaleCache -> stringResource(R.string.event_context_cached_banner, feedState.lastUpdated)
        is ContextFeedState.Loading -> stringResource(R.string.event_context_loading)
        is ContextFeedState.Invalid -> stringResource(R.string.event_context_invalid_banner)
        is ContextFeedState.OfflineOnly -> stringResource(R.string.event_context_offline_banner)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(CyanGlow, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(sourceLabelRes),
                color = CyanGlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = statusText,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
        lastChecked?.let {
            Text(
                text = stringResource(R.string.event_context_last_checked, it),
                color = TextTertiary,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * The single featured event card. Shows the current event (or nearest upcoming) with featured
 * Pokémon, bonuses, ELI5 sections, suggested search + copy, and event-specific notes.
 */
@Composable
private fun EventMainCard(
    event: EventContext,
    sourceLabelRes: Int,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val clipboard = LocalClipboardManager.current
    val useTurkish = Locale.getDefault().language == "tr"
    fun choose(en: String?, tr: String?): String = if (useTurkish && !tr.isNullOrBlank()) tr else en.orEmpty()

    val tone = themeTone(event.themeKey)
    val isCurrent = event.status == EventStatus.CURRENT
    val badgeLabel = stringResource(
        if (isCurrent) R.string.event_main_card_live_now else R.string.event_main_card_coming_up
    )
    val title = if (useTurkish && !event.titleTextTr.isNullOrBlank()) event.titleTextTr else event.titleText.orEmpty()
    val featured = choose(event.featuredPokemon, event.featuredPokemonTr)
    val bonuses = choose(event.bonusesText, event.bonusesTextTr)
    val note = choose(event.noteText, event.noteTextTr)
    val summary = choose(event.summaryText, event.summaryTextTr)
    val prep = choose(event.prepText, event.prepTextTr)
    val eventNotes = choose(event.eventNotesText, event.eventNotesTextTr)
    val search = event.suggestedSearch.orEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(tone.copy(alpha = 0.16f), CardDark)))
            .border(1.dp, tone.copy(alpha = 0.30f), shape)
            .padding(18.dp)
    ) {
        // Top: badge + theme mark.
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(tone.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .background(tone, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(badgeLabel, color = tone, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
            EventThemeMark(event.themeKey, tone, Modifier.size(40.dp))
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 22.sp
        )

        // Date range.
        val dateText = when {
            !event.startText.isNullOrBlank() && !event.endText.isNullOrBlank() ->
                stringResource(R.string.event_date_range, event.startText, event.endText)
            !event.startText.isNullOrBlank() -> stringResource(R.string.event_date_starts, event.startText)
            !event.endText.isNullOrBlank() -> stringResource(R.string.event_date_ends, event.endText)
            event.month != null && event.year != null ->
                stringResource(R.string.event_month_year, event.month, event.year)
            else -> null
        }
        if (dateText != null) {
            Spacer(Modifier.height(4.dp))
            Text(dateText, color = TextTertiary, fontSize = 12.sp)
        }

        // Featured Pokémon + bonuses — the two things users care about most.
        if (featured.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            SectionLabel(stringResource(R.string.event_featured_pokemon), tone)
            Spacer(Modifier.height(3.dp))
            Text(featured, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        if (bonuses.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            SectionLabel(stringResource(R.string.event_bonuses), tone)
            Spacer(Modifier.height(3.dp))
            Text(bonuses, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
        }

        // What's happening? (ELI5 note)
        Spacer(Modifier.height(14.dp))
        SectionLabel(stringResource(R.string.event_whats_happening), tone)
        Spacer(Modifier.height(3.dp))
        Text(note, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)

        // Why care about this? (summary)
        Spacer(Modifier.height(12.dp))
        SectionLabel(stringResource(R.string.event_why_care), tone)
        Spacer(Modifier.height(3.dp))
        Text(summary, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)

        // What to do in PokeQuery (prep).
        Spacer(Modifier.height(12.dp))
        SectionLabel(stringResource(R.string.event_what_to_do), tone)
        Spacer(Modifier.height(3.dp))
        Text(prep, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)

        // Suggested search with copy.
        if (search.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            SectionLabel(stringResource(R.string.event_suggested_for_event), tone)
            Spacer(Modifier.height(5.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateBlack.copy(alpha = 0.6f))
                    .border(1.dp, tone.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = search,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { clipboard.setText(AnnotatedString(search)) },
                    colors = ButtonDefaults.buttonColors(containerColor = tone, contentColor = SlateBlack),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        stringResource(R.string.event_card_copy_search),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Keep & review — event-specific notes.
        if (eventNotes.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            SectionLabel(stringResource(R.string.event_keep_review), AmberWarning)
            Spacer(Modifier.height(3.dp))
            Text(eventNotes, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
        }

        // Check before/during/after — the safety reminder, tied to this event.
        Spacer(Modifier.height(12.dp))
        SectionLabel(stringResource(R.string.event_check_before), AmberWarning)
        Spacer(Modifier.height(3.dp))
        Text(
            text = stringResource(R.string.event_safety_note),
            color = AmberWarning,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        // Source label.
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(sourceLabelRes),
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(CardPremium.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun EventEmptyState(modifier: Modifier = Modifier, onRefresh: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardDark)
            .border(1.dp, TealPrimary.copy(alpha = 0.2f), shape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.event_no_events_title),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.event_no_events_desc),
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = SlateBlack),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.events_refresh), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun themeTone(themeKey: String): Color = when (themeKey) {
    "candy_bonus" -> AmberWarning
    "trade_bonus" -> PurpleIV
    "raid" -> GoldCaution
    "spotlight_hour" -> CyanGlow
    "community_day" -> TealPrimary
    else -> CyanGlow
}

/**
 * Integrated, full-bleed background visual for the Event Guide. A soft scan/ribbon motif blending
 * AI-assist + GO-style search + PokeQuery identity, drawn entirely with Compose primitives.
 * IP-safe: no images, sprites, or official art. Honors reduced motion.
 */
@Composable
private fun EventGuideBackground(active: Boolean, modifier: Modifier = Modifier) {
    val sweep = if (active) {
        val transition = rememberInfiniteTransition(label = "event_scan")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 9000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "event_sweep"
        ).value
    } else {
        120f
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.78f
        val cy = h * 0.14f
        val maxR = size.minDimension * 0.45f

        // Top-right soft glow wash.
        drawCircle(
            color = TealPrimary.copy(alpha = 0.05f),
            radius = maxR * 1.4f,
            center = Offset(cx, cy)
        )

        // Faint radar rings anchored top-right.
        for (i in 1..4) {
            val r = maxR * (i / 4f)
            drawCircle(
                color = TealPrimary.copy(alpha = 0.06f - (i * 0.01f)),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 1.2f)
            )
        }

        // Slow sweeping scan wedge.
        rotate(sweep, pivot = Offset(cx, cy)) {
            for (i in 0..10) {
                val a = (10 - i) * 0.012f
                drawLine(
                    color = CyanGlow.copy(alpha = a),
                    start = Offset(cx, cy),
                    end = Offset(
                        cx + maxR * cos(Math.toRadians(0.0)).toFloat(),
                        cy + maxR * sin(Math.toRadians(0.0)).toFloat()
                    ),
                    strokeWidth = (10 - i).toFloat(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Sparse dot field — suggests event "blips".
        val dots = listOf(
            Offset(cx - maxR * 0.4f, cy - maxR * 0.1f),
            Offset(cx + maxR * 0.3f, cy - maxR * 0.35f),
            Offset(cx + maxR * 0.5f, cy + maxR * 0.15f),
            Offset(w * 0.12f, h * 0.35f),
            Offset(w * 0.2f, h * 0.62f)
        )
        dots.forEachIndexed { i, p ->
            if (p.x in 0f..w && p.y in 0f..h) {
                drawCircle(
                    color = CyanGlow.copy(alpha = 0.22f - (i * 0.03f)),
                    radius = 2.5f,
                    center = p
                )
            }
        }
    }
}

@Composable
private fun EventThemeMark(themeKey: String, tone: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tone.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = modifier) {
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
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = tone.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}
