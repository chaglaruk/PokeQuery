import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

test.describe('Events and Explain screens', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
  })

  test('15. Events screen loads and shows event cards', async ({ page }) => {
    await gotoRoute(page, '/events')
    // The events feed should eventually load or fall back; cards should appear
    await page.waitForTimeout(3000)
    const cards = page.locator('.card.card-tap')
    await expect(cards.first()).toBeVisible({ timeout: 15000 })
  })

  test('16. Events screen: tapping a card expands it', async ({ page }) => {
    await gotoRoute(page, '/events')
    await page.waitForTimeout(3000)
    const firstCard = page.locator('.card.card-tap').first()
    await expect(firstCard).toBeVisible({ timeout: 15000 })
    await firstCard.click()
    await expect(page.getByText('Copy', { exact: false }).first()).toBeVisible({ timeout: 5000 })
  })

  test('17. Explain screen: entering a query shows token breakdown', async ({ page }) => {
    await gotoRoute(page, '/explain')
    await page.locator('input[type="text"]').first().fill('shiny&4*')
    await expect(page.getByText('Token', { exact: false })).toBeVisible()
    await expect(page.locator('code').first()).toBeVisible()
  })

  test('18. Explain screen: empty input shows intro text', async ({ page }) => {
    await gotoRoute(page, '/explain')
    await expect(page.locator('input[type="text"]')).toBeVisible()
    await expect(page.getByText(/paste|type|enter/i).first()).toBeVisible()
  })
})
