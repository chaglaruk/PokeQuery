import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { copyToClipboard } from '@ui/clipboard'
import { AppIcon } from '@ui/components/SpriteIcon'
import { Dialog } from '@ui/components/Dialog'

type PresetRisk = 'low' | 'medium' | 'info'
type PresetCategory = 'cleanup' | 'candy_event' | 'trading' | 'battle_iv' | 'collection'

interface Preset {
  titleKey: string
  descKey: string
  syntax: string
  risk: PresetRisk
  category: PresetCategory
}

const PRESETS: Preset[] = [
  { titleKey: 'preset_recent_catches', descKey: 'preset_desc_recent_catches', syntax: 'age0-7', risk: 'low', category: 'cleanup' },
  { titleKey: 'preset_low_iv_cleanup', descKey: 'preset_desc_low_iv_cleanup', syntax: '0*,1*&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground', risk: 'medium', category: 'cleanup' },
  { titleKey: 'preset_duplicate_cleanup', descKey: 'preset_desc_duplicate_cleanup', syntax: 'count2-&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground', risk: 'medium', category: 'cleanup' },
  { titleKey: 'preset_untagged_review', descKey: 'preset_desc_untagged_review', syntax: '!#&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!traded&!costume&!background&!locationbackground&!specialbackground', risk: 'low', category: 'cleanup' },
  { titleKey: 'preset_evolve_ready', descKey: 'preset_desc_evolve_ready', syntax: 'evolve', risk: 'low', category: 'candy_event' },
  { titleKey: 'preset_recent_event_review', descKey: 'preset_desc_recent_event_review', syntax: 'age0-3', risk: 'low', category: 'candy_event' },
  { titleKey: 'preset_untraded_duplicates', descKey: 'preset_desc_untraded_duplicates', syntax: 'count2-&!traded', risk: 'medium', category: 'trading' },
  { titleKey: 'preset_older_untraded', descKey: 'preset_desc_older_untraded', syntax: 'age365-&!traded', risk: 'medium', category: 'trading' },
  { titleKey: 'preset_distance_trade', descKey: 'preset_desc_distance_trade', syntax: 'distance100-&!traded', risk: 'medium', category: 'trading' },
  { titleKey: 'preset_special_trade', descKey: 'preset_desc_special_trade', syntax: 'shiny,legendary,mythical', risk: 'info', category: 'trading' },
  { titleKey: 'preset_hundo', descKey: 'preset_desc_hundo', syntax: '4*', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_nundo', descKey: 'preset_desc_nundo', syntax: '0attack&0defense&0hp', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_great_league', descKey: 'preset_desc_great_league', syntax: '0-1attack&3-4defense&3-4hp&cp-1500', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_ultra_league', descKey: 'preset_desc_ultra_league', syntax: '0-1attack&3-4defense&3-4hp&cp-2500', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_perfect_shadows', descKey: 'preset_desc_perfect_shadows', syntax: 'shadow&4*', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_shiny_review', descKey: 'preset_desc_shiny_review', syntax: 'shiny', risk: 'info', category: 'collection' },
  { titleKey: 'preset_costume_review', descKey: 'preset_desc_costume_review', syntax: 'costume', risk: 'info', category: 'collection' },
  { titleKey: 'preset_lucky_review', descKey: 'preset_desc_lucky_review', syntax: 'lucky', risk: 'info', category: 'collection' },
]

const CATEGORIES: PresetCategory[] = ['cleanup', 'candy_event', 'trading', 'battle_iv', 'collection']

export function PresetsScreen({ personal = false }: { personal?: boolean }) {
  const { t } = useI18n()
  const navigate = useNavigate()
  const [preview, setPreview] = useState<Preset | null>(null)
  const [copied, setCopied] = useState(false)
  const groups = useMemo(() => CATEGORIES.map(category => ({ category, presets: PRESETS.filter(preset => preset.category === category) })), [])

  const copyPreview = async () => {
    if (!preview) return
    const result = await copyToClipboard(preview.syntax)
    if (result.status === 'copied') {
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    }
  }

  return (
    <main className="page content-with-nav">
      <header className="page-header">
        <button type="button" className="back-btn" onClick={() => navigate('/')} aria-label="Back">←</button>
        <h1>{t(personal ? 'goal_my_presets' : 'goal_presets')}</h1>
      </header>
      <p className="text-dim" style={{ margin: '-4px 0 16px', fontSize: '11px' }}>{t(personal ? 'goal_my_presets_desc' : 'presets_intro')}</p>

      {personal ? (
        <section className="card" style={{ textAlign: 'center', padding: '40px 20px' }}>
          <AppIcon name="copy" size={38} />
          <h2 style={{ marginTop: '12px', fontSize: '16px' }}>{t('my_presets_empty_title')}</h2>
          <p className="text-dim" style={{ marginTop: '6px' }}>{t('goal_my_presets_desc')}</p>
        </section>
      ) : groups.map(group => (
        <section key={group.category}>
          <h2 className="section-title">{t(`preset_cat_${group.category}`)}</h2>
          <div className="preset-grid">
            {group.presets.map(preset => (
              <button type="button" className="card preset-card" key={preset.titleKey} onClick={() => { setCopied(false); setPreview(preset) }}>
                <span className="preset-card-top">
                  <span className="preset-letter">{t(preset.titleKey).slice(0, 1)}</span>
                  <span className={`badge badge-${preset.risk}`}>{t(`risk_${preset.risk}_display`)}</span>
                </span>
                <strong className="preset-title">{t(preset.titleKey)}</strong>
                <span className="preset-desc">{t(preset.descKey)}</span>
                <span className="preset-preview-link">{t('presets_tap_preview')}</span>
              </button>
            ))}
          </div>
        </section>
      ))}

      <Dialog open={preview !== null} title={preview ? t(preview.titleKey) : ''} onClose={() => setPreview(null)} closeLabel={t('action_cancel')}>
        {preview && (
          <>
            <p className="preset-preview-kicker">{t('presets_preview')}</p>
            <h3 style={{ marginTop: '8px', fontSize: '13px' }}>{t('presets_what_finds')} <span className={`badge badge-${preview.risk}`}>{t(`risk_${preview.risk}_display`)}</span></h3>
            <p className="text-dim" style={{ marginTop: '6px', fontSize: '11px' }}>{t(preview.descKey)}</p>
            <div className="search-string" style={{ marginTop: '12px' }}>{preview.syntax}</div>
            {preview.risk === 'medium' && <p className="setting-help" style={{ color: 'var(--warning)', display: 'flex', gap: '6px' }}><AppIcon name="warning" size={14} /> {t('presets_review_matches')}</p>}
            <div className="dialog-actions">
              <button type="button" className="btn btn-secondary" onClick={() => setPreview(null)}>{t('action_cancel')}</button>
              <button type="button" className="btn btn-primary" onClick={copyPreview}><AppIcon name="copy" size={16} /> {copied ? t('goal_detail_copied') : t('goal_detail_copy_search_string')}</button>
            </div>
          </>
        )}
      </Dialog>
    </main>
  )
}
