package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.domain.engine.SearchTermMapper

@Composable
fun RiskWarningScreen(
    generatedString: GeneratedString,
    onConfirmCopy: () -> Unit,
    onBack: () -> Unit
) {
    // Package 4: per-goal explanation. RiskMessageBuilder appends the Turkish-beta
    // caution when the output looks Turkish, so the warning is goal-aware + localized.
    val turkish = SearchTermMapper.looksTurkish(generatedString.rawSyntax)
    val goalMessage = com.caglar.pokequery.domain.risk.RiskMessageBuilder
        .messageFor(generatedString.goalId, turkish)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = AmberWarning, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("${generatedString.riskLevel} Risk Copy", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        // v0.4.2 (Fix 7): raised contrast from Color.Gray to TextSecondary for WCAG AA.
        Text(
            "Review the search and its warnings before copying. The app only creates text; always inspect matches before acting.",
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Package 4: goal-specific explanation (already includes Turkish caution if relevant).
        Text(
            goalMessage,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onConfirmCopy, modifier = Modifier.fillMaxWidth()) {
            Text("Confirm and Copy")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            // v0.4.2 (Fix 7): raised contrast from Color.Gray to TextSecondary.
            Text("Go Back", color = TextSecondary)
        }
    }
}
