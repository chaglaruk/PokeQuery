import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { copyToClipboard, type ClipboardResult } from '@ui/clipboard'
import { AppIcon } from '@ui/components/SpriteIcon'

interface KnowledgeTerm {
  id: string
  title?: string | null
  syntax: string
  category: string
  tier: string
  description_tr: string
  description_en: string
  riskLevel: 'Info' | 'Low' | 'Medium' | 'High'
  sourceUrl: string
  lastVerified: string
  knownQuirks?: string | null
  example?: string | null
  commonMistake?: string | null
}

const categoryKeys: Record<string, string> = {
  'common misconception': 'kb_cat_common_mistake',
  'common misconceptions': 'kb_cat_common_mistake',
  'common mistake': 'kb_cat_common_mistake',
  counter: 'kb_cat_counter', encounter: 'kb_cat_encounter', evolution: 'kb_cat_evolution',
  iv: 'kb_cat_iv', max: 'kb_cat_max', move: 'kb_cat_move', moves: 'kb_cat_move',
  numeric: 'kb_cat_numeric', operator: 'kb_cat_operator', operators: 'kb_cat_operator',
  size: 'kb_cat_size', status: 'kb_cat_status', tag: 'kb_cat_tag', tags: 'kb_cat_tag',
}

export function KnowledgeScreen() {
  const { t, locale } = useI18n()
  const navigate = useNavigate()
  const [terms, setTerms] = useState<KnowledgeTerm[] | null>(null)
  const [loadFailed, setLoadFailed] = useState(false)
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('All')
  const [expanded, setExpanded] = useState<string | null>(null)
  const [clipboard, setClipboard] = useState<ClipboardResult | null>(null)

  useEffect(() => {
    fetch(`${import.meta.env.BASE_URL}knowledgebase.json`)
      .then(response => {
        if (!response.ok) throw new Error(String(response.status))
        return response.json() as Promise<KnowledgeTerm[]>
      })
      .then(setTerms)
      .catch(() => setLoadFailed(true))
  }, [])

  const categories = useMemo(() => ['All', ...new Set((terms ?? []).map(term => term.category).sort())], [terms])
  const filtered = useMemo(() => {
    const needle = query.trim().toLocaleLowerCase(locale)
    return (terms ?? []).filter(term => {
      const matchesCategory = category === 'All' || term.category === category
      const description = locale === 'tr' ? term.description_tr : term.description_en
      return matchesCategory && (!needle || [term.syntax, term.title ?? '', description, term.category].some(value => value.toLocaleLowerCase(locale).includes(needle)))
    })
  }, [terms, query, category, locale])

  const categoryLabel = (value: string) => value === 'All' ? t('knowledge_all') : t(categoryKeys[value.toLowerCase()] ?? value)
  const copyToken = async (syntax: string) => {
    const result = await copyToClipboard(syntax)
    setClipboard(result)
    if (result.status === 'copied') setTimeout(() => setClipboard(null), 2000)
  }

  return (
    <main className="page content-with-nav knowledge-page">
      <header className="page-header">
        <button type="button" className="back-btn" onClick={() => navigate('/')} aria-label={t('back')}>‹</button>
        <h1>{t('knowledge_title')}</h1>
      </header>
      <div className="knowledge-warning"><span />{t('knowledge_beta_warning')}</div>
      <label className="knowledge-search">
        <AppIcon name="search" size={18} />
        <input type="text" value={query} onChange={event => setQuery(event.target.value)} placeholder={t('knowledge_search_placeholder')} />
      </label>
      <div className="knowledge-categories" role="list">
        {categories.map(value => (
          <button type="button" className={`knowledge-chip ${category === value ? 'active' : ''}`} key={value} onClick={() => setCategory(value)}>{categoryLabel(value)}</button>
        ))}
      </div>

      {clipboard && <div className={`clipboard-feedback ${clipboard.status}`} role="status" aria-live="polite">{t(clipboard.i18nKey)}</div>}
      {!terms && !loadFailed && <div className="knowledge-loading" role="status" />}
      {loadFailed && <p className="knowledge-error">{t('knowledge_load_error')}</p>}
      {terms && filtered.length === 0 && (
        <section className="saved-empty"><AppIcon name="search" size={40} /><h2>{t('knowledge_empty_title')}</h2><p>{t('knowledge_empty_subtitle')}</p></section>
      )}
      <div className="knowledge-list">
        {filtered.map(term => {
          const isExpanded = expanded === term.id
          const description = locale === 'tr' ? term.description_tr : term.description_en
          return (
            <article className="knowledge-term" key={term.id}>
              <button type="button" className="knowledge-term-toggle" onClick={() => setExpanded(isExpanded ? null : term.id)} aria-expanded={isExpanded}>
                <span><strong>{term.title || term.syntax}</strong><code>{term.syntax}</code></span>
                <span className={`badge badge-${term.riskLevel.toLowerCase()}`}>{t(`risk_${term.riskLevel.toLowerCase()}_display`)}</span>
                <AppIcon name="chevron" size={17} className={isExpanded ? 'rotated' : ''} />
              </button>
              {isExpanded && (
                <div className="knowledge-term-detail">
                  <p>{description}</p>
                  {term.example && <p>{t('knowledge_example', term.example)}</p>}
                  {term.commonMistake && <p className="knowledge-caution">{t('knowledge_common_mistake', term.commonMistake)}</p>}
                  {term.knownQuirks && <p className="knowledge-caution">{t('knowledge_note', term.knownQuirks)}</p>}
                  <p>{t('knowledge_last_verified', term.lastVerified)}</p>
                  <button type="button" className="btn btn-primary" onClick={() => copyToken(term.syntax)}><AppIcon name="copy" size={16} />{t('knowledge_copy_token')}</button>
                </div>
              )}
            </article>
          )
        })}
      </div>
    </main>
  )
}
