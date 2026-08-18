import { afterEach, describe, expect, it, vi } from 'vitest'
import { parseSearchIntent } from '../engine/searchIntentParser'
import { parseCaughtDateIntent } from '../engine/caughtDateIntent'
import { dateLabel, determineCategory } from '../event/eventLifecycle'
import { fetchEventFeed } from '../event/eventFeedService'
import { changelogEntries } from '../changelog'
import type { EventFeedEntry } from '../types'

function event(overrides: Partial<EventFeedEntry> = {}): EventFeedEntry {
  return {
    id: 'event-test',
    title: 'Test Event',
    status: 'UPCOMING',
    note: 'Test note',
    summary: 'Test summary',
    prep: 'Test prep',
    suggestedSearch: 'shiny',
    eventNotes: 'Test notes',
    themeKey: 'generic_event',
    sourceName: 'Test source',
    sourceUrl: 'https://example.invalid/test',
    sourceType: 'official',
    lastUpdated: '2026-08-18',
    ...overrides,
  }
}

afterEach(() => {
  vi.restoreAllMocks()
  vi.unstubAllGlobals()
  localStorage.clear()
})

describe('v0.7.5 post-release Search Assistant regressions', () => {
  it('rejects accidental keyword substrings instead of emitting valid-looking wrong queries', () => {
    for (const input of [
      'traded',
      'check my storage',
      'small pokemon',
      'extraordinary',
      'eventually',
      'undelete',
      'junkyard',
      'footage',
    ]) {
      const result = parseSearchIntent(input)
      expect(result.canBuild, input).toBe(false)
      expect(result.rawQuery, input).toBe('')
    }
  })

  it('keeps punctuation-bearing exact keywords working', () => {
    expect(parseSearchIntent('100%').rawQuery).toBe('4*')
    expect(parseSearchIntent('15/15/15').rawQuery).toBe('4*')
  })

  it('blocks pipe input instead of changing OR into AND', () => {
    const result = parseSearchIntent('shiny|lucky')
    expect(result.canBuild).toBe(false)
    expect(result.rawQuery).toBe('')
    expect(result.pipeForbidden).toBe(true)
    expect(result.noteKeys).toContain('search_intent_pipe_forbidden')
  })

  it('does not interpret modal may as the month May', () => {
    const today = new Date(2026, 7, 18)
    const modal = parseCaughtDateIntent('caught anything I may, or may not, want', today)
    expect(modal?.canBuild).toBe(false)
    expect(modal?.tokens).toEqual([])

    const month = parseCaughtDateIntent('caught in May 2026', today)
    expect(month?.canBuild).toBe(true)
    expect(month?.tokens).toEqual(['year2026', 'age79-109'])
  })

  it('normalizes a smart apostrophe before inverted negation parsing', () => {
    expect(parseSearchIntent('don’t hide shiny').rawQuery).toBe('shiny')
  })

  it('normalizes Turkish non-ASCII prefix negation before JS word-boundary parsing', () => {
    expect(parseSearchIntent('hariç shiny').rawQuery).toBe('!shiny')
  })

  it('stops bare not at an explicit conjunction boundary', () => {
    const result = parseSearchIntent('not shiny and legendary')
    expect(result.tokens).toContain('legendary')
    expect(result.exclusions).toContain('shiny')
    expect(result.exclusions).not.toContain('legendary')
    expect(result.rawQuery).toBe('legendary&!shiny')
  })

  it('does not interpret affirmative tag words as untagged', () => {
    for (const input of ['tagged', 'tag', 'etiket']) {
      expect(parseSearchIntent(input).canBuild, input).toBe(false)
    }
    expect(parseSearchIntent('untagged').rawQuery).toBe('!#')
    expect(parseSearchIntent('etiketsiz').rawQuery).toBe('!#')
  })
})

describe('v0.7.5 post-release web parity regressions', () => {
  it('accepts punctuation after a caught-date month name', () => {
    const result = parseCaughtDateIntent('caught in April, 2025', new Date(2026, 7, 18))
    expect(result?.canBuild).toBe(true)
    expect(result?.tokens[0]).toBe('year2025')
  })

  it('uses the Android category priority for ambiguous fallback titles', () => {
    const ambiguous = event({
      title: 'Community Day Raid Rotation Rewards',
      eventCategory: null,
    })
    expect(determineCategory(ambiguous)).toBe('REWARD_DROP')
  })

  it('uses UTC calendar components when deciding whether a date range crosses months', () => {
    const localYear = vi.spyOn(Date.prototype, 'getFullYear').mockReturnValue(2026)
    const localMonth = vi.spyOn(Date.prototype, 'getMonth').mockReturnValue(6)
    try {
      const label = dateLabel(event({ startDate: '2026-07-31', endDate: '2026-08-01' }), 'en')
      expect(label).toBe('July 31, 2026 – August 1, 2026')
    } finally {
      localYear.mockRestore()
      localMonth.mockRestore()
    }
  })

  it('rejects malformed cached feed data, clears it, and falls back safely', async () => {
    localStorage.setItem('pq_event_feed_cache', JSON.stringify({ schemaVersion: 1, events: 'not-an-array' }))
    localStorage.setItem('pq_event_feed_cache_ts', 'stale')

    const fallbackFeed = {
      schemaVersion: 1,
      lastUpdated: '2026-08-18',
      events: [event()],
    }
    const fetchMock = vi.fn()
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => fallbackFeed,
      })
    vi.stubGlobal('fetch', fetchMock)

    const result = await fetchEventFeed()
    expect(result.source).toBe('fallback')
    expect(result.feed.events).toHaveLength(1)
    expect(localStorage.getItem('pq_event_feed_cache')).toBeNull()
    expect(localStorage.getItem('pq_event_feed_cache_ts')).toBeNull()
  })

  it('rejects online feed entries with an invalid status and falls back safely', async () => {
    const invalidOnline = {
      schemaVersion: 1,
      lastUpdated: '2026-08-18',
      events: [event({ status: 'BROKEN' as EventFeedEntry['status'] })],
    }
    const fallbackFeed = {
      schemaVersion: 1,
      lastUpdated: '2026-08-18',
      events: [event()],
    }
    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => invalidOnline })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => fallbackFeed }))

    const result = await fetchEventFeed()
    expect(result.source).toBe('fallback')
    expect(result.feed.events[0]?.status).toBe('UPCOMING')
  })

  it('rejects an online feed missing lastUpdated and falls back safely', async () => {
    const invalidOnline = {
      schemaVersion: 1,
      events: [event()],
    }
    const fallbackFeed = {
      schemaVersion: 1,
      lastUpdated: '2026-08-18',
      events: [event()],
    }
    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => invalidOnline })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => fallbackFeed }))

    const result = await fetchEventFeed()
    expect(result.source).toBe('fallback')
    expect(result.feed.lastUpdated).toBe('2026-08-18')
  })

  it('marks the shipping web changelog entry as v0.7.5 code 25', () => {
    const current = changelogEntries.find(entry => entry.isCurrent)
    expect(current?.versionName).toBe('0.7.5')
    expect(current?.versionCode).toBe(25)
  })

  it('records v0.7.4 as closed testing rather than a production release', () => {
    const previous = changelogEntries.find(entry => entry.versionName === '0.7.4')
    expect(previous?.releaseLabel).toBe('Closed Testing')
  })
})
