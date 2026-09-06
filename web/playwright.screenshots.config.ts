import { defineConfig } from '@playwright/test'
import baseConfig from './playwright.config'

const baseURL = 'http://localhost:4173'

export default defineConfig({
  testDir: './e2e',
  testMatch: 'screenshots.spec.ts',
  fullyParallel: false,
  workers: 1,
  reporter: [['list']],
  // Screenshot mode already mocks the PWA registration hook. Blocking service
  // workers here keeps the deterministic Event Guide route fixture visible to
  // Playwright instead of allowing a previously registered worker to bypass it.
  use: { ...baseConfig.use, baseURL, serviceWorkers: 'block' },
  projects: baseConfig.projects,
  webServer: {
    command: 'npm run build -- --mode screenshots && npm run preview',
    url: baseURL,
    reuseExistingServer: false,
    timeout: 120000,
  },
})
