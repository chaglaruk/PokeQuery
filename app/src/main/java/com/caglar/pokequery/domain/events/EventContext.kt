package com.caglar.pokequery.domain.events

import androidx.annotation.StringRes

data class EventContext(
    val id: String,
    @StringRes val titleRes: Int? = null,
    val titleText: String? = null,
    val contextType: EventContextType,
    @StringRes val noteRes: Int? = null,
    val noteText: String? = null,
    val month: Int? = null,
    val year: Int? = null,
    val isManual: Boolean = true
)

enum class EventContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }

object EventContextRepository {
    val entries: List<EventContext> = listOf(
        EventContext(
            id = "event_candy_prep_bonus",
            titleRes = com.caglar.pokequery.R.string.event_context_candy_events,
            contextType = EventContextType.GENERIC_EVENT,
            noteRes = com.caglar.pokequery.R.string.event_context_candy_events_note
        )
    )

    fun all(): List<EventContext> = entries

    @StringRes
    fun disclaimerRes(): Int = com.caglar.pokequery.R.string.event_context_disclaimer

    fun combined(manualMonthly: MonthlyContext? = MonthlyContextRepository.current): ContextFeedState =
        ContextFeedState.OfflineOnly(manualMonthly)
}

sealed class ContextFeedState {
    abstract val monthly: MonthlyContext?
    abstract val events: List<EventContext>

    data class OfflineOnly(
        override val monthly: MonthlyContext?,
        override val events: List<EventContext> = EventContextRepository.entries
    ) : ContextFeedState()

    data class Loading(
        override val monthly: MonthlyContext? = MonthlyContextRepository.current,
        override val events: List<EventContext> = EventContextRepository.entries
    ) : ContextFeedState()

    data class Online(
        override val monthly: MonthlyContext?,
        override val events: List<EventContext>,
        val lastUpdated: String
    ) : ContextFeedState()

    data class StaleCache(
        override val monthly: MonthlyContext?,
        override val events: List<EventContext>,
        val lastUpdated: String
    ) : ContextFeedState()

    data class Invalid(
        override val monthly: MonthlyContext?,
        override val events: List<EventContext> = EventContextRepository.entries
    ) : ContextFeedState()
}
