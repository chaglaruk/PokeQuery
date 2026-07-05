package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.SavedTemplate
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.engine.GoalStringBuilder
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import com.caglar.pokequery.domain.locale.LocalizationModel
import com.caglar.pokequery.domain.risk.RiskExplanation
import com.caglar.pokequery.domain.risk.RiskExplanations
import com.caglar.pokequery.requiresRiskWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CardPremium
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.SlateBlack
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.ui.pq.PqCard
import com.caglar.pokequery.ui.pq.PqGlowCard
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.pq.PqSectionHeader
import com.caglar.pokequery.ui.pq.PqStringBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    onBack: () -> Unit,
    onNavigateRisk: (GeneratedString) -> Unit,
    onEditSearch: (GeneratedString) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val scope = rememberCoroutineScope()
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val savedFavoriteToast = stringResource(R.string.goal_detail_saved_fav)
    val copiedToast = stringResource(R.string.goal_detail_copied)

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
    var showRefineOptions by remember { mutableStateOf(false) }

    val generatedString = remember(
        goalId, excludeShiny, excludeLegendary, excludeCostume, excludeShadow,
        excludeFavorite, excludeTraded, excludeHundos, include0Star, pvpLeague, luckyMode, userPrefs?.gameLanguage, userPrefs?.appLanguage
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

        val language = LocalizationModel.SearchStringLanguage.resolve(userPrefs?.gameLanguage, userPrefs?.appLanguage)
        val baseGoal = StringBuilderEngine.buildGoal(goalId, config, language = language)
        GoalStringBuilder.buildFinal(baseGoal, optionalProtections = protections, language = language)
    }

    val favorite = remember(userPrefs, generatedString.rawSyntax) {
        userPrefs?.favorites?.firstOrNull { it.rawSyntax == generatedString.rawSyntax }
    }
    val riskExplanation = remember(generatedString.goalId, generatedString.riskLevel) {
        RiskExplanations.forGoal(generatedString.goalId, generatedString.riskLevel)
    }

    val localizedTitle = when(generatedString.goalId) {
        "safe_cleanup" -> stringResource(R.string.goal_safe_cleanup)
        "candy_prep" -> stringResource(R.string.goal_candy_prep)
        "trade_fodder" -> stringResource(R.string.goal_trade_fodder)
        "hundo_check" -> stringResource(R.string.goal_hundo_check)
        "nundo_finder" -> stringResource(R.string.goal_nundo_finder)
        "pvp_candidates" -> stringResource(R.string.goal_pvp_candidates)
        "lucky_trade" -> stringResource(R.string.goal_lucky_trade)
        "untagged" -> stringResource(R.string.goal_untagged_cleanup)
        "expert" -> stringResource(R.string.goal_custom_search)
        else -> generatedString.title
    }

    val localizedExplanation = when(generatedString.goalId) {
        "safe_cleanup" -> stringResource(R.string.goal_exp_safe_cleanup)
        "candy_prep" -> stringResource(R.string.goal_exp_candy_prep)
        "trade_fodder" -> stringResource(R.string.goal_exp_trade_fodder)
        "hundo_check" -> stringResource(R.string.goal_exp_hundo_check)
        "nundo_finder" -> stringResource(R.string.goal_exp_nundo_finder)
        "pvp_candidates" -> stringResource(R.string.goal_exp_pvp_candidates)
        "lucky_trade" -> stringResource(R.string.goal_exp_lucky_trade)
        "untagged" -> stringResource(R.string.goal_exp_untagged_cleanup)
        "expert" -> stringResource(R.string.goal_exp_expert)
        else -> generatedString.plainLanguageExplanation
    }

    val density = com.caglar.pokequery.theme.density.currentDensity()
    val isMedium = generatedString.riskLevel == RiskLevel.Medium || generatedString.riskLevel == RiskLevel.High

    Scaffold(containerColor = BackgroundDark) { paddingValues ->
        com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Top action bar matching design
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .pqStaggeredItem(visible, 0)
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
                    text = localizedTitle,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (favorite == null) {
                        scope.launch { repository.addFavorite(SavedTemplate.from(generatedString)) }
                        Toast.makeText(context, savedFavoriteToast, Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch { repository.removeFavorite(favorite.id) }
                    }
                }) {
                    Icon(
                        imageVector = if (favorite == null) Icons.Default.BookmarkBorder else Icons.Default.Bookmark,
                        contentDescription = "Favorite",
                        tint = if (favorite == null) TextSecondary.copy(alpha = 0.5f) else GoldCaution,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { /* Settings context option or similar */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(density.sectionGap))

            // RESULT block: search box + edit/copy buttons directly beneath it.
            // Risk level is conveyed via accent color (no separate top risk card).
            PqGlowCard(
                modifier = Modifier.pqStaggeredItem(visible, 1),
                accent = if (isMedium) GoldCaution else TealPrimary
            ) {
                Text(
                    text = stringResource(R.string.goal_detail_search_to_copy),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                if (goalId == "pvp_candidates") {
                    Text(
                        text = stringResource(R.string.goal_detail_choose_league),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    com.caglar.pokequery.ui.pq.PqSegmentedControl(
                        options = listOf(
                            "great" to stringResource(R.string.goal_detail_great_league),
                            "ultra" to stringResource(R.string.goal_detail_ultra_league)
                        ),
                        selected = pvpLeague,
                        onSelect = { pvpLeague = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (pvpLeague == "ultra") stringResource(R.string.goal_detail_under_2500) else stringResource(R.string.goal_detail_under_1500),
                        color = TealPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.goal_detail_pvp_rank_note),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                }
                PqStringBox(generatedString.rawSyntax)
                Spacer(Modifier.height(14.dp))
                // High-contrast Edit search button directly under search box.
                CustomActionButton(
                    text = stringResource(R.string.goal_detail_edit_search),
                    containerColor = TealPrimary,
                    onClick = { showRefineOptions = !showRefineOptions }
                )
                Spacer(Modifier.height(10.dp))
                // Copy search button.
                CustomCopyButton(
                    text = stringResource(R.string.goal_detail_copy_search_string),
                    isMedium = isMedium,
                    onClick = {
                        if (requiresRiskWarning(generatedString.riskLevel)) {
                            onNavigateRisk(generatedString)
                        } else {
                            clipboard.setText(AnnotatedString(generatedString.rawSyntax))
                            scope.launch { repository.addHistory(SavedTemplate.from(generatedString)) }
                            Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // ONE combined info / risk / help box. No separate cards for
            // "what does this do", "about count", or "tip".
            Spacer(Modifier.height(density.sectionGap))
            IllustratedCard(
                borderColor = if (isMedium) GoldCaution else TealPrimary,
                modifier = Modifier.pqStaggeredItem(visible, 2)
            ) {
                // Header: what does this search do?
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.goal_detail_what_does_this_do),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = localizedExplanation,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Medium/high-risk sub-section: what to watch out for + check first.
                if (isMedium) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = GoldCaution,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.goal_detail_watch_out),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.goal_detail_about_count_desc),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldCaution,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.goal_detail_check_first),
                            color = GoldCaution,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.goal_detail_tip_desc),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            if (showRefineOptions) {
                Spacer(Modifier.height(density.sectionGap))
                PqSectionHeader(
                    text = stringResource(R.string.goal_detail_refine),
                    modifier = Modifier.pqStaggeredItem(visible, 3)
                )
                PqCard(modifier = Modifier.pqStaggeredItem(visible, 3)) {
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
            }

            // Protected categories chips (upgraded to teal rounded pills).
            if (generatedString.protectedCategories.isNotEmpty()) {
                Spacer(Modifier.height(density.sectionGap))
                PqSectionHeader(
                    text = stringResource(R.string.goal_detail_protected),
                    modifier = Modifier.pqStaggeredItem(visible, 6)
                )
                ProtectedCategoriesSection(
                    categories = generatedString.protectedCategories,
                    modifier = Modifier.pqStaggeredItem(visible, 6)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
        }
    }
}


@Composable
fun CustomCopyButton(
    text: String,
    isMedium: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isMedium) Color(0xFFF9A825) else Color(0xFF1E88E5)
    val contentColor = SlateBlack

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.3f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

/**
 * High-contrast outlined action button used for the "Edit search" CTA directly beneath
 * the search box. Outlined so it is visually distinct from the filled copy button.
 */
@Composable
fun CustomActionButton(
    text: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = containerColor
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, containerColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun IllustratedCard(
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(borderColor.copy(alpha = 0.10f), CardPremium)
                )
            )
            .border(1.dp, borderColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
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
                Text(stringResource(R.string.goal_detail_why_risk), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(stringResource(explanation.shortReasonRes), color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary
            )
        }
        if (expanded) {
            Spacer(Modifier.height(density.innerElementGap))
            Text(stringResource(explanation.titleRes), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(explanation.detailedReasonRes), color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            Spacer(Modifier.height(8.dp))
            explanation.safetyChecklistRes.take(3).forEach { itemRes ->
                Text("• ${stringResource(itemRes)}", color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
            }
            if (explanation.relatedKnowledgeIds.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.risk_learn_more, explanation.relatedKnowledgeIds.joinToString()),
                    color = TealPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProtectedCategoriesSection(categories: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            ProtectedCategoryChip(category)
        }
    }
}

@Composable
fun ProtectedCategoryChip(category: String) {
    val displayLabel = when (category.lowercase()) {
        "shiny" -> "Shiny"
        "legendary" -> "Legendary"
        "mythical" -> "Mythical"
        "ultrabeast" -> "Ultra Beast"
        "shadow" -> "Shadow"
        "purified" -> "Purified"
        "favorite" -> "Favorite"
        "lucky" -> "Lucky"
        "traded" -> "Traded"
        "costume" -> "Costume"
        "4*" -> "4* (Keep Perfect IVs)"
        "0*" -> "0* (Keep Perfect IVs)"
        "#" -> "Tagged"
        else -> category.replaceFirstChar { it.uppercase() }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(CardPremium.copy(alpha = 0.6f))
            .border(1.dp, TealPrimary.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = displayLabel,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
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
            if (goalId == "safe_cleanup") SwitchRow(stringResource(R.string.goal_detail_include_0star), stringResource(R.string.goal_detail_collector_interest), include0Star, onInclude0Star)
            SwitchRow(stringResource(R.string.goal_detail_exclude_shinies), stringResource(R.string.goal_detail_protect_accidental), excludeShiny, onExcludeShiny)
            SwitchRow(stringResource(R.string.goal_detail_exclude_legendaries), "", excludeLegendary, onExcludeLegendary)
            SwitchRow(stringResource(R.string.goal_detail_exclude_costumes), "", excludeCostume, onExcludeCostume)
            SwitchRow(stringResource(R.string.goal_detail_exclude_favorites), "", excludeFavorite, onExcludeFavorite)
            if (goalId != "trade_fodder") SwitchRow(stringResource(R.string.goal_detail_exclude_traded), stringResource(R.string.goal_detail_traded_cannot), excludeTraded, onExcludeTraded)
            SwitchRow(stringResource(R.string.goal_detail_exclude_hundos), "", excludeHundos, onExcludeHundos)
            if (goalId == "safe_cleanup" || goalId == "untagged") SwitchRow(stringResource(R.string.goal_detail_exclude_shadow), "", excludeShadow, onExcludeShadow)
        }
        "pvp_candidates" -> {
            Text(stringResource(R.string.goal_detail_choose_league), color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(10.dp))
            com.caglar.pokequery.ui.pq.PqSegmentedControl(
                options = listOf(
                    "great" to stringResource(R.string.goal_detail_great_league),
                    "ultra" to stringResource(R.string.goal_detail_ultra_league)
                ),
                selected = pvpLeague,
                onSelect = onPvpLeague
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (pvpLeague == "ultra") stringResource(R.string.goal_detail_under_2500) else stringResource(R.string.goal_detail_under_1500),
                color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium
            )
        }
        "lucky_trade" -> {
            RadioRow(stringResource(R.string.goal_detail_older_candidates), luckyMode == "age") { onLuckyMode("age") }
            RadioRow(stringResource(R.string.goal_detail_distance_candidates), luckyMode == "distance") { onLuckyMode("distance") }
            SwitchRow(stringResource(R.string.goal_detail_must_untraded), stringResource(R.string.goal_detail_cannot_trade), excludeTraded, onExcludeTraded)
        }
        else -> Text(stringResource(R.string.goal_detail_no_options), color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun SwitchRow(label: String, subLabel: String = "", checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier
            .weight(1f)
            .padding(end = 16.dp, top = 6.dp)) {
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = TealPrimary, unselectedColor = TextSecondary))
        Text(label, color = TextPrimary, fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.padding(start = 10.dp, top = 2.dp))
    }
}
