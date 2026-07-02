package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.caglar.pokequery.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.domain.engine.SearchTermMapper
import com.caglar.pokequery.domain.risk.RiskExplanations
import com.caglar.pokequery.domain.risk.RiskMessageBuilder
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.ui.motion.pqSpringPop
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.pq.PqPrimaryButton
import com.caglar.pokequery.ui.pq.PqSecondaryButton

@Composable
fun RiskWarningScreen(
    generatedString: GeneratedString,
    onConfirmCopy: () -> Unit,
    onBack: () -> Unit
) {
    // Package 4: per-goal explanation. RiskMessageBuilder appends the Turkish-beta
    // caution when the output looks Turkish, so the warning is goal-aware + localized.
    val turkish = SearchTermMapper.looksTurkish(generatedString.rawSyntax)
    val goalMessage = RiskMessageBuilder.messageFor(generatedString.goalId, turkish)
    val riskExplanation = RiskExplanations.forGoal(generatedString.goalId, generatedString.riskLevel)

    val riskColor = when (generatedString.riskLevel) {
        com.caglar.pokequery.data.model.RiskLevel.High -> CoralDanger
        else -> GoldCaution
    }
    val riskLabel = when (generatedString.riskLevel) {
        com.caglar.pokequery.data.model.RiskLevel.High -> stringResource(R.string.risk_high)
        com.caglar.pokequery.data.model.RiskLevel.Medium -> stringResource(R.string.risk_medium)
        com.caglar.pokequery.data.model.RiskLevel.Low -> stringResource(R.string.risk_low)
        com.caglar.pokequery.data.model.RiskLevel.Info -> stringResource(R.string.risk_info)
    }

    // v0.5.3 motion polish: staggered entrance. The risk icon gets a subtle spring-pop
    // (illustration only); title/explanation/buttons fade+slide. One hoisted flag → once only.
    com.caglar.pokequery.ui.motion.PqStaggeredEntrance { visible ->
    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(84.dp).clip(RoundedCornerShape(22.dp)).background(riskColor.copy(alpha = 0.18f))
                .pqStaggeredItem(visible, 0)
                .pqSpringPop(visible),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.common_warning), tint = riskColor, modifier = Modifier.size(46.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(
            stringResource(R.string.risk_warning_title, riskLabel),
            color = riskColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.pqStaggeredItem(visible, 1)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.risk_warning_review_before_copy),
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp).pqStaggeredItem(visible, 1)
        )
        Spacer(Modifier.height(14.dp))
        // Goal-specific explanation surface (includes Turkish caution if relevant).
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardDark)
                .padding(14.dp)
                .pqStaggeredItem(visible, 2)
        ) {
            Text(goalMessage, color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(riskColor.copy(alpha = 0.10f))
                .padding(14.dp)
                .pqStaggeredItem(visible, 2)
        ) {
            Column {
                Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.goal_detail_why_risk), color = riskColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(androidx.compose.ui.res.stringResource(riskExplanation.shortReasonRes), color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
                Spacer(Modifier.height(6.dp))
                riskExplanation.safetyChecklistRes.take(3).forEach { itemRes ->
                    Text("• ${androidx.compose.ui.res.stringResource(itemRes)}", color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
                if (riskExplanation.relatedKnowledgeIds.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(androidx.compose.ui.res.stringResource(com.caglar.pokequery.R.string.risk_learn_more, riskExplanation.relatedKnowledgeIds.joinToString()), color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Box(Modifier.pqStaggeredItem(visible, 3)) {
            PqPrimaryButton(text = stringResource(R.string.risk_warning_accept_copy), onClick = onConfirmCopy)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.pqStaggeredItem(visible, 4)) {
            PqSecondaryButton(text = stringResource(R.string.risk_warning_review_query), onClick = onBack)
        }
    }
    }
}
