import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:4173',
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
      ? 'npm run build && npm run preview -- --port 4173'
      : 'npm run preview -- --port 4173',
    url: 'http://localhost:4173',
    reuseExistingServer: !process.env.CI,
    timeout: 60000,
  },
})
