package com.caglar.pokequery

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.SavedTemplate
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import com.caglar.pokequery.growth.GrowthPromptStore
import com.caglar.pokequery.ui.components.BottomNavBar
import com.caglar.pokequery.ui.motion.PqMotionTokens
import com.caglar.pokequery.ui.motion.ProvidePqMotion
import com.caglar.pokequery.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun MainNavigation(
    startRoute: String? = null,
    copySearch: String? = null,
    debugEventFeedUrl: String? = null,
    navigationIntentVersion: Int = 0,
    onCopyHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val growthPromptStore = remember { GrowthPromptStore(context) }
    var showRatePrompt by remember { mutableStateOf(false) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val initialEntry = remember(startRoute, userPrefs) {
        startDestination(startRoute, userPrefs?.firstUseSeen)
    } ?: return

    val shareHeading = stringResource(R.string.growth_share_heading)
    val shareBuiltWith = stringResource(R.string.growth_share_built_with)
    val shareChooserTitle = stringResource(R.string.growth_share_search)
    val copiedToClipboard = stringResource(R.string.goal_detail_copied)
    val assistantExplanation = stringResource(R.string.search_assistant_generated_explanation)

    val backStack = rememberNavBackStack(initialEntry)
    var currentTab by remember { mutableStateOf(tabForStartRoute(startRoute)) }

    fun recordSuccessfulCopy() {
        if (growthPromptStore.recordSuccessfulCopyAndShouldPrompt()) {
            growthPromptStore.markPromptShown()
            showRatePrompt = true
        }
    }

    fun openPlayStoreRating() {
        val packageName = context.packageName
        val marketIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("market://details?id=$packageName")
        ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        val webIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

        val opened = runCatching {
            if (marketIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(marketIntent)
            } else {
                context.startActivity(webIntent)
            }
        }.isSuccess
        if (opened) growthPromptStore.markRatingOpened()
        showRatePrompt = false
    }

    fun shareGenerated(generated: GeneratedString) {
        val shareText = buildString {
            append(shareHeading)
            append('\n')
            append(generated.rawSyntax)
            append("\n\n")
            append(shareBuiltWith)
            append('\n')
            append("https://play.google.com/store/apps/details?id=${context.packageName}")
        }
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        runCatching {
            context.startActivity(android.content.Intent.createChooser(shareIntent, shareChooserTitle))
        }
    }

    LaunchedEffect(startRoute, navigationIntentVersion, userPrefs?.firstUseSeen) {
        if (navigationIntentVersion > 0 && startRoute != null) {
            val destination = startDestination(startRoute, userPrefs?.firstUseSeen) ?: return@LaunchedEffect
            currentTab = tabForStartRoute(startRoute)
            backStack.clear()
            backStack.add(destination)
        }
    }

    LaunchedEffect(startRoute, copySearch) {
        if (!copySearch.isNullOrBlank()) {
            clipboard.setText(AnnotatedString(copySearch))
            val explanation = when (startRoute) {
                "detail_safe_cleanup" -> "Safe Cleanup search string copied from widget"
                "detail_candy_prep" -> "Candy Prep search string copied from widget"
                "events" -> "Event Guide search string copied from widget"
                else -> "Search string copied from widget"
            }
            scope.launch {
                repository.addHistory(
                    SavedTemplate.from(
                        GeneratedString(
                            rawSyntax = copySearch,
                            plainLanguageExplanation = explanation,
                            protectedCategories = emptyList(),
                            includedHighRiskCategories = emptyList(),
                            riskLevel = RiskLevel.Low
                        )
                    )
                )
            }
            android.widget.Toast.makeText(context, copiedToClipboard, android.widget.Toast.LENGTH_SHORT).show()
            recordSuccessfulCopy()
            onCopyHandled()
        }
    }

    ProvidePqMotion {
        fun copyGenerated(generated: GeneratedString) {
            clipboard.setText(AnnotatedString(generated.rawSyntax))
            scope.launch { repository.addHistory(SavedTemplate.from(generated)) }
            android.widget.Toast.makeText(context, copiedToClipboard, android.widget.Toast.LENGTH_SHORT).show()
            recordSuccessfulCopy()
        }

        fun requestShare(generated: GeneratedString) {
            if (requiresRiskWarning(generated.riskLevel)) {
                backStack.add(RiskWarning(generated, RiskAction.Share))
            } else {
                shareGenerated(generated)
            }
        }

        val safePop: () -> Unit = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            } else if (backStack.lastOrNull() != Home) {
                currentTab = "builder"
                backStack.clear()
                backStack.add(Home)
            }
        }

        Scaffold(
            bottomBar = {
                if (backStack.lastOrNull() !is Onboarding) {
                    BottomNavBar(currentRoute = currentTab) { route ->
                        bottomTabDestination(route)?.let { destination ->
                            currentTab = route
                            backStack.clear()
                            backStack.add(destination)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { safePop() },
                    transitionSpec = { fadeIn(tween(PqMotionTokens.SCREEN_CROSSFADE_MS)) togetherWith fadeOut(tween(PqMotionTokens.CROSSFADE_FADE_MS)) },
                    popTransitionSpec = { fadeIn(tween(PqMotionTokens.SCREEN_CROSSFADE_MS)) togetherWith fadeOut(tween(PqMotionTokens.CROSSFADE_FADE_MS)) },
                    predictivePopTransitionSpec = { _ -> fadeIn(tween(PqMotionTokens.SCREEN_CROSSFADE_MS)) togetherWith fadeOut(tween(PqMotionTokens.CROSSFADE_FADE_MS)) },
                    entryProvider = entryProvider {
                        entry<Onboarding> { route ->
                            val onboardingScope = rememberCoroutineScope()
                            OnboardingScreen(initialPage = route.initialPage) {
                                onboardingScope.launch { repository.setFirstUseSeen(true) }
                                backStack.clear()
                                backStack.add(Home)
                            }
                        }
                        entry<Home> {
                            HomeScreen { goalId -> backStack.add(homeGoalDestination(goalId)) }
                        }
                        entry<GoalDetail> { route ->
                            GoalDetailScreen(
                                goalId = route.goalId,
                                onBack = { safePop() },
                                onNavigateRisk = { generatedString ->
                                    backStack.add(RiskWarning(generatedString, RiskAction.Copy))
                                },
                                onShare = ::requestShare,
                                onCopyCompleted = ::recordSuccessfulCopy
                            )
                        }
                        entry<ExpertBuilder> {
                            val language = userPrefs?.gameLanguage ?: "English"
                            ExpertBuilderScreen(
                                onGenerate = { query ->
                                    val generated = StringBuilderEngine.buildGoal("expert", customQuery = query, language = language)
                                    if (requiresRiskWarning(generated.riskLevel)) {
                                        backStack.add(RiskWarning(generated, RiskAction.Copy))
                                    } else {
                                        copyGenerated(generated)
                                    }
                                },
                                onBack = { safePop() }
                            )
                        }
                        entry<Presets> {
                            PresetsScreen(
                                onBack = { safePop() },
                                onCopy = ::copyGenerated,
                                onNavigateRisk = { generatedString -> backStack.add(RiskWarning(generatedString, RiskAction.Copy)) }
                            )
                        }
                        entry<MyPresets> {
                            MyPresetsScreen(
                                onBack = { safePop() },
                                onCopy = ::copyGenerated,
                                onNavigateRisk = { generatedString -> backStack.add(RiskWarning(generatedString, RiskAction.Copy)) }
                            )
                        }
                        entry<PracticeMode> { PracticeModeScreen(onBack = { safePop() }) }
                        entry<CleaningJournal> { CleaningJournalScreen(onBack = { safePop() }) }
                        entry<EventContext> {
                            EventContextScreen(onBack = { safePop() }, debugEventFeedUrl = debugEventFeedUrl)
                        }
                        entry<SearchAssistant> {
                            SearchAssistantScreen(
                                onBack = { safePop() },
                                onCopyRaw = { rawSyntax, riskLevel ->
                                    val generated = GeneratedString(
                                        rawSyntax = rawSyntax,
                                        plainLanguageExplanation = assistantExplanation,
                                        protectedCategories = emptyList(),
                                        includedHighRiskCategories = emptyList(),
                                        riskLevel = riskLevel
                                    )
                                    if (requiresRiskWarning(riskLevel)) {
                                        backStack.add(RiskWarning(generated, RiskAction.Copy))
                                    } else {
                                        copyGenerated(generated)
                                    }
                                },
                                onExplain = { query -> backStack.add(ExplainRoute(query)) }
                            )
                        }
                        entry<ExplainRoute> { route ->
                            ExplainScreen(onBack = { safePop() }, initialQuery = route.query)
                        }
                        entry<Favorites> {
                            FavoritesScreen(
                                onCopy = { favorite ->
                                    if (requiresRiskWarning(favorite.riskLevel)) {
                                        backStack.add(RiskWarning(favorite.asGeneratedString(), RiskAction.Copy))
                                    } else {
                                        copyGenerated(favorite.asGeneratedString())
                                    }
                                },
                                onBack = { safePop() }
                            )
                        }
                        entry<History> {
                            HistoryScreen(
                                onCopy = { history ->
                                    if (requiresRiskWarning(history.riskLevel)) {
                                        backStack.add(RiskWarning(history.asGeneratedString(), RiskAction.Copy))
                                    } else {
                                        copyGenerated(history.asGeneratedString())
                                    }
                                },
                                onBack = { safePop() }
                            )
                        }
                        entry<Settings> {
                            SettingsScreen(onBack = { safePop() }, onOpenChangelog = { backStack.add(ChangelogRoute) })
                        }
                        entry<ChangelogRoute> { ChangelogScreen { safePop() } }
                        entry<KnowledgeBase> { route -> KnowledgeBaseScreen(startExpanded = route.startExpanded) { safePop() } }
                        entry<RiskWarning> { route ->
                            RiskWarningScreen(
                                generatedString = route.generatedString,
                                action = route.action,
                                onConfirmAction = {
                                    if (route.action == RiskAction.Share) {
                                        shareGenerated(route.generatedString)
                                    } else {
                                        copyGenerated(route.generatedString)
                                    }
                                    safePop()
                                },
                                onBack = { safePop() }
                            )
                        }
                    }
                )
            }
        }

        if (showRatePrompt) {
            AlertDialog(
                onDismissRequest = { showRatePrompt = false },
                title = { Text(stringResource(R.string.growth_rate_title)) },
                text = { Text(stringResource(R.string.growth_rate_body)) },
                confirmButton = {
                    TextButton(onClick = ::openPlayStoreRating) {
                        Text(stringResource(R.string.growth_rate_action))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRatePrompt = false }) {
                        Text(stringResource(R.string.growth_rate_later))
                    }
                }
            )
        }
    }
}
