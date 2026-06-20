package com.example.pokequery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.theme.*

@Composable
fun NightMapBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        val w = size.width
        val h = size.height

        // Background gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF030D1B),
                    Color(0xFF0A1B38)
                )
            )
        )

        // Route paths
        val path1 = Path().apply {
            moveTo(0f, h * 0.4f)
            quadraticBezierTo(w * 0.3f, h * 0.3f, w * 0.5f, h * 0.5f)
            quadraticBezierTo(w * 0.8f, h * 0.7f, w, h * 0.6f)
        }
        drawPath(path1, color = TealPrimary.copy(alpha = 0.2f), style = Stroke(width = 6f))
        
        val path2 = Path().apply {
            moveTo(w * 0.2f, h)
            quadraticBezierTo(w * 0.4f, h * 0.8f, w * 0.6f, h * 0.4f)
            quadraticBezierTo(w * 0.7f, h * 0.2f, w, h * 0.1f)
        }
        drawPath(path2, color = TealPrimary.copy(alpha = 0.15f), style = Stroke(width = 4f))

        // Location pins
        val pins = listOf(
            Offset(w * 0.15f, h * 0.35f),
            Offset(w * 0.45f, h * 0.45f),
            Offset(w * 0.85f, h * 0.65f),
            Offset(w * 0.65f, h * 0.35f)
        )
        for (pin in pins) {
            drawCircle(color = TealPrimary.copy(alpha = 0.5f), radius = 12f, center = pin)
            drawCircle(color = TealPrimary.copy(alpha = 0.2f), radius = 24f, center = pin)
            drawCircle(color = TealPrimary, radius = 6f, center = pin)
        }
    }
}

@Composable
fun HeroIllustrationPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w/2, h/2)

            // Glowing ring base
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TealPrimary.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w/2, h*0.8f),
                    radius = w * 0.6f
                ),
                center = Offset(w/2, h*0.8f)
            )
            
            // Central shield outline
            val shieldPath = Path().apply {
                moveTo(w/2, h*0.1f)
                lineTo(w*0.8f, h*0.2f)
                quadraticBezierTo(w*0.8f, h*0.6f, w/2, h*0.9f)
                quadraticBezierTo(w*0.2f, h*0.6f, w*0.2f, h*0.2f)
                close()
            }
            drawPath(shieldPath, brush = Brush.verticalGradient(listOf(BlueCTA, TealPrimary)))
            
            // Inner shield
            val innerShieldPath = Path().apply {
                moveTo(w/2, h*0.15f)
                lineTo(w*0.75f, h*0.23f)
                quadraticBezierTo(w*0.75f, h*0.58f, w/2, h*0.85f)
                quadraticBezierTo(w*0.25f, h*0.58f, w*0.25f, h*0.23f)
                close()
            }
            drawPath(innerShieldPath, color = CardDark.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun PremiumGoalCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardPremium)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle top glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, iconTint.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
                    .align(Alignment.TopCenter)
            )
            
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                Text(text = title, color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun ProtectedCategoryChip(text: String, color: Color = TealPrimary) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(CardPremium, RoundedCornerShape(50))
            .border(1.dp, BorderDark, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Mocking the checkmark
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color, radius = 6f)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = TextPrimary, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
    }
}

@Composable
fun RiskHeroHeader(riskLevel: String, subtitle: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardPremium, RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        // Internal Radar background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.8f, size.height * 0.5f)
            drawCircle(color = color.copy(alpha = 0.05f), radius = 100f, center = center)
            drawCircle(color = color.copy(alpha = 0.05f), radius = 200f, center = center, style = Stroke(width = 2f))
            drawCircle(color = color.copy(alpha = 0.05f), radius = 300f, center = center, style = Stroke(width = 2f))
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Simplified icon
                Canvas(modifier = Modifier.size(24.dp)) {
                    val path = Path().apply {
                        moveTo(size.width/2, 0f)
                        lineTo(size.width, size.height*0.2f)
                        lineTo(size.width*0.8f, size.height)
                        lineTo(size.width*0.2f, size.height)
                        lineTo(0f, size.height*0.2f)
                        close()
                    }
                    drawPath(path, color = color)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = riskLevel, color = color, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = 18.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}
