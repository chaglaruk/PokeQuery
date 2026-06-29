package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.SavedTemplate
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.engine.GoalStringBuilder
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import com.caglar.pokequery.domain.risk.RiskExplanation
import com.caglar.pokequery.domain.risk.RiskExplanations
import com.caglar.pokequery.requiresRiskWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.ui.pq.PqCard
import com.caglar.pokequery.ui.pq.PqGlowCard
import com.caglar.pokequery.ui.pq.PqManualReviewPanel
import com.caglar.pokequery.ui.pq.PqPrimaryButton
import com.caglar.pokequery.ui.pq.PqRiskBadge
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.pq.PqSectionHeader
import com.caglar.pokequery.ui.pq.PqStringBox
import kotlinx.coroutines.launch

@Composable
fun GoalDetailScreen(
    goalId: String,
    onBack: () -> Unit,
    onNavigateRisk: (GeneratedString) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val scope = rememberCoroutineScope()
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)

    var excludeShiny by remember { mutableStateOf(true) }
    var excludeLegendary by remember { mutableStateOf(true) }
    var excludeCostume by remember { mutableStateOf(true) }
    var excludeShadow by remember { mutableStateOf(true) }
    var excludeFavorite by remember { mutableStateOf(true) }
    var excludeTraded by remember { mutableStateOf(true) }
    var excludeHundos by remember { mutableStateOf(true) }
    var include0Star by remember { mutableStateOf(false) }
    var pvpLeague by remember { mutableStateOf("great") }
    var luckyMode by remember { mutableStateOf("age") }

    val generatedString = remember(
        goalId, excludeShiny, excludeLegendary, excludeCostume, excludeShadow,
        excludeFavorite, excludeTraded, excludeHundos, include0Star, pvpLeague, luckyMode, userPrefs?.gameLanguage
    ) {
        val protections = mutableListOf<String>()
        if (excludeShiny) protections.add("shiny")
        if (excludeLegendary) protections.addAll(listOf("legendary", "mythical", "ultrabeast"))
        if (excludeCostume) protections.addAll(listOf("costume", "background", "locationbackground", "specialbackground"))
        if (excludeShadow) protections.addAll(listOf("shadow", "purified"))
        if (excludeFavorite) protections.addAll(listOf("favorite", "lucky", "#"))
        if (excludeTraded) protections.add("traded")
        if (excludeHundos) protections.add("4*")

        val config = when (goalId) {
            "safe_cleanup" -> if (include0Star) "include0Star" else ""
            "pvp_candidates" -> pvpLeague
            "lucky_trade" -> luckyMode
            else -> ""
        }

        val language = userPrefs?.gameLanguage ?: "English"
        val baseGoal = StringBuilderEngine.buildGoal(goalId, config, language = language)
        // v0.4.2 (Fix 1, BUG-001): tested helper preserves engine-mandated terms (e.g. '!traded').
        GoalStringBuilder.buildFinal(baseGoal, optionalProtections = protections, language = language)
    }

    val favorite = remember(userPrefs, generatedString.rawSyntax) {
        userPrefs?.favorites?.firstOrNull { it.rawSyntax == generatedString.rawSyntax }
    }
    val riskExplanation = remember(generatedString.goalId, generatedString.riskLevel) {
        RiskExplanations.forGoal(generatedString.goalId, generatedString.riskLevel)
    }

    val localizedTitle = when(generatedString.goalId) {
        "safe_cleanup" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_safe_cleanup)
        "candy_prep" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_candy_prep)
        "trade_fodder" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_trade_fodder)
        "hundo_check" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_hundo_check)
        "nundo_finder" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_nundo_finder)
        "pvp_candidates" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_pvp_candidates)
        "lucky_trade" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_lucky_trade)
        "untagged" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_untagged_cleanup)
        "expert" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_custom_search)
        else -> generatedString.title
    }

    val localizedExplanation = when(generatedString.goalId) {
        "safe_cleanup" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_safe_cleanup)
        "candy_prep" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_candy_prep)
        "trade_fodder" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_trade_fodder)
        "hundo_check" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_hundo_check)
        "nundo_finder" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_nundo_finder)
        "pvp_candidates" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_pvp_candidates)
        "lucky_trade" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_lucky_trade)
        "untagged" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_untagged_cleanup)
        "expert" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_exp_expert)
        else -> generatedString.plainLanguageExplanation
    }


    // v0.5.5 (Fix 1): Visual Density drives the section rhythm on this screen. The distinct
    // blocks (RESULT, REFINE, PROTECTED, NOTES, DETAILS) are separated by `sectionGap`; the
    // gaps between elements inside the RESULT card (badge→string→copy) use `innerElementGap`.
    // Compact tightens the whole scroll visibly without shrinking touch targets or titles.
    val density = com.caglar.pokequery.theme.density.currentDensity()

    Scaffold(containerColor = BackgroundDark) { paddingValues ->
        // v0.5.3 motion polish: staggered entrance. Top bar → result card → refine card. Final
        // layout is byte-identical to pre-animation (offset animates to 0, alpha to 1), so there
        // is no post-entrance layout shift. One hoisted flag → runs once, never on scroll.
        com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Top bar: back + title + favorite.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().pqStaggeredItem(visible, 0)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                val localizedTitle = when(generatedString.goalId) {
                    "safe_cleanup" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_safe_cleanup)
                    "candy_prep" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_candy_prep)
                    "trade_fodder" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_trade_fodder)
                    "hundo_check" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_hundo_check)
                    "nundo_finder" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_nundo_finder)
                    "pvp_candidates" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_pvp_candidates)
                    "lucky_trade" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_lucky_trade)
                    "untagged" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_untagged_cleanup)
                    "expert" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_custom_search)
                    else -> generatedString.title
                }
                Text(localizedTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    if (favorite == null) {
                        scope.launch { repository.addFavorite(SavedTemplate.from(generatedString)) }
                        Toast.makeText(context, context.getString(com.caglar.pokequery.R.string.goal_detail_saved_fav), Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch { repository.removeFavorite(favorite.id) }
                    }
                }) {
                    Icon(
                        imageVector = if (favorite == null) Icons.Default.StarBorder else Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = if (favorite == null) TextSecondary else TealPrimary
                    )
                }
            }

            Spacer(Modifier.height(density.sectionGap))

            // RESULT block: risk badge + string hero + copy CTA.
            PqSectionHeader(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_result), Modifier.pqStaggeredItem(visible, 1))
            PqGlowCard(modifier = Modifier.pqStaggeredItem(visible, 1)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localizedTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    PqRiskBadge(generatedString.riskLevel)
                }
                Spacer(Modifier.height(density.innerElementGap))
                PqStringBox(generatedString.rawSyntax)
                Spacer(Modifier.height(14.dp))
                PqPrimaryButton(
                    text = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_copy_search_string),
                    onClick = {
                        if (requiresRiskWarning(generatedString.riskLevel)) {
                            onNavigateRisk(generatedString)
                        } else {
                            clipboard.setText(AnnotatedString(generatedString.rawSyntax))
                            scope.launch { repository.addHistory(SavedTemplate.from(generatedString)) }
                            Toast.makeText(context, context.getString(com.caglar.pokequery.R.string.goal_detail_copied), Toast.LENGTH_SHORT).show()
                        }
                    },
                    leadingIcon = Icons.Default.ContentCopy
                )
            }

            Spacer(Modifier.height(density.sectionGap))
            RiskExplanationCard(
                explanation = riskExplanation,
                modifier = Modifier.pqStaggeredItem(visible, 2)
            )

            Spacer(Modifier.height(density.sectionGap))

            // Manual review reminder (always present for actionable goals).
            if (generatedString.riskLevel != RiskLevel.Info) {
                PqManualReviewPanel(modifier = Modifier.pqStaggeredItem(visible, 3))
                Spacer(Modifier.height(density.sectionGap))
            }

            // REFINE block: options.
            PqSectionHeader(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_refine), Modifier.pqStaggeredItem(visible, 2))
            PqCard(modifier = Modifier.pqStaggeredItem(visible, 2)) {
                OptionsPanel(
                    goalId = goalId,
                    include0Star = include0Star, onInclude0Star = { include0Star = it },
                    excludeShiny = excludeShiny, onExcludeShiny = { excludeShiny = it },
                    excludeLegendary = excludeLegendary, onExcludeLegendary = { excludeLegendary = it },
                    excludeCostume = excludeCostume, onExcludeCostume = { excludeCostume = it },
                    excludeShadow = excludeShadow, onExcludeShadow = { excludeShadow = it },
                    excludeFavorite = excludeFavorite, onExcludeFavorite = { excludeFavorite = it },
                    excludeTraded = excludeTraded, onExcludeTraded = { excludeTraded = it },
                    excludeHundos = excludeHundos, onExcludeHundos = { excludeHundos = it },
                    pvpLeague = pvpLeague, onPvpLeague = { pvpLeague = it },
                    luckyMode = luckyMode, onLuckyMode = { luckyMode = it }
                )
            }

            // Protected categories chips (when relevant).
            if (generatedString.protectedCategories.isNotEmpty()) {
                Spacer(Modifier.height(density.sectionGap))
                PqSectionHeader(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_protected), Modifier.pqStaggeredItem(visible, 4))
                PqCard(modifier = Modifier.pqStaggeredItem(visible, 4)) {
                    Text(
                        generatedString.protectedCategories.joinToString("  ") { "!$it" },
                        color = TealPrimary, fontSize = 12.sp
                    )
                }
            }

            // Warnings (goal-specific, e.g. count caveat).
            if (generatedString.warnings.isNotEmpty()) {
                Spacer(Modifier.height(density.sectionGap))
                PqSectionHeader(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_notes), Modifier.pqStaggeredItem(visible, 5))
                PqCard(modifier = Modifier.pqStaggeredItem(visible, 5)) {
                    // v0.5.1 (Fix 4): explicit vertical spacing between note rows so the
                    // Trade Fodder card (count caveat + trade disclaimer) no longer overlaps.
                                        generatedString.warnings.forEachIndexed { index, warning ->
                        if (index > 0) Spacer(Modifier.height(density.innerElementGap))
                        val locWarning = if (warning.contains("The '|' operator")) androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.warning_operator_replaced)
                        else if (warning.contains("Count is based on Pokédex")) androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.warning_count_output)
                        else if (warning.contains("Real trade eligibility depends")) androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.warning_trade_disclaimer)
                        else warning
                        Text("• $locWarning", color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }

            // DETAILS block: explanation.
            Spacer(Modifier.height(density.sectionGap))
            PqSectionHeader(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_details), Modifier.pqStaggeredItem(visible, 6))
            PqCard(modifier = Modifier.pqStaggeredItem(visible, 6)) {
                Text(localizedExplanation, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
        }
    }
}

@Composable
private fun RiskExplanationCard(
    explanation: RiskExplanation,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val density = com.caglar.pokequery.theme.density.currentDensity()
    PqCard(modifier = modifier.clickable { expanded = !expanded }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_why_risk), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(androidx.compose.ui.res.stringResource(explanation.shortReasonRes), color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary
            )
        }
        if (expanded) {
            Spacer(Modifier.height(density.innerElementGap))
            Text(androidx.compose.ui.res.stringResource(explanation.titleRes), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(androidx.compose.ui.res.stringResource(explanation.detailedReasonRes), color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            Spacer(Modifier.height(8.dp))
            explanation.safetyChecklistRes.take(3).forEach { itemRes ->
                Text("• ${androidx.compose.ui.res.stringResource(itemRes)}", color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
            }
            if (explanation.relatedKnowledgeIds.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Learn more in Knowledge Base: ${explanation.relatedKnowledgeIds.joinToString()}",
                    color = TealPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun OptionsPanel(
    goalId: String,
    include0Star: Boolean, onInclude0Star: (Boolean) -> Unit,
    excludeShiny: Boolean, onExcludeShiny: (Boolean) -> Unit,
    excludeLegendary: Boolean, onExcludeLegendary: (Boolean) -> Unit,
    excludeCostume: Boolean, onExcludeCostume: (Boolean) -> Unit,
    excludeShadow: Boolean, onExcludeShadow: (Boolean) -> Unit,
    excludeFavorite: Boolean, onExcludeFavorite: (Boolean) -> Unit,
    excludeTraded: Boolean, onExcludeTraded: (Boolean) -> Unit,
    excludeHundos: Boolean, onExcludeHundos: (Boolean) -> Unit,
    pvpLeague: String, onPvpLeague: (String) -> Unit,
    luckyMode: String, onLuckyMode: (String) -> Unit
) {
    when (goalId) {
        "safe_cleanup", "candy_prep", "trade_fodder", "untagged" -> {
            if (goalId == "safe_cleanup") SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_include_0star), androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_collector_interest), include0Star, onInclude0Star)
            SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_exclude_shinies), androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_protect_accidental), excludeShiny, onExcludeShiny)
            SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_exclude_legendaries), "", excludeLegendary, onExcludeLegendary)
            SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_exclude_costumes), "", excludeCostume, onExcludeCostume)
            SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_exclude_favorites), "", excludeFavorite, onExcludeFavorite)
            if (goalId != "trade_fodder") SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_exclude_traded), androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_traded_cannot), excludeTraded, onExcludeTraded)
            SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_exclude_hundos), "", excludeHundos, onExcludeHundos)
            if (goalId == "safe_cleanup" || goalId == "untagged") SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_exclude_shadow), "", excludeShadow, onExcludeShadow)
        }
        "pvp_candidates" -> {
            // v0.5.1 (Fix 5): Segmented control so each league shows its own concrete
            // search string. The selected league drives `config` -> StringBuilderEngine.
            Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_choose_league), color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(10.dp))
            com.caglar.pokequery.ui.pq.PqSegmentedControl(
                options = listOf("great" to "Great League", "ultra" to "Ultra League"),
                selected = pvpLeague,
                onSelect = onPvpLeague
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (pvpLeague == "ultra") androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_under_2500) else androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_under_1500),
                color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium
            )
        }
        "lucky_trade" -> {
            RadioRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_older_candidates), luckyMode == "age") { onLuckyMode("age") }
            RadioRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_distance_candidates), luckyMode == "distance") { onLuckyMode("distance") }
            SwitchRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_must_untraded), androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_cannot_trade), excludeTraded, onExcludeTraded)
        }
        else -> Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_no_options), color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun SwitchRow(label: String, subLabel: String = "", checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    // v0.5.1 (Fix 2): Robust wrapping row. Align Top so multi-line helper text never
    // overlaps the switch; generous vertical padding + explicit line heights keep labels
    // readable on 1080x2340 / 480dpi phones without fixed-height clipping.
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onCheckedChange(!checked) }.padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp, top = 6.dp)) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 19.sp)
            if (subLabel.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(subLabel, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealPrimary)
        )
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    // v0.5.1 (Fix 2): Top-aligned with line height + spacing so the radio never overlaps
    // multi-line labels (e.g. Great/Ultra League descriptions).
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = TealPrimary, unselectedColor = TextSecondary))
        Text(label, color = TextPrimary, fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.padding(start = 10.dp, top = 2.dp))
    }
}
