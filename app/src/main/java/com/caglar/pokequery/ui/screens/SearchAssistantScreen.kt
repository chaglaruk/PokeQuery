package com.caglar.pokequery.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.domain.assist.AiProviderRegistry
import com.caglar.pokequery.domain.assist.SearchIntentParser
import com.caglar.pokequery.domain.assist.SearchStringExplainer
import com.caglar.pokequery.domain.lint.ExpertCopyPolicy
import com.caglar.pokequery.domain.lint.Linter
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.BorderDark
import com.caglar.pokequery.theme.BorderSubtle
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.CardPremium
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.PurpleIV
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.ui.components.ScreenTitleBar
import com.caglar.pokequery.ui.motion.PqStaggeredEntrance
import com.caglar.pokequery.ui.clearFocusOnTap
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.pq.PqCard
import com.caglar.pokequery.ui.pq.PqPrimaryButton
import com.caglar.pokequery.ui.pq.PqSecondaryButton
import com.caglar.pokequery.ui.pq.PqStringBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SearchAssistantScreen(onBack: () -> Unit, onCopyRaw: (String) -> Unit = {}, onExplain: (String) -> Unit = {}) {
    val context = LocalContext.current
    val isTurkishUi = LocalConfiguration.current.locales[0]?.language == "tr"
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var parseResult by remember { mutableStateOf<com.caglar.pokequery.domain.assist.ParsedIntent?>(null) }
    var explainedResult by remember { mutableStateOf<com.caglar.pokequery.domain.assist.ExplainedString?>(null) }
    var aiLoading by remember { mutableStateOf(false) }

    val aiProvider = AiProviderRegistry.activeProvider
    val couldNotUnderstandText = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_could_not_understand)
    val generatedExplanationText = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_generated_explanation)
    val reviewInGameText = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_review_in_game)
    val lintIssueText = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_lint_issue)
    val reviewWarningText = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_review_warning)

    PqStaggeredEntrance { visible ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).clearFocusOnTap().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenTitleBar(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_title), onBack, Modifier.pqStaggeredItem(visible, 0))
        }
        item {
            Text(
                androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_desc_text),
                color = TextSecondary, fontSize = 13.sp, lineHeight = 17.sp,
                modifier = Modifier.pqStaggeredItem(visible, 1)
            )
        }
        item {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_placeholder), color = TextSecondary) },
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
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        parseResult = SearchIntentParser.parse(inputText)
                        explainedResult = SearchStringExplainer.explain(parseResult!!.rawQuery)
                    },
                    enabled = inputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) { Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_parse), color = TextPrimary, fontWeight = FontWeight.Bold) }

                if (aiProvider.isAvailable) {
                    OutlinedButton(
                        onClick = {
                            aiLoading = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    aiProvider.suggest(inputText)
                                }
                                aiLoading = false
                                result.fold(
                                    onSuccess = { suggestion ->
                                        parseResult = com.caglar.pokequery.domain.assist.ParsedIntent(
                                            tokens = suggestion.rawSyntax.split(Regex("[&!]")).filter { it.isNotBlank() },
                                            rawQuery = suggestion.rawSyntax,
                                            explanation = suggestion.explanation,
                                            limitations = suggestion.limitations
                                        )
                                        explainedResult = SearchStringExplainer.explain(suggestion.rawSyntax)
                                    },
                                    onFailure = {
                                        Toast.makeText(context, "AI: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        enabled = inputText.isNotBlank() && !aiLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_ai_suggest), color = TealPrimary)
                    }
                }
            }
        }

        if (parseResult == null) {
            item {
                Spacer(Modifier.height(24.dp))
                com.caglar.pokequery.ui.components.SearchAssistantEmptyState(
                    modifier = Modifier.fillMaxWidth().pqStaggeredItem(visible, 2)
                )
            }
        }

        parseResult?.let { result ->
            if (!result.canBuild && result.explanation.isNotBlank()) {
                item {
                    PqCard(borderColor = AmberWarning) {
                        Text(
                            if (isTurkishUi) couldNotUnderstandText else result.explanation,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (result.canBuild) {
                // Linter + ExpertCopyPolicy gate — same safety pattern as ExpertBuilderScreen.
                val warnings = Linter.lint(result.rawQuery)
                val copyBlocked = !ExpertCopyPolicy.canCopy(result.rawQuery)
                val hasAdvisoryOnly = !copyBlocked && warnings.isNotEmpty()

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_suggested_string), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        explainedResult?.let { exp ->
                            val labelColor = when (exp.precision) {
                                com.caglar.pokequery.domain.assist.SearchPrecision.EXACT -> TealPrimary
                                com.caglar.pokequery.domain.assist.SearchPrecision.SHORTLIST -> CyanGlow
                                com.caglar.pokequery.domain.assist.SearchPrecision.APPROXIMATE -> AmberWarning
                                else -> TextSecondary
                            }

                            val precisionLabelStr = when (exp.precision) {
                                com.caglar.pokequery.domain.assist.SearchPrecision.EXACT -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_prec_exact)
                                com.caglar.pokequery.domain.assist.SearchPrecision.SHORTLIST -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_prec_shortlist)
                                com.caglar.pokequery.domain.assist.SearchPrecision.APPROXIMATE -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_prec_approx)
                                com.caglar.pokequery.domain.assist.SearchPrecision.NEEDS_VERIFICATION -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_prec_needs_verif)
                                com.caglar.pokequery.domain.assist.SearchPrecision.UNKNOWN -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_prec_unknown)
                            }
                            com.caglar.pokequery.ui.pq.PqBadge(precisionLabelStr, labelColor)
                        }
                    }
                }
                item {
                    PqStringBox(result.rawQuery)
                }

                // Linter banner — shows errors (blocking) and advisory warnings.
                if (warnings.isNotEmpty()) {
                    item {
                        val shape = RoundedCornerShape(14.dp)
                        Column(
                            Modifier.fillMaxWidth().clip(shape).background(CardDark)
                                .border(1.dp, if (copyBlocked) CoralDanger.copy(alpha = 0.4f) else GoldCaution.copy(alpha = 0.3f), shape)
                                .padding(14.dp)
                        ) {
                            warnings.forEach { w ->
                                val warningText = if (isTurkishUi) {
                                    if (w.isError) lintIssueText else reviewWarningText
                                } else {
                                    w.message
                                }
                                Text(
                                    "\u2022 $warningText",
                                    color = if (w.isError) CoralDanger else GoldCaution,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            if (copyBlocked) {
                                Spacer(Modifier.height(6.dp))
                                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_fix_errors), color = CoralDanger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else if (hasAdvisoryOnly) {
                                Spacer(Modifier.height(6.dp))
                                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_advisory_only), color = GoldCaution, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PqPrimaryButton(
                            text = if (copyBlocked) androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_fix_errors) else androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_copy_btn),
                            onClick = {
                                clipboard.setText(AnnotatedString(result.rawQuery))
                                onCopyRaw(result.rawQuery)
                                Toast.makeText(context, context.resources.getString(com.caglar.pokequery.R.string.assistant_copied), Toast.LENGTH_SHORT).show()
                            },
                            enabled = !copyBlocked,
                            leadingIcon = Icons.Default.ContentCopy,
                            modifier = Modifier.weight(1f)
                        )
                        PqSecondaryButton(
                            text = androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_explain_btn),
                            onClick = { onExplain(result.rawQuery) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                explainedResult?.let { exp ->
                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            com.caglar.pokequery.ui.pq.PqRiskBadge(exp.totalRisk)

                            val scopeBreadthStr = when (exp.scopeBreadth) {
                                "All (no filter)" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_scope_all)
                                "Very Narrow" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_scope_very_narrow)
                                "Narrow" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_scope_narrow)
                                "Moderate" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_scope_moderate)
                                "Broad" -> androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.explainer_scope_broad)
                                else -> exp.scopeBreadth
                            }
                            Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_scope) + ": $scopeBreadthStr", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                if (result.tokens.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_tokens), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    item {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardDark).border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)).padding(12.dp)) {
                            result.tokens.forEach { token ->
                                Text("+ $token", color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                            result.exclusions.forEach { exclusion ->
                                Text("! $exclusion (" + androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_excluded) + ")", color = AmberWarning, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                val displayExplanation = if (isTurkishUi) generatedExplanationText else result.explanation
                if (displayExplanation.isNotBlank()) {
                    item {
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_explanation), color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    item {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardDark).border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)).padding(12.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(displayExplanation, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    }
                }

                val displayLimitations = if (isTurkishUi && result.limitations.isNotEmpty()) {
                    listOf(reviewInGameText)
                } else {
                    result.limitations
                }
                if (displayLimitations.isNotEmpty()) {
                    item {
                        Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.search_assistant_limitations), color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    item {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardDark).border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)).padding(12.dp)) {
                            displayLimitations.forEach { limitation ->
                                Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                    Text("\u2022 ", color = CoralDanger, fontSize = 12.sp)
                                    Text(limitation, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

