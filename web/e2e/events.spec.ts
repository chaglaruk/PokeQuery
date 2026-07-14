import { test, expect, type Page } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

// Scenarios 17-23, 29: Event feed production success, manual refresh,
// failed network → cached feed, no cache → bundled fallback, expired events
// sorted to bottom (not at top), duplicate GO Fest entries absent,
// offline reload after one successful online load, expanded event detail.
//
// Tests use route interception to mock the production feed URL for deterministic results.

const PRODUCTION_FEED_URL = 'https://raw.githubusercontent.com/chaglaruk/PokeQuery/master/docs/event-feed/pokequery-events.json'

// A minimal mock event feed with CURRENT, UPCOMING, and ENDED events.
// Dates are chosen so CURRENT/UPCOMING pass the lifecycle filter (aced with today's date),
// while the ENDED event has dates wholly in the past → gets filtered out.
function mockFeed() {
  return {
    schemaVersion: 1,
    lastUpdated: '2026-07-10',
    events: [
      {
        id: 'mock-current-event',
        title: 'Current Test Event',
        status: 'CURRENT',
        importanceTier: 'MAJOR',
        startDate: '2026-07-01',
        endDate: '2026-07-31',
        note: 'note',
        summary: 'summary text',
        prep: 'prep guidance',
        suggestedSearch: '0*&!traded',
        eventNotes: 'event notes here',
        themeKey: 'generic_event',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: '2026-07-10',
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
        id: 'mock-upcoming-event',
        title: 'Upcoming Test Event',
        titleTr: 'Yaklaşan Test Etkinliği',
        status: 'UPCOMING',
        importanceTier: 'STANDARD',
        startDate: '2026-12-01',
        endDate: '2026-12-31',
        note: 'note',
        summary: 'summary text',
        prep: 'prep guidance',
        suggestedSearch: '4*&!traded',
        eventNotes: 'event notes',
        themeKey: 'raid',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: '2026-07-10',
      },
      {
        id: 'mock-ended-event',
        title: 'Ended Test Event',
        status: 'ENDED',
        importanceTier: 'STANDARD',
        startDate: '2026-01-01',
        endDate: '2026-01-15',
        note: 'note',
        summary: 'summary text',
        prep: 'prep guidance',
        suggestedSearch: '1*&!traded',
        eventNotes: 'event notes',
        themeKey: 'research',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: '2026-07-10',
      },
      {
        id: 'event-pokemon-go-fest-2026-global',
        title: 'GO Fest Test',
        status: 'UPCOMING',
        importanceTier: 'MAJOR',
        startDate: '2026-07-20',
        endDate: '2026-07-21',
        note: 'note',
        summary: 'GO Fest summary',
        prep: 'GO Fest prep',
        suggestedSearch: 'shiny',
        eventNotes: 'event notes',
        themeKey: 'generic_event',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: '2026-07-10',
      },
      {
        id: 'event-go-fest-2026-global-final-details',
        title: 'GO Fest Test Duplicate',
        status: 'UPCOMING',
        importanceTier: 'MAJOR',
        startDate: '2026-07-20',
        endDate: '2026-07-21',
        note: 'duplicate',
        summary: 'duplicate',
        prep: 'duplicate',
        suggestedSearch: 'shiny',
        eventNotes: 'duplicate',
        themeKey: 'generic_event',
        sourceName: 'test',
        sourceUrl: 'https://example.com',
        sourceType: 'official',
        lastUpdated: '2026-07-10',
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
    // Wait for cards to appear (loading spinner removed)
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    // Verify the live feed status badge is shown
    await expect(page.getByText('Live event feed', { exact: false }).first()).toBeVisible()
  })

  test('18. manual event refresh button re-fetches', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })

    // Three canonical active events remain; the GO Fest alias and ENDED event are filtered out.
    const initialCount = await page.locator('[data-event-id]').count()
    expect(initialCount).toBe(3)

    // Click the Refresh button
    await page.getByText('Refresh now').click()
    // Cards should still be visible after refresh
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    const postRefreshCount = await page.locator('[data-event-id]').count()
    expect(postRefreshCount).toBe(3)
  })

  test('19. failed network → cached feed is used', async ({ page }) => {
    // First load: succeed and populate cache
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    // Wait for cache to be written
    await page.waitForFunction(() => localStorage.getItem('pq_event_feed_cache') !== null)

    // Now intercept with failure
    await page.unroute(PRODUCTION_FEED_URL)
    await interceptFeedFailure(page)

    // Refresh — should use cache (not crash, cards appear)
    await page.getByText('Refresh now').click()
    // Wait for cards visible after refresh (cache path)
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    // Source should be cached (Saved feed badge)
    await expect(page.getByText('Saved feed', { exact: false }).first()).toBeVisible()
  })

  test('20. no cache → bundled fallback is used', async ({ page }) => {
    // Ensure no cache exists
    await page.addInitScript(() => {
      localStorage.removeItem('pq_event_feed_cache')
      localStorage.removeItem('pq_event_feed_cache_ts')
    })

    // Intercept production feed with failure
    await interceptFeedFailure(page)
    await gotoRoute(page, '/events')

    // Wait for cards from bundled fallback (197 KB file, should load fast locally)
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    // Source badge should say "Bundled fallback guidance"
    await expect(page.getByText('Bundled fallback', { exact: false }).first()).toBeVisible()
  })

  test('21. ended events are hidden, current sorted before upcoming', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })

    // Get the titles in document order
    const cards = page.locator('[data-event-id]')
    const count = await cards.count()
    // ENDED and the canonical GO Fest duplicate are filtered out.
    expect(count).toBe(3)

    const firstTitle = await cards.nth(0).locator('p').first().textContent()
    const lastTitle = await cards.nth(count - 1).locator('p').first().textContent()
    // CURRENT sorts first, UPCOMING second
    expect(firstTitle).toContain('Current Test Event')
    expect(lastTitle).toContain('Upcoming Test Event')

    // Verify the ENDed event is not visible at all
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
    // Step 1: initial online load populates cache
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByText('Live event feed', { exact: false }).first()).toBeVisible()
    await page.waitForFunction(() => localStorage.getItem('pq_event_feed_cache') !== null)

    // Step 2: simulate offline by intercepting with network failure
    await page.unroute(PRODUCTION_FEED_URL)
    await interceptFeedFailure(page)

    // Reload — should pull from cache, not crash
    await page.reload()
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
    // Source badge should now be "Saved feed"
    await expect(page.getByText('Saved feed', { exact: false }).first()).toBeVisible()
  })

  test('29. featured event card shows summary, notes, and search string', async ({ page }) => {
    await interceptFeedSuccess(page)
    await gotoRoute(page, '/events')
    await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })

    // The featured hero card already shows full content (summary, search string, copy)
    // without needing a click. Verify these are visible.
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
    let dialog = page.getByRole('dialog', { name: 'Yaklaşan Test Etkinliği' })
    await expect(dialog).toBeVisible()
    await expect(dialog.getByText('summary text')).toBeVisible()
    await expect(dialog.getByText('Aramayı kopyala')).toBeVisible()
    const close = dialog.getByRole('button', { name: /close/i })
    await expect(close).toBeFocused()
    await close.click()
    await expect(dialog).toBeHidden()
    await expect(trigger).toBeFocused()

    await trigger.click()
    dialog = page.getByRole('dialog', { name: 'Yaklaşan Test Etkinliği' })
    await page.keyboard.press('Escape')
    await expect(dialog).toBeHidden()

    await trigger.click()
    dialog = page.getByRole('dialog', { name: 'Yaklaşan Test Etkinliği' })
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
    await expect(dialog).toHaveCSS('transform', 'none')
    const close = dialog.getByRole('button', { name: /close/i })
    await expect(close).toBeFocused()

    const box = await dialog.boundingBox()
    const viewport = page.viewportSize()
    expect(box).not.toBeNull()
    expect(viewport).not.toBeNull()
    expect(box!.y).toBeGreaterThanOrEqual(0)
    expect(box!.y + box!.height).toBeLessThanOrEqual(viewport!.height)

    await close.click()
    await expect(dialog).toBeHidden()
    await expect(trigger).toBeFocused()
  })
})
