import { test, expect, type Page } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

// Scenarios 17-23, 29-31: Event feed production success, manual refresh,
// failed network -> cached feed, no cache -> bundled fallback, expired events
// sorted to bottom (not at top), duplicate GO Fest entries absent,
// offline reload after one successful online load, expanded event detail.
//
// Tests use route interception to mock the production feed URL for deterministic results.

const PRODUCTION_FEED_URL = 'https://raw.githubusercontent.com/chaglaruk/PokeQuery/master/docs/event-feed/pokequery-events.json'

function isoDateFromToday(days: number) {
  const date = new Date()
  date.setDate(date.getDate() + days)
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

function mockFeed() {
  const today = isoDateFromToday(0)
  const currentStart = isoDateFromToday(-1)
  const currentEnd = isoDateFromToday(1)
  const goFestStart = isoDateFromToday(10)
  const goFestEnd = isoDateFromToday(11)
  const upcomingStart = isoDateFromToday(30)
  const upcomingEnd = isoDateFromToday(31)
  const endedStart = isoDateFromToday(-31)
  const endedEnd = isoDateFromToday(-30)

  return {
    schemaVersion: 1,
    lastUpdated: today,
    events: [
      {
        id: 'mock-current-event',
        title: 'Current Test Event',
        status: 'CURRENT',
        importanceTier: 'MAJOR',
        startDate: currentStart,
        endDate: currentEnd,
        note: 'note',
        summary: 'summary text',
        prep: 'prep guidance',
        suggestedSearch: '0*&!traded',
        eventNotes: 'event notes here',
        themeKey: 'generic_event',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: today,
        pokemon: [{
          name: 'Pikachu',
          nameTr: 'Pikachu',
          source: 'wild',
          note: 'Featured encounter guidance',
          badges: 'Featured',
          spriteKey: 'pikachu',
        }],
      },
      {
        id: 'event-pokemon-go-fest-2026-global',
        title: 'GO Fest 2026: Global',
        status: 'UPCOMING',
        importanceTier: 'MAJOR',
        startDate: goFestStart,
        endDate: goFestEnd,
        note: 'note',
        summary: 'summary text',
        prep: 'prep guidance',
        suggestedSearch: '0*&!traded',
        eventNotes: 'event notes here',
        themeKey: 'generic_event',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: today,
      },
      {
        id: 'event-go-fest-2026-global-final-details',
        title: 'GO Fest 2026: Global',
        status: 'UPCOMING',
        importanceTier: 'MAJOR',
        startDate: goFestStart,
        endDate: goFestEnd,
        note: 'note',
        summary: 'summary text',
        prep: 'prep guidance',
        suggestedSearch: '0*&!traded',
        eventNotes: 'event notes here',
        themeKey: 'generic_event',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: today,
      },
      {
        id: 'mock-upcoming-event',
        title: 'Upcoming Test Event',
        titleTr: 'Yaklaşan Test Etkinliği',
        status: 'UPCOMING',
        importanceTier: 'STANDARD',
        startDate: upcomingStart,
        endDate: upcomingEnd,
        note: 'note',
        summary: 'summary text',
        prep: 'prep guidance',
        suggestedSearch: '0*&!traded',
        eventNotes: 'event notes here',
        themeKey: 'generic_event',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: today,
      },
      {
        id: 'mock-ended-event',
        title: 'Ended Test Event',
        titleTr: 'Biten Test Etkinliği',
        status: 'ENDED',
        importanceTier: 'MINOR',
        startDate: endedStart,
        endDate: endedEnd,
        note: 'note',
        summary: 'summary text',
        prep: 'prep guidance',
        suggestedSearch: '0*&!traded',
        eventNotes: 'event notes here',
        themeKey: 'generic_event',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: today,
      },
    ],
  }
}

async function interceptFeedSuccess(page: Page) {
  await page.route(PRODUCTION_FEED_URL, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockFeed()),
    })
  })
}

async function interceptFeedFailure(page: Page) {
  await page.route(PRODUCTION_FEED_URL, async route => {
    await route.fulfill({ status: 500, body: 'Internal Server Error' })
  })
}

test.describe('Events and feed scenarios (scenarios 17-23, 29-31)', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
  })

  test('17. event production-feed success shows online badge', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByText('Live event feed', { exact: false }).first()).toBeVisible()
  })

  test('18. manual event refresh button re-fetches', async ({ page }) => {
    let fetchCount = 0
    await page.route(PRODUCTION_FEED_URL, route => {
      fetchCount++
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockFeed()),
      })
    })

    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    const initialCount = fetchCount

    await page.getByText('Refresh now').click()
    await expect(async () => {
      expect(fetchCount).toBeGreaterThan(initialCount)
    }).toPass({ timeout: 10000 })
  })

  test('19. failed network -> cached feed is used', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    await page.waitForFunction(() => localStorage.getItem('pq_event_feed_cache') !== null)

    await page.unroute(PRODUCTION_FEED_URL)
    await interceptFeedFailure(page)

    await page.getByText('Refresh now').click()
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByText('Saved feed', { exact: false }).first()).toBeVisible()
  })

  test('20. no cache -> bundled fallback is used', async ({ page }) => {
    await page.addInitScript(() => {
      try {
        localStorage.removeItem('pq_event_feed_cache')
        localStorage.removeItem('pq_event_feed_cache_ts')
      } catch { /* ignore */ }
    })

    await interceptFeedFailure(page)
    await gotoRoute(page, '/events')

    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByText('Bundled fallback', { exact: false }).first()).toBeVisible()
  })

  test('21. ended events are hidden, current sorted before upcoming', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })

    const cards = page.locator('[data-event-id]')
    const count = await cards.count()
    expect(count).toBe(3)

    const firstTitle = await cards.nth(0).locator('p').first().textContent()
    const lastTitle = await cards.nth(count - 1).locator('p').first().textContent()
    expect(firstTitle).toContain('Current Test Event')
    expect(lastTitle).toContain('Upcoming Test Event')

    await expect(page.getByText('Ended Test Event')).not.toBeVisible()
  })

  test('22. all rendered cards are active (ended excluded) and no duplicates', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    const cards = page.locator('[data-event-id]')
    await expect(cards.first()).toBeVisible({ timeout: 20000 })

    const ids = await cards.evaluateAll(elements => elements.map(element => element.getAttribute('data-event-id') ?? ''))
    const canonicalIds = ids.map(id => id === 'event-go-fest-2026-global-final-details'
      ? 'event-pokemon-go-fest-2026-global'
      : id)
    expect(ids).toHaveLength(3)
    expect(new Set(canonicalIds).size).toBe(ids.length)
    expect(canonicalIds).toEqual(expect.arrayContaining([
      'mock-current-event',
      'mock-upcoming-event',
      'event-pokemon-go-fest-2026-global',
    ]))
    expect(canonicalIds.filter(id => id === 'event-pokemon-go-fest-2026-global')).toHaveLength(1)
    await expect(page.locator('[data-event-id="mock-ended-event"]')).toHaveCount(0)

    const featuredId = await page.locator('[data-event-section="featured"]').getAttribute('data-event-id')
    expect(ids.filter(id => id === featuredId)).toHaveLength(1)
    const priorityIds = await page.locator('[data-event-section]:not([data-event-section="remainder"])')
      .evaluateAll(elements => elements.map(element => element.getAttribute('data-event-id')))
    const remainderIds = await page.locator('[data-event-section="remainder"]')
      .evaluateAll(elements => elements.map(element => element.getAttribute('data-event-id')))
    expect(remainderIds.every(id => !priorityIds.includes(id))).toBe(true)
  })

  test('23. offline reload after successful online load uses cache', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByText('Live event feed', { exact: false }).first()).toBeVisible()
    await page.waitForFunction(() => localStorage.getItem('pq_event_feed_cache') !== null)

    await page.unroute(PRODUCTION_FEED_URL)
    await interceptFeedFailure(page)

    await page.reload()
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByText('Saved feed', { exact: false }).first()).toBeVisible()
  })

  test('29. featured event card shows summary, notes, and search string', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })

    await expect(page.getByText('summary text').first()).toBeVisible({ timeout: 10000 })
    await expect(page.locator('.search-string').first()).toBeVisible({ timeout: 10000 })
    await expect(page.getByText('Copy search').first()).toBeVisible({ timeout: 10000 })
  })

  test('30. compact event dialog supports close, Escape, and backdrop dismissal', async ({ page }) => {
    await interceptFeedSuccess(page)
    await page.addInitScript(() => localStorage.setItem('pq_app_language', 'Türkçe'))
    await gotoRoute(page, '/events')
    const trigger = page.locator('[data-event-id="mock-upcoming-event"]')
    await expect(trigger).toBeVisible({ timeout: 20000 })

    await trigger.click()
    let dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await expect(dialog.getByText('summary text')).toBeVisible()
    await expect(dialog.locator('.search-string')).toBeVisible()
    const close = dialog.locator('.dialog-close')
    await expect(close).toBeFocused()
    await close.click()
    await expect(dialog).toBeHidden()
    await expect(trigger).toBeFocused()

    await trigger.click()
    dialog = page.getByRole('dialog')
    await page.keyboard.press('Escape')
    await expect(dialog).toBeHidden()

    await trigger.click()
    dialog = page.getByRole('dialog')
    await page.locator('.dialog-overlay').click({ position: { x: 5, y: 5 } })
    await expect(dialog).toBeHidden()
  })

  test('31. featured Pokemon dialog traps and restores focus within the viewport', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    const trigger = page.locator('[data-pokemon-name="Pikachu"]').first()
    await expect(trigger).toBeVisible({ timeout: 20000 })

    await trigger.click()
    const dialog = page.getByRole('dialog', { name: 'Pikachu' })
    await expect(dialog).toBeVisible()
    await expect(dialog.getByText('Featured encounter guidance')).toBeVisible()
    const close = dialog.locator('.dialog-close')
    await expect(close).toBeFocused()

    const triggerBox = await trigger.boundingBox()
    const dialogBox = await dialog.boundingBox()
    const viewport = page.viewportSize()!
    expect(dialogBox!.y + dialogBox!.height).toBeLessThanOrEqual(viewport.height + 2)
    if (triggerBox && triggerBox.y < viewport.height) {
      expect(Math.abs(dialogBox!.y - triggerBox.y)).toBeGreaterThan(10)
    }

    await close.click()
    await expect(dialog).toBeHidden()
    await expect(trigger).toBeFocused()
  })
})
