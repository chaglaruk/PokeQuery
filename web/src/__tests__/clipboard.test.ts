import { afterEach, describe, expect, it, vi } from 'vitest'
import { copyToClipboard } from '@ui/clipboard'

describe('clipboard result states', () => {
  afterEach(() => vi.restoreAllMocks())

  it('reports copied', async () => {
    const writeText = vi.fn().mockResolvedValue(undefined)
    Object.defineProperty(navigator, 'clipboard', { configurable: true, value: { writeText } })
    await expect(copyToClipboard('4*')).resolves.toMatchObject({ status: 'copied', i18nKey: 'clipboard_copied' })
  })

  it('distinguishes denied and generic failures', async () => {
    Object.defineProperty(navigator, 'clipboard', { configurable: true, value: { writeText: vi.fn().mockRejectedValue(new DOMException('denied', 'NotAllowedError')) } })
    await expect(copyToClipboard('4*')).resolves.toMatchObject({ status: 'denied' })
    Object.defineProperty(navigator, 'clipboard', { configurable: true, value: { writeText: vi.fn().mockRejectedValue(new Error('boom')) } })
    await expect(copyToClipboard('4*')).resolves.toMatchObject({ status: 'failed' })
  })

  it('reports an unavailable API', async () => {
    Object.defineProperty(navigator, 'clipboard', { configurable: true, value: undefined })
    await expect(copyToClipboard('4*')).resolves.toMatchObject({ status: 'unavailable' })
  })
})
