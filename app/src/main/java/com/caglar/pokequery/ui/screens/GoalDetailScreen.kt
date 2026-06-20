package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.caglar.pokequery.requiresRiskWarning
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.*
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
        excludeFavorite, excludeTraded, excludeHundos, include0Star, pvpLeague, luckyMode
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

        val baseGoal = StringBuilderEngine.buildGoal(goalId, config)
        if (goalId in listOf("hundo_check", "nundo_finder", "pvp_candidates")) {
            baseGoal
        } else {
            StringBuilderEngine.buildString(
                baseQuery = baseGoal.rawSyntax.split("&").firstOrNull { !it.startsWith("!") }.orEmpty(),
                protections = protections,
                explanation = baseGoal.plainLanguageExplanation,
                riskLevel = baseGoal.riskLevel,
                goalId = goalId,
                title = baseGoal.title
            )
        }
    }

    val favorite = remember(userPrefs, generatedString.rawSyntax) {
        userPrefs?.favorites?.firstOrNull { it.rawSyntax == generatedString.rawSyntax }
    }
    val accent = goalAccent(goalId, generatedString.riskLevel)

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            Button(
                onClick = {
                    if (requiresRiskWarning(generatedString.riskLevel)) {
                        onNavigateRisk(generatedString)
                    } else {
                        clipboard.setText(AnnotatedString(generatedString.rawSyntax))
                        scope.launch { repository.addHistory(SavedTemplate.from(generatedString)) }
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(60.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Copy Search String", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ScreenTitleBar(generatedString.title, onBack)
            Spacer(Modifier.height(14.dp))

            RiskHeader(
                riskLevel = generatedString.riskLevel,
                subtitle = if (generatedString.riskLevel == RiskLevel.Info) "No cleanup action implied" else "Review before acting",
                imageRes = goalHeaderRes(goalId)
            )

            Spacer(Modifier.height(14.dp))
            PremiumPanel(borderColor = accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Your search string", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(goalHeaderRes(goalId)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(16.dp))
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.Black.copy(alpha = 0.82f)).padding(14.dp)
                ) {
                    Text(generatedString.rawSyntax, color = TealPrimary, fontFamily = FontFamily.Monospace, fontSize = 15.sp, lineHeight = 21.sp)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        if (favorite == null) {
                            scope.launch { repository.addFavorite(SavedTemplate.from(generatedString)) }
                            Toast.makeText(context, "Saved to favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            scope.launch { repository.removeFavorite(favorite.id) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
                ) {
                    Text(if (favorite == null) "Save Favorite" else "Remove Favorite", fontWeight = FontWeight.Bold)
                }
            }

            if (generatedString.warnings.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                WarningPanel(generatedString.warnings)
            }

            Spacer(Modifier.height(14.dp))
            PremiumPanel {
                Text("What does this do?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(8.dp))
                Text(generatedString.plainLanguageExplanation, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
            }

            if (generatedString.protectedCategories.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                PremiumPanel {
                    Text("Protected categories", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(10.dp))
                    ProtectedChipGrid(generatedString.protectedCategories)
                }
            }

            Spacer(Modifier.height(14.dp))
            OptionsPanel(
                goalId = goalId,
                include0Star = include0Star,
                onInclude0Star = { include0Star = it },
                excludeShiny = excludeShiny,
                onExcludeShiny = { excludeShiny = it },
                excludeLegendary = excludeLegendary,
                onExcludeLegendary = { excludeLegendary = it },
                excludeCostume = excludeCostume,
                onExcludeCostume = { excludeCostume = it },
                excludeShadow = excludeShadow,
                onExcludeShadow = { excludeShadow = it },
                excludeFavorite = excludeFavorite,
                onExcludeFavorite = { excludeFavorite = it },
                excludeTraded = excludeTraded,
                onExcludeTraded = { excludeTraded = it },
                excludeHundos = excludeHundos,
                onExcludeHundos = { excludeHundos = it },
                pvpLeague = pvpLeague,
                onPvpLeague = { pvpLeague = it },
                luckyMode = luckyMode,
                onLuckyMode = { luckyMode = it }
            )
        }
    }
}

@Composable
private fun WarningPanel(warnings: List<String>) {
    PremiumPanel(borderColor = AmberWarning) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(goalHeaderRes("candy_prep")),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(88.dp).clip(RoundedCornerShape(18.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("About count (important)", color = AmberWarning, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                warnings.forEach { Text("• $it", color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp) }
            }
        }
    }
}

@Composable
private fun OptionsPanel(
    goalId: String,
    include0Star: Boolean,
    onInclude0Star: (Boolean) -> Unit,
    excludeShiny: Boolean,
    onExcludeShiny: (Boolean) -> Unit,
    excludeLegendary: Boolean,
    onExcludeLegendary: (Boolean) -> Unit,
    excludeCostume: Boolean,
    onExcludeCostume: (Boolean) -> Unit,
    excludeShadow: Boolean,
    onExcludeShadow: (Boolean) -> Unit,
    excludeFavorite: Boolean,
    onExcludeFavorite: (Boolean) -> Unit,
    excludeTraded: Boolean,
    onExcludeTraded: (Boolean) -> Unit,
    excludeHundos: Boolean,
    onExcludeHundos: (Boolean) -> Unit,
    pvpLeague: String,
    onPvpLeague: (String) -> Unit,
    luckyMode: String,
    onLuckyMode: (String) -> Unit
) {
    PremiumPanel {
        Text("Search Options", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(8.dp))
        when (goalId) {
            "safe_cleanup", "candy_prep", "trade_fodder", "untagged" -> {
                if (goalId == "safe_cleanup") SwitchRow("Include 0★ Candidates", "May include collector interest", include0Star, onInclude0Star)
                SwitchRow("Exclude Shinies", "Protect from accidental transfer", excludeShiny, onExcludeShiny)
                SwitchRow("Exclude Legendaries/Ultra Beasts", "", excludeLegendary, onExcludeLegendary)
                SwitchRow("Exclude Costumes/Backgrounds", "", excludeCostume, onExcludeCostume)
                SwitchRow("Exclude Favorites & Tags", "", excludeFavorite, onExcludeFavorite)
                if (goalId != "trade_fodder") SwitchRow("Exclude Traded", "Already traded Pokémon cannot be traded again", excludeTraded, onExcludeTraded)
                SwitchRow("Exclude Hundos (4★)", "", excludeHundos, onExcludeHundos)
                if (goalId == "safe_cleanup" || goalId == "untagged") SwitchRow("Exclude Shadow/Purified", "", excludeShadow, onExcludeShadow)
            }
            "pvp_candidates" -> {
                RadioRow("Great League (Under 1500 CP)", pvpLeague == "great") { onPvpLeague("great") }
                RadioRow("Ultra League (Under 2500 CP)", pvpLeague == "ultra") { onPvpLeague("ultra") }
            }
            "lucky_trade" -> {
                RadioRow("Older Candidates (Age > 365 days)", luckyMode == "age") { onLuckyMode("age") }
                RadioRow("Distance Candidates (> 100km)", luckyMode == "distance") { onLuckyMode("distance") }
                SwitchRow("Must be untraded", "Cannot trade a traded Pokémon", excludeTraded, onExcludeTraded)
            }
            else -> Text("No configurable options for this goal.", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SwitchRow(label: String, subLabel: String = "", checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onCheckedChange(!checked) }.padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
            if (subLabel.isNotEmpty()) Text(subLabel, color = TextSecondary, fontSize = 12.sp)
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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = TealPrimary, unselectedColor = TextSecondary))
        Text(label, color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
    }
}

private fun goalAccent(goalId: String, riskLevel: RiskLevel): Color = when (goalId) {
    "safe_cleanup", "untagged" -> TealPrimary
    "candy_prep", "trade_fodder", "lucky_trade" -> AmberWarning
    "hundo_check", "nundo_finder" -> PurpleIV
    "pvp_candidates", "expert" -> BlueCTA
    else -> riskLevel.toneColor()
}
