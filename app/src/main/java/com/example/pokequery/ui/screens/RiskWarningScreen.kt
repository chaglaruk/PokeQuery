package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pokequery.theme.AmberWarning
import com.example.pokequery.theme.BackgroundDark

@Composable
fun RiskWarningScreen(
    onAcknowledge: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = AmberWarning, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("High Risk Action", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "The search string you are about to view contains potentially unsafe conditions or misses standard protections.",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onAcknowledge, modifier = Modifier.fillMaxWidth()) {
            Text("I Understand, Show Me")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            Text("Go Back", color = Color.Gray)
        }
    }
}
