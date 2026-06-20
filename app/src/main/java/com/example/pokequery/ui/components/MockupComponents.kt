package com.example.pokequery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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

// 1. OnboardingHero (only used on Onboarding)
@Composable
fun OnboardingHero(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TealPrimary.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w/2, h*0.8f),
                    radius = w * 0.6f
                ),
                center = Offset(w/2, h*0.8f)
            )
            
            val shieldPath = Path().apply {
                moveTo(w/2, h*0.1f)
                lineTo(w*0.8f, h*0.2f)
                quadraticTo(w*0.8f, h*0.6f, w/2, h*0.9f)
                quadraticTo(w*0.2f, h*0.6f, w*0.2f, h*0.2f)
                close()
            }
            drawPath(shieldPath, brush = Brush.verticalGradient(listOf(BlueCTA, TealPrimary)))
            
            val innerShieldPath = Path().apply {
                moveTo(w/2, h*0.15f)
                lineTo(w*0.75f, h*0.23f)
                quadraticTo(w*0.75f, h*0.58f, w/2, h*0.85f)
                quadraticTo(w*0.25f, h*0.58f, w*0.25f, h*0.23f)
                close()
            }
            drawPath(innerShieldPath, color = CardDark.copy(alpha = 0.9f))
        }
    }
}

// 2. HomeMapHeader (used only on Home)
@Composable
fun HomeMapHeader(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF030D1B), Color(0xFF0A1B38))
                )
            )
            val path1 = Path().apply {
                moveTo(0f, h * 0.4f)
                quadraticTo(w * 0.3f, h * 0.3f, w * 0.5f, h * 0.5f)
                quadraticTo(w * 0.8f, h * 0.7f, w, h * 0.6f)
            }
            drawPath(path1, color = TealPrimary.copy(alpha = 0.2f), style = Stroke(width = 6f))
            
            val pins = listOf(Offset(w * 0.15f, h * 0.35f), Offset(w * 0.45f, h * 0.45f), Offset(w * 0.85f, h * 0.65f))
            for (pin in pins) {
                drawCircle(color = TealPrimary.copy(alpha = 0.5f), radius = 12f, center = pin)
                drawCircle(color = TealPrimary, radius = 6f, center = pin)
            }
        }
        Column(modifier = Modifier.padding(24.dp).align(Alignment.BottomStart)) {
            Text("What do you want to find?", color = Color.White, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Pick a goal and we'll build a safe search string for it.", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// 3. GoalCardGrid (used on Home)
@Composable
fun GoalCardGrid(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardPremium)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, iconTint.copy(alpha = 0.5f), Color.Transparent))).align(Alignment.TopCenter)
            )
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                Text(text = title, color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

// 4. RiskHeaderCard
@Composable
fun RiskHeaderCard(riskLevel: String, subtitle: String, color: Color) {
    Box(
        modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(20.dp)).border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(20.dp)).padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(if (color == AmberWarning) Icons.Default.Warning else Icons.Default.Info, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = riskLevel, color = color, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = 18.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

// 5. SearchStringPanel
@Composable
fun SearchStringPanel(query: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(CardDark, RoundedCornerShape(16.dp)).border(1.dp, BorderDark, RoundedCornerShape(16.dp)).padding(20.dp)
    ) {
        Text(text = query, color = TealPrimary, fontSize = 18.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

// 6. CopyCTA
@Composable
fun CopyCTA(color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Copy to Clipboard", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
    }
}

// 7. ExplanationCard
@Composable
fun ExplanationCard(explanation: String) {
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(16.dp)).border(1.dp, BorderDark, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Text("What does this do?", color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = explanation, color = TextSecondary, fontSize = 14.sp)
    }
}

// 8. ProtectedChipGrid
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProtectedChipGrid(protections: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        protections.forEach { protection ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(CardPremium, RoundedCornerShape(50)).border(1.dp, BorderDark, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Canvas(modifier = Modifier.size(12.dp)) { drawCircle(color = TealPrimary, radius = 6f) }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = protection, color = TextPrimary, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            }
        }
    }
}

// 9. WarningInfoPanel
@Composable
fun WarningInfoPanel(title: String, message: String) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF3E2723), RoundedCornerShape(16.dp)).border(1.dp, AmberWarning.copy(alpha=0.5f), RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = AmberWarning, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = Color.White, fontSize = 14.sp)
    }
}

// 10. KnowledgeTermCard
@Composable
fun KnowledgeTermCard(syntax: String, risk: String, description: String, quirks: String) {
    val riskColor = if (risk == "High") CoralDanger else if (risk == "Medium") AmberWarning else TealPrimary
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(16.dp)).border(1.dp, BorderDark, RoundedCornerShape(16.dp)).padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(syntax, color = riskColor, fontSize = 18.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Box(modifier = Modifier.background(riskColor.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(risk, color = riskColor, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(description, color = TextSecondary, fontSize = 14.sp)
        if (quirks != "null" && quirks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Note: $quirks", color = AmberWarning, fontSize = 12.sp)
        }
    }
}

// 11. ExpertEditorPanel
@Composable
fun ExpertEditorPanel(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().background(CardDark, RoundedCornerShape(16.dp)),
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 16.sp, color = TealPrimary),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderDark),
        placeholder = { Text("e.g. 4*&!shiny", color = TextSecondary) }
    )
}

// 12. EmptyFavoritesPanel
@Composable
fun EmptyFavoritesPanel() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp).padding(bottom = 16.dp))
            Text("No saved search strings.", color = TextSecondary)
        }
    }
}

// 13. SettingsCard
@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(16.dp)).border(1.dp, BorderDark, RoundedCornerShape(16.dp)).padding(16.dp), content = content)
}
