import { defineConfig } from '@playwright/test'
import baseConfig from './playwright.config'

const baseURL = 'http://localhost:4173'

export default defineConfig({
  testDir: './e2e',
  testMatch: 'screenshots.spec.ts',
  fullyParallel: false,
  workers: 1,
  reporter: [['list']],
  use: { ...baseConfig.use, baseURL },
  projects: baseConfig.projects,
  webServer: {
    command: 'npm run build -- --mode screenshots && npm run preview',
    url: baseURL,
    reuseExistingServer: false,
    timeout: 120000,
  },
})
