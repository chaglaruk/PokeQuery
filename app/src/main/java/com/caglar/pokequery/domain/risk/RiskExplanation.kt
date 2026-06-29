package com.caglar.pokequery.domain.risk

import com.caglar.pokequery.data.model.RiskLevel

data class RiskExplanation(
    val id: String,
    val riskLevel: RiskLevel,
    @androidx.annotation.StringRes val titleRes: Int,
    @androidx.annotation.StringRes val shortReasonRes: Int,
    @androidx.annotation.StringRes val detailedReasonRes: Int,
    val safetyChecklistRes: List<Int>,
    val relatedKnowledgeIds: List<String>,
    val appliesToGoalIds: List<String>,
    val isActionAdjacent: Boolean,
    val isInspectionOnly: Boolean
)

object RiskExplanations {
    val all = listOf(
        RiskExplanation(
            id = "risk_info_inspection",
            riskLevel = RiskLevel.Info,
            titleRes = com.caglar.pokequery.R.string.risk_info_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_info_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_info_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_info_check1, com.caglar.pokequery.R.string.risk_info_check2, com.caglar.pokequery.R.string.risk_info_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("hundo_check", "nundo_finder", "pvp_candidates"),
            isActionAdjacent = false,
            isInspectionOnly = true
        ),
        RiskExplanation(
            id = "risk_low_review",
            riskLevel = RiskLevel.Low,
            titleRes = com.caglar.pokequery.R.string.risk_low_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_low_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_low_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_low_check1, com.caglar.pokequery.R.string.risk_low_check2, com.caglar.pokequery.R.string.risk_low_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("untagged"),
            isActionAdjacent = false,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_medium_action_adjacent",
            riskLevel = RiskLevel.Medium,
            titleRes = com.caglar.pokequery.R.string.risk_medium_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_medium_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_medium_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_medium_check1, com.caglar.pokequery.R.string.risk_medium_check2, com.caglar.pokequery.R.string.risk_medium_check3),
            relatedKnowledgeIds = listOf("misconception_count_caveats", "misconception_inspection_vs_action", "counter_count"),
            appliesToGoalIds = listOf("safe_cleanup", "candy_prep", "trade_fodder", "lucky_trade", "expert"),
            isActionAdjacent = true,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_high_review",
            riskLevel = RiskLevel.High,
            titleRes = com.caglar.pokequery.R.string.risk_high_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_high_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_high_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_high_check1, com.caglar.pokequery.R.string.risk_high_check2, com.caglar.pokequery.R.string.risk_high_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = emptyList(),
            isActionAdjacent = true,
            isInspectionOnly = false
        )
    )

    fun forGoal(goalId: String, riskLevel: RiskLevel): RiskExplanation =
        all.firstOrNull { goalId in it.appliesToGoalIds }
            ?: all.first { it.riskLevel == riskLevel }
}
