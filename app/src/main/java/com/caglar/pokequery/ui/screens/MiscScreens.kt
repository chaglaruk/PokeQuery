package com.caglar.pokequery.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.KnowledgeBaseRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(startExpanded: Boolean = false, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { KnowledgeBaseRepository(context) }
    var result by remember { mutableStateOf<Result<List<com.caglar.pokequery.data.model.Term>>?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        result = repository.load()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(painterResource(id = android.R.drawable.ic_media_previous), contentDescription = "Back", tint = TextSecondary)
            }
            Text("Knowledge Base", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text("Search terms (e.g. shiny, distance)", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        val terms = result?.getOrNull()
        if (result == null) {
            CircularProgressIndicator(color = TealPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (terms == null) {
            Text("Knowledge base could not be loaded. The local data may be damaged.", color = CoralDanger)
        } else {
            val filteredTerms = terms.filter {
                it.syntax.contains(searchQuery, ignoreCase = true) || 
                it.descriptionEn.contains(searchQuery, ignoreCase = true)
            }
            
            if (filteredTerms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No search terms found for \"$searchQuery\".", color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredTerms) { term ->
                        var expanded by remember { mutableStateOf(startExpanded && term == filteredTerms.firstOrNull()) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(term.syntax, color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                        Text("Tier ${term.tier} • Risk: ${term.riskLevel}", color = TextSecondary, fontSize = 12.sp)
                                    }
                                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                                }
                                
                                AnimatedVisibility(visible = expanded) {
                                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                        Text(term.descriptionEn, color = TextPrimary, fontSize = 14.sp)
                                        if (!term.knownQuirks.isNullOrEmpty()) {
                                            Text("Quirks: ${term.knownQuirks}", color = AmberWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    onCopy: (com.caglar.pokequery.data.model.SavedTemplate) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(painterResource(id = android.R.drawable.ic_media_previous), contentDescription = "Back", tint = TextSecondary)
            }
            Text("Favorites", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        
        if (userPrefs == null) {
            CircularProgressIndicator(color = TealPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
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
                            Button(
                                onClick = { onCopy(favorite) },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Copy", color = Color.White) }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { scope.launch { repository.removeFavorite(favorite.id) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralDanger)
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(id = android.R.drawable.ic_media_previous), contentDescription = "Back", tint = TextSecondary)
                }
                Text("Settings", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
        }
        
        item {
            SettingsCard {
                Text("General", color = TealPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text("First-use Guide Seen", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Resetting this will show the onboarding tour again.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = userPrefs?.firstUseSeen ?: false,
                        onCheckedChange = { scope.launch { repository.setFirstUseSeen(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                    )
                }
                
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

                Text("Default Duplicate Threshold", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val currentThreshold = userPrefs?.duplicateThreshold ?: "Count 3 (Safe)"
                    listOf("Count 3 (Safe)", "Count 2").forEach { threshold ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                            scope.launch { repository.setSetting(UserPreferencesRepository.DUPLICATE_THRESHOLD, threshold) }
                        }) {
                            RadioButton(
                                selected = currentThreshold == threshold,
                                onClick = { scope.launch { repository.setSetting(UserPreferencesRepository.DUPLICATE_THRESHOLD, threshold) } },
                                colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                            )
                            Text(threshold, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
                
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

                Text("Language Mode", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("English (Turkish beta disabled for stability)", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        item {
            SettingsCard {
                Text("Safety", color = TealPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text("Warning Behavior", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Always warn on Broad/Medium strings (Deferred setting forced for safety).", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = true, // Hardcoded for safety per product spec
                        onCheckedChange = { /* Disabled by product spec */ },
                        enabled = false,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                    )
                }

                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

                Text("Copy Behavior", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val currentCopy = userPrefs?.copyBehavior ?: "Confirm Risky Copy"
                    listOf("Confirm Risky Copy", "Direct Copy").forEach { behavior ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                            scope.launch { repository.setSetting(UserPreferencesRepository.COPY_BEHAVIOR, behavior) }
                        }) {
                            RadioButton(
                                selected = currentCopy == behavior,
                                onClick = { scope.launch { repository.setSetting(UserPreferencesRepository.COPY_BEHAVIOR, behavior) } },
                                colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                            )
                            Text(behavior, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }

                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

                Text("Default Safety Style", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    val currentStyle = userPrefs?.safetyStyle ?: "Conservative"
                    listOf("Conservative", "Balanced", "Power User").forEach { style ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                            scope.launch { repository.setSetting(UserPreferencesRepository.SAFETY_STYLE, style) }
                        }) {
                            RadioButton(
                                selected = currentStyle == style,
                                onClick = { scope.launch { repository.setSetting(UserPreferencesRepository.SAFETY_STYLE, style) } },
                                colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                            )
                            Text(style, color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            SettingsCard {
                Text("Data & Privacy", color = TealPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                
                Text("Privacy Notes (No data collected)", color = TextPrimary, modifier = Modifier.padding(vertical = 8.dp))
                Text("Third-party Notices", color = TextPrimary, modifier = Modifier.padding(vertical = 8.dp))
                
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    "Reset All Settings & Favorites",
                    color = CoralDanger,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().clickable { scope.launch { repository.resetSettings() } }.padding(vertical = 8.dp)
                )
            }
        }

        item {
            SettingsCard {
                Text("About", color = TealPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Text("PokeQuery v0.3.1", color = TextPrimary)
                Text("Built for Pokémon GO Trainers", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
