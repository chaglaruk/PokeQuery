package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.GoalCardGrid

@Composable
fun HomeScreen(onGoalSelected: (String) -> Unit) {
    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                PremiumHeader(
                    onQuickCleanup = { onGoalSelected("safe_cleanup") },
                    onExpert = { onGoalSelected("expert") }
                )
            }

            item { SectionHeader("Quick Actions") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ActionCard("Safe Cleanup", "Exclude rare", R.drawable.goal_safe_cleanup_icon, TealPrimary, { onGoalSelected("safe_cleanup") })
                    }
                    item {
                        ActionCard("2x Candy", "Duplicates", R.drawable.goal_candy_prep_icon, AmberWarning, { onGoalSelected("candy_prep") })
                    }
                    item {
                        ActionCard("Trade Fodder", "Non-shiny", R.drawable.goal_trade_icon, BlueCTA, { onGoalSelected("trade_fodder") })
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionHeader("Cleanup & Sorting") }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ListGoalCard("Untagged Cleanup", "Find Pokémon missing tags to organize your storage", R.drawable.goal_tag_icon, TealPrimary) { onGoalSelected("untagged") }
                    ListGoalCard("Lucky Trade Prep", "Find older Pokémon or distance trades", R.drawable.goal_trade_icon, AmberWarning) { onGoalSelected("lucky_trade") }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionHeader("Battle & IV") }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ListGoalCard("Hundo Check", "Find perfect 4★ Pokémon instantly", R.drawable.goal_hundo_icon, Color(0xFF9C27B0)) { onGoalSelected("hundo_check") }
                    ListGoalCard("Nundo Finder", "Find exact 0% IV Pokémon", R.drawable.goal_hundo_icon, Color(0xFF607D8B)) { onGoalSelected("nundo_finder") }
                    ListGoalCard("PvP Candidates", "Find high bulk Great/Ultra League IVs", R.drawable.goal_expert_icon, BlueCTA) { onGoalSelected("pvp_candidates") }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionHeader("Power User") }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ListGoalCard("Expert Builder", "Write raw strings with our safety linter", R.drawable.goal_expert_icon, BlueCTA) { onGoalSelected("expert") }
                    ListGoalCard("Popular Presets", "Browse common community search strings", R.drawable.goal_expert_icon, TealPrimary) { onGoalSelected("presets") }
                }
            }
        }
    }
}

@Composable
private fun PremiumHeader(onQuickCleanup: () -> Unit, onExpert: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color(0xFF061B36))
    ) {
        // Subtle background texture
        Image(
            painter = painterResource(id = R.drawable.home_header_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.2f
        )
        
        // Gradient overlay
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF030D1B)))))

        Column(modifier = Modifier.padding(24.dp).padding(top = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App Logo Placeholder (Shield + Search)
                Box(modifier = Modifier.size(40.dp).background(TealPrimary.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(id = R.drawable.goal_expert_icon), contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("PokeQuery", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Build safe Pokémon GO search strings in seconds.", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 34.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onQuickCleanup,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Quick Cleanup", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onExpert,
                    colors = ButtonDefaults.buttonColors(containerColor = CardPremium),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Expert Builder", color = Color.White, fontWeight = FontWeight.Bold)
                }
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
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(title: String, subtitle: String, iconRes: Int, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(140.dp).height(120.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(36.dp).background(color.copy(alpha=0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(painterResource(iconRes), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListGoalCard(title: String, subtitle: String, iconRes: Int, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(color.copy(alpha=0.15f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(painterResource(iconRes), contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
