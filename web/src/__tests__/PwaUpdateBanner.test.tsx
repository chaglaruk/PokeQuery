import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, cleanup, act } from '@testing-library/react'
import { PwaUpdateBanner } from '../ui/components/PwaUpdateBanner'

// Mock the virtual:pwa-register/react module. We mutate the mock per test
// to simulate each lifecycle state (hidden, update-needed, offline-ready).
const mockUseRegisterSW = vi.fn()

// Mock I18nProvider useI18n hook — return predictable strings so tests assert
// behavior, key references and lifecycle wiring, not locale data integrity.
vi.mock('@i18n/I18nContext', () => ({
  useI18n: () => ({
    t: (key: string, ..._args: (string | number)[]) => key,
    locale: 'en' as const,
  }),
}))

vi.mock('virtual:pwa-register/react', () => ({
  useRegisterSW: (...args: unknown[]) => mockUseRegisterSW(...(args as unknown[])),
}))

// Helper to build the SW hook return shape for each test scenario.
function mockReturn(opts: {
  needRefresh?: boolean
  offlineReady?: boolean
  updateServiceWorker?: () => Promise<void>
}) {
  const setNeedRefresh = vi.fn()
  const setOfflineReady = vi.fn()
  return {
    needRefresh: [opts.needRefresh ?? false, setNeedRefresh],
    offlineReady: [opts.offlineReady ?? false, setOfflineReady],
    updateServiceWorker: opts.updateServiceWorker ?? (async () => {}),
  }
}

describe('PwaUpdateBanner', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    mockUseRegisterSW.mockReset()
  })
  afterEach(() => {
    vi.useRealTimers()
    cleanup()
  })

  it('renders nothing when no update and not offline-ready', () => {
    mockUseRegisterSW.mockReturnValue(mockReturn({}))
    const { container } = render(<PwaUpdateBanner />)
    expect(container.firstChild).toBeNull()
  })

  it('renders the update prompt with Reload and Later when needRefresh=true', () => {
    mockUseRegisterSW.mockReturnValue(mockReturn({ needRefresh: true }))
    render(<PwaUpdateBanner />)

    expect(screen.getByTestId('pwa-update-banner')).toBeInTheDocument()
    expect(screen.getByText('pwa_update_title')).toBeInTheDocument()
    expect(screen.getByText('pwa_update_desc')).toBeInTheDocument()
    expect(screen.getByText('pwa_update_reload')).toBeInTheDocument()
    expect(screen.getByText('pwa_update_dismiss')).toBeInTheDocument()
  })

  it('calls updateServiceWorker(true) when Reload button clicked', async () => {
    const updateServiceWorker = vi.fn().mockResolvedValue(undefined)
    mockUseRegisterSW.mockReturnValue(mockReturn({ needRefresh: true, updateServiceWorker }))
    render(<PwaUpdateBanner />)

    const reloadBtn = screen.getByText('pwa_update_reload')
    await act(async () => { fireEvent.click(reloadBtn) })
    expect(updateServiceWorker).toHaveBeenCalledWith(true)
    expect(updateServiceWatcherTimes(updateServiceWorker)).toBe(1)
  })

  it('hides the banner when "Later" is clicked', () => {
    const setNeedRefresh = vi.fn()
    const setOfflineReady = vi.fn()
    mockUseRegisterSW.mockReturnValue({
      needRefresh: [true, setNeedRefresh],
      offlineReady: [false, setOfflineReady],
      updateServiceWorker: async () => {},
    })
    const { container } = render(<PwaUpdateBanner />)

    const laterBtn = screen.getByText('pwa_update_dismiss')
    fireEvent.click(laterBtn)

    expect(setNeedRefresh).toHaveBeenCalledWith(false)
    expect(setOfflineReady).toHaveBeenCalledWith(false)
    // container is now empty because dismissed internal state flips true
    expect(container.firstChild).toBeNull()
  })

  it('renders the offline-ready banner with a dismiss button', () => {
    mockUseRegisterSW.mockReturnValue(mockReturn({ offlineReady: true }))
    render(<PwaUpdateBanner />)

    expect(screen.getByTestId('pwa-offline-banner')).toBeInTheDocument()
    expect(screen.getByText('pwa_offline_ready_title')).toBeInTheDocument()
    expect(screen.getByText('pwa_offline_ready_desc')).toBeInTheDocument()
    expect(screen.getByText('pwa_offline_ready_dismiss')).toBeInTheDocument()
  })

  it('auto-dismisses the offline-ready banner after 4 seconds', () => {
    const setOfflineReady = vi.fn()
    mockUseRegisterSW.mockReturnValue({
      needRefresh: [false, vi.fn()],
      offlineReady: [true, setOfflineReady],
      updateServiceWorker: async () => {},
    })
    render(<PwaUpdateBanner />)

    // Before 4 seconds — banner still visible
    expect(screen.getByTestId('pwa-offline-banner')).toBeInTheDocument()
    act(() => { vi.advanceTimersByTime(3999) })
    expect(setOfflineReady).not.toHaveBeenCalled()

    // At 4 seconds — auto-dismiss timer fires
    act(() => { vi.advanceTimersByTime(1) })
    expect(setOfflineReady).toHaveBeenCalledWith(false)
  })

  it('does not auto-dismiss the needRefresh banner after 4 seconds', () => {
    const setNeedRefresh = vi.fn()
    const setOfflineReady = vi.fn()
    mockUseRegisterSW.mockReturnValue({
      needRefresh: [true, setNeedRefresh],
      offlineReady: [false, setOfflineReady],
      updateServiceWorker: async () => {},
    })
    render(<PwaUpdateBanner />)

    act(() => { vi.advanceTimersByTime(5000) })
    expect(setNeedRefresh).not.toHaveBeenCalledWith(false)
    expect(setOfflineReady).not.toHaveBeenCalledWith(false)
  })
})

function updateServiceWatcherTimes(_fn: ReturnType<typeof vi.fn>): number {
  // Just a no-op alias to keep the test body readable; vitest mock.calls.length is the truth.
  return (_fn as unknown as { mock: { calls: unknown[] } }).mock.calls.length
}
