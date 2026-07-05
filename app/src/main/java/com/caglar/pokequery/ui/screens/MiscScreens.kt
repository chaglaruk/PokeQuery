package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.data.model.SavedTemplate
import com.caglar.pokequery.data.model.Term
import com.caglar.pokequery.data.repository.KnowledgeBaseRepository
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.changelog.Changelog
import com.caglar.pokequery.domain.locale.AppLocaleController
import com.caglar.pokequery.domain.locale.LocalizationModel
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.clearFocusOnTap
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
    val knowledgeCopied = stringResource(R.string.knowledge_copied)

    LaunchedEffect(Unit) { result = repository.load() }

    // v0.5.3 motion polish: staggered entrance — title bar + banner fade in first; list rows
    // appear at rest (no cascade while scrolling). One hoisted flag → runs once only.
    // v0.5.5 (Fix 1): the gap between homogeneous KB term rows follows the Visual Density
    // `listGap` token, so Compact tightens the list visibly.
    val density = currentDensity()
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).clearFocusOnTap().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(density.listGap),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { ScreenTitleBar(stringResource(R.string.knowledge_title), onBack, Modifier.pqStaggeredItem(visible, 0)) }
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
                    stringResource(R.string.knowledge_beta_warning),
                    color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp
                )
            }
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.knowledge_search_placeholder), color = TextSecondary) },
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
            terms == null -> item { Text(stringResource(R.string.knowledge_load_error), color = CoralDanger) }
            else -> {
                val categories = listOf("All") + terms.map { it.category }.distinct().sorted()
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(localizedKnowledgeCategory(cat)) },
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
                            title = stringResource(R.string.knowledge_empty_title),
                            subtitle = stringResource(R.string.knowledge_empty_subtitle)
                        )
                    }
                } else {
                    items(filtered, key = { it.id }) { term ->
                        var expanded by remember { mutableStateOf(startExpanded && term == filtered.firstOrNull()) }
                        KnowledgeTermRow(term, expanded, onToggle = { expanded = !expanded }) {
                            clipboard.setText(AnnotatedString(term.syntax))
                            Toast.makeText(context, knowledgeCopied, Toast.LENGTH_SHORT).show()
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
    val savedToPresets = stringResource(R.string.favorites_saved_to_presets)

    SavedTemplateScreen(
        title = stringResource(R.string.nav_favorites),
        templates = userPrefs?.favorites,
        emptyTitle = stringResource(R.string.favorites_empty_title),
        emptySubtitle = stringResource(R.string.favorites_empty_subtitle),
        onBack = onBack,
        onCopy = onCopy,
        onDelete = { scope.launch { repository.removeFavorite(it.id) } },
        // v0.6.1: Favorites -> Personal Presets bridge. Converts a favorite into a LOCAL personal
        // preset (risk level preserved, never downgraded). LOCAL ONLY — never synced/uploaded.
        onSaveAsPreset = { template ->
            scope.launch {
                repository.addPersonalPreset(com.caglar.pokequery.data.model.PersonalPreset.fromFavorite(template))
                Toast.makeText(context, savedToPresets, Toast.LENGTH_SHORT).show()
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
        title = stringResource(R.string.nav_history),
        templates = userPrefs?.history,
        emptyTitle = stringResource(R.string.history_empty_title),
        emptySubtitle = stringResource(R.string.history_empty_subtitle),
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
    val emailFallback = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.settings_email_fallback)

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
                Spacer(Modifier.height(14.dp))
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
                val appLang = userPrefs?.appLanguage ?: AppLocaleController.SYSTEM_DEFAULT
                var showAppLanguageDialog by remember { mutableStateOf(false) }
                val appLangLabel = if (appLang == AppLocaleController.SYSTEM_DEFAULT) {
                    localizedSystemDefaultLabel(appLang)
                } else {
                    appLang
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDark)
                        .border(1.dp, TealPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { showAppLanguageDialog = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_change_language), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TealPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = appLangLabel, color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (showAppLanguageDialog) {
                    AlertDialog(
                        onDismissRequest = { showAppLanguageDialog = false },
                        title = { Text(localizedChooseLanguageLabel(appLang)) },
                        text = {
                            Column {
                                AppLocaleController.OPTIONS.forEach { option ->
                                    val label = if (option == AppLocaleController.SYSTEM_DEFAULT) localizedSystemDefaultLabel(appLang) else option
                                    RadioRow(label, appLang == option) {
                                        scope.launch { repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, option) }
                                        showAppLanguageDialog = false
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        containerColor = CardDark
                    )
                }
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
                val searchLang = userPrefs?.gameLanguage ?: LocalizationModel.SearchStringLanguage.DEFAULT
                var showSearchLanguageDialog by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDark)
                        .border(1.dp, TealPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { showSearchLanguageDialog = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_change_language), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TealPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = localizedSearchLanguageLabel(searchLang, appLang), color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (showSearchLanguageDialog) {
                    AlertDialog(
                        onDismissRequest = { showSearchLanguageDialog = false },
                        title = { Text(localizedSearchStringLanguageTitle(appLang)) },
                        text = {
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_search_lang_beta_warning),
                                    color = AmberWarning,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                LocalizationModel.SearchStringLanguage.OPTIONS.forEach { option ->
                                    RadioRow(localizedSearchLanguageLabel(option, appLang), searchLang == option || (searchLang.isBlank() && option == LocalizationModel.SearchStringLanguage.DEFAULT)) {
                                        scope.launch { repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, option) }
                                        showSearchLanguageDialog = false
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        containerColor = CardDark
                    )
                }
                Spacer(Modifier.height(10.dp))
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
                        title = { Text(stringResource(action.titleRes)) },
                        text = { Text(stringResource(action.messageRes), color = TextSecondary) },
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
                            Toast.makeText(context, emailFallback, Toast.LENGTH_LONG).show()
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
    val isTurkishUi = LocalConfiguration.current.locales[0]?.language == "tr"
    val description = if (isTurkishUi && term.descriptionTr.isNotBlank()) term.descriptionTr else term.descriptionEn
    PremiumPanel(borderColor = term.riskLevel.toneColor()) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onToggle), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(term.title ?: term.syntax, color = term.riskLevel.toneColor(), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (term.title != null) {
                    Text(term.syntax, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Text(stringResource(R.string.knowledge_tier_risk, localizedKnowledgeCategory(term.category), term.tier, localizedRiskLabel(term.riskLevel)), color = TextSecondary, fontSize = 12.sp)
                // Package 8: compact verification/safety/language badges.
                KbBadges(term)
            }
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(top = 12.dp)) {
                Text(description, color = TextPrimary, fontSize = 14.sp)
                // Package 8: example + common-mistake when present.
                term.example?.let {
                    Text(stringResource(R.string.knowledge_example, it), color = TealPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 8.dp))
                }
                if (!isTurkishUi) term.commonMistake?.let {
                    Text(stringResource(R.string.knowledge_common_mistake, it), color = AmberWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
                if (!isTurkishUi && !term.knownQuirks.isNullOrEmpty()) {
                    Text(stringResource(R.string.knowledge_note, term.knownQuirks), color = AmberWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
                Text(stringResource(R.string.knowledge_source, term.sourceUrl), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                Text(stringResource(R.string.knowledge_last_verified, term.lastVerified), color = TextSecondary, fontSize = 11.sp)
                OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.knowledge_copy_token))
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
            com.caglar.pokequery.data.model.VerificationStatus.VERIFIED -> stringResource(R.string.badge_verified) to TealPrimary
            com.caglar.pokequery.data.model.VerificationStatus.BETA -> stringResource(R.string.badge_beta) to AmberWarning
            com.caglar.pokequery.data.model.VerificationStatus.NEEDS_VERIFICATION -> stringResource(R.string.badge_needs_verification) to TextSecondary
        }
        KbBadge(statusLabel, statusColor)
        if (status == com.caglar.pokequery.data.model.VerificationStatus.BETA ||
            term.safetyLevel?.lowercase() == "risky") {
            KbBadge(stringResource(R.string.badge_risky), CoralDanger)
        }
        if (term.languageSensitive == true) {
            KbBadge(stringResource(R.string.badge_language_sensitive), BlueCTA)
        }
    }
}

@Composable
private fun localizedKnowledgeCategory(category: String): String = when (category.trim().lowercase()) {
    "all" -> stringResource(R.string.knowledge_all)
    "common misconception", "common misconceptions", "common mistake" -> stringResource(R.string.kb_cat_common_mistake)
    "counter" -> stringResource(R.string.kb_cat_counter)
    "encounter" -> stringResource(R.string.kb_cat_encounter)
    "evolution" -> stringResource(R.string.kb_cat_evolution)
    "iv" -> stringResource(R.string.kb_cat_iv)
    "max" -> stringResource(R.string.kb_cat_max)
    "move", "moves" -> stringResource(R.string.kb_cat_move)
    "numeric" -> stringResource(R.string.kb_cat_numeric)
    "operator", "operators" -> stringResource(R.string.kb_cat_operator)
    "size" -> stringResource(R.string.kb_cat_size)
    "status" -> stringResource(R.string.kb_cat_status)
    "tag", "tags" -> stringResource(R.string.kb_cat_tag)
    else -> category
}

@Composable
private fun localizedRiskLabel(riskLevel: com.caglar.pokequery.data.model.RiskLevel): String = when (riskLevel) {
    com.caglar.pokequery.data.model.RiskLevel.Info -> stringResource(R.string.risk_info)
    com.caglar.pokequery.data.model.RiskLevel.Low -> stringResource(R.string.risk_low)
    com.caglar.pokequery.data.model.RiskLevel.Medium -> stringResource(R.string.risk_medium)
    com.caglar.pokequery.data.model.RiskLevel.High -> stringResource(R.string.risk_high)
}

private fun localizedChooseLanguageLabel(appLang: String): String = when (AppLocaleController.resolvedLocaleTagFor(appLang)) {
    "tr" -> "Dil Seçin"
    "de" -> "Sprache wählen"
    "es" -> "Elegir idioma"
    "fr" -> "Choisir la langue"
    "it" -> "Scegli la lingua"
    else -> "Choose Language"
}

private fun localizedSystemDefaultLabel(appLang: String): String = when (AppLocaleController.resolvedLocaleTagFor(appLang)) {
    "tr" -> "Sistem varsayılanı"
    "de" -> "Systemstandard"
    "es" -> "Predeterminado del sistema"
    "fr" -> "Paramètre système"
    "it" -> "Predefinito di sistema"
    else -> "System Default"
}

private fun localizedSearchStringLanguageTitle(appLang: String): String = when (AppLocaleController.resolvedLocaleTagFor(appLang)) {
    "tr" -> "Pokémon GO Arama Dili"
    "de" -> "Suchstring-Sprache"
    "es" -> "Idioma de cadena de búsqueda"
    "fr" -> "Langue des chaînes de recherche"
    "it" -> "Lingua delle stringhe di ricerca"
    else -> "Search String Language"
}

private fun localizedSearchLanguageLabel(option: String, appLang: String): String = when (option) {
    LocalizationModel.SearchStringLanguage.AUTO_SAFE -> when (AppLocaleController.resolvedLocaleTagFor(appLang)) {
        "tr" -> "Otomatik (Güvenli)"
        "de" -> "Auto (Sicher)"
        "es" -> "Auto (Seguro)"
        "fr" -> "Auto (Sécurisé)"
        "it" -> "Auto (Sicuro)"
        else -> "Auto"
    }
    LocalizationModel.SearchStringLanguage.MATCH_APP -> when (AppLocaleController.resolvedLocaleTagFor(appLang)) {
        "tr" -> "Uygulama Diliyle Eşleştir"
        "de" -> "App-Sprache übernehmen"
        "es" -> "Coincidir con idioma de la app"
        "fr" -> "Suivre la langue de l’appli"
        "it" -> "Usa la lingua dell’app"
        else -> "Match App Language"
    }
    LocalizationModel.SearchStringLanguage.ENGLISH -> "English"
    LocalizationModel.SearchStringLanguage.GERMAN -> "Deutsch"
    LocalizationModel.SearchStringLanguage.SPANISH -> "Español"
    LocalizationModel.SearchStringLanguage.FRENCH -> "Français"
    LocalizationModel.SearchStringLanguage.ITALIAN -> "Italiano"
    LocalizationModel.SearchStringLanguage.TURKISH -> "Türkçe"
    else -> option.ifBlank { localizedSearchLanguageLabel(LocalizationModel.SearchStringLanguage.AUTO_SAFE, appLang) }
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
                text = if (onDelete == null) stringResource(R.string.saved_template_copy_again) else stringResource(R.string.saved_template_copy),
                onClick = onCopy,
                leadingIcon = Icons.Default.ContentCopy,
                modifier = Modifier.weight(1f)
            )
            if (onSaveAsPreset != null) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onSaveAsPreset) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = stringResource(R.string.saved_template_save_preset),
                        tint = TealPrimary
                    )
                }
            }
            if (onDelete != null) {
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = CoralDanger) }
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

@Composable
private fun SettingsSwitchRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BlueCTA)
        )
    }
}

// v0.4.2 (Fix 7): models the pending destructive Settings action awaiting confirmation.
private enum class DestructiveAction(@StringRes val titleRes: Int, @StringRes val messageRes: Int) {
    ClearFavorites(R.string.destructive_clear_fav_title, R.string.destructive_clear_fav_msg),
    ClearHistory(R.string.destructive_clear_hist_title, R.string.destructive_clear_hist_msg),
    ResetSettings(R.string.destructive_reset_title, R.string.destructive_reset_msg)
}
