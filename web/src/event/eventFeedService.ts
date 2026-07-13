// Event feed service — NetworkFirst with bundled fallback.
// Production URL matches the Android app's remote feed.
// Fallback is byte-identical to the Android bundled fixture.

import type { EventFeed, EventFeedEntry, LocaleCode } from '../types'

const PRODUCTION_FEED_URL = 'https://raw.githubusercontent.com/chaglaruk/PokeQuery/master/docs/event-feed/pokequery-events.json'
const FALLBACK_PATH = `${import.meta.env.BASE_URL}event-feed-fallback.json`
const CACHE_KEY = 'pq_event_feed_cache'
const CACHE_TIMESTAMP_KEY = 'pq_event_feed_cache_ts'
const FETCH_TIMEOUT_MS = 5000

export type FeedSource = 'online' | 'cached' | 'fallback'

export interface FeedResult {
  feed: EventFeed
  source: FeedSource
  lastChecked: string | null
}

export async function fetchEventFeed(): Promise<FeedResult> {
  try {
    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), FETCH_TIMEOUT_MS)

    const res = await fetch(PRODUCTION_FEED_URL, { signal: controller.signal })
    clearTimeout(timeout)

    if (!res.ok) throw new Error(`Feed HTTP ${res.status}`)

    const feed = await res.json() as EventFeed
    cacheFeed(feed)
    return { feed, source: 'online', lastChecked: new Date().toISOString() }
  } catch {
    const cached = getCachedFeed()
    if (cached) {
      return { feed: cached, source: 'cached', lastChecked: getCachedTimestamp() }
    }
    return await loadFallback()
  }
}

async function loadFallback(): Promise<FeedResult> {
  const res = await fetch(FALLBACK_PATH)
  const feed = await res.json() as EventFeed
  return { feed, source: 'fallback', lastChecked: null }
}

function cacheFeed(feed: EventFeed): void {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify(feed))
    localStorage.setItem(CACHE_TIMESTAMP_KEY, new Date().toISOString())
  } catch { /* quota or private mode */ }
}

function getCachedFeed(): EventFeed | null {
  try {
    const raw = localStorage.getItem(CACHE_KEY)
    if (!raw) return null
    return JSON.parse(raw) as EventFeed
  } catch {
    return null
  }
}

function getCachedTimestamp(): string | null {
  return localStorage.getItem(CACHE_TIMESTAMP_KEY)
}

const localeSuffixMap: Record<LocaleCode, 'Tr' | 'De' | 'Es' | 'Fr' | 'It' | null> = {
  en: null,
  tr: 'Tr',
  de: 'De',
  es: 'Es',
  fr: 'Fr',
  it: 'It',
}

export interface LocalizedEvent {
  title: string
  note: string
  summary: string
  prep: string
  eventNotes: string
  bonuses?: string | null
  raids?: string | null
  research?: string | null
}

export function getLocalized(entry: EventFeedEntry, locale: LocaleCode): LocalizedEvent {
  const suffix = localeSuffixMap[locale]
  if (!suffix) {
    return {
      title: entry.title,
      note: entry.note,
      summary: entry.summary,
      prep: entry.prep,
      eventNotes: entry.eventNotes,
      bonuses: entry.bonuses,
      raids: entry.raids,
      research: entry.research,
    }
  }

  const pick = (en: string, localized: string | null | undefined) =>
    (localized && localized.length > 0) ? localized : en
  const pickNullable = (en: string | null | undefined, localized: string | null | undefined) => {
    if (localized && localized.length > 0) return localized
    return en ?? null
  }

  return {
    title: pick(entry.title, entry[`title${suffix}` as keyof EventFeedEntry] as string | null),
    note: pick(entry.note, entry[`note${suffix}` as keyof EventFeedEntry] as string | null),
    summary: pick(entry.summary, entry[`summary${suffix}` as keyof EventFeedEntry] as string | null),
    prep: pick(entry.prep, entry[`prep${suffix}` as keyof EventFeedEntry] as string | null),
    eventNotes: pick(entry.eventNotes, entry[`eventNotes${suffix}` as keyof EventFeedEntry] as string | null),
    bonuses: pickNullable(entry.bonuses, entry[`bonuses${suffix}` as keyof EventFeedEntry] as string | null),
    raids: pickNullable(entry.raids, entry[`raids${suffix}` as keyof EventFeedEntry] as string | null),
    research: pickNullable(entry.research, entry[`research${suffix}` as keyof EventFeedEntry] as string | null),
  }
}

export function sortByImportanceThenDate(events: EventFeedEntry[]): EventFeedEntry[] {
  const tierOrder: Record<string, number> = { MAJOR: 0, STANDARD: 1, ROUTINE: 2, NEWS: 3 }
  const statusOrder: Record<string, number> = { CURRENT: 0, UPCOMING: 1, ENDED: 2 }

  return [...events].sort((a, b) => {
    const sa = statusOrder[a.status] ?? 3
    const sb = statusOrder[b.status] ?? 3
    if (sa !== sb) return sa - sb

    const ta = tierOrder[a.importanceTier ?? 'ROUTINE'] ?? 3
    const tb = tierOrder[b.importanceTier ?? 'ROUTINE'] ?? 3
    if (ta !== tb) return ta - tb

    const da = a.startDate ?? a.start ?? ''
    const db = b.startDate ?? b.start ?? ''
    return db.localeCompare(da)
  })
}
