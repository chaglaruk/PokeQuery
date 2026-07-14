import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

// Scenarios 6-8: Home navigation, bottom nav, browser Back navigation.

test.describe('Home screen and navigation (scenarios 6-8)', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
    await gotoRoute(page, '/')
  })

  test('6. Home displays all goal cards and tool cards', async ({ page }) => {
    const goals = [
      'Safe Cleanup', '2x Candy Prep', 'Trade Fodder',
      'Hundo Check', 'Nundo Finder', 'PvP IV Candidates',
      'Lucky Trade Prep', 'Untagged Cleanup', 'Expert Builder',
    ]
    for (const goal of goals) {
      await expect(page.locator('.page').getByText(goal).first()).toBeVisible()
    }
    // Tools section
    await expect(page.locator('.page').getByText('Event Guide').first()).toBeVisible()
    await expect(page.locator('.page').getByText('Explain String').first()).toBeVisible()
  })

  test('7. Browser Back navigation: goal -> back -> home', async ({ page }) => {
    // Navigate to a goal
    await page.locator('.page').getByText('Safe Cleanup').first().click()
    await expect(page).toHaveURL(/#\/goal\/safe_cleanup/)
    // Browser back should return to Home
    await page.goBack()
    await expect(page).toHaveURL(/#\/$/)
  })

  test('7b. Browser Back: Home Event Guide card -> back -> home', async ({ page }) => {
    await page.locator('.page').getByText('Event Guide').first().click()
    await expect(page).toHaveURL(/#\/events/)
    await page.goBack()
    await expect(page).toHaveURL(/#\/$/)
  })

  test('8. bottom nav Home item is active on Home', async ({ page }) => {
    const homeNav = page.locator('.bottom-nav .nav-item').filter({ hasText: 'Home' })
    await expect(homeNav).toHaveClass(/active/)
  })

  test('8b. bottom nav matches Android five-tab order and routes', async ({ page }) => {
    const nav = page.locator('.bottom-nav .nav-item')
    await expect(nav).toHaveCount(5)
    await expect(nav).toHaveText(['Home', 'Favorites', 'History', 'Knowledge', 'Settings'])
    for (const destination of [
      { label: 'Favorites', route: 'favorites' },
      { label: 'History', route: 'history' },
      { label: 'Knowledge', route: 'knowledge' },
    ]) {
      await nav.filter({ hasText: destination.label }).click()
      await expect(page).toHaveURL(new RegExp(`#/${destination.route}`))
      await expect(nav.filter({ hasText: destination.label })).toHaveClass(/active/)
    }
  })

  test('8c. bottom nav: Settings item navigates to Settings screen', async ({ page }) => {
    await page.locator('.bottom-nav .nav-item').filter({ hasText: 'Settings' }).click()
    await expect(page).toHaveURL(/#\/settings/)
    const settingsNav = page.locator('.bottom-nav .nav-item').filter({ hasText: 'Settings' })
    await expect(settingsNav).toHaveClass(/active/)
  })

  test('8d. target routes have no horizontal overflow', async ({ page }) => {
    for (const route of ['/', '/favorites', '/history', '/knowledge', '/settings', '/presets', '/events']) {
      await gotoRoute(page, route)
      await expect.poll(() => page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true)
    }
  })
})
