package com.caglar.pokequery.domain.risk

import com.caglar.pokequery.data.model.RiskLevel

data class RiskExplanation(
    val id: String,
    val riskLevel: RiskLevel,
    val title: String,
    val shortReason: String,
    val detailedReason: String,
    val safetyChecklist: List<String>,
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
            title = "Inspection-only query",
            shortReason = "This query is for checking or reviewing, not cleanup or trading action.",
            detailedReason = "Info queries help you inspect Pokémon such as Hundos, Nundos, or PvP candidates. PokeQuery does not tell you to transfer, trade, power up, or delete anything.",
            safetyChecklist = listOf("Use it to inspect only", "Review results in Pokémon GO", "Do not treat matches as cleanup targets"),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("hundo_check", "nundo_finder", "pvp_candidates"),
            isActionAdjacent = false,
            isInspectionOnly = true
        ),
        RiskExplanation(
            id = "risk_low_review",
            riskLevel = RiskLevel.Low,
            title = "Usually safe, still review",
            shortReason = "This query is generally low-risk, but Pokémon GO results should still be reviewed.",
            detailedReason = "Low-risk queries are less action-adjacent and usually include conservative filters. They still produce text only; you remain responsible for checking the in-game result list.",
            safetyChecklist = listOf("Review before acting", "Check protected categories", "Treat the result as a helper, not a command"),
            relatedKnowledgeIds = listOf("misconception_inspection_vs_action"),
            appliesToGoalIds = listOf("untagged"),
            isActionAdjacent = false,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_medium_action_adjacent",
            riskLevel = RiskLevel.Medium,
            title = "Action-adjacent query",
            shortReason = "This is Medium because it may be used before trading, transferring, or cleanup.",
            detailedReason = "Action-adjacent queries can help prepare for transfer candy, trade review, or storage cleanup. They include protections, but broad matches and game-specific quirks still require manual review before copying or acting.",
            safetyChecklist = listOf("Review protected categories", "Spot-check matches before action", "Use RiskWarning confirmation seriously"),
            relatedKnowledgeIds = listOf("misconception_count_caveats", "misconception_inspection_vs_action", "counter_count"),
            appliesToGoalIds = listOf("safe_cleanup", "candy_prep", "trade_fodder", "lucky_trade", "expert"),
            isActionAdjacent = true,
            isInspectionOnly = false
        ),
        RiskExplanation(
            id = "risk_high_review",
            riskLevel = RiskLevel.High,
            title = "High review required",
            shortReason = "High-risk workflows need careful manual review before any action.",
            detailedReason = "High-risk queries can be broad, destructive, or advisory. PokeQuery never acts on your account; it only creates text, and you must inspect every match in Pokémon GO.",
            safetyChecklist = listOf("Do not act blindly", "Verify every match", "Avoid mass transfer/trade decisions"),
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
