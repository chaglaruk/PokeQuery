package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pokequery.data.repository.UserPreferencesRepository
import com.example.pokequery.data.repository.dataStore
import com.example.pokequery.theme.BackgroundDark
import com.example.pokequery.theme.CardDark
import com.example.pokequery.theme.TealPrimary
import kotlinx.coroutines.launch

@Composable
fun KnowledgeBaseScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var terms by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        val jsonString = context.assets.open("knowledgebase.json").bufferedReader().use { it.readText() }
        val jsonArray = org.json.JSONArray(jsonString)
        val parsedList = mutableListOf<Map<String, String>>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            parsedList.add(
                mapOf(
                    "syntax" to obj.optString("syntax"),
                    "description" to obj.optString("description_en"),
                    "risk" to obj.optString("riskLevel"),
                    "quirks" to obj.optString("knownQuirks")
                )
            )
        }
        terms = parsedList
    }

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
        
        if (terms.isEmpty()) {
            Text("Loading...", color = Color.Gray)
        } else {
            LazyColumn {
                items(terms) { term ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(CardDark, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(term["syntax"] ?: "", color = TealPrimary, style = MaterialTheme.typography.bodyLarge)
                            Text(term["risk"] ?: "", color = if (term["risk"] == "High") Color(0xFFE57373) else if (term["risk"] == "Medium") Color(0xFFFFC107) else TealPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(term["description"] ?: "", color = Color.White)
                        if (term["quirks"] != "null" && !term["quirks"].isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Note: ${term["quirks"]}", color = Color(0xFFFFC107), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

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
        
        if (userPrefs?.favorites?.isEmpty() == true) {
            Text("No saved search strings.", color = Color.Gray)
        } else {
            LazyColumn {
                items(userPrefs?.favorites?.toList() ?: emptyList()) { fav ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(CardDark, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(fav))
                                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(fav, color = TealPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { scope.launch { repository.removeFavorite(fav) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

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
        
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Aggressive Mode", color = Color.White)
            Switch(
                checked = userPrefs?.aggressiveMode ?: false,
                onCheckedChange = { scope.launch { repository.setAggressiveMode(it) } },
                colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Expert Mode", color = Color.White)
            Switch(
                checked = userPrefs?.expertMode ?: false,
                onCheckedChange = { scope.launch { repository.setExpertMode(it) } },
                colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("First-use Guide Seen", color = Color.White)
            Switch(
                checked = userPrefs?.firstUseSeen ?: false,
                onCheckedChange = { scope.launch { repository.setFirstUseSeen(it) } },
                colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
        }
    }
}
