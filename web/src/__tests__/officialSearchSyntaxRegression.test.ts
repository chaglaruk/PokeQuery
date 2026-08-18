import { describe, expect, it } from 'vitest'
import { buildGoal } from '../engine/stringBuilderEngine'
import { lint } from '../engine/linter'
import { translateSyntax } from '../engine/searchTermMapper'

describe('current official inventory search syntax', () => {
  it('preserves the officially documented pipe multi-criteria operator', () => {
    const generated = buildGoal('expert', '', 'shiny|legendary', 'English')
    expect(generated.rawSyntax).toBe('shiny|legendary')
    expect(lint(generated.rawSyntax).some(w => w.isError && w.message.includes('|'))).toBe(false)
  })

  it('translates localized tokens on both sides of pipe without changing the operator', () => {
    expect(translateSyntax('shiny|legendary', 'Turkish')).toBe('parlak|efsanevi')
  })

  it('does not silently add cleanup exclusions to an explicit expert filter', () => {
    expect(buildGoal('expert', '', 'shiny', 'English').rawSyntax).toBe('shiny')
  })

  it('retains mandatory count protections for action-adjacent expert queries', () => {
    const raw = buildGoal('expert', '', 'count2-', 'English').rawSyntax
    expect(raw).toContain('count2-')
    expect(raw).toContain('!shiny')
    expect(raw).toContain('!traded')
    expect(raw).toContain('!background')
  })
})
