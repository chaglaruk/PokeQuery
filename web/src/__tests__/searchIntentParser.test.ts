import { describe, it, expect } from 'vitest'
import { parseSearchIntent } from '../engine/searchIntentParser'

describe('parseSearchIntent', () => {
  it('returns empty intent for blank input', () => {
    const r = parseSearchIntent('')
    expect(r.tokens).toEqual([])
    expect(r.canBuild).toBe(false)
  })

  it('returns canBuild=false for unrecognized input', () => {
    const r = parseSearchIntent('xyz qwerty')
    expect(r.canBuild).toBe(false)
    expect(r.explanation).toContain('Could not understand')
  })

  it('parses "shiny" -> tokens [shiny]', () => {
    const r = parseSearchIntent('shiny')
    expect(r.tokens).toEqual(['shiny'])
    expect(r.canBuild).toBe(true)
    expect(r.rawQuery).toBe('shiny')
  })

  it('parses "hundo" -> tokens [4*]', () => {
    const r = parseSearchIntent('hundo')
    expect(r.tokens).toEqual(['4*'])
  })

  it('parses "nundo" -> tokens [0attack, 0defense, 0hp]', () => {
    const r = parseSearchIntent('nundo')
    expect(r.tokens).toEqual(['0attack', '0defense', '0hp'])
  })

  it('parses "great league" with CP cap', () => {
    const r = parseSearchIntent('great league')
    expect(r.tokens).toContain('cp-1500')
  })

  it('parses "ultra league" with CP cap', () => {
    const r = parseSearchIntent('ultra league')
    expect(r.tokens).toContain('cp-2500')
  })

  it('combines multiple patterns: "shiny legendary"', () => {
    const r = parseSearchIntent('shiny legendary')
    expect(r.tokens).toContain('shiny')
    expect(r.tokens).toContain('legendary')
    expect(r.rawQuery).toContain('&')
  })

  it('parses "cleanup" -> tokens [1*]', () => {
    const r = parseSearchIntent('cleanup')
    expect(r.tokens).toContain('1*')
  })

  it('parses "trade" -> tokens [count2-] exclusions [traded]', () => {
    const r = parseSearchIntent('trade')
    expect(r.tokens).toContain('count2-')
    expect(r.exclusions).toContain('traded')
    expect(r.rawQuery).toContain('!traded')
  })

  it('parses "untagged" -> exclusions [#]', () => {
    const r = parseSearchIntent('untagged')
    expect(r.tokens).toEqual([])
    expect(r.exclusions).toEqual(['#'])
    expect(r.rawQuery).toBe('!#')
    expect(r.canBuild).toBe(true)
  })

  it('parses "all" -> canBuild=false', () => {
    const r = parseSearchIntent('all')
    expect(r.canBuild).toBe(false)
  })

  it('parses Turkish "parlak" -> tokens [shiny]', () => {
    const r = parseSearchIntent('parlak')
    expect(r.tokens).toEqual(['shiny'])
  })

  it('parses Turkish "temizlik" -> tokens [1*]', () => {
    const r = parseSearchIntent('temizlik')
    expect(r.tokens).toContain('1*')
  })

  it('parses "lucky trade" -> tokens [age365-] exclusions [traded]', () => {
    const r = parseSearchIntent('lucky trade')
    expect(r.tokens).toContain('age365-')
    expect(r.exclusions).toContain('traded')
  })

  it('includes limitations when present', () => {
    const r = parseSearchIntent('hundo')
    expect(r.limitations.length).toBeGreaterThan(0)
  })

  it('includes explanation text', () => {
    const r = parseSearchIntent('shiny')
    expect(r.explanation.length).toBeGreaterThan(0)
    expect(r.explanation).toContain('Shiny')
  })

  it('negation: "not shiny" -> exclusions [shiny]', () => {
    const r = parseSearchIntent('not shiny')
    expect(r.exclusions).toContain('shiny')
    expect(r.tokens).not.toContain('shiny')
    expect(r.rawQuery).toBe('!shiny')
  })

  it('negation Turkish: "shiny hariç" -> exclusions [shiny]', () => {
    const r = parseSearchIntent('shiny hariç')
    expect(r.exclusions).toContain('shiny')
    expect(r.tokens).not.toContain('shiny')
  })

  it('old -> age365-', () => {
    const r = parseSearchIntent('old')
    expect(r.tokens).toContain('age365-')
  })

  it('distance -> distance100-', () => {
    const r = parseSearchIntent('distance')
    expect(r.tokens).toContain('distance100-')
  })

  it('candy -> count2-', () => {
    const r = parseSearchIntent('candy')
    expect(r.tokens).toContain('count2-')
  })

  it('shadow -> shadow', () => {
    const r = parseSearchIntent('shadow')
    expect(r.tokens).toContain('shadow')
  })

  it('costume -> costume', () => {
    const r = parseSearchIntent('costume')
    expect(r.tokens).toContain('costume')
  })

  it('favorite -> favorite', () => {
    const r = parseSearchIntent('favorite')
    expect(r.tokens).toContain('favorite')
  })

  // Production mixed-intent baseline
  it('handles "hide shiny and favourites" correctly', () => {
    const r = parseSearchIntent('hide shiny and favourites')
    expect(r.canBuild).toBe(true)
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).toContain('favorite')
    expect(r.tokens).not.toContain('shiny')
    expect(r.tokens).not.toContain('favorite')
    expect(r.rawQuery).toBe('!shiny&!favorite')
    expect(r.rawQuery).not.toContain('|')
  })

  it('handles "Find hundos and exclude shinies" correctly', () => {
    const r = parseSearchIntent('Find hundos and exclude shinies')
    expect(r.canBuild).toBe(true)
    expect(r.tokens).toContain('4*')
    expect(r.tokens).not.toContain('shiny')
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).not.toContain('4*')
    expect(r.rawQuery).toBe('4*&!shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  it('handles "exclude shinies and find hundos" correctly', () => {
    const r = parseSearchIntent('exclude shinies and find hundos')
    expect(r.canBuild).toBe(true)
    expect(r.tokens).toContain('4*')
    expect(r.tokens).not.toContain('shiny')
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).not.toContain('4*')
    expect(r.rawQuery).toBe('4*&!shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  // PQ-ZAI-FINAL-01: Negated controls
  it('PQ-ZAI-FINAL-01: parses "don\'t hide shiny" -> positive shiny', () => {
    const r = parseSearchIntent("don't hide shiny")
    expect(r.canBuild).toBe(true)
    expect(r.tokens).toContain('shiny')
    expect(r.exclusions).not.toContain('shiny')
    expect(r.rawQuery).toBe('shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  it('PQ-ZAI-FINAL-01: parses "dont hide shiny" and "do not hide shiny"', () => {
    const r1 = parseSearchIntent('dont hide shiny')
    expect(r1.tokens).toContain('shiny')
    expect(r1.exclusions).not.toContain('shiny')
    expect(r1.rawQuery).toBe('shiny')

    const r2 = parseSearchIntent('do not hide shiny')
    expect(r2.tokens).toContain('shiny')
    expect(r2.exclusions).not.toContain('shiny')
    expect(r2.rawQuery).toBe('shiny')
  })

  it('PQ-ZAI-FINAL-01: parses "don\'t include shiny" -> negative !shiny', () => {
    const r = parseSearchIntent("don't include shiny")
    expect(r.canBuild).toBe(true)
    expect(r.exclusions).toContain('shiny')
    expect(r.tokens).not.toContain('shiny')
    expect(r.rawQuery).toBe('!shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  // PQ-ZAI-FINAL-02: Contrast with explicit positive control
  it('PQ-ZAI-FINAL-02: parses "without shiny but with hundo" -> 4*&!shiny', () => {
    const r = parseSearchIntent('without shiny but with hundo')
    expect(r.canBuild).toBe(true)
    expect(r.tokens).toContain('4*')
    expect(r.tokens).not.toContain('shiny')
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).not.toContain('4*')
    expect(r.rawQuery).toBe('4*&!shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  // PQ-ZAI-FINAL-03: Contrast polarity inheritance
  it('PQ-ZAI-FINAL-03: parses "show all but hundos" -> !4*', () => {
    const r = parseSearchIntent('show all but hundos')
    expect(r.canBuild).toBe(true)
    expect(r.exclusions).toContain('4*')
    expect(r.tokens).not.toContain('4*')
    expect(r.rawQuery).toBe('!4*')
    expect(r.rawQuery).not.toContain('|')
  })

  it('PQ-ZAI-FINAL-03: parses "find all but shiny" -> !shiny', () => {
    const r = parseSearchIntent('find all but shiny')
    expect(r.canBuild).toBe(true)
    expect(r.exclusions).toContain('shiny')
    expect(r.tokens).not.toContain('shiny')
    expect(r.rawQuery).toBe('!shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  it('PQ-ZAI-FINAL-03: parses "show all but shiny" -> !shiny', () => {
    const r = parseSearchIntent('show all but shiny')
    expect(r.canBuild).toBe(true)
    expect(r.exclusions).toContain('shiny')
    expect(r.tokens).not.toContain('shiny')
    expect(r.rawQuery).toBe('!shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  it('PQ-ZAI-FINAL-03: parses "everything but shiny and legendary" -> !shiny&!legendary', () => {
    const r = parseSearchIntent('everything but shiny and legendary')
    expect(r.canBuild).toBe(true)
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).toContain('legendary')
    expect(r.tokens).toEqual([])
    expect(r.rawQuery).toBe('!shiny&!legendary')
    expect(r.rawQuery).not.toContain('|')
  })

  it('PQ-ZAI-FINAL-03: parses "hide shiny but hundo" -> 4*&!shiny', () => {
    const r = parseSearchIntent('hide shiny but hundo')
    expect(r.canBuild).toBe(true)
    expect(r.tokens).toContain('4*')
    expect(r.tokens).not.toContain('shiny')
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).not.toContain('4*')
    expect(r.rawQuery).toBe('4*&!shiny')
    expect(r.rawQuery).not.toContain('|')
  })
})
