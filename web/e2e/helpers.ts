import type { Page } from '@playwright/test'

// E2E test helpers shared across spec files.

// Clears all localStorage keys so the app re-enters first-run onboarding.
export async function clearStorage(page: Page): Promise<void> {
  await page.addInitScript(() => localStorage.clear())
}

// Pre-sets the onboarding-complete flag so the app starts directly at Home,
// skipping the onboarding flow. Call before page.goto().
export async function skipOnboarding(page: Page): Promise<void> {
  await page.addInitScript(() => localStorage.setItem('pq_onboarding_complete', 'true'))
}

// Navigates to a hash route (HashRouter) starting from the configured baseURL.
export async function gotoRoute(page: Page, route: string = '/'): Promise<void> {
  await page.goto(`/#${route}`)
}
