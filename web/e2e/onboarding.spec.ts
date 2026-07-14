import { test, expect } from '@playwright/test'
import { gotoRoute } from './helpers'

// Scenarios 1-5: Initial load, first-run onboarding, persistence, reopen.
// Each Playwright test starts with a fresh context (empty localStorage).

test.describe('Onboarding flow (scenarios 2-5)', () => {
  test('2. initial first load redirects to onboarding', async ({ page }) => {
    await page.goto('')
    await expect(page).toHaveURL(/#\/onboarding/)
    await expect(page.getByRole('heading', { name: 'PokeQuery' })).toBeVisible()
    // The three feature cards should be visible on page 1
    await expect(page.getByText('Build useful searches')).toBeVisible()
    await expect(page.getByText('Slow down on risk')).toBeVisible()
    await expect(page.getByText('Copy text only')).toBeVisible()
  })

  test('3. first-run onboarding shows two pages with Skip and Next', async ({ page }) => {
    await gotoRoute(page, '/onboarding')
    // Page 1 indicators
    await expect(page.getByRole('heading', { name: 'PokeQuery' })).toBeVisible()
    // Skip button present
    await expect(page.getByText('Skip', { exact: true })).toBeVisible()
    // Next button present
    await expect(page.getByText('Next', { exact: false })).toBeVisible()
  })

  test('3b. both onboarding pages finish their entrance animation', async ({ page }) => {
    await gotoRoute(page, '/onboarding')
    const panel = page.locator('.onboarding-page-enter')
    await expect(panel).toHaveCSS('opacity', '1')
    await expect(panel).toHaveCSS('transform', 'none')
    await page.getByText('Next', { exact: false }).click()
    await expect(panel).toHaveCSS('opacity', '1')
    await expect(panel).toHaveCSS('transform', 'none')
  })

  test('4. onboarding completion persists across reload (Start building)', async ({ page }) => {
    await gotoRoute(page, '/onboarding')
    // Advance to page 2
    await page.getByText('Next', { exact: false }).click()
    await expect(page.getByText('Review first, act second')).toBeVisible()
    // Complete onboarding
    await page.getByText('Start building', { exact: false }).click()
    await expect(page).toHaveURL(/#\/$/)
    // Reload — should NOT redirect to onboarding
    await page.reload()
    await expect(page).toHaveURL(/#\/$/)
    await expect(page.getByRole('heading', { name: 'PokeQuery' })).toBeVisible()
  })

  test('4b. onboarding completion persists across reload (Skip)', async ({ page }) => {
    await page.goto('')
    await expect(page).toHaveURL(/#\/onboarding/)
    await page.getByText('Skip', { exact: true }).click()
    await expect(page).toHaveURL(/#\/$/)
    // Reload persists
    await page.reload()
    await expect(page).toHaveURL(/#\/$/)
  })

  test('4c. onboarding flag written to localStorage', async ({ page }) => {
    await gotoRoute(page, '/onboarding')
    await page.getByText('Skip', { exact: true }).click()
    await expect(page).toHaveURL(/#\/$/)
    const flag = await page.evaluate(() => localStorage.getItem('pq_onboarding_complete'))
    expect(flag).toBe('true')
  })

  test('5. reopening onboarding is not accessible after completion without route', async ({ page }) => {
    // Complete onboarding
    await page.goto('')
    await page.getByText('Skip', { exact: true }).click()
    await expect(page).toHaveURL(/#\/$/)
    // Navigating directly to /onboarding is possible but doesn't re-trigger redirect
    await gotoRoute(page, '/onboarding')
    await expect(page).toHaveURL(/#\/onboarding$/)
    // Going back to home should stay (no forced redirect)
    await gotoRoute(page, '/')
    await expect(page).toHaveURL(/#\/$/)
  })
})
