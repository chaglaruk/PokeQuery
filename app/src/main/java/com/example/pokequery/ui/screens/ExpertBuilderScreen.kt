package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pokequery.theme.BackgroundDark
import com.example.pokequery.theme.TealPrimary

@Composable
fun ExpertBuilderScreen(
    onGenerate: (String) -> Unit,
    onBack: () -> Unit
) {
    var rawQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp)) {
            TextButton(onClick = onBack) {
                Text("<- Back", color = Color.White)
            }
            Text("Expert Builder", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, start = 8.dp))
        }

        OutlinedTextField(
            value = rawQuery,
            onValueChange = { rawQuery = it },
            label = { Text("Raw Search String") },
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGenerate(rawQuery) },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Review Custom String", color = Color.White)
        }
    }
}
