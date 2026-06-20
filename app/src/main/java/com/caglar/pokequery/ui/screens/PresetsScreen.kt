package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.RiskBadge
import androidx.compose.ui.graphics.Color

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
fun PresetsScreen(onBack: () -> Unit, onNavigateRisk: (GeneratedString) -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp)) {
        Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(painterResource(id = android.R.drawable.ic_media_previous), contentDescription = "Back", tint = TextSecondary)
            }
            Text("Popular Presets", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val grouped = POPULAR_PRESETS.groupBy { it.category }
            grouped.forEach { (category, presets) ->
                item {
                    Text(category, color = TealPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                }
                items(presets) { preset ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(preset.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                RiskBadge(preset.risk)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(preset.description, color = TextSecondary, fontSize = 14.sp)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().background(Color.Black, RoundedCornerShape(8.dp)).padding(12.dp)) {
                                Text(preset.syntax, color = TealPrimary, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                            }
                            
                            preset.warnings.forEach { warning ->
                                Text("Warning: $warning", color = AmberWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                            
                            Button(
                                onClick = {
                                    val generated = StringBuilderEngine.buildString(
                                        baseQuery = preset.syntax,
                                        protections = emptyList(), // Built-in
                                        explanation = preset.description,
                                        riskLevel = preset.risk,
                                        goalId = "preset",
                                        title = preset.title
                                    )
                                    if (preset.risk == RiskLevel.High || preset.risk == RiskLevel.Medium) {
                                        onNavigateRisk(generated)
                                    } else {
                                        clipboard.setText(AnnotatedString(preset.syntax))
                                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
