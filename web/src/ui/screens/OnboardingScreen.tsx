import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { SpriteIcon, goalIcon } from '../components/SpriteIcon'

const ONBOARDING_FLAG = 'pq_onboarding_complete'

export function isOnboardingComplete(): boolean {
  try {
    return localStorage.getItem(ONBOARDING_FLAG) === 'true'
  } catch {
    return false
  }
}

export function markOnboardingComplete(): void {
  try {
    localStorage.setItem(ONBOARDING_FLAG, 'true')
  } catch { /* private mode */ }
}

export function OnboardingScreen() {
  const { t } = useI18n()
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const pageCount = 2

  const handleComplete = () => {
    markOnboardingComplete()
    navigate('/', { replace: true })
  }

  const handleNext = () => {
    if (page < pageCount - 1) {
      setPage(page + 1)
    } else {
      handleComplete()
    }
  }

  const features = [
    { icon: goalIcon('lock'), titleKey: 'onboarding_feature_plan_title', descKey: 'onboarding_feature_plan_desc' },
    { icon: goalIcon('cloud_off'), titleKey: 'onboarding_feature_protect_title', descKey: 'onboarding_feature_protect_desc' },
    { icon: goalIcon('copy'), titleKey: 'onboarding_feature_copy_title', descKey: 'onboarding_feature_copy_desc' },
  ]

  return (
    <div className="page onboarding">
      <div className="onboarding-skip">
        <button
          onClick={handleComplete}
          style={{ fontSize: '14px', color: 'var(--text-muted)' }}
        >
          {t('onboarding_skip')}
        </button>
      </div>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        {page === 0 && (
          <div>
            <h1 className="visually-hidden">PokeQuery</h1>
            <div style={{ textAlign: 'center', marginBottom: '12px' }}>
              <SpriteIcon sprite="pokequery_wordmark" alt="PokeQuery" size={200} />
            </div>
            <div
              style={{
                background: 'rgba(11,140,156,0.10)',
                borderRadius: '24px',
                padding: '12px 18px',
                margin: '0 auto 24px',
                maxWidth: '320px',
                textAlign: 'center',
              }}
            >
              <p style={{ fontSize: '16px', fontWeight: 600, lineHeight: 1.35 }}>
                {t('onboarding_hero_tagline')}
              </p>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {features.map(f => (
                <div key={f.titleKey} className="card" style={{ display: 'flex', gap: '14px', alignItems: 'center' }}>
                  <span style={{ fontSize: '24px', flexShrink: 0 }}>{f.icon}</span>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <p style={{ fontSize: '14px', fontWeight: 700 }}>{t(f.titleKey)}</p>
                    <p className="text-muted" style={{ marginTop: '2px' }}>{t(f.descKey)}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {page === 1 && (
          <div style={{ textAlign: 'center' }}>
            <h1 style={{ fontSize: '28px', fontWeight: 800, marginBottom: '16px', lineHeight: 1.25 }}>
              {t('onboarding_card2_title')}
            </h1>
            <div
              style={{
                background: 'var(--bg-card)',
                borderRadius: '22px',
                border: '1px solid var(--border)',
                padding: '18px',
              }}
            >
              <p style={{ fontSize: '16px', lineHeight: 1.5 }}>
                {t('onboarding_card2_desc')}
              </p>
            </div>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: '18px', paddingBottom: 'calc(18px + var(--safe-bottom))' }}>
        <div style={{ display: 'flex', gap: '8px' }}>
          {Array.from({ length: pageCount }).map((_, i) => (
            <div
              key={i}
              style={{
                width: i === page ? '10px' : '8px',
                height: i === page ? '10px' : '8px',
                borderRadius: '50%',
                background: i === page ? 'var(--accent)' : 'var(--text-muted)',
                opacity: i === page ? 1 : 0.5,
              }}
            />
          ))}
        </div>
        <button className="btn btn-primary" onClick={handleNext} style={{ width: 'auto', minWidth: '200px' }}>
          {page === pageCount - 1 ? t('onboarding_start_building') : t('onboarding_next')} →
        </button>
      </div>
    </div>
  )
}
