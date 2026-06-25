package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Star
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
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.components.*
import com.caglar.pokequery.ui.motion.pqSpringPop
import com.caglar.pokequery.ui.motion.pqStaggeredItem
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

    // v0.5.3 motion polish: staggered entrance — title bar + banner fade in first; list rows
    // appear at rest (no cascade while scrolling). One hoisted flag → runs once only.
    // v0.5.5 (Fix 1): the gap between homogeneous KB term rows follows the Visual Density
    // `listGap` token, so Compact tightens the list visibly.
    val density = currentDensity()
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(density.listGap),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { ScreenTitleBar("Knowledge Base", onBack, Modifier.pqStaggeredItem(visible, 0)) }
        // v0.5.2 (Fix 9): Turkish guardrail banner. Turkish tokens are beta/unverified; the
        // "Language-sensitive" / "Beta" / "Risky" badges below come from the per-term metadata.
        item {
            val shape = RoundedCornerShape(14.dp)
            Row(
                Modifier.fillMaxWidth().pqStaggeredItem(visible, 1).clip(shape).background(AmberWarning.copy(alpha = 0.08f))
                    .border(1.dp, AmberWarning.copy(alpha = 0.35f), shape).padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(Modifier.size(6.dp).background(AmberWarning, androidx.compose.foundation.shape.CircleShape))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Turkish search tokens are BETA and language-sensitive. A wrong localized form can silently return no results. Verify in Pokémon GO before relying on a Turkish search.",
                    color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp
                )
            }
        }
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
                                    selectedContainerColor = TealPrimary.copy(alpha = 0.18f),
                                    selectedLabelColor = TealPrimary,
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
                    item {
                        com.caglar.pokequery.ui.pq.PqEmptyState(
                            icon = Icons.Default.Search,
                            title = "No search terms found",
                            subtitle = "Try another term or category."
                        )
                    }
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

    // v0.5.3 motion polish: staggered entrance — title bar + first panel fade in; subsequent
    // panels appear at rest (no cascade while scrolling). One hoisted flag → runs once only.
    // v0.5.5 (Fix 1): the gap between the distinct Settings PremiumPanels follows the Visual
    // Density `sectionGap` token; inner element gaps use `innerElementGap`. Compact tightens
    // the whole screen visibly.
    val density = currentDensity()
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(density.sectionGap),
        contentPadding = PaddingValues(bottom = 22.dp)
    ) {
        item { ScreenTitleBar("Settings", onBack, Modifier.pqStaggeredItem(visible, 0)) }

        item {
            PremiumPanel {
                Text("General", color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Visual Density", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Changes card padding, chip spacing and list gaps across the app. Comfortable is the default premium feel; Compact fits more on screen. Title sizes stay the same.",
                    color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp
                )
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

                // v0.5.2 (Fix 7): LAYER A — App Language (UI text only).
                // This controls the app interface text. It does NOT change the generated
                // Pokémon GO search strings. The two layers are intentionally independent.
                // v0.5.2.1 hotfix: full translated UI resources are not shipped yet, so this
                // is presented honestly as a foundation/preference. Selecting a language never
                // black-screens the app (in-process locale only; no OS LocaleManager call).
                // v0.5.5 (Fix 2): the section is now framed honestly as a FOUNDATION. The app has
                // almost no translated UI strings yet (no values-tr/ resources), so picking Turkish
                // does not actually translate the interface today. The preference is recorded and
                // ready for a future localization sprint, but we must not imply full translation
                // exists. The "(Foundation)" labels make that explicit.
                Text("App Language (Foundation)", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("This sets a language preference for the interface. Full UI translations are coming later and are not fully available yet — most of the interface stays in English today. This does NOT change generated search strings. Selecting a language never black-screens the app.", color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(Modifier.height(4.dp))
                val appLang = userPrefs?.appLanguage ?: "System Default"
                RadioRow("System Default (Foundation)", appLang == "System Default") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "System Default") } }
                RadioRow("English (Foundation)", appLang == "English") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "English") } }
                RadioRow("Turkish (Foundation — coming later)", appLang == "Turkish") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "Turkish") } }
                Spacer(Modifier.height(10.dp))
                Text(
                    "App Language applies to this app only and is a foundation for future translation. Pokémon GO itself is not affected.",
                    color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f)
                )
                Spacer(Modifier.height(12.dp))

                // v0.5.2 (Fix 7): LAYER B — Search String Language (generated strings only).
                // This controls the language of the text you paste into Pokémon GO. It is
                // independent of App Language: a Turkish UI can still emit safe English
                // strings, and vice-versa. Auto (Safe) stays English (conservative).
                Text("Search String Language", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Controls the language of the generated search strings you copy into Pokémon GO.", color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(Modifier.height(4.dp))
                // v0.5.4 (Fix 5): exactly one Search String Language option may be selected.
                // Previously the Auto predicate also matched "English" (conflating the
                // generation-time invariant "Auto resolves to English" with UI selection
                // state), so choosing English lit both Auto and English. Auto is now selected
                // ONLY when the stored value is "Auto" (or blank/unset). English and Turkish
                // are exact equality, matching the (working) App Language pattern above.
                val searchLang = userPrefs?.gameLanguage ?: "Auto"
                val searchAuto = searchLang == "Auto" || searchLang.isBlank()
                RadioRow("Auto (Safe — English)", searchAuto) { scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, "Auto") } }
                RadioRow("English", searchLang == "English") { scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, "English") } }
                RadioRow("Turkish (Beta — verify before use)", searchLang == "Turkish") { scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, "Turkish") } }
                Spacer(Modifier.height(10.dp))
                // v0.4.2 (Fix 3) / v0.5.2 (Fix 9): Turkish tokens are community-sourced and unverified.
                Text(
                    "Turkish search terms are beta. Please verify results in Pokémon GO before transferring or trading. Auto (Safe) never switches to Turkish automatically.",
                    color = AmberWarning,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

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

                // v0.4.2 (Fix 7): destructive actions require explicit confirmation.
                var pendingDestructive by remember { mutableStateOf<DestructiveAction?>(null) }
                Text("Clear favorites", color = CoralDanger, modifier = Modifier.fillMaxWidth().clickable { pendingDestructive = DestructiveAction.ClearFavorites }.padding(vertical = 8.dp))
                Text("Clear history", color = CoralDanger, modifier = Modifier.fillMaxWidth().clickable { pendingDestructive = DestructiveAction.ClearHistory }.padding(vertical = 8.dp))
                Text("Reset all settings", color = CoralDanger, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clickable { pendingDestructive = DestructiveAction.ResetSettings }.padding(vertical = 8.dp))

                pendingDestructive?.let { action ->
                    AlertDialog(
                        onDismissRequest = { pendingDestructive = null },
                        title = { Text(action.title) },
                        text = { Text(action.message, color = TextSecondary) },
                        confirmButton = {
                            TextButton(onClick = {
                                scope.launch {
                                    when (action) {
                                        DestructiveAction.ClearFavorites -> repository.clearFavorites()
                                        DestructiveAction.ClearHistory -> repository.clearHistory()
                                        DestructiveAction.ResetSettings -> repository.resetSettings()
                                    }
                                }
                                pendingDestructive = null
                            }) { Text("Confirm", color = CoralDanger, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingDestructive = null }) { Text("Cancel", color = TextSecondary) }
                        },
                        containerColor = CardDark
                    )
                }
            }
        }

        item {
            // v0.5.0 Stitch: disabled "Coming Later" section. Explicitly unavailable to
            // maintain trust in the offline-first model. Never active, never networked.
            // v0.5.2 (Fix 10): "AI Assistant" is documented as coming-later and is strictly
            // non-functional — see docs/ai/AI_FEASIBILITY.md + AI_ASSISTANT_ROADMAP.md. The
            // offline-first app remains unchanged.
            PremiumPanel {
                Text("Coming Later", color = TextSecondary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                com.caglar.pokequery.ui.pq.PqComingLaterCard("AI Assistant", "Coming later · Not available yet. The offline-first app remains unchanged.")
                Spacer(Modifier.height(8.dp))
                com.caglar.pokequery.ui.pq.PqComingLaterCard("Cloud Sync", "Offline-first. No cloud sync exists today.")
                Spacer(Modifier.height(8.dp))
                com.caglar.pokequery.ui.pq.PqComingLaterCard("Community Preset Packs", "Shareable preset packs are not available yet.")
                Spacer(Modifier.height(8.dp))
                com.caglar.pokequery.ui.pq.PqComingLaterCard("Import / Export", "Local favorites/history only; no import/export yet.")
                Spacer(Modifier.height(8.dp))
                com.caglar.pokequery.ui.pq.PqComingLaterCard("Automatic Database Updates", "The knowledge base is local and never auto-updates online.")
            }
        }

        item {
            PremiumPanel {
                Text("About", color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(com.caglar.pokequery.AppVersion.aboutDisplayString, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Safe search strings for Pokémon GO", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                // v0.4.2 (Fix 6): non-affiliation disclaimer.
                Text(
                    "PokeQuery is an independent helper app and is not affiliated with, endorsed by, or sponsored by Niantic, The Pokémon Company, or Nintendo.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(12.dp))
                // Package 2: tester feedback via mailto (no network, no analytics).
                // The user reviews and sends manually from their own email app.
                val feedbackContext = com.caglar.pokequery.feedback.FeedbackContext(
                    appVersion = com.caglar.pokequery.AppVersion.versionName,
                    androidVersion = android.os.Build.VERSION.RELEASE ?: "unknown",
                    deviceModel = android.os.Build.MODEL ?: "unknown",
                    gameLanguage = userPrefs?.gameLanguage ?: "English"
                )
                Text(
                    "Send tester feedback",
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().clickable {
                        val mailto = com.caglar.pokequery.feedback.FeedbackBuilder.buildMailtoUri(feedbackContext)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse(mailto))
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "No email app found. Please email caglar@caglardinc.com manually.", Toast.LENGTH_LONG).show()
                        }
                    }.padding(vertical = 8.dp)
                )
                Text("Opens your email app with a pre-filled template. Nothing is sent automatically.", color = TextSecondary, fontSize = 11.sp)
            }
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
                // Package 8: compact verification/safety/language badges.
                KbBadges(term)
            }
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(top = 12.dp)) {
                Text(term.descriptionEn, color = TextPrimary, fontSize = 14.sp)
                // Package 8: example + common-mistake when present.
                term.example?.let {
                    Text("Example: $it", color = TealPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 8.dp))
                }
                term.commonMistake?.let {
                    Text("Common mistake: $it", color = AmberWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
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

// Package 8: compact, dark-mode-readable badges. Verified=teal, Beta=amber, Needs verification=secondary.
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun KbBadges(term: Term) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val status = term.verificationStatus
        val (statusLabel, statusColor) = when (status) {
            com.caglar.pokequery.data.model.VerificationStatus.VERIFIED -> "Verified" to TealPrimary
            com.caglar.pokequery.data.model.VerificationStatus.BETA -> "Beta" to AmberWarning
            com.caglar.pokequery.data.model.VerificationStatus.NEEDS_VERIFICATION -> "Needs verification" to TextSecondary
        }
        KbBadge(statusLabel, statusColor)
        if (status == com.caglar.pokequery.data.model.VerificationStatus.BETA ||
            term.safetyLevel?.lowercase() == "risky") {
            KbBadge("Risky", CoralDanger)
        }
        if (term.languageSensitive == true) {
            KbBadge("Language-sensitive", BlueCTA)
        }
    }
}

@Composable
private fun KbBadge(label: String, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    // v0.5.3 motion polish: staggered entrance — title bar fades in first. Empty-state icon gets
    // a subtle spring-pop. List rows appear at rest (no cascade while scrolling).
    // v0.5.5 (Fix 1): the gap between saved-template rows follows the Visual Density `listGap`
    // token, so Compact fits more saved strings per screen.
    val density = currentDensity()
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(density.listGap),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { ScreenTitleBar(title, onBack, Modifier.pqStaggeredItem(visible, 0)) }
        when {
            templates == null -> item { CircularProgressIndicator(color = TealPrimary, modifier = Modifier.padding(20.dp)) }
            templates.isEmpty() -> item {
                androidx.compose.foundation.layout.Box(
                    Modifier.pqStaggeredItem(visible, 1).pqSpringPop(visible)
                ) {
                    com.caglar.pokequery.ui.pq.PqEmptyState(
                        icon = androidx.compose.material.icons.Icons.Default.Star,
                        title = emptyTitle,
                        subtitle = emptySubtitle
                    )
                }
            }
            else -> items(templates, key = { it.id }) { template ->
                SavedTemplateRow(template, onCopy = { onCopy(template) }, onDelete = onDelete?.let { { it(template) } })
            }
        }
    }
    }
}

@Composable
private fun SavedTemplateRow(template: SavedTemplate, onCopy: () -> Unit, onDelete: (() -> Unit)? = null) {
    // v0.5.5 (Fix 1): the inner element gaps (title → string box → copy button) follow the
    // Visual Density `innerElementGap` token so Compact tightens the saved-string cards.
    val density = currentDensity()
    PremiumPanel(borderColor = template.riskLevel.toneColor()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(template.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(template.goalId, color = TextSecondary, fontSize = 12.sp)
            }
            com.caglar.pokequery.ui.pq.PqRiskBadge(template.riskLevel)
        }
        Spacer(Modifier.height(density.innerElementGap))
        com.caglar.pokequery.ui.pq.PqStringBox(template.rawSyntax)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            com.caglar.pokequery.ui.pq.PqPrimaryButton(
                text = if (onDelete == null) "Copy again" else "Copy",
                onClick = onCopy,
                leadingIcon = Icons.Default.ContentCopy
            )
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

// v0.4.2 (Fix 7): models the pending destructive Settings action awaiting confirmation.
private enum class DestructiveAction(val title: String, val message: String) {
    ClearFavorites("Clear favorites?", "This removes all saved search strings. This cannot be undone."),
    ClearHistory("Clear history?", "This removes all recently copied search strings. This cannot be undone."),
    ResetSettings("Reset all settings?", "This restores language, copy, and threshold settings to defaults. Favorites and history are kept. This cannot be undone.")
}
