package com.caglar.pokequery.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.theme.*
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(initialPage: Int = 0, onStart: () -> Unit) {
    val pageCount = com.caglar.pokequery.onboarding.OnboardingContent.pageCount
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val ctaAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = 400)
    )
    val ctaScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 350, delayMillis = 400)
    )

    Box(Modifier.fillMaxSize().background(BackgroundDark)) {
        OnboardingSignalScene(Modifier.fillMaxSize())
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.fillMaxWidth().statusBarsPadding().height(18.dp))

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                when (page) {
                    0 -> OnboardingHeroPage(visible)
                    else -> OnboardingLargeCardPage(
                        visible = visible,
                        title = stringResource(R.string.onboarding_card2_title),
                        description = stringResource(R.string.onboarding_card2_desc)
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
                // Page Indicator Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pageCount) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) TealPrimary else TextSecondary.copy(alpha = 0.5f))
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == pageCount - 1) {
                            onStart()
                        } else {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueCTA,
                        contentColor = SlateBlack
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .height(58.dp)
                        .widthIn(min = 200.dp)
                        .graphicsLayer {
                            alpha = ctaAlpha
                            scaleX = ctaScale
                            scaleY = ctaScale
                        }
                ) {
                    Text(
                        text = if (pagerState.currentPage == pageCount - 1) stringResource(R.string.onboarding_start_building) else stringResource(R.string.onboarding_next),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlateBlack
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateBlack)
                }
            }
        }
    }
}

@Composable
private fun OnboardingHeroPage(visible: Boolean) {
    val taglineAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 250),
        label = "taglineAlpha"
    )
    val cardsAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = 350),
        label = "cardsAlpha"
    )
    val cardsOffsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(durationMillis = 350, delayMillis = 350),
        label = "cardsOffsetY"
    )

    Box(Modifier.fillMaxSize()) {
        // Content overlay
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))
            // Logo wordmark
            com.caglar.pokequery.ui.pq.PqWordmark(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = taglineAlpha },
                width = 280.dp,
                centered = true
            )
            Spacer(Modifier.height(10.dp))
            // Tagline
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(TealPrimary.copy(alpha = 0.10f))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .graphicsLayer { alpha = taglineAlpha }
            ) {
                Text(
                    text = stringResource(R.string.onboarding_hero_tagline),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )
            }

            // Spacer pushes cards to bottom
            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = cardsAlpha
                        translationY = cardsOffsetY
                    },
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureCard(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.onboarding_feature_plan_title),
                    description = stringResource(R.string.onboarding_feature_plan_desc)
                )
                FeatureCard(
                    icon = Icons.Default.CloudOff,
                    title = stringResource(R.string.onboarding_feature_protect_title),
                    description = stringResource(R.string.onboarding_feature_protect_desc)
                )
                FeatureCard(
                    icon = Icons.Default.ContentCopy,
                    title = stringResource(R.string.onboarding_feature_copy_title),
                    description = stringResource(R.string.onboarding_feature_copy_desc)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OnboardingLargeCardPage(
    visible: Boolean,
    title: String,
    description: String
) {
    val textAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 200),
        label = "page2TextAlpha"
    )

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(6.dp))
            com.caglar.pokequery.ui.pq.PqWordmark(width = 200.dp, centered = true,
                modifier = Modifier.graphicsLayer { alpha = textAlpha })
            Spacer(Modifier.weight(0.3f))
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(CardPremium.copy(alpha = 0.72f))
                    .padding(18.dp)
                    .graphicsLayer { alpha = textAlpha }
            ) {
                Text(
                    text = description,
                    color = TextPrimary.copy(alpha = 0.88f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
            Spacer(Modifier.weight(0.7f))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardPremium.copy(alpha = 0.88f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun OnboardingSignalScene(
    modifier: Modifier = Modifier,
    variant: Int = 0
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(Color(0xFF06131F), BackgroundDark, Color(0xFF061A2A))
            )
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * if (variant == 0) 0.43f else 0.48f)
            val teal = TealPrimary.copy(alpha = 0.22f)
            val gold = GoldCaution.copy(alpha = 0.2f)

            drawCircle(teal, radius = w * 0.38f, center = center, style = Stroke(width = 3f))
            drawCircle(teal.copy(alpha = 0.14f), radius = w * 0.24f, center = center, style = Stroke(width = 2f))
            drawCircle(gold, radius = w * 0.08f, center = center)

            val pathPoints = listOf(
                Offset(w * 0.18f, h * 0.32f),
                Offset(w * 0.36f, h * 0.48f),
                Offset(w * 0.58f, h * 0.37f),
                Offset(w * 0.78f, h * 0.55f)
            )
            pathPoints.zipWithNext().forEach { (a, b) ->
                drawLine(TealPrimary.copy(alpha = 0.34f), a, b, strokeWidth = 5f)
            }
            pathPoints.forEachIndexed { index, point ->
                drawCircle(if (index % 2 == 0) TealPrimary else GoldCaution, 13f, point)
                drawCircle(Color.White.copy(alpha = 0.18f), 24f, point, style = Stroke(width = 2f))
            }

            drawCircle(TealPrimary.copy(alpha = 0.10f), radius = w * 0.18f, center = Offset(w * 0.2f, h * 0.78f))
            drawCircle(GoldCaution.copy(alpha = 0.08f), radius = w * 0.20f, center = Offset(w * 0.84f, h * 0.18f))
        }
    }
}
