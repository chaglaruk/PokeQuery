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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.caglar.pokequery.domain.changelog.Changelog
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
                        (it.syntax.contains(searchQuery, true) || it.title?.contains(searchQuery, true) == true || it.descriptionEn.contains(searchQuery, true) || it.category.contains(searchQuery, true))
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
        onDelete = { scope.launch { repository.removeFavorite(it.id) } },
        // v0.6.1: Favorites -> Personal Presets bridge. Converts a favorite into a LOCAL personal
        // preset (risk level preserved, never downgraded). LOCAL ONLY — never synced/uploaded.
        onSaveAsPreset = { template ->
            scope.launch {
                repository.addPersonalPreset(com.caglar.pokequery.data.model.PersonalPreset.fromFavorite(template))
                Toast.makeText(context, "Saved to My Presets", Toast.LENGTH_SHORT).show()
            }
        }
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
fun SettingsScreen(onBack: () -> Unit, onOpenChangelog: () -> Unit = {}) {
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
        item { ScreenTitleBar(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_title), onBack, Modifier.pqStaggeredItem(visible, 0)) }

        item {
            PremiumPanel {
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_general), color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_first_use_guide), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_first_use_guide_desc), color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = userPrefs?.firstUseSeen ?: false,
                        onCheckedChange = { scope.launch { repository.setFirstUseSeen(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                    )
                }
                Spacer(Modifier.height(14.dp))
                com.caglar.pokequery.ui.pq.PqComingLaterCard(
                    title = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_online_events),
                    description = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_online_events_desc)
                )
            }
        }

        item {
            PremiumPanel(borderColor = TealPrimary) {
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_search_language), color = TealPrimary, fontWeight = FontWeight.Bold)
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
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_app_language), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_app_language_desc), color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(Modifier.height(4.dp))
                val appLang = userPrefs?.appLanguage ?: "System Default"
                RadioRow("System Default", appLang == "System Default") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "System Default") } }
                RadioRow("English", appLang == "English") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "English") } }
                RadioRow("Deutsch", appLang == "Deutsch") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "Deutsch") } }
                RadioRow("Español", appLang == "Español") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "Español") } }
                RadioRow("Français", appLang == "Français") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "Français") } }
                RadioRow("Italiano", appLang == "Italiano") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "Italiano") } }
                RadioRow("Türkçe", appLang == "Turkish" || appLang == "Türkçe") { scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, "Türkçe") } }
                Spacer(Modifier.height(10.dp))
                Text(
                    androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_app_language_footnote),
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
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_search_string_lang), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_search_string_lang_desc), color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(Modifier.height(4.dp))
                // v0.5.4 (Fix 5): exactly one Search String Language option may be selected.
                // Previously the Auto predicate also matched "English" (conflating the
                // generation-time invariant "Auto resolves to English" with UI selection
                // state), so choosing English lit both Auto and English. Auto is now selected
                // ONLY when the stored value is "Auto" (or blank/unset). English and Turkish
                // are exact equality, matching the (working) App Language pattern above.
                val searchLang = userPrefs?.gameLanguage ?: "Auto"
                val searchAuto = searchLang == "Auto" || searchLang.isBlank()
                RadioRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_search_lang_auto), searchAuto) { scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, "Auto") } }
                RadioRow("English", searchLang == "English") { scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, "English") } }
                RadioRow(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_search_lang_turkish), searchLang == "Turkish") { scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, "Turkish") } }
                Spacer(Modifier.height(10.dp))
                // v0.4.2 (Fix 3) / v0.5.2 (Fix 9): Turkish tokens are community-sourced and unverified.
                Text(
                    androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_search_lang_beta_warning),
                    color = AmberWarning,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_clipboard_detect), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_clipboard_detect_desc), color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = userPrefs?.clipboardDetectionEnabled ?: true,
                        onCheckedChange = { scope.launch { repository.setClipboardDetectionEnabled(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_safety), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_safety_desc1), color = TextSecondary, fontSize = 13.sp)
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_safety_desc2), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        item {
            PremiumPanel {
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_about_privacy), color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(com.caglar.pokequery.AppVersion.aboutDisplayString, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_about_desc1), color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_changelog_label),
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenChangelog).padding(vertical = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_about_desc2), color = TextPrimary)
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_about_desc3), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(12.dp))
                // v0.4.2 (Fix 6): non-affiliation disclaimer.
                Text(
                    androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_disclaimer),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(12.dp))
                // v0.4.2 (Fix 7): destructive data actions require explicit confirmation.
                var pendingDestructive by remember { mutableStateOf<DestructiveAction?>(null) }
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_clear_fav), color = CoralDanger, modifier = Modifier.fillMaxWidth().clickable { pendingDestructive = DestructiveAction.ClearFavorites }.padding(vertical = 8.dp))
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_clear_hist), color = CoralDanger, modifier = Modifier.fillMaxWidth().clickable { pendingDestructive = DestructiveAction.ClearHistory }.padding(vertical = 8.dp))
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_reset_all), color = CoralDanger, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clickable { pendingDestructive = DestructiveAction.ResetSettings }.padding(vertical = 8.dp))

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
                            }) { Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_confirm), color = CoralDanger, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingDestructive = null }) { Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_cancel), color = TextSecondary) }
                        },
                        containerColor = CardDark
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.08f)
                )
                Spacer(Modifier.height(8.dp))
                // Tester feedback via mailto (no network, no analytics).
                val feedbackContext = com.caglar.pokequery.feedback.FeedbackContext(
                    appVersion = com.caglar.pokequery.AppVersion.versionName,
                    androidVersion = android.os.Build.VERSION.RELEASE ?: "unknown",
                    deviceModel = android.os.Build.MODEL ?: "unknown",
                    gameLanguage = userPrefs?.gameLanguage ?: "English"
                )
                Text(
                    androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_feedback),
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().clickable {
                        val mailto = com.caglar.pokequery.feedback.FeedbackBuilder.buildMailtoUri(feedbackContext)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse(mailto))
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, context.getString(com.caglar.pokequery.R.string.settings_email_fallback), Toast.LENGTH_LONG).show()
                        }
                    }.padding(vertical = 8.dp)
                )
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_email_desc), color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
    }
}

@Composable
fun ChangelogScreen(onBack: () -> Unit) {
    val density = currentDensity()
    val isTurkishUi = LocalConfiguration.current.locales[0]?.language == "tr"
    val entries = if (isTurkishUi) Changelog.entries.filter { it.isCurrent } else Changelog.entries
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(density.listGap),
        contentPadding = PaddingValues(bottom = 22.dp)
    ) {
        item { ScreenTitleBar(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_changelog), onBack) }
        item {
            PremiumPanel(borderColor = TealPrimary) {
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_safety_stance), color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_safety_disclaimer),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        items(entries, key = { it.versionName }) { entry ->
            PremiumPanel(borderColor = if (entry.isCurrent) TealPrimary else BorderDark) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("v${entry.versionName} (${entry.versionCode})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        if (entry.isCurrent) {
                            Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_subtitle), color = TextSecondary, fontSize = 12.sp)
                        } else {
                            Text("${entry.releaseLabel} \u2022 ${entry.title}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    if (entry.isCurrent) {
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.changelog_current), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(density.innerElementGap))
                if (entry.isCurrent) {
                    Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_b1), color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_b2), color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_b3), color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                } else {
                    entry.highlights.forEach { Text("\u2022 $it", color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp) }
                }
                if (entry.safetyNotes.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.changelog_safety_notes), color = AmberWarning, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    val safetyNotes = if (entry.isCurrent) listOf(
                        androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_safety1),
                        androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_safety2),
                        androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_safety3)
                    ) else entry.safetyNotes
                    safetyNotes.forEach { Text("\u2022 $it", color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp) }
                }
                if (entry.testerNotes.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.changelog_tester_notes), color = TealPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    val testerNotes = if (entry.isCurrent) listOf(
                        androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_tester1),
                        androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_tester2),
                        androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.what_changed_v066_tester3)
                    ) else entry.testerNotes
                    testerNotes.forEach { Text("\u2022 $it", color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp) }
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
                Text(term.title ?: term.syntax, color = term.riskLevel.toneColor(), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (term.title != null) {
                    Text(term.syntax, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
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
    onDelete: ((SavedTemplate) -> Unit)? = null,
    onSaveAsPreset: ((SavedTemplate) -> Unit)? = null
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
                SavedTemplateRow(
                    template,
                    onCopy = { onCopy(template) },
                    onDelete = onDelete?.let { { it(template) } },
                    onSaveAsPreset = onSaveAsPreset?.let { { it(template) } }
                )
            }
        }
    }
    }
}

@Composable
private fun SavedTemplateRow(
    template: SavedTemplate,
    onCopy: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSaveAsPreset: (() -> Unit)? = null
) {
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
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            com.caglar.pokequery.ui.pq.PqPrimaryButton(
                text = if (onDelete == null) "Copy again" else "Copy",
                onClick = onCopy,
                leadingIcon = Icons.Default.ContentCopy,
                modifier = Modifier.weight(1f)
            )
            if (onSaveAsPreset != null) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onSaveAsPreset) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Save as preset",
                        tint = TealPrimary
                    )
                }
            }
            if (onDelete != null) {
                Spacer(Modifier.width(4.dp))
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
    ResetSettings("Reset all settings?", "This restores language settings to defaults. Favorites and history are kept. This cannot be undone.")
}
