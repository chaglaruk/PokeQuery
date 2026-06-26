package com.caglar.pokequery.domain.events

/**
 * v0.6.1 — Offline/manual Event Context.
 *
 * Option A of the spec: a LOCAL, manually-maintained set of event context entries bundled with
 * the app. There is NO network: no INTERNET permission, no fetch, no ScrapedDuck/LeekDuck remote
 * provider. These notes can go stale; the UI always discloses "manually maintained and may be
 * outdated" and "No live event data is fetched."
 *
 * A `RemoteEventProvider` is intentionally NOT shipped. See docs for the future-provider plan.
 */
data class EventContext(
    val id: String,
    val title: String,
    val contextType: EventContextType,
    val note: String,
    val isManual: Boolean = true
)

enum class EventContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }

object EventContextRepository {
    /**
     * Manually maintained, local-only event notes. Kept deliberately generic and minimal to avoid
     * implying live data or official status. Updated only in app releases.
     */
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

    fun disclaimer(): String =
        "Event context is manually maintained and may be outdated. No live event data is fetched."
}
