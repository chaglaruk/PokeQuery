import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

test.describe('Home screen and navigation', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
    await gotoRoute(page, '/')
  })

  test('6. Home displays all goal cards', async ({ page }) => {
    const goals = [
      'Safe Cleanup', '2x Candy Prep', 'Trade Fodder',
      'Hundo Check', 'Nundo Finder', 'PvP IV Candidates',
      'Lucky Trade Prep', 'Untagged Cleanup', 'Expert Builder',
    ]
    for (const goal of goals) {
      // Scope to the main content area (not bottom nav) to avoid false matches.
      await expect(page.locator('.page').getByText(goal).first()).toBeVisible()
    }
  })

  test('7. tapping a goal card navigates to goal detail', async ({ page }) => {
    await page.locator('.page').getByText('Safe Cleanup').first().click()
    await expect(page).toHaveURL(/#\/goal\/safe_cleanup/)
  })

  test('8. bottom nav Home item is active on Home', async ({ page }) => {
    const homeNav = page.locator('.bottom-nav .nav-item').first()
    await expect(homeNav).toHaveClass(/active/)
  })

  test('9. bottom nav: Events item navigates to Events screen', async ({ page }) => {
    await page.locator('.bottom-nav .nav-item').filter({ hasText: '📅' }).click()
    await expect(page).toHaveURL(/#\/events/)
  })

  test('10. bottom nav: Settings item navigates to Settings screen', async ({ page }) => {
    await page.locator('.bottom-nav .nav-item').filter({ hasText: '⚙️' }).click()
    await expect(page).toHaveURL(/#\/settings/)
  })
})
