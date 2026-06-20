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
            // Mock MVP string generation for now based on goalId
            val explanation = if (preview.goalId == "candy_prep") "Finds regular, non-special creatures you can transfer safely." else "Safe search string"
            val query = if (preview.goalId == "candy_prep") "count2-" else ""
            val generated = StringBuilderEngine.buildString(
                baseQuery = query,
                includeProtections = true,
                explanation = explanation,
                riskLevel = if (preview.goalId == "candy_prep") RiskLevel.Medium else RiskLevel.Low
            )
            PreviewScreen(
                generatedString = generated,
                onCopy = { /* TODO implement clipboard copy */ },
                onBack = { backStack.removeLastOrNull() }
            )
        }
      },
  )
}
