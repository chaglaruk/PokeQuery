const noop = () => {}

export function useRegisterSW() {
  const needRefresh = localStorage.getItem('pq_screenshot_need_refresh') === 'true'
  return {
    needRefresh: [needRefresh, noop] as const,
    offlineReady: [false, noop] as const,
    updateServiceWorker: async () => {},
  }
}
