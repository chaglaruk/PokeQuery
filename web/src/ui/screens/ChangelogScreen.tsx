import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { changelogEntries, type ChangelogEntry } from '@/changelog'

export function ChangelogScreen() {
  const { t, locale } = useI18n()
  const navigate = useNavigate()

  // Turkish UI mirrors the Android behaviour: only show the current entry,
  // because past-entry highlights are English-only.
  const entries = locale === 'tr'
    ? changelogEntries.filter(e => e.isCurrent)
    : changelogEntries

  return (
    <div className="page content-with-nav">
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>‹</button>
        <h1>{t('goal_changelog')}</h1>
      </div>

      <div className="card" style={{ borderColor: 'var(--accent)' }}>
        <p style={{ fontWeight: 700, color: 'var(--accent)' }}>{t('settings_safety_stance')}</p>
        <p style={{ marginTop: '8px', fontSize: '13px', lineHeight: 1.4 }}>
          {t('settings_safety_disclaimer')}
        </p>
      </div>

      {entries.map(entry => (
        <ChangelogCard key={entry.versionName} entry={entry} />
      ))}
    </div>
  )
}

function ChangelogCard({ entry }: { entry: ChangelogEntry }) {
  const { t } = useI18n()

  // For the current entry, read from i18n keys; for past entries, use the stored strings.
  const highlights = entry.isCurrent
    ? [t('what_changed_v066_b1'), t('what_changed_v066_b2'), t('what_changed_v066_b3')]
    : entry.highlights
  const safetyNotes = entry.isCurrent
    ? [t('what_changed_v066_safety1'), t('what_changed_v066_safety2'), t('what_changed_v066_safety3')]
    : entry.safetyNotes
  const testerNotes = entry.isCurrent
    ? [t('what_changed_v066_tester1'), t('what_changed_v066_tester2'), t('what_changed_v066_tester3')]
    : entry.testerNotes

  return (
    <div className="card" style={{ borderColor: entry.isCurrent ? 'var(--accent)' : 'var(--border)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ flex: 1 }}>
          <p style={{ fontWeight: 700, fontSize: '17px' }}>
            v{entry.versionName} ({entry.versionCode})
          </p>
          <p className="text-muted" style={{ marginTop: '2px' }}>
            {entry.isCurrent
              ? t('what_changed_v066_subtitle')
              : `${entry.releaseLabel} • ${entry.title}`}
          </p>
        </div>
        {entry.isCurrent && (
          <span style={{ color: 'var(--accent)', fontWeight: 700, fontSize: '12px' }}>
            {t('changelog_current')}
          </span>
        )}
      </div>

      <div style={{ marginTop: '14px' }}>
        {highlights.map((h, i) => (
          <p key={i} style={{ fontSize: '13px', lineHeight: 1.4, marginTop: i > 0 ? '4px' : 0 }}>
            {h}
          </p>
        ))}
      </div>

      {safetyNotes.length > 0 && (
        <div style={{ marginTop: '10px' }}>
          <p style={{ color: 'var(--warning)', fontWeight: 600, fontSize: '12px' }}>
            {t('changelog_safety_notes')}
          </p>
          {safetyNotes.map((n, i) => (
            <p key={i} className="text-muted" style={{ fontSize: '12px', marginTop: '3px' }}>
              {n}
            </p>
          ))}
        </div>
      )}

      {testerNotes.length > 0 && (
        <div style={{ marginTop: '10px' }}>
          <p style={{ color: 'var(--accent)', fontWeight: 600, fontSize: '12px' }}>
            {t('changelog_tester_notes')}
          </p>
          {testerNotes.map((n, i) => (
            <p key={i} className="text-muted" style={{ fontSize: '12px', marginTop: '3px' }}>
              {n}
            </p>
          ))}
        </div>
      )}
    </div>
  )
}
