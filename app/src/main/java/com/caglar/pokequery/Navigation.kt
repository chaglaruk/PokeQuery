package com.caglar.pokequery

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
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import com.caglar.pokequery.ui.components.BottomNavBar
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
        when (startRoute) {
            "onboarding" -> Onboarding
            "home" -> Home
            "detail_safe_cleanup" -> GoalDetail("safe_cleanup")
            "detail_candy_prep" -> GoalDetail("candy_prep")
            "detail_trade_fodder" -> GoalDetail("trade_fodder")
            "detail_pvp_candidates" -> GoalDetail("pvp_candidates")
            "detail_nundo_finder" -> GoalDetail("nundo_finder")
            "detail_lucky_trade" -> GoalDetail("lucky_trade")
            "knowledge" -> KnowledgeBase
            "presets" -> Presets
            "expert" -> ExpertBuilder
            "favorites" -> Favorites
            "settings" -> Settings
            null -> when {
                userPrefs == null -> null
                userPrefs!!.firstUseSeen -> Home
                else -> Onboarding
            }
            else -> Home // Default to Home. Specific goals are handled inside Home -> GoalDetail
        }
    } ?: return

    val backStack = rememberNavBackStack(initialEntry)
    var currentTab by remember { mutableStateOf("home") }

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
            // By omitting custom animators that scale awkwardly, we get a clean fade/slide default from NavDisplay
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Onboarding> {
                        val scope = rememberCoroutineScope()
                        OnboardingScreen {
                            scope.launch { repository.setFirstUseSeen(true) }
                            backStack.clear()
                            backStack.add(Home)
                        }
                    }
                    entry<Home> {
                        HomeScreen { goalId -> 
                            if (goalId == "expert") {
                                backStack.add(ExpertBuilder)
                            } else if (goalId == "presets") {
                                backStack.add(Presets)
                            } else {
                                backStack.add(GoalDetail(goalId)) 
                            }
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
                        ExpertBuilderScreen(
                            onGenerate = { query ->
                                // Custom queries might need risk warning checks directly or through a unified preview.
                                // For now, we can show a toast or bypass to a warning.
                                val generated = StringBuilderEngine.buildGoal("expert", customQuery = query)
                                clipboard.setText(AnnotatedString(generated.rawSyntax))
                                android.widget.Toast.makeText(context, "Copied custom string", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Presets> {
                        PresetsScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateRisk = { generatedString ->
                                backStack.add(RiskWarning(generatedString))
                            }
                        )
                    }
                    entry<Favorites> {
                        FavoritesScreen(
                            onCopy = { favorite ->
                                if (requiresRiskWarning(favorite.riskLevel)) {
                                    backStack.add(RiskWarning(favorite.asGeneratedString()))
                                } else {
                                    clipboard.setText(AnnotatedString(favorite.rawSyntax))
                                    android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Settings> { SettingsScreen { backStack.removeLastOrNull() } }
                    entry<KnowledgeBase> { KnowledgeBaseScreen { backStack.removeLastOrNull() } }
                    entry<RiskWarning> { route ->
                        RiskWarningScreen(
                            generatedString = route.generatedString,
                            onConfirmCopy = {
                                clipboard.setText(AnnotatedString(route.generatedString.rawSyntax))
                                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                backStack.removeLastOrNull()
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
}
