import { test, expect } from '@playwright/test'
import { gotoRoute, skipOnboarding } from './helpers'

test.describe('Favorites, History and clipboard persistence', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'clipboard', {
        configurable: true,
        value: { writeText: async () => undefined },
      })
    })
  })

  test('favorite survives refresh and direct route navigation', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    await page.locator('.header-action').click()
    await page.reload()
    await expect(page.locator('.header-action')).toHaveClass(/active/)
    await gotoRoute(page, '/favorites')
    await expect(page.getByRole('heading', { name: 'Safe Cleanup' })).toBeVisible()
    await expect(page.locator('.saved-search-card .search-string')).toContainText('!traded')
  })

  test('successful copy appears in History and remains after refresh', async ({ page }) => {
    await gotoRoute(page, '/goal/pvp_candidates')
    await page.getByRole('button', { name: /Copy search/i }).click()
    await expect(page.getByRole('status')).toContainText('Copied')
    await gotoRoute(page, '/history')
    await expect(page.getByRole('heading', { name: 'PvP IV Candidates' })).toBeVisible()
    await page.reload()
    await expect(page.getByRole('heading', { name: 'PvP IV Candidates' })).toBeVisible()
  })

  test('shows Clipboard API unavailable instead of failing silently', async ({ page }) => {
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'clipboard', { configurable: true, value: undefined })
    })
    await gotoRoute(page, '/goal/hundo_check')
    await page.getByRole('button', { name: /Copy search/i }).click()
    await expect(page.getByRole('status')).toContainText('unavailable')
  })
})
