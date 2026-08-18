import { afterEach, describe, expect, it } from 'vitest'
import { remainingTimeLabel, type Clock } from '../event/eventLifecycle'
import type { EventFeedEntry } from '../types'

const previousTz = process.env.TZ

afterEach(() => {
  process.env.TZ = previousTz
})

describe('Event Guide DST end-of-day regression', () => {
  it('keeps a current event on Ends today after the fall-back transition', () => {
    process.env.TZ = 'America/New_York'
    const now = new Date(2026, 10, 1, 23, 30, 0).getTime()
    const clock: Clock = {
      todayIso: () => '2026-11-01',
      nowMillis: () => now,
    }
    const entry = {
      id: 'dst-event',
      title: 'DST Event',
      status: 'CURRENT',
      note: 'Note',
      summary: 'Summary',
      prep: 'Prep',
      suggestedSearch: 'shiny',
      eventNotes: 'Notes',
      themeKey: 'generic_event',
      sourceName: 'Test',
      sourceUrl: 'https://example.invalid/test',
      sourceType: 'official',
      lastUpdated: '2026-11-01',
      startDate: '2026-11-01',
      endDate: '2026-11-01',
    } as EventFeedEntry

    expect(new Date(now).getTimezoneOffset()).toBe(300)
    expect(remainingTimeLabel(entry, clock, 'en')).toBe('Ends today')
  })
})
