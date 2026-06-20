package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.components.MapBackdrop
import com.caglar.pokequery.ui.components.PremiumPanel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(initialPage: Int = 0, onStart: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        MapBackdrop(Modifier.matchParentSize(), imageAlpha = 0.48f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onStart) { Text("Skip", color = TextSecondary, fontWeight = FontWeight.Bold) }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> OnboardingHeroPage()
                    1 -> OnboardingLargeCardPage(
                        title = "Build the right search in seconds",
                        description = "Use safe defaults for cleanup, candy prep, trading, PvP checks, Hundos and Nundos.",
                        goalId = "candy_prep",
                        accent = AmberWarning,
                        imageRes = R.drawable.candy_prep_header
                    )
                    else -> OnboardingLargeCardPage(
                        title = "Copy-only and offline-first",
                        description = "PokeQuery creates text only. No account login, no scraping, no connection to the game.",
                        goalId = "safe_cleanup",
                        accent = TealPrimary,
                        imageRes = R.drawable.safe_cleanup_header,
                        showTrustRows = true
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == index) BlueCTA else TextSecondary.copy(alpha = 0.45f))
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == 2) {
                            onStart()
                        } else {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.height(58.dp).widthIn(min = 152.dp)
                ) {
                    Text(if (pagerState.currentPage == 2) "Start building" else "Next", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OnboardingHeroPage() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(18.dp))
        Image(
            painter = painterResource(R.drawable.logo_wordmark_source),
            contentDescription = "PokeQuery",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text("Safe search strings for Pokémon GO", color = TextSecondary, fontSize = 16.sp, textAlign = TextAlign.Center)
        Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp)) {
            Image(
                painter = painterResource(R.drawable.onboarding_hero_scene),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(Modifier.matchParentSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, BackgroundDark))))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TrustFeature(Icons.Default.Search, "Powerful", "Searches", Modifier.weight(1f))
            TrustFeature(Icons.Default.Lock, "Protected", "Defaults", Modifier.weight(1f))
            TrustFeature(Icons.Default.CheckCircle, "Keep", "Value", Modifier.weight(1f))
        }
    }
}

@Composable
private fun OnboardingLargeCardPage(
    title: String,
    description: String,
    goalId: String,
    accent: Color,
    imageRes: Int,
    showTrustRows: Boolean = false
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        PremiumPanel(borderColor = accent, modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(22.dp))
            )
            Spacer(Modifier.height(12.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, textAlign = TextAlign.Center, lineHeight = 34.sp)
            Spacer(Modifier.height(10.dp))
            Text(description, color = TextSecondary, fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        }
        if (showTrustRows) {
            Spacer(Modifier.height(16.dp))
            TrustStrip("Private", "Offline-first", "Copy-only")
        }
    }
}

@Composable
private fun TrustFeature(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardPremium.copy(alpha = 0.86f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(8.dp))
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
        Text(subtitle, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TrustStrip(vararg labels: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        labels.forEach {
            Text(
                it,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(CardPremium.copy(alpha = 0.92f))
                    .padding(vertical = 12.dp)
            )
        }
    }
}
