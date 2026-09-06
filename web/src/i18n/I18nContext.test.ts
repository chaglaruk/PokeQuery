import { describe, expect, it } from 'vitest'
import { resolveSearchLanguage } from './I18nContext'

describe('resolveSearchLanguage parity with Android', () => {
  it('Auto follows supported system locale', () => {
    expect(resolveSearchLanguage('Auto', 'System Default', 'tr')).toBe('Turkish')
    expect(resolveSearchLanguage('Auto', 'System Default', 'de')).toBe('German')
    expect(resolveSearchLanguage('Auto', 'System Default', 'en')).toBe('English')
  })

  it('Match App Language follows an explicit app language', () => {
    expect(resolveSearchLanguage('Match App Language', 'Français', 'tr')).toBe('French')
    expect(resolveSearchLanguage('Match App Language', 'Italiano', 'en')).toBe('Italian')
  })

  it('Match App Language follows system locale when app language is System Default', () => {
    expect(resolveSearchLanguage('Match App Language', 'System Default', 'es')).toBe('Spanish')
  })

  it('explicit search language ignores app and system language', () => {
    expect(resolveSearchLanguage('German', 'Türkçe', 'fr')).toBe('German')
  })
})
