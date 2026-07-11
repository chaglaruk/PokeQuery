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

  test('7b. Browser Back: events -> back -> home', async ({ page }) => {
    await page.locator('.bottom-nav .nav-item').filter({ hasText: 'Event Guide' }).click()
    await expect(page).toHaveURL(/#\/events/)
    await page.goBack()
    await expect(page).toHaveURL(/#\/$/)
  })

  test('8. bottom nav Home item is active on Home', async ({ page }) => {
    const homeNav = page.locator('.bottom-nav .nav-item').filter({ hasText: 'Home' })
    await expect(homeNav).toHaveClass(/active/)
  })

  test('8b. bottom nav: Events item navigates to Events screen', async ({ page }) => {
    await page.locator('.bottom-nav .nav-item').filter({ hasText: 'Event Guide' }).click()
    await expect(page).toHaveURL(/#\/events/)
    // Events nav now active
    const eventsNav = page.locator('.bottom-nav .nav-item').filter({ hasText: 'Event Guide' })
    await expect(eventsNav).toHaveClass(/active/)
  })

  test('8c. bottom nav: Settings item navigates to Settings screen', async ({ page }) => {
    await page.locator('.bottom-nav .nav-item').filter({ hasText: 'Settings' }).click()
    await expect(page).toHaveURL(/#\/settings/)
    const settingsNav = page.locator('.bottom-nav .nav-item').filter({ hasText: 'Settings' })
    await expect(settingsNav).toHaveClass(/active/)
  })
})
