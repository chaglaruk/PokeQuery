import { test, expect } from '@playwright/test'
import { gotoRoute } from './helpers'

test.describe('Web entry flow without onboarding', () => {
  test('first load opens Home directly', async ({ page }) => {
    await page.goto('')
    await expect(page).toHaveURL(/#\/$/)
    await expect(page.getByText('Safe Cleanup').first()).toBeVisible()
    await expect(page.getByText('Skip', { exact: true })).toHaveCount(0)
  })

  test('legacy onboarding route redirects to Home', async ({ page }) => {
    await gotoRoute(page, '/onboarding')
    await expect(page).toHaveURL(/#\/$/)
    await expect(page.getByText('Safe Cleanup').first()).toBeVisible()
  })

  test('reload stays on Home without an onboarding flag', async ({ page }) => {
    await page.goto('')
    await page.evaluate(() => localStorage.removeItem('pq_onboarding_complete'))
    await page.reload()
    await expect(page).toHaveURL(/#\/$/)
    await expect(page.getByText('Safe Cleanup').first()).toBeVisible()
  })
})
