package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.caglar.pokequery.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.MapBackdrop
import com.caglar.pokequery.ui.components.goalHeaderRes

import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

private data class HomeGoal(
    val id: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val icon: ImageVector
)

private val homeGoals = listOf(
    HomeGoal("safe_cleanup", "Safe Cleanup", "Remove clutter safely", TealPrimary, Icons.Default.CleaningServices),
    HomeGoal("candy_prep", "2x Candy Prep", "Find extras to transfer", AmberWarning, Icons.Default.FolderSpecial),
    HomeGoal("trade_fodder", "Trade Fodder", "Find good trade candidates", BlueCTA, Icons.Default.SwapHoriz),
    HomeGoal("hundo_check", "Hundo Check", "Perfect 15/15/15 IVs", PurpleIV, Icons.Default.Diamond),
    HomeGoal("untagged", "Untagged Cleanup", "Organize missing tags", TealPrimary, Icons.Default.FilterList),
    HomeGoal("expert", "Expert Builder", "Raw strings with linter", BlueCTA, Icons.Default.Build),
    HomeGoal("nundo_finder", "Nundo Finder", "Exact 0/0/0 IVs", Color(0xFF90A4AE), Icons.Default.WaterDrop),
    HomeGoal("pvp_candidates", "PvP IVs", "Great & Ultra", Color(0xFF4FC3F7), Icons.Default.Star),
    HomeGoal("lucky_trade", "Lucky Trade", "Older/distance", AmberWarning, Icons.Default.Favorite),
    HomeGoal("presets", "Popular Presets", "Ready examples", Color(0xFF64B5F6), Icons.Default.Favorite)
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
            .height(260.dp)
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.home_header_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, BackgroundDark))))
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.IconButton(onClick = {}) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    androidx.compose.material3.IconButton(onClick = {}) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Image(
                    painter = painterResource(com.caglar.pokequery.R.drawable.logo_wordmark_source),
                    contentDescription = "PokeQuery",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(34.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Safe search strings for Pokémon GO",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(34.dp))
        }
    }
}

@Composable
private fun RichGoalCard(goal: HomeGoal, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardPremium)
            .border(1.dp, goal.accent.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(goal.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(goal.icon, contentDescription = null, tint = goal.accent, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(goal.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(goal.subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 2)
            }
        }
    }
}
