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

  it('parses "shiny" → tokens [shiny]', () => {
    const r = parseSearchIntent('shiny')
    expect(r.tokens).toEqual(['shiny'])
    expect(r.canBuild).toBe(true)
    expect(r.rawQuery).toBe('shiny')
  })

  it('parses "hundo" → tokens [4*]', () => {
    const r = parseSearchIntent('hundo')
    expect(r.tokens).toEqual(['4*'])
  })

  it('parses "nundo" → tokens [0attack, 0defense, 0hp]', () => {
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

  it('parses "cleanup" → tokens [1*]', () => {
    const r = parseSearchIntent('cleanup')
    expect(r.tokens).toEqual(['1*'])
  })

  it('parses "trade" → tokens [count2-] exclusions [traded]', () => {
    const r = parseSearchIntent('trade')
    expect(r.tokens).toContain('count2-')
    expect(r.exclusions).toContain('traded')
    expect(r.rawQuery).toContain('!traded')
  })

  it('parses "untagged" → exclusions [#]', () => {
    const r = parseSearchIntent('untagged')
    expect(r.tokens).toEqual([])
    expect(r.exclusions).toEqual(['#'])
    expect(r.rawQuery).toBe('!#')
    expect(r.canBuild).toBe(true)
  })

  it('parses "all" → canBuild=false', () => {
    const r = parseSearchIntent('all')
    expect(r.canBuild).toBe(false)
  })

  it('parses Turkish "parlak" → tokens [shiny]', () => {
    const r = parseSearchIntent('parlak')
    expect(r.tokens).toEqual(['shiny'])
  })

  it('parses Turkish "temizlik" → tokens [1*]', () => {
    const r = parseSearchIntent('temizlik')
    expect(r.tokens).toEqual(['1*'])
  })

  it('parses "lucky trade" → tokens [age365-] exclusions [traded]', () => {
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

  it('negation: "not shiny" → exclusions [shiny]', () => {
    const r = parseSearchIntent('not shiny')
    expect(r.exclusions).toContain('shiny')
    expect(r.tokens).not.toContain('shiny')
  })

  it('negation Turkish: "shiny hariç" → exclusions [shiny]', () => {
    const r = parseSearchIntent('shiny hariç')
    // "hariç" triggers the global negation check
    expect(r.exclusions).toContain('shiny')
  })

  it('old → age365-', () => {
    const r = parseSearchIntent('old')
    expect(r.tokens).toContain('age365-')
  })

  it('distance → distance100-', () => {
    const r = parseSearchIntent('distance')
    expect(r.tokens).toContain('distance100-')
  })

  it('candy → count2-', () => {
    const r = parseSearchIntent('candy')
    expect(r.tokens).toContain('count2-')
  })

  it('shadow → shadow', () => {
    const r = parseSearchIntent('shadow')
    expect(r.tokens).toContain('shadow')
  })

  it('costume → costume', () => {
    const r = parseSearchIntent('costume')
    expect(r.tokens).toContain('costume')
  })

  it('favorite → favorite', () => {
    const r = parseSearchIntent('favorite')
    expect(r.tokens).toContain('favorite')
  })
})
