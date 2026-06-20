package com.example.pokequery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.pokequery.domain.engine.StringBuilderEngine
import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.ui.screens.*
import com.example.pokequery.ui.components.BottomNavBar

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Home)
  var currentTab by remember { mutableStateOf("home") }

  Scaffold(
      bottomBar = {
          BottomNavBar(
              currentRoute = currentTab,
              onNavigate = { route ->
                  currentTab = route
                  backStack.clear()
                  when (route) {
                      "home" -> backStack.add(Home)
                      "builder" -> backStack.add(ExpertBuilder)
                      "favorites" -> backStack.add(Favorites)
                      "settings" -> backStack.add(Settings)
                  }
              }
          )
      }
  ) { paddingValues ->
      Box(modifier = Modifier.padding(paddingValues)) {
          NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Home> {
                  HomeScreen(onGoalSelected = { goalId -> backStack.add(GuidedQuestions(goalId)) })
                }
                entry<GuidedQuestions> { gq ->
                    GuidedQuestionsScreen(
                        goalId = gq.goalId,
                        onGenerate = { include0Star ->
                            val baseQuery = when (gq.goalId) {
                                "safe_cleanup" -> "1*"
                                "candy_prep" -> "count2-"
                                "trade_fodder" -> "count2-&!traded"
                                "hundo_check" -> "4*"
                                "untagged" -> "!#"
                                "expert" -> ""
                                else -> ""
                            }
                            backStack.add(Preview(baseQuery, gq.goalId, include0Star))
                        },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<Preview> { preview ->
                    var query = preview.baseQuery
                    val protections = StringBuilderEngine.DEFAULT_PROTECTIONS.toMutableList()
                    if (preview.goalId == "safe_cleanup" && preview.include0Star) {
                        query = "0*,1*"
                        protections.remove("0*")
                    } else if (preview.goalId == "safe_cleanup" && !preview.include0Star) {
                        protections.remove("0*") // handled by guided flow
                    } else if (preview.goalId == "hundo_check") {
                        protections.clear()
                    }

                    val explanation = when (preview.goalId) {
                        "safe_cleanup" -> "This is a REVIEW string targeting 1-star low-value candidates. It is not an automatic transfer command."
                        "candy_prep" -> "Finds extras. Count is based on Pokédex species number and may not distinguish shiny/form/costume differences."
                        "trade_fodder" -> "Finds candidates for trading. Real trade eligibility depends on friendship level and cannot be guaranteed by search strings."
                        "hundo_check" -> "Finds all perfect IV / hundo Pokémon. 4★ means 15/15/15."
                        "untagged" -> "Finds Pokemon without any tags."
                        else -> "Custom or generated search string."
                    }
                    val risk = when (preview.goalId) {
                        "safe_cleanup" -> if (preview.include0Star) RiskLevel.Medium else RiskLevel.Low
                        "candy_prep" -> RiskLevel.Medium
                        "trade_fodder" -> RiskLevel.Low
                        else -> RiskLevel.Low
                    }

                    val generated = StringBuilderEngine.buildString(
                        baseQuery = query,
                        protections = protections,
                        explanation = explanation,
                        riskLevel = risk
                    )
                    
                    PreviewScreen(
                        generatedString = generated,
                        onCopy = { /* TODO implement clipboard copy feedback */ },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<ExpertBuilder> {
                    ExpertBuilderScreen(
                        onGenerate = { query -> backStack.add(Preview(query, "expert", false)) },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<Favorites> {
                    FavoritesScreen(onBack = { backStack.removeLastOrNull() })
                }
                entry<Settings> {
                    SettingsScreen(onBack = { backStack.removeLastOrNull() })
                }
                entry<KnowledgeBase> {
                    KnowledgeBaseScreen(onBack = { backStack.removeLastOrNull() })
                }
                entry<RiskWarning> {
                    RiskWarningScreen(
                        onAcknowledge = { backStack.removeLastOrNull() },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
              }
          )
      }
  }
}
