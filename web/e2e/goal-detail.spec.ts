import { test, expect } from '@playwright/test'
import { skipOnboarding, gotoRoute } from './helpers'

// Scenarios 8-14: Goal selection, exact search text, forbidden |, risk gate, clipboard, sharing.

test.describe('Goal selection and search text (scenarios 8-14)', () => {
  test.beforeEach(async ({ page }) => {
    await skipOnboarding(page)
  })

  test('8. tapping a goal card navigates to goal detail', async ({ page }) => {
    await gotoRoute(page, '/')
    await page.locator('.page').getByText('Safe Cleanup').first().click()
    await expect(page).toHaveURL(/#\/goal\/safe_cleanup/)
  })

  test('9. Safe Cleanup shows exact generated search text', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()
    const text = await page.locator('.search-string').textContent()
    expect(text).toContain('1*')
    expect(text).toContain('!shiny')
    expect(text).toContain('!legendary')
    expect(text).toContain('!mythical')
    expect(text).toContain('!ultrabeast')
    expect(text).toContain('!costume')
    expect(text).toContain('!shadow')
    expect(text).toContain('!favorite')
    expect(text).toContain('!lucky')
    expect(text).toContain('!traded')
    expect(text).toContain('!4*')
    expect(text).toBe('1*&!shiny&!legendary&!mythical&!ultrabeast&!costume&!background&!locationbackground&!specialbackground&!shadow&!purified&!favorite&!lucky&!#&!traded&!4*')
  })

  test('9b. Trade Fodder shows exact generated search text', async ({ page }) => {
    await gotoRoute(page, '/goal/trade_fodder')
    await expect(page.locator('.search-string')).toBeVisible()
    const text = await page.locator('.search-string').textContent()
    expect(text).toContain('count2-')
    expect(text).toContain('!traded')
    expect(text).toContain('!shiny')
    expect(text).toContain('!4*')
  })

  test('10. forbidden | in expert builder disables copy and shows error', async ({ page }) => {
    await gotoRoute(page, '/goal/expert')
    const input = page.locator('input[type="text"]').first()
    await input.fill('shiny|4*')
    await expect(input).toHaveValue('shiny|4*')
    await expect(page.locator('.btn-copy')).toBeDisabled()
    await expect(page.getByText('Fix errors to copy').first()).toBeVisible()
  })

  test('10b. valid expert builder input enables copy', async ({ page }) => {
    await gotoRoute(page, '/goal/expert')
    const input = page.locator('input[type="text"]').first()
    await input.fill('shiny&4*')
    await expect(page.locator('.btn-copy')).toBeEnabled()
  })

  test('11. !traded appears exactly once in Safe Cleanup search', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()
    const text = await page.locator('.search-string').textContent() ?? ''
    const count = (text.match(/!traded/g) ?? []).length
    expect(count).toBe(1)
    const tradedPositive = (text.match(/(?<!!)traded/g) ?? []).length
    expect(tradedPositive).toBe(0)
  })

  test('12. medium-risk copy requires review and writes exact search string', async ({ page, browserName }) => {
    test.skip(browserName === 'webkit', 'Clipboard write requires secure context or user gesture in WebKit; tested in chromium')

    await page.context().grantPermissions(['clipboard-read', 'clipboard-write'])
    await page.addInitScript(() => {
      const items = new Map<string, string>()
      const clipboard = {
        writeText: (text: string) => Promise.resolve().then(() => { items.set('text', text) }),
        readText: () => Promise.resolve(items.get('text') ?? ''),
      }
      Object.defineProperty(navigator, 'clipboard', { value: clipboard, configurable: true })
    })

    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()
    const expectedText = await page.locator('.search-string').textContent()

    await page.getByRole('button', { name: /Copy/i }).click()
    await expect(page.getByRole('dialog', { name: 'Check this search first' })).toBeVisible()
    await page.getByRole('button', { name: 'Review & copy' }).click()

    await expect(page.getByText(/Copied/i).first()).toBeVisible({ timeout: 5000 })
    const clipboardText = await page.evaluate(() => navigator.clipboard.readText())
    expect(clipboardText).toBe(expectedText)
  })

  test('13. clipboard denied/failure after review does not crash the app', async ({ page }) => {
    await gotoRoute(page, '/goal/safe_cleanup')
    await expect(page.locator('.search-string')).toBeVisible()

    await page.evaluate(() => {
      const writeText = () => Promise.reject(new DOMException('Permission denied', 'NotAllowedError'))
      Object.defineProperty(navigator, 'clipboard', { value: { writeText, readText: () => Promise.resolve('') }, configurable: true })
    })

    await page.getByRole('button', { name: /Copy/i }).click()
    await expect(page.getByRole('dialog', { name: 'Check this search first' })).toBeVisible()
    await page.getByRole('button', { name: 'Review & copy' }).click()

    await expect(page.locator('.search-string')).toBeVisible()
    await page.locator('.back-btn').click()
    await expect(page).toHaveURL(/#\/$/)
  })

  test('14. share search preserves the generated query and PokeQuery explain link', async ({ page }) => {
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'share', { value: undefined, configurable: true })
      let sharedText = ''
      Object.defineProperty(navigator, 'clipboard', {
        value: {
          writeText: (text: string) => { sharedText = text; return Promise.resolve() },
          readText: () => Promise.resolve(sharedText),
        },
        configurable: true,
      })
    })

    await gotoRoute(page, '/goal/safe_cleanup')
    const expectedText = await page.locator('.search-string').textContent() ?? ''
    await page.getByRole('button', { name: 'Share search' }).click()
    await expect(page.getByRole('dialog', { name: 'Check this search first' })).toBeVisible()
    await page.getByRole('button', { name: 'Review & share' }).click()

    await expect(page.getByText('Share text copied')).toBeVisible()
    const shared = await page.evaluate(() => navigator.clipboard.readText())
    expect(shared).toContain(expectedText)
    expect(shared).toContain('Built with PokeQuery')

    const sharedUrl = shared.split('\n').at(-1) ?? ''
    const parsed = new URL(sharedUrl)
    expect(parsed.hash).toMatch(/^#\/explain\?query=/)
    const queryParams = new URLSearchParams(parsed.hash.split('?')[1] ?? '')
    expect(queryParams.get('query')).toBe(expectedText)
  })
})
