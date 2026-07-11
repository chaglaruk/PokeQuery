// React hook for loading and caching the event feed.

import { useState, useEffect, useCallback } from 'react'
import { fetchEventFeed, sortByImportanceThenDate, getLocalized, type FeedSource } from './eventFeedService'
import type { EventFeed, EventFeedEntry, LocaleCode } from '../types'

export interface UseEventFeedResult {
  events: EventFeedEntry[]
  source: FeedSource | null
  loading: boolean
  error: string | null
  lastChecked: string | null
  refresh: () => void
}

export function useEventFeed(_locale: LocaleCode): UseEventFeedResult {
  const [feed, setFeed] = useState<EventFeed | null>(null)
  const [source, setSource] = useState<FeedSource | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [lastChecked, setLastChecked] = useState<string | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)

  const refresh = useCallback(() => setRefreshKey(k => k + 1), [])

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const result = await fetchEventFeed()
        if (cancelled) return
        setFeed(result.feed)
        setSource(result.source)
        setLastChecked(result.lastChecked)
      } catch (e) {
        if (cancelled) return
        setError(e instanceof Error ? e.message : 'Failed to load events')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => { cancelled = true }
  }, [refreshKey])

  const events = feed ? sortByImportanceThenDate(feed.events) : []

  return { events, source, loading, error, lastChecked, refresh }
}

export function useLocalizedEvent(entry: EventFeedEntry | null, locale: LocaleCode) {
  const [localized, setLocalized] = useState(() =>
    entry ? getLocalized(entry, locale) : null
  )

  useEffect(() => {
    if (entry) {
      setLocalized(getLocalized(entry, locale))
    } else {
      setLocalized(null)
    }
  }, [entry, locale])

  return localized
}
