package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.data.repository.UserPreferencesRepository
import com.example.pokequery.data.repository.dataStore
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.*
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
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary, fontWeight = FontWeight.Bold) }
            Text("Knowledge Base", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        
        if (terms.isEmpty()) {
            Text("Loading...", color = TextSecondary)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(terms) { term ->
                    KnowledgeTermCard(
                        syntax = term["syntax"] ?: "",
                        risk = term["risk"] ?: "",
                        description = term["description"] ?: "",
                        quirks = term["quirks"] ?: ""
                    )
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
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary, fontWeight = FontWeight.Bold) }
            Text("Favorites", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        
        if (userPrefs?.favorites?.isEmpty() == true) {
            EmptyFavoritesPanel()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(userPrefs?.favorites?.toList() ?: emptyList()) { fav ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardDark, RoundedCornerShape(16.dp))
                            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                            .clickable {
                                clipboard?.setText(AnnotatedString(fav))
                                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(fav, color = TealPrimary, modifier = Modifier.weight(1f), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        IconButton(onClick = { scope.launch { repository.removeFavorite(fav) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary)
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
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary, fontWeight = FontWeight.Bold) }
            Text("Settings", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        
        SettingsCard {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("First-use Guide Seen", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = userPrefs?.firstUseSeen ?: false,
                    onCheckedChange = { scope.launch { repository.setFirstUseSeen(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                )
            }
        }
    }
}
