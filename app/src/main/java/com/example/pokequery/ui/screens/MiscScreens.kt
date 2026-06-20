package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.data.repository.UserPreferencesRepository
import com.example.pokequery.data.repository.KnowledgeBaseRepository
import com.example.pokequery.data.repository.dataStore
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun KnowledgeBaseScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { KnowledgeBaseRepository(context) }
    var result by remember { mutableStateOf<Result<List<com.example.pokequery.data.model.Term>>?>(null) }

    LaunchedEffect(Unit) {
        result = repository.load()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary, fontWeight = FontWeight.Bold) }
            Text("Knowledge Base", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        
        val terms = result?.getOrNull()
        if (result == null) {
            Text("Loading...", color = TextSecondary)
        } else if (terms == null) {
            Text("Knowledge base could not be loaded. The local data may be damaged.", color = CoralDanger)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(terms) { term ->
                    KnowledgeTermCard(
                        syntax = term.syntax,
                        tier = term.tier,
                        risk = term.riskLevel.name,
                        description = term.descriptionEn,
                        quirks = term.knownQuirks.orEmpty(),
                        source = term.sourceUrl,
                        lastVerified = term.lastVerified
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    onCopy: (com.example.pokequery.data.model.SavedTemplate) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary, fontWeight = FontWeight.Bold) }
            Text("Favorites", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        
        if (userPrefs == null) {
            CircularProgressIndicator(color = TealPrimary)
        } else if (userPrefs!!.favorites.isEmpty()) {
            EmptyFavoritesPanel()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(userPrefs!!.favorites, key = { it.id }) { favorite ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardDark, RoundedCornerShape(16.dp))
                            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(favorite.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(favorite.riskLevel.name, color = AmberWarning, fontSize = 12.sp)
                        }
                        Text(favorite.rawSyntax, color = TealPrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = {
                                onCopy(favorite)
                            }) { Text("Copy") }
                            IconButton(onClick = { scope.launch { repository.removeFavorite(favorite.id) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary)
                            }
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
