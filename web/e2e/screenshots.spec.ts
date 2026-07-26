import { test, expect, type Page } from '@playwright/test'
import { gotoRoute } from './helpers'
import * as fs from 'fs'
import * as path from 'path'
import { fileURLToPath } from 'url'

const PRODUCTION_FEED_URL = 'https://raw.githubusercontent.com/chaglaruk/PokeQuery/master/docs/event-feed/pokequery-events.json'
const SCREENSHOT_DIR = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../docs/screenshots/pwa_initial_qa')

const screenshotFeed = {
  schemaVersion: 1,
  lastUpdated: '2026-07-13',
  events: [
    {
      id: 'screenshot-featured',
      title: 'Summer Showcase',
      status: 'CURRENT',
      importanceTier: 'MAJOR',
      eventCategory: 'MAJOR_GAMEPLAY',
      startDate: '2026-07-01',
      endDate: '2026-07-31',
      note: 'Review featured catches before cleanup.',
      summary: 'A deterministic current event for visual review.',
      prep: 'Tag featured catches and review the suggested search.',
      suggestedSearch: '0*&!traded',
      eventNotes: 'Visual QA fixture',
      themeKey: 'generic_event',
      sourceName: 'PokeQuery visual fixture',
      sourceUrl: 'https://example.com',
      sourceType: 'official',
      lastUpdated: '2026-07-13',
      pokemon: [{
        name: 'Pikachu',
        source: 'featured encounter',
        note: 'Keep one for visual review.',
        badges: 'Featured',
        spriteKey: 'pikachu',
      }],
    },
    {
      id: 'screenshot-upcoming',
      title: 'Upcoming Review Event',
      status: 'UPCOMING',
      importanceTier: 'STANDARD',
      startDate: '2026-12-01',
      endDate: '2026-12-02',
      note: 'Plan storage before the event starts.',
      summary: 'A compact event used to verify the detail dialog.',
      prep: 'Open the detail sheet and review the action.',
      suggestedSearch: '4*&!traded',
      eventNotes: 'Visual QA fixture',
      themeKey: 'generic_event',
      sourceName: 'PokeQuery visual fixture',
      sourceUrl: 'https://example.com',
      sourceType: 'official',
      lastUpdated: '2026-07-13',
    },
  ],
}

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

async function screenshot(page: Page, projectName: string, stateName: string, capture: 'viewport' | 'full-content' = 'viewport') {
  const dir = path.join(SCREENSHOT_DIR, deviceLabel(projectName))
  fs.mkdirSync(dir, { recursive: true })
  const filePath = path.join(dir, `${stateName}.png`)
  const qaStyle = capture === 'full-content'
    ? await page.addStyleTag({ content: '.bottom-nav { display: none !important; } .content-with-nav { padding-bottom: 16px !important; }' })
    : null
  await page.screenshot({ path: filePath, fullPage: capture === 'full-content' })
  await qaStyle?.evaluate(element => element.remove())
  console.log(`  Saved: ${filePath}`)
}

test('generate current PWA visual QA states', async ({ page }) => {
  const projectName = test.info().project.name
  test.setTimeout(180000)

  await page.goto('')
  await expect(page).toHaveURL(/#\/$/)
  await expect(page.getByText('Safe Cleanup').first()).toBeVisible()
  await page.evaluate(() => window.scrollTo(0, 0))
  await screenshot(page, projectName, '01-home-en-viewport')

  await gotoRoute(page, '/settings')
  await page.locator('select').first().selectOption('Türkçe')
  await gotoRoute(page, '/')
  await expect(page.getByText('Güvenli Temizlik').first()).toBeVisible()
  await page.evaluate(() => window.scrollTo(0, 0))
  await screenshot(page, projectName, '02-home-tr-viewport')
  await gotoRoute(page, '/settings')
  await page.locator('select').first().selectOption('English')

  await gotoRoute(page, '/assistant')
  const assistantInput = page.getByRole('textbox', { name: 'Search Assistant' })
  await expect(assistantInput).toBeVisible()
  await page.evaluate(() => window.scrollTo(0, 0))
  await screenshot(page, projectName, '03-search-assistant-empty-viewport')

  await assistantInput.fill('shiny legendary')
  await assistantInput.press('Enter')
  await expect(page.locator('.search-string')).toBeVisible()
  await page.locator('.search-string').scrollIntoViewIfNeeded()
  await screenshot(page, projectName, '04-search-assistant-success-viewport')

  await assistantInput.fill('xyz qwerty')
  await assistantInput.press('Enter')
  await expect(page.getByRole('status')).toBeVisible()
  await page.getByRole('status').scrollIntoViewIfNeeded()
  await screenshot(page, projectName, '05-search-assistant-unknown-viewport')

  await gotoRoute(page, '/goal/safe_cleanup')
  await expect(page.locator('.search-string')).toBeVisible()
  await page.locator('.search-string').scrollIntoViewIfNeeded()
  await screenshot(page, projectName, '06-safe-cleanup-viewport')

  await page.route(PRODUCTION_FEED_URL, route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify(screenshotFeed),
  }))
  await gotoRoute(page, '/events')
  await expect(page.locator('[data-event-id]').first()).toBeVisible({ timeout: 20000 })
  await page.evaluate(() => window.scrollTo(0, 0))
  await screenshot(page, projectName, '07-event-guide-top-viewport')

  await page.locator('[data-event-id="screenshot-upcoming"]').click()
  const eventDialog = page.getByRole('dialog', { name: 'Upcoming Review Event' })
  await expect(eventDialog).toHaveCSS('transform', 'none')
  await screenshot(page, projectName, '08-event-detail-dialog-viewport')
  await eventDialog.getByRole('button', { name: /close/i }).click()

  await page.locator('[data-pokemon-name="Pikachu"]').first().click()
  const pokemonDialog = page.getByRole('dialog', { name: 'Pikachu' })
  await expect(pokemonDialog).toHaveCSS('transform', 'none')
  await screenshot(page, projectName, '09-pokemon-detail-dialog-viewport')
  await pokemonDialog.getByRole('button', { name: /close/i }).click()

  await gotoRoute(page, '/settings')
  await expect(page.locator('select').first()).toBeVisible()
  await expect(page.locator('.bottom-nav .nav-item.active')).toHaveText('Settings')
  await page.evaluate(() => window.scrollTo(0, 0))
  await screenshot(page, projectName, '10-settings-viewport')

  await page.getByText('What Changed / Changelog').click()
  await expect(page).toHaveURL(/#\/changelog/)
  await screenshot(page, projectName, '11-changelog-full-content', 'full-content')

  await gotoRoute(page, '/events')
  await page.waitForFunction(() => localStorage.getItem('pq_event_feed_cache') !== null)
  await page.unroute(PRODUCTION_FEED_URL)
  await page.route(PRODUCTION_FEED_URL, route => route.fulfill({ status: 503, body: 'Service Unavailable' }))
  await page.getByText('Refresh now').click()
  await expect(page.getByText('Saved feed', { exact: false }).first()).toBeVisible({ timeout: 20000 })
  await page.evaluate(() => window.scrollTo(0, 0))
  await screenshot(page, projectName, '12-offline-cached-feed-viewport')

  await gotoRoute(page, '/')
  await page.evaluate(() => localStorage.setItem('pq_screenshot_need_refresh', 'true'))
  await page.reload()
  const updateBanner = page.getByTestId('pwa-update-banner')
  await expect(updateBanner).toBeVisible()
  await page.evaluate(() => window.scrollTo(0, 0))
  await updateBanner.evaluate(element => Promise.all(
    element.getAnimations().map(animation => animation.finished)
  ))
  await screenshot(page, projectName, '13-update-banner-viewport')
})
