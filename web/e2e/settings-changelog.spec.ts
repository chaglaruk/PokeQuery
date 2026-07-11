import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

test.describe('Settings and Changelog screens', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
    await gotoRoute(page, '/settings')
  })

  test('19. Settings shows app language selector with all supported languages', async ({ page }) => {
    const select = page.locator('select').first()
    await expect(select).toBeVisible()

    // App language options
    const options = select.locator('option')
    const optionTexts = await options.allTextContents()
    expect(optionTexts.some(t => t.includes('English'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Deutsch'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Türkçe'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Français'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Español'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Italiano'))).toBeTruthy()
  })

  test('20. Changelog is reachable from Settings and shows version entries', async ({ page }) => {
    // The tappable card in Settings uses the settings_changelog_label text.
    const changelogLink = page.locator('.card.card-tap').filter({ hasText: 'What Changed' })
    await expect(changelogLink).toBeVisible()
    await changelogLink.click()
    await expect(page).toHaveURL(/#\/changelog/)
    await expect(page.getByText(/v0\.\d+\.\d+/, { exact: false }).first()).toBeVisible()
  })
})
