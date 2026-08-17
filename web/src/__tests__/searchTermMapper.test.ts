import { describe, expect, it } from 'vitest'
import {
  findUnverifiedTokens,
  looksTurkish,
  resolveLanguage,
  translateSyntax,
} from '../engine/searchTermMapper'

describe('searchTermMapper', () => {
  it('English is the safe default', () => {
    expect(resolveLanguage('English')).toBe('English')
  })

  it('Auto resolves to English regardless of device locale', () => {
    expect(resolveLanguage('Auto')).toBe('English')
    expect(resolveLanguage('')).toBe('English')
    expect(resolveLanguage('   ')).toBe('English')
  })

  it('explicit Turkish selection is preserved', () => {
    expect(resolveLanguage('Turkish')).toBe('Turkish')
  })

  it('English pass-through does not translate tokens', () => {
    const result = translateSyntax('count2-&!shiny&!traded', 'English')
    expect(result).toBe('count2-&!shiny&!traded')
  })

  it('explicit Turkish selection translates according to the official Niantic map', () => {
    const result = translateSyntax('count2-&!shiny&!traded', 'Turkish')
    expect(result).toContain('count2-')
    expect(result).not.toContain('toplam')
    expect(result).toContain('!parlak')
    expect(result).toContain('!takas edilen')
  })

  it('Auto does not emit Turkish tokens', () => {
    const result = translateSyntax('shiny&!traded', 'Auto')
    expect(result).not.toContain('parlak')
    expect(result).toBe('shiny&!traded')
  })

  it('looksTurkish accurately detects Turkish output with token boundaries and avoids false positives', () => {
    expect(looksTurkish('!specialbackground')).toBe(false)
    expect(looksTurkish('!shiny')).toBe(false)
    expect(looksTurkish('distance100-')).toBe(false)
    expect(looksTurkish('sp150')).toBe(true)
    expect(looksTurkish('dg300')).toBe(true)
    expect(looksTurkish('!şanslı')).toBe(true)
    expect(looksTurkish('!takas edilen')).toBe(true)
    expect(looksTurkish('!gölge')).toBe(true)
    expect(looksTurkish('4*')).toBe(false)
    expect(looksTurkish('')).toBe(false)
  })

  it('Turkish count cleanup translates only officially documented safe tokens', () => {
    const base = 'count2-&!shiny&!lucky&!legendary&!mythical&!shadow&!purified&!favorite&' +
      '!traded&!costume&!ultrabeast&!background&!locationbackground&!specialbackground&!#&!4*'
    const result = translateSyntax(base, 'Turkish')

    expect(result).toContain('count2-')
    expect(result).not.toContain('toplam')
    expect(result).toContain('!takas edilen')
    expect(result).toContain('!specialbackground')
    expect(result).toContain('!parlak')
    expect(result).toContain('!efsanevi')
    expect(result).toContain('!kostüm')
    expect(result).toContain('!ultracanavar')
    expect(result).toContain('!arkaplan')
    expect(result).toContain('!konumarkaplanı')
  })

  it('German official search tokens match official Niantic FAQ', () => {
    expect(translateSyntax('lucky', 'German')).toBe('glücks')
    expect(translateSyntax('!lucky', 'German')).toBe('!glücks')
    expect(translateSyntax('eggsonly', 'German')).toBe('nurausEiern')
    expect(translateSyntax('buddy', 'German')).toBe('kumpel')
    expect(translateSyntax('tradeevolve', 'German')).toBe('tauschentwicklung')
    expect(translateSyntax('!tradeevolve', 'German')).toBe('!tauschentwicklung')
  })

  it('Spanish official search tokens match official Niantic FAQ', () => {
    expect(translateSyntax('lucky', 'Spanish')).toBe('con suerte')
    expect(translateSyntax('!lucky', 'Spanish')).toBe('!con suerte')
    expect(translateSyntax('traded', 'Spanish')).toBe('intercambiados')
    expect(translateSyntax('!traded', 'Spanish')).toBe('!intercambiados')
    expect(translateSyntax('hypertraining', 'Spanish')).toBe('entrenamiento extremo')
  })

  it('French official search tokens handle numeric suffix properly', () => {
    expect(translateSyntax('mega1', 'French')).toBe('méga1')
    expect(translateSyntax('buddy', 'French')).toBe('copain')
  })

  it('Italian official search tokens translate evolve to fai evolvere', () => {
    expect(translateSyntax('evolve', 'Italian')).toBe('fai evolvere')
  })

  it('Turkish official CP and HP abbreviations remain DG and SP', () => {
    expect(translateSyntax('cp300&hp150', 'Turkish')).toBe('dg300&sp150')
  })

  it('translates with semicolon and colon boundaries', () => {
    expect(translateSyntax('shiny;legendary', 'Turkish')).toBe('parlak;efsanevi')
    expect(translateSyntax('shiny:legendary', 'Turkish')).toBe('parlak:efsanevi')
  })

  it('unverified tokens are reported for localized output', () => {
    const unverified = findUnverifiedTokens('count2-&!traded&!specialbackground', 'Turkish')
    expect(unverified).toEqual(['count', 'specialbackground'])
  })
})
