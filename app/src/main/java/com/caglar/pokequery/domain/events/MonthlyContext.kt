package com.caglar.pokequery.domain.events

import androidx.annotation.StringRes
import com.caglar.pokequery.AppVersion
import java.util.Calendar

data class MonthlyContext(
    val month: Int,
    val year: Int,
    @StringRes val titleRes: Int,
    val contextType: MonthlyContextType,
    val pokemonName: String? = null,
    @StringRes val noteRes: Int,
    val lastUpdatedInAppVersion: String,
    val confidence: MonthlyConfidence = MonthlyConfidence.MANUAL
) {
    val isManual: Boolean get() = confidence == MonthlyConfidence.MANUAL
}

enum class MonthlyContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }
enum class MonthlyConfidence { MANUAL, UNVERIFIED, CONFIRMED }

object MonthlyContextRepository {
    val current: MonthlyContext? = MonthlyContext(
        month = 7,
        year = 2026,
        titleRes = com.caglar.pokequery.R.string.event_context_community_day,
        contextType = MonthlyContextType.COMMUNITY_DAY,
        noteRes = com.caglar.pokequery.R.string.event_context_community_day_note,
        lastUpdatedInAppVersion = AppVersion.versionName,
        confidence = MonthlyConfidence.MANUAL
    )

    fun currentWithStaleness(now: Calendar = Calendar.getInstance()): MonthlyContextView? {
        val note = current ?: return null
        val noteYearMonth = note.year * 12 + (note.month - 1)
        val nowYearMonth = now.get(Calendar.YEAR) * 12 + now.get(Calendar.MONTH)
        val stale = noteYearMonth < nowYearMonth
        return MonthlyContextView(note = note, isStale = stale)
    }

    @StringRes
    fun noNoteMessageRes(): Int = com.caglar.pokequery.R.string.event_context_no_note
}

data class MonthlyContextView(val note: MonthlyContext, val isStale: Boolean) {
    @get:StringRes
    val disclaimerRes: Int
        get() = if (isStale) {
            com.caglar.pokequery.R.string.event_context_disclaimer_stale
        } else {
            com.caglar.pokequery.R.string.event_context_disclaimer_fresh
        }
}
