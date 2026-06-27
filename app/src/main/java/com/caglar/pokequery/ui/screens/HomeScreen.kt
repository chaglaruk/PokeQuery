package com.caglar.pokequery.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CardPremium
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.PurpleIV
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.motion.PqMotionTokens
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.pq.PqTrustChip

// v0.6.2 polish Home: header + 3 trust chips (horizontal scroll) + 8 primary goal cards (2-col grid)
// + collapsible "More Tools" section. Favorites removed from Home (accessible via bottom nav).

private data class HomeGoal(
    val id: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val icon: ImageVector
)

// v0.6.2 polish: 8 primary search goals (Safe, 2x, Lucky, Nundo, PvP, Event, Popular, My Presets)
// + remaining tools in collapsible "More Tools" (Favorites removed from Home cards).
private val primaryGoals = listOf(
    HomeGoal("safe_cleanup", "Safe Cleanup", "Review low-value candidates", TealPrimary, Icons.Default.CleaningServices),
    HomeGoal("candy_prep", "2x Candy Prep", "Find extras to transfer", GoldCaution, Icons.Default.FolderSpecial),
    HomeGoal("lucky_trade", "Lucky Trade", "Older / distance Pokémon", GoldCaution, Icons.Default.Favorite),
    HomeGoal("nundo_finder", "Nundo Finder", "Exact 0/0/0 IVs", Color(0xFF90A4AE), Icons.Default.WaterDrop),
    HomeGoal("pvp_candidates", "PvP Candidates", "Great & Ultra League", Color(0xFF4FC3F7), Icons.Default.Star),
    HomeGoal("events", "Event Context", "Daily event search context", TealPrimary, Icons.Default.Event),
    HomeGoal("presets", "Popular Presets", "Ready-made strings", Color(0xFF64B5F6), Icons.Default.Search),
    HomeGoal("my_presets", "My Presets", "Your saved personal presets", CyanGlow, Icons.Default.Star)
)

private val toolGoals = listOf(
    HomeGoal("hundo_check", "Hundo Check", "Perfect 15/15/15 IVs", PurpleIV, Icons.Default.Diamond),
    HomeGoal("trade_fodder", "Trade Fodder", "Untraded duplicates", CyanGlow, Icons.Default.SwapHoriz),
    HomeGoal("untagged", "Untagged Cleanup", "Pokémon without tags", TealPrimary, Icons.Default.FilterList),
    HomeGoal("practice", "Practice Mode", "Fake inventory sandbox", Color(0xFF4FC3F7), Icons.Default.SportsEsports),
    HomeGoal("journal", "Cleaning Journal", "Manual notes (local)", GoldCaution, Icons.Default.School),
    HomeGoal("expert", "Expert Builder", "Build your own", CyanGlow, Icons.Default.Build),
    HomeGoal("knowledge", "Knowledge Base", "All search tokens", TealPrimary, Icons.Default.MenuBook),
    HomeGoal("assistant", "Search Assistant", "Describe in plain English", PurpleIV, Icons.Default.Search),
    HomeGoal("explain", "Explain String", "Token-by-token breakdown", TealPrimary, Icons.Default.FilterList),
    HomeGoal("changelog", "What Changed", "Version history", TealPrimary, Icons.Default.MenuBook),
    HomeGoal("settings", "Settings", "Privacy, language, safety", TextSecondary, Icons.Default.Build)
)

@Composable
fun HomeScreen(onGoalSelected: (String) -> Unit) {
    val density = currentDensity()
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    Scaffold(containerColor = BackgroundDark) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { HomeHeader(Modifier.pqStaggeredItem(visible, 0)) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .pqStaggeredItem(visible, 1),
                    horizontalArrangement = Arrangement.spacedBy(density.chipSpacing)
                ) {
                    PqTrustChip("Offline-First")
                    PqTrustChip("No Login")
                    PqTrustChip("No Tracking")
                }
            }
            primaryGoals.chunked(2).forEachIndexed { rowIndex, row ->
                item {
                    val staggerIndex = (2 + rowIndex).coerceAtMost(PqMotionTokens.MAX_STAGGER_INDEX)
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = (density.listGap.value / 2).dp)
                            .pqStaggeredItem(visible, staggerIndex),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { goal ->
                            StitchGoalCard(goal, Modifier.weight(1f)) { onGoalSelected(goal.id) }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            item { MoreToolsSection(onGoalSelected) }
        }
    }
    }
}

@Composable
private fun HomeHeader(entranceModifier: Modifier = Modifier) {
    Column(Modifier.fillMaxWidth().then(entranceModifier).padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 6.dp)) {
        com.caglar.pokequery.ui.pq.PqWordmark(fontSize = 30.sp)
        Spacer(Modifier.height(6.dp))
        Text("Build safer search strings for Pokémon GO", color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun MoreToolsSection(onGoalSelected: (String) -> Unit) {
    val density = currentDensity()
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300)
    )

    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "More Tools",
                color = TextSecondary.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp).rotate(rotation)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(350)),
            exit = shrinkVertically(animationSpec = tween(250))
        ) {
            Column {
                toolGoals.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = (density.listGap.value / 2).dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { goal ->
                            StitchGoalCard(goal, Modifier.weight(1f)) { onGoalSelected(goal.id) }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchGoalCard(goal: HomeGoal, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // v0.5.5 (Fix 1): the goal card padding and its inner element gap (icon → title) follow
    // the Visual Density tokens, so Compact tightens each Home tile visibly. Touch targets are
    // untouched (the whole card remains tappable); only padding/gaps scale.
    val density = currentDensity()
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(CardPremium, CardDark)))
            .border(1.dp, goal.accent.copy(alpha = 0.3f), shape)
            .clickable(onClick = onClick)
            .padding(density.cardPadding)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(goal.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(goal.icon, contentDescription = null, tint = goal.accent, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(density.innerElementGap))
        Text(goal.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(goal.subtitle, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
    }
}
