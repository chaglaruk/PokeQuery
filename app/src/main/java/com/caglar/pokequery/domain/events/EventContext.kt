package com.caglar.pokequery.domain.events

import java.io.File

data class EventContext(
    val id: String,
    val title: String,
    val contextType: EventContextType,
    val note: String,
    val isManual: Boolean = true
)

enum class EventContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }

object EventContextRepository {
    val entries: List<EventContext> = listOf(
        EventContext(
            id = "event_candy_prep_bonus",
            title = "Candy events",
            contextType = EventContextType.GENERIC_EVENT,
            note = "During candy-transfer-bonus events, extra candy can make Candy Prep goals more impactful. " +
                "PokeQuery does not fetch live event data — confirm any active event in Pokémon GO itself."
        )
    )

    fun all(): List<EventContext> = entries

    fun feedEntries(feed: EventFeed): List<EventContext> =
        feed.notes.map { entry ->
            EventContext(
                id = entry.id,
                title = entry.title,
                contextType = try { EventContextType.valueOf(entry.contextType) } catch (_: Exception) { EventContextType.GENERIC_EVENT },
                note = entry.note,
                isManual = false
            )
        }

    fun feedMonthly(feed: EventFeed): MonthlyContext? {
        val m = feed.monthlyNote ?: return null
        val type = try { MonthlyContextType.valueOf(m.contextType) } catch (_: Exception) { MonthlyContextType.COMMUNITY_DAY }
        return MonthlyContext(
            month = m.month, year = m.year, title = m.title,
            contextType = type, pokemonName = m.pokemonName,
            note = m.note, lastUpdatedInAppVersion = "feed",
            confidence = MonthlyConfidence.UNVERIFIED
        )
    }

    fun disclaimer(onlineEnabled: Boolean = false): String =
        if (onlineEnabled) {
            "Event context may include online-sourced notes. Always confirm any active event in Pokémon GO itself."
        } else {
            "Event context is manually maintained and may be outdated. No live event data is fetched."
        }

    fun feedAgeMinutes(feed: EventFeed): Long =
        (System.currentTimeMillis() - feed.fetchedAt) / 60_000

    fun combined(
        onlineEnabled: Boolean,
        cacheDir: File,
        manualMonthly: MonthlyContext? = MonthlyContextRepository.current
    ): ContextFeedState {
        if (!onlineEnabled) {
            return ContextFeedState.OfflineOnly(manualMonthly)
        }
        val cached = EventFeedClient.readCached(cacheDir)
        val feedMonthly = cached?.let { feedMonthly(it) }
        val feedNotes = cached?.let { feedEntries(it) } ?: emptyList()
        val fresh = cached != null && EventFeedClient.isCachedFresh(cacheDir)
        val ageMin = cached?.let { feedAgeMinutes(it) } ?: 0L
        val allNotes = entries + feedNotes
        return ContextFeedState.OnlineAvailable(
            monthly = feedMonthly ?: manualMonthly,
            notes = allNotes,
            isFresh = fresh,
            ageMinutes = ageMin,
            fetchedAt = cached?.fetchedAt ?: 0L
        )
    }
}

sealed class ContextFeedState {
    abstract val monthly: MonthlyContext?

    data class OfflineOnly(
        override val monthly: MonthlyContext?
    ) : ContextFeedState()

    data class OnlineAvailable(
        override val monthly: MonthlyContext?,
        val notes: List<EventContext>,
        val isFresh: Boolean,
        val ageMinutes: Long,
        val fetchedAt: Long
    ) : ContextFeedState()
}
