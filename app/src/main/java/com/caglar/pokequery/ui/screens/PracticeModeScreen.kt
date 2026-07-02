package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.caglar.pokequery.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.domain.practice.PracticeMatcher
import com.caglar.pokequery.domain.practice.PracticeStatus
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.BorderDark
import com.caglar.pokequery.theme.BorderSubtle
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.GreenVerified
import com.caglar.pokequery.theme.SlateBlack
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.TextTertiary
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.clearFocusOnTap
import com.caglar.pokequery.ui.components.ScreenTitleBar
import com.caglar.pokequery.ui.motion.PqStaggeredEntrance
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.pq.PqStringBox

/**
 * v0.6.1 — Practice Mode (fake inventory sandbox).
 *
 * An entirely CONCEPTUAL teaching tool: the inventory shown here is synthetic and fictional. It is
 * NOT connected to Pokémon GO — no API, no screenshots, no OCR, no account access. Real Pokémon GO
 * results will differ. The disclaimer is always shown so the screen can never be mistaken for a
 * real inventory reader.
 *
 * The user types (or pastes) a search string and the conceptual matcher classifies each fake item
 * as MATCHED / PROTECTED / NOT_MATCHED, explaining why. This teaches how `&`, `,` and `!token`
 * exclusions behave — without touching any real data.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun PracticeModeScreen(onBack: () -> Unit) {
    // A safe, well-formed default that demonstrates an exclusion (so the first frame is meaningful
    // even before the user types anything).
    var query by rememberSaveable { mutableStateOf("0*,1*&!shiny&!legendary&!favorite") }
    val results = remember(query) { PracticeMatcher.match(query) }
    val density = currentDensity()

    PqStaggeredEntrance { visible ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundDark).clearFocusOnTap().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(density.listGap),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item {
                ScreenTitleBar(stringResource(R.string.goal_practice), onBack, Modifier.pqStaggeredItem(visible, 0).padding(bottom = 4.dp))
            }
            item {
                ConceptualBanner(Modifier.pqStaggeredItem(visible, 1))
            }
            item {
                Text(stringResource(R.string.practice_intro), color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    placeholder = { Text(stringResource(R.string.practice_placeholder), color = TextTertiary, fontFamily = FontFamily.Monospace) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TealPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = BorderDark,
                        cursorColor = TealPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
            item {
                PqStringBox(if (query.isBlank()) stringResource(R.string.practice_empty) else query)
            }
            item {
                ResultSummary(results, Modifier.fillMaxWidth())
            }
            items(results, key = { it.item.id }) { result ->
                PracticeItemRow(result)
            }
        }
    }
}

@Composable
private fun ConceptualBanner(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(CyanGlow.copy(alpha = 0.08f))
            .border(1.dp, CyanGlow.copy(alpha = 0.35f), shape).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(6.dp).background(CyanGlow, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(stringResource(R.string.practice_banner_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.practice_banner_body),
                color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ResultSummary(results: List<com.caglar.pokequery.domain.practice.PracticeMatchResult>, modifier: Modifier) {
    val matched = results.count { it.status == PracticeStatus.MATCHED }
    val protected = results.count { it.status == PracticeStatus.PROTECTED }
    val notMatched = results.count { it.status == PracticeStatus.NOT_MATCHED }
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier.clip(shape).background(SlateBlack).border(1.dp, BorderSubtle, shape).padding(12.dp)
    ) {
        Text(stringResource(R.string.practice_summary_title), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryChip(stringResource(R.string.practice_matched, matched), GreenVerified)
            Spacer(Modifier.width(8.dp))
            SummaryChip(stringResource(R.string.practice_protected, protected), AmberWarning)
            Spacer(Modifier.width(8.dp))
            SummaryChip(stringResource(R.string.practice_not_matched, notMatched), TextTertiary)
        }
    }
}

@Composable
private fun SummaryChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun PracticeItemRow(result: com.caglar.pokequery.domain.practice.PracticeMatchResult) {
    val appLanguage = LocalConfiguration.current.locales[0]?.language ?: "en"
    val taggedLabel = stringResource(R.string.kb_cat_tag)
    val (tone, label) = when (result.status) {
        PracticeStatus.MATCHED -> GreenVerified to stringResource(R.string.practice_status_matched)
        PracticeStatus.PROTECTED -> AmberWarning to stringResource(R.string.practice_status_protected)
        PracticeStatus.EXCLUDED -> CoralDanger to stringResource(R.string.practice_status_excluded)
        PracticeStatus.NOT_MATCHED -> TextTertiary to stringResource(R.string.practice_status_not_matched)
    }
    val item = result.item
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(CardDark).border(1.dp, tone.copy(alpha = 0.3f), shape).padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.displayName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    buildString {
                        append("CP ${item.cp ?: "?"} · ${item.ivTag.name.lowercase().replace('_', ' ')}")
                        val flags = listOfNotNull(
                            if (item.shiny) "shiny" else null,
                            if (item.legendary) "legendary" else null,
                            if (item.mythical) "mythical" else null,
                            if (item.ultrabeast) "ultrabeast" else null,
                            if (item.costume) "costume" else null,
                            if (item.shadow) "shadow" else null,
                            if (item.purified) "purified" else null,
                            if (item.favorite) "favorite" else null,
                            if (item.traded) "traded" else null,
                            if (item.lucky) "lucky" else null,
                            if (item.background) "background" else null,
                            if (item.locationbackground) "locationbackground" else null,
                            if (item.specialbackground) "specialbackground" else null
                        )
                        if (flags.isNotEmpty()) append(" · ").append(flags.joinToString(" "))
                        if (item.tagged) append(" · ").append(taggedLabel).append(": ").append(item.tags.joinToString(",").ifEmpty { "#" })
                    },
                    color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(tone.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)
            ) { Text(label, color = tone, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
        if (result.reasons.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            result.reasons.forEach { reason ->
                val displayReason = if (appLanguage == "en") reason else label
                Text("• $displayReason", color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}
