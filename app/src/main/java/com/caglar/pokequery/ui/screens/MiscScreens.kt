package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.SavedTemplate
import com.caglar.pokequery.data.model.Term
import com.caglar.pokequery.data.repository.KnowledgeBaseRepository
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(startExpanded: Boolean = false, onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val repository = remember { KnowledgeBaseRepository(context) }
    var result by remember { mutableStateOf<Result<List<Term>>?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) { result = repository.load() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { ScreenTitleBar("Knowledge Base", onBack) }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search terms (e.g. shiny, distance)", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        val terms = result?.getOrNull()
        when {
            result == null -> item { CircularProgressIndicator(color = TealPrimary, modifier = Modifier.padding(20.dp)) }
            terms == null -> item { Text("Knowledge base could not be loaded. The local data may be damaged.", color = CoralDanger) }
            else -> {
                val categories = listOf("All") + terms.map { it.category }.distinct().sorted()
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BlueCTA,
                                    selectedLabelColor = Color.White,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                val filtered = terms.filter {
                    (category == "All" || it.category == category) &&
                        (it.syntax.contains(searchQuery, true) || it.descriptionEn.contains(searchQuery, true) || it.category.contains(searchQuery, true))
                }
                if (filtered.isEmpty()) {
                    item { EmptyState("No search terms found", "Try another term or category.") }
                } else {
                    items(filtered, key = { it.id }) { term ->
                        var expanded by remember { mutableStateOf(startExpanded && term == filtered.firstOrNull()) }
                        KnowledgeTermRow(term, expanded, onToggle = { expanded = !expanded }) {
                            clipboard.setText(AnnotatedString(term.syntax))
                            Toast.makeText(context, "Copied ${term.syntax}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    onCopy: (SavedTemplate) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    SavedTemplateScreen(
        title = "Favorites",
        templates = userPrefs?.favorites,
        emptyTitle = "No saved search strings",
        emptySubtitle = "Tap Save Favorite on a generated string.",
        onBack = onBack,
        onCopy = onCopy,
        onDelete = { scope.launch { repository.removeFavorite(it.id) } }
    )
}

@Composable
fun HistoryScreen(
    onCopy: (SavedTemplate) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)

    SavedTemplateScreen(
        title = "History",
        templates = userPrefs?.history,
        emptyTitle = "No recent copies yet",
        emptySubtitle = "Copied search strings will appear here.",
        onBack = onBack,
        onCopy = onCopy
    )
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 22.dp)
    ) {
        item { ScreenTitleBar("Settings", onBack) }

        item {
            PremiumPanel {
                Text("General", color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Visual Density", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { RadioRow("Comfortable", userPrefs?.visualDensity == "Comfortable") { scope.launch { repository.setSetting(UserPreferencesRepository.VISUAL_DENSITY, "Comfortable") } } }
                    Box(Modifier.weight(1f)) { RadioRow("Compact", userPrefs?.visualDensity == "Compact") { scope.launch { repository.setSetting(UserPreferencesRepository.VISUAL_DENSITY, "Compact") } } }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(end = 16.dp)) {
                        Text("First-use guide seen", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Turn off to show onboarding again.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = userPrefs?.firstUseSeen ?: false,
                        onCheckedChange = { scope.launch { repository.setFirstUseSeen(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                    )
                }
            }
        }

        item {
            PremiumPanel(borderColor = TealPrimary) {
                Text("Search & Language", color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Search Term Language", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Applies language translations to generated strings.", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                RadioRow("English", userPrefs?.gameLanguage == "English") { scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, "English") } }
                RadioRow("Turkish", userPrefs?.gameLanguage == "Turkish") { scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, "Turkish") } }
                
                Spacer(Modifier.height(14.dp))
                Text("Copy Behavior", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                RadioRow("Always Warn", userPrefs?.copyBehavior == "Always Warn") { scope.launch { repository.setSetting(UserPreferencesRepository.COPY_BEHAVIOR, "Always Warn") } }
                RadioRow("Confirm Risky Copy", userPrefs?.copyBehavior == "Confirm Risky Copy") { scope.launch { repository.setSetting(UserPreferencesRepository.COPY_BEHAVIOR, "Confirm Risky Copy") } }

                Spacer(Modifier.height(14.dp))
                Text("Default Duplicate Threshold", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                RadioRow("Count 2 (Strict)", userPrefs?.duplicateThreshold == "Count 2 (Strict)") { scope.launch { repository.setSetting(UserPreferencesRepository.DUPLICATE_THRESHOLD, "Count 2 (Strict)") } }
                RadioRow("Count 3 (Safe)", userPrefs?.duplicateThreshold == "Count 3 (Safe)") { scope.launch { repository.setSetting(UserPreferencesRepository.DUPLICATE_THRESHOLD, "Count 3 (Safe)") } }
                
                Spacer(Modifier.height(14.dp))
                Text("Safety", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Medium and High risk copies always show confirmation first.", color = TextSecondary, fontSize = 13.sp)
                Text("The app generates text only. It never connects to Pokémon GO.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        item {
            PremiumPanel {
                Text("Data & privacy", color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("No account access. No scraping. Local favorites and history only.", color = TextPrimary)
                Text("Privacy notes and third-party notices are in docs/release.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(16.dp))
                Text("Clear favorites", color = CoralDanger, modifier = Modifier.fillMaxWidth().clickable { scope.launch { repository.clearFavorites() } }.padding(vertical = 8.dp))
                Text("Clear history", color = CoralDanger, modifier = Modifier.fillMaxWidth().clickable { scope.launch { repository.clearHistory() } }.padding(vertical = 8.dp))
                Text("Reset all settings", color = CoralDanger, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clickable { scope.launch { repository.resetSettings() } }.padding(vertical = 8.dp))
            }
        }

        item {
            PremiumPanel {
                Text("About", color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("PokeQuery v0.3.4", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Safe search strings for Pokémon GO", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun KnowledgeTermRow(term: Term, expanded: Boolean, onToggle: () -> Unit, onCopy: () -> Unit) {
    PremiumPanel(borderColor = term.riskLevel.toneColor()) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onToggle), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(term.syntax, color = term.riskLevel.toneColor(), fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = FontFamily.Monospace)
                Text("${term.category} • Tier ${term.tier} • Risk: ${term.riskLevel}", color = TextSecondary, fontSize = 12.sp)
            }
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(top = 12.dp)) {
                Text(term.descriptionEn, color = TextPrimary, fontSize = 14.sp)
                if (!term.knownQuirks.isNullOrEmpty()) {
                    Text("Note: ${term.knownQuirks}", color = AmberWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
                Text("Source: ${term.sourceUrl}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                Text("Last verified: ${term.lastVerified}", color = TextSecondary, fontSize = 11.sp)
                OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Copy token")
                }
            }
        }
    }
}

@Composable
private fun SavedTemplateScreen(
    title: String,
    templates: List<SavedTemplate>?,
    emptyTitle: String,
    emptySubtitle: String,
    onBack: () -> Unit,
    onCopy: (SavedTemplate) -> Unit,
    onDelete: ((SavedTemplate) -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { ScreenTitleBar(title, onBack) }
        when {
            templates == null -> item { CircularProgressIndicator(color = TealPrimary, modifier = Modifier.padding(20.dp)) }
            templates.isEmpty() -> item { EmptyState(emptyTitle, emptySubtitle) }
            else -> items(templates, key = { it.id }) { template ->
                SavedTemplateRow(template, onCopy = { onCopy(template) }, onDelete = onDelete?.let { { it(template) } })
            }
        }
    }
}

@Composable
private fun SavedTemplateRow(template: SavedTemplate, onCopy: () -> Unit, onDelete: (() -> Unit)? = null) {
    PremiumPanel(borderColor = template.riskLevel.toneColor()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(template.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${template.riskLevel} • ${template.goalId}", color = TextSecondary, fontSize = 12.sp)
            }
            RiskBadge(template.riskLevel)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.80f)).padding(12.dp)) {
            Text(template.rawSyntax, color = TealPrimary, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onCopy, colors = ButtonDefaults.buttonColors(containerColor = BlueCTA), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Copy", color = Color.White)
            }
            if (onDelete != null) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralDanger) }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Box(Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GoalArt("safe_cleanup", TealPrimary, Modifier.size(132.dp))
            Spacer(Modifier.height(18.dp))
            Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = TealPrimary, unselectedColor = TextSecondary))
        Text(label, color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
    }
}
