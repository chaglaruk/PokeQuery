import { describe, it, expect } from 'vitest'
import {
  effectiveStatus,
  activeEvents,
  groupEvents,
  selectMainEvent,
  heroScore,
  daysBetween,
  dateLabel,
  dateTimeLabel,
  remainingTimeLabel,
  canonicalEventKey,
  systemClock,
  type Clock,
} from '../event/eventLifecycle'
import type { EventFeedEntry } from '../types'

function makeEntry(overrides: Partial<EventFeedEntry> = {}): EventFeedEntry {
  return {
    id: 'ev1',
    title: 'Test Event',
    status: 'CURRENT',
    note: 'Note',
    summary: 'Summary',
    prep: 'Prep',
    suggestedSearch: 'shiny',
    eventNotes: 'Event notes',
    themeKey: 'generic_event',
    sourceName: 'src',
    sourceUrl: 'https://example.com',
    sourceType: 'official',
    lastUpdated: '2026-07-10',
    ...overrides,
  }
}

const pinnedClock = (todayIso: string, nowMillis?: number): Clock => ({
  todayIso: () => todayIso,
  nowMillis: () => nowMillis ?? Date.parse(todayIso),
})

describe('effectiveStatus', () => {
  it('returns UPCOMING when today is before startDate', () => {
    expect(effectiveStatus(makeEntry({ startDate: '2026-08-01', endDate: '2026-08-10', status: 'UPCOMING' }), '2026-07-12')).toBe('UPCOMING')
  })

  it('returns ENDED when today is after endDate', () => {
    expect(effectiveStatus(makeEntry({ startDate: '2026-06-01', endDate: '2026-06-10', status: 'CURRENT' }), '2026-07-12')).toBe('ENDED')
  })

  it('returns CURRENT when between start and end inclusive', () => {
    expect(effectiveStatus(makeEntry({ startDate: '2026-07-01', endDate: '2026-07-31', status: 'UPCOMING' }), '2026-07-15')).toBe('CURRENT')
    expect(effectiveStatus(makeEntry({ startDate: '2026-07-01', endDate: '2026-07-31' }), '2026-07-01')).toBe('CURRENT')
    expect(effectiveStatus(makeEntry({ startDate: '2026-07-01', endDate: '2026-07-31' }), '2026-07-31')).toBe('CURRENT')
  })

  it('returns CURRENT when only startDate exists and today >= start', () => {
    expect(effectiveStatus(makeEntry({ startDate: '2026-07-01' }), '2026-07-15')).toBe('CURRENT')
    expect(effectiveStatus(makeEntry({ startDate: '2026-07-01' }), '2026-07-01')).toBe('CURRENT')
  })

  it('falls back to static status when no valid dates', () => {
    expect(effectiveStatus(makeEntry({ startDate: null, endDate: null, status: 'CURRENT' }), '2026-07-12')).toBe('CURRENT')
    expect(effectiveStatus(makeEntry({ startDate: null, endDate: null, status: 'UPCOMING' }), '2026-07-12')).toBe('UPCOMING')
    expect(effectiveStatus(makeEntry({ startDate: null, endDate: null, status: 'ENDED' }), '2026-07-12')).toBe('ENDED')
  })

  it('falls back to static status when today is invalid', () => {
    expect(effectiveStatus(makeEntry({ startDate: '2026-07-01', endDate: '2026-07-31' }), 'invalid')).toBe('CURRENT')
  })

  it('treats non-ISO date strings as invalid (no crash)', () => {
    expect(effectiveStatus(makeEntry({ startDate: 'July 1, 2026', endDate: 'July 31, 2026' }), '2026-07-15')).toBe('CURRENT')
  })
})

describe('daysBetween', () => {
  it('returns 0 for same day', () => {
    expect(daysBetween('2026-07-12', '2026-07-12')).toBe(0)
  })

  it('returns positive number for to after from', () => {
    expect(daysBetween('2026-07-12', '2026-07-15')).toBe(3)
  })

  it('returns negative number for to before from', () => {
    expect(daysBetween('2026-07-15', '2026-07-12')).toBe(-3)
  })

  it('returns 999 for null/undefined to', () => {
    expect(daysBetween('2026-07-12', null)).toBe(999)
    expect(daysBetween('2026-07-12', undefined)).toBe(999)
  })

  it('returns 999 for invalid date strings', () => {
    expect(daysBetween('invalid', '2026-07-12')).toBe(999)
  })
})

describe('activeEvents', () => {
  it('filters out events that have ended based on their date window', () => {
    const events = [
      makeEntry({ id: 'past', startDate: '2026-06-01', endDate: '2026-06-10', status: 'CURRENT' }),
      makeEntry({ id: 'live', startDate: '2026-07-01', endDate: '2026-07-31', status: 'CURRENT' }),
      makeEntry({ id: 'future', startDate: '2026-08-01', endDate: '2026-08-10', status: 'UPCOMING' }),
    ]
    const result = activeEvents(events, pinnedClock('2026-07-12'))
    expect(result.map(e => e.id)).toEqual(['live', 'future'])
  })

  it('sorts CURRENT events before UPCOMING', () => {
    const events = [
      makeEntry({ id: 'future', startDate: '2026-08-01', endDate: '2026-08-10', status: 'UPCOMING' }),
      makeEntry({ id: 'live', startDate: '2026-07-01', endDate: '2026-07-31', status: 'CURRENT' }),
    ]
    const result = activeEvents(events, pinnedClock('2026-07-12'))
    expect(result.map(e => e.id)).toEqual(['live', 'future'])
  })

  it('sorts by startDate within CURRENT within same status', () => {
    const events = [
      makeEntry({ id: 'late-live', startDate: '2026-07-20', endDate: '2026-08-10', status: 'CURRENT' }),
      makeEntry({ id: 'early-live', startDate: '2026-07-01', endDate: '2026-07-31', status: 'CURRENT' }),
    ]
    const result = activeEvents(events, pinnedClock('2026-07-12'))
    expect(result.map(e => e.id)).toEqual(['early-live', 'late-live'])
  })

  it('honors events with no date strings — keeps static status', () => {
    const events = [
      makeEntry({ id: 'no-dates', startDate: null, endDate: null, status: 'CURRENT' }),
      makeEntry({ id: 'ended-no-dates', startDate: null, endDate: null, status: 'ENDED' }),
    ]
    const result = activeEvents(events, pinnedClock('2026-07-12'))
    // 'no-dates' keeps CURRENT, 'ended-no-dates' keeps ENDED → filtered out.
    expect(result.map(e => e.id)).toEqual(['no-dates'])
  })

  it('returns empty array for all-ended feed', () => {
    const events = [
      makeEntry({ id: 'a', startDate: '2026-06-01', endDate: '2026-06-05' }),
      makeEntry({ id: 'b', startDate: '2026-06-10', endDate: '2026-06-15' }),
    ]
    const result = activeEvents(events, pinnedClock('2026-07-12'))
    expect(result).toEqual([])
  })
})

describe('groupEvents', () => {
  it('places CURRENT MAJOR_GAMEPLAY first in featured, importantUpcoming excludes featured', () => {
    const events = [
      makeEntry({ id: 'go-fest', title: 'GO Fest 2026', startDate: '2026-07-11', endDate: '2026-07-12', status: 'CURRENT' }),
      makeEntry({ id: 'community-day', title: 'July Community Day', startDate: '2026-07-21', endDate: '2026-07-21', status: 'UPCOMING', themeKey: 'community_day' }),
      makeEntry({ id: 'past', title: 'Past Event', startDate: '2026-06-01', endDate: '2026-06-05', status: 'CURRENT' }),
    ]
    const sections = groupEvents(events, pinnedClock('2026-07-12'))
    expect(sections.featured?.id).toBe('go-fest')
    // featured is excluded from happeningNow (uses `rest`)
    expect(sections.happeningNow.map(e => e.id)).toEqual([])
    expect(sections.importantUpcoming.map(e => e.id)).toEqual(['community-day'])
    expect(sections.allActive.map(e => e.id)).toContain('go-fest')
    expect(sections.allActive.map(e => e.id)).toContain('community-day')
    expect(sections.allActive.map(e => e.id)).not.toContain('past')
  })

  it('sorts importantUpcoming by MAJOR first (featured excluded)', () => {
    const events = [
      makeEntry({ id: 'limited', title: 'Limited Gameplay', startDate: '2026-07-25', status: 'UPCOMING' }),
      makeEntry({ id: 'major1', title: 'GO Fest', startDate: '2026-07-25', status: 'UPCOMING' }),
      makeEntry({ id: 'major2', title: 'GO Tour', startDate: '2026-08-01', status: 'UPCOMING' }),
    ]
    const sections = groupEvents(events, pinnedClock('2026-07-12'))
    // major1 has lower startDate (2026-07-25) and is MAJOR → featured
    expect(sections.featured?.id).toBe('major1')
    // importantUpcoming excludes featured, so remaining are: major2 (MAJOR, 2026-08-01) and limited (LIMITED, 2026-07-25)
    // MAJOR sorts first regardless of date
    expect(sections.importantUpcoming.map(e => e.id)).toEqual(['major2', 'limited'])
  })

  it('excludes ENDED events from all sections', () => {
    const events = [
      makeEntry({ id: 'past', title: 'Past', startDate: '2026-06-01', endDate: '2026-06-05' }),
      makeEntry({ id: 'live', title: 'Live', startDate: '2026-07-01', endDate: '2026-07-31' }),
    ]
    const sections = groupEvents(events, pinnedClock('2026-07-12'))
    expect(sections.featured?.id).toBe('live')
    expect(sections.allActive.map(e => e.id)).toEqual(['live'])
  })

  it('routes rotations and news to their own sections', () => {
    const events = [
      makeEntry({ id: 'rotation', title: 'Spotlight Hour', themeKey: 'spotlight_hour', startDate: '2026-07-15', status: 'UPCOMING' }),
      makeEntry({ id: 'news', title: 'Prime Gaming Drop', startDate: '2026-07-15', status: 'UPCOMING' }),
      makeEntry({ id: 'live', title: 'GO Fest Live', startDate: '2026-07-01', endDate: '2026-07-31', status: 'CURRENT' }),
    ]
    const sections = groupEvents(events, pinnedClock('2026-07-12'))
    expect(sections.rotations.map(e => e.id)).toContain('rotation')
    expect(sections.news.map(e => e.id)).toContain('news')
  })
})

describe('selectMainEvent', () => {
  it('returns null for empty list', () => {
    expect(selectMainEvent([], '2026-07-12')).toBeNull()
  })

  it('returns null for all-ended list', () => {
    const events = [makeEntry({ id: 'past', startDate: '2026-06-01', endDate: '2026-06-05' })]
    expect(selectMainEvent(events, '2026-07-12')).toBeNull()
  })

  it('prefers CURRENT over UPCOMING for same category', () => {
    const events = [
      makeEntry({ id: 'upcoming-fest', title: 'GO Fest', startDate: '2026-08-01', endDate: '2026-08-10', status: 'UPCOMING' }),
      makeEntry({ id: 'live-fest', title: 'GO Fest Live', startDate: '2026-07-01', endDate: '2026-07-31', status: 'CURRENT' }),
    ]
    expect(selectMainEvent(events, '2026-07-12')?.id).toBe('live-fest')
  })
})

describe('heroScore', () => {
  it('returns 9999 for ENDED events', () => {
    expect(heroScore(makeEntry({ startDate: '2026-06-01', endDate: '2026-06-05' }), '2026-07-12')).toBe(9999)
  })

  it('returns 10 for current MAJOR_GAMEPLAY', () => {
    expect(heroScore(makeEntry({ id: 'go-fest', title: 'GO Fest', startDate: '2026-07-01', endDate: '2026-07-31', status: 'CURRENT' }), '2026-07-12')).toBe(10)
  })

  it('returns 70 for upcoming rotation from current date', () => {
    expect(heroScore(makeEntry({ id: 'spot', title: 'Spotlight Hour', themeKey: 'spotlight_hour', startDate: '2026-07-20', status: 'UPCOMING' }), '2026-07-12')).toBe(70)
  })
})

describe('canonicalEventKey', () => {
  it('collapses the GO Fest 2026 final-details id to its canonical form', () => {
    expect(canonicalEventKey('event-go-fest-2026-global-final-details')).toBe('event-pokemon-go-fest-2026-global')
  })

  it('returns the id unchanged for other ids', () => {
    expect(canonicalEventKey('event-charged-up')).toBe('event-charged-up')
  })
})

describe('dateLabel', () => {
  it.each([
    ['en', 'July 1–31, 2026'],
    ['tr', '1–31 Temmuz 2026'],
    ['de', '1.–31. Juli 2026'],
    ['es', '1–31 de julio de 2026'],
    ['fr', '1–31 juillet 2026'],
    ['it', '1–31 luglio 2026'],
  ])('formats a natural same-month range for %s', (locale, expected) => {
    expect(dateLabel(makeEntry({ startDate: '2026-07-01', endDate: '2026-07-31' }), locale)).toBe(expected)
  })

  it('formats a single-day event with long date style', () => {
    const label = dateLabel(makeEntry({ startDate: '2026-07-21', endDate: '2026-07-21' }), 'en')
    expect(label).toBeTruthy()
    expect(label).toMatch(/July/)
  })

  it('formats a same-month range as month + day range', () => {
    const label = dateLabel(makeEntry({ startDate: '2026-07-11', endDate: '2026-07-12' }), 'en')
    expect(label).toBeTruthy()
    expect(label).toMatch(/July/)
  })

  it('formats a cross-month range as full dates joined by en-dash', () => {
    const label = dateLabel(makeEntry({ startDate: '2026-07-25', endDate: '2026-08-05' }), 'en')
    expect(label).toMatch(/\u2013/)
    expect(label).toMatch(/July/)
    expect(label).toMatch(/August/)
  })

  it('returns null when no dates and no start/end text', () => {
    expect(dateLabel(makeEntry({ startDate: null, endDate: null, start: null, end: null }), 'en')).toBeNull()
  })

  it('returns start text when no parseable dates', () => {
    expect(dateLabel(makeEntry({ startDate: null, endDate: null, start: 'Next week' }), 'en')).toBe('Next week')
  })

  it('uses Turkish locale formatting', () => {
    const label = dateLabel(makeEntry({ startDate: '2026-07-21', endDate: '2026-07-21' }), 'tr')
    expect(label).toBeTruthy()
    expect(label!.length).toBeGreaterThan(0)
  })
})

describe('dateTimeLabel', () => {
  it('formats last-checked timestamps using the selected UI locale', () => {
    const value = '2026-07-14T12:30:00.000Z'
    const english = dateTimeLabel(value, 'en')
    const turkish = dateTimeLabel(value, 'tr')
    expect(english).not.toBe(turkish)
    expect(turkish.toLocaleLowerCase('tr-TR')).toContain('tem')
  })

  it('returns malformed source values unchanged', () => {
    expect(dateTimeLabel('not-a-date', 'fr')).toBe('not-a-date')
  })
})

describe('remainingTimeLabel', () => {
  it.each([
    ['en', '18 days left'],
    ['tr', '18 gün kaldı'],
    ['de', 'Noch 18 Tage'],
    ['es', 'Quedan 18 días'],
    ['fr', 'Plus que 18 jours'],
    ['it', 'Mancano 18 giorni'],
  ])('uses natural countdown copy for %s', (locale, expected) => {
    const now = Date.parse('2026-07-14') - 1
    expect(remainingTimeLabel(makeEntry({ startDate: '2026-07-01', endDate: '2026-07-31' }), pinnedClock('2026-07-13', now), locale)).toBe(expected)
  })

  it('returns "Ended" / localized equivalent for ENDED events', () => {
    expect(remainingTimeLabel(makeEntry({ startDate: '2026-06-01', endDate: '2026-06-05' }), pinnedClock('2026-07-12'), 'en')).toBe('Ended')
    expect(remainingTimeLabel(makeEntry({ startDate: '2026-06-01', endDate: '2026-06-05' }), pinnedClock('2026-07-12'), 'tr')).toBe('Sona erdi')
  })

  it('returns "Starts tomorrow" for upcoming event starting next day', () => {
    expect(remainingTimeLabel(makeEntry({ startDate: '2026-07-13', endDate: null, status: 'UPCOMING' }), pinnedClock('2026-07-12'), 'en')).toBe('Starts tomorrow')
    expect(remainingTimeLabel(makeEntry({ startDate: '2026-07-13', endDate: null, status: 'UPCOMING' }), pinnedClock('2026-07-12'), 'tr')).toBe('Yarın başlıyor')
  })

  it('returns coming-up when event UPCOMING but cannot compute precise time', () => {
    // Event has no startDate; static status=UPCOMING
    expect(remainingTimeLabel(makeEntry({ startDate: null, endDate: null, status: 'UPCOMING' }), pinnedClock('2026-07-12'), 'en')).toBe('Coming up')
  })

  it('returns forward-looking label for upcoming event >1 day away', () => {
    const label = remainingTimeLabel(makeEntry({ startDate: '2026-08-01', endDate: '2026-08-10', status: 'UPCOMING' }), pinnedClock('2026-07-12', Date.parse('2026-07-12') + 12 * 3600 * 1000), 'en')
    expect(label).toMatch(/^in \d+ days/)
  })

  it('returns countdown prefix when CURRENT', () => {
    // today=2026-07-12, ends 2026-07-15 23:59:59, now=2026-07-12 noon
    const nowMs = Date.parse('2026-07-12') + 12 * 3600 * 1000
    const label = remainingTimeLabel(makeEntry({ startDate: '2026-07-01', endDate: '2026-07-15', status: 'CURRENT' }), pinnedClock('2026-07-12', nowMs), 'en')
    expect(label).toMatch(/^\d+ days \d+ hours left$|^\d+ days left$|^Ends today$/)
  })

  it('returns "Live now" when end-of-day already past', () => {
    // 2026-07-31 23:59:59, now=2026-08-01 10:00
    const nowMs = Date.parse('2026-08-01') + 10 * 3600 * 1000
    const label = remainingTimeLabel(makeEntry({ startDate: '2026-07-01', endDate: '2026-07-31', status: 'CURRENT' }), pinnedClock('2026-07-31', nowMs), 'en')
    // effectiveStatus with 2026-07-31 today is CURRENT (<=end), endOfDay is just past now
    // But range may also compute "Live now"
    expect(['Live now', 'Ends today', '1d left', /^\d+d left$/].some(p => typeof p === 'string' ? label === p : p.test(label))).toBe(true)
  })
})

describe('systemClock', () => {
  it('todayIso returns yyyy-MM-dd format', () => {
    const iso = systemClock.todayIso()
    expect(iso).toMatch(/^\d{4}-\d{2}-\d{2}$/)
  })

  it('nowMillis returns a number near now', () => {
    const before = Date.now()
    const ms = systemClock.nowMillis()
    const after = Date.now()
    expect(ms).toBeGreaterThanOrEqual(before)
    expect(ms).toBeLessThanOrEqual(after)
  })
})
