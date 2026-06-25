package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.ScreenTitleBar
import com.caglar.pokequery.ui.components.RiskBadge
import androidx.compose.ui.graphics.Color
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class Preset(
    val title: String,
    val category: String,
    val description: String,
    val syntax: String,
    val risk: RiskLevel,
    val warnings: List<String> = emptyList()
)

val POPULAR_PRESETS = listOf(
    Preset("Recent Catches", "Cleanup", "Find Pokémon caught in the last 7 days", "age0-7", RiskLevel.Low),
    Preset("Low IV Cleanup Candidate", "Cleanup", "Find 0★ and 1★ candidates (excludes protected categories)", "0*,1*&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground", RiskLevel.Medium, listOf("Review manually before transfer.")),
    Preset("Duplicate Cleanup", "Cleanup", "Find species you have more than 2 of (excludes protected categories)", "count2-&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground", RiskLevel.Medium, listOf("Count is species-wide. Check for forms.")),
    Preset("Untagged Review", "Cleanup", "Find untagged Pokémon (excludes protected categories)", "!#&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!traded&!costume&!background&!locationbackground&!specialbackground", RiskLevel.Low),
    
    Preset("Evolve Ready", "Candy/Event", "Find Pokémon that can be evolved right now", "evolve", RiskLevel.Low),
    Preset("Recent Event Review", "Candy/Event", "Find Pokémon caught in the last 3 days", "age0-3", RiskLevel.Low),
    
    Preset("Untraded Duplicates", "Trading", "Find untraded duplicates", "count2-&!traded", RiskLevel.Medium),
    Preset("Older Untraded", "Trading", "Find untraded Pokémon over a year old", "age365-&!traded", RiskLevel.Medium),
    Preset("Distance Trade Candidates", "Trading", "Find untraded Pokémon caught >100km away", "distance100-&!traded", RiskLevel.Medium),
    Preset("Special Trade Review", "Trading", "Find shiny, legendary, or mythical Pokémon", "shiny,legendary,mythical", RiskLevel.Info, listOf("Special trades are limited to 1 per day.")),
    
    Preset("Hundo", "Battle/IV", "Find perfect 4★ Pokémon", "4*", RiskLevel.Info),
    Preset("Nundo", "Battle/IV", "Find exact 0% IV Pokémon", "0attack&0defense&0hp", RiskLevel.Info),
    Preset("Great League Candidate", "Battle/IV", "Find low attack / high bulk candidates under 1500 CP", "0-1attack&3-4defense&3-4hp&cp-1500", RiskLevel.Info),
    Preset("Ultra League Candidate", "Battle/IV", "Find low attack / high bulk candidates under 2500 CP", "0-1attack&3-4defense&3-4hp&cp-2500", RiskLevel.Info),
    Preset("Perfect Shadows", "Battle/IV", "Find 4★ shadows", "shadow&4*", RiskLevel.Info),
    
    Preset("Shiny Review", "Collection", "Find all shiny Pokémon", "shiny", RiskLevel.Info),
    Preset("Costume Review", "Collection", "Find all costume Pokémon", "costume", RiskLevel.Info),
    Preset("Lucky Review", "Collection", "Find all lucky Pokémon", "lucky", RiskLevel.Info)
)

@Composable
fun PresetsScreen(
    onBack: () -> Unit,
    onCopy: (GeneratedString) -> Unit,
    onNavigateRisk: (GeneratedString) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = androidx.compose.runtime.remember { com.caglar.pokequery.data.repository.UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val language = userPrefs?.gameLanguage ?: "English"

    // v0.5.2 (Fix 5): which preset is expanded for preview/customize/copy. Only one open at
    // a time keeps the screen compact (less scrolling) while still showing the full string,
    // warnings, and copy flow when a card is tapped.
    var expandedTitle by remember { mutableStateOf<String?>(null) }

    val grouped = POPULAR_PRESETS.groupBy { it.category }

    // v0.5.3 motion polish: staggered entrance — title bar fades in first; preset cards appear
    // at rest (no cascade while scrolling). One hoisted flag → runs once only.
    // v0.5.5 (Fix 1): the gap between preset cards follows the Visual Density `listGap`
    // token, so Compact fits more presets per screen.
    val density = com.caglar.pokequery.theme.density.currentDensity()
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(density.listGap),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenTitleBar("Popular Presets", onBack, Modifier.pqStaggeredItem(visible, 0).padding(bottom = 4.dp))
            Text(
                "Tap a preset to preview, customize and copy. Risk badges show how careful to be.",
                color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp
            )
        }
        grouped.forEach { (category, presets) ->
            item {
                com.caglar.pokequery.ui.pq.PqSectionHeader(
                    category.uppercase(),
                    Modifier.padding(top = 8.dp)
                )
            }
            items(presets, key = { it.title }) { preset ->
                CompactPresetCard(
                    preset = preset,
                    isExpanded = expandedTitle == preset.title,
                    language = language,
                    onToggle = { expandedTitle = if (expandedTitle == preset.title) null else preset.title },
                    onCopy = onCopy,
                    onNavigateRisk = onNavigateRisk
                )
            }
        }
    }
    }
}

/**
 * v0.5.2 (Fix 5): compact preset row. Collapsed = one-line title + risk badge + short
 * purpose (fits several per screen, far less scrolling). Expanded = the preview string box,
 * warnings, manual-review reminder, and Copy — the full preview/customize/copy flow, using
 * the EXACT same StringBuilderEngine.buildString path and risk routing as before.
 */
@Composable
private fun CompactPresetCard(
    preset: Preset,
    isExpanded: Boolean,
    language: String,
    onToggle: () -> Unit,
    onCopy: (GeneratedString) -> Unit,
    onNavigateRisk: (GeneratedString) -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    // v0.5.5 (Fix 1): preset card padding and the expanded inner element gaps (string box →
    // copy button) follow the Visual Density tokens so Compact tightens the preset list.
    val density = com.caglar.pokequery.theme.density.currentDensity()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardDark)
            .border(1.dp, BorderSubtle, shape)
            .clickable(onClick = onToggle)
            .padding(horizontal = density.cardPadding, vertical = (density.innerElementGap.value * 1.1f).dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(preset.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(preset.description, color = TextSecondary, fontSize = 11.sp, maxLines = if (isExpanded) 3 else 1)
            }
            Spacer(Modifier.width(8.dp))
            com.caglar.pokequery.ui.pq.PqRiskBadge(preset.risk)
        }

        androidx.compose.animation.AnimatedVisibility(isExpanded) {
            Column(Modifier.padding(top = density.innerElementGap)) {
                com.caglar.pokequery.ui.pq.PqStringBox(preset.syntax)
                preset.warnings.forEach { warning ->
                    Text("• $warning", color = AmberWarning, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
                if (preset.risk == RiskLevel.Medium || preset.risk == RiskLevel.High) {
                    Spacer(Modifier.height(6.dp))
                    com.caglar.pokequery.ui.pq.PqManualReviewPanel(text = "Review matches in Pokémon GO before transferring or trading.")
                }
                Spacer(Modifier.height(density.innerElementGap))
                com.caglar.pokequery.ui.pq.PqPrimaryButton(
                    text = "Preview & Copy",
                    leadingIcon = Icons.Default.ContentCopy,
                    onClick = {
                        val generated = StringBuilderEngine.buildString(
                            baseQuery = preset.syntax,
                            protections = emptyList(), // Built-in; preset safety contract tested.
                            explanation = preset.description,
                            riskLevel = preset.risk,
                            goalId = "preset",
                            title = preset.title,
                            language = language
                        )
                        if (preset.risk == RiskLevel.High || preset.risk == RiskLevel.Medium) {
                            onNavigateRisk(generated)
                        } else {
                            onCopy(generated)
                        }
                    }
                )
            }
        }
    }
}
