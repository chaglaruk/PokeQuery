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

    Scaffold(containerColor = BackgroundDark) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Top bar: back + title + favorite.
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(generatedString.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    if (favorite == null) {
                        scope.launch { repository.addFavorite(SavedTemplate.from(generatedString)) }
                        Toast.makeText(context, "Saved to favorites", Toast.LENGTH_SHORT).show()
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

            Spacer(Modifier.height(16.dp))

            // RESULT block: risk badge + string hero + copy CTA.
            PqSectionHeader("RESULT")
            PqGlowCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(generatedString.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    PqRiskBadge(generatedString.riskLevel)
                }
                Spacer(Modifier.height(10.dp))
                PqStringBox(generatedString.rawSyntax)
                Spacer(Modifier.height(14.dp))
                PqPrimaryButton(
                    text = "Copy Search String",
                    onClick = {
                        if (requiresRiskWarning(generatedString.riskLevel)) {
                            onNavigateRisk(generatedString)
                        } else {
                            clipboard.setText(AnnotatedString(generatedString.rawSyntax))
                            scope.launch { repository.addHistory(SavedTemplate.from(generatedString)) }
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    },
                    leadingIcon = Icons.Default.ContentCopy
                )
            }

            Spacer(Modifier.height(18.dp))

            // Manual review reminder (always present for actionable goals).
            if (generatedString.riskLevel != RiskLevel.Info) {
                PqManualReviewPanel()
                Spacer(Modifier.height(18.dp))
            }

            // REFINE block: options.
            PqSectionHeader("REFINE")
            PqCard {
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
                Spacer(Modifier.height(18.dp))
                PqSectionHeader("PROTECTED")
                PqCard {
                    Text(
                        generatedString.protectedCategories.joinToString("  ") { "!$it" },
                        color = TealPrimary, fontSize = 12.sp
                    )
                }
            }

            // Warnings (goal-specific, e.g. count caveat).
            if (generatedString.warnings.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                PqSectionHeader("NOTES")
                PqCard {
                    // v0.5.1 (Fix 4): explicit vertical spacing between note rows so the
                    // Trade Fodder card (count caveat + trade disclaimer) no longer overlaps.
                    generatedString.warnings.forEachIndexed { index, warning ->
                        if (index > 0) Spacer(Modifier.height(8.dp))
                        Text("• $warning", color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }

            // DETAILS block: explanation.
            Spacer(Modifier.height(18.dp))
            PqSectionHeader("DETAILS")
            PqCard {
                Text(generatedString.plainLanguageExplanation, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
            }
            Spacer(Modifier.height(24.dp))
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
            if (goalId == "safe_cleanup") SwitchRow("Include 0★ candidates", "May include collector interest", include0Star, onInclude0Star)
            SwitchRow("Exclude Shinies", "Protect from accidental transfer", excludeShiny, onExcludeShiny)
            SwitchRow("Exclude Legendaries / Ultra Beasts", "", excludeLegendary, onExcludeLegendary)
            SwitchRow("Exclude Costumes / Backgrounds", "", excludeCostume, onExcludeCostume)
            SwitchRow("Exclude Favorites & Tags", "", excludeFavorite, onExcludeFavorite)
            if (goalId != "trade_fodder") SwitchRow("Exclude Traded", "Already traded Pokémon cannot be traded again", excludeTraded, onExcludeTraded)
            SwitchRow("Exclude Hundos (4★)", "", excludeHundos, onExcludeHundos)
            if (goalId == "safe_cleanup" || goalId == "untagged") SwitchRow("Exclude Shadow / Purified", "", excludeShadow, onExcludeShadow)
        }
        "pvp_candidates" -> {
            // v0.5.1 (Fix 5): Segmented control so each league shows its own concrete
            // search string. The selected league drives `config` -> StringBuilderEngine.
            Text("Choose a league. The generated string updates instantly.", color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(10.dp))
            com.caglar.pokequery.ui.pq.PqSegmentedControl(
                options = listOf("great" to "Great League", "ultra" to "Ultra League"),
                selected = pvpLeague,
                onSelect = onPvpLeague
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (pvpLeague == "ultra") "Under 2500 CP" else "Under 1500 CP",
                color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium
            )
        }
        "lucky_trade" -> {
            RadioRow("Older candidates (Age > 365 days)", luckyMode == "age") { onLuckyMode("age") }
            RadioRow("Distance candidates (> 100km)", luckyMode == "distance") { onLuckyMode("distance") }
            SwitchRow("Must be untraded", "Cannot trade a traded Pokémon", excludeTraded, onExcludeTraded)
        }
        else -> Text("No configurable options for this goal.", color = TextSecondary, fontSize = 13.sp)
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
