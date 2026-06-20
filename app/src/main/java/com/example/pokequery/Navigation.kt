package com.example.pokequery

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
import com.example.pokequery.data.repository.UserPreferencesRepository
import com.example.pokequery.data.repository.dataStore
import com.example.pokequery.data.model.SavedTemplate
import com.example.pokequery.domain.engine.StringBuilderEngine
import com.example.pokequery.ui.components.BottomNavBar
import com.example.pokequery.ui.screens.*
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
            "guided_safe_cleanup" -> GuidedQuestions("safe_cleanup")
            "preview_safe_cleanup" -> Preview(StringBuilderEngine.buildGoal("safe_cleanup"))
            "preview_candy_prep" -> Preview(StringBuilderEngine.buildGoal("candy_prep"))
            "preview_trade_fodder" -> Preview(StringBuilderEngine.buildGoal("trade_fodder"))
            "knowledge" -> KnowledgeBase
            "expert" -> ExpertBuilder
            "favorites" -> Favorites
            "settings" -> Settings
            null -> when {
                userPrefs == null -> null
                userPrefs!!.firstUseSeen -> Home
                else -> Onboarding
            }
            else -> Home
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
                        HomeScreen { goalId -> backStack.add(homeGoalDestination(goalId)) }
                    }
                    entry<GuidedQuestions> { route ->
                        GuidedQuestionsScreen(
                            goalId = route.goalId,
                            onGenerate = { include0Star ->
                                backStack.add(Preview(StringBuilderEngine.buildGoal(route.goalId, include0Star)))
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Preview> { route ->
                        PreviewScreen(
                            generatedString = route.generatedString,
                            onCopy = {
                                if (requiresRiskWarning(route.generatedString.riskLevel)) {
                                    backStack.add(RiskWarning(route.generatedString))
                                } else {
                                    clipboard.setText(AnnotatedString(route.generatedString.rawSyntax))
                                    android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onSaveFavorite = {
                                scope.launch { repository.addFavorite(SavedTemplate.from(route.generatedString)) }
                                android.widget.Toast.makeText(context, "Saved to favorites", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<ExpertBuilder> {
                        ExpertBuilderScreen(
                            onGenerate = { query ->
                                backStack.add(Preview(StringBuilderEngine.buildGoal("expert", customQuery = query)))
                            },
                            onBack = { backStack.removeLastOrNull() }
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
