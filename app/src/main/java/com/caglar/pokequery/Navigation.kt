package com.caglar.pokequery

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.SavedTemplate
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.engine.StringBuilderEngine

import com.caglar.pokequery.ui.components.BottomNavBar
import com.caglar.pokequery.ui.motion.PqMotionTokens
import com.caglar.pokequery.ui.motion.ProvidePqMotion
import com.caglar.pokequery.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun MainNavigation(startRoute: String? = null) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val initialEntry = remember(startRoute, userPrefs) {
        startDestination(startRoute, userPrefs?.firstUseSeen)
    } ?: return

    val backStack = rememberNavBackStack(initialEntry)
    var currentTab by remember { mutableStateOf(tabForStartRoute(startRoute)) }

    // v0.5.3 motion polish: provide the resolved reduced-motion state to the whole UI.
    ProvidePqMotion {

    fun copyGenerated(generated: GeneratedString) {
        clipboard.setText(AnnotatedString(generated.rawSyntax))
        scope.launch { repository.addHistory(SavedTemplate.from(generated)) }
        android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
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
            // v0.5.3 motion polish: smooth PURE crossfade between destinations (no vertical slide).
            // Subtlety over visible motion — a slide was deliberately NOT added (the reference app
            // has more motion, but PokeQuery stays premium/utility-like). Durations centralized in
            // PqMotionTokens so the "reduce until premium" dial lives in one place.
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                transitionSpec = { fadeIn(tween(PqMotionTokens.SCREEN_CROSSFADE_MS)) togetherWith fadeOut(tween(PqMotionTokens.CROSSFADE_FADE_MS)) },
                popTransitionSpec = { fadeIn(tween(PqMotionTokens.SCREEN_CROSSFADE_MS)) togetherWith fadeOut(tween(PqMotionTokens.CROSSFADE_FADE_MS)) },
                predictivePopTransitionSpec = { _ -> fadeIn(tween(PqMotionTokens.SCREEN_CROSSFADE_MS)) togetherWith fadeOut(tween(PqMotionTokens.CROSSFADE_FADE_MS)) },
                entryProvider = entryProvider {
                    entry<Onboarding> { route ->
                        val scope = rememberCoroutineScope()
                        OnboardingScreen(initialPage = route.initialPage) {
                            scope.launch { repository.setFirstUseSeen(true) }
                            backStack.clear()
                            backStack.add(Home)
                        }
                    }
                    entry<Home> {
                        HomeScreen { goalId -> 
                            backStack.add(homeGoalDestination(goalId))
                        }
                    }
                    entry<GoalDetail> { route ->
                        GoalDetailScreen(
                            goalId = route.goalId,
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateRisk = { generatedString ->
                                backStack.add(RiskWarning(generatedString))
                            }
                        )
                    }
                    entry<ExpertBuilder> {
                        val language = userPrefs?.gameLanguage ?: "English"
                        ExpertBuilderScreen(
                            onGenerate = { query ->
                                val generated = StringBuilderEngine.buildGoal("expert", customQuery = query, language = language)
                                if (requiresRiskWarning(generated.riskLevel)) backStack.add(RiskWarning(generated)) else copyGenerated(generated)
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Presets> {
                        PresetsScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onCopy = ::copyGenerated,
                            onNavigateRisk = { generatedString ->
                                backStack.add(RiskWarning(generatedString))
                            }
                        )
                    }
                    // v0.6.1: Personal Presets (local only). Risk gating preserved.
                    entry<MyPresets> {
                        MyPresetsScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onCopy = ::copyGenerated,
                            onNavigateRisk = { generatedString ->
                                backStack.add(RiskWarning(generatedString))
                            }
                        )
                    }
                    // v0.6.1: Practice Mode (fake inventory sandbox, conceptual only).
                    entry<PracticeMode> {
                        PracticeModeScreen(onBack = { backStack.removeLastOrNull() })
                    }
                    // v0.6.1: Cleaning Journal (user-entered memory only, local).
                    entry<CleaningJournal> {
                        CleaningJournalScreen(onBack = { backStack.removeLastOrNull() })
                    }
                    // v0.6.2: Event Context.
                    entry<EventContext> {
                        EventContextScreen(
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    // v0.6.2: Safe NL search-string assistant.
                    entry<SearchAssistant> {
                        SearchAssistantScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onCopyRaw = { rawSyntax ->
                                clipboard.setText(AnnotatedString(rawSyntax))
                                scope.launch { repository.addHistory(com.caglar.pokequery.data.model.SavedTemplate.from(com.caglar.pokequery.data.model.GeneratedString(rawSyntax, "Search Assistant suggestion", emptyList(), emptyList(), com.caglar.pokequery.data.model.RiskLevel.Medium))) }
                            },
                            onExplain = { query ->
                                backStack.add(ExplainRoute(query))
                            }
                        )
                    }
                    // v0.6.2: Search String Explain mode.
                    entry<ExplainRoute> { route ->
                        ExplainScreen(
                            onBack = { backStack.removeLastOrNull() },
                            initialQuery = route.query
                        )
                    }
                    entry<Favorites> {
                        FavoritesScreen(
                            onCopy = { favorite ->
                                if (requiresRiskWarning(favorite.riskLevel)) {
                                    backStack.add(RiskWarning(favorite.asGeneratedString()))
                                } else {
                                    copyGenerated(favorite.asGeneratedString())
                                }
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<History> {
                        HistoryScreen(
                            onCopy = { history ->
                                if (requiresRiskWarning(history.riskLevel)) {
                                    backStack.add(RiskWarning(history.asGeneratedString()))
                                } else {
                                    copyGenerated(history.asGeneratedString())
                                }
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Settings> {
                        SettingsScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onOpenChangelog = { backStack.add(ChangelogRoute) }
                        )
                    }
                    entry<ChangelogRoute> { ChangelogScreen { backStack.removeLastOrNull() } }
                    entry<KnowledgeBase> { route -> KnowledgeBaseScreen(startExpanded = route.startExpanded) { backStack.removeLastOrNull() } }
                    entry<RiskWarning> { route ->
                        RiskWarningScreen(
                            generatedString = route.generatedString,
                            onConfirmCopy = {
                                copyGenerated(route.generatedString)
                                backStack.removeLastOrNull()
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
    } // end ProvidePqMotion
}
