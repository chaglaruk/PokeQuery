import type { Page } from '@playwright/test'

// E2E test helpers shared across spec files.
//
// Navigation uses relative URLs so that the same tests work at both
// baseURL = http://localhost:4173/  (dev, base = '/')
// baseURL = http://localhost:4173/PokeQuery/  (production, base = '/PokeQuery/')
//
// HashRouter makes routes live in the URL fragment, so we append `#route`
// to the base URL rather than replacing the path with `/route`.

// Clears all localStorage keys so the app re-enters first-run onboarding.
export async function clearStorage(page: Page): Promise<void> {
  await page.addInitScript(() => { try { window.localStorage.clear() } catch { /* private mode */ } })
}

// Pre-sets the onboarding-complete flag so the app starts directly at Home,
// skipping the onboarding flow. Call before page.goto().
export async function skipOnboarding(page: Page): Promise<void> {
  await page.addInitScript(() => {
    try {
      window.localStorage.setItem('pq_onboarding_complete', 'true')
    } catch { /* private mode */ }
  })
}

// Navigates to a hash route relative to baseURL.
// gotoRoute(page, '/')          => baseURL#/  (Home)
// gotoRoute(page, '/events')    => baseURL#/events
// gotoRoute(page, '/goal/safe_cleanup') => baseURL#/goal/safe_cleanup
//
// Using '#/' for the root ensures HashRouter's URL always has the hash prefix
// so that toHaveURL(/#\/$/) assertions match consistently.
export async function gotoRoute(page: Page, route: string = '/'): Promise<void> {
  await page.goto(`#${route}`)
}

// Waits for the app shell to finish initialising: the root element has
// content and no spinner overlay is present.
export async function waitForAppReady(page: Page): Promise<void> {
  await page.waitForSelector('#root:has(*)', { timeout: 15000 })
}

// Waits until event cards appear on the Events screen (loading finished).
export async function waitForEventCards(page: Page): Promise<void> {
  await page.locator('.card.card-tap').first().waitFor({ state: 'visible', timeout: 20000 })
}

// Installs a localStorage entry before the page loads.
export async function setStorage(page: Page, key: string, value: string): Promise<void> {
  await page.addInitScript(([k, v]) => { try { window.localStorage.setItem(k, v) } catch { /* private mode */ } }, [key, value])
}
