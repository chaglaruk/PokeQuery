import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { I18nProvider } from '@i18n/I18nContext'
import { fetchEventFeed } from '@event/eventFeedService'
import { systemClock } from '@event/eventLifecycle'
import { EventsScreen } from '@ui/screens/EventsScreen'
import { en } from '@i18n/locales/en'
import { tr } from '@i18n/locales/tr'
import { de } from '@i18n/locales/de'
import { es } from '@i18n/locales/es'
import { fr } from '@i18n/locales/fr'
import { it as italian } from '@i18n/locales/it'

vi.mock('@event/eventFeedService', async importOriginal => ({
  ...await importOriginal<typeof import('@event/eventFeedService')>(),
  fetchEventFeed: vi.fn(),
}))

describe('publication-dated announcement presentation', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.spyOn(systemClock, 'todayIso').mockReturnValue('2026-10-09')
    vi.mocked(fetchEventFeed).mockResolvedValue({
      source: 'online', lastChecked: '2026-10-09T12:00:00Z',
      feed: { schemaVersion: 1, lastUpdated: '2026-10-09', events: [{
        id: 'gameplay-update', title: 'Gameplay improvements', status: 'ENDED',
        eventCategory: 'ANNOUNCEMENT', importanceTier: 'NEWS',
        publishedDate: '2026-10-08', startDate: '2026-10-08', endDate: '2026-10-08',
        note: 'Permanent changes, not a timed event.', summary: 'Gameplay improvements announced.',
        prep: '', suggestedSearch: '', eventNotes: '', themeKey: 'generic_event',
        sourceName: 'Pokémon GO official news', sourceUrl: 'https://pokemongo.com/news/pgo-gameplay-update-oct-2026',
        sourceType: 'official', lastUpdated: '2026-10-09',
      }] },
    })
  })
  afterEach(() => { cleanup(); vi.restoreAllMocks() })

  it.each([
    ['English', en, 'Published:'], ['Türkçe', tr, 'Yayımlandı:'],
    ['Deutsch', de, 'Veröffentlicht:'], ['Español', es, 'Publicado:'],
    ['Français', fr, 'Publié:'], ['Italiano', italian, 'Pubblicato:'],
  ] as const)('keeps %s news free of ended-event labels and search actions', async (language, copy, publicationLabel) => {
    localStorage.setItem('pq_app_language', language)
    render(<I18nProvider><MemoryRouter><EventsScreen /></MemoryRouter></I18nProvider>)
    const card = await screen.findByRole('button', { name: `Gameplay improvements — ${copy.event_chip_news}` })
    expect(within(card).queryByText(copy.event_main_card_ended)).not.toBeInTheDocument()
    fireEvent.click(card)
    const dialog = within(screen.getByRole('dialog'))
    expect(dialog.queryByText(copy.event_main_card_ended)).not.toBeInTheDocument()
    expect(dialog.queryByText(copy.event_status_label.replace('{0}', copy.event_main_card_ended))).not.toBeInTheDocument()
    expect(dialog.getByText(copy.event_status_label.replace('{0}', copy.event_chip_news))).toBeVisible()
    expect(dialog.getByText(new RegExp(publicationLabel))).toBeVisible()
    expect(dialog.queryByText(/age0/)).not.toBeInTheDocument()
    expect(dialog.queryByRole('button', { name: copy.event_copy_search })).not.toBeInTheDocument()
  })
})
