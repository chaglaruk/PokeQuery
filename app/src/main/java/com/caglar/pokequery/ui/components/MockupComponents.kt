package com.caglar.pokequery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.theme.*

// 1. RiskHeaderCardCompose
@Composable
fun RiskHeaderCardCompose(riskLevel: String, subtitle: String, color: Color, backgroundDrawableResId: Int? = null) {
    Box(
        modifier = Modifier.fillMaxWidth().height(120.dp).background(CardPremium, RoundedCornerShape(16.dp)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        if (backgroundDrawableResId != null) {
            Box(modifier = Modifier.matchParentSize().padding(start = 100.dp)) {
                Image(
                    painter = painterResource(id = backgroundDrawableResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.9f // Increased visibility from 0.7f
                )
                // Stronger gradient to fade the left edge smoothly into the solid background color
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(CardPremium, CardPremium.copy(alpha=0.6f), Color.Transparent))))
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Box(modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(if (color == AmberWarning) Icons.Default.Warning else Icons.Default.Info, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = riskLevel, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

// 2. GoalCardGrid
@Composable
fun GoalCardGrid(
    title: String,
    subtitle: String,
    iconResId: Int,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(160.dp), // Increased height to match reference
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardPremium)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Stronger gradient border glow
            Box(
                modifier = Modifier.fillMaxSize().border(2.dp, Brush.verticalGradient(listOf(iconTint.copy(alpha = 0.8f), iconTint.copy(alpha=0.2f), Color.Transparent)), RoundedCornerShape(20.dp))
            )
            // Stronger top accent background
            Box(
                modifier = Modifier.fillMaxWidth().height(70.dp).background(Brush.verticalGradient(listOf(iconTint.copy(alpha = 0.3f), Color.Transparent))).align(Alignment.TopCenter)
            )
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Larger icon
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).offset(y = (-4).dp), // Increased from 42dp by ~33%
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

// 3. ProtectedChipGrid
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
                Text(text = protection, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// 4. SearchStringPanel
@Composable
fun SearchStringPanel(query: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(CardDark, RoundedCornerShape(12.dp)).border(1.dp, BorderDark, RoundedCornerShape(12.dp)).padding(16.dp)
    ) {
        Text(text = query, color = TealPrimary, fontSize = 16.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

// 5. WarningInfoPanel
@Composable
fun WarningInfoPanel(title: String, message: String) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF3E2723), RoundedCornerShape(12.dp)).border(1.dp, AmberWarning.copy(alpha=0.5f), RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = Color.White.copy(alpha=0.9f), fontSize = 14.sp)
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
        Text("Copy to Clipboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// 7. ExplanationCard
@Composable
fun ExplanationCard(explanation: String) {
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(12.dp)).border(1.dp, BorderDark, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Text("What does this do?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = explanation, color = TextSecondary, fontSize = 14.sp)
    }
}

// 8. KnowledgeTermCard
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
            Text(syntax, color = riskColor, fontSize = 16.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.background(riskColor.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("$tier · $risk", color = riskColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

// 9. ExpertEditorPanel
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

// 10. EmptyFavoritesPanel
@Composable
fun EmptyFavoritesPanel() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Replaced image placeholder with polished Compose empty state
            Box(
                modifier = Modifier.size(100.dp).background(CardPremium, RoundedCornerShape(50.dp)).border(2.dp, BorderDark, RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("No saved search strings.", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your favorite queries will appear here.", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// 11. SettingsCard
@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(12.dp)).border(1.dp, BorderDark, RoundedCornerShape(12.dp)).padding(16.dp), content = content)
}

// 12. ScopeMeterPanel
@Composable
fun ScopeMeterPanel(scopeBreadth: String) {
    val meterColor = when (scopeBreadth) {
        "Very Narrow" -> Color(0xFF607D8B) // Grey-blue
        "Narrow" -> TealPrimary
        "Moderate" -> BlueCTA
        "Broad" -> AmberWarning
        "Very Broad" -> CoralDanger
        else -> BlueCTA
    }
    
    Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(12.dp)).border(1.dp, meterColor.copy(alpha=0.3f), RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Result Breadth", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Box(modifier = Modifier.background(meterColor.copy(alpha=0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(scopeBreadth, color = meterColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("This is a query breadth hint, not a live Pokémon count.", color = TextSecondary, fontSize = 12.sp)
    }
}
