package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.domain.events.EventContextRepository
import com.caglar.pokequery.domain.events.MonthlyContextRepository
import com.caglar.pokequery.domain.events.MonthlyContextView
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.BorderSubtle
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.TextTertiary
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.components.ScreenTitleBar
import com.caglar.pokequery.ui.motion.PqStaggeredEntrance
import com.caglar.pokequery.ui.motion.pqStaggeredItem

/**
 * v0.6.1 — Offline/manual Event Context.
 *
 * A LOCAL, manually-maintained set of event context notes bundled with the app. There is NO
 * network: no INTERNET permission, no fetch, no remote provider. The notes can go stale, so every
 * section discloses "manually maintained and may be outdated" and "No live event data is fetched."
 *
 * There is no live calendar integration and no event logos/assets — text only.
 */
@Composable
fun EventContextScreen(onBack: () -> Unit) {
    val monthly = MonthlyContextRepository.currentWithStaleness()
    val events = EventContextRepository.all()
    val density = currentDensity()

    PqStaggeredEntrance { visible ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(density.listGap),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item {
                ScreenTitleBar("Event Context", onBack, Modifier.pqStaggeredItem(visible, 0).padding(bottom = 4.dp))
            }
            item { OfflineBanner(Modifier.pqStaggeredItem(visible, 1)) }

            monthly?.let { view ->
                item {
                    MonthlyNoteCard(view, Modifier.pqStaggeredItem(visible, 2))
                }
            } ?: item {
                val shape = RoundedCornerShape(14.dp)
                Column(Modifier.fillMaxWidth().clip(shape).background(CardDark).border(1.dp, BorderSubtle, shape).padding(12.dp)) {
                    Text("This month's Community Day", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(MonthlyContextRepository.noNoteMessage(), color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }

            item {
                Text("General event notes", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            items(events, key = { it.id }) { event ->
                val shape = RoundedCornerShape(14.dp)
                Column(Modifier.fillMaxWidth().clip(shape).background(CardDark).border(1.dp, BorderSubtle, shape).padding(12.dp)) {
                    Text(event.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(event.note, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }

            item {
                Text(
                    EventContextRepository.disclaimer(),
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
            "Offline and manual. PokeQuery does not fetch live event data. Notes are maintained in app " +
                "releases and may be outdated — always confirm any active event in Pokémon GO itself.",
            color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp
        )
    }
}

@Composable
private fun MonthlyNoteCard(view: MonthlyContextView, modifier: Modifier = Modifier) {
    val note = view.note
    val shape = RoundedCornerShape(14.dp)
    Column(modifier.fillMaxWidth().clip(shape).background(CardDark).border(1.dp, BorderSubtle, shape).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("This month's Community Day", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            val tone = if (view.isStale) AmberWarning else CyanGlow
            val label = if (view.isStale) "May be outdated" else "Manual note"
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(tone.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)
            ) { Text(label, color = tone, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(6.dp))
        Text("${note.month}/${note.year} · updated in app v${note.lastUpdatedInAppVersion}", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        Text(note.note, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(8.dp))
        Text(view.disclaimer, color = AmberWarning, fontSize = 11.sp, lineHeight = 15.sp)
    }
}
