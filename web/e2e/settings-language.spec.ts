import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute, setStorage } from './helpers'

// Scenarios 14-16: UI Language persistence, Search String Language persistence,
// Independent UI/Search Language values.

test.describe('Language persistence (scenarios 14-16)', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
  })

  test('14. UI Language persists across reload', async ({ page }) => {
    await gotoRoute(page, '/settings')
    const select = page.locator('select').first()
    await expect(select).toBeVisible()

    // Change app language to Turkish
    await select.selectOption('Türkçe')
    // Verify the page text immediately changes to Turkish
    const pageHeader = page.locator('.page-header h1')
    await expect(pageHeader).not.toHaveText(/Settings/)  // English "Settings"
    // The "Settings" heading should be in Turkish now
    await expect(pageHeader).toContainText(/./)  // has some text

    // Reload — app language should be preserved
    await page.reload()
    await expect(select).toHaveValue('Türkçe')

    // Verify localStorage persistence
    const stored = await page.evaluate(() => localStorage.getItem('pq_app_language'))
    expect(stored).toBe('Türkçe')
  })

  test('15. Search String Language persists across reload', async ({ page }) => {
    await gotoRoute(page, '/settings')
    const searchSelect = page.locator('select').nth(1)  // Second select is Search String Language
    await expect(searchSelect).toBeVisible()

    // Change search language to German
    await searchSelect.selectOption('German')
    // Verify localStorage
    const stored = await page.evaluate(() => localStorage.getItem('pq_search_language'))
    expect(stored).toBe('German')

    // Reload — search language should be preserved
    await page.reload()
    await expect(searchSelect).toHaveValue('German')
  })

  test('16. UI Language and Search String Language are independent', async ({ page }) => {
    await gotoRoute(page, '/settings')
    const appSelect = page.locator('select').first()
    const searchSelect = page.locator('select').nth(1)

    // Set App Language = Turkish, Search String Language = German
    await appSelect.selectOption('Türkçe')
    await searchSelect.selectOption('German')

    // Verify each select shows its own value separately
    await expect(appSelect).toHaveValue('Türkçe')
    await expect(searchSelect).toHaveValue('German')

    // Reload — both should preserve independently
    await page.reload()
    await expect(appSelect).toHaveValue('Türkçe')
    await expect(searchSelect).toHaveValue('German')

    // Change App Language back to English, Search Language stays German
    await appSelect.selectOption('English')
    await expect(appSelect).toHaveValue('English')
    await expect(searchSelect).toHaveValue('German')

    // Reload — confirm separate persistence
    await page.reload()
    await expect(appSelect).toHaveValue('English')
    await expect(searchSelect).toHaveValue('German')
  })

  test('16b. initial language from localStorage', async ({ page }) => {
    // Pre-set both language values before navigation
    await setStorage(page, 'pq_app_language', 'Español')
    await setStorage(page, 'pq_search_language', 'French')

    await gotoRoute(page, '/settings')
    const appSelect = page.locator('select').first()
    const searchSelect = page.locator('select').nth(1)

    await expect(appSelect).toHaveValue('Español')
    await expect(searchSelect).toHaveValue('French')
  })
})
