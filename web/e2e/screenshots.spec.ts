import { test, expect, type Page } from '@playwright/test'
import { skipOnboarding, gotoRoute, setStorage } from './helpers'
import * as fs from 'fs'
import * as path from 'path'
import { fileURLToPath } from 'url'

const PRODUCTION_FEED_URL = 'https://raw.githubusercontent.com/chaglaruk/PokeQuery/master/docs/event-feed/pokequery-events.json'

const __filename2 = fileURLToPath(import.meta.url)
const __dirname2 = path.dirname(__filename2)
const SCREENSHOT_DIR = path.resolve(__dirname2, '../../docs/screenshots/pwa_initial_qa')

// Ensure device subdirectory exists
function ensureDir(deviceName: string): string {
  const dir = path.join(SCREENSHOT_DIR, deviceName)
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true })
  return dir
}

// Map project name to device label for screenshot directory
function deviceLabel(projectName: string): string {
  switch (projectName) {
    case 'chromium-mobile': return 'pixel-5'
    case 'webkit-iphone-se': return 'iphone-se'
    case 'webkit-iphone-13': return 'iphone-13'
    case 'webkit-iphone-pro-max': return 'iphone-14-pro-max'
    case 'desktop': return 'desktop'
    default: return projectName
  }
}

async function screenshot(page: Page, projectName: string, stateName: string) {
  const dir = ensureDir(deviceLabel(projectName))
  const filePath = path.join(dir, `${stateName}.png`)
  await page.screenshot({ path: filePath, fullPage: true })
  console.log(`  Saved: ${filePath}`)
}

test.describe('Visual QA screenshots', () => {
  test.skip(!process.env.SCREENSHOTS, 'Set SCREENSHOTS=true to generate visual QA screenshots')

  test('generate all PWA screenshots', async ({ page, browserName }) => {
    const projectName = test.info().project.name
    test.setTimeout(120000)

    // 1. Onboarding page 1 (fresh context, no skipOnboarding)
    await page.goto('')
    await expect(page).toHaveURL(/#\/onboarding/)
    await expect(page.getByRole('heading', { name: 'PokeQuery' })).toBeVisible()
    await screenshot(page, projectName, '01-onboarding-page-1')

    // 2. Onboarding page 2
    await page.getByText('Next', { exact: false }).click()
    await expect(page.getByText('Review first, act second')).toBeVisible()
    await screenshot(page, projectName, '02-onboarding-page-2')

    // Complete onboarding for remaining screenshots
    await page.getByText('Start building', { exact: false }).click()
    await expect(page).toHaveURL(/#\/$/)

    // 3. Home (English)
    await screenshot(page, projectName, '03-home-en')

    // 4. Safe Cleanup goal detail
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()
    await screenshot(page, projectName, '04-safe-cleanup')

    // 5. Search Explain
    await gotoRoute(page, '/explain')
    await expect(page.locator('input[type="text"]')).toBeVisible()
    await page.locator('input[type="text"]').first().fill('shiny&4*&!legendary')
    await expect(page.getByText('Token breakdown')).toBeVisible()
    await screenshot(page, projectName, '05-explain')

    // 6. Event Guide (use bundled fallback for deterministic screenshots)
    await page.route(PRODUCTION_FEED_URL, route => route.fulfill({ status: 500, body: 'Server Error' }))
    await gotoRoute(page, '/events')
    await expect(page.locator('.card.card-tap').first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByText('Bundled fallback', { exact: false }).first()).toBeVisible()
    await screenshot(page, projectName, '06-event-guide')

    // 7. Expanded Event detail (click first card)
    const firstCard = page.locator('.card.card-tap').first()
    await firstCard.click()
    await expect(page.locator('.search-string').first()).toBeVisible()
    await screenshot(page, projectName, '07-expanded-event-detail')

    // 8. Settings
    await gotoRoute(page, '/settings')
    await expect(page.locator('select').first()).toBeVisible()
    await screenshot(page, projectName, '08-settings')

    // 9. Changelog
    const changelogLink = page.locator('.card.card-tap').filter({ hasText: 'What Changed' })
    await expect(changelogLink).toBeVisible()
    await changelogLink.click()
    await expect(page).toHaveURL(/#\/changelog/)
    await expect(page.getByText(/v0\.\d+\.\d+/i, { exact: false }).first()).toBeVisible()
    await screenshot(page, projectName, '09-changelog')

    // 10. Turkish Home
    await gotoRoute(page, '/settings')
    await page.locator('select').first().selectOption('T\u00fcrk\u00e7e')
    await gotoRoute(page, '/')
    await page.waitForTimeout(500) // re-render with Turkish locale
    await screenshot(page, projectName, '10-turkish-home')

    // 11. Turkish Goal detail (Safe Cleanup in Turkish)
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()
    await screenshot(page, projectName, '11-turkish-goal-detail')

    // Reset language to English for remaining screenshots
    await gotoRoute(page, '/settings')
    await page.locator('select').first().selectOption('English')

    // 12. Offline/cached feed state
    // First: populate the localStorage cache with a successful online load
    await page.unroute(PRODUCTION_FEED_URL)
    await page.route(PRODUCTION_FEED_URL, route => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        schemaVersion: 1,
        lastUpdated: '2026-07-10',
        events: [{
          id: 'mock-cached-event',
          title: 'Cached Event for Screenshot',
          status: 'CURRENT',
          importanceTier: 'MAJOR',
          note: 'note',
          summary: 'This is a cached event shown when offline.',
          prep: 'prep guidance',
          suggestedSearch: '0*&!traded',
          eventNotes: 'event notes',
          themeKey: 'generic_event',
          sourceName: 'test',
          sourceUrl: 'https://example.com',
          sourceType: 'official',
          lastUpdated: '2026-07-10',
        }],
      }),
    }))
    await gotoRoute(page, '/events')
    await expect(page.locator('.card.card-tap').first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByText('Live event feed', { exact: false }).first()).toBeVisible()
    // Wait for cache to be written to localStorage
    await page.waitForFunction(() => localStorage.getItem('pq_event_feed_cache') !== null)

    // Now make the network fail and refresh — should show "Saved feed" from cache
    await page.unroute(PRODUCTION_FEED_URL)
    await page.route(PRODUCTION_FEED_URL, route => route.fulfill({ status: 503, body: 'Service Unavailable' }))
    await page.getByText('Refresh now').click()
    await expect(page.getByText('Saved feed', { exact: false }).first()).toBeVisible({ timeout: 20000 })
    await screenshot(page, projectName, '12-offline-cached-feed')

    // 13. Update state (update-available prompt — best-effort)
    // The PWA plugin uses registerType: 'prompt'. In normal operation, an update
    // prompt appears only when a new SW version is detected. We simulate the
    // prompt by dispatching the custom event the plugin listens for.
    await page.unroute(PRODUCTION_FEED_URL)
    await page.evaluate(() => {
      // Dispatch the event that triggers the update prompt UI
      window.dispatchEvent(new CustomEvent('vite-pwa:updated'))
    })
    await page.waitForTimeout(1000)
    await screenshot(page, projectName, '13-update-prompt')

    expect(true).toBe(true) // All screenshots captured
  })
})
