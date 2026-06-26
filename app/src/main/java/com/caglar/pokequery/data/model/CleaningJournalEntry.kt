package com.caglar.pokequery.data.model

import java.util.UUID

/**
 * v0.6.1 — Cleaning Journal.
 *
 * A USER-ENTERED note that the user keeps when they manually use a query in Pokémon GO.
 *
 * Critical honesty contract: PokeQuery CANNOT and MUST NOT know what Pokémon were
 * deleted/traded/transferred. This entry is user-entered memory only. Copying a string never
 * automatically creates an "applied" note. The app never claims it knows what changed in-game.
 */
data class CleaningJournalEntry(
    val id: String,
    val queryString: String,
    val title: String,
    val note: String,
    val actionType: JournalAction,
    val relatedHistoryId: String? = null,
    val relatedFavoriteId: String? = null,
    val relatedPersonalPresetId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun new(
            queryString: String,
            title: String,
            note: String,
            actionType: JournalAction,
            relatedHistoryId: String? = null,
            relatedFavoriteId: String? = null,
            relatedPersonalPresetId: String? = null
        ): CleaningJournalEntry {
            val now = System.currentTimeMillis()
            return CleaningJournalEntry(
                id = UUID.randomUUID().toString(),
                queryString = queryString,
                title = title.ifBlank { "Journal note" },
                note = note,
                actionType = actionType,
                relatedHistoryId = relatedHistoryId,
                relatedFavoriteId = relatedFavoriteId,
                relatedPersonalPresetId = relatedPersonalPresetId,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * The action a user associated with a journal note. All are user-chosen; the app never
 * infers an "applied" state automatically.
 *
 * `copied`/`reviewed` are explicitly non-action states. The `_session` / `applied_manually`
 * types are user-asserted memory — never auto-populated by the copy flow.
 */
enum class JournalAction(val label: String) {
    COPIED("Copied"),
    REVIEWED("Reviewed"),
    APPLIED_MANUALLY("Applied manually"),
    CLEANUP_SESSION("Cleanup session"),
    TRADE_SESSION("Trade session"),
    CANDY_PREP_SESSION("Candy prep session");

    companion object {
        fun fromName(name: String?): JournalAction =
            entries.firstOrNull { it.name == name } ?: COPIED
    }
}
