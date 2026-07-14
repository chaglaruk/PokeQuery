import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { copyToClipboard } from '@ui/clipboard'
import type { ClipboardResult } from '@ui/clipboard'
import { AppIcon } from '@ui/components/SpriteIcon'
import { Dialog } from '@ui/components/Dialog'
import { addHistory } from '@ui/savedSearches'
import { buildPresetOutput, PRESETS, PRESET_CATEGORIES, type Preset } from '@ui/presetCatalog'

export function PresetsScreen({ personal = false }: { personal?: boolean }) {
  const { t, resolvedSearchLanguage } = useI18n()
  const navigate = useNavigate()
  const [preview, setPreview] = useState<Preset | null>(null)
  const [clipboard, setClipboard] = useState<ClipboardResult | null>(null)
  const groups = useMemo(() => PRESET_CATEGORIES.map(category => ({ category, presets: PRESETS.filter(preset => preset.category === category) })), [])
  const previewOutput = useMemo(() => preview ? buildPresetOutput(preview, resolvedSearchLanguage) : null, [preview, resolvedSearchLanguage])

  const copyPreview = async () => {
    if (!preview || !previewOutput || previewOutput.copyBlocked) return
    const result = await copyToClipboard(previewOutput.rawSyntax)
    setClipboard(result)
    if (result.status === 'copied') {
      addHistory({ name: t(preview.titleKey), rawSyntax: previewOutput.rawSyntax, goalId: 'preset', riskLevel: previewOutput.riskLevel })
      setTimeout(() => setClipboard(null), 2000)
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
              <button type="button" className="card preset-card" key={preset.titleKey} onClick={() => { setClipboard(null); setPreview(preset) }}>
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
            <div className="search-string preset-output">{previewOutput?.rawSyntax}</div>
            {(preview.risk === 'medium' || (previewOutput?.warnings.length ?? 0) > 0) && <p className="setting-help preset-advisory"><AppIcon name="warning" size={14} /> {t('presets_review_matches')}</p>}
            {previewOutput?.copyBlocked && <p className="setting-help preset-blocked"><AppIcon name="error" size={14} /> {t('goal_detail_fix_errors')}</p>}
            {clipboard && <div className={`clipboard-feedback ${clipboard.status}`} role="status" aria-live="polite">{t(clipboard.i18nKey)}</div>}
            <div className="dialog-actions">
              <button type="button" className="btn btn-secondary" onClick={() => setPreview(null)}>{t('action_cancel')}</button>
              <button type="button" className="btn btn-primary" onClick={copyPreview} disabled={previewOutput?.copyBlocked}><AppIcon name="copy" size={16} /> {clipboard?.status === 'copied' ? t('goal_detail_copied') : t('goal_detail_copy_search_string')}</button>
            </div>
          </>
        )}
      </Dialog>
    </main>
  )
}
