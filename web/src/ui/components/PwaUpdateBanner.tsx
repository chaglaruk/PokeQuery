// Real PWA update banner using the official vite-plugin-pwa React hook.
// Shows a localized banner when (a) a new SW version is ready → reload/cancel,
// and (b) the first successful offline cache is ready → dismiss toast.

import { useState, useEffect } from 'react'
import { useRegisterSW } from 'virtual:pwa-register/react'
import { useI18n } from '@i18n/I18nContext'
import { AppIcon } from './SpriteIcon'

export function PwaUpdateBanner() {
  const { t } = useI18n()
  const {
    needRefresh: [needRefresh, setNeedRefresh],
    offlineReady: [offlineReady, setOfflineReady],
    updateServiceWorker,
  } = useRegisterSW()

  const [dismissed, setDismissed] = useState(false)

  // Auto-dismiss the offline-ready toast after 4 seconds.
  useEffect(() => {
    if (!offlineReady) return
    const timer = setTimeout(() => setOfflineReady(false), 4000)
    return () => clearTimeout(timer)
  }, [offlineReady, setOfflineReady])

  if (dismissed) return null
  if (!needRefresh && !offlineReady) return null

  const handleReload = async () => {
    await updateServiceWorker(true)
  }

  const handleClose = () => {
    setDismissed(true)
    setNeedRefresh(false)
    setOfflineReady(false)
  }

  if (needRefresh) {
    return (
      <div className="pwa-banner pwa-banner-update" role="alert" aria-live="assertive" data-testid="pwa-update-banner">
        <div className="pwa-banner-body">
          <div className="pwa-banner-icon"><AppIcon name="refresh" size={20} /></div>
          <div className="pwa-banner-text">
            <p className="pwa-banner-title">{t('pwa_update_title')}</p>
            <p className="pwa-banner-desc">{t('pwa_update_desc')}</p>
          </div>
        </div>
        <div className="pwa-banner-actions">
          <button className="pwa-banner-btn pwa-banner-btn-primary" onClick={handleReload}>
            {t('pwa_update_reload')}
          </button>
          <button className="pwa-banner-btn pwa-banner-btn-secondary" onClick={handleClose} aria-label={t('pwa_update_dismiss')}>
            {t('pwa_update_dismiss')}
          </button>
        </div>
      </div>
    )
  }

  // offlineReady
  return (
    <div className="pwa-banner pwa-banner-offline" role="status" aria-live="polite" data-testid="pwa-offline-banner">
      <div className="pwa-banner-body">
        <div className="pwa-banner-icon"><AppIcon name="check" size={20} /></div>
        <div className="pwa-banner-text">
          <p className="pwa-banner-title">{t('pwa_offline_ready_title')}</p>
          <p className="pwa-banner-desc">{t('pwa_offline_ready_desc')}</p>
        </div>
      </div>
      <button className="pwa-banner-btn pwa-banner-btn-secondary" onClick={handleClose} aria-label={t('pwa_offline_ready_dismiss')}>
        {t('pwa_offline_ready_dismiss')}
      </button>
    </div>
  )
}
