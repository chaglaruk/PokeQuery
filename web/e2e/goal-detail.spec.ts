import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

// Scenarios 8-13: Goal selection, exact search text, forbidden |, !traded, clipboard.

test.describe('Goal selection and search text (scenarios 8-13)', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
  })

  test('8. tapping a goal card navigates to goal detail', async ({ page }) => {
    await gotoRoute(page, '/')
    await page.locator('.page').getByText('Safe Cleanup').first().click()
    await expect(page).toHaveURL(/#\/goal\/safe_cleanup/)
  })

  test('9. Safe Cleanup shows exact generated search text', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    // Wait for the result section with .search-string
    await expect(page.locator('.search-string')).toBeVisible()
    const text = await page.locator('.search-string').textContent()
    expect(text).toContain('1*')
    expect(text).toContain('!shiny')
    expect(text).toContain('!legendary')
    expect(text).toContain('!mythical')
    expect(text).toContain('!ultrabeast')
    expect(text).toContain('!costume')
    expect(text).toContain('!shadow')
    expect(text).toContain('!favorite')
    expect(text).toContain('!lucky')
    expect(text).toContain('!traded')
    expect(text).toContain('!4*')
    // Exact full string
    expect(text).toBe('1*&!shiny&!legendary&!mythical&!ultrabeast&!costume&!background&!locationbackground&!specialbackground&!shadow&!purified&!favorite&!lucky&!#&!traded&!4*')
  })

  test('9b. Trade Fodder shows exact generated search text', async ({ page }) => {
    await gotoRoute(page, '/goal/trade_fodder')
    await expect(page.locator('.search-string')).toBeVisible()
    const text = await page.locator('.search-string').textContent()
    expect(text).toContain('count2-')
    expect(text).toContain('!traded')
    expect(text).toContain('!shiny')
    expect(text).toContain('!4*')
  })

  test('10. forbidden | in expert builder disables copy and shows error', async ({ page }) => {
    await gotoRoute(page, '/goal/expert')
    const input = page.locator('input[type="text"]').first()
    await input.fill('shiny|4*')
    await expect(input).toHaveValue('shiny|4*')
    // Copy button should be disabled
    await expect(page.locator('.btn-copy')).toBeDisabled()
    await expect(page.getByText('Fix errors to copy').first()).toBeVisible()
  })

  test('10b. valid expert builder input enables copy', async ({ page }) => {
    await gotoRoute(page, '/goal/expert')
    const input = page.locator('input[type="text"]').first()
    await input.fill('shiny&4*')
    await expect(page.locator('.btn-copy')).toBeEnabled()
  })

  test('11. !traded appears exactly once in Safe Cleanup search', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()
    const text = await page.locator('.search-string').textContent() ?? ''
    const count = (text.match(/!traded/g) ?? []).length
    expect(count).toBe(1)
    // traded without ! should NOT appear (would be a positive filter)
    const tradedPositive = (text.match(/(?<!!)traded/g) ?? []).length
    expect(tradedPositive).toBe(0)
  })

  test('12. clipboard success writes search string', async ({ page, browserName }) => {
    test.skip(browserName === 'webkit', 'Clipboard write requires secure context or user gesture in WebKit; tested in chromium')

    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()

    // Grant clipboard permissions and mock the API
    await page.context().grantPermissions(['clipboard-read', 'clipboard-write'])
    await page.addInitScript(() => {
      const items = new Map<string, string>()
      const clipboard = {
        writeText: (text: string) => Promise.resolve().then(() => { items.set('text', text) }),
        readText: () => Promise.resolve(items.get('text') ?? ''),
      }
      Object.defineProperty(navigator, 'clipboard', { value: clipboard, configurable: true })
    })

    // Navigate fresh so the mock is installed
    await page.reload()
    await expect(page.locator('.search-string')).toBeVisible()

    const expectedText = await page.locator('.search-string').textContent()
    await page.getByText('Copy', { exact: false }).first().click()
    // Verify copied indicator appears
    await expect(page.getByText(/Copied/i).first()).toBeVisible({ timeout: 5000 })
    // Verify clipboard content matches the search string
    const clipboardText = await page.evaluate(() => navigator.clipboard.readText())
    expect(clipboardText).toBe(expectedText)
  })

  test('13. clipboard denied/failure does not crash the app', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()

    // Override clipboard.writeText to reject (simulate permission denied)
    await page.evaluate(() => {
      const writeText = () => Promise.reject(new DOMException('Permission denied', 'NotAllowedError'))
      Object.defineProperty(navigator, 'clipboard', { value: { writeText, readText: () => Promise.resolve('') }, configurable: true })
    })

    // Clicking copy should not throw; app continues
    await page.getByText('Copy', { exact: false }).first().click()
    // Wait briefly to confirm no crash
    await expect(page.locator('.search-string')).toBeVisible()
    // App is still functional: can navigate
    await page.locator('.back-btn').click()
    await expect(page).toHaveURL(/#\/$/)
  })
})
