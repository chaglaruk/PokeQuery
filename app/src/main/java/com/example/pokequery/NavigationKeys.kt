package com.example.pokequery

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Onboarding : NavKey
@Serializable data object Home : NavKey
@Serializable data class GuidedQuestions(val goalId: String) : NavKey
@Serializable data class Preview(val baseQuery: String, val goalId: String, val include0Star: Boolean = false) : NavKey
@Serializable data object ExpertBuilder : NavKey
@Serializable data object Favorites : NavKey
@Serializable data object KnowledgeBase : NavKey
@Serializable data object Settings : NavKey
@Serializable data object RiskWarning : NavKey
