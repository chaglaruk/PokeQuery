import { afterEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { I18nProvider } from '@i18n/I18nContext'
import { KnowledgeScreen } from '@ui/screens/KnowledgeScreen'
import { canCopyKnowledgeToken } from '@engine/knowledgeCopyPolicy'
import { copyToClipboard } from '@ui/clipboard'

vi.mock('@ui/clipboard', () => ({ copyToClipboard: vi.fn() }))
afterEach(() => { vi.unstubAllGlobals(); vi.clearAllMocks() })

describe('Knowledge reference copy guard', () => {
  it.each(['cp[N]', 'attack[0-4]', '#[tag]', '@[type]', '|', 'shiny|legendary', 'cp<N>', '', ' '])('blocks %s', syntax => {
    expect(canCopyKnowledgeToken(syntax)).toBe(false)
  })
  it.each(['shiny', '!favorite', 'cp1500', 'count2-', '@fire', '#keep', '&', ','])('keeps %s copyable', syntax => {
    expect(canCopyKnowledgeToken(syntax)).toBe(true)
  })
  it.each(['cp[N]', '|'])('does not replace clipboard from a reference-only row: %s', async syntax => {
    localStorage.clear()
    localStorage.setItem('pq_app_language', 'English')
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => [{
      id: 'reference', title: 'Reference term', syntax, category: 'Numeric', tier: 'T1',
      description_en: 'Reference description', description_tr: '', riskLevel: 'Info', lastVerified: '2026-10-08', sourceUrl: '',
    }] }))
    render(<I18nProvider><MemoryRouter><KnowledgeScreen /></MemoryRouter></I18nProvider>)
    fireEvent.click(await screen.findByRole('button', { name: /Reference term/ }))
    const button = screen.getByRole('button', { name: 'Copy Token' })
    expect(button).toBeDisabled()
    fireEvent.click(button)
    expect(copyToClipboard).not.toHaveBeenCalled()
    expect(screen.getByText(/Reference only/)).toBeVisible()
  })
  it('copies a concrete token unchanged', async () => {
    localStorage.clear()
    localStorage.setItem('pq_app_language', 'English')
    vi.mocked(copyToClipboard).mockResolvedValue({ status: 'copied', i18nKey: 'knowledge_copied' })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => [{
      id: 'concrete', title: 'Concrete term', syntax: 'cp1500', category: 'Numeric', tier: 'T1',
      description_en: 'Concrete description', description_tr: '', riskLevel: 'Info', lastVerified: '2026-10-08', sourceUrl: '',
    }] }))
    render(<I18nProvider><MemoryRouter><KnowledgeScreen /></MemoryRouter></I18nProvider>)
    fireEvent.click(await screen.findByRole('button', { name: /Concrete term/ }))
    const button = screen.getByRole('button', { name: 'Copy Token' })
    expect(button).toBeEnabled()
    fireEvent.click(button)
    expect(copyToClipboard).toHaveBeenCalledWith('cp1500')
  })
})
