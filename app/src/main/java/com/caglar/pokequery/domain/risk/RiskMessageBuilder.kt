package com.caglar.pokequery.domain.risk

/**
 * Package 4 — per-goal risk explanation builder (pure, testable).
 *
 * RiskWarning copy is chosen by goalId so each goal explains its own nuance instead of
 * showing a generic warning. A Turkish beta caution is appended when the generated output
 * is Turkish. Messages stay short and readable; they never claim automation or that the
 * app reads a Pokémon collection.
 */
object RiskMessageBuilder {

    private const val TURKISH_CAUTION =
        "Turkish search terms are beta. Please verify results in Pokémon GO before transferring or trading."

    fun messageFor(goalId: String, turkish: Boolean): String {
        val base = when (goalId) {
            "safe_cleanup" ->
                "Safe Cleanup excludes protected categories like shinies, legendaries, and favorites, but you must still review every match before transferring."
            "candy_prep" ->
                "2x Candy Prep is intended for transfer-candy events. Do not use it blindly outside the event; review matches first."
            "trade_fodder" ->
                "Real trade eligibility depends on friendship level and special-trade limits, and cannot be guaranteed by search strings alone. Review before trading."
            "lucky_trade" ->
                "Lucky trade candidates may include valuable Pokémon (shinies, legendaries). Review every match manually before trading."
            "pvp_candidates" ->
                "These are only candidate searches based on IV bands and CP. Verify true PvP IV/rank using dedicated PvP tools before investing."
            "hundo_check", "nundo_finder" ->
                "This is an inspection string, not a cleanup string. Use it to find perfect or 0/0/0 Pokémon to admire or track."
            "untagged" ->
                "Untagged Cleanup excludes protected categories. Review each untagged match before organizing or transferring."
            "expert" ->
                "You built this string yourself. The app cannot verify its safety — review all matches carefully before acting."
            else ->
                "This search may surface valuable Pokémon. Review every match in the game before acting."
        }
        return if (turkish) "$base\n\n$TURKISH_CAUTION" else base
    }
}
