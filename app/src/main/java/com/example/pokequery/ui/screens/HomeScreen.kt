package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.GoalCardGrid
import com.example.pokequery.ui.components.HomeMapHeaderCompose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onGoalSelected: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PokeQuery", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF030D1B))
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Map header with clean compose decor
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color(0xFF030D1B))) {
                HomeMapHeaderCompose()
                
                Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                    Text("What do you want to find?", color = Color.White, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pick a goal to build a safe search string.", color = TextSecondary, fontSize = 14.sp)
                }
            }
            
            val goals = listOf(
                GoalData("Safe Cleanup", "Find junk without deleting shinies", Icons.Default.Delete, TealPrimary, "safe_cleanup"),
                GoalData("2x Candy Prep", "Find duplicates to transfer", Icons.Default.Build, AmberWarning, "candy_prep"),
                GoalData("Trade Fodder", "Find non-shiny trade candidates", Icons.AutoMirrored.Filled.Send, BlueCTA, "trade_fodder"),
                GoalData("Hundo Check", "Find perfect 4★ Pokémon", Icons.Default.Star, Color(0xFF9C27B0), "hundo_check"),
                GoalData("Untagged Cleanup", "Find Pokémon missing tags", Icons.Default.Clear, TealPrimary, "untagged"),
                GoalData("Expert Builder", "Write your own raw strings", Icons.AutoMirrored.Filled.List, BlueCTA, "expert")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(goals) { goal ->
                    GoalCardGrid(
                        title = goal.title,
                        subtitle = goal.subtitle,
                        icon = goal.icon,
                        iconTint = goal.color,
                        onClick = { onGoalSelected(goal.id) }
                    )
                }
            }
        }
    }
}

private data class GoalData(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val id: String
)
