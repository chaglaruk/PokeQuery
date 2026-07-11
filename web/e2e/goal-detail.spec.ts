import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

test.describe('Goal detail screen', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
    await gotoRoute(page, '/goal/safe_cleanup')
  })

  test('11. Safe Cleanup shows Medium risk badge', async ({ page }) => {
    await expect(page.locator('.badge-medium')).toBeVisible()
  })

  test('12. Copy button writes search string to clipboard', async ({ page, browserName }) => {
    test.skip(browserName === 'webkit', 'clipboard API requires user gesture in WebKit')
    await page.context().grantPermissions(['clipboard-read', 'clipboard-write'])
    await page.getByText('Copy', { exact: false }).first().click()
    const clipboard = await page.evaluate(() => navigator.clipboard.readText())
    expect(clipboard.length).toBeGreaterThan(0)
    expect(clipboard).toContain('!shiny')
  })

  test('13. Expert builder accepts custom input and shows result', async ({ page }) => {
    await page.goto('/#/goal/expert')
    await page.locator('input[type="text"]').first().fill('shiny&4*')
    await expect(page.locator('.search-string')).toBeVisible()
    await expect(page.locator('.search-string')).toContainText('shiny')
    await expect(page.locator('.search-string')).toContainText('4*')
  })

  test('14. Expert copy disabled when linter reports pipe operator error', async ({ page }) => {
    await page.goto('/#/goal/expert')
    await page.locator('input[type="text"]').first().fill('shiny|4*')
    // Copy button should be disabled
    await expect(page.getByText('Copy', { exact: false }).first()).toBeDisabled()
  })
})
