import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { useEventFeed, useLocalizedEvent } from '@event/useEventFeed'
import type { EventFeedEntry } from '@/types'

export function EventsScreen() {
  const { t, locale } = useI18n()
  const navigate = useNavigate()
  const { events, source, loading, error, lastChecked, refresh } = useEventFeed(locale)
  const [expandedId, setExpandedId] = useState<string | null>(null)

  const sourceLabel = source === 'online' ? t('event_status_live_feed')
    : source === 'cached' ? t('event_status_saved_guide')
    : source === 'fallback' ? t('event_status_bundled_fallback')
    : ''

  return (
    <div className="page content-with-nav">
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>‹</button>
        <h1>{t('events_title')}</h1>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
        <button className="btn btn-secondary" onClick={refresh} style={{ width: 'auto', padding: '8px 16px', fontSize: '14px' }}>
          {t('events_refresh')}
        </button>
        {source && (
          <span className="badge badge-info">{sourceLabel}</span>
        )}
      </div>

      {lastChecked && (
        <p className="text-muted" style={{ marginBottom: '12px' }}>
          {t('event_context_last_checked', new Date(lastChecked).toLocaleString())}
        </p>
      )}

      {loading && (
        <div className="card" style={{ textAlign: 'center' }}>
          <p className="text-dim">Loading events…</p>
        </div>
      )}

      {error && (
        <div className="card" style={{ borderColor: 'var(--danger)' }}>
          <p style={{ color: 'var(--danger)', fontSize: '14px' }}>{error}</p>
        </div>
      )}

      {!loading && !error && events.length === 0 && (
        <div className="card">
          <p className="text-dim">No events found.</p>
        </div>
      )}

      {events.map(event => (
        <EventCard
          key={event.id}
          event={event}
          expanded={expandedId === event.id}
          onToggle={() => setExpandedId(prev => prev === event.id ? null : event.id)}
        />
      ))}

      <div className="card" style={{ marginTop: '16px' }}>
        <p className="text-muted">{t('event_context_disclaimer')}</p>
      </div>
    </div>
  )
}

function EventCard({ event, expanded, onToggle }: {
  event: EventFeedEntry
  expanded: boolean
  onToggle: () => void
}) {
  const { t, locale } = useI18n()
  const localized = useLocalizedEvent(event, locale)
  const [copied, setCopied] = useState(false)

  const handleCopy = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(event.suggestedSearch)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch { /* clipboard API may not be available */ }
  }, [event.suggestedSearch])

  if (!localized) return null

  const statusLabel = event.status === 'CURRENT' ? t('event_label_current')
    : event.status === 'UPCOMING' ? t('event_label_upcoming')
    : event.status

  const tierBadge = event.importanceTier === 'MAJOR' ? 'badge-medium'
    : event.importanceTier === 'STANDARD' ? 'badge-info'
    : 'badge-beta'

  const dateRange = (event.startDate || event.start) && (event.endDate || event.end)
    ? `${event.startDate || event.start} – ${event.endDate || event.end}`
    : event.month && event.year
    ? t('event_month_year', event.month, event.year)
    : null

  return (
    <div className="card card-tap" onClick={onToggle}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
        <div style={{ flex: 1 }}>
          <p style={{ fontSize: '16px', fontWeight: 600, lineHeight: 1.3 }}>{localized.title}</p>
          {dateRange && (
            <p className="text-muted" style={{ marginTop: '4px', fontSize: '13px' }}>{dateRange}</p>
          )}
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', alignItems: 'flex-end' }}>
          <span className="badge badge-low">{statusLabel}</span>
          {event.importanceTier && (
            <span className={`badge ${tierBadge}`}>{event.importanceTier}</span>
          )}
        </div>
      </div>

      {expanded && (
        <div style={{ marginTop: '12px' }} onClick={e => e.stopPropagation()}>
          <p className="text-muted" style={{ marginBottom: '10px', lineHeight: 1.5 }}>{localized.summary}</p>

          {localized.eventNotes && (
            <div style={{ marginBottom: '10px' }}>
              <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-dim)', marginBottom: '4px' }}>{t('event_card_notes')}</p>
              <p className="text-muted" style={{ lineHeight: 1.5 }}>{localized.eventNotes}</p>
            </div>
          )}

          {localized.prep && (
            <div style={{ marginBottom: '10px' }}>
              <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-dim)', marginBottom: '4px' }}>{t('event_prep_guidance')}</p>
              <p className="text-muted" style={{ lineHeight: 1.5 }}>{localized.prep}</p>
            </div>
          )}

          <div style={{ marginTop: '12px' }}>
            <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-dim)', marginBottom: '6px' }}>{t('event_card_search')}</p>
            <div className="search-string" style={{ fontSize: '13px' }}>{event.suggestedSearch}</div>
            <button
              className="btn btn-primary"
              onClick={handleCopy}
              style={{ marginTop: '10px' }}
            >
              {copied ? `✅ ${t('event_card_copy_search')}` : t('event_card_copy_search')}
            </button>
          </div>

          <p className="text-muted" style={{ marginTop: '12px', fontSize: '12px' }}>
            {t('event_safety_note')}
          </p>
        </div>
      )}
    </div>
  )
}
