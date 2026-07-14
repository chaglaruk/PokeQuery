// Parity test for SearchStringExplainer — mirrors Android behavior

import { describe, it, expect } from 'vitest'
import { explain } from '../engine/searchStringExplainer'

describe('SearchStringExplainer parity', () => {
  it('empty string returns empty result', () => {
    const result = explain('')
    expect(result.original).toBe('')
    expect(result.tokens).toHaveLength(0)
    expect(result.totalRisk).toBe('Info')
    expect(result.hasUnknownTokens).toBe(false)
  })

  it('4* is exact precision and very narrow scope', () => {
    const result = explain('4*')
    expect(result.tokens).toHaveLength(1)
    expect(result.tokens[0].token).toBe('4*')
    expect(result.tokens[0].category).toBe('4*')
    expect(result.tokens[0].isExclusion).toBe(false)
    expect(result.precision).toBe('EXACT')
    expect(result.scopeBreadth).toBe('Very Narrow')
  })

  it('shiny is shortlist precision and tagged medium risk', () => {
    const result = explain('shiny')
    expect(result.tokens[0].category).toBe('shiny')
    expect(result.tokens[0].riskHint).toBe('Medium')
    expect(result.precision).toBe('SHORTLIST')
  })

  it('0attack&0defense&0hp is exact precision with 3 tokens', () => {
    const result = explain('0attack&0defense&0hp')
    expect(result.tokens).toHaveLength(3)
    expect(result.precision).toBe('EXACT')
    expect(result.scopeBreadth).toBe('Moderate')
  })

  it('count2- is categorized as unknown (regex count\\d* does not match trailing -)', () => {
    const result = explain('count2-')
    expect(result.tokens).toHaveLength(1)
    expect(result.tokens[0].category).toBe('unknown')
    expect(result.hasUnknownTokens).toBe(true)
  })

  it('!shiny is an exclusion token', () => {
    const result = explain('1*&!shiny')
    expect(result.tokens).toHaveLength(2)
    const shinyTok = result.tokens.find(t => t.token === '!shiny')
    expect(shinyTok?.isExclusion).toBe(true)
    expect(shinyTok?.category).toBe('shiny')
    const oneStarTok = result.tokens.find(t => t.token === '1*')
    expect(oneStarTok?.isExclusion).toBe(false)
  })

  it('unknown token is flagged', () => {
    const result = explain('foobar')
    expect(result.tokens[0].category).toBe('unknown')
    expect(result.hasUnknownTokens).toBe(true)
    expect(result.precision).toBe('NEEDS_VERIFICATION')
  })

  it('summary mentions inclusions and exclusions', () => {
    const result = explain('4*&!shiny&!legendary')
    expect(result.summary).toContain('4*')
    expect(result.summary).toContain('!shiny')
    expect(result.summary).toContain('!legendary')
  })

  it('total risk is Medium when any token is Medium', () => {
    const result = explain('shiny&legendary')
    expect(result.totalRisk).toBe('Medium')
  })

  it('total risk is Info when all tokens are Info', () => {
    const result = explain('4*')
    expect(result.totalRisk).toBe('Info')
  })

  it('0* is a known token (category 0*)', () => {
    const result = explain('0*')
    expect(result.tokens[0].category).toBe('0*')
  })

  it('1* is a known token with NEEDS_VERIFICATION precision', () => {
    const result = explain('1*')
    expect(result.tokens[0].category).toBe('1*')
    expect(result.precision).toBe('NEEDS_VERIFICATION')
  })

  it('age0 is a known token filter', () => {
    const result = explain('age0')
    expect(result.tokens[0].category).toBe('age0')
  })

  it('distance100- is a known token with NEEDS_VERIFICATION precision', () => {
    const result = explain('distance100-')
    expect(result.tokens[0].category).toBe('distance100-')
    expect(result.precision).toBe('NEEDS_VERIFICATION')
  })

  it('exclusion-only string has All (no filter) scope', () => {
    const result = explain('!shiny&!legendary')
    expect(result.scopeBreadth).toBe('All (no filter)')
  })
})
