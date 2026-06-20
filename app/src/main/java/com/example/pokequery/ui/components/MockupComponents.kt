package com.example.pokequery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.theme.*

// 1. NightMapBackground
@Composable
fun NightMapBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF030D1B), Color(0xFF0A1B38))))) {
        val w = size.width
        val h = size.height

        // Abstract hills
        val hillPath = Path().apply {
            moveTo(0f, h * 0.8f)
            quadraticTo(w * 0.3f, h * 0.6f, w * 0.7f, h * 0.85f)
            quadraticTo(w * 0.9f, h * 0.9f, w, h * 0.8f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(hillPath, color = Color(0xFF061428))

        // Curved route lines
        val route1 = Path().apply {
            moveTo(0f, h * 0.2f)
            quadraticTo(w * 0.4f, h * 0.3f, w * 0.8f, h * 0.1f)
        }
        val route2 = Path().apply {
            moveTo(w * 0.2f, h)
            quadraticTo(w * 0.5f, h * 0.6f, w, h * 0.4f)
        }
        drawPath(route1, color = TealPrimary.copy(alpha = 0.15f), style = Stroke(width = 6f))
        drawPath(route2, color = TealPrimary.copy(alpha = 0.15f), style = Stroke(width = 6f))

        // Waypoint dots
        val dots = listOf(
            Offset(w * 0.2f, h * 0.23f),
            Offset(w * 0.6f, h * 0.18f),
            Offset(w * 0.35f, h * 0.78f),
            Offset(w * 0.8f, h * 0.52f)
        )
        dots.forEach { dot ->
            drawCircle(color = TealPrimary.copy(alpha = 0.4f), radius = 12f, center = dot)
            drawCircle(color = TealPrimary, radius = 6f, center = dot)
        }
    }
}

// 2. OnboardingHeroCompose
@Composable
fun OnboardingHeroCompose(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2

            // Radar rings
            for (i in 1..4) {
                drawCircle(
                    color = TealPrimary.copy(alpha = 0.1f * (5 - i)),
                    radius = w * 0.15f * i,
                    center = Offset(cx, cy),
                    style = Stroke(width = 4f)
                )
            }

            // Shield emblem
            val shieldW = w * 0.4f
            val shieldH = h * 0.6f
            val shieldPath = Path().apply {
                moveTo(cx, cy - shieldH/2)
                lineTo(cx + shieldW/2, cy - shieldH/3)
                quadraticTo(cx + shieldW/2, cy + shieldH/3, cx, cy + shieldH/2)
                quadraticTo(cx - shieldW/2, cy + shieldH/3, cx - shieldW/2, cy - shieldH/3)
                close()
            }
            drawPath(shieldPath, brush = Brush.verticalGradient(listOf(BlueCTA, TealPrimary)))
            
            // Inner dark shield
            val innerShieldPath = Path().apply {
                moveTo(cx, cy - shieldH/2 + 10f)
                lineTo(cx + shieldW/2 - 10f, cy - shieldH/3 + 5f)
                quadraticTo(cx + shieldW/2 - 10f, cy + shieldH/3 - 10f, cx, cy + shieldH/2 - 10f)
                quadraticTo(cx - shieldW/2 + 10f, cy + shieldH/3 - 10f, cx - shieldW/2 + 10f, cy - shieldH/3 + 5f)
                close()
            }
            drawPath(innerShieldPath, color = CardDark.copy(alpha = 0.9f))
        }
        
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
    }
}

// 3. HomeMapHeaderCompose
@Composable
fun HomeMapHeaderCompose(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().background(Color(0xFF030D1B))) {
        val w = size.width
        val h = size.height

        val route = Path().apply {
            moveTo(0f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.2f, w, h * 0.6f)
        }
        drawPath(route, color = TealPrimary.copy(alpha = 0.2f), style = Stroke(width = 8f))

        val dots = listOf(Offset(w * 0.15f, h * 0.44f), Offset(w * 0.65f, h * 0.35f))
        dots.forEach { dot ->
            drawCircle(color = TealPrimary.copy(alpha = 0.3f), radius = 16f, center = dot)
            drawCircle(color = TealPrimary, radius = 8f, center = dot)
        }
    }
}

// 4. RiskHeaderCardCompose
@Composable
fun RiskHeaderCardCompose(riskLevel: String, subtitle: String, color: Color) {
    Box(
        modifier = Modifier.fillMaxWidth().height(120.dp).background(CardPremium, RoundedCornerShape(16.dp)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        // Decorative radar rings on the right
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.85f
            val cy = h / 2
            
            clipRect(right = w, bottom = h) {
                drawCircle(color.copy(alpha = 0.05f), radius = h * 0.4f, center = Offset(cx, cy), style = Stroke(6f))
                drawCircle(color.copy(alpha = 0.1f), radius = h * 0.7f, center = Offset(cx, cy), style = Stroke(6f))
                drawCircle(color.copy(alpha = 0.15f), radius = h, center = Offset(cx, cy), style = Stroke(6f))
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(16.dp)) {
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

// 5. GoalCardVisualIcon (Just using Material icons via GoalCardGrid)
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
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardPremium)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, iconTint.copy(alpha = 0.8f), Color.Transparent))).align(Alignment.TopCenter)
            )
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                Text(text = title, color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

// 6. ProtectedChipGrid
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProtectedChipGrid(protections: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        protections.forEach { protection ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(CardDark, RoundedCornerShape(50)).border(1.dp, BorderDark, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).background(TealPrimary, RoundedCornerShape(50)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = protection, color = TextPrimary, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            }
        }
    }
}

// 7. SearchStringPanel
@Composable
fun SearchStringPanel(query: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(CardDark, RoundedCornerShape(12.dp)).border(1.dp, BorderDark, RoundedCornerShape(12.dp)).padding(16.dp)
    ) {
        Text(text = query, color = TealPrimary, fontSize = 16.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

// 8. WarningInfoPanel
@Composable
fun WarningInfoPanel(title: String, message: String) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF3E2723), RoundedCornerShape(12.dp)).border(1.dp, AmberWarning.copy(alpha=0.5f), RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = AmberWarning, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = Color.White.copy(alpha=0.9f), fontSize = 14.sp)
    }
}

// 9. CopyCTA
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

// 10. ExplanationCard
@Composable
fun ExplanationCard(explanation: String) {
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(12.dp)).border(1.dp, BorderDark, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Text("What does this do?", color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = explanation, color = TextSecondary, fontSize = 14.sp)
    }
}

// 11. KnowledgeTermCard
@Composable
fun KnowledgeTermCard(
    syntax: String,
    tier: String,
    risk: String,
    description: String,
    quirks: String,
    source: String,
    lastVerified: String
) {
    val riskColor = if (risk == "High") CoralDanger else if (risk == "Medium") AmberWarning else TealPrimary
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(12.dp)).border(1.dp, BorderDark, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(syntax, color = riskColor, fontSize = 16.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Box(modifier = Modifier.background(riskColor.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("$tier · $risk", color = riskColor, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(description, color = TextSecondary, fontSize = 14.sp)
        if (quirks != "null" && quirks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Note: $quirks", color = AmberWarning, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Source: $source", color = TextSecondary, fontSize = 11.sp)
        Text("Last verified: $lastVerified", color = TextSecondary, fontSize = 11.sp)
    }
}

// 12. ExpertEditorPanel
@Composable
fun ExpertEditorPanel(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().background(CardDark, RoundedCornerShape(12.dp)),
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 16.sp, color = TealPrimary),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderDark),
        placeholder = { Text("e.g. 4*&!shiny", color = TextSecondary) }
    )
}

// 13. EmptyFavoritesPanel (Compose only)
@Composable
fun EmptyFavoritesPanel() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(100.dp).background(CardDark, RoundedCornerShape(50.dp)).border(2.dp, BorderDark, RoundedCornerShape(50.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("No saved search strings.", color = TextSecondary)
        }
    }
}

// 14. SettingsCard
@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(12.dp)).border(1.dp, BorderDark, RoundedCornerShape(12.dp)).padding(16.dp), content = content)
}
