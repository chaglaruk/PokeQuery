package com.caglar.pokequery.domain.events

import com.caglar.pokequery.AppVersion
import java.util.Calendar

/**
 * v0.6.1 — Manual "This month's Community Day" context note.
 *
 * A lightweight, OFFLINE-ONLY context note that is updated manually per app release. This is
 * separate from any live event calendar: there is NO network. The note can go stale, so the UI
 * always discloses "Manual app note" and shows "This note may be outdated." when the note's
 * month/year is in the past relative to the device clock.
 *
 * No official assets, no event logos — text-only.
 */
data class MonthlyContext(
    val month: Int,          // 1..12
    val year: Int,
    val title: String,
    val contextType: MonthlyContextType,
    val pokemonName: String? = null,
    val note: String,
    val lastUpdatedInAppVersion: String,
    val confidence: MonthlyConfidence = MonthlyConfidence.MANUAL
) {
    val isManual: Boolean get() = confidence == MonthlyConfidence.MANUAL
}

enum class MonthlyContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }
enum class MonthlyConfidence { MANUAL, UNVERIFIED, CONFIRMED }

object MonthlyContextRepository {

    /**
     * Manually maintained current note. Kept generic and intentionally not tied to a specific
     * real event date so it never claims confirmed/official live data. Returned as a single
     * optional entry.
     *
     * To update a note in a future release, edit [current] here. There is no network fetch.
     */
    val current: MonthlyContext? = MonthlyContext(
        month = 6,
        year = 2026,
        title = "Community Day context",
        contextType = MonthlyContextType.COMMUNITY_DAY,
        note = "During Community Day, candy and evolution bonuses can make Candy Prep and " +
            "cleanup-style reviews more useful. This is a manual app note — PokeQuery does not " +
            "fetch live event data and the note may be outdated.",
        lastUpdatedInAppVersion = AppVersion.versionName,
        confidence = MonthlyConfidence.MANUAL
    )

    /**
     * Returns the current note together with whether it is stale (its month/year precedes the
     * device's current month). A null note yields null (caller shows a neutral fallback).
     */
    fun currentWithStaleness(now: Calendar = Calendar.getInstance()): MonthlyContextView? {
        val note = current ?: return null
        val noteYearMonth = note.year * 12 + (note.month - 1)
        val nowYearMonth = now.get(Calendar.YEAR) * 12 + now.get(Calendar.MONTH)
        val stale = noteYearMonth < nowYearMonth
        return MonthlyContextView(note = note, isStale = stale)
    }

    fun noNoteMessage(): String = "No current manual event note."
}

data class MonthlyContextView(val note: MonthlyContext, val isStale: Boolean) {
    val disclaimer: String
        get() = if (isStale) {
            "Manual app note. This note may be outdated. PokeQuery does not fetch event data."
        } else {
            "Manual app note. This is not live event data. PokeQuery does not fetch event data."
        }
}
