import { defineConfig, devices } from '@playwright/test'

const PREVIEW_PORT = 4173

// When VITE_BASE=/PokeQuery/ the preview server serves at /PokeQuery/.
// Playwright baseURL must point there so page.goto('/') resolves correctly.
const basePath = process.env.VITE_BASE || '/'
const baseURL = basePath !== '/'
  ? `http://localhost:${PREVIEW_PORT}${basePath}`
  : `http://localhost:${PREVIEW_PORT}`

export default defineConfig({
  testDir: './e2e',
  testIgnore: process.env.SCREENSHOTS ? [] : ['screenshots.spec.ts'],
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [
    ['list'],
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
  ],
  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium-mobile', use: { ...devices['Pixel 5'] } },
    { name: 'webkit-iphone-se', use: { ...devices['iPhone SE'] } },
    { name: 'webkit-iphone-13', use: { ...devices['iPhone 13'] } },
    { name: 'webkit-iphone-pro-max', use: { ...devices['iPhone 14 Pro Max'] } },
    { name: 'desktop', use: { viewport: { width: 1280, height: 720 } } },
  ],
  webServer: {
    command: process.env.CI
      ? 'npm run build && npm run preview'
      : 'npm run preview',
    url: baseURL,
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
})
