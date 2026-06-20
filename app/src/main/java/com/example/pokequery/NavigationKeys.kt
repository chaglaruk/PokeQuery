package com.example.pokequery

import androidx.navigation3.runtime.NavKey
import com.example.pokequery.data.model.GeneratedString
import kotlinx.serialization.Serializable

@Serializable data object Onboarding : NavKey
@Serializable data object Home : NavKey
@Serializable data class GuidedQuestions(val goalId: String) : NavKey
@Serializable data class Preview(val generatedString: GeneratedString) : NavKey
@Serializable data object ExpertBuilder : NavKey
@Serializable data object Favorites : NavKey
@Serializable data object KnowledgeBase : NavKey
@Serializable data object Settings : NavKey
@Serializable data object RiskWarning : NavKey
