package com.caglar.pokequery.domain.events

import androidx.annotation.StringRes

data class EventContext(
    val id: String,
    @StringRes val titleRes: Int? = null,
    val titleText: String? = null,
    val titleTextTr: String? = null,
    val contextType: EventContextType,
    val status: EventStatus = EventStatus.CURRENT,
    @StringRes val noteRes: Int? = null,
    val noteText: String? = null,
    val noteTextTr: String? = null,
    val month: Int? = null,
    val year: Int? = null,
    val startText: String? = null,
    val endText: String? = null,
    val summaryText: String? = null,
    val summaryTextTr: String? = null,
    val prepText: String? = null,
    val prepTextTr: String? = null,
    val suggestedSearch: String? = null,
    val eventNotesText: String? = null,
    val eventNotesTextTr: String? = null,
    val featuredPokemon: String? = null,
    val featuredPokemonTr: String? = null,
    val bonusesText: String? = null,
    val bonusesTextTr: String? = null,
    val themeKey: String = "generic_event",
    val isManual: Boolean = true
)

enum class EventContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }

enum class EventStatus { CURRENT, UPCOMING }

/**
 * Selects the single main event to feature prominently on the Event Guide.
 *
 * Strategy (human-friendly, not debug): prefer a [EventStatus.CURRENT] event. If none is current,
 * pick the first UPCOMING event (the nearest one the feed lists). If the list is empty, returns
 * null so the UI can fall back to an honest empty state.
 *
 * Pure function — unit-testable without Android.
 */
fun selectMainEvent(events: List<EventContext>): EventContext? {
    if (events.isEmpty()) return null
    return events.firstOrNull { it.status == EventStatus.CURRENT }
        ?: events.firstOrNull { it.status == EventStatus.UPCOMING }
        ?: events.first()
}

object EventContextRepository {
    val entries: List<EventContext> = listOf(
        EventContext(
            id = "event-go-fest-global",
            titleText = "GO Fest 2026: Global",
            titleTextTr = "GO Fest 2026: Küresel",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.CURRENT,
            noteText = "The biggest event of the year! Special spawns, raids, and bonuses are everywhere.",
            noteTextTr = "Yılın en büyük etkinliği! Özel belirmeler, akınlar ve bonuslar her yerde.",
            month = 7,
            year = 2026,
            startText = "July 11",
            endText = "July 12",
            summaryText = "Storage fills up extremely fast. You will need a lot of free space to enjoy the event without stopping.",
            summaryTextTr = "Depo çok hızlı dolar. Etkinliğin tadını duraksamadan çıkarmak için bolca boş alana ihtiyacın olacak.",
            prepText = "Run Safe Cleanup and clear out all fodder. Make sure to tag your keepers so you don't accidentally transfer them.",
            prepTextTr = "Güvenli Temizliği çalıştır ve gereksizleri yolla. Saklayacaklarını etiketlediğinden emin ol ki yanlışlıkla transfer etmeyesin.",
            suggestedSearch = "age0-2&!favorite&!shiny&!legendary&!mythical&!costume&!ultra beasts",
            eventNotesText = "Review your shiny catches, ultra beasts, and high IVs before clearing. Take advantage of special trades if applicable.",
            eventNotesTextTr = "Temizlemeden önce parlakları, ultra canavarları ve yüksek IV'leri incele. Varsa özel takas haklarını değerlendir.",
            themeKey = "raid"
        ),
        EventContext(
            id = "event-july-cd",
            titleText = "July Community Day",
            titleTextTr = "Temmuz Topluluk Günü",
            contextType = EventContextType.COMMUNITY_DAY,
            status = EventStatus.UPCOMING,
            noteText = "A classic Community Day featuring a rare Pokémon with an exclusive move.",
            noteTextTr = "Nadir bir Pokémon ve özel yetenek içeren klasik bir Topluluk Günü.",
            month = 7,
            year = 2026,
            startText = "July 21",
            endText = "July 21",
            summaryText = "Catching hundreds of the same Pokémon means you can be very strict about what you keep.",
            summaryTextTr = "Aynı Pokémon'dan yüzlerce yakalamak, hangilerini tutacağın konusunda çok daha seçici olmanı sağlar.",
            prepText = "Run Candy Prep. Keep only the highest IVs for raids or specific PvP spreads. Transfer the rest.",
            prepTextTr = "Şeker Hazırlığı çalıştır. Sadece akınlar için en yüksek IV'leri veya özel PvP dağılımlarını sakla. Kalanını yolla.",
            suggestedSearch = "age0&!favorite&!shiny&!3*&!4*",
            eventNotesText = "Check for PvP IVs (low attack, high defense/HP) before transferring, especially if the Pokémon is good in Great or Ultra League.",
            eventNotesTextTr = "Özellikle Süper veya Ultra Lig'de iyiyse, transfer etmeden önce PvP IV'lerini (düşük saldırı, yüksek savunma/HP) kontrol et.",
            themeKey = "community_day"
        ),
        EventContext(
            id = "event-aquatic-paradise",
            titleText = "Aquatic Paradise",
            titleTextTr = "Su Cenneti",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.UPCOMING,
            noteText = "Water-type Pokémon are spawning more frequently. 2x Catch Candy bonus is active.",
            noteTextTr = "Su türü Pokémonlar daha sık beliriyor. 2x Yakalama Şekeri bonusu aktif.",
            month = 7,
            year = 2026,
            startText = "July 25",
            endText = "July 29",
            summaryText = "It's a great time to farm candies for Water-types, but watch your storage space.",
            summaryTextTr = "Su türleri için şeker toplamak adına harika bir zaman, ancak depo alanına dikkat et.",
            prepText = "Clear out non-event fodder beforehand so you can focus on catching Water-types.",
            prepTextTr = "Su türlerine odaklanabilmek için etkinlik öncesinde diğer gereksizleri temizle.",
            suggestedSearch = "water&age0-5&!favorite&!shiny&!legendary",
            eventNotesText = "Keep an eye out for rare Water-types that are usually hard to find. Use Pinap Berries for even more candy.",
            eventNotesTextTr = "Genelde bulması zor olan nadir Su türlerine dikkat et. Daha fazla şeker için Pinap Meyvesi kullan.",
            themeKey = "candy_bonus"
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
