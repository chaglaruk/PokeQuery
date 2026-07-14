import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { copyToClipboard, type ClipboardResult } from '@ui/clipboard'
import { addHistory, removeFavorite, useSavedSearches, type SavedSearchKind } from '@ui/savedSearches'
import { AppIcon } from '@ui/components/SpriteIcon'

export function SavedSearchesScreen({ kind }: { kind: SavedSearchKind }) {
  const { t } = useI18n()
  const navigate = useNavigate()
  const searches = useSavedSearches(kind)
  const [clipboard, setClipboard] = useState<ClipboardResult | null>(null)

  const handleCopy = async (index: number) => {
    const search = searches[index]
    if (!search) return
    const result = await copyToClipboard(search.rawSyntax)
    setClipboard(result)
    if (result.status === 'copied') {
      addHistory(search)
      setTimeout(() => setClipboard(null), 2000)
    }
  }

  const isFavorites = kind === 'favorites'
  return (
    <main className="page content-with-nav saved-search-page">
      <header className="page-header">
        <button type="button" className="back-btn" onClick={() => navigate('/')} aria-label={t('back')}>‹</button>
        <h1>{t(isFavorites ? 'nav_favorites' : 'nav_history')}</h1>
      </header>

      {clipboard && (
        <div className={`clipboard-feedback ${clipboard.status}`} role="status" aria-live="polite">
          {t(clipboard.i18nKey)}
        </div>
      )}

      {searches.length === 0 ? (
        <section className="saved-empty">
          <AppIcon name={isFavorites ? 'favorite' : 'history'} size={40} />
          <h2>{t(isFavorites ? 'favorites_empty_title' : 'history_empty_title')}</h2>
          <p>{t(isFavorites ? 'favorites_empty_subtitle' : 'history_empty_subtitle')}</p>
        </section>
      ) : (
        <div className="saved-search-list">
          {searches.map((search, index) => (
            <article className={`card saved-search-card risk-${search.riskLevel.toLowerCase()}`} key={search.id}>
              <div className="saved-search-head">
                <div><h2>{search.name}</h2><p>{search.goalId}</p></div>
                <span className={`badge badge-${search.riskLevel.toLowerCase()}`}>{t(`risk_${search.riskLevel.toLowerCase()}_display`)}</span>
              </div>
              <div className="search-string">{search.rawSyntax}</div>
              <div className="saved-search-actions">
                <button type="button" className="btn btn-primary" onClick={() => handleCopy(index)}>
                  <AppIcon name="copy" size={16} /> {t(isFavorites ? 'saved_template_copy' : 'saved_template_copy_again')}
                </button>
                {isFavorites && (
                  <button type="button" className="icon-action danger" onClick={() => removeFavorite(search.id)} aria-label={t('action_delete')}>
                    <AppIcon name="delete" size={19} />
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </main>
  )
}
