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
import androidx.compose.ui.graphics.Brush
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
import com.caglar.pokequery.ui.components.PremiumPanel
import com.caglar.pokequery.ui.motion.pqSpringPop
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(initialPage: Int = 0, onStart: () -> Unit) {
    val pageCount = com.caglar.pokequery.onboarding.OnboardingContent.pageCount
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BackgroundDark, SlateBlack, BackgroundDark)))) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 14.dp), contentAlignment = Alignment.TopEnd) {
                TextButton(onClick = onStart) { Text(androidx.compose.ui.res.stringResource(R.string.onboarding_skip), color = TextSecondary, fontWeight = FontWeight.Bold) }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                // Simplified to 2 pages
                when (page) {
                    0 -> OnboardingHeroPage()
                    else -> OnboardingLargeCardPage(
                        title = androidx.compose.ui.res.stringResource(R.string.onboarding_card2_title),
                        description = androidx.compose.ui.res.stringResource(R.string.onboarding_card2_desc),
                        goalId = "safe_cleanup",
                        accent = TealPrimary,
                        imageRes = R.drawable.safe_cleanup_header,
                        showTrustRows = false
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Removed dots to make it cleaner since it's just 2 pages
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == pageCount - 1) {
                            onStart()
                        } else {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.height(58.dp).widthIn(min = 152.dp)
                ) {
                    Text(if (pagerState.currentPage == pageCount - 1) androidx.compose.ui.res.stringResource(R.string.onboarding_start_building) else androidx.compose.ui.res.stringResource(R.string.onboarding_next), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OnboardingHeroPage() {
    // v0.5.3 motion polish: staggered entrance for the hero page. The wordmark + hero art get a
    // subtle spring-pop (icons/illustrations only); the tagline and trust chips fade+slide. The
    // entrance is driven by ONE hoisted `visible` flag, so it runs once and never replays.
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(18.dp))
        // v0.5.2 (Fix 2): use the vector PqWordmark instead of the raster logo_wordmark_source
        // WebP, which rendered as an opaque black block. Same brand treatment as Home (Fix 3).
        com.caglar.pokequery.ui.pq.PqWordmark(
            modifier = Modifier.fillMaxWidth()
                .pqStaggeredItem(visible, 0)
                .pqSpringPop(visible),
            width = 340.dp,
            centered = true
        )
        Spacer(Modifier.height(8.dp))
        Text(
            androidx.compose.ui.res.stringResource(R.string.onboarding_hero_tagline),
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.pqStaggeredItem(visible, 0)
        )
        // v0.5.4 (Fix 1): the hero is now a real WIDE asset (onboarding_hero_wide, 3:2) rendered
        // with ContentScale.Crop in an edge-to-edge rounded panel — not the square
        // onboarding_hero_scene tile it replaced. The panel keeps a navy background as a fallback
        // under any letterboxing, and a bottom fade blends the art into the page background.
        // `.weight()` is a ColumnScope call, so it is applied first; the motion modifiers follow.
        Box(
            Modifier.weight(1f).fillMaxWidth()
                .pqStaggeredItem(visible, 1)
                .pqSpringPop(visible),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(SlateBlack)
            ) {
                Image(
                    painter = painterResource(R.drawable.onboarding_hero_wide),
                    contentDescription = null,
                    // Crop (not FillWidth) so the wide art fills the panel edge-to-edge at any
                    // aspect ratio, matching the rest of the onboarding card treatment.
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
                // Subtle navy fade at the bottom so the hero blends into the background.
                Box(
                    Modifier.matchParentSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, BackgroundDark))
                    )
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp).pqStaggeredItem(visible, 2),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrustFeature(Icons.Default.Search, androidx.compose.ui.res.stringResource(R.string.onboarding_trust_powerful), androidx.compose.ui.res.stringResource(R.string.onboarding_trust_searches), Modifier.weight(1f))
            TrustFeature(Icons.Default.Lock, androidx.compose.ui.res.stringResource(R.string.onboarding_trust_protected), androidx.compose.ui.res.stringResource(R.string.onboarding_trust_defaults), Modifier.weight(1f))
            TrustFeature(Icons.Default.CheckCircle, androidx.compose.ui.res.stringResource(R.string.onboarding_trust_keep), androidx.compose.ui.res.stringResource(R.string.onboarding_trust_value), Modifier.weight(1f))
        }
    }
    }
}

// Package 3: renders the paste-flow steps and the risk-color legend from the tested
// OnboardingContent model. Uses a PremiumPanel for readability; no new art required.
@Composable
private fun OnboardingBulletsPage(
    title: String,
    bullets: List<String>,
    description: String,
    accent: Color
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp)
        Spacer(Modifier.height(24.dp))
        bullets.forEach { step ->
            Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 8.dp).size(6.dp).clip(CircleShape).background(accent))
                Spacer(Modifier.width(16.dp))
                Text(step, color = TextSecondary, fontSize = 16.sp, lineHeight = 24.sp)
            }
        }
        if (description.isNotBlank()) {
            Spacer(Modifier.height(24.dp))
            Text(description, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(start = 22.dp))
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
    // v0.5.3 motion polish: each large-card page gets its own one-shot entrance so the hero art
    // spring-pops when the page is swiped in. Driven by a per-page hoisted flag (one page = one
    // entrance), so swiping back and forth replays cleanly and never loops.
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().pqSpringPop(visible)
        )
        // Dark gradient overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.6f), BackgroundDark),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, textAlign = TextAlign.Center, lineHeight = 36.sp, modifier = Modifier.pqStaggeredItem(visible, 0))
            Spacer(Modifier.height(14.dp))
            Text(description, color = TextSecondary, fontSize = 17.sp, textAlign = TextAlign.Center, lineHeight = 24.sp, modifier = Modifier.pqStaggeredItem(visible, 1))
            if (showTrustRows) {
                Spacer(Modifier.height(24.dp))
                TrustStrip(
                    androidx.compose.ui.res.stringResource(R.string.onboarding_trust_strip_private),
                    androidx.compose.ui.res.stringResource(R.string.onboarding_trust_strip_offline),
                    androidx.compose.ui.res.stringResource(R.string.onboarding_trust_strip_copy)
                )
            }
        }
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
