package com.caglar.pokequery.data.repository

import com.caglar.pokequery.data.model.CleaningJournalEntry
import com.caglar.pokequery.data.model.JournalAction
import com.caglar.pokequery.data.model.PersonalPreset
import com.caglar.pokequery.data.model.RiskLevel

/**
 * v0.6.1 — Length-prefixed codecs for Personal Presets and Journal entries.
 *
 * Mirrors the proven [SavedTemplateCodec] pattern (fields joined as `<len>:<value>`). Corrupt or
 * partial entries decode to null and are silently dropped — the app never crashes on bad data.
 * These are LOCAL ONLY: nothing here ever touches the network or an account.
 */
object PersonalPresetCodec {
    private fun fields(p: PersonalPreset) = listOf(
        p.id, p.title, p.queryString,
        p.sourceGoalId.orEmpty(), p.sourceFavoriteId.orEmpty(), p.sourceHistoryId.orEmpty(),
        p.riskLevel.name,
        p.notes.orEmpty(),
        p.createdAt.toString(), p.updatedAt.toString()
    )

    fun encode(p: PersonalPreset): String =
        fields(p).joinToString("") { "${it.length}:$it" }

    fun decode(value: String): PersonalPreset? = runCatching {
        var offset = 0
        val f = List(10) {
            val sep = value.indexOf(':', offset)
            require(sep >= offset)
            val len = value.substring(offset, sep).toInt()
            val start = sep + 1
            val end = start + len
            require(end <= value.length)
            offset = end
            value.substring(start, end)
        }
        require(offset == value.length)
        PersonalPreset(
            id = f[0],
            title = f[1],
            queryString = f[2],
            sourceGoalId = f[3].ifBlank { null },
            sourceFavoriteId = f[4].ifBlank { null },
            sourceHistoryId = f[5].ifBlank { null },
            riskLevel = runCatching { RiskLevel.valueOf(f[6]) }.getOrElse { RiskLevel.Medium },
            notes = f[7].ifBlank { null },
            createdAt = f[8].toLong(),
            updatedAt = f[9].toLong()
        )
    }.getOrNull()
}

object JournalCodec {
    private fun fields(e: CleaningJournalEntry) = listOf(
        e.id, e.queryString, e.title, e.note, e.actionType.name,
        e.relatedHistoryId.orEmpty(), e.relatedFavoriteId.orEmpty(), e.relatedPersonalPresetId.orEmpty(),
        e.createdAt.toString(), e.updatedAt.toString()
    )

    fun encode(e: CleaningJournalEntry): String =
        fields(e).joinToString("") { "${it.length}:$it" }

    fun decode(value: String): CleaningJournalEntry? = runCatching {
        var offset = 0
        val f = List(10) {
            val sep = value.indexOf(':', offset)
            require(sep >= offset)
            val len = value.substring(offset, sep).toInt()
            val start = sep + 1
            val end = start + len
            require(end <= value.length)
            offset = end
            value.substring(start, end)
        }
        require(offset == value.length)
        CleaningJournalEntry(
            id = f[0],
            queryString = f[1],
            title = f[2],
            note = f[3],
            actionType = JournalAction.fromName(f[4]),
            relatedHistoryId = f[5].ifBlank { null },
            relatedFavoriteId = f[6].ifBlank { null },
            relatedPersonalPresetId = f[7].ifBlank { null },
            createdAt = f[8].toLong(),
            updatedAt = f[9].toLong()
        )
    }.getOrNull()
}
