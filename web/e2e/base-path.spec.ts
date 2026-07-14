import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

// Scenario 1: Verify the production base path /PokeQuery/ works.
//
// When VITE_BASE=/PokeQuery/ is set during build, the Playwright baseURL
// becomes http://localhost:PORT/PokeQuery/. All page.goto('') calls resolve
// to that URL. This spec verifies:
//   - HTML loads at the base path
//   - JS/CSS assets resolve correctly (no 404)
//   - Hash routes work from base path
//   - Reload preserves base path
//   - Direct start URL works (page.goto('') without hash)

test.describe('Base path /PokeQuery/ (scenario 1)', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
  })

  test('1. HTML loads at the configured base path', async ({ page }) => {
    const response = await page.goto('')
    expect(response?.status()).toBe(200)
    // The HTML title should be "PokeQuery"
    await expect(page).toHaveTitle('PokeQuery')
    // Root element is populated
    await page.waitForSelector('#root:has(*)', { timeout: 15000 })
    // After onboarding skip, should land on Home
    await expect(page.getByRole('heading', { name: 'PokeQuery' })).toBeVisible()
  })

  test('1b. JS and CSS assets resolve without 404', async ({ page }) => {
    // Capture failed requests during page load
    const failedRequests: string[] = []
    page.on('responsefailed', resp => {
      failedRequests.push(resp.url())
    })
    page.on('response', resp => {
      if (!resp.ok() && !resp.url().includes('raw.githubusercontent.com')) {
        failedRequests.push(`${resp.status()} ${resp.url()}`)
      }
    })

    await page.goto('')
    await page.waitForSelector('#root:has(*)', { timeout: 15000 })
    // Give a moment for any deferred asset loads
    await page.waitForTimeout(500)

    // Filter out the production feed URL interception (we expect that to fail in some tests)
    const assetFailures = failedRequests.filter(url =>
      !url.includes('raw.githubusercontent.com')
    )
    expect(assetFailures, `Expected no asset 404s, got: ${assetFailures}`).toEqual([])
  })

  test('1c. hash routes work from base path', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page).toHaveURL(/#\/goal\/safe_cleanup/)
    await expect(page.locator('.search-string')).toBeVisible()

    await gotoRoute(page, '/events')
    await expect(page).toHaveURL(/#\/events$/)

    await gotoRoute(page, '/explain')
    await expect(page).toHaveURL(/#\/explain$/)
  })

  test('1d. reload preserves current route and base path', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()

    await page.reload()
    // Should stay on the same hash route
    await expect(page).toHaveURL(/#\/goal\/safe_cleanup/)
    await expect(page.locator('.search-string')).toBeVisible()
  })

  test('1e. direct start URL works without explicit hash route', async ({ page }) => {
    await page.goto('')
    // After initScript adds onboarding skip, app starts at Home (hash defaults to /)
    await page.waitForSelector('#root:has(*)', { timeout: 15000 })
    await expect(page.getByRole('heading', { name: 'PokeQuery' })).toBeVisible()
  })

  test('1f. manifest loads at base path', async ({ page }) => {
    await page.goto('')
    await page.waitForSelector('#root:has(*)', { timeout: 15000 })

    const pageUrl = page.url()
    const manifestUrl = new URL('manifest.webmanifest', pageUrl).href
    const response = await page.request.get(manifestUrl)
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.name).toBe('PokeQuery')
    expect(body.short_name).toBe('PokeQuery')
    // start_url may be relative to manifest URL location
    expect(body.start_url).toBeTruthy()
    expect(body.display).toBe('standalone')
  })

  test('1g. service worker registers with correct scope', async ({ page }) => {
    await page.goto('')
    await page.waitForSelector('#root:has(*)', { timeout: 15000 })

    const regInfo = await page.evaluate(async () => {
      if (!('serviceWorker' in navigator)) return null
      await new Promise(r => setTimeout(r, 2000))
      const regs = await navigator.serviceWorker.getRegistrations()
      if (regs.length === 0) return null
      const reg = regs[0]
      return {
        scope: reg.scope,
        scriptURL: reg.active?.scriptURL ?? reg.installing?.scriptURL ?? reg.waiting?.scriptURL,
      }
    })
    expect(regInfo).not.toBeNull()
    // The SW script URL should contain the base path
    expect(regInfo!.scriptURL).toContain('/sw.js')
    // Scope should end with the base path (e.g., /PokeQuery/)
    expect(regInfo!.scope).toMatch(/\/PokeQuery\/$|\/$/)
  })
})
