import { useState, useMemo } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { growthStrings } from '@i18n/growthStrings'
import { explain } from '@engine/searchStringExplainer'
import type { ExplainedToken } from '@engine/searchStringExplainer'
import type { RiskLevel } from '@/types'
import { copyToClipboard, type ClipboardResult } from '@ui/clipboard'
import { addHistory } from '@ui/savedSearches'
import { Dialog } from '@ui/components/Dialog'
import { AppIcon } from '@ui/components/SpriteIcon'

const riskBadgeClass: Record<RiskLevel, string> = {
  Info: 'badge-info',
  Low: 'badge-low',
  Medium: 'badge-medium',
  High: 'badge-high',
}

const precisionLabels: Record<string, string> = {
  'EXACT': 'explainer_prec_exact',
  'SHORTLIST': 'explainer_prec_shortlist',
  'APPROXIMATE': 'explainer_prec_approx',
  'NEEDS_VERIFICATION': 'explainer_prec_needs_verif',
  'UNKNOWN': 'explainer_prec_unknown',
}

const scopeLabels: Record<string, string> = {
  'All (no filter)': 'explainer_scope_all',
  'Very Narrow': 'explainer_scope_very_narrow',
  'Narrow': 'explainer_scope_narrow',
  'Moderate': 'explainer_scope_moderate',
  'Broad': 'explainer_scope_broad',
}

export function ExplainScreen() {
  const { t, locale } = useI18n()
  const growth = growthStrings(locale)
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const sharedQuery = (searchParams.get('query') ?? '').slice(0, 2000)
  const [query, setQuery] = useState(sharedQuery)
  const [clipboard, setClipboard] = useState<ClipboardResult | null>(null)
  const [reviewCopy, setReviewCopy] = useState(false)

  const result = useMemo(() => explain(query), [query])
  const requiresReview = result.totalRisk === 'Medium' || result.totalRisk === 'High'

  const performCopy = async () => {
    const copyResult = await copyToClipboard(result.original)
    setClipboard(copyResult)
    if (copyResult.status === 'copied') {
      addHistory({ name: t('goal_explain'), rawSyntax: result.original, goalId: 'explain', riskLevel: result.totalRisk })
      setTimeout(() => setClipboard(null), 2000)
    }
  }

  const handleCopy = () => {
    if (requiresReview) {
      setReviewCopy(true)
      return
    }
    void performCopy()
  }

  const confirmCopy = () => {
    setReviewCopy(false)
    void performCopy()
  }

  return (
    <div className="page content-with-nav">
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>‹</button>
        <h1>{t('explain_title')}</h1>
      </div>

      <div className="card">
        <p className="text-muted" style={{ marginBottom: '12px' }}>{t('explain_intro')}</p>
        <input
          type="text"
          value={query}
          onChange={e => setQuery(e.target.value)}
          placeholder={t('explain_placeholder')}
          autoFocus
        />
      </div>

      {result.tokens.length > 0 && (
        <>
          <div className="card">
            <div className="section-title" style={{ margin: '0 0 8px' }}>{t('explain_summary')}</div>
            <p style={{ fontSize: '14px', lineHeight: 1.6 }}>
              {locale === 'en' ? result.summary : t('explain_summary_generic')}
            </p>

            <div style={{ display: 'flex', gap: '16px', marginTop: '12px', flexWrap: 'wrap' }}>
              <div>
                <span className="text-muted" style={{ fontSize: '12px' }}>{t('explain_risk_prefix')}</span>
                <span className={`badge ${riskBadgeClass[result.totalRisk]}`} style={{ marginLeft: '4px' }}>
                  {t(`risk_${result.totalRisk.toLowerCase()}`)}
                </span>
              </div>
              <div>
                <span className="text-muted" style={{ fontSize: '12px' }}>{t('explain_precision_prefix')}</span>
                <span style={{ fontSize: '13px', marginLeft: '4px' }}>
                  {t(precisionLabels[result.precision] ?? 'explainer_prec_unknown')}
                </span>
              </div>
              <div>
                <span className="text-muted" style={{ fontSize: '12px' }}>{t('explain_scope_prefix')}</span>
                <span style={{ fontSize: '13px', marginLeft: '4px' }}>
                  {t(scopeLabels[result.scopeBreadth] ?? 'explainer_scope_moderate')}
                </span>
              </div>
            </div>

            {result.hasUnknownTokens && (
              <p style={{ marginTop: '10px', color: 'var(--warning)', fontSize: '12px' }}>
                {t('explain_unknown_warning')}
              </p>
            )}
          </div>

          <div className="card">
            <div className="section-title" style={{ margin: '0 0 10px' }}>{t('explain_tokens_title')}</div>
            {result.tokens.map((tok, i) => (
              <TokenRow key={i} token={tok} t={t} locale={locale} />
            ))}
          </div>

          <div style={{ marginTop: '16px' }}>
            <button className="btn btn-primary" onClick={handleCopy}>
              {clipboard?.status === 'copied' ? `\u2714 ${t('explain_copied')}` : t('explain_copy_search_string')}
            </button>
            {clipboard && <div className={`clipboard-feedback ${clipboard.status}`} role="status" aria-live="polite">{t(clipboard.i18nKey)}</div>}
          </div>
        </>
      )}

      {query.trim().length === 0 && (
        <div className="card">
          <p className="text-muted" style={{ textAlign: 'center' }}>{t('explain_intro')}</p>
        </div>
      )}

      <Dialog open={reviewCopy} title={growth.riskTitle} onClose={() => setReviewCopy(false)} closeLabel={growth.cancel}>
        <p>{growth.riskBody}</p>
        <div className="detail-actions" style={{ marginTop: '16px' }}>
          <button type="button" className="btn btn-copy medium" onClick={confirmCopy}>
            <AppIcon name="copy" size={18} /> {growth.continueCopy}
          </button>
          <button type="button" className="btn btn-edit" onClick={() => setReviewCopy(false)}>{growth.cancel}</button>
        </div>
      </Dialog>
    </div>
  )
}

function TokenRow({ token, t, locale }: { token: ExplainedToken; t: (key: string) => string; locale: string }) {
  const tagLabel = token.isExclusion ? t('explain_exclusion') : t('explain_inclusion')
  const descLabel = locale === 'en'
    ? token.description
    : (token.isExclusion ? t('explain_token_excludes') : t('explain_token_includes'))

  return (
    <div style={{
      padding: '12px 0',
      borderBottom: '1px solid var(--border)',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
        <code style={{
          fontFamily: 'SF Mono, Consolas, monospace',
          fontSize: '14px',
          color: token.isExclusion ? 'var(--danger)' : 'var(--accent)',
          fontWeight: 600,
        }}>
          {token.token}
        </code>
        <span className="badge" style={{
          background: token.isExclusion ? 'rgba(231,76,60,0.15)' : 'rgba(11,140,156,0.15)',
          color: token.isExclusion ? 'var(--danger)' : 'var(--accent)',
        }}>
          {tagLabel}
        </span>
        {token.riskHint !== 'Info' && (
          <span className={`badge ${riskBadgeClass[token.riskHint]}`}>
            {t(`risk_${token.riskHint.toLowerCase()}`)}
          </span>
        )}
      </div>
      <p className="text-muted" style={{ fontSize: '13px' }}>
        {descLabel}
      </p>
    </div>
  )
}
