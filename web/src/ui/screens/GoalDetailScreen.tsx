import { useState, useMemo, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { buildGoal } from '@engine/stringBuilderEngine'
import { buildFinal } from '@engine/goalStringBuilder'
import { lint } from '@engine/linter'
import { canCopy } from '@engine/expertCopyPolicy'
import type { RiskLevel } from '@/types'
import { AppIcon } from '@ui/components/SpriteIcon'

const riskBadgeClass: Record<RiskLevel, string> = {
  Info: 'badge-info',
  Low: 'badge-low',
  Medium: 'badge-medium',
  High: 'badge-high',
}

export function GoalDetailScreen() {
  const { goalId = '' } = useParams<{ goalId: string }>()
  const navigate = useNavigate()
  const { t, resolvedSearchLanguage } = useI18n()

  const [config, setConfig] = useState<string>('')
  const [expertQuery, setExpertQuery] = useState<string>('')
  const [copied, setCopied] = useState(false)
  const [optionalProtections, setOptionalProtections] = useState<string[]>([])

  const goal = useMemo(() => {
    return buildGoal(goalId, config, expertQuery, resolvedSearchLanguage)
  }, [goalId, config, expertQuery, resolvedSearchLanguage])

  const finalString = useMemo(() => {
    return buildFinal(goal, optionalProtections, resolvedSearchLanguage)
  }, [goal, optionalProtections, resolvedSearchLanguage])

  const expertWarnings = useMemo(() => {
    if (goalId !== 'expert') return []
    return lint(expertQuery || goal.rawSyntax)
  }, [goalId, expertQuery, goal.rawSyntax])

  const canCopyResult = useMemo(() => {
    if (goalId === 'expert') return canCopy(expertQuery || goal.rawSyntax)
    return true
  }, [goalId, expertQuery, goal.rawSyntax])

  const handleCopy = useCallback(async () => {
    if (!canCopyResult) return
    try {
      await navigator.clipboard.writeText(finalString.rawSyntax)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // Fallback: select text
    }
  }, [finalString.rawSyntax, canCopyResult])

  const toggleProtection = useCallback((token: string) => {
    setOptionalProtections(prev =>
      prev.includes(token) ? prev.filter(p => p !== token) : [...prev, token]
    )
  }, [])

  const hasOptions = ['safe_cleanup', 'pvp_candidates', 'lucky_trade', 'expert'].includes(goalId)

  return (
    <div className="page content-with-nav">
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>‹</button>
        <h1>{t(`goal_${goalId}`)}</h1>
      </div>

      {/* Risk badge */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
        <span className={`badge ${riskBadgeClass[finalString.riskLevel]}`}>
          {t(`risk_${finalString.riskLevel.toLowerCase()}`)}
        </span>
        <span className="badge badge-beta">{finalString.scopeBreadth}</span>
      </div>

      {/* Explanation */}
      <div className="card">
        <p style={{ fontSize: '15px', lineHeight: 1.6 }}>{finalString.plainLanguageExplanation}</p>
      </div>

      {/* Expert input */}
      {goalId === 'expert' && (
        <div className="card">
          <p style={{ fontWeight: 600, marginBottom: '12px' }}>{t('goal_detail_your_string')}</p>
          <input
            type="text"
            value={expertQuery}
            onChange={e => setExpertQuery(e.target.value)}
            placeholder={t('explain_placeholder')}
          />
          {expertWarnings.length > 0 && (
            <div style={{ marginTop: '10px' }}>
              {expertWarnings.map((w, i) => (
                <p
                  key={i}
                  style={{
                    fontSize: '13px',
                    marginTop: '6px',
                    color: w.isError ? 'var(--danger)' : 'var(--warning)',
                  }}
                >
                  <AppIcon name={w.isError ? 'error' : 'warning'} size={16} /> {w.message}
                </p>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Options */}
      {hasOptions && (
        <div className="card">
          <div className="section-title" style={{ margin: '0 0 12px' }}>{t('goal_detail_refine')}</div>

          {goalId === 'safe_cleanup' && (
            <div className="toggle">
              <div>
                <div className="toggle-label">{t('goal_detail_include_0star')}</div>
                <div className="toggle-desc">{t('goal_detail_collector_interest')}</div>
              </div>
              <input
                type="checkbox"
                checked={config === 'include0Star'}
                onChange={e => setConfig(e.target.checked ? 'include0Star' : '')}
              />
            </div>
          )}

          {goalId === 'pvp_candidates' && (
            <div>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  className={`btn ${config !== 'ultra' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setConfig('great')}
                  style={{ flex: 1 }}
                >
                  {t('goal_detail_great_league')}
                </button>
                <button
                  className={`btn ${config === 'ultra' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setConfig('ultra')}
                  style={{ flex: 1 }}
                >
                  {t('goal_detail_ultra_league')}
                </button>
              </div>
              <p className="text-muted" style={{ marginTop: '10px' }}>
                {config === 'ultra' ? t('goal_detail_under_2500') : t('goal_detail_under_1500')}
              </p>
              <p className="text-muted" style={{ marginTop: '4px' }}>{t('goal_detail_pvp_rank_note')}</p>
            </div>
          )}

          {goalId === 'lucky_trade' && (
            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                className={`btn ${config !== 'distance' ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setConfig('age')}
                style={{ flex: 1 }}
              >
                {t('goal_detail_older_candidates')}
              </button>
              <button
                className={`btn ${config === 'distance' ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setConfig('distance')}
                style={{ flex: 1 }}
              >
                {t('goal_detail_distance_candidates')}
              </button>
            </div>
          )}

          {/* Optional protections (non-passthrough goals) */}
          {goalId !== 'expert' && ['safe_cleanup', 'untagged'].includes(goalId) && (
            <div style={{ marginTop: '16px' }}>
              <div className="section-title" style={{ margin: '0 0 8px' }}>{t('goal_detail_protected')}</div>
              {['shiny', 'legendary', 'costume', '4*'].map(token => {
                const isAlreadyIn = finalString.rawSyntax.includes(`!${token}`)
                const isChecked = isAlreadyIn || optionalProtections.includes(token)
                return (
                  <div key={token} className="toggle">
                    <div className="toggle-label">
                      {t(`goal_detail_exclude_${token === '4*' ? 'hundos' : token === 'legendary' ? 'legendaries' : token === 'costume' ? 'costumes' : 'shinies'}`)}
                    </div>
                    <input
                      type="checkbox"
                      checked={isChecked}
                      disabled={isAlreadyIn}
                      onChange={() => toggleProtection(token)}
                    />
                  </div>
                )
              })}
            </div>
          )}
        </div>
      )}

      {/* Warnings */}
      {finalString.warnings.length > 0 && (
        <div className="card">
          <div className="section-title" style={{ margin: '0 0 10px', display: 'flex', gap: '6px', alignItems: 'center' }}><AppIcon name="warning" size={16} /> {t('goal_detail_watch_out')}</div>
          {finalString.warnings.map((w, i) => (
            <p key={i} className="text-muted" style={{ marginTop: i > 0 ? '8px' : 0, lineHeight: 1.5 }}>
              {'\u2022'} {w}
            </p>
          ))}
        </div>
      )}

      {/* Engine warnings for expert */}
      {goalId === 'expert' && goal.warnings.length > 0 && (
        <div className="card">
          <div className="section-title" style={{ margin: '0 0 10px', display: 'flex', gap: '6px', alignItems: 'center' }}><AppIcon name="warning" size={16} /> {t('goal_detail_watch_out')}</div>
          {goal.warnings.map((w, i) => (
            <p key={i} className="text-muted" style={{ marginTop: i > 0 ? '8px' : 0 }}>
              • {w}
            </p>
          ))}
        </div>
      )}

      {/* Result */}
      <div className="card">
        <div className="section-title" style={{ margin: '0 0 10px' }}>{t('goal_detail_result')}</div>
        <div className="search-string">{finalString.rawSyntax}</div>
        {finalString.protectedCategories.length > 0 && (
          <div style={{ marginTop: '10px' }}>
            <p className="text-muted" style={{ marginBottom: '6px' }}>{t('goal_detail_protected')}:</p>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
              {finalString.protectedCategories.map(cat => (
                <span key={cat} className="badge badge-low">!{cat}</span>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Copy button */}
      <div style={{ marginTop: '16px' }}>
        <button
          className="btn btn-primary"
          onClick={handleCopy}
          disabled={!canCopyResult}
        >
          {copied ? `\u2714 ${t('goal_detail_copied')}` : t('goal_detail_copy_search_string')}
        </button>
        {!canCopyResult && (
          <p className="text-muted" style={{ textAlign: 'center', marginTop: '8px' }}>
            {t('goal_detail_fix_errors')}
          </p>
        )}
      </div>

      <div className="card" style={{ marginTop: '16px' }}>
        <p className="text-muted">{'\u29BF'} {t('goal_detail_review_matches')}</p>
        <p className="text-muted" style={{ marginTop: '4px' }}>{'\u2716'} {t('goal_detail_never_blind')}</p>
      </div>
    </div>
  )
}
