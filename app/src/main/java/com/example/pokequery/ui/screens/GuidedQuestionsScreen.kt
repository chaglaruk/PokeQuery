package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.theme.*

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
        // Top Bar Mock
        Row(modifier = Modifier.padding(bottom = 24.dp)) {
            TextButton(onClick = onBack) {
                Text("<- Back", color = Color.White)
            }
            Text("Customize Cleanup", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, start = 8.dp))
        }

        if (goalId == "safe_cleanup") {
            Text("Safe Cleanup settings", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            Text("This will target 1★ low-value review candidates. It excludes protected categories.", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Include 0★ Candidates?", color = Color.White)
                    Text(
                        "WARNING: 0★ is a broad low-IV band and NOT exact 0% IV. It may include collector-interest Pokémon.",
                        color = AmberWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                    )
                }
                Switch(
                    checked = include0Star,
                    onCheckedChange = { include0Star = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary, checkedTrackColor = TealPrimary.copy(alpha = 0.5f))
                )
            }
        } else {
            Text("No configuration needed for this goal.", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onGenerate(include0Star) },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Review Search String", color = Color.White)
        }
    }
}
