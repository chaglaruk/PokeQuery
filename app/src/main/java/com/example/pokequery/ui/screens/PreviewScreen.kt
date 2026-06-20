package com.example.pokequery.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pokequery.data.model.GeneratedString
import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.*
import com.example.pokequery.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    generatedString: GeneratedString,
    onCopy: () -> Unit,
    onSaveFavorite: () -> Unit,
    onBack: () -> Unit
) {
    val riskLabel = when (generatedString.riskLevel) {
        RiskLevel.Info -> "Info"
        RiskLevel.Low -> "Low Risk"
        RiskLevel.Medium -> "Medium Risk"
        RiskLevel.High -> "High Risk"
    }
    val riskColor = when (generatedString.riskLevel) {
        RiskLevel.Info -> BlueCTA
        RiskLevel.Low -> TealPrimary
        RiskLevel.Medium -> AmberWarning
        RiskLevel.High -> CoralDanger
    }
    val riskSubtitle = when (generatedString.riskLevel) {
        RiskLevel.Info -> "Inspection only"
        RiskLevel.Low -> "Protected cleanup output"
        RiskLevel.Medium -> "Requires manual review"
        RiskLevel.High -> "Contains high-risk inclusions"
    }
    
    val backgroundResId = when (generatedString.title) {
        "Safe Cleanup" -> R.drawable.safe_cleanup_header
        "2x Candy Prep" -> R.drawable.candy_prep_header
        "Trade Fodder" -> R.drawable.trade_fodder_header
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(generatedString.title, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RiskHeaderCardCompose(riskLevel = riskLabel, subtitle = riskSubtitle, color = riskColor, backgroundDrawableResId = backgroundResId)
            Text("Your search string", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            SearchStringPanel(query = generatedString.rawSyntax)
            CopyCTA(color = riskColor, onClick = onCopy)
            OutlinedButton(onClick = onSaveFavorite, modifier = Modifier.fillMaxWidth()) {
                Text("Save Favorite")
            }
            generatedString.warnings.forEach { WarningInfoPanel(title = "Warning", message = it) }
            ExplanationCard(explanation = generatedString.plainLanguageExplanation)

            if (generatedString.protectedCategories.isNotEmpty()) {
                Text("Protected Categories", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                ProtectedChipGrid(protections = generatedString.protectedCategories)
            }
            if (generatedString.includedHighRiskCategories.isNotEmpty()) {
                Text("Included Categories", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                ProtectedChipGrid(protections = generatedString.includedHighRiskCategories)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
