import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import type { AppLanguage, SearchStringLanguage } from '@/types'

export function SettingsScreen() {
  const { t, appLanguage, setAppLanguage, searchLanguage, setSearchLanguage } = useI18n()
  const navigate = useNavigate()

  const appLanguages: AppLanguage[] = ['System Default', 'English', 'Deutsch', 'Español', 'Français', 'Italiano', 'Türkçe']
  const searchLanguages: SearchStringLanguage[] = ['Auto', 'Match App Language', 'English', 'German', 'Spanish', 'French', 'Italian', 'Turkish']

  const searchLanguageLabel = (lang: SearchStringLanguage) => {
    const labels: Partial<Record<SearchStringLanguage, string>> = {
      Auto: t('settings_search_lang_auto'),
      'Match App Language': t('settings_search_lang_match_app'),
      English: t('settings_search_lang_english'),
      German: t('settings_search_lang_german'),
      Spanish: t('settings_search_lang_spanish'),
      French: t('settings_search_lang_french'),
      Italian: t('settings_search_lang_italian'),
      Turkish: t('settings_search_lang_turkish'),
    }
    return labels[lang] ?? lang
  }

  return (
    <main className="page content-with-nav">
      <header className="page-header">
        <button type="button" className="back-btn" onClick={() => navigate('/')} aria-label="Back">←</button>
        <h1>{t('settings_title')}</h1>
      </header>

      <div className="settings-stack">
        <section className="settings-panel accent">
          <h2 className="panel-title">{t('settings_search_language')}</h2>
          <div className="setting-block">
            <div className="setting-label">{t('settings_app_language')}</div>
            <div className="setting-help">{t('settings_app_language_desc')}</div>
            <label className="setting-select-row">
              <span>{t('settings_change_language')}</span>
              <select value={appLanguage} onChange={event => setAppLanguage(event.target.value as AppLanguage)}>
                {appLanguages.map(lang => (
                  <option key={lang} value={lang}>{lang === 'System Default' ? t('settings_system_default') : lang}</option>
                ))}
              </select>
            </label>
            <p className="setting-help">{t('settings_app_language_footnote')}</p>
          </div>

          <div className="setting-block">
            <div className="setting-label">{t('settings_search_string_lang')}</div>
            <div className="setting-help">{t('settings_search_string_lang_desc')}</div>
            <label className="setting-select-row">
              <span>{t('settings_change_language')}</span>
              <select value={searchLanguage} onChange={event => setSearchLanguage(event.target.value as SearchStringLanguage)}>
                {searchLanguages.map(lang => <option key={lang} value={lang}>{searchLanguageLabel(lang)}</option>)}
              </select>
            </label>
            {searchLanguage !== 'English' && searchLanguage !== 'Auto' && (
              <p className="setting-help">{t('settings_search_lang_warning')}</p>
            )}
          </div>
        </section>

        <section className="settings-panel">
          <h2 className="panel-title">{t('settings_safety')}</h2>
          <div className="setting-block">
            <div className="setting-label">{t('settings_online_events')}</div>
            <p className="setting-help">{t('settings_online_events_desc')}</p>
          </div>
          <div className="setting-block">
            <div className="setting-label">{t('settings_safety_stance')}</div>
            <p className="setting-help">{t('settings_safety_desc1')}</p>
            <p className="setting-help">{t('settings_safety_desc2')}</p>
            <p className="setting-help">{t('settings_safety_disclaimer')}</p>
          </div>
        </section>

        <section className="settings-panel">
          <h2 className="panel-title">{t('settings_about_privacy')}</h2>
          <p className="setting-help">{t('settings_about_desc1')}</p>
          <p className="setting-help">{t('settings_about_desc2')}</p>
          <p className="setting-help">{t('settings_about_desc3')}</p>
          <button type="button" className="setting-select-row card card-tap" onClick={() => navigate('/changelog')}>
            <span>{t('settings_changelog_label')}</span><span aria-hidden="true">›</span>
          </button>
        </section>

        <p className="text-muted" style={{ fontSize: '10px', textAlign: 'center', lineHeight: 1.45 }}>
          {t('settings_disclaimer')}
        </p>
      </div>
    </main>
  )
}
