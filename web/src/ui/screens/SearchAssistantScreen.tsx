import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { parseSearchIntent, type ParsedIntent } from '@engine/searchIntentParser'

export function SearchAssistantScreen() {
  const { t } = useI18n()
  const navigate = useNavigate()
  const [input, setInput] = useState('')
  const [result, setResult] = useState<ParsedIntent | null>(null)
  const [copied, setCopied] = useState(false)

  const handleParse = useCallback(() => {
    if (!input.trim()) {
      setResult(null)
      return
    }
    setResult(parseSearchIntent(input))
    setCopied(false)
  }, [input])

  const handleCopy = useCallback(async () => {
    if (!result?.rawQuery) return
    try {
      await navigator.clipboard.writeText(result.rawQuery)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch { /* clipboard may not be available */ }
  }, [result])

  return (
    <div className="page content-with-nav">
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>‹</button>
        <h1>{t('search_assistant_title') || 'Search Assistant'}</h1>
      </div>

      <div className="card" style={{ marginBottom: '16px' }}>
        <p style={{ fontSize: '15px', fontWeight: 600, marginBottom: '8px' }}>
          {t('search_assistant_empty_state_title')}
        </p>
        <p className="text-muted" style={{ marginBottom: '12px' }}>
          {t('search_assistant_empty_state_desc')}
        </p>
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => { if (e.key === 'Enter') handleParse() }}
          placeholder={t('search_assistant_placeholder') || 'e.g. shiny legendary for great league'}
          style={{ marginBottom: '12px' }}
        />
        <button className="btn btn-primary" onClick={handleParse}>
          {t('search_assistant_parse') || 'Build search'}
        </button>
      </div>

      {result && (
        <div className="card">
          {result.canBuild && result.rawQuery ? (
            <>
              <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-dim)', marginBottom: '6px' }}>
                {t('event_suggested_for_event')}
              </p>
              <div className="search-string" style={{ marginBottom: '12px' }}>{result.rawQuery}</div>
              <button className="btn btn-primary" onClick={handleCopy} style={{ marginBottom: '16px' }}>
                {copied ? t('event_copied') : t('event_copy_search')}
              </button>
            </>
          ) : (
            <p style={{ fontSize: '14px', color: 'var(--text-dim)', marginBottom: '12px', fontStyle: 'italic' }}>
              {result.explanation}
            </p>
          )}

          {result.explanation && result.canBuild && (
            <div style={{ marginBottom: '12px' }}>
              <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-dim)', marginBottom: '4px' }}>
                {t('goal_detail_what_does_this_do') || 'What does this search do?'}
              </p>
              <p className="text-muted" style={{ lineHeight: 1.5, fontSize: '13px' }}>{result.explanation}</p>
            </div>
          )}

          {result.limitations.length > 0 && (
            <div>
              <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--warning)', marginBottom: '4px' }}>
                {t('event_safety_note')}
              </p>
              <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
                {result.limitations.map((lim, i) => (
                  <li key={i} className="text-muted" style={{ fontSize: '12px', lineHeight: 1.5, marginBottom: '4px' }}>
                    {'\u2022'} {lim}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
