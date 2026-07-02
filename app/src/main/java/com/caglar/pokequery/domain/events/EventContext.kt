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
    val themeKey: String = "generic_event",
    val isManual: Boolean = true
)

enum class EventContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }

enum class EventStatus { CURRENT, UPCOMING }

object EventContextRepository {
    val entries: List<EventContext> = listOf(
        EventContext(
            id = "fallback-community-day-prep",
            titleText = "Community Day prep",
            titleTextTr = "Topluluk Günü hazırlığı",
            contextType = EventContextType.COMMUNITY_DAY,
            status = EventStatus.CURRENT,
            noteText = "Bundled fallback guidance only. Use this when no live or saved public feed is available.",
            noteTextTr = "Yalnızca yerleşik yedek rehberlik. Canlı veya kayıtlı herkese açık akış yoksa kullanılır.",
            month = 7,
            year = 2026,
            startText = "Event day",
            endText = "After event review",
            summaryText = "Community Day bonuses often make catching, evolving, tagging, and candy review more useful.",
            summaryTextTr = "Topluluk Günü bonusları yakalama, evrim, etiketleme ve şeker incelemesini daha yararlı hale getirebilir.",
            prepText = "Before the event, tag keepers and run Candy Prep. After the event, review recent catches before cleanup.",
            prepTextTr = "Etkinlikten önce saklanacakları etiketle ve Şeker Hazırlığı kullan. Etkinlikten sonra yeni yakalananları temizlemeden önce incele.",
            suggestedSearch = "age0-2",
            eventNotesText = "Review favorites, shinies, costumes, high IV, PvP candidates, and traded Pokémon before transfer decisions.",
            eventNotesTextTr = "Transfer kararı vermeden önce favori, parlak, kostümlü, yüksek IV, PvP adayı ve takas edilmiş Pokémonları incele.",
            themeKey = "community_day"
        ),
        EventContext(
            id = "fallback-candy-bonus-prep",
            titleText = "Candy bonus prep",
            titleTextTr = "Şeker bonusu hazırlığı",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.UPCOMING,
            noteText = "Bundled fallback guidance only. Verify the real active bonus in Pokémon GO before acting.",
            noteTextTr = "Yalnızca yerleşik yedek rehberlik. İşlem yapmadan önce gerçek aktif bonusu Pokémon GO içinde doğrula.",
            month = 7,
            year = 2026,
            startText = "Before transfer bonus",
            endText = "After transfer review",
            summaryText = "Transfer-candy windows are useful only after careful review, because cleanup searches are action-adjacent.",
            summaryTextTr = "Transfer şekeri dönemleri ancak dikkatli incelemeden sonra yararlıdır; çünkü temizlik aramaları işlem öncesi kullanılır.",
            prepText = "Use Safe Cleanup and 2x Candy Prep, then manually review protected categories before copying.",
            prepTextTr = "Güvenli Temizlik ve 2x Şeker Hazırlığı kullan; kopyalamadan önce korunan kategorileri elle incele.",
            suggestedSearch = "age0-7&!favorite&!shiny&!legendary&!mythical&!costume",
            eventNotesText = "Do not transfer from the search result blindly. Check tags, costumes, luckies, shadows, purified, and traded status.",
            eventNotesTextTr = "Arama sonucundan körlemesine transfer yapma. Etiket, kostüm, şanslı, gölge, arındırılmış ve takas durumunu kontrol et.",
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
