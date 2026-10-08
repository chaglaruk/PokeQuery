import { test, expect } from '@playwright/test'
import { skipOnboarding } from './helpers'

const guides = ['index.html', 'pokemon-go-search-string-generator.html', 'pokemon-go-storage-cleanup-search.html', 'pokemon-go-transfer-candy-search.html', 'pokemon-go-trade-search.html']

for (const name of guides) {
  test(`static guide is readable and links to the app: ${name}`, async ({ page }, testInfo) => {
    await skipOnboarding(page)
    await page.setViewportSize({ width: 320, height: 780 })
    const response = await page.goto(`guides/${name}`)
    expect(response?.status()).toBe(200)
    await expect(page.locator('main h1')).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
    for (const selector of ['.brand', '.secondary', '.cta']) {
      for (const link of await page.locator(selector).all()) {
        expect((await link.boundingBox())!.height).toBeGreaterThanOrEqual(48)
      }
    }
    await page.locator('.brand').focus()
    expect(await page.locator('.brand').evaluate(el => getComputedStyle(el).outlineStyle)).toBe('solid')
    await page.screenshot({ path: testInfo.outputPath(`guide-${name}.png`), fullPage: true })
    await page.locator('.cta').first().click()
    await expect(page.locator('#root')).not.toBeEmpty()
    expect(new URL(page.url()).pathname).toBe(process.env.VITE_BASE || '/')
    const routes: Record<string, string> = {
      'index.html': '#/', 'pokemon-go-search-string-generator.html': '#/',
      'pokemon-go-storage-cleanup-search.html': '#/goal/safe_cleanup',
      'pokemon-go-transfer-candy-search.html': '#/goal/candy_prep',
      'pokemon-go-trade-search.html': '#/goal/trade_fodder',
    }
    expect(new URL(page.url()).hash).toBe(routes[name])
  })
}

test('visited static guide survives an offline reload after service worker activation', async ({ page, context, browserName }) => {
  test.skip(browserName === 'webkit', 'Service worker control is unavailable in preview WebKit')
  await skipOnboarding(page)
  await page.goto('')
  await page.evaluate(async () => { await navigator.serviceWorker.ready })
  await page.goto('guides/pokemon-go-storage-cleanup-search.html')
  const title = await page.locator('main h1').innerText()
  await context.setOffline(true)
  try {
    await page.reload()
    await expect(page.locator('main h1')).toHaveText(title)
    expect(await page.locator('link[rel="stylesheet"]').evaluate(el => !!(el as HTMLLinkElement).sheet)).toBe(true)
  } finally { await context.setOffline(false) }
})
