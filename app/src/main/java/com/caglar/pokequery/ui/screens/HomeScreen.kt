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
import androidx.compose.foundation.layout.heightIn
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
    val titleRes: Int,
    val subtitleRes: Int,
    val accent: Color,
    val icon: ImageVector
)

// v0.6.2 polish: 8 primary search goals (Safe, 2x, Lucky, Nundo, PvP, Event, Popular, My Presets)
// + remaining tools in collapsible "More Tools" (Favorites removed from Home cards).
private val primaryGoals = listOf(
    HomeGoal("safe_cleanup", com.caglar.pokequery.R.string.goal_safe_cleanup, com.caglar.pokequery.R.string.goal_safe_cleanup_desc, TealPrimary, Icons.Default.CleaningServices),
    HomeGoal("candy_prep", com.caglar.pokequery.R.string.goal_candy_prep, com.caglar.pokequery.R.string.goal_candy_prep_desc, GoldCaution, Icons.Default.FolderSpecial),
    HomeGoal("lucky_trade", com.caglar.pokequery.R.string.goal_lucky_trade, com.caglar.pokequery.R.string.goal_lucky_trade_desc, GoldCaution, Icons.Default.Favorite),
    HomeGoal("assistant", com.caglar.pokequery.R.string.goal_assistant, com.caglar.pokequery.R.string.goal_assistant_desc, PurpleIV, Icons.Default.Search),
    HomeGoal("pvp_candidates", com.caglar.pokequery.R.string.goal_pvp_candidates, com.caglar.pokequery.R.string.goal_pvp_candidates_desc, Color(0xFF4FC3F7), Icons.Default.Star),
    HomeGoal("events", com.caglar.pokequery.R.string.goal_events, com.caglar.pokequery.R.string.goal_events_desc, TealPrimary, Icons.Default.Event),
    HomeGoal("presets", com.caglar.pokequery.R.string.goal_presets, com.caglar.pokequery.R.string.goal_presets_desc, Color(0xFF64B5F6), Icons.Default.Search),
    HomeGoal("my_presets", com.caglar.pokequery.R.string.goal_my_presets, com.caglar.pokequery.R.string.goal_my_presets_desc, CyanGlow, Icons.Default.Star)
)

private val toolGoals = listOf(
    HomeGoal("nundo_finder", com.caglar.pokequery.R.string.goal_nundo_finder, com.caglar.pokequery.R.string.goal_nundo_finder_desc, Color(0xFF90A4AE), Icons.Default.WaterDrop),
    HomeGoal("hundo_check", com.caglar.pokequery.R.string.goal_hundo_check, com.caglar.pokequery.R.string.goal_hundo_check_desc, PurpleIV, Icons.Default.Diamond),
    HomeGoal("trade_fodder", com.caglar.pokequery.R.string.goal_trade_fodder, com.caglar.pokequery.R.string.goal_trade_fodder_desc, CyanGlow, Icons.Default.SwapHoriz),
    HomeGoal("untagged", com.caglar.pokequery.R.string.goal_untagged, com.caglar.pokequery.R.string.goal_untagged_desc, TealPrimary, Icons.Default.FilterList),
    HomeGoal("practice", com.caglar.pokequery.R.string.goal_practice, com.caglar.pokequery.R.string.goal_practice_desc, Color(0xFF4FC3F7), Icons.Default.SportsEsports),
    HomeGoal("journal", com.caglar.pokequery.R.string.goal_journal, com.caglar.pokequery.R.string.goal_journal_desc, GoldCaution, Icons.Default.School),
    HomeGoal("expert", com.caglar.pokequery.R.string.goal_expert, com.caglar.pokequery.R.string.goal_expert_desc, CyanGlow, Icons.Default.Build),
    HomeGoal("knowledge", com.caglar.pokequery.R.string.goal_knowledge, com.caglar.pokequery.R.string.goal_knowledge_desc, TealPrimary, Icons.Default.MenuBook),
    HomeGoal("explain", com.caglar.pokequery.R.string.goal_explain, com.caglar.pokequery.R.string.goal_explain_desc, TealPrimary, Icons.Default.FilterList),
    HomeGoal("changelog", com.caglar.pokequery.R.string.goal_changelog, com.caglar.pokequery.R.string.goal_changelog_desc, TealPrimary, Icons.Default.MenuBook),
    HomeGoal("settings", com.caglar.pokequery.R.string.goal_settings, com.caglar.pokequery.R.string.goal_settings_desc, TextSecondary, Icons.Default.Build)
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
                    PqTrustChip(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.home_chip_offline))
                    PqTrustChip(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.home_chip_nologin))
                    PqTrustChip(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.home_chip_notracking))
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
        com.caglar.pokequery.ui.pq.PqWordmark(width = 160.dp)
        Spacer(Modifier.height(6.dp))
        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.home_header_desc), color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun MoreToolsSection(onGoalSelected: (String) -> Unit) {
    val density = currentDensity()
    var expanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
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
            Column(Modifier.weight(1f)) {
                Text(
                    androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.home_more_tools),
                    color = TextSecondary.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.home_more_tools_subtitle),
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
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
            .heightIn(min = 120.dp)
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
        Text(androidx.compose.ui.res.stringResource(goal.titleRes), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(androidx.compose.ui.res.stringResource(goal.subtitleRes), color = TextSecondary, fontSize = 12.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}
