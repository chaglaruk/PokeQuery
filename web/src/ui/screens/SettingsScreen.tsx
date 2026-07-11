import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import type { AppLanguage, SearchStringLanguage } from '@/types'

export function SettingsScreen() {
  const { t, appLanguage, setAppLanguage, searchLanguage, setSearchLanguage } = useI18n()
  const navigate = useNavigate()

  const appLanguages: AppLanguage[] = ['System Default', 'English', 'Deutsch', 'Español', 'Français', 'Italiano', 'Türkçe']
  const searchLanguages: SearchStringLanguage[] = ['Auto', 'Match App Language', 'English', 'German', 'Spanish', 'French', 'Italian', 'Turkish']

  return (
    <div className="page content-with-nav">
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>‹</button>
        <h1>{t('settings_title')}</h1>
      </div>
      <div className="section-title">{t('settings_general')}</div>

      <div className="card">
        <div className="toggle">
          <div>
            <div className="toggle-label">{t('settings_app_language')}</div>
            <div className="toggle-desc">{t('settings_app_language_desc')}</div>
          </div>
          <select
            value={appLanguage}
            onChange={e => setAppLanguage(e.target.value as AppLanguage)}
            style={{ width: 'auto', minWidth: '140px' }}
          >
            {appLanguages.map(lang => (
              <option key={lang} value={lang}>{lang === 'System Default' ? t('settings_system_default') : lang}</option>
            ))}
          </select>
        </div>
        <div className="text-muted" style={{ marginTop: '8px' }}>
          {t('settings_app_language_footnote')}
        </div>
      </div>

      <div className="card">
        <div className="toggle">
          <div>
            <div className="toggle-label">{t('settings_search_string_lang')}</div>
            <div className="toggle-desc">{t('settings_search_string_lang_desc')}</div>
          </div>
          <select
            value={searchLanguage}
            onChange={e => setSearchLanguage(e.target.value as SearchStringLanguage)}
            style={{ width: 'auto', minWidth: '160px' }}
          >
            {searchLanguages.map(lang => (
              <option key={lang} value={lang}>
                {lang === 'Auto' ? t('settings_search_lang_auto') :
                 lang === 'Turkish' ? t('settings_search_lang_turkish') :
                 lang === 'Match App Language' ? t('settings_search_lang_match_app') :
                 lang === 'English' ? t('settings_search_lang_english') :
                 lang === 'German' ? t('settings_search_lang_german') :
                 lang === 'Spanish' ? t('settings_search_lang_spanish') :
                 lang === 'French' ? t('settings_search_lang_french') :
                 lang === 'Italian' ? t('settings_search_lang_italian') :
                 lang}
              </option>
            ))}
          </select>
        </div>
        {searchLanguage !== 'English' && searchLanguage !== 'Auto' && (
          <div className="text-muted" style={{ marginTop: '8px' }}>
            {t('settings_search_lang_warning')}
          </div>
        )}
      </div>

      <div className="section-title">{t('settings_online_events')}</div>
      <div className="card">
        <p style={{ fontWeight: 600, marginBottom: '8px' }}>{t('settings_about_privacy')}</p>
        <p className="text-muted">{t('settings_online_events_desc')}</p>
      </div>

      <div className="section-title">{t('settings_safety')}</div>
      <div className="card">
        <p className="text-muted">{t('settings_safety_desc1')}</p>
        <p className="text-muted" style={{ marginTop: '8px' }}>{t('settings_safety_desc2')}</p>
        <p className="text-muted" style={{ marginTop: '16px', fontWeight: 600 }}>{t('settings_safety_stance')}</p>
        <p className="text-muted" style={{ marginTop: '4px' }}>{t('settings_safety_disclaimer')}</p>
      </div>

      <div className="section-title">{t('settings_about_privacy')}</div>
      <div className="card">
        <p className="text-muted">{t('settings_about_desc1')}</p>
        <p className="text-muted" style={{ marginTop: '8px' }}>{t('settings_about_desc2')}</p>
        <p className="text-muted" style={{ marginTop: '8px' }}>{t('settings_about_desc3')}</p>
      </div>

      <div
        className="card card-tap"
        onClick={() => navigate('/changelog')}
        style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}
      >
        <div>
          <p style={{ fontWeight: 600 }}>{t('settings_changelog_label')}</p>
          <p className="text-muted" style={{ marginTop: '2px' }}>{t('goal_changelog_desc')}</p>
        </div>
        <span style={{ color: 'var(--text-muted)', fontSize: '20px' }}>›</span>
      </div>

      <div className="card">
        <p className="text-muted" style={{ fontSize: '12px', textAlign: 'center' }}>
          {t('settings_disclaimer')}
        </p>
      </div>
    </div>
  )
}
