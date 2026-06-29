package com.caglar.pokequery

import androidx.navigation3.runtime.NavKey
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import kotlinx.serialization.Serializable

@Serializable data class Onboarding(val initialPage: Int = 0) : NavKey
@Serializable data object Home : NavKey
@Serializable data class GoalDetail(val goalId: String) : NavKey
@Serializable data object ExpertBuilder : NavKey
@Serializable data object Favorites : NavKey
@Serializable data object History : NavKey
@Serializable data class KnowledgeBase(val startExpanded: Boolean = false) : NavKey
@Serializable data object Settings : NavKey
@Serializable data object ChangelogRoute : NavKey
@Serializable data object Presets : NavKey
@Serializable data class RiskWarning(val generatedString: GeneratedString) : NavKey
// v0.6.1: new workflow + context surfaces.
@Serializable data object MyPresets : NavKey
@Serializable data object PracticeMode : NavKey
@Serializable data object CleaningJournal : NavKey
@Serializable data object EventContext : NavKey
// v0.6.2: Safe NL search-string assistant (local parser) + optional remote AI provider.
@Serializable data object SearchAssistant : NavKey
// v0.6.2: Search String Explain mode — token-by-token breakdown.
@Serializable data class ExplainRoute(val query: String = "") : NavKey

fun startDestination(startRoute: String?, firstUseSeen: Boolean?): NavKey? = when (startRoute) {
    "onboarding" -> Onboarding(0)
    "onboarding_step_2" -> Onboarding(1)
    "onboarding_step_3" -> Onboarding(2)
    "home", "builder" -> Home
    "detail_safe_cleanup" -> GoalDetail("safe_cleanup")
    "detail_candy_prep" -> GoalDetail("candy_prep")
    "detail_trade_fodder" -> GoalDetail("trade_fodder")
    "detail_pvp_candidates" -> GoalDetail("pvp_candidates")
    "detail_nundo_finder" -> GoalDetail("nundo_finder")
    "detail_lucky_trade" -> GoalDetail("lucky_trade")
    "knowledge" -> KnowledgeBase(false)
    "knowledge_expanded" -> KnowledgeBase(true)
    "presets" -> Presets
    "expert" -> ExpertBuilder
    "favorites" -> Favorites
    "history" -> History
    "settings" -> Settings
    "changelog" -> ChangelogRoute
    // v0.6.1: new workflow + context surfaces reachable from Home cards, shortcuts and the widget.
    "my_presets" -> MyPresets
    "practice" -> PracticeMode
    "journal" -> CleaningJournal
    "events" -> EventContext
    "assistant" -> SearchAssistant
    "explain" -> ExplainRoute()
    null -> when (firstUseSeen) {
        null -> null
        true -> Home
        false -> Onboarding(0)
    }
    else -> Home
}

/**
 * Resolves a Home card goalId to its real NavKey destination.
 *
 * v0.5.2 (Fix 4 — Knowledge Base navigation bug): the Home grid contains several cards
 * that are NOT goal details (Knowledge Base, Expert Builder, Popular Presets,
 * Favorites). Previously only "expert"/"presets" were mapped, so "knowledge" fell into
 * the `else` branch and produced GoalDetail("knowledge"), which opened the Expert Builder
 * via the detail screen's unknown-goal fallback. Now every non-detail Home card is mapped
 * explicitly, and GoalDetail is reserved for genuine goal IDs.
 */
fun homeGoalDestination(goalId: String): NavKey = when (goalId) {
    "expert" -> ExpertBuilder
    "presets" -> Presets
    "knowledge" -> KnowledgeBase()
    "favorites" -> Favorites
    // v0.6.1: Home cards for the new workflow + context surfaces.
    "my_presets" -> MyPresets
    "practice" -> PracticeMode
    "journal" -> CleaningJournal
    "events" -> EventContext
    // v0.6.2: Search Assistant (local NL parser).
    "assistant" -> SearchAssistant
    "explain" -> ExplainRoute()
    "changelog" -> ChangelogRoute
    "settings" -> Settings
    else -> GoalDetail(goalId)
}

fun bottomTabDestination(route: String): NavKey? = when (route) {
    "home", "builder" -> Home
    "favorites" -> Favorites
    "history" -> History
    "knowledge" -> KnowledgeBase()
    "settings" -> Settings
    else -> null
}

fun tabForStartRoute(startRoute: String?): String = when (startRoute) {
    "favorites" -> "favorites"
    "history" -> "history"
    "knowledge", "knowledge_expanded" -> "knowledge"
    "settings" -> "settings"
    else -> "builder"
}

fun requiresRiskWarning(riskLevel: RiskLevel): Boolean =
    riskLevel == RiskLevel.Medium || riskLevel == RiskLevel.High
