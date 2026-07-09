package com.caglar.pokequery.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import androidx.compose.ui.platform.LocalConfiguration
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.events.*
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
    val currentLocale = LocalConfiguration.current.locales[0]
    val lang = currentLocale.language
    val scope = rememberCoroutineScope()
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    var feedState by remember { mutableStateOf<ContextFeedState>(ContextFeedState.Loading()) }
    var refreshing by remember { mutableStateOf(false) }
    var lastChecked by remember { mutableStateOf<String?>(null) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        refreshing = true
        feedState = ContextFeedState.Loading(feedState.monthly, feedState.events)
        scope.launch {
            feedState = withContext(Dispatchers.IO) {
                EventFeedLoader.load(
                    context.applicationContext,
                    preferCachedOnFailure = userPrefs?.eventGuidePreferSavedOffline ?: true
                )
            }
            lastChecked = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            refreshing = false
        }
    }

    var hasAutoRefreshed by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Auto-refresh on every Event Guide open (online-first, cache fallback, bundled fallback).
        // Manual "Refresh now" button remains for user-initiated refresh.
        if (!hasAutoRefreshed) {
            hasAutoRefreshed = true
            refresh()
        }
    }

    val visibleEvents = activeEvents(feedState.events)
    // Event selection is now handled by groupEvents() inside LazyColumn
    androidx.compose.runtime.LaunchedEffect(visibleEvents.map { it.id }) {
        if (selectedEventId == null || visibleEvents.none { it.id == selectedEventId }) {
            selectedEventId = selectMainEvent(visibleEvents)?.id
        }
    }
    val sourceLabelRes = when (feedState) {
        is ContextFeedState.Online -> R.string.event_status_live_feed
        is ContextFeedState.StaleCache -> R.string.event_status_saved_guide
        else -> R.string.event_status_bundled_fallback
    }
    val isLoading = feedState is ContextFeedState.Loading && visibleEvents.isEmpty()

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
                    visibleEvents.isNotEmpty() -> {
                        val sections = groupEvents(visibleEvents)

                        // ── Featured hero card ──
                        if (sections.featured != null) {
                            item {
                                SectionHeader(
                                    title = sectionTitle("featured", lang),
                                    modifier = Modifier.pqStaggeredItem(visible, 2)
                                )
                            }
                            item {
                                EventMainCard(
                                    event = sections.featured,
                                    sourceLabelRes = sourceLabelRes,
                                    lastChecked = lastChecked,
                                    lang = lang,
                                    modifier = Modifier.pqStaggeredItem(visible, 3)
                                )
                            }
                        }

                        // ── Happening Now ──
                        if (sections.happeningNow.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = sectionTitle("live", lang),
                                    modifier = Modifier.pqStaggeredItem(visible, 4)
                                )
                            }
                            sections.happeningNow.take(3).forEachIndexed { idx, event ->
                                item(key = "now-${event.id}") {
                                    CompactEventCard(
                                        event = event,
                                        lang = lang,
                                        statusLabel = if (lang == "tr") "Canlı" else "Live",
                                        statusColor = CyanGlow,
                                        onClick = { selectedEventId = event.id },
                                        modifier = Modifier.pqStaggeredItem(visible, 5 + idx)
                                    )
                                }
                            }
                        }

                        // ── Important Upcoming ──
                        if (sections.importantUpcoming.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = sectionTitle("upcoming", lang),
                                    modifier = Modifier.pqStaggeredItem(visible, 6)
                                )
                            }
                            sections.importantUpcoming.forEachIndexed { idx, event ->
                                item(key = "up-${event.id}") {
                                    CompactEventCard(
                                        event = event,
                                        lang = lang,
                                        statusLabel = if (lang == "tr") "Yakında" else "Upcoming",
                                        statusColor = AmberWarning,
                                        onClick = { selectedEventId = event.id },
                                        modifier = Modifier.pqStaggeredItem(visible, 7 + idx)
                                    )
                                }
                            }
                        }

                        // ── Rotations & Regular Events ──
                        if (sections.rotations.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = sectionTitle("rotations", lang),
                                    modifier = Modifier.pqStaggeredItem(visible, 8)
                                )
                            }
                            sections.rotations.forEachIndexed { idx, event ->
                                item(key = "rot-${event.id}") {
                                    CompactEventCard(
                                        event = event,
                                        lang = lang,
                                        statusLabel = if (lang == "tr") "Rotasyon" else "Rotation",
                                        statusColor = PurpleIV,
                                        onClick = { selectedEventId = event.id },
                                        modifier = Modifier.pqStaggeredItem(visible, 9 + idx)
                                    )
                                }
                            }
                        }

                        // ── News & Announcements ──
                        if (sections.news.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = sectionTitle("news", lang),
                                    modifier = Modifier.pqStaggeredItem(visible, 10)
                                )
                            }
                            sections.news.forEachIndexed { idx, event ->
                                item(key = "news-${event.id}") {
                                    CompactEventCard(
                                        event = event,
                                        lang = lang,
                                        statusLabel = if (lang == "tr") "Duyuru" else "News",
                                        statusColor = TextTertiary,
                                        onClick = { selectedEventId = event.id },
                                        modifier = Modifier.pqStaggeredItem(visible, 11 + idx)
                                    )
                                }
                            }
                        }

                        // ── All Events ──
                        if (sections.allActive.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = sectionTitle("allActive", lang),
                                    modifier = Modifier.pqStaggeredItem(visible, 14)
                                )
                            }
                            sections.allActive.forEachIndexed { idx, event ->
                                item(key = "all-${event.id}") {
                                    CompactEventCard(
                                        event = event,
                                        lang = lang,
                                        statusLabel = when (event.determineCategory()) {
                                            EventCategory.MAJOR_GAMEPLAY -> if (lang == "tr") "Büyük" else "Major"
                                            EventCategory.LIMITED_GAMEPLAY -> if (lang == "tr") "Sınırlı" else "Limited"
                                            EventCategory.ROUTINE_ROTATION, EventCategory.RAID_ROTATION, EventCategory.SEASON_GBL -> if (lang == "tr") "Rotasyon" else "Rotation"
                                            else -> if (lang == "tr") "Haber" else "News"
                                        },
                                        statusColor = when (event.determineCategory()) {
                                            EventCategory.MAJOR_GAMEPLAY -> CyanGlow
                                            EventCategory.LIMITED_GAMEPLAY -> AmberWarning
                                            EventCategory.ROUTINE_ROTATION, EventCategory.RAID_ROTATION, EventCategory.SEASON_GBL -> PurpleIV
                                            else -> TextTertiary
                                        },
                                        onClick = { selectedEventId = event.id },
                                        modifier = Modifier.pqStaggeredItem(visible, 15 + idx)
                                    )
                                }
                            }
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

                // Selected event detail card (when user taps a compact card)
                if (selectedEventId != null && visibleEvents.isNotEmpty()) {
                    val sections = groupEvents(visibleEvents)
                    if (selectedEventId != sections.featured?.id) {
                        val detailEvent = visibleEvents.firstOrNull { it.id == selectedEventId }
                        if (detailEvent != null) {
                            item {
                                SectionHeader(
                                    title = detailEvent.localizedTitle(lang),
                                    modifier = Modifier.pqStaggeredItem(visible, 12)
                                )
                            }
                            item {
                                EventMainCard(
                                    event = detailEvent,
                                    sourceLabelRes = sourceLabelRes,
                                    lastChecked = lastChecked,
                                    lang = lang,
                                    modifier = Modifier.pqStaggeredItem(visible, 13)
                                )
                            }
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
    Button(
        onClick = onRefresh,
        enabled = !refreshing,
        modifier = modifier
            .height(38.dp),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TealPrimary.copy(alpha = 0.16f),
            contentColor = TealPrimary,
            disabledContainerColor = TealPrimary.copy(alpha = 0.10f),
            disabledContentColor = TealPrimary.copy(alpha = 0.70f)
        )
    ) {
        if (refreshing) {
            CircularProgressIndicator(
                color = TealPrimary,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.events_refresh), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.events_refresh),
                tint = TealPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.events_refresh), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    lastChecked: String?,
    lang: String,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val tone = themeTone(event.themeKey)
    var dialog by remember { mutableStateOf<EventDialogContent?>(null) }
    EventDashboardContent(
        event = event,
        sourceLabelRes = sourceLabelRes,
        lastChecked = lastChecked,
        tone = tone,
        clipboard = clipboard,
        lang = lang,
        onOpen = { dialog = it },
        modifier = modifier
    )
    dialog?.let { content ->
        EventInfoDialog(content = content, event = event, lang = lang, onDismiss = { dialog = null })
    }
}

@Composable
private fun EventPickerPanel(
    events: List<EventContext>,
    selectedId: String,
    lang: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        events.take(3).forEach { event ->
            val selected = event.id == selectedId
            val tone = if (selected) TealPrimary else TextTertiary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) TealPrimary.copy(alpha = 0.16f) else CardPremium.copy(alpha = 0.70f))
                    .border(1.5.dp, tone.copy(alpha = if (selected) 0.65f else 0.15f), RoundedCornerShape(10.dp))
                    .clickable { onSelected(event.id) }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier
                        .size(5.dp)
                        .background(tone, CircleShape)
                )
                Spacer(Modifier.height(4.dp))

                val displayTitle = when {
                    event.id.contains("anniversary") -> {
                        if (lang == "tr") "10. Yıl Dönümü" else "10th Anniv."
                    }
                    event.id.contains("legends") -> {
                        if (lang == "tr") "Efsaneler Yolu" else "Road of Legends"
                    }
                    event.id.contains("fest") -> {
                        if (lang == "tr") "GO Fest 2026" else "GO Fest 2026"
                    }
                    else -> event.localizedTitle(lang)
                }

                Text(
                    text = displayTitle,
                    color = if (selected) TextPrimary else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = event.remainingTimeLabel(lang = lang),
                    color = tone.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Section title localization helper — no XML resources needed. */
private fun sectionTitle(section: String, lang: String): String = when (lang) {
    "tr" -> when (section) {
        "featured" -> "Öne çıkan"
        "upcoming" -> "Yakında önemli"
        "live" -> "Şu an olanlar"
        "rotations" -> "Rotasyonlar ve ligler"
        "news" -> "Duyurular ve ödüller"
        "allActive" -> "Tüm etkinlikler"
        else -> ""
    }
    "de" -> when (section) {
        "featured" -> "Hervorgehoben"
        "upcoming" -> "Wichtig & Bevorstehend"
        "live" -> "Jetzt aktiv"
        "rotations" -> "Rotationen & Ligen"
        "news" -> "Neuigkeiten & Belohnungen"
        "allActive" -> "Alle Events"
        else -> ""
    }
    "es" -> when (section) {
        "featured" -> "Destacado"
        "upcoming" -> "Importante Próximo"
        "live" -> "Activo ahora"
        "rotations" -> "Rotaciones y Ligas"
        "news" -> "Noticias y Recompensas"
        "allActive" -> "Todos los eventos"
        else -> ""
    }
    "fr" -> when (section) {
        "featured" -> "Vedette"
        "upcoming" -> "Important à venir"
        "live" -> "Actif maintenant"
        "rotations" -> "Rotations & Ligues"
        "news" -> "Nouvelles & Récompenses"
        "allActive" -> "Tous les événements"
        else -> ""
    }
    "it" -> when (section) {
        "featured" -> "In evidenza"
        "upcoming" -> "Importante in arrivo"
        "live" -> "Attivo ora"
        "rotations" -> "Rotazioni e Leghe"
        "news" -> "Notizie e Premi"
        "allActive" -> "Tutti gli eventi"
        else -> ""
    }
    else -> when (section) {
        "featured" -> "Featured"
        "upcoming" -> "Important upcoming"
        "live" -> "Happening now"
        "rotations" -> "Rotations and leagues"
        "news" -> "News and rewards"
        "allActive" -> "All active events"
        else -> ""
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = modifier.padding(top = 14.dp, bottom = 4.dp)
    )
}

@Composable
private fun CompactEventCard(
    event: EventContext,
    lang: String,
    statusLabel: String,
    statusColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardPremium.copy(alpha = 0.70f))
            .border(1.dp, statusColor.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status dot
        Box(
            Modifier
                .size(8.dp)
                .background(statusColor, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.localizedTitle(lang),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.14f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(6.dp))
                // Date range
                val dateText = buildString {
                    event.startDate?.let { append(it) }
                    event.endDate?.let { if (it != event.startDate) append(" – $it") }
                }
                if (dateText.isNotBlank()) {
                    Text(
                        text = dateText,
                        color = TextTertiary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        // Time remaining label
        Text(
            text = event.remainingTimeLabel(lang = lang),
            color = statusColor.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun EventDashboardContent(
    event: EventContext,
    sourceLabelRes: Int,
    lastChecked: String?,
    tone: Color,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    lang: String,
    onOpen: (EventDialogContent) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val effectiveStatus = event.effectiveStatus()
    val timerLabel = event.remainingTimeLabel(lang = lang)
    val badgeLabel = timerLabel
    val search = event.suggestedSearch.orEmpty()
    val featuredAction = stringResource(R.string.event_group_featured_action)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(tone.copy(alpha = 0.16f), CardDark)))
            .border(1.dp, tone.copy(alpha = 0.34f), shape)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(tone.copy(alpha = 0.18f)).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(7.dp).background(tone, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(badgeLabel, color = tone, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
            EventThemeMark(event.themeKey, tone, Modifier.size(40.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(event.localizedTitle(lang), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 23.sp)
        event.dateLabel(lang)?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = TextTertiary, fontSize = 12.sp)
        }

        if (event.pokemon.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            EventSpriteRow(
                entries = event.pokemon,
                tone = tone,
                lang = lang,
                onOpen = { entry ->
                    onOpen(
                        EventDialogContent(
                            title = entry.localizedName(lang),
                            badge = entry.localizedBadges(lang),
                            body = entry.localizedNote(lang).ifBlank { entry.localizedSource(lang) },
                            action = featuredAction,
                            spriteKey = entry.spriteKey,
                            cardKey = entry.spriteKey
                        )
                    )
                }
            )
        }

        EventGroupCard(
            title = stringResource(R.string.event_featured_pokemon),
            badge = stringResource(R.string.event_group_featured_badge),
            body = event.localizedFeatured(lang).ifBlank { event.pokemon.joinToString { it.name } },
            action = stringResource(R.string.event_group_featured_action),
            tone = tone,
            spriteKey = event.pokemon.firstOrNull { it.spriteKey != null }?.spriteKey,
            onOpen = onOpen,
            cardKey = null
        )
        val raidsText = event.localizedRaids(lang)
        if (raidsText.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            EventGroupCard(
                title = stringResource(R.string.event_feature_raids),
                badge = stringResource(R.string.event_group_raids_badge),
                body = raidsText,
                action = stringResource(R.string.event_feature_raids_action),
                tone = CyanGlow,
                spriteKey = event.pokemon.firstOrNull { it.spriteKey == "mewtwo" || it.spriteKey == "necrozma" }?.spriteKey,
                onOpen = onOpen,
                cardKey = "raid_targets"
            )
        }
        Spacer(Modifier.height(10.dp))
        EventGroupCard(
            title = stringResource(R.string.event_research),
            badge = stringResource(R.string.event_group_research_badge),
            body = event.localizedResearch(lang),
            action = stringResource(R.string.event_group_raids_action),
            tone = PurpleIV,
            spriteKey = event.pokemon.getOrNull(1)?.spriteKey,
            onOpen = onOpen,
            cardKey = null
        )
        Spacer(Modifier.height(10.dp))
        EventGroupCard(
            title = stringResource(R.string.event_group_collection_title),
            badge = stringResource(R.string.event_group_collection_badge),
            body = event.localizedNotes(lang).ifBlank { stringResource(R.string.event_group_collection_body) },
            action = stringResource(R.string.event_group_collection_action),
            tone = GoldCaution,
            spriteKey = event.pokemon.firstOrNull { it.spriteKey == "pikachu" || it.spriteKey == "eevee" }?.spriteKey,
            onOpen = onOpen,
            cardKey = null
        )
        Spacer(Modifier.height(10.dp))
        val hasFusion = event.id.contains("go-fest") || event.id.contains("legends") ||
                event.localizedBonuses(lang).contains("energy", ignoreCase = true) ||
                event.localizedNotes(lang).contains("energy", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("enerji", ignoreCase = true) ||
                event.localizedNotes(lang).contains("enerji", ignoreCase = true)
        EventGroupCard(
            title = stringResource(R.string.event_bonuses),
            badge = stringResource(R.string.event_group_bonuses_badge),
            body = event.localizedBonuses(lang),
            action = stringResource(R.string.event_group_bonuses_action),
            tone = AmberWarning,
            spriteKey = if (hasFusion) "link_energy" else null,
            onOpen = onOpen,
            cardKey = if (hasFusion) "link_energy" else "bonuses"
        )
        Spacer(Modifier.height(10.dp))
        EventGroupCard(
            title = stringResource(R.string.event_group_prep_title),
            badge = stringResource(R.string.event_group_prep_badge),
            body = event.localizedPrep(lang),
            action = stringResource(R.string.event_group_prep_action),
            tone = TealPrimary,
            spriteKey = "prep_list",
            onOpen = onOpen,
            cardKey = "prep_list"
        )

        if (search.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            SectionLabel(stringResource(R.string.event_suggested_for_event), tone)
            Spacer(Modifier.height(5.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SlateBlack.copy(alpha = 0.6f))
                    .border(1.dp, tone.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = search,
                    color = TextPrimary,
                    fontSize = 12.sp,
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
                    Text(stringResource(R.string.event_card_copy_search), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(sourceLabelRes),
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(50)).background(CardPremium.copy(alpha = 0.7f)).padding(horizontal = 8.dp, vertical = 3.dp)
        )
        lastChecked?.let {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.event_context_last_checked, it), color = TextTertiary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun EventSpriteRow(
    entries: List<EventPokemonEntry>,
    tone: Color,
    lang: String,
    onOpen: (EventPokemonEntry) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.take(6).chunked(2).forEach { rowEntries ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowEntries.forEach { entry ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardPremium.copy(alpha = 0.9f))
                            .border(1.dp, tone.copy(alpha = 0.26f), RoundedCornerShape(16.dp))
                            .clickable { onOpen(entry) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EventSprite(entry.spriteKey, tone, Modifier.size(54.dp))
                        Spacer(Modifier.height(5.dp))
                        Text(entry.localizedName(lang), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(entry.localizedBadges(lang), color = tone, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (rowEntries.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EventGroupCard(
    title: String,
    badge: String,
    body: String,
    action: String,
    tone: Color,
    spriteKey: String?,
    onOpen: (EventDialogContent) -> Unit,
    cardKey: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardPremium.copy(alpha = 0.86f))
            .border(1.dp, tone.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
            .clickable { onOpen(EventDialogContent(title, badge, body, action, spriteKey, cardKey)) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EventSprite(spriteKey, tone, Modifier.size(52.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    badge,
                    color = tone,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(tone.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(body, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EventSprite(spriteKey: String?, tone: Color, modifier: Modifier = Modifier) {
    val res = spriteKey?.let(::spriteRes)
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(tone.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        if (res != null) {
            Image(painter = painterResource(res), contentDescription = null, modifier = Modifier.size(42.dp))
        } else {
            when (spriteKey) {
                "link_energy", "link_charges" -> LinkEnergyIcon(tone, Modifier.size(42.dp))
                "incubators" -> IncubatorsIcon(tone, Modifier.size(42.dp))
                "prep_list" -> PrepListIcon(tone, Modifier.size(42.dp))
                else -> EventThemeMark("generic_event", tone, Modifier.size(42.dp))
            }
        }
    }
}

@Composable
private fun LinkEnergyIcon(tone: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(tone, radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.5f))
        val stroke = Stroke(width = w * 0.08f)
        drawCircle(tone.copy(alpha = 0.4f), radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
        drawLine(tone, Offset(w * 0.15f, h * 0.35f), Offset(w * 0.85f, h * 0.65f), strokeWidth = w * 0.07f)
        drawLine(tone, Offset(w * 0.15f, h * 0.65f), Offset(w * 0.85f, h * 0.35f), strokeWidth = w * 0.07f)
    }
}

@Composable
private fun IncubatorsIcon(tone: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.07f)
        drawRoundRect(
            color = tone,
            topLeft = Offset(w * 0.25f, h * 0.2f),
            size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.25f, w * 0.25f),
            style = stroke
        )
        drawCircle(tone.copy(alpha = 0.5f), radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.52f))
    }
}

@Composable
private fun PrepListIcon(tone: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.07f)
        drawRoundRect(
            color = tone.copy(alpha = 0.5f),
            topLeft = Offset(w * 0.24f, h * 0.24f),
            size = androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.56f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f, w * 0.08f),
            style = stroke
        )
        drawRoundRect(
            color = tone,
            topLeft = Offset(w * 0.4f, h * 0.16f),
            size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, w * 0.04f)
        )
        drawLine(tone, Offset(w * 0.36f, h * 0.42f), Offset(w * 0.64f, h * 0.42f), strokeWidth = w * 0.06f)
        drawLine(tone, Offset(w * 0.36f, h * 0.56f), Offset(w * 0.64f, h * 0.56f), strokeWidth = w * 0.06f)
        drawLine(tone, Offset(w * 0.36f, h * 0.70f), Offset(w * 0.64f, h * 0.70f), strokeWidth = w * 0.06f)
    }
}

private data class EventDialogContent(
    val title: String,
    val badge: String,
    val body: String,
    val action: String,
    val spriteKey: String?,
    val cardKey: String? = null
)

private fun spriteRes(key: String): Int? = when (key) {
    "unown" -> R.drawable.event_unown
    "kangaskhan" -> R.drawable.event_kangaskhan
    "mr_mime" -> R.drawable.event_mr_mime
    "heracross" -> R.drawable.event_heracross
    "corsola" -> R.drawable.event_corsola
    "gimmighoul" -> R.drawable.event_gimmighoul
    "pikachu" -> R.drawable.event_pikachu
    "necrozma" -> R.drawable.event_necrozma
    "eevee" -> R.drawable.event_eevee
    "zeraora" -> R.drawable.event_zeraora
    "wurmple" -> R.drawable.event_wurmple
    "mewtwo" -> R.drawable.event_mewtwo
    else -> null
}

@Composable
private fun EventInfoDialog(
    content: EventDialogContent,
    event: EventContext,
    lang: String,
    onDismiss: () -> Unit
) {
    val closeLabel = stringResource(R.string.event_close)
    val prepItems = listOf(
        stringResource(R.string.event_detail_prep_item_storage),
        stringResource(R.string.event_detail_prep_item_trades),
        stringResource(R.string.event_detail_prep_item_passes),
        stringResource(R.string.event_detail_prep_item_megas),
        stringResource(R.string.event_detail_prep_item_copy),
        stringResource(R.string.event_detail_prep_item_keep)
    )
    val localCtx = androidx.compose.ui.platform.LocalContext.current
    val localConfig = androidx.compose.ui.platform.LocalConfiguration.current
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalContext provides localCtx,
        androidx.compose.ui.platform.LocalConfiguration provides localConfig
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = SlateBlack)) {
                    Text(closeLabel, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EventSprite(content.spriteKey, TealPrimary, Modifier.size(64.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(content.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(content.badge, color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column {
                    val detailBody = cardKeyBody(content.cardKey, event, lang, localCtx)
                    val detailAction = cardKeyAction(content.cardKey, event, lang, localCtx)
                    Text(
                        text = detailBody ?: content.body,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    if (content.cardKey == "prep_list") {
                        Spacer(Modifier.height(10.dp))
                        PrepChecklist(prepItems)
                    }
                    Spacer(Modifier.height(10.dp))
                    SectionLabel(whatToDoLabel(lang), AmberWarning)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = detailAction ?: content.action,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        )
    }
}

@Composable
private fun PrepChecklist(items: List<String>) {
    val checked = remember { mutableStateListOf(*Array(items.size) { false }) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEachIndexed { index, label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (checked[index]) TealPrimary.copy(alpha = 0.08f) else CardPremium.copy(alpha = 0.5f))
                    .clickable {
                        checked[index] = !checked[index]
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (checked[index]) TealPrimary else TextTertiary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (checked[index]) {
                        Text("✓", color = SlateBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = label,
                    color = if (checked[index]) TextTertiary else TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textDecoration = if (checked[index]) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
            }
        }
    }
}

private fun cardKeyBody(cardKey: String?, event: EventContext, lang: String, context: android.content.Context): String? = when (cardKey) {
    "raid_targets" -> {
        val specific = event.localizedRaids(lang)
        val bodyPrefix = if (specific.isNotBlank()) {
            if (lang == "tr") "Akın Detayları:\n$specific\n\n" else "Raid Details:\n$specific\n\n"
        } else ""
        val isGoFest = event.id.contains("go-fest") || event.id.contains("legends")
        val resId = if (isGoFest) R.string.event_detail_raid_targets_body_gofest else R.string.event_detail_raid_targets_body
        bodyPrefix + context.getString(resId)
    }
    "link_energy", "fusion_energy" -> {
        val isGoFest = event.id.contains("go-fest") || event.id.contains("legends") ||
                event.localizedBonuses(lang).contains("energy", ignoreCase = true) ||
                event.localizedNotes(lang).contains("energy", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("enerji", ignoreCase = true) ||
                event.localizedNotes(lang).contains("enerji", ignoreCase = true)
        if (isGoFest) {
            context.getString(R.string.event_detail_link_energy_body)
        } else {
            null
        }
    }
    "link_charges" -> {
        val isGoFest = event.id.contains("go-fest") || event.id.contains("legends") ||
                event.localizedBonuses(lang).contains("charge", ignoreCase = true) ||
                event.localizedNotes(lang).contains("charge", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("şarj", ignoreCase = true) ||
                event.localizedNotes(lang).contains("şarj", ignoreCase = true)
        if (isGoFest) {
            context.getString(R.string.event_detail_link_charges_body)
        } else {
            null
        }
    }
    "incubators" -> {
        val isGoFest = event.id.contains("go-fest") || event.id.contains("legends") ||
                event.localizedBonuses(lang).contains("distance", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("mesafe", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("egg", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("yumurta", ignoreCase = true)
        if (isGoFest) {
            context.getString(R.string.event_detail_incubators_body)
        } else {
            null
        }
    }
    "prep_list" -> context.getString(R.string.event_detail_prep_list_body)
    else -> null
}

private fun cardKeyAction(cardKey: String?, event: EventContext, lang: String, context: android.content.Context): String? = when (cardKey) {
    "raid_targets" -> context.getString(R.string.event_detail_raid_targets_action)
    "link_energy", "fusion_energy" -> {
        val isGoFest = event.id.contains("go-fest") || event.id.contains("legends")
        if (isGoFest) {
            context.getString(R.string.event_detail_link_energy_action)
        } else {
            null
        }
    }
    "link_charges" -> {
        val isGoFest = event.id.contains("go-fest") || event.id.contains("legends")
        if (isGoFest) {
            context.getString(R.string.event_detail_link_charges_action)
        } else {
            null
        }
    }
    "incubators" -> {
        val isGoFest = event.id.contains("go-fest") || event.id.contains("legends") ||
                event.localizedBonuses(lang).contains("distance", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("mesafe", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("egg", ignoreCase = true) ||
                event.localizedBonuses(lang).contains("yumurta", ignoreCase = true)
        if (isGoFest) {
            context.getString(R.string.event_detail_incubators_action)
        } else {
            null
        }
    }
    "prep_list" -> context.getString(R.string.event_detail_prep_list_action)
    else -> null
}

private fun whatToDoLabel(lang: String): String = when (lang) {
    "tr" -> "Ne yapmalı?"
    "de" -> "Was tun?"
    "es" -> "¿Qué hacer?"
    "fr" -> "Que faire ?"
    "it" -> "Cosa fare?"
    else -> "What to do?"
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
