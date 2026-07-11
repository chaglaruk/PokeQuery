import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

// Scenario 30: Settings and changelog navigation — full coverage.

test.describe('Settings and Changelog (scenario 30)', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
    await gotoRoute(page, '/settings')
  })

  test('30. Settings shows app language selector with all supported languages', async ({ page }) => {
    const select = page.locator('select').first()
    await expect(select).toBeVisible()

    const optionTexts = await select.locator('option').allTextContents()
    // English display name (always present)
    expect(optionTexts.some(t => t.includes('English'))).toBeTruthy()
    // Other languages — use Unicode escapes to avoid encoding ambiguity
    expect(optionTexts.some(t => t.includes('Deutsch'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('T\u00fcrk\u00e7e'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Fran\u00e7ais'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Espa\u00f1ol'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Italiano'))).toBeTruthy()
    // System Default is translated to selected language's value
    expect(optionTexts.some(t => t.toLowerCase().includes('system') || t.toLowerCase().includes('default'))).toBeTruthy()
  })

  test('30b. Settings shows search string language selector with all options', async ({ page }) => {
    const select = page.locator('select').nth(1)
    await expect(select).toBeVisible()

    // Verify all 8 options exist by their values (more stable than localized labels)
    const values = await select.locator('option').evaluateAll(opts => opts.map(o => o.value))
    expect(values).toContain('Auto')
    expect(values).toContain('Match App Language')
    expect(values).toContain('English')
    expect(values).toContain('German')
    expect(values).toContain('Spanish')
    expect(values).toContain('French')
    expect(values).toContain('Italian')
    expect(values).toContain('Turkish')

    // Verify that labels are localized: 'German' shows as 'Deutsch', 'Spanish' as 'Español', etc.
    const optionTexts = await select.locator('option').allTextContents()
    expect(optionTexts.some(t => t.includes('Auto'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Match App Language'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('English'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Deutsch'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Espa\u00f1ol'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Fran\u00e7ais'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('Italiano'))).toBeTruthy()
    expect(optionTexts.some(t => t.includes('T\u00fcrk\u00e7e'))).toBeTruthy()
  })

  test('30c. Changelog is reachable from Settings and shows version entries', async ({ page }) => {
    const changelogLink = page.locator('.card.card-tap').filter({ hasText: 'What Changed' })
    await expect(changelogLink).toBeVisible()
    await changelogLink.click()
    await expect(page).toHaveURL(/#\/changelog/)
    // Version entries should be visible
    await expect(page.getByText(/v0\.\d+\.\d+/i, { exact: false }).first()).toBeVisible()
  })

  test('30d. Browser Back: home then settings then changelog then back twice', async ({ page }) => {
    // First navigate to Home explicitly (creates initial history entry)
    await gotoRoute(page, '/')
    await expect(page).toHaveURL(/#\/$/)

    // Navigate to Settings via bottom nav (creates history entry)
    await page.locator('.bottom-nav .nav-item').filter({ hasText: 'Settings' }).click()
    await expect(page).toHaveURL(/#\/settings$/)

    // Click changelog link (creates history entry)
    const changelogLink = page.locator('.card.card-tap').filter({ hasText: 'What Changed' })
    await changelogLink.click()
    await expect(page).toHaveURL(/#\/changelog$/)

    // Go back: changelog -> settings
    await page.goBack()
    await expect(page).toHaveURL(/#\/settings/)

    // Go back: settings -> home
    await page.goBack()
    await expect(page).toHaveURL(/#\/$/)
  })
})
