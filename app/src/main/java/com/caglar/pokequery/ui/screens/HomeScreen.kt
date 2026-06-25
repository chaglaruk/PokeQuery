package com.caglar.pokequery.ui.screens

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// v0.5.0 Stitch Home: header + 3 trust chips (horizontal scroll) + 2-col goal grid.
// Public HomeScreen(onGoalSelected) signature unchanged so Navigation.kt is untouched.

private data class HomeGoal(
    val id: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val icon: ImageVector
)

private val homeGoals = listOf(
    HomeGoal("safe_cleanup", "Safe Cleanup", "Review low-value candidates", TealPrimary, Icons.Default.CleaningServices),
    HomeGoal("candy_prep", "2x Candy Prep", "Find extras to transfer", GoldCaution, Icons.Default.FolderSpecial),
    HomeGoal("trade_fodder", "Trade Fodder", "Untraded duplicates", CyanGlow, Icons.Default.SwapHoriz),
    HomeGoal("lucky_trade", "Lucky Trade", "Older / distance Pokémon", GoldCaution, Icons.Default.Favorite),
    HomeGoal("hundo_check", "Hundo Check", "Perfect 15/15/15 IVs", PurpleIV, Icons.Default.Diamond),
    HomeGoal("nundo_finder", "Nundo Finder", "Exact 0/0/0 IVs", Color(0xFF90A4AE), Icons.Default.WaterDrop),
    HomeGoal("pvp_candidates", "PvP Candidates", "Great & Ultra League", Color(0xFF4FC3F7), Icons.Default.Star),
    HomeGoal("untagged", "Untagged Cleanup", "Pokémon without tags", TealPrimary, Icons.Default.FilterList),
    HomeGoal("presets", "Popular Presets", "Ready-made strings", Color(0xFF64B5F6), Icons.Default.Search),
    HomeGoal("expert", "Expert Builder", "Build your own", CyanGlow, Icons.Default.Build),
    HomeGoal("favorites", "Favorites", "Saved strings", GoldCaution, Icons.Default.Favorite),
    HomeGoal("knowledge", "Knowledge Base", "All search tokens", TealPrimary, Icons.Default.MenuBook)
)

@Composable
fun HomeScreen(onGoalSelected: (String) -> Unit) {
    // v0.5.2 (Fix 6): goal-grid vertical gaps and trust-chip spacing follow Visual Density.
    val density = currentDensity()
    // v0.5.3 motion polish: staggered entrance driven by ONE hoisted `visible` flag. Per C1,
    // only the header + first couple of goal rows (the part painted on the first frame) are
    // tagged — rows that first compose after the entrance is complete animate instantly from
    // rest, so scrolling never replays the cascade. The top-level `pqStaggeredItem(visible, i)`
    // modifier works inside LazyColumn item blocks without a scope receiver.
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
            homeGoals.chunked(2).forEachIndexed { rowIndex, row ->
                item {
                    // v0.5.4 (Fix 3): EVERY goal row animates once on the screen entrance, not
                    // just the first two. Each row gets a stable stagger index (header=0, chips=1,
                    // rows start at 2) capped at MAX_STAGGER_INDEX so the cascade stays subtle and
                    // rows beyond the cap share the final (longest) delay. The screen-level `visible`
                    // flag flips once and never resets, so scrolling never replays the entrance
                    // (C1); an item composed after entrance animates instantly to its at-rest state.
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
        }
    }
    }
}

@Composable
private fun HomeHeader(entranceModifier: Modifier = Modifier) {
    // v0.5.2 (Fix 3): Home now uses the SAME vector PqWordmark as onboarding, so the brand
    // logo is consistent across the two screens instead of diverging. Original artwork — no
    // Pokémon logo font, colors, Poké Ball, or creatures.
    Column(Modifier.fillMaxWidth().then(entranceModifier).padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 6.dp)) {
        com.caglar.pokequery.ui.pq.PqWordmark(fontSize = 30.sp)
        Spacer(Modifier.height(6.dp))
        Text("Build safer search strings for Pokémon GO", color = TextSecondary, fontSize = 14.sp)
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
