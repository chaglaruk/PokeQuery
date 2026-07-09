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
    val titleTextDe: String? = null,
    val titleTextEs: String? = null,
    val titleTextFr: String? = null,
    val titleTextIt: String? = null,
    val contextType: EventContextType,
    val status: EventStatus = EventStatus.CURRENT,
    @StringRes val noteRes: Int? = null,
    val noteText: String? = null,
    val noteTextTr: String? = null,
    val noteTextDe: String? = null,
    val noteTextEs: String? = null,
    val noteTextFr: String? = null,
    val noteTextIt: String? = null,
    val month: Int? = null,
    val year: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val startText: String? = null,
    val endText: String? = null,
    val summaryText: String? = null,
    val summaryTextTr: String? = null,
    val summaryTextDe: String? = null,
    val summaryTextEs: String? = null,
    val summaryTextFr: String? = null,
    val summaryTextIt: String? = null,
    val prepText: String? = null,
    val prepTextTr: String? = null,
    val prepTextDe: String? = null,
    val prepTextEs: String? = null,
    val prepTextFr: String? = null,
    val prepTextIt: String? = null,
    val suggestedSearch: String? = null,
    val eventNotesText: String? = null,
    val eventNotesTextTr: String? = null,
    val eventNotesTextDe: String? = null,
    val eventNotesTextEs: String? = null,
    val eventNotesTextFr: String? = null,
    val eventNotesTextIt: String? = null,
    val featuredPokemon: String? = null,
    val featuredPokemonTr: String? = null,
    val featuredPokemonDe: String? = null,
    val featuredPokemonEs: String? = null,
    val featuredPokemonFr: String? = null,
    val featuredPokemonIt: String? = null,
    val boostedPokemonText: String? = null,
    val boostedPokemonTextTr: String? = null,
    val boostedPokemonTextDe: String? = null,
    val boostedPokemonTextEs: String? = null,
    val boostedPokemonTextFr: String? = null,
    val boostedPokemonTextIt: String? = null,
    val bonusesText: String? = null,
    val bonusesTextTr: String? = null,
    val bonusesTextDe: String? = null,
    val bonusesTextEs: String? = null,
    val bonusesTextFr: String? = null,
    val bonusesTextIt: String? = null,
    val raidsText: String? = null,
    val raidsTextTr: String? = null,
    val raidsTextDe: String? = null,
    val raidsTextEs: String? = null,
    val raidsTextFr: String? = null,
    val raidsTextIt: String? = null,
    val researchText: String? = null,
    val researchTextTr: String? = null,
    val researchTextDe: String? = null,
    val researchTextEs: String? = null,
    val researchTextFr: String? = null,
    val researchTextIt: String? = null,
    val pokemon: List<EventPokemonEntry> = emptyList(),
    val themeKey: String = "generic_event",
    val isManual: Boolean = true,
    val importanceTier: String = "STANDARD",
    val eventCategory: String? = null
)

data class EventPokemonEntry(
    val name: String,
    val nameTr: String? = null,
    val nameDe: String? = null,
    val nameEs: String? = null,
    val nameFr: String? = null,
    val nameIt: String? = null,
    val source: String = "unknown/check-in-game",
    val sourceTr: String? = null,
    val sourceDe: String? = null,
    val sourceEs: String? = null,
    val sourceFr: String? = null,
    val sourceIt: String? = null,
    val shinyAvailable: Boolean = false,
    val note: String? = null,
    val noteTr: String? = null,
    val noteDe: String? = null,
    val noteEs: String? = null,
    val noteFr: String? = null,
    val noteIt: String? = null,
    val badges: String? = null,
    val badgesTr: String? = null,
    val badgesDe: String? = null,
    val badgesEs: String? = null,
    val badgesFr: String? = null,
    val badgesIt: String? = null,
    val spriteKey: String? = null
)

enum class EventContextType { COMMUNITY_DAY, SPOTLIGHT_HOUR, GENERIC_EVENT }

enum class EventStatus { CURRENT, UPCOMING, ENDED }

private val isoDatePattern = Regex("""\d{4}-\d{2}-\d{2}""")

private fun todayIsoDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

private fun String?.validIsoDate(): String? =
    this?.takeIf { isoDatePattern.matches(it) }

object EventCategory {
    const val MAJOR_GAMEPLAY = "MAJOR_GAMEPLAY"
    const val LIMITED_GAMEPLAY = "LIMITED_GAMEPLAY"
    const val ROUTINE_ROTATION = "ROUTINE_ROTATION"
    const val SEASON_GBL = "SEASON_GBL"
    const val RAID_ROTATION = "RAID_ROTATION"
    const val NEWS_PROMO = "NEWS_PROMO"
    const val REWARD_DROP = "REWARD_DROP"
    const val ANNOUNCEMENT = "ANNOUNCEMENT"
}

fun EventContext.determineCategory(): String {
    val cat = eventCategory?.uppercase()?.trim()
    if (!cat.isNullOrBlank()) {
        return cat
    }
    val title = (titleText ?: "").lowercase(Locale.US)
    val kind = themeKey.lowercase(Locale.US)
    return when {
        title.contains("twitch drops") || title.contains("prime gaming") || title.contains("reward") || title.contains("drop") -> EventCategory.REWARD_DROP
        title.contains("save the date") || title.contains("save-the-date") || title.contains("wallpapers") || title.contains("diary") || title.contains("promo") || title.contains("store") || title.contains("coupon") || title.contains("code") -> EventCategory.NEWS_PROMO
        title.contains("lego") || title.contains("art") || title.contains("birthday") || title.contains("announcement") || title.contains("partnership") || title.contains("showcase") || title.contains("professor willow") || title.contains("scopely") -> EventCategory.ANNOUNCEMENT
        title.contains("gbl") || title.contains("go battle league") || title.contains("season:") || title.contains("forever forward") || title.contains("season of") || title.contains("league") || title.contains("cup") -> EventCategory.SEASON_GBL
        title.contains("spotlight hour") || title.contains("raid hour") || title.contains("max mondays") || title.contains("max monday") || kind == "spotlight_hour" -> EventCategory.ROUTINE_ROTATION
        !title.contains("raid day") && (title.contains("in 5-star") || title.contains("in mega raids") || title.contains("in shadow raids") || title.contains("5-star raid") || title.contains("mega raid") || title.contains("shadow raid") || title.contains("raid rotation")) -> EventCategory.RAID_ROTATION
        title.contains("go fest") || title.contains("go tour") || title.contains("safari zone") || title.contains("community day") || title.contains("road of legends") || title.contains("global") || kind == "community_day" -> EventCategory.MAJOR_GAMEPLAY
        else -> EventCategory.LIMITED_GAMEPLAY
    }
}

/**
 * Selects the single main event to feature prominently on the Event Guide.
 *
 * Strategy: prefer MAJOR_GAMEPLAY / LIMITED_GAMEPLAY.
 * Prefer current or upcoming starting within 21 days.
 * Pure function — unit-testable without Android.
 */
fun selectMainEvent(events: List<EventContext>, todayIsoDate: String = todayIsoDate()): EventContext? {
    if (events.isEmpty()) return null
    return events
        .filter { it.effectiveStatus(todayIsoDate) != EventStatus.ENDED }
        .sortedWith(compareBy<EventContext> { it.heroScore(todayIsoDate) }
            .thenBy { if (it.effectiveStatus(todayIsoDate) == EventStatus.CURRENT) it.endDate ?: "9999-12-31" else it.startDate ?: "9999-12-31" })
        .firstOrNull()
}

/** Lower score = higher priority for featured selection. */
fun EventContext.heroScore(todayIso: String): Int {
    val status = effectiveStatus(todayIso)
    if (status == EventStatus.ENDED) return 9999

    val cat = determineCategory()
    val startDiff = daysBetween(todayIso, startDate)
    val isNear = startDiff in 0..21

    return when (cat) {
        EventCategory.MAJOR_GAMEPLAY -> {
            if (status == EventStatus.CURRENT) 10
            else if (isNear) 30
            else 50
        }
        EventCategory.LIMITED_GAMEPLAY -> {
            if (status == EventStatus.CURRENT) 20
            else if (isNear) 40
            else 55
        }
        EventCategory.RAID_ROTATION, EventCategory.ROUTINE_ROTATION, EventCategory.SEASON_GBL -> {
            if (status == EventStatus.CURRENT) 60
            else 70
        }
        EventCategory.REWARD_DROP, EventCategory.NEWS_PROMO, EventCategory.ANNOUNCEMENT -> {
            100
        }
        else -> 80
    }
}

/** Legacy featuredScore utility keeping backward compatibility. */
fun EventContext.featuredScore(todayIso: String): Int {
    return heroScore(todayIso)
}

/** Compute days between two ISO date strings. Positive means to is after from. */
fun daysBetween(from: String, to: String?): Long {
    if (to == null) return 999
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d1 = sdf.parse(from)
        val d2 = sdf.parse(to)
        if (d1 == null || d2 == null) 999 else (d2.time - d1.time) / (1000 * 60 * 60 * 24)
    } catch (_: Exception) { 999 }
}

/**
 * Groups active events into display sections for the Event Guide.
 * Pure function — unit-testable without Android.
 */
data class EventSections(
    val featured: EventContext?,
    val importantUpcoming: List<EventContext>,
    val happeningNow: List<EventContext>,
    val rotations: List<EventContext>,
    val news: List<EventContext>,
    val allActive: List<EventContext>
)

fun groupEvents(events: List<EventContext>, todayIso: String = todayIsoDate()): EventSections {
    val active = events.filter { it.effectiveStatus(todayIso) != EventStatus.ENDED }
    val featured = selectMainEvent(active, todayIso)
    val rest = active.filter { it.id != featured?.id }

    val happeningNow = rest
        .filter {
            it.effectiveStatus(todayIso) == EventStatus.CURRENT &&
            it.determineCategory() in listOf(EventCategory.MAJOR_GAMEPLAY, EventCategory.LIMITED_GAMEPLAY)
        }
        .sortedBy { it.endDate ?: "9999-12-31" }

    val importantUpcoming = rest
        .filter {
            it.effectiveStatus(todayIso) == EventStatus.UPCOMING &&
            it.determineCategory() in listOf(EventCategory.MAJOR_GAMEPLAY, EventCategory.LIMITED_GAMEPLAY) &&
            daysBetween(todayIso, it.startDate) in 0..21
        }
        .sortedWith(compareBy<EventContext> {
            if (it.determineCategory() == EventCategory.MAJOR_GAMEPLAY) 0 else 1
        }.thenBy { it.startDate ?: "9999-12-31" })

    val rotations = rest
        .filter {
            it.determineCategory() in listOf(EventCategory.SEASON_GBL, EventCategory.ROUTINE_ROTATION, EventCategory.RAID_ROTATION)
        }
        .sortedWith(compareBy<EventContext> {
            if (it.effectiveStatus(todayIso) == EventStatus.CURRENT) 0 else 1
        }.thenBy { it.startDate ?: "9999-12-31" })

    val newsItems = rest
        .filter {
            it.determineCategory() in listOf(EventCategory.REWARD_DROP, EventCategory.NEWS_PROMO, EventCategory.ANNOUNCEMENT)
        }
        .sortedBy { it.startDate ?: "9999-12-31" }

    val allActive = active
        .sortedWith(compareBy<EventContext> {
            if (it.effectiveStatus(todayIso) == EventStatus.CURRENT) 0 else 1
        }.thenBy { it.startDate ?: "9999-12-31" })

    return EventSections(featured, importantUpcoming, happeningNow, rotations, newsItems, allActive)
}

// Localized string helpers for EventContext and EventPokemonEntry
// These are public so they can be used by widget providers and other components.

private fun localized(
    en: String?,
    tr: String?,
    de: String?,
    es: String?,
    fr: String?,
    it: String?,
    lang: String = Locale.getDefault().language
): String = when (lang) {
    "tr" -> tr
    "de" -> de
    "es" -> es
    "fr" -> fr
    "it" -> it
    else -> en
}.takeUnless { it.isNullOrBlank() } ?: en.orEmpty()

fun EventContext.localizedTitle(lang: String = Locale.getDefault().language): String =
    localized(titleText, titleTextTr, titleTextDe, titleTextEs, titleTextFr, titleTextIt, lang)

fun EventContext.localizedFeatured(lang: String = Locale.getDefault().language): String =
    localized(featuredPokemon, featuredPokemonTr, featuredPokemonDe, featuredPokemonEs, featuredPokemonFr, featuredPokemonIt, lang)

fun EventContext.localizedResearch(lang: String = Locale.getDefault().language): String =
    localized(researchText, researchTextTr, researchTextDe, researchTextEs, researchTextFr, researchTextIt, lang)

fun EventContext.localizedBonuses(lang: String = Locale.getDefault().language): String =
    localized(bonusesText, bonusesTextTr, bonusesTextDe, bonusesTextEs, bonusesTextFr, bonusesTextIt, lang)

fun EventContext.localizedPrep(lang: String = Locale.getDefault().language): String =
    localized(prepText, prepTextTr, prepTextDe, prepTextEs, prepTextFr, prepTextIt, lang)

fun EventContext.localizedNotes(lang: String = Locale.getDefault().language): String =
    localized(eventNotesText, eventNotesTextTr, eventNotesTextDe, eventNotesTextEs, eventNotesTextFr, eventNotesTextIt, lang)

fun EventContext.localizedRaids(lang: String = Locale.getDefault().language): String =
    localized(raidsText, raidsTextTr, raidsTextDe, raidsTextEs, raidsTextFr, raidsTextIt, lang)

fun EventPokemonEntry.localizedName(lang: String = Locale.getDefault().language): String =
    localized(name, nameTr, nameDe, nameEs, nameFr, nameIt, lang)

fun EventPokemonEntry.localizedSource(lang: String = Locale.getDefault().language): String =
    localized(source, sourceTr, sourceDe, sourceEs, sourceFr, sourceIt, lang)

fun EventPokemonEntry.localizedNote(lang: String = Locale.getDefault().language): String =
    localized(note, noteTr, noteDe, noteEs, noteFr, noteIt, lang)

fun EventPokemonEntry.localizedBadges(lang: String = Locale.getDefault().language): String =
    localized(badges, badgesTr, badgesDe, badgesEs, badgesFr, badgesIt, lang)

fun EventContext.dateLabel(lang: String = Locale.getDefault().language): String? = when {
    isoMonthDay(startDate) != null && isoMonthDay(endDate) != null ->
        localizedDateRange(isoMonthDay(startDate)!!, isoMonthDay(endDate)!!, lang)
    isoMonthDay(startDate) != null -> localizedSingleDate(isoMonthDay(startDate)!!, lang)
    isoMonthDay(endDate) != null -> localizedSingleDate(isoMonthDay(endDate)!!, lang)
    !startText.isNullOrBlank() && !endText.isNullOrBlank() -> "$startText – $endText"
    !startText.isNullOrBlank() -> startText
    !endText.isNullOrBlank() -> endText
    month != null && year != null -> "$month/$year"
    else -> null
}

private fun isoMonthDay(value: String?): Pair<Int, Int>? {
    val text = value?.takeIf { it.length >= 10 } ?: return null
    val month = text.substring(5, 7).toIntOrNull() ?: return null
    val day = text.substring(8, 10).toIntOrNull() ?: return null
    return month to day
}

private fun localizedDateRange(start: Pair<Int, Int>, end: Pair<Int, Int>, lang: String): String =
    if (start.first == end.first) {
        when (lang) {
            "tr" -> "${start.second}–${end.second} ${monthName(start.first, lang)}"
            "de" -> "${start.second}.–${end.second}. ${monthName(start.first, lang)}"
            "es" -> "${start.second}–${end.second} de ${monthName(start.first, lang)}"
            "fr", "it" -> "${start.second}–${end.second} ${monthName(start.first, lang)}"
            else -> "${monthName(start.first, lang)} ${start.second}–${end.second}"
        }
    } else {
        "${localizedSingleDate(start, lang)} – ${localizedSingleDate(end, lang)}"
    }

private fun localizedSingleDate(date: Pair<Int, Int>, lang: String): String = when (lang) {
    "tr" -> "${date.second} ${monthName(date.first, lang)}"
    "de" -> "${date.second}. ${monthName(date.first, lang)}"
    "es" -> "${date.second} de ${monthName(date.first, lang)}"
    "fr", "it" -> "${date.second} ${monthName(date.first, lang)}"
    else -> "${monthName(date.first, lang)} ${date.second}"
}

private fun monthName(month: Int, lang: String): String {
    val names = when (lang) {
        "tr" -> listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
        "de" -> listOf("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember")
        "es" -> listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
        "fr" -> listOf("janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre")
        "it" -> listOf("gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno", "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre")
        else -> listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }
    return names.getOrElse(month - 1) { month.toString() }
}

fun EventContext.effectiveStatus(todayIsoDate: String = todayIsoDate()): EventStatus {
    val today = todayIsoDate.validIsoDate() ?: return status
    val start = startDate.validIsoDate()
    val end = endDate.validIsoDate()
    return when {
        start != null && today.compareTo(start) < 0 -> EventStatus.UPCOMING
        end != null && today.compareTo(end) > 0 -> EventStatus.ENDED
        start != null && end != null -> EventStatus.CURRENT
        start != null && today.compareTo(start) >= 0 -> EventStatus.CURRENT
        else -> status
    }
}

/**
 * Computes a compact remaining-time label for this event, e.g. "3D 14H left", "Live now",
 * "Coming up", or "Ended". Pure function — testable without Android.
 *
 * @param todayIso ISO date string for "today" (yyyy-MM-dd)
 * @param nowMillis current time in epoch millis (defaults to system time)
 */
fun EventContext.remainingTimeLabel(
    todayIso: String = todayIsoDate(),
    nowMillis: Long = System.currentTimeMillis(),
    lang: String = Locale.getDefault().language
): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val status = effectiveStatus(todayIso)
    return when (status) {
        EventStatus.ENDED -> localizedTimerLabel("ended", lang = lang)
        EventStatus.UPCOMING -> {
            val startMs = startDate?.let { runCatching { sdf.parse(it)?.time }.getOrNull() }
            if (startMs != null && startMs > nowMillis) {
                val diffMs = startMs - nowMillis
                formatRemainingTime(diffMs, prefix = false, lang = lang)
            } else {
                localizedTimerLabel("coming_up", lang = lang)
            }
        }
        EventStatus.CURRENT -> {
            val endMs = endDate?.let { runCatching { sdf.parse(it)?.time }.getOrNull() }
            if (endMs != null) {
                // End date is inclusive — event ends at end of that day (23:59:59)
                val endOfDayMs = endMs + 24 * 60 * 60 * 1000 - 1
                if (endOfDayMs > nowMillis) {
                    formatRemainingTime(endOfDayMs - nowMillis, prefix = true, lang = lang)
                } else {
                    localizedTimerLabel("live_now", lang = lang)
                }
            } else {
                localizedTimerLabel("live_now", lang = lang)
            }
        }
    }
}

private fun formatRemainingTime(diffMs: Long, prefix: Boolean, lang: String): String {
    val totalHours = (diffMs / (1000 * 60 * 60)).toInt()
    val days = totalHours / 24
    val hours = totalHours % 24
    val timeStr = when {
        days > 0 && hours > 0 -> "${days}D ${hours}H"
        days > 0 -> "${days}D"
        hours > 0 -> "${hours}H"
        else -> "<1H"
    }
    return if (prefix) {
        localizedTimerLabel("remaining", timeStr, lang)
    } else {
        localizedTimerLabel("starts_in", timeStr, lang)
    }
}

private fun localizedTimerLabel(key: String, arg: String = "", lang: String): String = when (key) {
    "ended" -> when (lang) {
        "tr" -> "Sona erdi"
        "de" -> "Beendet"
        "es" -> "Finalizado"
        "fr" -> "Terminé"
        "it" -> "Terminato"
        else -> "Ended"
    }
    "coming_up" -> when (lang) {
        "tr" -> "Yakında"
        "de" -> "Demnächst"
        "es" -> "Próximamente"
        "fr" -> "Bientôt"
        "it" -> "In arrivo"
        else -> "Coming up"
    }
    "live_now" -> when (lang) {
        "tr" -> "Şu an canlı"
        "de" -> "Jetzt live"
        "es" -> "En vivo"
        "fr" -> "En cours"
        "it" -> "In corso"
        else -> "Live now"
    }
    "remaining" -> when (lang) {
        "tr" -> "$arg kaldı"
        "de" -> "$arg übrig"
        "es" -> "$arg restante"
        "fr" -> "$arg restant"
        "it" -> "$arg rimanente"
        else -> "$arg left"
    }
    "starts_in" -> when (lang) {
        "tr" -> "$arg sonra"
        "de" -> "in $arg"
        "es" -> "en $arg"
        "fr" -> "dans $arg"
        "it" -> "tra $arg"
        else -> "in $arg"
    }
    else -> arg
}

/**
 * Filters out events that have ended based on their date window. Pure function.
 */
fun activeEvents(events: List<EventContext>, todayIso: String = todayIsoDate()): List<EventContext> =
    events.filter { it.effectiveStatus(todayIso) != EventStatus.ENDED }
        .sortedWith(compareBy<EventContext> {
            if (it.effectiveStatus(todayIso) == EventStatus.CURRENT) 0 else 1
        }.thenBy {
            it.startDate ?: "9999-12-31"
        })

object EventContextRepository {
    val entries: List<EventContext> = listOf(
        EventContext(
            id = "event-go-fest-global",
            titleText = "GO Fest 2026: Global",
            titleTextTr = "GO Fest 2026: Küresel",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.UPCOMING,
            noteText = "A large global event with special spawns, raids, research, and bonuses.",
            noteTextTr = "Özel çıkışlar, akınlar, araştırmalar ve ek ödüller içeren büyük bir küresel etkinlik.",
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
            boostedPokemonTextTr = "Etkinlik çıkışları ve küresel öne çıkan Pokémonlar",
            bonusesText = "Special spawns, raids, research, and event bonuses",
            bonusesTextTr = "Özel çıkışlar, akınlar, araştırmalar ve etkinlik ödülleri",
            raidsText = "Check raid catches separately before cleanup.",
            raidsTextTr = "Temizlikten önce akın yakalamalarını ayrı kontrol et.",
            researchText = "Research rewards can have good IV floors; review before transfer.",
            researchTextTr = "Araştırma ödüllerinde iyi IV tabanı olabilir; transferden önce incele.",
            themeKey = "raid"
        ),
        EventContext(
            id = "event-go-fest-global-2026",
            titleText = "GO Fest 2026: Global",
            titleTextTr = "GO Fest 2026: Küresel",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.UPCOMING,
            noteText = "Official event window: July 11–12, 2026, 10:00 a.m. to 7:00 p.m. local time.",
            noteTextTr = "Resmi etkinlik aralığı: 11–12 Temmuz 2026, 10.00–19.00 yerel saat.",
            month = 7,
            year = 2026,
            startDate = "2026-07-11",
            endDate = "2026-07-12",
            startText = "July 11",
            endText = "July 12",
            featuredPokemon = "Global GO Fest featuring Mewtwo and Zeraora, with raid, research, shiny, background, and storage prep checks.",
            featuredPokemonTr = "Mewtwo ve Zeraora odaklı küresel GO Fest; akın, araştırma, shiny, arka plan ve depo hazırlığı kontrolleri.",
            boostedPokemonText = "Mewtwo, Zeraora, team-hat Pikachu, raid catches, research rewards, and trade candidates.",
            boostedPokemonTextTr = "Mewtwo, Zeraora, takım şapkalı Pikachu, akın yakalamaları, araştırma ödülleri ve takas adayları.",
            bonusesText = "Free for all Trainers during the event weekend, with Special Research, additional bonuses, and increased shiny chance.",
            bonusesTextTr = "Etkinlik hafta sonu tüm Eğitmenlere ücretsiz; Özel Araştırma, ek bonuslar ve artan shiny şansı içerir.",
            raidsText = "Review Mewtwo and other raid catches separately for IVs, shiny status, and special backgrounds.",
            raidsTextTr = "Mewtwo ve diğer akın yakalamalarını IV, shiny durumu ve özel arka plan için ayrı incele.",
            researchText = "Special Research leads to Zeraora. Do not mix research reward checks with bulk cleanup.",
            researchTextTr = "Özel Araştırma Zeraora'ya götürür. Araştırma ödülü kontrollerini toplu temizlikle karıştırma.",
            summaryText = "GO Fest creates many valuable catches quickly. Protect shiny, legendary, mythical, costume, background, raid, and trade candidates first.",
            summaryTextTr = "GO Fest kısa sürede çok değerli yakalama oluşturur. Önce shiny, efsanevi, özel, kostüm, arka plan, akın ve takas adaylarını koru.",
            prepText = "Open storage before the event. After each session, review raid catches and tagged keepers before cleanup.",
            prepTextTr = "Etkinlikten önce depo aç. Her oturumdan sonra temizlikten önce akın yakalamalarını ve etiketli saklanacakları incele.",
            suggestedSearch = "age0-2&!favorite&!shiny&!legendary&!mythical&!ultrabeast&!costume&!background&!locationbackground&!specialbackground&!traded",
            eventNotesText = "Keep shiny, legendary, mythical, costume, special-background, raid, research, and trade candidates before any transfer.",
            eventNotesTextTr = "Transferden önce shiny, efsanevi, özel, kostüm, özel arka plan, akın, araştırma ve takas adaylarını sakla.",
            themeKey = "raid",
            pokemon = listOf(
                EventPokemonEntry(
                    name = "Mewtwo",
                    nameTr = "Mewtwo",
                    source = "Super Mega Raids / special background",
                    sourceTr = "Süper Mega Akınlar / özel arka plan",
                    shinyAvailable = true,
                    note = "Check raid IVs, shiny status, and special background before cleanup.",
                    noteTr = "Temizlikten önce akın IV'lerini, shiny durumunu ve özel arka planı kontrol et.",
                    badges = "Shiny, Raid, Background",
                    badgesTr = "Shiny çıkabilir, Akın, Özel arka plan",
                    spriteKey = "mewtwo"
                ),
                EventPokemonEntry(
                    name = "Zeraora",
                    nameTr = "Zeraora",
                    source = "Special Research",
                    sourceTr = "Özel Araştırma",
                    shinyAvailable = false,
                    note = "Research reward encounter; keep it out of cleanup flows.",
                    noteTr = "Araştırma ödülü karşılaşmasıdır; temizlik akışlarından ayrı tut.",
                    badges = "Research, Mythical check",
                    badgesTr = "Araştırma, özel kontrol",
                    spriteKey = "zeraora"
                ),
                EventPokemonEntry(
                    name = "Pikachu",
                    nameTr = "Pikachu",
                    source = "team-hat costume",
                    sourceTr = "takım şapkalı kostüm",
                    shinyAvailable = true,
                    note = "Team-hat Pikachu is a costume keep-check.",
                    noteTr = "Takım şapkalı Pikachu kostümlü saklama kontrolüdür.",
                    badges = "Shiny, Costume",
                    badgesTr = "Shiny çıkabilir, Kostümlü",
                    spriteKey = "pikachu"
                ),
                EventPokemonEntry(
                    name = "Raid and trade candidates",
                    nameTr = "Akın ve takas adayları",
                    source = "raids / trades / storage",
                    sourceTr = "akınlar / takaslar / depo",
                    shinyAvailable = true,
                    note = "Tag good raid catches and trade candidates before using cleanup searches.",
                    noteTr = "Temizlik aramalarından önce iyi akın yakalamalarını ve takas adaylarını etiketle.",
                    badges = "Raid, Trade, Storage",
                    badgesTr = "Akın, Takas, Depo",
                    spriteKey = "necrozma"
                )
            )
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
