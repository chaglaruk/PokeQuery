package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.NightMapBackground
import com.example.pokequery.ui.components.PremiumGoalCard

data class GoalItem(val id: String, val title: String, val description: String, val icon: ImageVector, val color: Color)

@Composable
fun HomeScreen(onGoalSelected: (String) -> Unit) {
    val goals = listOf(
        GoalItem("safe_cleanup", "Safe Cleanup", "Remove clutter safely", Icons.Default.CheckCircle, TealPrimary),
        GoalItem("candy_prep", "2x Candy Prep", "Find extras to transfer", Icons.Default.List, AmberWarning),
        GoalItem("trade_fodder", "Trade Fodder", "Find good trade picks", Icons.Default.Info, BlueCTA),
        GoalItem("hundo_check", "Hundo Check", "Check for perfect IVs", Icons.Default.Search, PurpleIV),
        GoalItem("untagged", "Untagged Cleanup", "Organize your tags", Icons.Default.Delete, TealPrimary),
        GoalItem("expert", "Expert Builder", "Advanced custom rules", Icons.Default.Build, TextSecondary)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Map Header Background
        NightMapBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "What do you want to find?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Pick a goal and we'll build a safe\nsearch string for it.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(goals) { goal ->
                    PremiumGoalCard(
                        title = goal.title,
                        subtitle = goal.description,
                        icon = goal.icon,
                        iconTint = goal.color,
                        onClick = { onGoalSelected(goal.id) }
                    )
                }
            }
        }
    }
}
