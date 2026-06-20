package com.example.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokequery.theme.*
import com.example.pokequery.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    generatedString: String,
    onCopy: () -> Unit,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    
    // Attempting to deduce the type from the context/string for the UI display
    val isSafeCleanup = generatedString.contains("!shiny") && !generatedString.contains("traded") && !generatedString.contains("count2-")
    val isCandyPrep = generatedString.contains("count2-") && !generatedString.contains("traded")
    val isTradeFodder = generatedString.contains("count2-") && generatedString.contains("traded")
    val isHundoCheck = generatedString == "4*"
    val isUntagged = generatedString == "!#"
    
    val title = when {
        isSafeCleanup -> "Safe Cleanup"
        isCandyPrep -> "2x Candy Prep"
        isTradeFodder -> "Trade Fodder"
        isHundoCheck -> "Hundo Check"
        isUntagged -> "Untagged Cleanup"
        else -> "Custom Search"
    }

    val riskLevel = when {
        isSafeCleanup -> "Low Risk"
        isCandyPrep -> "Medium Risk"
        isTradeFodder -> "Medium Risk"
        isHundoCheck -> "Low Risk"
        isUntagged -> "Low Risk"
        else -> "Low Risk"
    }
    
    val riskColor = if (riskLevel == "Medium Risk") AmberWarning else TealPrimary
    val riskSubtitle = if (riskLevel == "Medium Risk") "Requires manual review of results" else "Protects important categories"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("<- Back", color = TextSecondary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RiskHeaderCardCompose(riskLevel = riskLevel, subtitle = riskSubtitle, color = riskColor)
            
            Text("Your search string", color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            
            SearchStringPanel(query = generatedString)
            
            CopyCTA(color = if (riskLevel == "Medium Risk") AmberWarning else BlueCTA) {
                clipboard?.setText(AnnotatedString(generatedString))
                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                onCopy()
            }
            
            if (isCandyPrep || isTradeFodder) {
                WarningInfoPanel(
                    title = if (isCandyPrep) "Count Limitation" else "Trade Warning",
                    message = if (isCandyPrep) "Count is based on Pokédex species number and may not distinguish shiny, form, or costume differences." else "Real trade eligibility depends on friendship level and cannot be guaranteed by search strings alone."
                )
            }
            
            val explanation = when {
                isSafeCleanup -> "This is a REVIEW string targeting low-value candidates. It is not an automatic transfer command."
                isCandyPrep -> "Finds extras. Use during 2x transfer candy spotlight hours."
                isTradeFodder -> "Finds candidates for trading. Excludes previously traded Pokémon."
                isHundoCheck -> "Finds all perfect IV / hundo Pokémon. 4★ means 15/15/15."
                isUntagged -> "Finds Pokemon without any tags."
                else -> "Custom or generated search string."
            }
            ExplanationCard(explanation = explanation)
            
            if (!isHundoCheck && !isUntagged && generatedString.isNotEmpty()) {
                Text("Protected Categories", color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                val protections = mutableListOf<String>()
                if (generatedString.contains("!shiny")) protections.add("Shiny")
                if (generatedString.contains("!legendary")) protections.add("Legendary")
                if (generatedString.contains("!mythical")) protections.add("Mythical")
                if (generatedString.contains("!lucky")) protections.add("Lucky")
                if (generatedString.contains("!shadow")) protections.add("Shadow")
                if (generatedString.contains("!purified")) protections.add("Purified")
                if (generatedString.contains("!favorite")) protections.add("Favorite")
                if (generatedString.contains("!4*")) protections.add("4★ (Hundo)")
                if (generatedString.contains("!costume")) protections.add("Costume")
                if (generatedString.contains("!#")) protections.add("Tagged")
                if (generatedString.contains("!traded")) protections.add("Traded")
                if (generatedString.contains("!background")) protections.add("Backgrounds")
                if (generatedString.contains("!ultrabeast")) protections.add("Ultra Beasts")
                
                if (protections.isNotEmpty()) {
                    ProtectedChipGrid(protections = protections)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
