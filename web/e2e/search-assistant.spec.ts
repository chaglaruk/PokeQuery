import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

test.describe('Search Assistant copy safety', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
    await page.addInitScript(() => {
      let copiedText = ''
      Object.defineProperty(navigator, 'clipboard', {
        value: {
          writeText: (text: string) => { copiedText = text; return Promise.resolve() },
          readText: () => Promise.resolve(copiedText),
        },
        configurable: true,
      })
    })
  })

  test('medium-risk assistant output is not copied until review is confirmed', async ({ page }) => {
    await gotoRoute(page, '/assistant')
    const input = page.getByRole('textbox', { name: 'Search Assistant' })
    await input.fill('shiny')
    await input.press('Enter')

    const generated = await page.locator('.search-string').textContent()
    expect(generated).toBe('shiny')

    await page.getByTestId('assistant-copy-button').click()
    const reviewDialog = page.getByRole('dialog', { name: 'Check this search first' })
    await expect(reviewDialog).toBeVisible()

    const beforeConfirm = await page.evaluate(() => navigator.clipboard.readText())
    expect(beforeConfirm).toBe('')

    await reviewDialog.evaluate(async element => {
      await Promise.all(element.getAnimations({ subtree: true }).map(animation => animation.finished))
    })
    const reviewCopy = page.getByTestId('assistant-review-copy-button')
    const box = await reviewCopy.boundingBox()
    expect(box?.height ?? 0).toBeGreaterThanOrEqual(48)
    await reviewCopy.click()

    await expect(page.getByText(/Copied/i).first()).toBeVisible()
    const afterConfirm = await page.evaluate(() => navigator.clipboard.readText())
    expect(afterConfirm).toBe(generated)
  })

  test('low-risk assistant output copies directly without review dialog', async ({ page }) => {
    await gotoRoute(page, '/assistant')
    const input = page.getByRole('textbox', { name: 'Search Assistant' })
    await input.fill('hundo')
    await input.press('Enter')

    const generated = await page.locator('.search-string').textContent()
    expect(generated).toBe('4*')

    await page.getByTestId('assistant-copy-button').click()
    await expect(page.getByRole('dialog')).toHaveCount(0)
    const copied = await page.evaluate(() => navigator.clipboard.readText())
    expect(copied).toBe(generated)
  })
})
