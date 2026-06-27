package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.domain.assist.SearchStringExplainer
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.BorderDark
import com.caglar.pokequery.theme.BorderSubtle
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.PurpleIV
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.components.ScreenTitleBar
import com.caglar.pokequery.ui.motion.PqStaggeredEntrance
import com.caglar.pokequery.ui.motion.pqStaggeredItem

@Composable
fun ExplainScreen(onBack: () -> Unit, initialQuery: String = "") {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var queryText by remember { mutableStateOf(initialQuery) }
    var result by remember { mutableStateOf(SearchStringExplainer.explain(initialQuery)) }
    var showClipboardOffer by remember { mutableStateOf(initialQuery.isBlank()) }
    val density = currentDensity()

    val clipboardText = remember {
        val text = clipboard.getText()?.text?.trim() ?: ""
        if (text.isNotBlank() && text.matches(Regex("^[\\w!@&*#,><\\-\\d]+$")) && text.length in 2..200) text else ""
    }

    PqStaggeredEntrance { visible ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(density.listGap),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenTitleBar("Explain", onBack, Modifier.pqStaggeredItem(visible, 0))
        }
        item {
            Text(
                "Paste or type a search string to see what each part does.",
                color = TextSecondary, fontSize = 13.sp, lineHeight = 17.sp,
                modifier = Modifier.pqStaggeredItem(visible, 1)
            )
        }
        if (showClipboardOffer && clipboardText.isNotBlank()) {
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CyanGlow.copy(alpha = 0.08f)).border(1.dp, CyanGlow.copy(alpha = 0.35f), RoundedCornerShape(14.dp)).padding(12.dp)) {
                    Text("Clipboard contains text that looks like a search string.", color = TextPrimary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                queryText = clipboardText
                                result = SearchStringExplainer.explain(clipboardText)
                                showClipboardOffer = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Load & Explain", color = TealPrimary, fontSize = 12.sp) }
                        OutlinedButton(
                            onClick = { showClipboardOffer = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Dismiss", color = TextSecondary, fontSize = 12.sp) }
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = queryText,
                onValueChange = {
                    queryText = it
                    result = SearchStringExplainer.explain(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 4*&!shiny&!legendary", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        if (result.original.isNotBlank()) {
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardDark).border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)).padding(14.dp)) {
                    Text("Summary", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(result.summary, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    val (riskLabel, riskColor) = when (result.totalRisk) {
                        com.caglar.pokequery.data.model.RiskLevel.Medium -> "Medium Risk" to GoldCaution
                        com.caglar.pokequery.data.model.RiskLevel.Low -> "Low Risk" to AmberWarning
                        else -> "Inspection" to TealPrimary
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Risk: ", color = TextSecondary, fontSize = 12.sp)
                        Text(riskLabel, color = riskColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Precision: ", color = TextSecondary, fontSize = 12.sp)
                        val precisionColor = when (result.precision) {
                            com.caglar.pokequery.domain.assist.SearchPrecision.EXACT -> TealPrimary
                            com.caglar.pokequery.domain.assist.SearchPrecision.SHORTLIST -> CyanGlow
                            com.caglar.pokequery.domain.assist.SearchPrecision.APPROXIMATE -> GoldCaution
                            else -> AmberWarning
                        }
                        Text(result.precisionLabel, color = precisionColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Scope: ", color = TextSecondary, fontSize = 12.sp)
                        Text(result.scopeBreadth, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (result.hasUnknownTokens) {
                        Spacer(Modifier.height(4.dp))
                        Text("Some tokens are not recognized. Verify them in Pokémon GO.", color = AmberWarning, fontSize = 11.sp)
                    }
                }
            }

            if (result.tokens.isNotEmpty()) {
                item {
                    Text("Token breakdown", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                items(result.tokens, key = { it.token + it.hashCode() }) { token ->
                    val tokenColor = when {
                        token.riskHint == com.caglar.pokequery.data.model.RiskLevel.Medium -> GoldCaution
                        token.category == "unknown" -> AmberWarning
                        token.isExclusion -> CyanGlow
                        else -> TealPrimary
                    }
                    val tag = if (token.isExclusion) "Exclusion" else "Inclusion"
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardDark).border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)).padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(token.token, color = tokenColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(Modifier.clip(RoundedCornerShape(50)).background(tokenColor.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text(tag, color = tokenColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(token.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(result.original))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copy search string", color = TealPrimary)
                    }
                }
            }
        }
    }
    }
}
