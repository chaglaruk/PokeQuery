import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import {
  fetchEventFeed,
  sortByImportanceThenDate,
  getLocalized,
} from '../event/eventFeedService'
import type { EventFeedEntry, EventFeed } from '../types'

// jsdom in vitest may not provide a full localStorage implementation.
// Install a Map-backed polyfill so tests can rely on getItem/setItem/clear.
const lsStore = new Map<string, string>()
const lsPolyfill: Storage = {
  getItem: (key: string) => lsStore.get(key) ?? null,
  setItem: (key: string, value: string) => { lsStore.set(key, String(value)) },
  removeItem: (key: string) => { lsStore.delete(key) },
  clear: () => lsStore.clear(),
  key: (index: number) => Array.from(lsStore.keys())[index] ?? null,
  get length() { return lsStore.size },
}
vi.stubGlobal('localStorage', lsPolyfill)

function makeEntry(overrides: Partial<EventFeedEntry> = {}): EventFeedEntry {
  return {
    id: 'test-event',
    title: 'Test Event',
    status: 'CURRENT',
    note: 'Note',
    summary: 'Summary',
    prep: 'Prep',
    suggestedSearch: 'shiny',
    eventNotes: 'Event notes',
    themeKey: 'test',
    sourceName: 'official',
    sourceUrl: 'https://example.com',
    sourceType: 'official',
    lastUpdated: '2026-01-01',
    ...overrides,
  }
}

describe('sortByImportanceThenDate', () => {
  it('sorts CURRENT before UPCOMING before ENDED', () => {
    const events = [
      makeEntry({ id: '3', status: 'ENDED', importanceTier: 'ROUTINE' }),
      makeEntry({ id: '1', status: 'UPCOMING', importanceTier: 'ROUTINE' }),
      makeEntry({ id: '2', status: 'CURRENT', importanceTier: 'ROUTINE' }),
    ]
    const sorted = sortByImportanceThenDate(events)
    expect(sorted[0].id).toBe('2')
    expect(sorted[1].id).toBe('1')
    expect(sorted[2].id).toBe('3')
  })

  it('within same status, MAJOR before STANDARD before ROUTINE before NEWS', () => {
    const events = [
      makeEntry({ id: 'news', status: 'CURRENT', importanceTier: 'NEWS' }),
      makeEntry({ id: 'routine', status: 'CURRENT', importanceTier: 'ROUTINE' }),
      makeEntry({ id: 'major', status: 'CURRENT', importanceTier: 'MAJOR' }),
      makeEntry({ id: 'standard', status: 'CURRENT', importanceTier: 'STANDARD' }),
    ]
    const sorted = sortByImportanceThenDate(events)
    expect(sorted.map(e => e.id)).toEqual(['major', 'standard', 'routine', 'news'])
  })

  it('within same status and tier, sorts most recent date first', () => {
    const events = [
      makeEntry({ id: 'a', status: 'CURRENT', importanceTier: 'STANDARD', startDate: '2026-01-01' }),
      makeEntry({ id: 'b', status: 'CURRENT', importanceTier: 'STANDARD', startDate: '2026-06-15' }),
      makeEntry({ id: 'c', status: 'CURRENT', importanceTier: 'STANDARD', startDate: '2026-03-10' }),
    ]
    const sorted = sortByImportanceThenDate(events)
    expect(sorted.map(e => e.id)).toEqual(['b', 'c', 'a'])
  })

  it('handles null importanceTier as ROUTINE', () => {
    const events = [
      makeEntry({ id: 'null', status: 'CURRENT', importanceTier: null }),
      makeEntry({ id: 'major', status: 'CURRENT', importanceTier: 'MAJOR' }),
    ]
    const sorted = sortByImportanceThenDate(events)
    expect(sorted[0].id).toBe('major')
    expect(sorted[1].id).toBe('null')
  })

  it('does not mutate the input array', () => {
    const original = [
      makeEntry({ id: 'b', status: 'ENDED', importanceTier: 'ROUTINE' }),
      makeEntry({ id: 'a', status: 'CURRENT', importanceTier: 'ROUTINE' }),
    ]
    const originalCopy = [...original]
    sortByImportanceThenDate(original)
    expect(original.map(e => e.id)).toEqual(originalCopy.map(e => e.id))
  })
})

describe('getLocalized', () => {
  const entry = makeEntry({
    title: 'English Title',
    note: 'English Note',
    summary: 'English Summary',
    prep: 'English Prep',
    eventNotes: 'English Notes',
    titleTr: 'Turkish Title',
    noteTr: 'Turkish Note',
    summaryTr: '',
    titleDe: 'German Title',
  })

  it('returns English fields for locale en', () => {
    const result = getLocalized(entry, 'en')
    expect(result.title).toBe('English Title')
    expect(result.summary).toBe('English Summary')
    expect(result.eventNotes).toBe('English Notes')
  })

  it('returns Turkish fields when localized field is non-empty', () => {
    const result = getLocalized(entry, 'tr')
    expect(result.title).toBe('Turkish Title')
    expect(result.note).toBe('Turkish Note')
  })

  it('falls back to English when localized field is empty', () => {
    const result = getLocalized(entry, 'tr')
    expect(result.summary).toBe('English Summary')
  })

  it('falls back to English when localized field is null/undefined', () => {
    const result = getLocalized(entry, 'de')
    expect(result.title).toBe('German Title')
    expect(result.note).toBe('English Note')
    expect(result.summary).toBe('English Summary')
  })
})

describe('fetchEventFeed', () => {
  const mockFeed: EventFeed = {
    schemaVersion: 1,
    lastUpdated: '2026-07-10',
    events: [makeEntry({ id: 'mock-1', title: 'Mock Event' })],
  }

  const mockCacheKey = 'pq_event_feed_cache'
  const mockCacheTsKey = 'pq_event_feed_cache_ts'

  beforeEach(() => {
    vi.spyOn(globalThis, 'fetch').mockReset()
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('fetches from production URL and returns source online on success', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify(mockFeed), { status: 200 })
    )

    const result = await fetchEventFeed()

    expect(result.source).toBe('online')
    expect(result.feed.events.length).toBe(1)
    expect(result.feed.events[0]?.id).toBe('mock-1')
    expect(result.lastChecked).not.toBeNull()

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const callUrl = fetchMock.mock.calls[0]?.[0]
    expect(String(callUrl)).toContain('raw.githubusercontent.com')
  })

  it('writes to localStorage cache on successful fetch', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify(mockFeed), { status: 200 })
    )

    await fetchEventFeed()

    const cached = localStorage.getItem(mockCacheKey)
    expect(cached).not.toBeNull()
    const parsed = JSON.parse(cached!)
    expect(parsed.events[0].id).toBe('mock-1')
    expect(localStorage.getItem(mockCacheTsKey)).not.toBeNull()
  })

  it('returns cached feed when network fetch rejects', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValueOnce(new Error('Network error'))
    localStorage.setItem(mockCacheKey, JSON.stringify({ ...mockFeed, events: [{ ...makeEntry({ id: 'cached-1' }) }] }))
    localStorage.setItem(mockCacheTsKey, '2026-07-05T10:00:00.000Z')

    const result = await fetchEventFeed()

    expect(result.source).toBe('cached')
    expect(result.feed.events[0]?.id).toBe('cached-1')
    expect(result.lastChecked).toBe('2026-07-05T10:00:00.000Z')
  })

  it('returns cached feed when fetch returns non-ok status', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response('Server error', { status: 503 })
    )
    localStorage.setItem(mockCacheKey, JSON.stringify({ ...mockFeed, events: [{ ...makeEntry({ id: 'cached-2' }) }] }))

    const result = await fetchEventFeed()

    expect(result.source).toBe('cached')
    expect(result.feed.events[0]?.id).toBe('cached-2')
  })

  it('falls back to bundled fallback when network rejects and no cache', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockRejectedValueOnce(new Error('Network error'))
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ ...mockFeed, events: [{ ...makeEntry({ id: 'fallback-1' }) }] }), { status: 200 })
      )

    const result = await fetchEventFeed()

    expect(result.source).toBe('fallback')
    expect(result.feed.events[0]?.id).toBe('fallback-1')
    expect(result.lastChecked).toBeNull()
    expect(vi.mocked(globalThis.fetch).mock.calls.length).toBe(2)
    const fallbackUrl = String(vi.mocked(globalThis.fetch).mock.calls[1]?.[0])
    expect(fallbackUrl).toContain('event-feed-fallback.json')
  })

  it('falls back to bundled fallback when fetch rejects and cache throws', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockRejectedValueOnce(new Error('Network error'))
      .mockResolvedValueOnce(
        new Response(JSON.stringify(mockFeed), { status: 200 })
      )

    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => null)

    const result = await fetchEventFeed()

    expect(result.source).toBe('fallback')
    vi.mocked(Storage.prototype.getItem).mockRestore()
  })

  it('handles AbortError (timeout scenario) by falling through to cache/fallback', async () => {
    // Simulate what happens when the 5s AbortController abort fires:
    // fetch rejects with an AbortError, which triggers the cache/fallback path.
    const abortError = new DOMException('The operation was aborted', 'AbortError')
    vi.spyOn(globalThis, 'fetch')
      .mockRejectedValueOnce(abortError)
      .mockResolvedValueOnce(
        new Response(JSON.stringify(mockFeed), { status: 200 })
      )

    const result = await fetchEventFeed()

    expect(result.source).toBe('fallback')
    expect(result.feed.events[0]?.id).toBe('mock-1')
  })
})
