import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { buildGoal } from '@engine/stringBuilderEngine'
import { buildFinal } from '@engine/goalStringBuilder'
import { lint } from '@engine/linter'
import { canCopy } from '@engine/expertCopyPolicy'
import { AppIcon } from '@ui/components/SpriteIcon'
import { copyToClipboard, type ClipboardResult } from '@ui/clipboard'
import { addFavorite, addHistory, findFavorite, removeFavorite, type SavedSearch } from '@ui/savedSearches'

export function GoalDetailScreen() {
  const { goalId = '' } = useParams<{ goalId: string }>()
  const navigate = useNavigate()
  const { t, resolvedSearchLanguage } = useI18n()
  const [config, setConfig] = useState('')
  const [expertQuery, setExpertQuery] = useState('')
  const [clipboard, setClipboard] = useState<ClipboardResult | null>(null)
  const [favorite, setFavorite] = useState<SavedSearch | null>(null)
  const [showRefine, setShowRefine] = useState(false)
  const [optionalProtections, setOptionalProtections] = useState<string[]>([])

  const goal = useMemo(
    () => buildGoal(goalId, config, expertQuery, resolvedSearchLanguage),
    [goalId, config, expertQuery, resolvedSearchLanguage],
  )
  const finalString = useMemo(
    () => buildFinal(goal, optionalProtections, resolvedSearchLanguage),
    [goal, optionalProtections, resolvedSearchLanguage],
  )
  const expertWarnings = useMemo(
    () => goalId === 'expert' ? lint(expertQuery || goal.rawSyntax) : [],
    [goalId, expertQuery, goal.rawSyntax],
  )
  const canCopyResult = useMemo(
    () => goalId !== 'expert' || canCopy(expertQuery || goal.rawSyntax),
    [goalId, expertQuery, goal.rawSyntax],
  )
  const hasOptions = ['safe_cleanup', 'pvp_candidates', 'lucky_trade'].includes(goalId)

  useEffect(() => {
    setFavorite(findFavorite(finalString.rawSyntax) ?? null)
  }, [finalString.rawSyntax])

  const handleCopy = useCallback(async () => {
    if (!canCopyResult) return
    const result = await copyToClipboard(finalString.rawSyntax)
    setClipboard(result)
    if (result.status === 'copied') {
      addHistory({ name: t(`goal_${goalId}`), rawSyntax: finalString.rawSyntax, goalId, riskLevel: finalString.riskLevel })
      setTimeout(() => setClipboard(null), 2000)
    }
  }, [finalString.rawSyntax, finalString.riskLevel, canCopyResult, goalId, t])

  const toggleFavorite = useCallback(() => {
    if (favorite) {
      removeFavorite(favorite.id)
      setFavorite(null)
      return
    }
    setFavorite(addFavorite({ name: t(`goal_${goalId}`), rawSyntax: finalString.rawSyntax, goalId, riskLevel: finalString.riskLevel }))
  }, [favorite, finalString.rawSyntax, finalString.riskLevel, goalId, t])

  const toggleProtection = useCallback((token: string) => {
    setOptionalProtections(current => current.includes(token) ? current.filter(value => value !== token) : [...current, token])
  }, [])

  const renderLeagueSelector = () => (
    <>
      <div className="segmented" role="tablist" aria-label={t('goal_pvp_candidates')}>
        <button type="button" className={`segment ${config !== 'ultra' ? 'active' : ''}`} onClick={() => setConfig('great')}>{t('goal_detail_great_league')}</button>
        <button type="button" className={`segment ${config === 'ultra' ? 'active' : ''}`} onClick={() => setConfig('ultra')}>{t('goal_detail_ultra_league')}</button>
      </div>
      <div className="league-copy">
        <p className="league-limit">{config === 'ultra' ? t('goal_detail_under_2500') : t('goal_detail_under_1500')}</p>
        <p className="league-note">{t('goal_detail_pvp_rank_note')}</p>
      </div>
    </>
  )

  return (
    <main className="page content-with-nav">
      <header className="page-header">
        <button type="button" className="back-btn" onClick={() => navigate('/')} aria-label="Back">←</button>
        <h1>{t(`goal_${goalId}`)}</h1>
        <button type="button" className={`header-action ${favorite ? 'active' : ''}`} onClick={toggleFavorite} aria-label={t('nav_favorites')}>
          <AppIcon name="favorite" size={21} />
        </button>
      </header>

      <section className={`detail-result ${finalString.riskLevel === 'Medium' ? 'medium' : ''}`}>
        <h2 className="detail-result-title">{t('goal_detail_result')}</h2>
        {goalId === 'pvp_candidates' && renderLeagueSelector()}
        {goalId === 'expert' && (
          <div style={{ marginBottom: '12px' }}>
            <label className="setting-label" htmlFor="expert-query">{t('goal_detail_your_string')}</label>
            <input id="expert-query" type="text" value={expertQuery} onChange={event => setExpertQuery(event.target.value)} placeholder={t('explain_placeholder')} />
            {expertWarnings.map((warning, index) => (
              <p key={index} className="setting-help" style={{ color: warning.isError ? 'var(--danger)' : 'var(--warning)' }}>
                <AppIcon name={warning.isError ? 'error' : 'warning'} size={14} /> {t(warning.isError ? 'goal_detail_fix_errors' : 'search_intent_lint_advisory')}
              </p>
            ))}
          </div>
        )}
        <div className="search-string">{finalString.rawSyntax}</div>
        <div className="detail-actions">
          {hasOptions && (
            <button type="button" className="btn btn-edit" onClick={() => setShowRefine(value => !value)}><AppIcon name="assistant" size={18} /> {t('goal_detail_edit_search')}</button>
          )}
          <button type="button" className={`btn btn-copy ${finalString.riskLevel === 'Medium' ? 'medium' : ''}`} onClick={handleCopy} disabled={!canCopyResult}>
            <AppIcon name="copy" size={18} /> {clipboard?.status === 'copied' ? t('goal_detail_copied') : t('goal_detail_copy_search_string')}
          </button>
        </div>
        {clipboard && <div className={`clipboard-feedback ${clipboard.status}`} role="status" aria-live="polite">{t(clipboard.i18nKey)}</div>}
        {!canCopyResult && <p className="setting-help">{t('goal_detail_fix_errors')}</p>}
      </section>

      {showRefine && hasOptions && (
        <section className="card">
          <h2 className="section-title" style={{ marginTop: 0 }}>{t('goal_detail_refine')}</h2>
          {goalId === 'safe_cleanup' && (
            <label className="setting-row">
              <span className="setting-copy"><strong className="setting-label">{t('goal_detail_include_0star')}</strong><span className="setting-help">{t('goal_detail_collector_interest')}</span></span>
              <span className="switch"><input type="checkbox" checked={config === 'include0Star'} onChange={event => setConfig(event.target.checked ? 'include0Star' : '')} /><span className="switch-track" /></span>
            </label>
          )}
          {goalId === 'pvp_candidates' && renderLeagueSelector()}
          {goalId === 'lucky_trade' && (
            <div className="segmented">
              <button type="button" className={`segment ${config !== 'distance' ? 'active' : ''}`} onClick={() => setConfig('age')}>{t('goal_detail_older_candidates')}</button>
              <button type="button" className={`segment ${config === 'distance' ? 'active' : ''}`} onClick={() => setConfig('distance')}>{t('goal_detail_distance_candidates')}</button>
            </div>
          )}
          {goalId === 'safe_cleanup' && (
            <div style={{ marginTop: '16px' }}>
              <h3 className="section-title" style={{ marginTop: 0 }}>{t('goal_detail_protected')}</h3>
              <div className="protection-chips">
                {['shiny', 'legendary', 'costume', '4*'].map(token => {
                  const isAlreadyIn = finalString.rawSyntax.includes(`!${token}`)
                  const isChecked = isAlreadyIn || optionalProtections.includes(token)
                  const key = token === '4*' ? 'hundos' : token === 'legendary' ? 'legendaries' : token === 'costume' ? 'costumes' : 'shinies'
                  return <button type="button" key={token} className="protection-chip" disabled={isAlreadyIn} onClick={() => toggleProtection(token)}>{isChecked && <AppIcon name="check" size={13} />}{t(`goal_detail_exclude_${key}`)}</button>
                })}
              </div>
            </div>
          )}
        </section>
      )}

      <section className={`card info-card ${finalString.riskLevel === 'Medium' ? 'medium' : ''}`}>
        <h2 className="info-heading"><AppIcon name="info" size={16} /> {t('goal_detail_what_does_this_do')}</h2>
        <p style={{ marginTop: '8px' }}>{t(`risk_${goalId}_short`)}</p>
        <h3 className="info-heading warning-heading"><AppIcon name="warning" size={16} /> {t('goal_detail_watch_out')}</h3>
        <p style={{ marginTop: '5px' }}>• {t(`risk_${goalId}_check1`)}</p>
        <h3 className="info-heading warning-heading"><AppIcon name="warning" size={16} /> {t('goal_detail_review_aid')}</h3>
        <p>{t('goal_detail_review_matches')} {t('goal_detail_never_blind')}</p>
      </section>

      {finalString.protectedCategories.length > 0 && (
        <section>
          <h2 className="section-title">{t('goal_detail_protected')}</h2>
          <div className="protection-chips">
            {finalString.protectedCategories.map(category => <span key={category} className="protection-chip"><AppIcon name="check" size={13} /> {category}</span>)}
          </div>
        </section>
      )}
    </main>
  )
}
