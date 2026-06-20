package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.data.model.GeneratedString
import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.data.repository.UserPreferencesRepository
import com.example.pokequery.data.repository.dataStore
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.ProtectedCategoryChip
import com.example.pokequery.ui.components.RiskHeroHeader
import kotlinx.coroutines.launch

@Composable
fun PreviewScreen(
    generatedString: GeneratedString,
    onCopy: () -> Unit,
    onBack: () -> Unit
) {
    val riskColor = when (generatedString.riskLevel) {
        RiskLevel.Low -> TealPrimary
        RiskLevel.Medium -> AmberWarning
        RiskLevel.High -> CoralDanger
    }

    val riskSubtitle = when (generatedString.riskLevel) {
        RiskLevel.Low -> "Safe to use"
        RiskLevel.Medium -> "Review before using"
        RiskLevel.High -> "High risk of transferring valuables"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Top Bar
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text(text = "<- Back", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "Preview", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(64.dp))
        }

        // Risk Hero Header
        RiskHeroHeader(
            riskLevel = "${generatedString.riskLevel} Risk",
            subtitle = riskSubtitle,
            color = riskColor
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Your search string", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))

        // String Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark, RoundedCornerShape(16.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = generatedString.rawSyntax,
                color = riskColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(end = 32.dp)
            )
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val context = androidx.compose.ui.platform.LocalContext.current
        val repository = remember { UserPreferencesRepository(context.dataStore) }
        val scope = rememberCoroutineScope()

        val copyButtonColor = if (generatedString.riskLevel == RiskLevel.Low) BlueCTA else AmberWarning
        val copyTextColor = if (generatedString.riskLevel == RiskLevel.Low) Color.White else Color.Black

        Button(
            onClick = { 
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(generatedString.rawSyntax))
                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                onCopy() 
            },
            colors = ButtonDefaults.buttonColors(containerColor = copyButtonColor, contentColor = copyTextColor),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp).padding(end = 8.dp))
            Text("Copy", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Explanation
        Row(modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = BlueCTA, modifier = Modifier.size(20.dp).padding(end = 8.dp))
            Text(text = "What does this do?", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
        Text(text = generatedString.plainLanguageExplanation, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(bottom = 24.dp))
        
        if (generatedString.warnings.isNotEmpty()) {
            Row(modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp).padding(end = 8.dp))
                Text(text = "About count (important)", color = AmberWarning, fontWeight = FontWeight.SemiBold)
            }
            generatedString.warnings.forEach { warning ->
                Text(text = "• $warning", color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Tip
        Row(modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp).padding(end = 8.dp))
            Text(text = "Tip", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
        Text(text = "Spot-check a few results before mass transferring to be extra safe.", color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(bottom = 32.dp))

        // Protections
        Text(text = "Protected categories", color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
        
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            generatedString.excludedCategories.forEach { cat ->
                ProtectedCategoryChip(text = cat, color = TealPrimary)
            }
        }
    }
}
