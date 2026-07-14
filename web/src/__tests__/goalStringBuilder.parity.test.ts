// Parity test: mirrors GoalStringBuilderTest.kt (Android JUnit)

import { describe, it, expect } from 'vitest'
import { buildGoal } from '../engine/stringBuilderEngine'
import { buildFinal } from '../engine/goalStringBuilder'

function baseGoal(goalId: string, config: string = '', language: string = 'English') {
  return buildGoal(goalId, config, '', language)
}

describe('GoalStringBuilder parity', () => {
  it('trade_fodder final string always contains !traded', () => {
    const result = buildFinal(baseGoal('trade_fodder'), [])
    expect(result.rawSyntax).toContain('!traded')
  })

  it('lucky_trade final string always contains !traded', () => {
    const result = buildFinal(baseGoal('lucky_trade', 'age'), [])
    expect(result.rawSyntax).toContain('!traded')
  })

  it('optional protections never remove engine-mandated terms', () => {
    const result = buildFinal(baseGoal('trade_fodder'), [])
    expect(result.rawSyntax).toContain('!traded')
    expect(result.rawSyntax).toContain('count2-')
  })

  it('optional protections extend a trade goal without dropping !traded', () => {
    const result = buildFinal(baseGoal('trade_fodder'), ['shiny', 'legendary'])
    expect(result.rawSyntax).toContain('!traded')
    expect(result.rawSyntax).toContain('!shiny')
    expect(result.rawSyntax).toContain('!legendary')
  })

  it('no duplicate !traded is produced for trade goals', () => {
    const result = buildFinal(baseGoal('trade_fodder'), ['traded'])
    expect(result.rawSyntax).not.toContain('!traded&!traded')
    expect(result.rawSyntax).toContain('!traded')
  })

  it('safe_cleanup keeps its positive condition and added protections', () => {
    const result = buildFinal(baseGoal('safe_cleanup'), ['shiny', '4*'])
    expect(result.rawSyntax).toContain('1*')
    expect(result.rawSyntax).toContain('!shiny')
    expect(result.rawSyntax).toContain('!4*')
  })

  it('hundo_check passes through unchanged', () => {
    const result = buildFinal(baseGoal('hundo_check'), [])
    expect(result.rawSyntax).toBe('4*')
  })

  it('pvp_candidates passes through unchanged', () => {
    const result = buildFinal(baseGoal('pvp_candidates', 'great'), [])
    expect(result.rawSyntax).toBe('0-1attack&3-4defense&3-4hp&cp-1500')
  })
})
