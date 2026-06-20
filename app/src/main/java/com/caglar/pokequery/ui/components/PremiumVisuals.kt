package com.caglar.pokequery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapBackdrop(modifier: Modifier = Modifier, imageAlpha: Float = 0.34f, imageRes: Int = R.drawable.v033_home_header_bg) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(Color(0xFF04152D), Color(0xFF07264C), BackgroundDark)
            )
        )
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = imageAlpha,
            modifier = Modifier.matchParentSize()
        )
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            repeat(5) { i ->
                val y = h * (0.22f + i * 0.13f)
                drawLine(
                    color = BlueCTA.copy(alpha = 0.20f),
                    start = Offset(w * -0.1f, y),
                    end = Offset(w * 1.1f, y + if (i % 2 == 0) h * 0.10f else -h * 0.08f),
                    strokeWidth = 2.5f
                )
            }
            listOf(0.16f to 0.42f, 0.48f to 0.30f, 0.82f to 0.46f).forEach { (x, y) ->
                val c = Offset(w * x, h * y)
                drawCircle(BlueCTA.copy(alpha = 0.24f), h * 0.055f, c)
                drawCircle(BlueCTA, h * 0.018f, c)
                drawLine(AmberWarning.copy(alpha = 0.55f), c, Offset(c.x, c.y + h * 0.08f), 3f)
            }
        }
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.88f)))))
    }
}

@Composable
fun ScreenTitleBar(title: String, onBack: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun PremiumPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderDark,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(CardPremium, CardDark)))
            .border(1.dp, borderColor.copy(alpha = 0.75f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun HeroSearchShield(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val accent = TealPrimary
        drawCircle(BlueCTA.copy(alpha = 0.20f), w * 0.36f, Offset(w * 0.50f, h * 0.58f))
        drawCircle(TealPrimary.copy(alpha = 0.13f), w * 0.24f, Offset(w * 0.50f, h * 0.58f), style = Stroke(w * 0.02f))

        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.14f)
            lineTo(w * 0.24f, h * 0.25f)
            lineTo(w * 0.24f, h * 0.50f)
            cubicTo(w * 0.25f, h * 0.72f, w * 0.50f, h * 0.86f, w * 0.50f, h * 0.86f)
            cubicTo(w * 0.50f, h * 0.86f, w * 0.75f, h * 0.72f, w * 0.76f, h * 0.50f)
            lineTo(w * 0.76f, h * 0.25f)
            close()
        }
        drawPath(shield, Brush.verticalGradient(listOf(Color(0xFF1EE8D6), Color(0xFF0D6EFD)), 0f, h))
        drawPath(shield, Color.White.copy(alpha = 0.55f), style = Stroke(w * 0.018f))
        drawCircle(Color.White.copy(alpha = 0.20f), w * 0.14f, Offset(w * 0.48f, h * 0.47f), style = Stroke(w * 0.036f))
        drawLine(Color.White.copy(alpha = 0.85f), Offset(w * 0.57f, h * 0.57f), Offset(w * 0.70f, h * 0.70f), w * 0.045f)
        drawCircle(Color.White.copy(alpha = 0.90f), w * 0.07f, Offset(w * 0.48f, h * 0.47f), style = Stroke(w * 0.03f))
        drawPath(starPath(Offset(w * 0.78f, h * 0.18f), w * 0.055f, w * 0.023f), AmberWarning)
        drawPath(starPath(Offset(w * 0.21f, h * 0.73f), w * 0.038f, w * 0.016f), accent)
    }
}

@Composable
fun GoalArt(goalId: String, accent: Color, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val c = Offset(w * 0.54f, h * 0.54f)
            drawCircle(accent.copy(alpha = 0.18f), w * 0.40f, c)
            drawCircle(accent.copy(alpha = 0.22f), w * 0.28f, c, style = Stroke(w * 0.025f))

            drawOval(
                Brush.verticalGradient(listOf(accent.copy(alpha = 0.95f), Color(0xFF102B58)), 0f, h),
                topLeft = Offset(w * 0.35f, h * 0.36f),
                size = Size(w * 0.35f, h * 0.42f)
            )
            drawCircle(Color.White.copy(alpha = 0.86f), w * 0.028f, Offset(w * 0.48f, h * 0.48f))
            drawCircle(Color.White.copy(alpha = 0.86f), w * 0.028f, Offset(w * 0.59f, h * 0.48f))
            drawPath(Path().apply {
                moveTo(w * 0.38f, h * 0.38f)
                lineTo(w * 0.30f, h * 0.22f)
                lineTo(w * 0.47f, h * 0.33f)
                close()
            }, accent.copy(alpha = 0.72f))
            drawPath(Path().apply {
                moveTo(w * 0.67f, h * 0.38f)
                lineTo(w * 0.78f, h * 0.24f)
                lineTo(w * 0.61f, h * 0.33f)
                close()
            }, accent.copy(alpha = 0.72f))

            when (goalId) {
                "candy_prep" -> {
                    drawCircle(AmberWarning, w * 0.11f, Offset(w * 0.25f, h * 0.30f))
                    drawCircle(AmberWarning.copy(alpha = 0.75f), w * 0.09f, Offset(w * 0.78f, h * 0.68f))
                }
                "trade_fodder", "lucky_trade" -> {
                    drawLine(BlueCTA, Offset(w * 0.22f, h * 0.66f), Offset(w * 0.45f, h * 0.54f), w * 0.045f)
                    drawLine(AmberWarning, Offset(w * 0.84f, h * 0.40f), Offset(w * 0.62f, h * 0.54f), w * 0.045f)
                    drawPath(starPath(Offset(w * 0.76f, h * 0.24f), w * 0.055f, w * 0.022f), AmberWarning)
                }
                "hundo_check", "nundo_finder" -> {
                    repeat(3) { drawCircle(PurpleIV.copy(alpha = 0.35f), w * (0.13f + it * 0.06f), Offset(w * 0.25f, h * 0.33f), style = Stroke(w * 0.018f)) }
                    drawPath(starPath(Offset(w * 0.78f, h * 0.26f), w * 0.070f, w * 0.030f), PurpleIV)
                }
                "pvp_candidates" -> {
                    drawRoundRect(BlueCTA.copy(alpha = 0.72f), Offset(w * 0.17f, h * 0.60f), Size(w * 0.24f, h * 0.16f), CornerRadius(w * 0.04f))
                    drawRoundRect(AmberWarning.copy(alpha = 0.72f), Offset(w * 0.68f, h * 0.26f), Size(w * 0.18f, h * 0.26f), CornerRadius(w * 0.04f))
                }
                "expert", "presets" -> {
                    repeat(3) { i ->
                        val y = h * (0.30f + i * 0.17f)
                        drawLine(TealPrimary.copy(alpha = 0.85f), Offset(w * 0.18f, y), Offset(w * 0.84f, y), w * 0.025f)
                        drawCircle(if (i == 1) AmberWarning else BlueCTA, w * 0.045f, Offset(w * (0.36f + i * 0.16f), y))
                    }
                }
                else -> {
                    val shield = Path().apply {
                        moveTo(w * 0.23f, h * 0.28f)
                        lineTo(w * 0.10f, h * 0.34f)
                        lineTo(w * 0.10f, h * 0.49f)
                        cubicTo(w * 0.11f, h * 0.61f, w * 0.23f, h * 0.70f, w * 0.23f, h * 0.70f)
                        cubicTo(w * 0.23f, h * 0.70f, w * 0.36f, h * 0.61f, w * 0.37f, h * 0.49f)
                        lineTo(w * 0.37f, h * 0.34f)
                        close()
                    }
                    drawPath(shield, TealPrimary.copy(alpha = 0.80f))
                }
            }
        }
    }
}

@Composable
fun RiskHeader(riskLevel: RiskLevel, subtitle: String, modifier: Modifier = Modifier, imageRes: Int? = null) {
    val tone = riskLevel.toneColor()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(CardPremium)
            .border(1.dp, tone.copy(alpha = 0.82f), RoundedCornerShape(22.dp))
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.78f,
                modifier = Modifier.matchParentSize()
            )
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(CardPremium, CardPremium.copy(alpha = 0.78f), Color.Transparent))))
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.42f)))))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(16.dp)) {
            HeroSearchShield(Modifier.size(66.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (riskLevel == RiskLevel.Info) "Inspection only" else "${riskLevel.name} Risk",
                    color = tone,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Text(subtitle, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

fun RiskLevel.toneColor(): Color = when (this) {
    RiskLevel.Info -> BlueCTA
    RiskLevel.Low -> TealPrimary
    RiskLevel.Medium -> AmberWarning
    RiskLevel.High -> CoralDanger
}

fun goalHeaderRes(goalId: String): Int = when (goalId) {
    "safe_cleanup", "untagged" -> R.drawable.v033_safe_cleanup_header
    "candy_prep" -> R.drawable.v033_candy_prep_header
    "trade_fodder" -> R.drawable.v033_trade_fodder_header
    "lucky_trade" -> R.drawable.v033_lucky_trade_header
    "pvp_candidates" -> R.drawable.v033_pvp_header
    "hundo_check", "nundo_finder" -> R.drawable.v033_nundo_header
    else -> R.drawable.v033_safe_cleanup_header
}

private fun starPath(center: Offset, outer: Float, inner: Float): Path {
    val p = Path()
    repeat(10) { i ->
        val r = if (i % 2 == 0) outer else inner
        val a = -PI / 2 + i * PI / 5
        val point = Offset(center.x + cos(a).toFloat() * r, center.y + sin(a).toFloat() * r)
        if (i == 0) p.moveTo(point.x, point.y) else p.lineTo(point.x, point.y)
    }
    p.close()
    return p
}
