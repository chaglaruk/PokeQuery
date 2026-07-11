import { test, expect } from '@playwright/test'
import { gotoRoute } from './helpers'

test.describe('Onboarding flow', () => {
  // Each Playwright test gets a fresh browser context with empty localStorage,
  // so onboarding will always show on first visit without explicit clearing.

  test('1. first visit redirects to onboarding', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveURL(/#\/onboarding/)
    await expect(page.getByRole('heading', { name: 'PokeQuery' })).toBeVisible()
  })

  test('2. Next button advances to second page', async ({ page }) => {
    await gotoRoute(page, '/onboarding')
    await expect(page.getByText('Next', { exact: false })).toBeVisible()
    await page.getByText('Next', { exact: false }).click()
    await expect(page.getByText('Start building', { exact: false })).toBeVisible()
  })

  test('3. Start building completes onboarding and shows Home', async ({ page }) => {
    await gotoRoute(page, '/onboarding')
    // Navigate to page 2
    await page.getByText('Next', { exact: false }).click()
    await page.getByText('Start building', { exact: false }).click()
    await expect(page).toHaveURL(/#\/$/)
  })

  test('4. Skip button completes onboarding and shows Home', async ({ page }) => {
    await gotoRoute(page, '/onboarding')
    await page.getByText('Skip', { exact: true }).click()
    await expect(page).toHaveURL(/#\/$/)
  })

  test('5. second visit skips onboarding and goes to Home', async ({ page }) => {
    // First visit: complete onboarding
    await page.goto('/')
    await page.getByText('Skip', { exact: true }).click()
    await expect(page).toHaveURL(/#\/$/)
    // Reload — should stay on Home since onboarding is persisted
    await page.reload()
    await expect(page).toHaveURL(/#\/$/)
    await expect(page.getByRole('heading', { name: 'PokeQuery' })).toBeVisible()
  })
})
