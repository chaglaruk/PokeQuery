// Parity test: mirrors StringBuilderEngineTest.kt (Android JUnit)
// Every assertion here must match the Kotlin test's expectation 1:1.

import { describe, it, expect } from 'vitest'
import { buildString, buildGoal, COUNT_MANDATORY_PROTECTIONS } from '../engine/stringBuilderEngine'
import { lint } from '../engine/linter'

describe('StringBuilderEngine parity', () => {
  it('no generated default string contains pipe', () => {
    const result = buildString('test|query', undefined, 'test')
    expect(result.rawSyntax).not.toContain('|')
    expect(result.rawSyntax).toContain(',')
  })

  it('count templates include required exclusions', () => {
    const result = buildString('count2-', [], 'test')
    expect(result.rawSyntax).toContain('!shiny')
    expect(result.rawSyntax).toContain('!legendary')
    expect(result.rawSyntax).toContain('!costume')
  })

  it('count cleanup protects ultra beast and background variants', () => {
    const result = buildString('count2-', [], 'test')
    expect(result.rawSyntax).toContain('!ultrabeast')
    expect(result.rawSyntax).toContain('!background')
    expect(result.rawSyntax).toContain('!locationbackground')
    expect(result.rawSyntax).toContain('!specialbackground')
  })

  it('count cleanup always keeps the traded invariant and existing protections', () => {
    const result = buildString('count2-', [], 'test')
    for (const token of COUNT_MANDATORY_PROTECTIONS) {
      expect(result.rawSyntax).toContain(`!${token}`)
    }
    expect(result.rawSyntax).toContain('!traded')
  })

  it('safe cleanup includes default exclusions', () => {
    const result = buildString('1*', undefined, 'test')
    expect(result.rawSyntax).toContain('!shiny')
    expect(result.rawSyntax).toContain('!4*')
    expect(result.rawSyntax).not.toContain('!0*')
  })

  it('2x candy prep includes count2- and warning', () => {
    const result = buildString('count2-', undefined, 'test')
    expect(result.rawSyntax).toContain('count2-')
    expect(result.riskLevel).toBe('Medium')
  })

  it('linter catches pipe', () => {
    const warnings = lint('shiny|lucky')
    expect(warnings.some(w => w.message.includes('|'))).toBe(true)
  })

  it('linter catches unsafe count', () => {
    const warnings = lint('count3-')
    expect(warnings.some(w => w.message.includes('Unsafe count usage'))).toBe(true)
    expect(warnings.some(w => w.message.includes('!costume'))).toBe(true)
  })

  it('linter catches count shortcut risky inclusion and reserved tag collision', () => {
    const warnings = lint('count&shiny&#shiny')
    expect(warnings.some(w => w.message.includes('count2-'))).toBe(true)
    expect(warnings.some(w => w.message.includes('Risky inclusion of shiny'))).toBe(true)
    expect(warnings.some(w => w.message.includes('collides'))).toBe(true)
  })

  it('engine adds count warning', () => {
    const result = buildString('count2-', undefined, 'test')
    expect(result.warnings.some(w => w.includes('Count is based on Pokédex species number'))).toBe(true)
  })

  it('trade fodder includes trade warning and correct protections', () => {
    const result = buildGoal('trade_fodder')
    expect(result.warnings.some(w => w.includes('Real trade eligibility depends on friendship level'))).toBe(true)
    expect(result.warnings.some(w => w.includes('Count is based on Pokédex species number'))).toBe(true)
    expect(result.rawSyntax).not.toBe('traded')
    expect(result.rawSyntax).toContain('!traded')
    expect(result.rawSyntax).toContain('!shiny')
    expect(result.rawSyntax).toContain('!lucky')
    expect(result.rawSyntax).toContain('!legendary')
  })

  it('safe cleanup includes positive condition and review explanation', () => {
    const explanation = 'This is a REVIEW string targeting 1-star low-value candidates. It is not an automatic transfer command.'
    const result = buildString('1*', undefined, explanation)
    expect(result.rawSyntax).toContain('1*')
    expect(result.rawSyntax).toContain('!shiny')
    expect(result.rawSyntax).toContain('!4*')
    expect(result.plainLanguageExplanation).toContain('REVIEW string')
    expect(result.plainLanguageExplanation).toContain('not an automatic transfer')
  })

  it('hundo check does not hide special categories and has correct explanation', () => {
    const result = buildGoal('hundo_check')
    expect(result.rawSyntax).toBe('4*')
    expect(result.rawSyntax).not.toContain('!shiny')
    expect(result.rawSyntax).not.toContain('!legendary')
    expect(result.rawSyntax).not.toContain('!shadow')
    expect(result.rawSyntax).not.toContain('!lucky')
    expect(result.rawSyntax).not.toContain('!costume')
    expect(result.rawSyntax).not.toContain('!#')
    expect(result.rawSyntax).not.toContain('!traded')
    expect(result.plainLanguageExplanation).toContain('perfect IV / hundo')
    expect(result.plainLanguageExplanation).toContain('15/15/15')
    expect(result.riskLevel).toBe('Info')
    expect(result.warnings.length).toBe(0)
  })

  it('all default count goals exclude costume', () => {
    for (const goal of ['candy_prep', 'trade_fodder']) {
      expect(buildGoal(goal).rawSyntax).toContain('!costume')
    }
  })

  it('nundo finder generates exact string without protections', () => {
    const result = buildGoal('nundo_finder')
    expect(result.rawSyntax).toBe('0attack&0defense&0hp')
    expect(result.rawSyntax).not.toContain('!shiny')
    expect(result.riskLevel).toBe('Info')
    expect(result.scopeBreadth).toBe('Very Narrow')
  })

  it('pvp candidates generate correctly without cleanup protections', () => {
    const greatLeague = buildGoal('pvp_candidates', 'great')
    const ultraLeague = buildGoal('pvp_candidates', 'ultra')
    expect(greatLeague.rawSyntax).toBe('0-1attack&3-4defense&3-4hp&cp-1500')
    expect(ultraLeague.rawSyntax).toBe('0-1attack&3-4defense&3-4hp&cp-2500')
    expect(greatLeague.rawSyntax).not.toContain('!shiny')
    expect(greatLeague.scopeBreadth).toBe('Narrow')
  })

  it('lucky trade prep generates age or distance modes with warnings', () => {
    const ageMode = buildGoal('lucky_trade', 'age')
    const distMode = buildGoal('lucky_trade', 'distance')
    expect(ageMode.rawSyntax).toBe('age365-&!traded')
    expect(distMode.rawSyntax).toBe('distance100-&!traded')
    expect(ageMode.rawSyntax).not.toContain('!shiny')
    expect(ageMode.scopeBreadth).toBe('Moderate')
  })

  it('linter ignores exact nundo pattern for 0 star warning', () => {
    const warnings = lint('0attack&0defense&0hp')
    expect(warnings.some(w => w.message.includes('0* is an IV band'))).toBe(false)
    const warningsWith0Star = lint('0*')
    expect(warningsWith0Star.some(w => w.message.includes('0* is an IV band'))).toBe(true)
  })

  it('linter bypasses transfer warnings for pvp and trade prep', () => {
    const pvpWarnings = lint('0-1attack&3-4defense&3-4hp&cp-1500&shiny')
    expect(pvpWarnings.some(w => w.message.includes('Risky inclusion'))).toBe(false)
    const tradePrepWarnings = lint('age365-&!traded')
    expect(tradePrepWarnings.some(w => w.message.includes('Trade prep search'))).toBe(true)
  })

  it('risk model splits inspection-only goals from action-adjacent goals', () => {
    const inspection = ['hundo_check', 'nundo_finder', 'pvp_candidates']
    const actionAdjacent = ['safe_cleanup', 'candy_prep', 'trade_fodder', 'lucky_trade']

    for (const goal of inspection) {
      expect(buildGoal(goal).riskLevel).toBe('Info')
      expect(buildGoal(goal).rawSyntax).not.toContain('!')
    }
    for (const goal of actionAdjacent) {
      expect(buildGoal(goal).riskLevel).toBe('Medium')
    }
  })
})
