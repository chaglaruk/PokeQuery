package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.SettingsCard

@Composable
fun GuidedQuestionsScreen(
    goalId: String,
    onGenerate: (String) -> Unit,
    onBack: () -> Unit
) {
    var configString by remember { mutableStateOf("") }
    
    // Set defaults when screen opens based on goalId
    LaunchedEffect(goalId) {
        if (goalId == "pvp_candidates") configString = "great"
        if (goalId == "lucky_trade") configString = "age"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary, fontWeight = FontWeight.Bold) }
            Text(
                text = when(goalId) {
                    "pvp_candidates" -> "Configure PvP Search"
                    "lucky_trade" -> "Configure Trade Search"
                    else -> "Customize Cleanup"
                }, 
                color = TextPrimary, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (goalId == "safe_cleanup") {
            var include0Star by remember { mutableStateOf(false) }
            LaunchedEffect(include0Star) {
                configString = if (include0Star) "include0Star" else ""
            }
            SettingsCard {
                Text("Safe Cleanup Settings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text("This will target 1★ low-value review candidates. It excludes protected categories.", color = TextSecondary, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Include 0★ Candidates?", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            "WARNING: 0★ is a broad low-IV band and NOT exact 0% IV. It may include collector-interest Pokémon.",
                            color = AmberWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                        )
                    }
                    Switch(
                        checked = include0Star,
                        onCheckedChange = { include0Star = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                    )
                }
            }
        } else if (goalId == "pvp_candidates") {
            SettingsCard {
                Text("Select League", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text("Choose which PvP league you are hunting candidates for.", color = TextSecondary, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = configString == "great",
                        onClick = { configString = "great" },
                        colors = RadioButtonDefaults.colors(selectedColor = BlueCTA, unselectedColor = TextSecondary)
                    )
                    Text("Great League (Under 1500 CP)", color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = configString == "ultra",
                        onClick = { configString = "ultra" },
                        colors = RadioButtonDefaults.colors(selectedColor = BlueCTA, unselectedColor = TextSecondary)
                    )
                    Text("Ultra League (Under 2500 CP)", color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
                }
            }
        } else if (goalId == "lucky_trade") {
            SettingsCard {
                Text("Select Trade Mode", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text("Choose the type of trade candidates you want to find.", color = TextSecondary, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = configString == "age",
                        onClick = { configString = "age" },
                        colors = RadioButtonDefaults.colors(selectedColor = BlueCTA, unselectedColor = TextSecondary)
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Older Candidates (Age > 365 days)", color = TextPrimary)
                        Text("Better chance to trigger a Lucky Trade.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = configString == "distance",
                        onClick = { configString = "distance" },
                        colors = RadioButtonDefaults.colors(selectedColor = BlueCTA, unselectedColor = TextSecondary)
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Distance Candidates (Distance > 100km)", color = TextPrimary)
                        Text("Guaranteed XL Candy when traded.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            SettingsCard {
                Text("No configuration needed for this goal.", color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onGenerate(configString) },
            colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Review Search String", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
