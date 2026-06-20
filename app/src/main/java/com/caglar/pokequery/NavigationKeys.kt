package com.caglar.pokequery

import androidx.navigation3.runtime.NavKey
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import kotlinx.serialization.Serializable

@Serializable data object Onboarding : NavKey
@Serializable data object Home : NavKey
@Serializable data class GoalDetail(val goalId: String) : NavKey
@Serializable data object ExpertBuilder : NavKey
@Serializable data object Favorites : NavKey
@Serializable data object KnowledgeBase : NavKey
@Serializable data object Settings : NavKey
@Serializable data object Presets : NavKey
@Serializable data class RiskWarning(val generatedString: GeneratedString) : NavKey

fun bottomTabDestination(route: String): NavKey? = when (route) {
    "home" -> Home
    "builder" -> ExpertBuilder
    "favorites" -> Favorites
    "knowledge" -> KnowledgeBase
    "settings" -> Settings
    else -> null
}



fun requiresRiskWarning(riskLevel: RiskLevel): Boolean =
    riskLevel == RiskLevel.Medium || riskLevel == RiskLevel.High
