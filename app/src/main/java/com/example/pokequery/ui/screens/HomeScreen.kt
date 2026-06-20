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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.pokequery.theme.AmberWarning
import com.example.pokequery.theme.BackgroundDark
import com.example.pokequery.theme.CoralDanger
import com.example.pokequery.theme.PurpleIV
import com.example.pokequery.theme.TealPrimary
import com.example.pokequery.ui.components.GoalCard

data class GoalItem(val id: String, val title: String, val description: String, val icon: ImageVector, val color: Color)

@Composable
fun HomeScreen(onGoalSelected: (String) -> Unit) {
    val goals = listOf(
        GoalItem("safe_cleanup", "Safe Cleanup", "Remove clutter safely", Icons.Default.CheckCircle, TealPrimary),
        GoalItem("candy_prep", "2x Candy Prep", "Find extras to transfer", Icons.Default.List, AmberWarning),
        GoalItem("trade_fodder", "Trade Fodder", "Find good trade picks", Icons.Default.Info, CoralDanger),
        GoalItem("hundo_check", "Hundo Check", "Check for perfect IVs", Icons.Default.Search, PurpleIV),
        GoalItem("untagged", "Untagged Cleanup", "Organize your tags", Icons.Default.Delete, TealPrimary),
        GoalItem("expert", "Expert Builder", "Advanced custom rules", Icons.Default.Build, Color.Gray)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "What do you want to find?",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Pick a goal and we'll build a safe search string for it.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(goals) { goal ->
                GoalCard(
                    title = goal.title,
                    description = goal.description,
                    icon = goal.icon,
                    iconTint = goal.color,
                    onClick = { onGoalSelected(goal.id) }
                )
            }
        }
    }
}
