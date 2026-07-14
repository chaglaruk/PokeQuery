import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { parseSearchIntent, type ParsedIntent } from '@engine/searchIntentParser'
import { lint, type LintWarning } from '@engine/linter'
import { canCopy } from '@engine/expertCopyPolicy'
import { explain, type ExplainedString } from '@engine/searchStringExplainer'
import { translateSyntax, findUnverifiedTokens, resolveLanguage } from '@engine/searchTermMapper'
import { copyToClipboard, type ClipboardStatus } from '@ui/clipboard'
import type { ClipboardResult } from '@ui/clipboard'
import { AppIcon } from '@ui/components/SpriteIcon'

const scopeBreadthKeys: Record<string, string> = {
  'All (no filter)': 'explainer_scope_all',
  'Very Narrow': 'explainer_scope_very_narrow',
  'Narrow': 'explainer_scope_narrow',
  'Moderate': 'explainer_scope_moderate',
  'Broad': 'explainer_scope_broad',
}

const precisionLabelKeys: Record<string, string> = {
  EXACT: 'explainer_prec_exact',
  SHORTLIST: 'explainer_prec_shortlist',
  APPROXIMATE: 'explainer_prec_approx',
  NEEDS_VERIFICATION: 'explainer_prec_needs_verif',
  UNKNOWN: 'explainer_prec_unknown',
}

export function SearchAssistantScreen() {
  const { t, resolvedSearchLanguage } = useI18n()
  const navigate = useNavigate()
  const [input, setInput] = useState('')
  const [result, setResult] = useState<ParsedIntent | null>(null)
  const [explained, setExplained] = useState<ExplainedString | null>(null)
  const [clipboard, setClipboard] = useState<ClipboardResult | null>(null)

  // Resolve the raw query through search-string-language localization:
  // translate tokens (e.g. `legendary` → `efsanevi` for Turkish), find any
  // unverified tokens (English fallback applied for those), run the linter
  // against the *untranslated* query, then gate copy through ExpertCopyPolicy.
  // Linter + ExpertCopyPolicy always run on the English rawQuery to avoid
  // false positives on localized syntax; translation is only applied to the
  // copy target visible to the user.
  const rawQuery = result?.rawQuery ?? ''
  const resolvedLanguage = resolveLanguage(resolvedSearchLanguage)
  const translatedQuery = rawQuery ? translateSyntax(rawQuery, resolvedLanguage) : ''
  const unverifiedTokens = rawQuery ? findUnverifiedTokens(rawQuery, resolvedLanguage) : []
  const warnings: LintWarning[] = rawQuery ? lint(rawQuery) : []
  const copyBlocked = rawQuery ? !canCopy(rawQuery) : false
  const hasAdvisory = !copyBlocked && warnings.length > 0

  const handleParse = useCallback(() => {
    if (!input.trim()) {
      setResult(null)
      setExplained(null)
      setClipboard(null)
      return
    }
    const parsed = parseSearchIntent(input)
    setResult(parsed)
    setExplained(parsed.rawQuery ? explain(parsed.rawQuery) : null)
    setClipboard(null)
  }, [input])

  const handleCopy = useCallback(async () => {
    if (!result?.rawQuery) return
    if (copyBlocked) return
    const target = translatedQuery || result.rawQuery
    const res = await copyToClipboard(target)
    setClipboard(res)
    if (res.status === 'copied') {
      setTimeout(() => setClipboard(null), 2500)
    }
  }, [result, translatedQuery, copyBlocked])

  return (
    <div className="page content-with-nav">
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>‹</button>
        <h1>{t('search_assistant_title')}</h1>
      </div>

      <div className="card" style={{ marginBottom: '16px' }}>
        <p style={{ fontSize: '13px', color: 'var(--text-dim)', marginBottom: '12px', lineHeight: 1.5 }}>
          {t('search_assistant_desc_text')}
        </p>
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => { if (e.key === 'Enter') handleParse() }}
          placeholder={t('search_assistant_placeholder')}
          aria-label={t('search_assistant_title')}
          style={{ marginBottom: '12px' }}
        />
        <button className="btn btn-primary" onClick={handleParse} disabled={!input.trim()}>
          {t('search_assistant_parse')}
        </button>
      </div>

      {result && !result.canBuild && result.explanation && (
        <div className="card" style={{ borderColor: 'var(--warning)' }}>
          <p style={{ fontSize: '14px', color: 'var(--text)', lineHeight: 1.5 }} role="status">
            {result.explanationKey === 'search_intent_could_not_understand'
              ? t('search_intent_could_not_understand', input)
              : t(result.explanationKey) !== result.explanationKey
                ? t(result.explanationKey)
                : result.explanation}
          </p>
        </div>
      )}

      {result && result.canBuild && (
        <div className="card">
          {/* Suggested string + precision badge */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
            <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--accent)', margin: 0 }}>
              {t('search_assistant_suggested_string')}
            </p>
            {explained && (
              <span className={`badge ${precisionBadgeClass(explained.precision)}`}>
                {t(precisionLabelKeys[explained.precision] ?? 'explain_prec_unknown')}
              </span>
            )}
          </div>
          <div className="search-string" style={{ marginBottom: '12px' }}>
            {translatedQuery || result.rawQuery}
          </div>

          {/* Locale + unverified-token notice */}
          {resolvedLanguage !== 'English' && (
            <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '8px' }}>
              {t('search_assistant_locale_label', resolvedLanguage)}
              {unverifiedTokens.length > 0 && (
                <>
                  {' '}
                  {t('search_assistant_unverified_tokens', unverifiedTokens.join(', '))}
                </>
              )}
            </p>
          )}

          {/* Linter banner — blocking vs advisory */}
          {warnings.length > 0 && (
            <div className={`lint-banner ${copyBlocked ? 'blocking' : 'advisory'}`} role="status">
              <ul style={{ listStyle: 'none', padding: 0, margin: '0 0 6px 0' }}>
                {warnings.map((w, i) => (
                  <li key={i} style={{ fontSize: '12px', lineHeight: 1.5, marginBottom: '4px', display: 'flex', gap: '6px', alignItems: 'flex-start' }}>
                    <span className={w.isError ? 'lint-marker-error' : 'lint-marker-advisory'} aria-hidden="true">
                      <AppIcon name={w.isError ? 'error' : 'warning'} size={16} />
                    </span>
                    <span>{w.message}</span>
                  </li>
                ))}
              </ul>
              {copyBlocked && (
                <p style={{ fontSize: '12px', fontWeight: 700, color: 'var(--danger)' }}>
                  {t('search_intent_fix_errors')}
                </p>
              )}
              {hasAdvisory && (
                <p style={{ fontSize: '12px', fontWeight: 700, color: 'var(--warning)' }}>
                  {t('search_intent_lint_advisory')}
                </p>
              )}
            </div>
          )}

          {/* Notes — pipe-forbidden, !traded-kept */}
          {result.noteKeys.map((key) => (
            <p key={key} style={{
              fontSize: '12px',
              color: 'var(--text-muted)',
              marginTop: '6px',
              fontStyle: 'italic',
            }}>
              {t(key)}
            </p>
          ))}

          {/* Copy + Explain */}
          <div style={{ display: 'flex', gap: '8px', marginTop: '12px' }}>
            <button
              className="btn btn-primary"
              onClick={handleCopy}
              disabled={copyBlocked}
              style={{ flex: 1 }}
            >
              {copyBlocked
                ? t('search_intent_fix_errors')
                : hasAdvisory
                  ? t('search_intent_copy_advisory')
                  : t('search_assistant_copy_btn')}
            </button>
            <button
              className="btn btn-secondary"
              onClick={() => navigate('/explain', { state: { rawQuery: result.rawQuery } })}
              style={{ flex: 1 }}
            >
              {t('search_assistant_explain_btn')}
            </button>
          </div>

          {/* Clipboard status feedback — visible for all 4 outcomes */}
          {clipboard && (
            <div
              className={`clipboard-feedback ${clipboard.status}`}
              role="status"
              aria-live="polite"
            >
              {t(clipboard.i18nKey)}
            </div>
          )}
        </div>
      )}

      {result && result.canBuild && (
        <div className="card">
          {/* Tokens */}
          {result.tokens.length > 0 && (
            <>
              <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--accent)' }}>
                {t('search_assistant_tokens')}
              </p>
              <div style={{ fontFamily: 'SF Mono, Consolas, monospace', background: 'var(--bg)', border: '1px solid var(--border)', borderRadius: '8px', padding: '10px 12px', marginBottom: '12px' }}>
                {result.tokens.map((token, i) => (
                  <div key={i} style={{ fontSize: '13px', color: 'var(--text)' }}>+ {token}</div>
                ))}
                {result.exclusions.map((ex, i) => (
                  <div key={`ex-${i}`} style={{ fontSize: '13px', color: 'var(--warning)' }}>
                    ! {ex} ({t('search_assistant_excluded')})
                  </div>
                ))}
              </div>
            </>
          )}

          {/* Explanation — i18n key resolved */}
          {result.explanationKey && (
            <>
              <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--accent)', marginBottom: '4px' }}>
                {t('search_assistant_explanation')}
              </p>
              <p style={{ fontSize: '12px', color: 'var(--text-dim)', lineHeight: 1.6, marginBottom: '12px' }}>
                {t(result.explanationKey) !== result.explanationKey
                  ? t(result.explanationKey)
                  : result.explanation}
              </p>
            </>
          )}

          {/* Limitations — i18n keys resolved */}
          {result.limitationKeys.length > 0 && (
            <>
              <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--warning)', marginBottom: '4px' }}>
                {t('search_assistant_limitations')}
              </p>
              <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                {result.limitationKeys.map((key, i) => {
                  const txt = t(key) !== key ? t(key) : (result.limitations[i] ?? key)
                  return (
                    <li key={i} style={{ fontSize: '12px', lineHeight: 1.5, marginBottom: '4px', display: 'flex', gap: '6px' }}>
                      <span style={{ color: 'var(--danger)' }} aria-hidden="true">{'\u2022'}</span>
                      <span style={{ color: 'var(--text-dim)' }}>{txt}</span>
                    </li>
                  )
                })}
              </ul>
            </>
          )}

          {/* Scope + Risk explainer */}
          {explained && (
            <div style={{ marginTop: '12px' }}>
              <span className={`badge ${riskBadgeClass(explained.totalRisk)}`} style={{ marginRight: '8px' }}>
                {t(riskDisplayKey(explained.totalRisk))}
              </span>
              <span style={{ fontSize: '11px', color: 'var(--text-dim)' }}>
                {t('search_assistant_scope')}: {t(scopeBreadthKeys[explained.scopeBreadth] ?? 'explainer_scope_moderate')}
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

function precisionBadgeClass(precision: string): string {
  switch (precision) {
    case 'EXACT': return 'badge-low'
    case 'SHORTLIST': return 'badge-info'
    case 'APPROXIMATE': return 'badge-medium'
    default: return 'badge-beta'
  }
}

function riskBadgeClass(level: string): string {
  switch (level) {
    case 'High': return 'badge-high'
    case 'Medium': return 'badge-medium'
    case 'Low': return 'badge-low'
    default: return 'badge-info'
  }
}

function riskDisplayKey(level: string): string {
  switch (level) {
    case 'High': return 'risk_high'
    case 'Medium': return 'risk_medium'
    case 'Low': return 'risk_low'
    default: return 'risk_info'
  }
}

void ({} as ClipboardStatus)
