package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pokequery.theme.BackgroundDark

@Composable
fun KnowledgeBaseScreen(onBack: () -> Unit) {
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
            Text("Knowledge Base", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, start = 8.dp))
        }
        Text("Library of Search Strings will appear here.", color = Color.Gray)
    }
}

@Composable
fun FavoritesScreen(onBack: () -> Unit) {
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
            Text("Favorites", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, start = 8.dp))
        }
        Text("Saved search strings will appear here.", color = Color.Gray)
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
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
            Text("Settings", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, start = 8.dp))
        }
        Text("App configuration will appear here.", color = Color.Gray)
    }
}
