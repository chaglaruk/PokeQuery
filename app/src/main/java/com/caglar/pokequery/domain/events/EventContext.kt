package com.caglar.pokequery.domain.events

import androidx.annotation.StringRes

data class EventContext(
    val id: String,
    @StringRes val titleRes: Int,
    val contextType: EventContextType,
    @StringRes val noteRes: Int,
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

    fun combined(
        manualMonthly: MonthlyContext? = MonthlyContextRepository.current
    ): ContextFeedState {
        return ContextFeedState.OfflineOnly(manualMonthly)
    }
}

sealed class ContextFeedState {
    abstract val monthly: MonthlyContext?

    data class OfflineOnly(
        override val monthly: MonthlyContext?
    ) : ContextFeedState()
}
