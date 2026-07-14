import { beforeEach, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { I18nProvider } from '@i18n/I18nContext'
import { GoalDetailScreen } from '@ui/screens/GoalDetailScreen'

function renderGoal(path: string) {
  return render(
    <I18nProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes><Route path="/goal/:goalId" element={<GoalDetailScreen />} /></Routes>
      </MemoryRouter>
    </I18nProvider>,
  )
}

describe('UI and Search String language independence', () => {
  beforeEach(() => localStorage.clear())

  it('keeps Turkish UI around English search syntax', () => {
    localStorage.setItem('pq_app_language', 'Türkçe')
    localStorage.setItem('pq_search_language', 'English')
    renderGoal('/goal/safe_cleanup')
    expect(screen.getByText('SONUÇ')).toBeVisible()
    expect(screen.getByText(/1\*&!shiny/)).toBeVisible()
    expect(screen.queryByText(/This is a REVIEW string/i)).not.toBeInTheDocument()
  })

  it('keeps English UI around Turkish search syntax', () => {
    localStorage.setItem('pq_app_language', 'English')
    localStorage.setItem('pq_search_language', 'Turkish')
    renderGoal('/goal/pvp_candidates')
    expect(screen.getByText('RESULT')).toBeVisible()
    expect(screen.getByText(/&dg-1500/)).toBeVisible()
    expect(screen.queryByText('Bu arama ne yapar?')).not.toBeInTheDocument()
  })

  it.each([
    ['Deutsch', 'German'], ['Español', 'Spanish'], ['Français', 'French'], ['Italiano', 'Italian'],
  ])('does not leak known English engine prose into %s UI', (appLanguage, searchLanguage) => {
    localStorage.setItem('pq_app_language', appLanguage)
    localStorage.setItem('pq_search_language', searchLanguage)
    renderGoal('/goal/safe_cleanup')
    expect(screen.queryByText(/This is a REVIEW string/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/Candidate search only\. Final PvP rank/i)).not.toBeInTheDocument()
  })

  it('keeps Auto English-safe while Match App Language changes only generated tokens', () => {
    localStorage.setItem('pq_app_language', 'Türkçe')
    localStorage.setItem('pq_search_language', 'Auto')
    const first = renderGoal('/goal/safe_cleanup')
    expect(screen.getByText(/!shiny/)).toBeVisible()
    expect(screen.getByText('Bu arama ne yapar?')).toBeVisible()
    first.unmount()

    localStorage.setItem('pq_search_language', 'Match App Language')
    renderGoal('/goal/safe_cleanup')
    expect(screen.getByText(/!parlak/)).toBeVisible()
    expect(screen.getByText('Bu arama ne yapar?')).toBeVisible()
  })
})
