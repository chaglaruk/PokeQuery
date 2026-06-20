package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.SavedTemplate
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.RiskBadge
import com.caglar.pokequery.ui.components.ScopeBadge
import com.caglar.pokequery.ui.components.SettingsCard
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

    // Option States
    var excludeShiny by remember { mutableStateOf(true) }
    var excludeLegendary by remember { mutableStateOf(true) }
    var excludeCostume by remember { mutableStateOf(true) }
    var excludeShadow by remember { mutableStateOf(true) }
    var excludeFavorite by remember { mutableStateOf(true) }
    var excludeTraded by remember { mutableStateOf(true) }
    var excludeHundos by remember { mutableStateOf(true) }
    var include0Star by remember { mutableStateOf(false) } // For Safe Cleanup
    var pvpLeague by remember { mutableStateOf("great") }
    var luckyMode by remember { mutableStateOf("age") }

    // Live Generate String
    val generatedString = remember(
        goalId, excludeShiny, excludeLegendary, excludeCostume, excludeShadow, 
        excludeFavorite, excludeTraded, excludeHundos, include0Star, pvpLeague, luckyMode
    ) {
        val protections = mutableListOf<String>()
        if (excludeShiny) protections.add("shiny")
        if (excludeLegendary) { protections.add("legendary"); protections.add("mythical"); protections.add("ultrabeast") }
        if (excludeCostume) { protections.add("costume"); protections.add("background"); protections.add("locationbackground"); protections.add("specialbackground") }
        if (excludeShadow) { protections.add("shadow"); protections.add("purified") }
        if (excludeFavorite) { protections.add("favorite"); protections.add("lucky"); protections.add("#") }
        if (excludeTraded) protections.add("traded")
        if (excludeHundos) protections.add("4*")

        val config = when (goalId) {
            "safe_cleanup" -> if (include0Star) "include0Star" else ""
            "pvp_candidates" -> pvpLeague
            "lucky_trade" -> luckyMode
            else -> ""
        }
        
        // We bypass buildGoal's static protections and call buildString directly if we need dynamic protections
        val baseGoal = StringBuilderEngine.buildGoal(goalId, config)
        
        // If it's a hundo, nundo, or PvP, protections aren't typically applied
        if (goalId in listOf("hundo_check", "nundo_finder", "pvp_candidates")) {
            baseGoal
        } else {
            StringBuilderEngine.buildString(
                baseQuery = baseGoal.rawSyntax.split("&").firstOrNull { !it.startsWith("!") } ?: "", // Keep base terms, rebuild !terms
                protections = protections,
                explanation = baseGoal.plainLanguageExplanation,
                riskLevel = baseGoal.riskLevel,
                goalId = goalId,
                title = baseGoal.title
            )
        }
    }

    val isFavorited = remember(userPrefs, generatedString.rawSyntax) {
        userPrefs?.favorites?.any { it.rawSyntax == generatedString.rawSyntax } == true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // App Bar / Title
        Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(painterResource(id = android.R.drawable.ic_media_previous), contentDescription = "Back", tint = TextSecondary)
            }
            Text(generatedString.title, color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // String Preview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(generatedString.plainLanguageExplanation, color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Black, RoundedCornerShape(8.dp)).padding(16.dp)) {
                        Text(generatedString.rawSyntax, color = TealPrimary, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RiskBadge(generatedString.riskLevel)
                            ScopeBadge(generatedString.scopeBreadth)
                        }
                        IconButton(onClick = {
                            if (isFavorited) {
                                scope.launch { repository.removeFavorite(generatedString.rawSyntax) }
                            } else {
                                scope.launch { repository.addFavorite(SavedTemplate.from(generatedString)) }
                                Toast.makeText(context, "Saved to favorites", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = TealPrimary)
                        }
                    }
                }
            }

            // Warnings
            if (generatedString.warnings.isNotEmpty()) {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Warnings", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    generatedString.warnings.forEach { warning ->
                        Text("• $warning", color = AmberWarning, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Live Options
            SettingsCard {
                Text("Search Options", color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                if (goalId in listOf("safe_cleanup", "candy_prep", "trade_fodder", "untagged")) {
                    if (goalId == "safe_cleanup") {
                        SwitchRow("Include 0★ Candidates", "May include collector interest", include0Star) { include0Star = it }
                    }
                    SwitchRow("Exclude Shinies", "Protect from accidental transfer", excludeShiny) { excludeShiny = it }
                    SwitchRow("Exclude Legendaries/Ultra Beasts", "", excludeLegendary) { excludeLegendary = it }
                    SwitchRow("Exclude Costumes/Backgrounds", "", excludeCostume) { excludeCostume = it }
                    SwitchRow("Exclude Favorites & Tags", "", excludeFavorite) { excludeFavorite = it }
                    if (goalId != "trade_fodder") {
                        SwitchRow("Exclude Traded", "Already traded Pokémon cannot be traded again", excludeTraded) { excludeTraded = it }
                    }
                    SwitchRow("Exclude Hundos (4★)", "", excludeHundos) { excludeHundos = it }
                    if (goalId == "safe_cleanup" || goalId == "untagged") {
                        SwitchRow("Exclude Shadow/Purified", "", excludeShadow) { excludeShadow = it }
                    }
                } else if (goalId == "pvp_candidates") {
                    RadioRow("Great League (Under 1500 CP)", pvpLeague == "great") { pvpLeague = "great" }
                    RadioRow("Ultra League (Under 2500 CP)", pvpLeague == "ultra") { pvpLeague = "ultra" }
                } else if (goalId == "lucky_trade") {
                    RadioRow("Older Candidates (Age > 365 days)", luckyMode == "age") { luckyMode = "age" }
                    RadioRow("Distance Candidates (> 100km)", luckyMode == "distance") { luckyMode = "distance" }
                    SwitchRow("Must be untraded", "Cannot trade a traded Pokémon", excludeTraded) { excludeTraded = it }
                } else {
                    Text("No configurable options for this goal.", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }

        // Copy Button (Always visible at bottom)
        Button(
            onClick = {
                if (generatedString.riskLevel == RiskLevel.High) {
                    onNavigateRisk(generatedString)
                } else {
                    clipboard.setText(AnnotatedString(generatedString.rawSyntax))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Search String", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SwitchRow(label: String, subLabel: String = "", checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
            if (subLabel.isNotEmpty()) {
                Text(subLabel, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null, // Handled by Row clickable
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealPrimary)
        )
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary, unselectedColor = TextSecondary)
        )
        Text(label, color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun painterResource(id: Int): androidx.compose.ui.graphics.painter.Painter {
    return androidx.compose.ui.res.painterResource(id)
}
