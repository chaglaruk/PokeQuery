import { test, expect } from '@playwright/test'
import { gotoRoute, setStorage, skipOnboarding } from './helpers'

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
    await page.getByRole('button', { name: 'Delete' }).click()
    await expect(page.getByRole('heading', { name: 'Safe Cleanup' })).toHaveCount(0)
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

  test('localizes saved goal metadata and preserves legacy fallback names', async ({ page }) => {
    const createdAt = Date.UTC(2026, 6, 14, 12, 34)
    await setStorage(page, 'pq_app_language', 'English')
    await setStorage(page, 'pq_favorites', JSON.stringify([{
      id: 'favorite', name: 'Stale stored title', goalId: 'safe_cleanup',
      rawSyntax: '1*&!shiny&!traded', riskLevel: 'Medium', createdAt,
    }]))
    await setStorage(page, 'pq_history', JSON.stringify([{
      id: 'legacy', name: 'Legacy Cleanup Search', goalId: 'legacy_unknown',
      rawSyntax: 'age0-7', riskLevel: 'Low', createdAt,
    }]))

    await gotoRoute(page, '/favorites')
    await expect(page.getByRole('heading', { name: 'Safe Cleanup' })).toBeVisible()
    await expect(page.getByText('safe_cleanup', { exact: true })).toHaveCount(0)
    const englishDate = await page.evaluate(timestamp => new Intl.DateTimeFormat('en', { dateStyle: 'medium', timeStyle: 'short' }).format(timestamp), createdAt)
    await expect(page.getByText(englishDate)).toBeVisible()

    await gotoRoute(page, '/settings')
    await page.locator('select').first().selectOption('T\u00fcrk\u00e7e')
    await gotoRoute(page, '/favorites')
    await expect(page.getByRole('heading', { name: 'G\u00fcvenli Temizlik' })).toBeVisible()
    const turkishDate = await page.evaluate(timestamp => new Intl.DateTimeFormat('tr', { dateStyle: 'medium', timeStyle: 'short' }).format(timestamp), createdAt)
    await expect(page.getByText(turkishDate)).toBeVisible()

    await gotoRoute(page, '/history')
    await expect(page.getByRole('heading', { name: 'Legacy Cleanup Search' })).toBeVisible()
    await expect(page.getByText('legacy_unknown', { exact: true })).toHaveCount(0)
  })
})
