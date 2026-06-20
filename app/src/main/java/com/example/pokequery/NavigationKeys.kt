package com.example.pokequery

import androidx.navigation3.runtime.NavKey
import com.example.pokequery.data.model.GeneratedString
import com.example.pokequery.data.model.RiskLevel
import kotlinx.serialization.Serializable

@Serializable data object Onboarding : NavKey
@Serializable data object Home : NavKey
@Serializable data class GuidedQuestions(val goalId: String) : NavKey
@Serializable data class Preview(val generatedString: GeneratedString) : NavKey
@Serializable data object ExpertBuilder : NavKey
@Serializable data object Favorites : NavKey
@Serializable data object KnowledgeBase : NavKey
@Serializable data object Settings : NavKey
@Serializable data class RiskWarning(val generatedString: GeneratedString) : NavKey

fun bottomTabDestination(route: String): NavKey? = when (route) {
    "home" -> Home
    "builder" -> ExpertBuilder
    "favorites" -> Favorites
    "knowledge" -> KnowledgeBase
    "settings" -> Settings
    else -> null
}

fun homeGoalDestination(goalId: String): NavKey =
    if (goalId == "expert") ExpertBuilder else GuidedQuestions(goalId)

fun requiresRiskWarning(riskLevel: RiskLevel): Boolean =
    riskLevel == RiskLevel.Medium || riskLevel == RiskLevel.High
