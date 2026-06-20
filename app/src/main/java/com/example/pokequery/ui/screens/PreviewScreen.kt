package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.data.model.GeneratedString
import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.theme.*

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Top Bar Mock
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "<- Back", color = Color.White, modifier = Modifier.padding(end = 16.dp))
            Text(text = "Preview", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }

        // Risk Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = riskColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "${generatedString.riskLevel} Risk", color = riskColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(text = if (generatedString.riskLevel == RiskLevel.Low) "Safe to use" else "Review before using", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Your search string", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))

        // String Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = generatedString.rawSyntax,
                color = TealPrimary,
                fontFamily = FontFamily.Monospace,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onCopy,
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Copy", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Explanation
        Text(text = "What does this do?", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = generatedString.plainLanguageExplanation, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Protections
        Text(text = "Protected categories", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
        
        // Wrap protections
        Column {
            generatedString.excludedCategories.chunked(3).forEach { row ->
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    row.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .background(CardDark, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .padding(end = 8.dp)
                        ) {
                            Text(text = cat, color = TealPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
