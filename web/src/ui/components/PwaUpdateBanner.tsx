import { useEffect } from 'react'
import { useRegisterSW } from 'virtual:pwa-register/react'
import { useI18n } from '@i18n/I18nContext'

/**
 * Lightweight PWA lifecycle banner. Prompt-style SW registration means updates never
 * silently replace the current session; the user explicitly chooses when to refresh.
 * Banner is pinned above the bottom navigation so it cannot cover page headers/back buttons.
 */
export function PwaUpdateBanner() {
  const { t } = useI18n()
  const {
    needRefresh: [needRefresh, setNeedRefresh],
    offlineReady: [offlineReady, setOfflineReady],
    updateServiceWorker,
  } = useRegisterSW({
    onRegisteredSW(_swUrl, registration) {
      if (registration) {
        window.setInterval(() => registration.update(), 60 * 60 * 1000)
      }
    },
  })

  useEffect(() => {
    if (!offlineReady) return
    const id = window.setTimeout(() => setOfflineReady(false), 4000)
    return () => window.clearTimeout(id)
  }, [offlineReady, setOfflineReady])

  const safeBottomStyle = { top: 'auto', bottom: 'calc(82px + var(--safe-bottom))' } as const

  if (needRefresh) {
    return (
      <div className="pwa-update-banner" role="status" aria-live="polite" style={safeBottomStyle}>
        <span>{t('pwa_update_available')}</span>
        <div className="pwa-update-actions">
          <button type="button" onClick={() => updateServiceWorker(true)}>{t('pwa_update_now')}</button>
          <button type="button" className="secondary" onClick={() => setNeedRefresh(false)}>{t('pwa_update_later')}</button>
        </div>
      </div>
    )
  }

  if (offlineReady) {
    return (
      <div className="pwa-update-banner offline-ready" role="status" aria-live="polite" style={safeBottomStyle}>
        <span>{t('pwa_offline_ready')}</span>
        <button type="button" className="secondary" onClick={() => setOfflineReady(false)}>{t('pwa_dismiss')}</button>
      </div>
    )
  }

  return null
}
