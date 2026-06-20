package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.theme.*

@Composable
fun ExpertBuilderScreen(
    onGenerate: (String) -> Unit,
    onBack: () -> Unit
) {
    var rawQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text("<- Back", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
            Text("Expert Builder", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        Text("Raw Search String", color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))

        OutlinedTextField(
            value = rawQuery,
            onValueChange = { rawQuery = it },
            modifier = Modifier.fillMaxWidth().weight(1f).background(CardDark, RoundedCornerShape(16.dp)),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp, color = TealPrimary),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = BorderDark
            ),
            placeholder = { Text("e.g. 4*&!shiny", color = TextSecondary) }
        )
        
        val linterWarnings = com.example.pokequery.domain.lint.Linter.lint(rawQuery)
        if (linterWarnings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth().background(CardPremium, RoundedCornerShape(12.dp)).border(1.dp, BorderDark, RoundedCornerShape(12.dp)).padding(16.dp)) {
                Text("Linter Warnings", color = AmberWarning, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                linterWarnings.forEach { warning ->
                    Text(
                        text = "• ${warning.message}", 
                        color = if (warning.isError) CoralDanger else AmberWarning, 
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onGenerate(rawQuery) },
            colors = ButtonDefaults.buttonColors(containerColor = BlueCTA),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Review Custom String", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
