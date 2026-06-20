package com.example.pokequery

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.pokequery.domain.engine.StringBuilderEngine
import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.ui.screens.HomeScreen
import com.example.pokequery.ui.screens.PreviewScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Home)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Home> {
          HomeScreen(onGoalSelected = { goalId -> backStack.add(Preview(goalId)) })
        }
        entry<Preview> { preview ->
            // Phase B MVP String generation
            val baseQuery = when (preview.goalId) {
                "safe_cleanup" -> "1*"
                "candy_prep" -> "count2-"
                "trade_fodder" -> "traded" // Example placeholder
                else -> ""
            }
            val explanation = when (preview.goalId) {
                "safe_cleanup" -> "This is a REVIEW string targeting 1-star low-value candidates. It is not an automatic transfer command."
                "candy_prep" -> "Finds extras. Count is based on Pokédex species number and may not distinguish shiny/form/costume differences."
                "trade_fodder" -> "Finds candidates for trading. Real trade eligibility depends on friendship level and cannot be guaranteed by search strings."
                else -> "Safe search string"
            }
            val risk = when (preview.goalId) {
                "safe_cleanup" -> RiskLevel.Low
                "candy_prep" -> RiskLevel.Medium
                "trade_fodder" -> RiskLevel.Low
                else -> RiskLevel.Low
            }

            val generated = StringBuilderEngine.buildString(
                baseQuery = baseQuery,
                explanation = explanation,
                riskLevel = risk
            )
            PreviewScreen(
                generatedString = generated,
                onCopy = { /* TODO implement clipboard copy feedback */ },
                onBack = { backStack.removeLastOrNull() }
            )
        }
      },
  )
}
