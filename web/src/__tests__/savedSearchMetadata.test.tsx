import { beforeEach, describe, expect, it } from 'vitest'
import { fireEvent, render, screen, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { I18nProvider, useI18n } from '@i18n/I18nContext'
import { SavedSearchesScreen } from '@ui/screens/SavedSearchesScreen'

const createdAt = Date.UTC(2026, 6, 14, 12, 34)

function LanguageHarness({ kind }: { kind: 'favorites' | 'history' }) {
  const { setAppLanguage } = useI18n()
  return <><button onClick={() => setAppLanguage('T\u00fcrk\u00e7e')}>TR</button><SavedSearchesScreen kind={kind} /></>
}

function renderSaved(kind: 'favorites' | 'history') {
  return render(<I18nProvider><MemoryRouter><LanguageHarness kind={kind} /></MemoryRouter></I18nProvider>)
}

describe('saved-search metadata', () => {
  beforeEach(() => localStorage.clear())

  it('localizes known goal titles and dates when UI language changes', () => {
    localStorage.setItem('pq_app_language', 'English')
    localStorage.setItem('pq_favorites', JSON.stringify([{
      id: 'favorite', name: 'Stale stored title', goalId: 'safe_cleanup',
      rawSyntax: '1*&!shiny&!traded', riskLevel: 'Medium', createdAt,
    }]))
    renderSaved('favorites')

    const englishCard = screen.getByRole('heading', { name: 'Safe Cleanup' }).closest('article')!
    expect(within(englishCard).getByText(new Intl.DateTimeFormat('en', { dateStyle: 'medium', timeStyle: 'short' }).format(createdAt))).toBeVisible()
    expect(screen.queryByText('safe_cleanup')).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'TR' }))
    const turkishCard = screen.getByRole('heading', { name: 'G\u00fcvenli Temizlik' }).closest('article')!
    expect(within(turkishCard).getByText(new Intl.DateTimeFormat('tr', { dateStyle: 'medium', timeStyle: 'short' }).format(createdAt))).toBeVisible()
    expect(screen.queryByText('Safe Cleanup')).not.toBeInTheDocument()
  })

  it('uses the stored name for unknown legacy history records', () => {
    localStorage.setItem('pq_app_language', 'English')
    localStorage.setItem('pq_history', JSON.stringify([{
      id: 'legacy', name: 'Legacy Cleanup Search', goalId: 'legacy_unknown',
      rawSyntax: 'age0-7', riskLevel: 'Low', createdAt,
    }]))
    renderSaved('history')

    expect(screen.getByRole('heading', { name: 'Legacy Cleanup Search' })).toBeVisible()
    expect(screen.queryByText('legacy_unknown')).not.toBeInTheDocument()
    expect(screen.getByText(new Intl.DateTimeFormat('en', { dateStyle: 'medium', timeStyle: 'short' }).format(createdAt))).toBeVisible()
  })
})
