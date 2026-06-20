package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.GoalArt
import com.caglar.pokequery.ui.components.HeroSearchShield
import com.caglar.pokequery.ui.components.MapBackdrop

private data class HomeGoal(
    val id: String,
    val title: String,
    val subtitle: String,
    val accent: Color
)

private val homeGoals = listOf(
    HomeGoal("safe_cleanup", "Safe Cleanup", "Remove clutter safely", TealPrimary),
    HomeGoal("candy_prep", "2x Candy Prep", "Find extras to transfer", AmberWarning),
    HomeGoal("trade_fodder", "Trade Fodder", "Find good trade candidates", BlueCTA),
    HomeGoal("hundo_check", "Hundo Check", "Perfect 15/15/15 IVs", PurpleIV),
    HomeGoal("nundo_finder", "Nundo Finder", "Exact 0/0/0 IVs", Color(0xFF90A4AE)),
    HomeGoal("pvp_candidates", "PvP IVs", "Great and Ultra candidates", Color(0xFF4FC3F7)),
    HomeGoal("lucky_trade", "Lucky Trade", "Older or distance trades", AmberWarning),
    HomeGoal("untagged", "Untagged", "Organize missing tags", TealPrimary),
    HomeGoal("expert", "Expert Builder", "Raw strings with linter", BlueCTA),
    HomeGoal("presets", "Popular Presets", "Ready-made examples", Color(0xFF64B5F6))
)

@Composable
fun HomeScreen(onGoalSelected: (String) -> Unit) {
    Scaffold(containerColor = BackgroundDark) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            item { HomeHero() }

            item {
                Text(
                    "What do you want to find?",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 12.dp)
                )
            }

            homeGoals.chunked(2).forEach { row ->
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { goal ->
                            RichGoalCard(goal, Modifier.weight(1f)) { onGoalSelected(goal.id) }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
    ) {
        MapBackdrop(Modifier.matchParentSize(), imageAlpha = 0.58f)
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PokeQuery", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Safe search strings for Pokémon GO",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                HeroSearchShield(Modifier.size(190.dp))
            }
        }
    }
}

@Composable
private fun RichGoalCard(goal: HomeGoal, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(188.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(CardPremium, CardDark)))
            .border(1.dp, goal.accent.copy(alpha = 0.48f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            Modifier.matchParentSize().background(
                Brush.radialGradient(
                    listOf(goal.accent.copy(alpha = 0.22f), Color.Transparent),
                    radius = 280f
                )
            )
        )
        Column(Modifier.fillMaxSize()) {
            GoalArt(goal.id, goal.accent, Modifier.fillMaxWidth().height(104.dp))
            Spacer(Modifier.weight(1f))
            Text(goal.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
            Text(goal.subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 2)
        }
    }
}
