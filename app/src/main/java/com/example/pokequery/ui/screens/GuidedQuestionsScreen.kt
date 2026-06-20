package com.example.pokequery.ui.screens

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
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.SettingsCard

@Composable
fun GuidedQuestionsScreen(
    goalId: String,
    onGenerate: (Boolean) -> Unit, // pass include0Star or other config
    onBack: () -> Unit
) {
    var include0Star by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary, fontWeight = FontWeight.Bold) }
            Text("Customize Cleanup", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        if (goalId == "safe_cleanup") {
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
        } else {
            SettingsCard {
                Text("No configuration needed for this goal.", color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onGenerate(include0Star) },
            colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Review Search String", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
