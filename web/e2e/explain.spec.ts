import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

// Scenarios: Explain screen — token breakdown for input queries.

test.describe('Explain String screen', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
  })

  test('entering a query shows token breakdown', async ({ page }) => {
    await gotoRoute(page, '/explain')
    const input = page.locator('input[type="text"]').first()
    await expect(input).toBeVisible()
    await input.fill('shiny&4*')

    // Token breakdown section should appear
    await expect(page.getByText('Token breakdown')).toBeVisible()
    await expect(page.locator('code').first()).toBeVisible()
  })

  test('empty input shows intro text', async ({ page }) => {
    await gotoRoute(page, '/explain')
    await expect(page.locator('input[type="text"]')).toBeVisible()
    // Intro text should be visible when input is empty
    await expect(page.getByText(/paste|type|enter/i).first()).toBeVisible()
  })

  test('complex query with exclusions shows multiple tokens', async ({ page }) => {
    await gotoRoute(page, '/explain')
    await page.locator('input[type="text"]').first().fill('shiny&!legendary&4*&!traded')

    // Multiple code blocks for tokens
    const codes = page.locator('code')
    await expect(codes.first()).toBeVisible()
    const count = await codes.count()
    expect(count).toBeGreaterThanOrEqual(4)  // shiny, !legendary, 4*, !traded
  })

  test('invalid token shows unknown warning', async ({ page }) => {
    await gotoRoute(page, '/explain')
    await page.locator('input[type="text"]').first().fill('xyzzy')

    // Some warning about unknown tokens should appear
    await expect(page.getByText(/unknown/i).first()).toBeVisible({ timeout: 10000 })
  })
})
