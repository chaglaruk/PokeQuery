package com.caglar.pokequery.domain.events

import androidx.annotation.StringRes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val startDate: String? = null,
    val endDate: String? = null,
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
    val boostedPokemonText: String? = null,
    val boostedPokemonTextTr: String? = null,
    val bonusesText: String? = null,
    val bonusesTextTr: String? = null,
    val raidsText: String? = null,
    val raidsTextTr: String? = null,
    val researchText: String? = null,
    val researchTextTr: String? = null,
    val themeKey: String = "generic_event",
    val isManual: Boolean = true
)

enum class EventContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }

enum class EventStatus { CURRENT, UPCOMING, ENDED }

private val isoDatePattern = Regex("""\d{4}-\d{2}-\d{2}""")

private fun todayIsoDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

private fun String?.validIsoDate(): String? =
    this?.takeIf { isoDatePattern.matches(it) }

fun EventContext.effectiveStatus(todayIsoDate: String = todayIsoDate()): EventStatus {
    val today = todayIsoDate.validIsoDate() ?: return status
    val start = startDate.validIsoDate()
    val end = endDate.validIsoDate()
    return when {
        start != null && today < start -> EventStatus.UPCOMING
        end != null && today > end -> EventStatus.ENDED
        start != null && end != null -> EventStatus.CURRENT
        start != null && today >= start -> EventStatus.CURRENT
        else -> status
    }
}

/**
 * Selects the single main event to feature prominently on the Event Guide.
 *
 * Strategy (human-friendly, not debug): prefer a [EventStatus.CURRENT] event. If none is current,
 * pick the first UPCOMING event (the nearest one the feed lists). If the list is empty, returns
 * null so the UI can fall back to an honest empty state.
 *
 * Pure function — unit-testable without Android.
 */
fun selectMainEvent(events: List<EventContext>, todayIsoDate: String = todayIsoDate()): EventContext? {
    if (events.isEmpty()) return null
    return events.firstOrNull { it.effectiveStatus(todayIsoDate) == EventStatus.CURRENT }
        ?: events.firstOrNull { it.effectiveStatus(todayIsoDate) == EventStatus.UPCOMING }
        ?: events.first()
}

object EventContextRepository {
    val entries: List<EventContext> = listOf(
        EventContext(
            id = "event-go-fest-global",
            titleText = "GO Fest 2026: Global",
            titleTextTr = "GO Fest 2026: Küresel",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.UPCOMING,
            noteText = "A large global event with special spawns, raids, research, and bonuses.",
            noteTextTr = "Özel çıkışlar, akınlar, araştırmalar ve bonuslar içeren büyük bir global etkinlik.",
            month = 7,
            year = 2026,
            startDate = "2026-07-11",
            endDate = "2026-07-12",
            startText = "July 11",
            endText = "July 12",
            summaryText = "Make storage space before the event, then review recent catches after each play session.",
            summaryTextTr = "Etkinlikten önce depoda yer aç, sonra her oyun oturumundan sonra son yakalamaları incele.",
            prepText = "Tag keepers first. Use PokeQuery after catching to review recent Pokémon before any transfer.",
            prepTextTr = "Önce saklayacaklarını etiketle. Yakaladıktan sonra transferden önce son Pokémonları PokeQuery ile incele.",
            suggestedSearch = "age0-2&!favorite&!shiny&!legendary&!mythical&!costume&!ultrabeast",
            eventNotesText = "Keep shinies, costumes, hundos, PvP candidates, raid catches, and anything tagged for trade.",
            eventNotesTextTr = "Parlakları, kostümlüleri, hundoları, PvP adaylarını, akın yakalamalarını ve takas için etiketlenenleri sakla.",
            boostedPokemonText = "Event spawns and global featured Pokémon",
            boostedPokemonTextTr = "Etkinlik çıkışları ve global öne çıkan Pokémonlar",
            bonusesText = "Special spawns, raids, research, and event bonuses",
            bonusesTextTr = "Özel çıkışlar, akınlar, araştırmalar ve etkinlik bonusları",
            raidsText = "Check raid catches separately before cleanup.",
            raidsTextTr = "Temizlikten önce akın yakalamalarını ayrı kontrol et.",
            researchText = "Research rewards can have good IV floors; review before transfer.",
            researchTextTr = "Araştırma ödüllerinde iyi IV tabanı olabilir; transferden önce incele.",
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
            startDate = "2026-07-21",
            endDate = "2026-07-21",
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
            startDate = "2026-07-25",
            endDate = "2026-07-29",
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
