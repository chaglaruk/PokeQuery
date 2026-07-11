import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@engine': path.resolve(__dirname, './src/engine'),
      '@i18n': path.resolve(__dirname, './src/i18n'),
      '@event': path.resolve(__dirname, './src/event'),
      '@ui': path.resolve(__dirname, './src/ui'),
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test-setup.ts'],
    include: ['src/**/*.test.ts'],
    exclude: ['node_modules', 'dist', 'e2e'],
  },
})
