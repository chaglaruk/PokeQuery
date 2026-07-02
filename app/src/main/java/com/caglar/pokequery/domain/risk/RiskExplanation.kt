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
        ),
        // Goal-specific ELI5 risk explanations (v0.6.8)
        RiskExplanation(
            id = "risk_safe_cleanup",
            riskLevel = RiskLevel.Medium,
            titleRes = com.caglar.pokequery.R.string.risk_safe_cleanup_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_safe_cleanup_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_safe_cleanup_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_safe_cleanup_check1, com.caglar.pokequery.R.string.risk_safe_cleanup_check2, com.caglar.pokequery.R.string.risk_safe_cleanup_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action", "counter_count"),
            appliesToGoalIds = listOf("safe_cleanup"),
            isActionAdjacent = true,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_candy_prep",
            riskLevel = RiskLevel.Medium,
            titleRes = com.caglar.pokequery.R.string.risk_candy_prep_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_candy_prep_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_candy_prep_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_candy_prep_check1, com.caglar.pokequery.R.string.risk_candy_prep_check2, com.caglar.pokequery.R.string.risk_candy_prep_check3),
            relatedKnowledgeIds = listOf("misconception_count_caveats", "misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("candy_prep"),
            isActionAdjacent = true,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_trade_fodder",
            riskLevel = RiskLevel.Medium,
            titleRes = com.caglar.pokequery.R.string.risk_trade_fodder_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_trade_fodder_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_trade_fodder_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_trade_fodder_check1, com.caglar.pokequery.R.string.risk_trade_fodder_check2, com.caglar.pokequery.R.string.risk_trade_fodder_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("trade_fodder"),
            isActionAdjacent = true,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_hundo_check",
            riskLevel = RiskLevel.Info,
            titleRes = com.caglar.pokequery.R.string.risk_hundo_check_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_hundo_check_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_hundo_check_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_hundo_check_check1, com.caglar.pokequery.R.string.risk_hundo_check_check2, com.caglar.pokequery.R.string.risk_hundo_check_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("hundo_check"),
            isActionAdjacent = false,
            isInspectionOnly = true
        ),
        RiskExplanation(
            id = "risk_nundo_finder",
            riskLevel = RiskLevel.Info,
            titleRes = com.caglar.pokequery.R.string.risk_nundo_finder_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_nundo_finder_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_nundo_finder_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_nundo_finder_check1, com.caglar.pokequery.R.string.risk_nundo_finder_check2, com.caglar.pokequery.R.string.risk_nundo_finder_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("nundo_finder"),
            isActionAdjacent = false,
            isInspectionOnly = true
        ),
        RiskExplanation(
            id = "risk_pvp_candidates",
            riskLevel = RiskLevel.Info,
            titleRes = com.caglar.pokequery.R.string.risk_pvp_candidates_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_pvp_candidates_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_pvp_candidates_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_pvp_candidates_check1, com.caglar.pokequery.R.string.risk_pvp_candidates_check2, com.caglar.pokequery.R.string.risk_pvp_candidates_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("pvp_candidates"),
            isActionAdjacent = false,
            isInspectionOnly = true
        ),
        RiskExplanation(
            id = "risk_lucky_trade",
            riskLevel = RiskLevel.Medium,
            titleRes = com.caglar.pokequery.R.string.risk_lucky_trade_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_lucky_trade_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_lucky_trade_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_lucky_trade_check1, com.caglar.pokequery.R.string.risk_lucky_trade_check2, com.caglar.pokequery.R.string.risk_lucky_trade_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("lucky_trade"),
            isActionAdjacent = true,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_untagged",
            riskLevel = RiskLevel.Low,
            titleRes = com.caglar.pokequery.R.string.risk_untagged_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_untagged_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_untagged_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_untagged_check1, com.caglar.pokequery.R.string.risk_untagged_check2, com.caglar.pokequery.R.string.risk_untagged_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("untagged"),
            isActionAdjacent = false,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_expert",
            riskLevel = RiskLevel.Medium,
            titleRes = com.caglar.pokequery.R.string.risk_expert_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_expert_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_expert_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_expert_check1, com.caglar.pokequery.R.string.risk_expert_check2, com.caglar.pokequery.R.string.risk_expert_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("expert"),
            isActionAdjacent = true,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_assistant",
            riskLevel = RiskLevel.Medium,
            titleRes = com.caglar.pokequery.R.string.risk_assistant_title,
            shortReasonRes = com.caglar.pokequery.R.string.risk_assistant_short,
            detailedReasonRes = com.caglar.pokequery.R.string.risk_assistant_detailed,
            safetyChecklistRes = listOf(com.caglar.pokequery.R.string.risk_assistant_check1, com.caglar.pokequery.R.string.risk_assistant_check2, com.caglar.pokequery.R.string.risk_assistant_check3),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("assistant"),
            isActionAdjacent = true,
            isInspectionOnly = false
        )
    )

    fun forGoal(goalId: String, riskLevel: RiskLevel): RiskExplanation =
        all.firstOrNull { goalId in it.appliesToGoalIds }
            ?: all.first { it.riskLevel == riskLevel }
}
