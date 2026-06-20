package com.example.pokequery.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.R
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.GoalCardGrid

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
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color(0xFF030D1B))) {
                Image(
                    painter = painterResource(id = R.drawable.home_header_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.5f
                )
                
                Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                    Text("What do you want to find?", color = Color.White, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pick a goal to build a safe search string.", color = TextSecondary, fontSize = 14.sp)
                }
            }
            
            val cleanupGoals = listOf(
                GoalData("Safe Cleanup", "Find junk without deleting shinies", R.drawable.goal_safe_cleanup_icon, TealPrimary, "safe_cleanup"),
                GoalData("2x Candy Prep", "Find duplicates to transfer", R.drawable.goal_candy_prep_icon, AmberWarning, "candy_prep"),
                GoalData("Untagged Cleanup", "Find Pokémon missing tags", R.drawable.goal_tag_icon, TealPrimary, "untagged")
            )
            val tradingGoals = listOf(
                GoalData("Trade Fodder", "Find non-shiny trade candidates", R.drawable.goal_trade_icon, BlueCTA, "trade_fodder"),
                GoalData("Lucky Trade Prep", "Find old or distance trades", R.drawable.goal_trade_icon, AmberWarning, "lucky_trade")
            )
            val battleIvGoals = listOf(
                GoalData("Hundo Check", "Find perfect 4★ Pokémon", R.drawable.goal_hundo_icon, Color(0xFF9C27B0), "hundo_check"),
                GoalData("Nundo Finder", "Find exact 0% IV Pokémon", R.drawable.goal_hundo_icon, Color(0xFF607D8B), "nundo_finder"),
                GoalData("PvP Candidates", "Find Great/Ultra League IVs", R.drawable.goal_expert_icon, BlueCTA, "pvp_candidates")
            )
            val expertGoals = listOf(
                GoalData("Expert Builder", "Write your own raw strings", R.drawable.goal_expert_icon, BlueCTA, "expert")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(2) }) { SectionHeader("Cleanup") }
                items(cleanupGoals) { goal -> GoalCardGrid(goal.title, goal.subtitle, goal.icon, goal.color, { onGoalSelected(goal.id) }) }

                item(span = { GridItemSpan(2) }) { SectionHeader("Trading") }
                items(tradingGoals) { goal -> GoalCardGrid(goal.title, goal.subtitle, goal.icon, goal.color, { onGoalSelected(goal.id) }) }

                item(span = { GridItemSpan(2) }) { SectionHeader("Battle & IV Checks") }
                items(battleIvGoals) { goal -> GoalCardGrid(goal.title, goal.subtitle, goal.icon, goal.color, { onGoalSelected(goal.id) }) }

                item(span = { GridItemSpan(2) }) { SectionHeader("Expert") }
                items(expertGoals) { goal -> GoalCardGrid(goal.title, goal.subtitle, goal.icon, goal.color, { onGoalSelected(goal.id) }) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

private data class GoalData(
    val title: String,
    val subtitle: String,
    val icon: Int,
    val color: Color,
    val id: String
)
