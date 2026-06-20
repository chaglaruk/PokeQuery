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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.pokequery.data.repository.UserPreferencesRepository
import com.example.pokequery.data.repository.dataStore
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Top Bar
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text(text = "<- Back", color = TealPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "Preview", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(64.dp))
        }

        // Risk Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(riskColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = riskColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "${generatedString.riskLevel} Risk", color = riskColor, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = 18.sp)
                Text(text = if (generatedString.riskLevel == RiskLevel.Low) "Safe to use" else "Review before using", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Your search string", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        // String Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = generatedString.rawSyntax,
                color = riskColor, // Colors match risk level!
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val context = androidx.compose.ui.platform.LocalContext.current
        val repository = remember { UserPreferencesRepository(context.dataStore) }
        val scope = rememberCoroutineScope()

        Button(
            onClick = { 
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(generatedString.rawSyntax))
                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                onCopy() 
            },
            colors = ButtonDefaults.buttonColors(containerColor = riskColor, contentColor = if (riskColor == AmberWarning) Color.Black else Color.White),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Copy String", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { 
                scope.launch { repository.addFavorite(generatedString.rawSyntax) }
                android.widget.Toast.makeText(context, "Saved to favorites", android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
        ) {
            Text("Save as Favorite")
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Explanation
        Text(text = "What does this do?", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = generatedString.plainLanguageExplanation, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
        
        if (generatedString.warnings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Warnings", color = CoralDanger, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            generatedString.warnings.forEach { warning ->
                Text(text = "• $warning", color = AmberWarning, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
        
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
