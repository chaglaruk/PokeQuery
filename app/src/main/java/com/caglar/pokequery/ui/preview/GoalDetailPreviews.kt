package com.caglar.pokequery.ui.preview

import androidx.compose.runtime.Composable
import com.caglar.pokequery.ui.screens.GoalDetailScreen

@Composable
private fun GoalDetailPreviewContent() {
    PokeQueryPreviewFrame(currentRoute = "builder") {
        GoalDetailScreen(
            goalId = "safe_cleanup",
            onBack = {},
            onNavigateRisk = {}
        )
    }
}

@PokeQueryPhonePreviews
@Composable
fun GoalDetailScreenPhonePreviews() {
    GoalDetailPreviewContent()
}

@PokeQueryLocalePreviews
@Composable
fun GoalDetailScreenLocalePreviews() {
    GoalDetailPreviewContent()
}
