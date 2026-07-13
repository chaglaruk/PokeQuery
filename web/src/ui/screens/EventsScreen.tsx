import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { useEventFeed, useLocalizedEvent } from '@event/useEventFeed'
import {
  groupEvents,
  remainingTimeLabel,
  dateLabel,
  systemClock,
} from '@event/eventLifecycle'
import { getLocalized } from '@event/eventFeedService'
import type { EventFeedEntry, EventPokemonEntry, LocaleCode } from '@/types'

// Sprite src helper — maps spriteKey to public/sprites/event_*.png
function spriteSrc(key: string | null): string | null {
  if (!key) return null
  const known = [
    'mewtwo', 'zeraora', 'pikachu', 'necrozma', 'eevee', 'wurmple',
    'unown', 'kangaskhan', 'mr_mime', 'heracross', 'corsola', 'gimmighoul',
  ]
  if (known.includes(key)) {
    return `${import.meta.env.BASE_URL}sprites/event_${key}.png`
  }
  return null
}

// Theme tone color per themeKey
function themeTone(themeKey: string): string {
  switch (themeKey) {
    case 'candy_bonus': return 'var(--warning)'
    case 'trade_bonus': return 'var(--info)'
    case 'raid': return 'var(--text-dim)'
    case 'spotlight_hour': return 'var(--accent)'
    case 'community_day': return 'var(--accent-hover)'
    default: return 'var(--accent)'
  }
}

// Category → compact chip label + color
function categoryChip(event: EventFeedEntry, lang: string): { label: string; color: string } {
  const cat = determineCategoryLabel(event)
  const t: Record<string, Record<string, string>> = {
    MAJOR_GAMEPLAY: { en: 'Major', tr: 'Buyuk', de: 'Gross', es: 'Mayor', fr: 'Majeur', it: 'Maggiore' },
    LIMITED_GAMEPLAY: { en: 'Limited', tr: 'Sinirli', de: 'Begrenzt', es: 'Limitado', fr: 'Limite', it: 'Limitato' },
    ROUTINE_ROTATION: { en: 'Rotation', tr: 'Rotasyon', de: 'Rotation', es: 'Rotacion', fr: 'Rotation', it: 'Rotazione' },
    RAID_ROTATION: { en: 'Rotation', tr: 'Rotasyon', de: 'Rotation', es: 'Rotacion', fr: 'Rotation', it: 'Rotazione' },
    SEASON_GBL: { en: 'Rotation', tr: 'Rotasyon', de: 'Rotation', es: 'Rotacion', fr: 'Rotation', it: 'Rotazione' },
    NEWS_PROMO: { en: 'News', tr: 'Duyuru', de: 'News', es: 'News', fr: 'News', it: 'News' },
    REWARD_DROP: { en: 'News', tr: 'Duyuru', de: 'News', es: 'News', fr: 'News', it: 'News' },
    ANNOUNCEMENT: { en: 'News', tr: 'Duyuru', de: 'News', es: 'News', fr: 'News', it: 'News' },
  }
  const colors: Record<string, string> = {
    MAJOR_GAMEPLAY: 'var(--accent)',
    LIMITED_GAMEPLAY: 'var(--warning)',
    ROUTINE_ROTATION: 'var(--info)',
    RAID_ROTATION: 'var(--info)',
    SEASON_GBL: 'var(--info)',
  }
  return { label: t[cat]?.[lang] ?? cat, color: colors[cat] ?? 'var(--text-dim)' }
}

function determineCategoryLabel(event: EventFeedEntry): string {
  const cat = (event.eventCategory ?? '').toUpperCase().trim()
  if (cat) return cat
  const title = (event.title ?? '').toLowerCase()
  const kind = (event.themeKey ?? '').toLowerCase()
  if (title.includes('gbl') || title.includes('go battle league') || title.includes('league') || title.includes('cup')) return 'SEASON_GBL'
  if (title.includes('spotlight hour') || title.includes('raid hour') || title.includes('max monday') || kind === 'spotlight_hour') return 'ROUTINE_ROTATION'
  if (!title.includes('raid day') && (title.includes('5-star') || title.includes('mega raid') || title.includes('shadow raid') || title.includes('raid rotation'))) return 'RAID_ROTATION'
  if (title.includes('go fest') || title.includes('go tour') || title.includes('safari zone') || title.includes('community day') || title.includes('road of legends') || kind === 'community_day') return 'MAJOR_GAMEPLAY'
  if (title.includes('twitch drops') || title.includes('prime gaming') || title.includes('reward') || title.includes('drop')) return 'REWARD_DROP'
  if (title.includes('save the date') || title.includes('wallpapers') || title.includes('diary') || title.includes('promo') || title.includes('store') || title.includes('coupon') || title.includes('code')) return 'NEWS_PROMO'
  if (title.includes('announcement') || title.includes('partnership') || title.includes('showcase') || title.includes('professor willow') || title.includes('scopely')) return 'ANNOUNCEMENT'
  return 'LIMITED_GAMEPLAY'
}

// Localized title (reuses getLocalized with new fields)
function eventLocaleTitle(entry: EventFeedEntry, locale: LocaleCode): string {
  return getLocalized(entry, locale).title
}

// Localized pokemon name
function pokeLocalizedName(entry: EventPokemonEntry, locale: LocaleCode): string {
  const suffix: Record<string, string | null> = { en: null, tr: 'Tr', de: 'De', es: 'Es', fr: 'Fr', it: 'It' }
  const s = suffix[locale]
  if (!s) return entry.name
  const key = `name${s}` as keyof EventPokemonEntry
  const val = entry[key] as string | null | undefined
  return (val && val.length > 0) ? val : entry.name
}

function pokeLocalizedBadges(entry: EventPokemonEntry, locale: LocaleCode): string {
  const suffix: Record<string, string | null> = { en: null, tr: 'Tr', de: 'De', es: 'Es', fr: 'Fr', it: 'It' }
  const s = suffix[locale]
  let val: string | string[] | null | undefined
  if (!s) {
    val = entry.badges
  } else {
    const key = `badges${s}` as keyof EventPokemonEntry
    val = entry[key] as string | string[] | null | undefined
    if (!val) val = entry.badges
  }
  if (!val) return ''
  return typeof val === 'string' ? val : val.join(', ')
}

export function EventsScreen() {
  const { t, locale } = useI18n()
  const navigate = useNavigate()
  const { events: allEvents, source, loading, error, lastChecked, refresh } = useEventFeed(locale)
  const [dialogs, setDialogs] = useState<Record<string, { entry?: EventPokemonEntry; kind?: string }>>({})
  const clock = systemClock

  // Use full groupEvents for parity with Android Event Guide sections.
  const sections = groupEvents(allEvents, clock)

  const sourceLabel = source === 'online' ? t('event_status_live_feed')
    : source === 'cached' ? t('event_status_saved_guide')
    : source === 'fallback' ? t('event_status_bundled_fallback')
    : ''
  const isLoading = loading && sections.allActive.length === 0

  return (
    <div className="page content-with-nav">
      {/* Header */}
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>‹</button>
        <h1 style={{ flex: 1 }}>{t('events_title')}</h1>
        <button
          className="btn btn-secondary"
          onClick={refresh}
          disabled={loading && sections.allActive.length > 0}
          style={{ width: 'auto', padding: '6px 14px', fontSize: '13px' }}
        >
          <span style={{ marginRight: '4px', fontSize: '16px', lineHeight: 1 }}>{'\u21bb'}</span>
          {t('events_refresh')}
        </button>
      </div>

      {/* Source + lastChecked status line */}
      {source && (
        <div style={{ marginBottom: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
            <span style={{ width: '7px', height: '7px', borderRadius: '50%', background: 'var(--accent)', display: 'inline-block' }} />
            <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--accent)' }}>{sourceLabel}</span>
          </div>
          {lastChecked && (
            <p style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
              {t('event_context_last_checked', new Date(lastChecked).toLocaleString())}
            </p>
          )}
        </div>
      )}

      {/* Loading spinner (only when empty) */}
      {isLoading && (
        <div style={{ textAlign: 'center', padding: '48px 0' }}>
          <div style={{
            width: '32px', height: '32px', border: '3px solid var(--border)',
            borderTopColor: 'var(--accent)', borderRadius: '50%',
            animation: 'spin 0.8s linear infinite', margin: '0 auto',
          }} />
        </div>
      )}

      {error && (
        <div className="card" style={{ borderColor: 'var(--danger)' }}>
          <p style={{ color: 'var(--danger)', fontSize: '14px' }}>{error}</p>
        </div>
      )}

      {!isLoading && !error && sections.allActive.length === 0 && (
        <div className="card" style={{ textAlign: 'center' }}>
          <p style={{ fontSize: '15px', fontWeight: 700 }}>{t('event_no_events_title')}</p>
          <p className="text-muted" style={{ marginTop: '6px' }}>{t('event_no_events_desc')}</p>
          <button className="btn btn-primary" onClick={refresh} style={{ marginTop: '12px', width: 'auto', display: 'inline-flex' }}>
            {t('events_refresh')}
          </button>
        </div>
      )}

      {/* ── Featured hero card ── */}
      {sections.featured && (
        <>
          <div className="section-title">{t('event_section_featured')}</div>
          <EventMainCard
            event={sections.featured}
            sourceLabel={sourceLabel}
            lastChecked={lastChecked}
            clock={clock}
            locale={locale}
            onOpenPokemon={(entry) => setDialogs(d => ({ ...d, [entry.name]: { entry } }))}
          />
        </>
      )}

      {/* ── Happening Now ── */}
      {sections.happeningNow.length > 0 && (
        <>
          <div className="section-title">{t('event_section_live')}</div>
          {sections.happeningNow.slice(0, 3).map(event => (
            <CompactEventCard
              key={`now-${event.id}`}
              event={event}
              clock={clock}
              locale={locale}
              statusLabel={locale === 'tr' ? 'Canli' : 'Live'}
              statusColor="var(--accent)"
              onClick={() => setDialogs(d => ({ ...d, [event.id]: { kind: 'detail' } }))}
            />
          ))}
        </>
      )}

      {/* ── Important Upcoming ── */}
      {sections.importantUpcoming.length > 0 && (
        <>
          <div className="section-title">{t('event_section_upcoming')}</div>
          {sections.importantUpcoming.map(event => (
            <CompactEventCard
              key={`up-${event.id}`}
              event={event}
              clock={clock}
              locale={locale}
              statusLabel={locale === 'tr' ? 'Yakinda' : 'Upcoming'}
              statusColor="var(--warning)"
              onClick={() => setDialogs(d => ({ ...d, [event.id]: { kind: 'detail' } }))}
            />
          ))}
        </>
      )}

      {/* ── Rotations ── */}
      {sections.rotations.length > 0 && (
        <>
          <div className="section-title">{t('event_section_rotations')}</div>
          {sections.rotations.map(event => (
            <CompactEventCard
              key={`rot-${event.id}`}
              event={event}
              clock={clock}
              locale={locale}
              statusLabel={locale === 'tr' ? 'Rotasyon' : 'Rotation'}
              statusColor="var(--info)"
              onClick={() => setDialogs(d => ({ ...d, [event.id]: { kind: 'detail' } }))}
            />
          ))}
        </>
      )}

      {/* ── News ── */}
      {sections.news.length > 0 && (
        <>
          <div className="section-title">{t('event_section_news')}</div>
          {sections.news.map(event => (
            <CompactEventCard
              key={`news-${event.id}`}
              event={event}
              clock={clock}
              locale={locale}
              statusLabel={locale === 'tr' ? 'Duyuru' : 'News'}
              statusColor="var(--text-dim)"
              onClick={() => setDialogs(d => ({ ...d, [event.id]: { kind: 'detail' } }))}
            />
          ))}
        </>
      )}

      {/* ── All Active ── */}
      {sections.allActive.length > 0 && (
        <>
          <div className="section-title">{t('event_section_all')}</div>
          {sections.allActive.map(event => (
            <CompactEventCard
              key={`all-${event.id}`}
              event={event}
              clock={clock}
              locale={locale}
              statusLabel={categoryChip(event, locale).label}
              statusColor={categoryChip(event, locale).color}
              onClick={() => setDialogs(d => ({ ...d, [event.id]: { kind: 'detail' } }))}
            />
          ))}
        </>
      )}

      {/* Disclaimer */}
      <div style={{ marginTop: '16px', padding: '12px 0' }}>
        <p style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: 1.5 }}>
          {t('event_context_disclaimer')}
        </p>
      </div>

      {Object.keys(dialogs).length > 0 && (
        <div style={{ display: 'none' }} /> /* placeholder until dialog overlay wired */
      )}
    </div>
  )
}

/* ──────────────── EventMainCard (Featured Hero) ──────────────── */

function EventMainCard({
  event,
  sourceLabel,
  lastChecked,
  clock,
  locale,
  onOpenPokemon,
}: {
  event: EventFeedEntry
  sourceLabel: string
  lastChecked: string | null
  clock: { todayIso: () => string; nowMillis: () => number }
  locale: LocaleCode
  onOpenPokemon: (entry: EventPokemonEntry) => void
}) {
  const { t } = useI18n()
  const localized = useLocalizedEvent(event, locale)
  const [copied, setCopied] = useState(false)
  const tone = themeTone(event.themeKey)
  const timerLabel = remainingTimeLabel(event, clock, locale)
  const dLabel = dateLabel(event, locale)
  const search = event.suggestedSearch ?? ''
  const pokemon = event.pokemon ?? []

  const handleCopy = useCallback(async () => {
    if (!search) return
    try { await navigator.clipboard.writeText(search); setCopied(true); setTimeout(() => setCopied(false), 2000) } catch { /* no clipboard */ }
  }, [search])

  if (!localized) return null

  return (
    <div className="card" style={{ borderColor: tone, position: 'relative' }}>
      {/* Timer badge + theme mark */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
        <span className="badge" style={{ background: `${tone}22`, color: tone }}>{timerLabel}</span>
        <div style={{ width: '40px', height: '40px', borderRadius: '12px', background: `${tone}1a`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <span style={{ fontSize: '18px', opacity: 0.6 }}>{'\u2728'}</span>
        </div>
      </div>

      {/* Title + date */}
      <p style={{ fontSize: '19px', fontWeight: 700, lineHeight: 1.3, marginBottom: '4px' }}>{eventLocaleTitle(event, locale)}</p>
      {dLabel && <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '8px' }}>{dLabel}</p>}

      {/* Summary */}
      {localized.summary && localized.summary.length > 0 && (
        <p style={{ fontSize: '13px', color: 'var(--text-dim)', lineHeight: 1.4, marginBottom: '10px' }}>{localized.summary}</p>
      )}

      {/* Pokemon sprite row */}
      {pokemon.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginBottom: '10px' }}>
          {pokemon.slice(0, 6).map((p: EventPokemonEntry, idx: number) => (
            <div
              key={`sprite-${idx}`}
              className="card"
              style={{ padding: '8px', cursor: 'pointer', textAlign: 'center' }}
              onClick={() => onOpenPokemon(p)}
            >
              {p.spriteKey ? (
                <img src={spriteSrc(p.spriteKey) ?? ''} alt={pokeLocalizedName(p, locale)}
                  style={{ width: '48px', height: '48px', margin: '0 auto', display: 'block' }} />
              ) : (
                <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: `${tone}1a`, margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <span style={{ fontSize: '22px' }}>?</span>
                </div>
              )}
              <p style={{ fontSize: '12px', fontWeight: 700, marginTop: '4px', color: tone }}>
                {pokeLocalizedName(p, locale)}
              </p>
              <p style={{ fontSize: '9px', color: 'var(--text-muted)' }}>
                {pokeLocalizedBadges(p, locale)}
              </p>
            </div>
          ))}
        </div>
      )}

      {/* Suggested search + copy */}
      {search ? (
        <>
          <p style={{ fontSize: '11px', fontWeight: 700, color: tone, marginBottom: '4px' }}>
            {t('event_suggested_for_event')}
          </p>
          <div className="search-string" style={{ fontSize: '12px', padding: '10px 12px', marginBottom: '8px' }}>{search}</div>
          <button className="btn btn-primary" onClick={handleCopy} style={{ padding: '8px 12px', fontSize: '13px', width: 'auto' }}>
            {copied ? t('event_copied') : t('event_copy_search')}
          </button>
        </>
      ) : (
        <p style={{ fontSize: '13px', color: 'var(--text-dim)', fontStyle: 'italic' }}>
          {t('event_detail_limited')}
        </p>
      )}

      {/* Source badge in bottom-right */}
      <div style={{ marginTop: '12px', textAlign: 'right', opacity: 0.7 }}>
        <span className="badge badge-beta">{sourceLabel}</span>
        {lastChecked && <span style={{ fontSize: '10px', color: 'var(--text-muted)', marginLeft: '6px' }}>{t('event_context_last_checked', lastChecked)}</span>}
      </div>
    </div>
  )
}

/* ──────────────── CompactEventCard ──────────────── */

function CompactEventCard({
  event,
  clock,
  locale,
  statusLabel,
  statusColor,
  onClick,
}: {
  event: EventFeedEntry
  clock: { todayIso: () => string; nowMillis: () => number }
  locale: string
  statusLabel: string
  statusColor: string
  onClick: () => void
}) {
  const timer = remainingTimeLabel(event, clock, locale)
  const dLabel = dateLabel(event, locale)

  return (
    <div className="card card-tap" onClick={onClick}>
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
        {/* Status dot */}
        <span style={{
          width: '8px', height: '8px', borderRadius: '50%', background: statusColor,
          flexShrink: 0, marginTop: '4px',
        }} />
        <div style={{ flex: 1, minWidth: 0 }}>
          <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text)', lineHeight: 1.3 }}>{eventLocaleTitle(event, locale as LocaleCode)}</p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', alignItems: 'center', marginTop: '3px' }}>
            <span style={{ padding: '1px 5px', borderRadius: '4px', background: `${statusColor}1a`, color: statusColor, fontSize: '9px', fontWeight: 700 }}>
              {statusLabel}
            </span>
            {dLabel && <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>{dLabel}</span>}
          </div>
        </div>
        <span style={{ fontSize: '10px', fontWeight: 700, color: statusColor, opacity: 0.85, whiteSpace: 'nowrap' }}>
          {timer}
        </span>
      </div>
    </div>
  )
}