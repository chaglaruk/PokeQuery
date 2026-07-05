package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.caglar.pokequery.R
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
    val warnings: List<String> = emptyList(),
    @StringRes val titleRes: Int? = null,
    @StringRes val descriptionRes: Int? = null
)

val POPULAR_PRESETS = listOf(
    Preset("Recent Catches", "Cleanup", "Find Pokémon caught in the last 7 days", "age0-7", RiskLevel.Low, titleRes = R.string.preset_recent_catches, descriptionRes = R.string.preset_desc_recent_catches),
    Preset("Low IV Cleanup Candidate", "Cleanup", "Find 0★ and 1★ candidates (excludes protected categories)", "0*,1*&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground", RiskLevel.Medium, listOf("Review manually before transfer."), R.string.preset_low_iv_cleanup, R.string.preset_desc_low_iv_cleanup),
    Preset("Duplicate Cleanup", "Cleanup", "Find species you have more than 2 of (excludes protected categories)", "count2-&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground", RiskLevel.Medium, listOf("Count is species-wide. Check for forms."), R.string.preset_duplicate_cleanup, R.string.preset_desc_duplicate_cleanup),
    Preset("Untagged Review", "Cleanup", "Find untagged Pokémon (excludes protected categories)", "!#&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!traded&!costume&!background&!locationbackground&!specialbackground", RiskLevel.Low, titleRes = R.string.preset_untagged_review, descriptionRes = R.string.preset_desc_untagged_review),

    Preset("Evolve Ready", "Candy/Event", "Find Pokémon that can be evolved right now", "evolve", RiskLevel.Low, titleRes = R.string.preset_evolve_ready, descriptionRes = R.string.preset_desc_evolve_ready),
    Preset("Recent Event Review", "Candy/Event", "Find Pokémon caught in the last 3 days", "age0-3", RiskLevel.Low, titleRes = R.string.preset_recent_event_review, descriptionRes = R.string.preset_desc_recent_event_review),

    Preset("Untraded Duplicates", "Trading", "Find untraded duplicates", "count2-&!traded", RiskLevel.Medium, titleRes = R.string.preset_untraded_duplicates, descriptionRes = R.string.preset_desc_untraded_duplicates),
    Preset("Older Untraded", "Trading", "Find untraded Pokémon over a year old", "age365-&!traded", RiskLevel.Medium, titleRes = R.string.preset_older_untraded, descriptionRes = R.string.preset_desc_older_untraded),
    Preset("Distance Trade Candidates", "Trading", "Find untraded Pokémon caught >100km away", "distance100-&!traded", RiskLevel.Medium, titleRes = R.string.preset_distance_trade, descriptionRes = R.string.preset_desc_distance_trade),
    Preset("Special Trade Review", "Trading", "Find shiny, legendary, or mythical Pokémon", "shiny,legendary,mythical", RiskLevel.Info, listOf("Special trades are limited to 1 per day."), R.string.preset_special_trade, R.string.preset_desc_special_trade),

    Preset("Hundo", "Battle/IV", "Find perfect 4★ Pokémon", "4*", RiskLevel.Info, titleRes = R.string.preset_hundo, descriptionRes = R.string.preset_desc_hundo),
    Preset("Nundo", "Battle/IV", "Find exact 0% IV Pokémon", "0attack&0defense&0hp", RiskLevel.Info, titleRes = R.string.preset_nundo, descriptionRes = R.string.preset_desc_nundo),
    Preset("Great League Candidate", "Battle/IV", "Find low attack / high bulk candidates under 1500 CP", "0-1attack&3-4defense&3-4hp&cp-1500", RiskLevel.Info, titleRes = R.string.preset_great_league, descriptionRes = R.string.preset_desc_great_league),
    Preset("Ultra League Candidate", "Battle/IV", "Find low attack / high bulk candidates under 2500 CP", "0-1attack&3-4defense&3-4hp&cp-2500", RiskLevel.Info, titleRes = R.string.preset_ultra_league, descriptionRes = R.string.preset_desc_ultra_league),
    Preset("Perfect Shadows", "Battle/IV", "Find 4★ shadows", "shadow&4*", RiskLevel.Info, titleRes = R.string.preset_perfect_shadows, descriptionRes = R.string.preset_desc_perfect_shadows),

    Preset("Shiny Review", "Collection", "Find all shiny Pokémon", "shiny", RiskLevel.Info, titleRes = R.string.preset_shiny_review, descriptionRes = R.string.preset_desc_shiny_review),
    Preset("Costume Review", "Collection", "Find all costume Pokémon", "costume", RiskLevel.Info, titleRes = R.string.preset_costume_review, descriptionRes = R.string.preset_desc_costume_review),
    Preset("Lucky Review", "Collection", "Find all lucky Pokémon", "lucky", RiskLevel.Info, titleRes = R.string.preset_lucky_review, descriptionRes = R.string.preset_desc_lucky_review)
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
    var previewPreset by remember { mutableStateOf<Preset?>(null) }

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
            ScreenTitleBar(stringResource(R.string.goal_presets), onBack, Modifier.pqStaggeredItem(visible, 0).padding(bottom = 4.dp))
            Text(
                stringResource(R.string.presets_intro),
                color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp
            )
        }
        grouped.forEach { (category, presets) ->
            item {
                com.caglar.pokequery.ui.pq.PqSectionHeader(
                    localizedPresetCategory(category).uppercase(),
                    Modifier.padding(top = 8.dp)
                )
            }
            presets.chunked(2).forEach { rowPresets ->
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowPresets.forEach { preset ->
                            PresetCard(
                                preset = preset,
                                modifier = Modifier.weight(1f),
                                onOpen = { previewPreset = preset }
                            )
                        }
                        if (rowPresets.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
    previewPreset?.let { preset ->
        PresetPreviewDialog(
            preset = preset,
            language = language,
            onDismiss = { previewPreset = null },
            onCopy = { generated ->
                previewPreset = null
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
    val title = preset.titleRes?.let { stringResource(it) } ?: preset.title
    val description = preset.descriptionRes?.let { stringResource(it) } ?: preset.description
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
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(description, color = TextSecondary, fontSize = 11.sp, maxLines = if (isExpanded) 3 else 1)
            }
            Spacer(Modifier.width(8.dp))
            com.caglar.pokequery.ui.pq.PqRiskBadge(preset.risk)
        }

        androidx.compose.animation.AnimatedVisibility(isExpanded) {
            Column(Modifier.padding(top = density.innerElementGap)) {
                com.caglar.pokequery.ui.pq.PqStringBox(preset.syntax)
                preset.warnings.forEach { warning ->
                    Text("• ${localizedPresetWarning(warning)}", color = AmberWarning, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
                if (preset.risk == RiskLevel.Medium || preset.risk == RiskLevel.High) {
                    Spacer(Modifier.height(6.dp))
                    com.caglar.pokequery.ui.pq.PqManualReviewPanel(text = stringResource(R.string.presets_review_matches))
                }
                Spacer(Modifier.height(density.innerElementGap))
                com.caglar.pokequery.ui.pq.PqPrimaryButton(
                    text = stringResource(R.string.action_preview_copy),
                    leadingIcon = Icons.Default.ContentCopy,
                    onClick = {
                        val generated = StringBuilderEngine.buildString(
                            baseQuery = preset.syntax,
                            protections = emptyList(), // Built-in; preset safety contract tested.
                            explanation = description,
                            riskLevel = preset.risk,
                            goalId = "preset",
                            title = title,
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

@Composable
private fun PresetCard(
    preset: Preset,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val density = com.caglar.pokequery.theme.density.currentDensity()
    val title = preset.titleRes?.let { stringResource(it) } ?: preset.title
    val description = preset.descriptionRes?.let { stringResource(it) } ?: preset.description
    Column(
        modifier = modifier
            .clip(shape)
            .background(CardDark)
            .border(1.dp, BorderSubtle, shape)
            .clickable(onClick = onOpen)
            .padding(horizontal = density.cardPadding, vertical = (density.innerElementGap.value * 1.1f).dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(riskColor(preset.risk).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(title.take(1), color = riskColor(preset.risk), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            com.caglar.pokequery.ui.pq.PqRiskBadge(preset.risk)
        }
        Spacer(Modifier.height(10.dp))
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Text(description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 3)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.presets_tap_preview), color = TealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PresetPreviewDialog(
    preset: Preset,
    language: String,
    onDismiss: () -> Unit,
    onCopy: (GeneratedString) -> Unit
) {
    val title = preset.titleRes?.let { stringResource(it) } ?: preset.title
    val description = preset.descriptionRes?.let { stringResource(it) } ?: preset.description
    val previewLabel = stringResource(R.string.presets_preview)
    val whatFindsLabel = stringResource(R.string.presets_what_finds)
    val checkBeforeCopyLabel = stringResource(R.string.presets_check_before_copy)
    val copyLabel = stringResource(R.string.goal_detail_copy_search_string)
    val cancelLabel = stringResource(R.string.action_cancel)
    val riskLabel = stringResource(
        when (preset.risk) {
            RiskLevel.Info -> R.string.risk_info
            RiskLevel.Low -> R.string.risk_low
            RiskLevel.Medium -> R.string.risk_medium
            RiskLevel.High -> R.string.risk_high
        }
    )
    val generated = remember(preset, language, title, description) {
        StringBuilderEngine.buildString(
            baseQuery = preset.syntax,
            protections = emptyList(),
            explanation = description,
            riskLevel = preset.risk,
            goalId = "preset",
            title = title,
            language = language
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onCopy(generated) },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = SlateBlack),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(copyLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel, color = TextSecondary)
            }
        },
        containerColor = CardDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Column {
                Text(previewLabel, color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(title, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(whatFindsLabel, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        riskLabel,
                        color = riskColor(preset.risk),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(riskColor(preset.risk).copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                if (preset.warnings.isNotEmpty() || preset.risk == RiskLevel.Medium || preset.risk == RiskLevel.High) {
                    Spacer(Modifier.height(10.dp))
                    Text(checkBeforeCopyLabel, color = AmberWarning, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    preset.warnings.forEach { warning ->
                        Text("• ${localizedPresetWarning(warning)}", color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                com.caglar.pokequery.ui.pq.PqStringBox(generated.rawSyntax)
            }
        }
    )
}

private fun riskColor(risk: RiskLevel): Color = when (risk) {
    RiskLevel.Info, RiskLevel.Low -> TealPrimary
    RiskLevel.Medium -> GoldCaution
    RiskLevel.High -> AmberWarning
}

@Composable
private fun localizedPresetCategory(category: String): String = when (category) {
    "Cleanup" -> stringResource(R.string.preset_cat_cleanup)
    "Candy/Event" -> stringResource(R.string.preset_cat_candy_event)
    "Trading" -> stringResource(R.string.preset_cat_trading)
    "Battle/IV" -> stringResource(R.string.preset_cat_battle_iv)
    "Collection" -> stringResource(R.string.preset_cat_collection)
    else -> category
}

@Composable
private fun localizedPresetWarning(warning: String): String = when (warning) {
    "Review manually before transfer." -> stringResource(R.string.preset_warning_review_transfer)
    "Count is species-wide. Check for forms." -> stringResource(R.string.preset_warning_count_forms)
    "Special trades are limited to 1 per day." -> stringResource(R.string.preset_warning_special_trade)
    else -> warning
}
