package com.caglar.pokequery.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CardPremium
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.GreenVerified
import com.caglar.pokequery.theme.PurpleIV
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.clearFocusOnTap
import com.caglar.pokequery.ui.motion.PqMotionTokens
import com.caglar.pokequery.ui.motion.pqStaggeredItem

private data class HomeGoal(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val accent: Color,
    val icon: ImageVector
)

private val primaryGoals = listOf(
    HomeGoal("safe_cleanup", R.string.goal_safe_cleanup, R.string.goal_safe_cleanup_desc, TealPrimary, Icons.Default.CleaningServices),
    HomeGoal("candy_prep", R.string.goal_candy_prep, R.string.goal_candy_prep_desc, GoldCaution, Icons.Default.FolderSpecial),
    HomeGoal("lucky_trade", R.string.goal_lucky_trade, R.string.goal_lucky_trade_desc, GoldCaution, Icons.Default.Favorite),
    HomeGoal("assistant", R.string.goal_assistant, R.string.goal_assistant_desc, PurpleIV, Icons.Default.Search),
    HomeGoal("pvp_candidates", R.string.goal_pvp_candidates, R.string.goal_pvp_candidates_desc, Color(0xFF4FC3F7), Icons.Default.Star),
    HomeGoal("events", R.string.goal_events, R.string.goal_events_desc, TealPrimary, Icons.Default.Event),
    HomeGoal("presets", R.string.goal_presets, R.string.goal_presets_desc, Color(0xFF64B5F6), Icons.Default.Search),
    HomeGoal("my_presets", R.string.goal_my_presets, R.string.goal_my_presets_desc, CyanGlow, Icons.Default.Star)
)

private val toolGoals = listOf(
    HomeGoal("nundo_finder", R.string.goal_nundo_finder, R.string.goal_nundo_finder_desc, Color(0xFF90A4AE), Icons.Default.WaterDrop),
    HomeGoal("hundo_check", R.string.goal_hundo_check, R.string.goal_hundo_check_desc, PurpleIV, Icons.Default.Diamond),
    HomeGoal("trade_fodder", R.string.goal_trade_fodder, R.string.goal_trade_fodder_desc, CyanGlow, Icons.Default.SwapHoriz),
    HomeGoal("untagged", R.string.goal_untagged, R.string.goal_untagged_desc, TealPrimary, Icons.Default.FilterList),
    HomeGoal("practice", R.string.goal_practice, R.string.goal_practice_desc, Color(0xFF4FC3F7), Icons.Default.SportsEsports),
    HomeGoal("journal", R.string.goal_journal, R.string.goal_journal_desc, GoldCaution, Icons.Default.School),
    HomeGoal("expert", R.string.goal_expert, R.string.goal_expert_desc, CyanGlow, Icons.Default.Build),
    HomeGoal("knowledge", R.string.goal_knowledge, R.string.goal_knowledge_desc, TealPrimary, Icons.Default.MenuBook),
    HomeGoal("explain", R.string.goal_explain, R.string.goal_explain_desc, TealPrimary, Icons.Default.FilterList),
    HomeGoal("changelog", R.string.goal_changelog, R.string.goal_changelog_desc, TealPrimary, Icons.Default.MenuBook),
    HomeGoal("settings", R.string.goal_settings, R.string.goal_settings_desc, TextSecondary, Icons.Default.Build)
)

data class ChipInfo(val title: String, val desc: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(onGoalSelected: (String) -> Unit) {
    val density = currentDensity()
    var activeChipInfo by remember { mutableStateOf<ChipInfo?>(null) }

    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
        Box(Modifier.fillMaxSize().background(BackgroundDark)) {
            HomeBackground()
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().clearFocusOnTap().padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item { HomeHeader(Modifier.pqStaggeredItem(visible, 0)) }
                    primaryGoals.chunked(2).forEachIndexed { rowIndex, row ->
                        item {
                            val staggerIndex = (1 + rowIndex).coerceAtMost(PqMotionTokens.MAX_STAGGER_INDEX)
                            Row(
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 5.dp)
                                    .pqStaggeredItem(visible, staggerIndex),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { goal ->
                                    GoalCard(goal, Modifier.weight(1f)) { onGoalSelected(goal.id) }
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    item { MoreToolsSection(onGoalSelected) }
                }

                // Compact Trust Information Dialog
                activeChipInfo?.let { info ->
                    AlertDialog(
                        onDismissRequest = { activeChipInfo = null },
                        title = {
                            Text(info.title, color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        },
                        text = {
                            Text(info.desc, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                        },
                        confirmButton = {
                            TextButton(onClick = { activeChipInfo = null }) {
                                Text(stringResource(android.R.string.ok), color = TealPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = CardPremium,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBackground() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.5f, h * 0.22f)
        drawCircle(TealPrimary.copy(alpha = 0.10f), w * 0.42f, center, style = Stroke(width = 4f))
        drawCircle(CyanGlow.copy(alpha = 0.07f), w * 0.62f, Offset(w * 0.7f, h * 0.62f), style = Stroke(width = 3f))
        drawLine(TealPrimary.copy(alpha = 0.12f), Offset(w * 0.05f, h * 0.18f), Offset(w * 0.92f, h * 0.55f), strokeWidth = 4f)
        drawLine(GoldCaution.copy(alpha = 0.10f), Offset(w * 0.12f, h * 0.72f), Offset(w * 0.82f, h * 0.24f), strokeWidth = 3f)
        drawCircle(GoldCaution.copy(alpha = 0.12f), 38f, Offset(w * 0.78f, h * 0.14f))
        drawCircle(TealPrimary.copy(alpha = 0.10f), 28f, Offset(w * 0.22f, h * 0.68f))
    }
}

@Composable
private fun HomeHeader(entranceModifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(entranceModifier)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF071C30),
                            BackgroundDark
                        )
                    )
                )
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.5f, size.height * 0.48f)
                drawCircle(TealPrimary.copy(alpha = 0.12f), size.width * 0.38f, center, style = Stroke(width = 4f))
                drawCircle(CyanGlow.copy(alpha = 0.10f), size.width * 0.24f, center, style = Stroke(width = 2f))
                drawLine(TealPrimary.copy(alpha = 0.18f), Offset(size.width * 0.15f, size.height * 0.75f), Offset(size.width * 0.82f, size.height * 0.28f), strokeWidth = 3f)
                drawCircle(GoldCaution.copy(alpha = 0.22f), 34f, Offset(size.width * 0.78f, size.height * 0.3f))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            com.caglar.pokequery.ui.pq.PqWordmark(width = 200.dp, centered = true)
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_subtitle),
                color = TextPrimary,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_subtitle_secondary),
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MoreToolsSection(onGoalSelected: (String) -> Unit) {
    val density = currentDensity()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300)
    )

    Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_more_tools),
                    color = TextSecondary.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.home_more_tools_subtitle),
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
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { goal ->
                            GoalCard(goal, Modifier.weight(1f)) { onGoalSelected(goal.id) }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: HomeGoal, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(CardPremium, CardDark)))
            .border(1.dp, goal.accent.copy(alpha = 0.25f), shape)
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .size(72.dp)
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(topStart = 28.dp))
                .background(goal.accent.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            GoalGlyph(goal.id, goal.accent.copy(alpha = 0.42f), Modifier.size(44.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(goal.accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    GoalGlyph(goal.id, goal.accent, Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(goal.titleRes),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(goal.subtitleRes),
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GoalGlyph(goalId: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = (w * 0.10f).coerceAtLeast(2f))
        fun node(x: Float, y: Float, r: Float = w * 0.08f) = drawCircle(color, r, Offset(w * x, h * y))
        when (goalId) {
            "safe_cleanup" -> {
                drawLine(color, Offset(w * 0.28f, h * 0.70f), Offset(w * 0.70f, h * 0.28f), strokeWidth = w * 0.12f)
                drawRoundRect(color.copy(alpha = 0.24f), Offset(w * 0.22f, h * 0.16f), Size(w * 0.56f, h * 0.58f), CornerRadius(w * 0.18f), style = stroke)
            }
            "candy_prep" -> {
                drawCircle(color.copy(alpha = 0.22f), w * 0.24f, Offset(w * 0.50f, h * 0.50f))
                node(0.22f, 0.50f, w * 0.10f); node(0.78f, 0.50f, w * 0.10f)
            }
            "trade_fodder", "lucky_trade" -> {
                drawLine(color, Offset(w * 0.18f, h * 0.35f), Offset(w * 0.78f, h * 0.35f), strokeWidth = w * 0.10f)
                drawLine(color, Offset(w * 0.82f, h * 0.35f), Offset(w * 0.66f, h * 0.22f), strokeWidth = w * 0.10f)
                drawLine(color, Offset(w * 0.82f, h * 0.35f), Offset(w * 0.66f, h * 0.48f), strokeWidth = w * 0.10f)
                drawLine(color.copy(alpha = 0.8f), Offset(w * 0.82f, h * 0.68f), Offset(w * 0.22f, h * 0.68f), strokeWidth = w * 0.10f)
                if (goalId == "lucky_trade") node(0.50f, 0.50f, w * 0.08f)
            }
            "assistant" -> {
                drawCircle(color.copy(alpha = 0.20f), w * 0.24f, Offset(w * 0.42f, h * 0.42f), style = stroke)
                drawLine(color, Offset(w * 0.60f, h * 0.60f), Offset(w * 0.82f, h * 0.82f), strokeWidth = w * 0.10f)
                node(0.76f, 0.22f, w * 0.07f)
            }
            "pvp_candidates", "nundo_finder" -> {
                drawCircle(color.copy(alpha = 0.18f), w * 0.35f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(color.copy(alpha = 0.38f), w * if (goalId == "nundo_finder") 0.18f else 0.08f, Offset(w * 0.5f, h * 0.5f), style = if (goalId == "nundo_finder") stroke else Stroke(width = w * 0.2f))
                drawLine(color, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.30f), strokeWidth = w * 0.08f)
                drawLine(color, Offset(w * 0.14f, h * 0.50f), Offset(w * 0.30f, h * 0.50f), strokeWidth = w * 0.08f)
            }
            "events" -> {
                drawRoundRect(color.copy(alpha = 0.24f), Offset(w * 0.20f, h * 0.20f), Size(w * 0.56f, h * 0.56f), CornerRadius(w * 0.10f), style = stroke)
                node(0.68f, 0.68f, w * 0.10f)
            }
            "presets", "my_presets" -> {
                drawRoundRect(color.copy(alpha = 0.22f), Offset(w * 0.24f, h * 0.18f), Size(w * 0.46f, h * 0.64f), CornerRadius(w * 0.08f), style = stroke)
                node(if (goalId == "my_presets") 0.72f else 0.38f, 0.34f, w * 0.07f)
                drawLine(color, Offset(w * 0.36f, h * 0.54f), Offset(w * 0.62f, h * 0.54f), strokeWidth = w * 0.08f)
            }
            "expert" -> {
                drawLine(color, Offset(w * 0.25f, h * 0.30f), Offset(w * 0.75f, h * 0.30f), strokeWidth = w * 0.08f)
                drawLine(color, Offset(w * 0.25f, h * 0.55f), Offset(w * 0.75f, h * 0.55f), strokeWidth = w * 0.08f)
                node(0.40f, 0.30f, w * 0.08f); node(0.62f, 0.55f, w * 0.08f)
            }
            "hundo_check" -> {
                drawCircle(color.copy(alpha = 0.22f), w * 0.30f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawLine(color, Offset(w * 0.34f, h * 0.52f), Offset(w * 0.46f, h * 0.66f), strokeWidth = w * 0.10f)
                drawLine(color, Offset(w * 0.46f, h * 0.66f), Offset(w * 0.72f, h * 0.34f), strokeWidth = w * 0.10f)
            }
            else -> {
                drawCircle(color.copy(alpha = 0.22f), w * 0.30f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                node(0.5f, 0.5f, w * 0.08f)
            }
        }
    }
}
