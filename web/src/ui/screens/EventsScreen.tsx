import { useState, useCallback, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '@i18n/I18nContext'
import { useEventFeed, useLocalizedEvent } from '@event/useEventFeed'
import {
  groupEvents,
  remainingTimeLabel,
  dateLabel,
  effectiveStatus,
  determineCategory,
  systemClock,
  type Clock,
} from '@event/eventLifecycle'
import { getLocalized } from '@event/eventFeedService'
import { Dialog } from '@ui/components/Dialog'
import { copyToClipboard, type ClipboardResult } from '@ui/clipboard'
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

function categoryChipColor(category: string): string {
  if (category === 'MAJOR_GAMEPLAY') return 'var(--accent)'
  if (category === 'LIMITED_GAMEPLAY') return 'var(--warning)'
  if (category === 'SEASON_GBL' || category === 'ROUTINE_ROTATION' || category === 'RAID_ROTATION') return 'var(--info)'
  return 'var(--text-dim)'
}

function categoryLabelKey(category: string): string {
  switch (category) {
    case 'MAJOR_GAMEPLAY': return 'event_chip_major'
    case 'LIMITED_GAMEPLAY': return 'event_chip_limited'
    case 'SEASON_GBL':
    case 'ROUTINE_ROTATION':
    case 'RAID_ROTATION':
      return 'event_chip_rotation'
    case 'NEWS_PROMO':
    case 'REWARD_DROP':
    case 'ANNOUNCEMENT':
      return 'event_chip_news'
    default: return 'event_chip_news'
  }
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

// Status label keys — replaces all hardcoded `locale === 'tr' ? '...' : '...'`
function statusLabelKey(status: 'CURRENT' | 'UPCOMING' | 'ENDED'): string {
  switch (status) {
    case 'CURRENT': return 'event_chip_live'
    case 'UPCOMING': return 'event_chip_upcoming'
    default: return 'event_main_card_ended'
  }
}

export function EventsScreen() {
  const { t, locale } = useI18n()
  const navigate = useNavigate()
  const { events: allEvents, source, loading, error, lastChecked, refresh } = useEventFeed(locale)
  const [openEvent, setOpenEvent] = useState<EventFeedEntry | null>(null)
  const [openPokemon, setOpenPokemon] = useState<{ entry: EventPokemonEntry; event: EventFeedEntry } | null>(null)
  const clock = systemClock

  const sections = useMemo(() => groupEvents(allEvents, clock), [allEvents, clock])

  const sourceLabel = source === 'online' ? t('event_status_live_feed')
    : source === 'cached' ? t('event_status_saved_guide')
    : source === 'fallback' ? t('event_status_bundled_fallback')
    : ''
  const isLoading = loading && sections.allActive.length === 0

  // ── Deduplication proof ──
  // We collect every event ID rendered into any priority section
  // (featured/happeningNow/importantUpcoming/rotations/news) and feed the
  // remainder (only events NOT rendered elsewhere) to "All Active". This
  // guarantees each event appears EXACTLY once across all visible sections.
  const remainder = useMemo(() => {
    const renderedIds = new Set<string>()
    if (sections.featured) renderedIds.add(sections.featured.id)
    for (const e of sections.happeningNow) renderedIds.add(e.id)
    for (const e of sections.importantUpcoming) renderedIds.add(e.id)
    for (const e of sections.rotations) renderedIds.add(e.id)
    for (const e of sections.news) renderedIds.add(e.id)
    return sections.allActive.filter(e => !renderedIds.has(e.id))
  }, [sections])

  return (
    <div className="page content-with-nav">
      {/* Header */}
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')} aria-label={t('back')}>{'\u2039'}</button>
        <h1 style={{ flex: 1 }}>{t('events_title')}</h1>
        <button
          className="btn btn-secondary"
          onClick={refresh}
          disabled={loading && sections.allActive.length > 0}
          aria-label={t('events_refresh')}
          style={{ width: 'auto', padding: '6px 14px', fontSize: '13px' }}
        >
          <span style={{ marginRight: '4px', fontSize: '16px', lineHeight: 1 }} aria-hidden="true">{'\u21bb'}</span>
          {t('events_refresh')}
        </button>
      </div>

      {/* Source + lastChecked status line */}
      {source && (
        <div style={{ marginBottom: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
            <span aria-hidden="true" style={{ width: '7px', height: '7px', borderRadius: '50%', background: 'var(--accent)', display: 'inline-block' }} />
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
        <div style={{ textAlign: 'center', padding: '48px 0' }} role="status">
          <div style={{
            width: '32px', height: '32px', border: '3px solid var(--border)',
            borderTopColor: 'var(--accent)', borderRadius: '50%',
            animation: 'spin 0.8s linear infinite', margin: '0 auto',
          }} aria-hidden="true" />
        </div>
      )}

      {error && (
        <div className="card" style={{ borderColor: 'var(--danger)' }} role="alert">
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
            onOpenPokemon={(entry) => setOpenPokemon({ entry, event: sections.featured! })}
            onOpenDetail={() => setOpenEvent(sections.featured)}
          />
        </>
      )}

      {/* ── Happening Now ── */}
      {sections.happeningNow.length > 0 && (
        <>
          <div className="section-title">{t('event_section_live')}</div>
          {sections.happeningNow.map(event => {
            const status = effectiveStatus(event, clock.todayIso())
            return (
              <CompactEventCard
                key={`now-${event.id}`}
                event={event}
                clock={clock}
                locale={locale}
                statusLabel={t(statusLabelKey(status))}
                statusColor="var(--accent)"
                onClick={() => setOpenEvent(event)}
              />
            )
          })}
        </>
      )}

      {/* ── Important Upcoming ── */}
      {sections.importantUpcoming.length > 0 && (
        <>
          <div className="section-title">{t('event_section_upcoming')}</div>
          {sections.importantUpcoming.map(event => {
            const status = effectiveStatus(event, clock.todayIso())
            return (
              <CompactEventCard
                key={`up-${event.id}`}
                event={event}
                clock={clock}
                locale={locale}
                statusLabel={t(statusLabelKey(status))}
                statusColor="var(--warning)"
                onClick={() => setOpenEvent(event)}
              />
            )
          })}
        </>
      )}

      {/* ── Rotations ── */}
      {sections.rotations.length > 0 && (
        <>
          <div className="section-title">{t('event_section_rotations')}</div>
          {sections.rotations.map(event => {
            const status = effectiveStatus(event, clock.todayIso())
            return (
              <CompactEventCard
                key={`rot-${event.id}`}
                event={event}
                clock={clock}
                locale={locale}
                statusLabel={t(statusLabelKey(status))}
                statusColor="var(--info)"
                onClick={() => setOpenEvent(event)}
              />
            )
          })}
        </>
      )}

      {/* ── News ── */}
      {sections.news.length > 0 && (
        <>
          <div className="section-title">{t('event_section_news')}</div>
          {sections.news.map(event => {
            const status = effectiveStatus(event, clock.todayIso())
            return (
              <CompactEventCard
                key={`news-${event.id}`}
                event={event}
                clock={clock}
                locale={locale}
                statusLabel={t(statusLabelKey(status))}
                statusColor="var(--text-dim)"
                onClick={() => setOpenEvent(event)}
              />
            )
          })}
        </>
      )}

      {/* ── Remainder (events not already shown in any priority section) ── */}
      {remainder.length > 0 && (
        <>
          <div className="section-title">{t('event_section_all')}</div>
          {remainder.map(event => {
            const cat = determineCategory(event)
            return (
              <CompactEventCard
                key={`all-${event.id}`}
                event={event}
                clock={clock}
                locale={locale}
                statusLabel={t(categoryLabelKey(cat))}
                statusColor={categoryChipColor(cat)}
                onClick={() => setOpenEvent(event)}
              />
            )
          })}
        </>
      )}

      {/* Disclaimer */}
      <div style={{ marginTop: '16px', padding: '12px 0' }}>
        <p style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: 1.5 }}>
          {t('event_context_disclaimer')}
        </p>
      </div>

      {/* Compact event detail dialog */}
      <EventDetailDialog
        event={openEvent}
        onClose={() => setOpenEvent(null)}
        closeLabel={t('event_dialog_close')}
        clock={clock}
        locale={locale}
        sourceLabel={sourceLabel}
        lastChecked={lastChecked}
        onOpenPokemon={(entry) => setOpenPokemon({ entry, event: openEvent! })}
      />

      {/* Pokémon-entry dialog */}
      <PokemonDetailDialog
        data={openPokemon}
        onClose={() => setOpenPokemon(null)}
        closeLabel={t('event_dialog_close')}
        locale={locale}
        eventTitle={openPokemon?.event ? eventLocaleTitle(openPokemon.event, locale) : ''}
      />
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
  onOpenDetail,
}: {
  event: EventFeedEntry
  sourceLabel: string
  lastChecked: string | null
  clock: Clock
  locale: LocaleCode
  onOpenPokemon: (entry: EventPokemonEntry) => void
  onOpenDetail: () => void
}) {
  const { t } = useI18n()
  const localized = useLocalizedEvent(event, locale)
  const [clipboard, setClipboard] = useState<ClipboardResult | null>(null)
  const tone = themeTone(event.themeKey)
  const timerLabel = remainingTimeLabel(event, clock, locale)
  const dLabel = dateLabel(event, locale)
  const search = event.suggestedSearch ?? ''
  const pokemon = event.pokemon ?? []

  const handleCopy = useCallback(async () => {
    if (!search) return
    const res = await copyToClipboard(search)
    setClipboard(res)
    if (res.status === 'copied') {
      setTimeout(() => setClipboard(null), 2500)
    }
  }, [search])

  if (!localized) return null

  return (
    <div className="card" style={{ borderColor: tone, position: 'relative' }}>
      {/* Timer badge + theme mark */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
        <span className="badge" style={{ background: `${tone}22`, color: tone }}>{timerLabel}</span>
        <button
          type="button"
          className="dialog-close"
          aria-label={t('event_open_details')}
          onClick={onOpenDetail}
          style={{ background: `${tone}1a` }}
        >
          {'\u2139'}
        </button>
      </div>

      {/* Title + date */}
      <p style={{ fontSize: '19px', fontWeight: 700, lineHeight: 1.3, marginBottom: '4px' }}>{eventLocaleTitle(event, locale)}</p>
      {dLabel && <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '8px' }}>{dLabel}</p>}

      {/* Summary */}
      {localized.summary && localized.summary.length > 0 && (
        <p style={{ fontSize: '13px', color: 'var(--text-dim)', lineHeight: 1.4, marginBottom: '10px' }}>{localized.summary}</p>
      )}

      {/* Pokémon sprite row */}
      {pokemon.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginBottom: '10px' }}>
          {pokemon.slice(0, 6).map((p: EventPokemonEntry, idx: number) => (
            <div
              key={`sprite-${idx}`}
              className="card card-tap"
              style={{ padding: '8px', textAlign: 'center' }}
              onClick={() => onOpenPokemon(p)}
            >
              {p.spriteKey ? (
                <img src={spriteSrc(p.spriteKey) ?? ''} alt={pokeLocalizedName(p, locale)}
                  style={{ width: '48px', height: '48px', margin: '0 auto', display: 'block' }} />
              ) : (
                <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: `${tone}1a`, margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <span style={{ fontSize: '22px' }} aria-hidden="true">?</span>
                </div>
              )}
              <p style={{ fontSize: '12px', fontWeight: 700, marginTop: '4px', color: tone }}>
                {pokeLocalizedName(p, locale)}
              </p>
              {pokeLocalizedBadges(p, locale) && (
                <p style={{ fontSize: '9px', color: 'var(--text-muted)' }}>
                  {pokeLocalizedBadges(p, locale)}
                </p>
              )}
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
            {t('event_copy_search')}
          </button>
          {clipboard && (
            <div className={`clipboard-feedback ${clipboard.status}`} role="status" aria-live="polite">
              {t(clipboard.i18nKey)}
            </div>
          )}
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
  clock: Clock
  locale: LocaleCode
  statusLabel: string
  statusColor: string
  onClick: () => void
}) {
  const timer = remainingTimeLabel(event, clock, locale)
  const dLabel = dateLabel(event, locale)

  return (
    <div className="card card-tap" onClick={onClick} role="button" tabIndex={0}
      onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); onClick() } }}
      aria-label={`${eventLocaleTitle(event, locale)} — ${statusLabel}`}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
        {/* Status dot */}
        <span aria-hidden="true" style={{
          width: '8px', height: '8px', borderRadius: '50%', background: statusColor,
          flexShrink: 0, marginTop: '4px',
        }} />
        <div style={{ flex: 1, minWidth: 0 }}>
          <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text)', lineHeight: 1.3 }}>{eventLocaleTitle(event, locale)}</p>
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

/* ──────────────── EventDetailDialog ──────────────── */

function EventDetailDialog({
  event,
  onClose,
  closeLabel,
  clock,
  locale,
  sourceLabel,
  lastChecked,
  onOpenPokemon,
}: {
  event: EventFeedEntry | null
  onClose: () => void
  closeLabel: string
  clock: Clock
  locale: LocaleCode
  sourceLabel: string
  lastChecked: string | null
  onOpenPokemon: (entry: EventPokemonEntry) => void
}) {
  const { t } = useI18n()
  const localized = useLocalizedEvent(event, locale)
  const [clipboard, setClipboard] = useState<ClipboardResult | null>(null)
  const tone = themeTone(event?.themeKey ?? '')
  const timerLabel = event ? remainingTimeLabel(event, clock, locale) : ''
  const dLabel = event ? dateLabel(event, locale) : ''
  const search = event?.suggestedSearch ?? ''
  const pokemon = event?.pokemon ?? []
  const status = event ? effectiveStatus(event, clock.todayIso()) : 'ENDED'
  const cat = event ? determineCategory(event) : ''

  const handleCopy = useCallback(async () => {
    if (!search) return
    const res = await copyToClipboard(search)
    setClipboard(res)
    if (res.status === 'copied') setTimeout(() => setClipboard(null), 2500)
  }, [search])

  const title = event ? eventLocaleTitle(event, locale) : ''

  if (!event) return null

  return (
    <Dialog
      open={!!event}
      title={title}
      onClose={onClose}
      closeLabel={closeLabel}
    >
      <div style={{ display: 'flex', gap: '6px', alignItems: 'center', marginBottom: '8px', flexWrap: 'wrap' }}>
        <span className="badge" style={{ background: `${tone}22`, color: tone }}>{timerLabel}</span>
        <span className="badge" style={{ background: `${categoryChipColor(cat)}1a`, color: categoryChipColor(cat) }}>
          {t(categoryLabelKey(cat))}
        </span>
        <span className="badge badge-beta">{t(statusLabelKey(status))}</span>
      </div>

      {dLabel && (
        <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '8px' }}>
          <strong>{t('event_dialog_dates')}:</strong> {dLabel}
        </p>
      )}

      {localized?.summary && (
        <p style={{ fontSize: '13px', color: 'var(--text-dim)', lineHeight: 1.5, marginBottom: '12px' }}>
          {localized.summary}
        </p>
      )}

      {/* Why it matters */}
      {localized?.note && localized.note.length > 0 && (
        <div style={{ marginBottom: '12px' }}>
          <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--accent)', marginBottom: '4px' }}>
            {t('event_dialog_why_matters')}
          </p>
          <p style={{ fontSize: '13px', color: 'var(--text-dim)', lineHeight: 1.5 }}>
            {localized.note}
          </p>
        </div>
      )}

      {/* What to do */}
      {localized?.prep && localized.prep.length > 0 && (
        <div style={{ marginBottom: '12px' }}>
          <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--warning)', marginBottom: '4px' }}>
            {t('event_dialog_what_to_do')}
          </p>
          <p style={{ fontSize: '13px', color: 'var(--text-dim)', lineHeight: 1.5 }}>
            {localized.prep}
          </p>
        </div>
      )}

      {/* Bonus section — only if data exists */}
      {localized?.bonuses && localized.bonuses.length > 0 && (
        <div style={{ marginBottom: '12px' }}>
          <p style={{ fontSize: '11px', fontWeight: 700, color: 'var(--warning)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            {t('event_chip_bonus')}
          </p>
          <p style={{ fontSize: '13px', color: 'var(--text-dim)', lineHeight: 1.5 }}>{localized.bonuses}</p>
        </div>
      )}

      {/* Raid section — only if data exists */}
      {localized?.raids && localized.raids.length > 0 && (
        <div style={{ marginBottom: '12px' }}>
          <p style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Raids
          </p>
          <p style={{ fontSize: '13px', color: 'var(--text-dim)', lineHeight: 1.5 }}>{localized.raids}</p>
        </div>
      )}

      {/* Research section — only if data exists */}
      {localized?.research && localized.research.length > 0 && (
        <div style={{ marginBottom: '12px' }}>
          <p style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Research
          </p>
          <p style={{ fontSize: '13px', color: 'var(--text-dim)', lineHeight: 1.5 }}>{localized.research}</p>
        </div>
      )}

      {/* Pokémon list */}
      {pokemon.length > 0 && (
        <div style={{ marginBottom: '12px' }}>
          <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--accent)', marginBottom: '6px' }}>
            {t('event_dialog_pokemon')}
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            {pokemon.map((p: EventPokemonEntry, idx: number) => (
              <div
                key={`pk-${idx}`}
                className="card card-tap"
                style={{ padding: '8px', textAlign: 'center' }}
                onClick={() => onOpenPokemon(p)}
              >
                {p.spriteKey ? (
                  <img src={spriteSrc(p.spriteKey) ?? ''} alt={pokeLocalizedName(p, locale)}
                    style={{ width: '48px', height: '48px', margin: '0 auto', display: 'block' }} />
                ) : (
                  <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: `${tone}1a`, margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <span style={{ fontSize: '22px' }} aria-hidden="true">?</span>
                  </div>
                )}
                <p style={{ fontSize: '12px', fontWeight: 700, marginTop: '4px', color: tone }}>
                  {pokeLocalizedName(p, locale)}
                </p>
                {pokeLocalizedBadges(p, locale) && (
                  <p style={{ fontSize: '9px', color: 'var(--text-muted)' }}>
                    {pokeLocalizedBadges(p, locale)}
                  </p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Suggested search */}
      {search ? (
        <>
          <p style={{ fontSize: '11px', fontWeight: 700, color: tone, marginBottom: '4px' }}>
            {t('event_suggested_for_event')}
          </p>
          <div className="search-string" style={{ fontSize: '12px', padding: '10px 12px', marginBottom: '8px' }}>{search}</div>
          <button className="btn btn-primary" onClick={handleCopy} style={{ padding: '8px 12px', fontSize: '13px', width: 'auto' }}>
            {t('event_copy_search')}
          </button>
          {clipboard && (
            <div className={`clipboard-feedback ${clipboard.status}`} role="status" aria-live="polite">
              {t(clipboard.i18nKey)}
            </div>
          )}
        </>
      ) : (
        <p style={{ fontSize: '13px', color: 'var(--text-dim)', fontStyle: 'italic' }}>
          {t('event_dialog_no_search')}
        </p>
      )}

      {/* Source + status */}
      <div style={{ marginTop: '14px', paddingTop: '10px', borderTop: '1px solid var(--border)' }}>
        {event.sourceName && (
          <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
            <strong>{t('event_dialog_source')}:</strong> {event.sourceName}
          </p>
        )}
        <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
          <strong>{t('event_status_label', t(statusLabelKey(status)))}</strong>{sourceLabel && ` · ${sourceLabel}`}
        </p>
        {lastChecked && (
          <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
            {t('event_context_last_checked', new Date(lastChecked).toLocaleString())}
          </p>
        )}
      </div>
    </Dialog>
  )
}

/* ──────────────── PokemonDetailDialog ──────────────── */

function PokemonDetailDialog({
  data,
  onClose,
  closeLabel,
  locale,
  eventTitle,
}: {
  data: { entry: EventPokemonEntry; event: EventFeedEntry } | null
  onClose: () => void
  closeLabel: string
  locale: LocaleCode
  eventTitle: string
}) {
  if (!data) return null
  const { entry, event } = data
  const tone = themeTone(event.themeKey)
  const name = pokeLocalizedName(entry, locale)
  const badges = pokeLocalizedBadges(entry, locale)

  return (
    <Dialog
      open={!!data}
      title={name}
      onClose={onClose}
      closeLabel={closeLabel}
    >
      {/* Featured-in context */}
      <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '8px' }}>
        {eventTitle}
      </p>

      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '12px' }}>
        {entry.spriteKey ? (
          <img src={spriteSrc(entry.spriteKey) ?? ''} alt={name}
            style={{ width: '96px', height: '96px', display: 'block' }} />
        ) : (
          <div style={{
            width: '96px', height: '96px', borderRadius: '16px', background: `${tone}1a`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <span style={{ fontSize: '40px' }} aria-hidden="true">?</span>
          </div>
        )}
      </div>

      <p style={{ fontSize: '15px', fontWeight: 700, textAlign: 'center', marginBottom: '6px', color: tone }}>
        {name}
      </p>

      {badges && (
        <p style={{ fontSize: '12px', color: 'var(--text-dim)', textAlign: 'center', marginBottom: '12px' }}>
          {badges}
        </p>
      )}

      {entry.note && (
        <p style={{ fontSize: '12px', color: 'var(--text-dim)', lineHeight: 1.5, marginBottom: '12px' }}>
          {entry.note}
        </p>
      )}
    </Dialog>
  )
}
