// Event lifecycle — pure date-based status computation.
// Ports Android `EventContext.kt:effectiveStatus`, `activeEvents`, `daysBetween`.
// All functions are pure so they can be unit-tested without Android or DOM.

import type { EventFeedEntry, EventStatus } from '../types'

const ISO_DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/

export interface Clock {
  todayIso(): string
  nowMillis(): number
}

const systemDateFormatter = () => {
  const d = new Date()
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

export const systemClock: Clock = {
  todayIso: systemDateFormatter,
  nowMillis: () => Date.now(),
}

function validIsoDate(value: string | null | undefined): string | null {
  if (!value) return null
  return ISO_DATE_PATTERN.test(value) ? value : null
}

/**
 * Compute the effective status of an event using its date window.
 * 1:1 port of Android `EventContext.effectiveStatus(todayIsoDate)`.
 *
 * - If startDate exists and today < start → UPCOMING
 * - If endDate exists and today > end → ENDED
 * - If both start and end exist (and start <= today <= end) → CURRENT
 * - If only start exists and today >= start → CURRENT
 * - Otherwise, fall back to the static `entry.status` from the feed
 *
 * @param entry event feed entry
 * @param todayIso ISO date `yyyy-MM-dd` representing "today"
 */
export function effectiveStatus(entry: EventFeedEntry, todayIso: string): EventStatus {
  const today = validIsoDate(todayIso)
  if (!today) return entry.status
  const start = validIsoDate(entry.startDate)
  const end = validIsoDate(entry.endDate)
  if (start && today < start) return 'UPCOMING'
  if (end && today > end) return 'ENDED'
  if (start && end) return 'CURRENT'
  if (start && today >= start) return 'CURRENT'
  return entry.status
}

/**
 * Days between two ISO dates. Positive means `to` is after `from`.
 * Returns 999 on parse failure (matches Android convention).
 */
export function daysBetween(from: string, to: string | null | undefined): number {
  if (!to) return 999
  const a = validIsoDate(from)
  const b = validIsoDate(to)
  if (!a || !b) return 999
  const ms = Date.parse(b) - Date.parse(a)
  if (Number.isNaN(ms)) return 999
  return Math.trunc(ms / (1000 * 60 * 60 * 24))
}

/**
 * Filter out events that have ended based on their date window.
 * Events without valid dates keep their static `status` (paranoia: feed may
 * not have date strings; honor the field's own status rather than hiding it).
 * ACTIVE = CURRENT or UPCOMING. ENDED events are excluded.
 *
 * Pure — no Android or DOM dependencies.
 *
 * @param events full feed list
 * @param clock injectable clock for testing; defaults to systemClock
 */
export function activeEvents(events: EventFeedEntry[], clock: Clock = systemClock): EventFeedEntry[] {
  const today = clock.todayIso()
  return events
    .filter(e => effectiveStatus(e, today) !== 'ENDED')
    .sort((a, b) => {
      const sa = effectiveStatus(a, today) === 'CURRENT' ? 0 : 1
      const sb = effectiveStatus(b, today) === 'CURRENT' ? 0 : 1
      if (sa !== sb) return sa - sb
      const da = a.startDate ?? '9999-12-31'
      const db = b.startDate ?? '9999-12-31'
      return da.localeCompare(db)
    })
}

/**
 * Group active events into the 6 sections the Event Guide UI uses.
 * Pure port of Android `groupEvents(events, todayIso)` from EventContext.kt:246.
 *
 * Sections: featured (single), happeningNow, importantUpcoming, rotations, news,
 * allActive.
 */
export interface EventSections {
  featured: EventFeedEntry | null
  importantUpcoming: EventFeedEntry[]
  happeningNow: EventFeedEntry[]
  rotations: EventFeedEntry[]
  news: EventFeedEntry[]
  allActive: EventFeedEntry[]
}

const MAJOR_LIMITED = new Set(['MAJOR_GAMEPLAY', 'LIMITED_GAMEPLAY'])
const ROTATION_CATEGORIES = new Set(['SEASON_GBL', 'ROUTINE_ROTATION', 'RAID_ROTATION'])
const NEWS_CATEGORIES = new Set(['NEWS_PROMO', 'REWARD_DROP', 'ANNOUNCEMENT'])

function determineCategory(entry: EventFeedEntry): string {
  const cat = (entry.eventCategory ?? '').toUpperCase().trim()
  if (cat) return cat
  const title = (entry.title ?? '').toLowerCase()
  const kind = (entry.themeKey ?? '').toLowerCase()
  if (title.includes('gbl') || title.includes('go battle league') || title.includes('season:') ||
      title.includes('forever forward') || title.includes('season of') ||
      title.includes('league') || title.includes('cup')) return 'SEASON_GBL'
  if (title.includes('spotlight hour') || title.includes('raid hour') ||
      title.includes('max monday') || kind === 'spotlight_hour') return 'ROUTINE_ROTATION'
  if (!title.includes('raid day') && (title.includes('5-star') || title.includes('mega raid') ||
      title.includes('shadow raid') || title.includes('raid rotation'))) return 'RAID_ROTATION'
  if (title.includes('go fest') || title.includes('go tour') || title.includes('safari zone') ||
      title.includes('community day') || title.includes('road of legends') ||
      title.includes('global') || kind === 'community_day') return 'MAJOR_GAMEPLAY'
  if (title.includes('twitch drops') || title.includes('prime gaming') ||
      title.includes('reward') || title.includes('drop')) return 'REWARD_DROP'
  if (title.includes('save the date') || title.includes('wallpapers') || title.includes('diary') ||
      title.includes('promo') || title.includes('store') || title.includes('coupon') ||
      title.includes('code')) return 'NEWS_PROMO'
  if (title.includes('announcement') || title.includes('partnership') ||
      title.includes('showcase') || title.includes('professor willow') || title.includes('scopely')) return 'ANNOUNCEMENT'
  return 'LIMITED_GAMEPLAY'
}

export { determineCategory }

/**
 * Hero score — lower is higher priority for featured selection.
 * Pure port of Android `EventContext.heroScore(todayIso)`.
 */
export function heroScore(entry: EventFeedEntry, todayIso: string): number {
  const status = effectiveStatus(entry, todayIso)
  if (status === 'ENDED') return 9999
  const cat = determineCategory(entry)
  const startDiff = daysBetween(todayIso, entry.startDate)
  const isNear = startDiff >= 0 && startDiff <= 21
  if (cat === 'MAJOR_GAMEPLAY') {
    if (status === 'CURRENT') return 10
    if (isNear) return 30
    return 50
  }
  if (cat === 'LIMITED_GAMEPLAY') {
    if (status === 'CURRENT') return 20
    if (isNear) return 40
    return 55
  }
  if (ROTATION_CATEGORIES.has(cat)) {
    if (status === 'CURRENT') return 60
    return 70
  }
  if (NEWS_CATEGORIES.has(cat)) return 100
  return 80
}

/**
 * Selects the single main event to feature prominently on the Event Guide.
 * Pure port of Android `selectMainEvent(events, todayIsoDate)`.
 */
export function selectMainEvent(events: EventFeedEntry[], todayIso: string): EventFeedEntry | null {
  if (events.length === 0) return null
  const candidates = events.filter(e => effectiveStatus(e, todayIso) !== 'ENDED')
  if (candidates.length === 0) return null
  return [...candidates].sort((a, b) => {
    const ha = heroScore(a, todayIso)
    const hb = heroScore(b, todayIso)
    if (ha !== hb) return ha - hb
    const sa = effectiveStatus(a, todayIso) === 'CURRENT' ? (a.endDate ?? '9999-12-31') : (a.startDate ?? '9999-12-31')
    const sb = effectiveStatus(b, todayIso) === 'CURRENT' ? (b.endDate ?? '9999-12-31') : (b.startDate ?? '9999-12-31')
    return sa.localeCompare(sb)
  })[0] ?? null
}

export function groupEvents(events: EventFeedEntry[], clock: Clock = systemClock): EventSections {
  const today = clock.todayIso()
  const active = events
    .filter(e => effectiveStatus(e, today) !== 'ENDED')
    // deduplicate by canonical event key (Android: canonicalEventKey)
    .filter((e, idx, arr) => arr.findIndex(x => canonicalEventKey(x.id) === canonicalEventKey(e.id)) === idx)
  const featured = selectMainEvent(active, today)
  const rest = active.filter(e => e.id !== featured?.id)

  const happeningNow = rest
    .filter(e => effectiveStatus(e, today) === 'CURRENT' && MAJOR_LIMITED.has(determineCategory(e)))
    .sort((a, b) => (a.endDate ?? '9999-12-31').localeCompare(b.endDate ?? '9999-12-31'))

  const importantUpcoming = rest
    .filter(e => effectiveStatus(e, today) === 'UPCOMING' &&
      MAJOR_LIMITED.has(determineCategory(e)) &&
      daysBetween(today, e.startDate) >= 0 && daysBetween(today, e.startDate) <= 21)
    .sort((a, b) => {
      const ma = determineCategory(a) === 'MAJOR_GAMEPLAY' ? 0 : 1
      const mb = determineCategory(b) === 'MAJOR_GAMEPLAY' ? 0 : 1
      if (ma !== mb) return ma - mb
      return (a.startDate ?? '9999-12-31').localeCompare(b.startDate ?? '9999-12-31')
    })

  const rotations = rest
    .filter(e => ROTATION_CATEGORIES.has(determineCategory(e)))
    .sort((a, b) => {
      const sa = effectiveStatus(a, today) === 'CURRENT' ? 0 : 1
      const sb = effectiveStatus(b, today) === 'CURRENT' ? 0 : 1
      if (sa !== sb) return sa - sb
      return (a.startDate ?? '9999-12-31').localeCompare(b.startDate ?? '9999-12-31')
    })

  const news = rest
    .filter(e => NEWS_CATEGORIES.has(determineCategory(e)))
    .sort((a, b) => (a.startDate ?? '9999-12-31').localeCompare(b.startDate ?? '9999-12-31'))

  const allActive = [...active].sort((a, b) => {
    const sa = effectiveStatus(a, today) === 'CURRENT' ? 0 : 1
    const sb = effectiveStatus(b, today) === 'CURRENT' ? 0 : 1
    if (sa !== sb) return sa - sb
    return (a.startDate ?? '9999-12-31').localeCompare(b.startDate ?? '9999-12-31')
  })

  return { featured, importantUpcoming, happeningNow, rotations, news, allActive }
}

/**
 * Mirrors Android `canonicalEventKey` — collapses known duplicate IDs to a canonical key.
 */
export function canonicalEventKey(id: string): string {
  if (id === 'event-go-fest-2026-global-final-details') return 'event-pokemon-go-fest-2026-global'
  return id
}

/**
 * Compute a localized date range label for an event using Intl.DateTimeFormat.
 * Ports Android `EventContext.dateLabel(lang)`.
 *
 * Supported locale codes: en, tr, de, es, fr, it.
 */
export function dateLabel(entry: EventFeedEntry, locale: string): string | null {
  const start = validIsoDate(entry.startDate)
  const end = validIsoDate(entry.endDate)
  if (!start && !end) {
    return entry.start ?? entry.end ?? null
  }
  const intlLocale = localeToIntl(locale)
  const parsedStart = start ? Date.parse(start) : NaN
  const parsedEnd = end ? Date.parse(end) : NaN
  if (!Number.isNaN(parsedStart) && (Number.isNaN(parsedEnd) || parsedEnd === parsedStart)) {
    return new Intl.DateTimeFormat(intlLocale, { dateStyle: 'long', timeZone: 'UTC' }).format(new Date(parsedStart))
  }
  if (!Number.isNaN(parsedStart) && !Number.isNaN(parsedEnd)) {
    const d1 = new Date(parsedStart)
    const d2 = new Date(parsedEnd)
    const sameYear = d1.getFullYear() === d2.getFullYear()
    const sameMonth = sameYear && d1.getMonth() === d2.getMonth()
    if (sameYear && sameMonth) {
      const month = new Intl.DateTimeFormat(intlLocale, { month: 'long', timeZone: 'UTC' }).format(d1)
      const days = `${d1.getUTCDate()}\u2013${d2.getUTCDate()}`
      const year = d1.getUTCFullYear()
      if (locale === 'en') return `${month} ${days}, ${year}`
      if (locale === 'de') return `${d1.getUTCDate()}.\u2013${d2.getUTCDate()}. ${month} ${year}`
      if (locale === 'es') return `${days} de ${month} de ${year}`
      return `${days} ${month} ${year}`
    }
    const fmtFull = new Intl.DateTimeFormat(intlLocale, { dateStyle: 'long', timeZone: 'UTC' })
    return `${fmtFull.format(d1)} \u2013 ${fmtFull.format(d2)}`
  }
  if (!Number.isNaN(parsedStart)) {
    return new Intl.DateTimeFormat(intlLocale, { dateStyle: 'long', timeZone: 'UTC' }).format(new Date(parsedStart))
  }
  if (!Number.isNaN(parsedEnd)) {
    return new Intl.DateTimeFormat(intlLocale, { dateStyle: 'long', timeZone: 'UTC' }).format(new Date(parsedEnd))
  }
  return null
}

function localeToIntl(locale: string): string {
  switch (locale) {
    case 'en': return 'en-US'
    case 'tr': return 'tr-TR'
    case 'de': return 'de-DE'
    case 'es': return 'es-ES'
    case 'fr': return 'fr-FR'
    case 'it': return 'it-IT'
    default: return 'en-US'
  }
}

/**
 * Remaining-time label — pure port of Android `EventContext.remainingTimeLabel`.
 * Returns a localized, compact "X days Y hours left" / "Live now" / "Ended"
 * / "Starts tomorrow" style string.
 *
 * @param entry event feed entry
 * @param clock injectable clock for testing
 * @param lang locale code (en, tr, de, es, fr, it)
 */
export function remainingTimeLabel(
  entry: EventFeedEntry,
  clock: Clock,
  lang: string
): string {
  const today = clock.todayIso()
  const now = clock.nowMillis()
  const status = effectiveStatus(entry, today)
  if (status === 'ENDED') return labelEnded(lang)
  if (status === 'UPCOMING') {
    const diff = daysBetween(today, entry.startDate)
    if (diff === 0) return startsToday(lang)
    if (diff === 1) return startsTomorrow(lang)
    const startMs = entry.startDate ? Date.parse(entry.startDate) : NaN
    if (!Number.isNaN(startMs) && startMs > now) {
      return forwardLabel(startMs - now, lang)
    }
    return comingUp(lang)
  }
  // CURRENT
  const endMs = entry.endDate ? Date.parse(entry.endDate) : NaN
  if (!Number.isNaN(endMs)) {
    // End date is inclusive — event ends at end of that day (23:59:59)
    const endOfDayMs = endMs + 24 * 60 * 60 * 1000 - 1
    if (endOfDayMs > now) {
      return prefixLabel(endOfDayMs - now, lang)
    }
    return liveNow(lang)
  }
  return liveNow(lang)
}

function startsToday(lang: string): string {
  return ({ tr: 'Bugün başlıyor', de: 'Beginnt heute', es: 'Empieza hoy', fr: "Commence aujourd'hui", it: 'Inizia oggi' } as Record<string, string>)[lang] ?? 'Starts today'
}
function startsTomorrow(lang: string): string {
  return ({ tr: 'Yarın başlıyor', de: 'Beginnt morgen', es: 'Empieza mañana', fr: 'Commence demain', it: 'Inizia domani' } as Record<string, string>)[lang] ?? 'Starts tomorrow'
}
function labelEnded(lang: string): string {
  return ({ tr: 'Sona erdi', de: 'Beendet', es: 'Finalizado', fr: 'Terminé', it: 'Terminato' } as Record<string, string>)[lang] ?? 'Ended'
}
function comingUp(lang: string): string {
  return ({ tr: 'Yakında', de: 'Demnächst', es: 'Próximamente', fr: 'Bientôt', it: 'In arrivo' } as Record<string, string>)[lang] ?? 'Coming up'
}
function liveNow(lang: string): string {
  return ({ tr: 'Şu an canlı', de: 'Jetzt live', es: 'En vivo', fr: 'En cours', it: 'In corso' } as Record<string, string>)[lang] ?? 'Live now'
}

function prefixLabel(diffMs: number, lang: string): string {
  const totalHours = Math.trunc(diffMs / (1000 * 60 * 60))
  const days = Math.trunc(totalHours / 24)
  const hours = totalHours % 24
  if (lang === 'tr') {
    if (days > 0 && hours > 0) return `${days} gün ${hours} saat kaldı`
    if (days > 0) return `${days} gün kaldı`
    return 'Bugün bitiyor'
  }
  if (lang === 'de') {
    if (days > 0 && hours > 0) return `Noch ${days} ${days === 1 ? 'Tag' : 'Tage'} und ${hours} ${hours === 1 ? 'Stunde' : 'Stunden'}`
    if (days > 0) return `Noch ${days} ${days === 1 ? 'Tag' : 'Tage'}`
    return 'Endet heute'
  }
  if (lang === 'es') {
    if (days > 0 && hours > 0) return `Quedan ${days} ${days === 1 ? 'día' : 'días'} y ${hours} ${hours === 1 ? 'hora' : 'horas'}`
    if (days > 0) return `Quedan ${days} ${days === 1 ? 'día' : 'días'}`
    return 'Termina hoy'
  }
  if (lang === 'fr') {
    if (days > 0 && hours > 0) return `Plus que ${days} ${days === 1 ? 'jour' : 'jours'} et ${hours} ${hours === 1 ? 'heure' : 'heures'}`
    if (days > 0) return `Plus que ${days} ${days === 1 ? 'jour' : 'jours'}`
    return "Se termine aujourd'hui"
  }
  if (lang === 'it') {
    if (days > 0 && hours > 0) return `Mancano ${days} ${days === 1 ? 'giorno' : 'giorni'} e ${hours} ${hours === 1 ? 'ora' : 'ore'}`
    if (days > 0) return `Mancano ${days} ${days === 1 ? 'giorno' : 'giorni'}`
    return 'Termina oggi'
  }
  if (days > 0 && hours > 0) return `${days} ${days === 1 ? 'day' : 'days'} ${hours} ${hours === 1 ? 'hour' : 'hours'} left`
  if (days > 0) return `${days} ${days === 1 ? 'day' : 'days'} left`
  return 'Ends today'
}

function forwardLabel(diffMs: number, lang: string): string {
  const totalHours = Math.trunc(diffMs / (1000 * 60 * 60))
  const days = Math.trunc(totalHours / 24)
  const hours = totalHours % 24
  if (lang === 'tr') {
    if (days === 1) return 'Yarın başlıyor'
    if (days > 1 && hours > 0) return `${days} gün ${hours} saat sonra`
    if (days > 1) return `${days} gün sonra`
    if (hours > 0) return `${hours} saat sonra`
    return 'Bugün başlıyor'
  }
  if (lang === 'de') {
    if (days === 1) return 'Beginnt morgen'
    if (days > 1 && hours > 0) return `in ${days} Tagen und ${hours} ${hours === 1 ? 'Stunde' : 'Stunden'}`
    if (days > 1) return `in ${days} Tagen`
    if (hours > 0) return `in ${hours} ${hours === 1 ? 'Stunde' : 'Stunden'}`
    return 'Beginnt heute'
  }
  if (lang === 'es') {
    if (days === 1) return 'Empieza mañana'
    if (days > 1 && hours > 0) return `en ${days} días y ${hours} ${hours === 1 ? 'hora' : 'horas'}`
    if (days > 1) return `en ${days} días`
    if (hours > 0) return `en ${hours} ${hours === 1 ? 'hora' : 'horas'}`
    return 'Empieza hoy'
  }
  if (lang === 'fr') {
    if (days === 1) return 'Commence demain'
    if (days > 1 && hours > 0) return `dans ${days} jours et ${hours} ${hours === 1 ? 'heure' : 'heures'}`
    if (days > 1) return `dans ${days} jours`
    if (hours > 0) return `dans ${hours} ${hours === 1 ? 'heure' : 'heures'}`
    return "Commence aujourd'hui"
  }
  if (lang === 'it') {
    if (days === 1) return 'Inizia domani'
    if (days > 1 && hours > 0) return `tra ${days} giorni e ${hours} ${hours === 1 ? 'ora' : 'ore'}`
    if (days > 1) return `tra ${days} giorni`
    if (hours > 0) return `tra ${hours} ${hours === 1 ? 'ora' : 'ore'}`
    return 'Inizia oggi'
  }
  if (days === 1) return 'Starts tomorrow'
  if (days > 1 && hours > 0) return `in ${days} days and ${hours} ${hours === 1 ? 'hour' : 'hours'}`
  if (days > 1) return `in ${days} days`
  if (hours > 0) return `in ${hours} ${hours === 1 ? 'hour' : 'hours'}`
  return 'Starts today'
}
