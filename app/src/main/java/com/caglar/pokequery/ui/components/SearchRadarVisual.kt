package com.caglar.pokequery.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.ui.motion.rememberReducedMotion
import kotlin.math.cos
import kotlin.math.sin

/**
 * v0.6.9 — Integrated, IP-safe empty-state visual for Search Assistant.
 *
 * Design intent: blend AI assistance + Pokémon GO-style search + PokeQuery identity using
 * code-drawn primitives only (no images, no sprites, no official art). The result is a soft,
 * full-bleed scanning-radar motif with floating search-tag chips that feels native to the dark
 * app theme — not a pasted boxed poster.
 *
 * Honors [rememberReducedMotion]: when the user has disabled animation, the radar renders static.
 */
@Composable
fun SearchAssistantEmptyState(modifier: Modifier = Modifier) {
    val reducedMotion = rememberReducedMotion()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        TealPrimary.copy(alpha = 0.06f),
                        BackgroundDark.copy(alpha = 0.0f)
                    )
                )
            )
    ) {
        SearchRadarBackground(
            active = !reducedMotion,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchRadarCore(active = !reducedMotion)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.search_assistant_empty_state_title),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.search_assistant_empty_state_desc),
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Full-bleed subtle radar grid + sweeping wave behind the core. */
@Composable
private fun SearchRadarBackground(active: Boolean, modifier: Modifier = Modifier) {
    val sweep = if (active) {
        val transition = rememberInfiniteTransition(label = "radar_sweep")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sweep_angle"
        ).value
    } else {
        45f
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.42f
        val maxR = size.minDimension * 0.55f

        // Soft radar rings
        for (i in 1..3) {
            val r = maxR * (i / 3f)
            drawCircle(
                color = TealPrimary.copy(alpha = 0.10f - (i * 0.02f)),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
        }

        // Sweeping wave — a translucent wedge gradient.
        rotate(sweep, pivot = Offset(cx, cy)) {
            for (i in 0..12) {
                val a = (12 - i) * 0.018f
                drawLine(
                    color = CyanGlow.copy(alpha = a),
                    start = Offset(cx, cy),
                    end = Offset(
                        cx + maxR * cos(Math.toRadians(0.0)).toFloat(),
                        cy + maxR * sin(Math.toRadians(0.0)).toFloat()
                    ),
                    strokeWidth = (12 - i).toFloat(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Faint dot field suggesting search matches.
        val dots = listOf(
            Offset(cx - maxR * 0.5f, cy - maxR * 0.2f),
            Offset(cx + maxR * 0.4f, cy - maxR * 0.45f),
            Offset(cx + maxR * 0.55f, cy + maxR * 0.1f),
            Offset(cx - maxR * 0.35f, cy + maxR * 0.3f),
            Offset(cx + maxR * 0.1f, cy + maxR * 0.5f)
        )
        dots.forEachIndexed { i, p ->
            if (p.x in 0f..w && p.y in 0f..h) {
                drawCircle(
                    color = CyanGlow.copy(alpha = 0.35f - (i * 0.05f)),
                    radius = 3f,
                    center = p
                )
            }
        }
    }
}

/** Center radar node with pulsing core + crosshair, plus the AI sparkle. */
@Composable
private fun SearchRadarCore(active: Boolean) {
    val pulse = if (active) {
        val transition = rememberInfiniteTransition(label = "radar_pulse")
        transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        ).value
    } else {
        0.85f
    }

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.5f
            val coreR = size.minDimension * 0.16f

            // Pulsing halo
            drawCircle(
                color = TealPrimary.copy(alpha = 0.12f * pulse),
                radius = coreR * 2.4f,
                center = Offset(cx, cy)
            )

            // Crosshair lines
            val armLen = size.minDimension * 0.42f
            drawLine(
                color = TealPrimary.copy(alpha = 0.5f),
                start = Offset(cx - armLen, cy),
                end = Offset(cx + armLen, cy),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = TealPrimary.copy(alpha = 0.5f),
                start = Offset(cx, cy - armLen),
                end = Offset(cx, cy + armLen),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )

            // Outer ring
            drawCircle(
                color = TealPrimary.copy(alpha = 0.55f),
                radius = armLen,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )

            // Solid core node
            drawCircle(
                color = TealPrimary.copy(alpha = pulse),
                radius = coreR,
                center = Offset(cx, cy)
            )
        }
        // AI sparkle floating at top-right of the core.
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = CyanGlow,
            modifier = Modifier
                .size(22.dp)
                .align(Alignment.TopEnd)
                .padding(top = 6.dp)
        )
    }
}
