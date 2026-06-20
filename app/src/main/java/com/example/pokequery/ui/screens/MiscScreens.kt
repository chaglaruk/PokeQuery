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
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import com.example.pokequery.theme.*
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
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text("<- Back", color = TextSecondary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Text("Knowledge Base", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        
        if (terms.isEmpty()) {
            Text("Loading...", color = TextSecondary)
        } else {
            LazyColumn {
                items(terms) { term ->
                    val riskColor = if (term["risk"] == "High") CoralDanger else if (term["risk"] == "Medium") AmberWarning else TealPrimary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(CardDark, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .border(1.dp, BorderDark, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(term["syntax"] ?: "", color = riskColor, style = MaterialTheme.typography.titleMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            Box(modifier = Modifier.background(riskColor.copy(alpha=0.1f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(term["risk"] ?: "", color = riskColor, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(term["description"] ?: "", color = TextSecondary, fontSize = 14.sp)
                        if (term["quirks"] != "null" && !term["quirks"].isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Note: ${term["quirks"]}", color = AmberWarning, style = MaterialTheme.typography.bodySmall)
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
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text("<- Back", color = TextSecondary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Text("Favorites", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        
        if (userPrefs?.favorites?.isEmpty() == true) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp).padding(bottom = 16.dp))
                    Text("No saved search strings.", color = TextSecondary)
                }
            }
        } else {
            LazyColumn {
                items(userPrefs?.favorites?.toList() ?: emptyList()) { fav ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(CardDark, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .border(1.dp, BorderDark, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .clickable {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(fav))
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
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text("<- Back", color = TextSecondary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Text("Settings", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        
        Column(modifier = Modifier.background(CardDark, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).border(1.dp, BorderDark, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Aggressive Mode", color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Switch(
                    checked = userPrefs?.aggressiveMode ?: false,
                    onCheckedChange = { scope.launch { repository.setAggressiveMode(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                )
            }
            Divider(color = BorderDark)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Expert Mode", color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Switch(
                    checked = userPrefs?.expertMode ?: false,
                    onCheckedChange = { scope.launch { repository.setExpertMode(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                )
            }
            Divider(color = BorderDark)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("First-use Guide Seen", color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Switch(
                    checked = userPrefs?.firstUseSeen ?: false,
                    onCheckedChange = { scope.launch { repository.setFirstUseSeen(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                )
            }
        }
    }
}
