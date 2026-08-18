import { describe, expect, it } from 'vitest'
import { buildGoal } from '../engine/stringBuilderEngine'
import { lint } from '../engine/linter'
import { translateSyntax } from '../engine/searchTermMapper'

describe('current official inventory search syntax', () => {
  it('keeps generated PokeQuery syntax pipe-free even though the game documents pipe', () => {
    const generated = buildGoal('expert', '', 'shiny|legendary', 'English')
    expect(generated.rawSyntax).toBe('shiny,legendary')
    expect(generated.rawSyntax).not.toContain('|')
    expect(lint('shiny|legendary').some(w => w.isError && w.message.includes('|'))).toBe(true)
  })

  it('localized canonical generated output remains pipe-free', () => {
    expect(buildGoal('expert', '', 'shiny|legendary', 'Turkish').rawSyntax).toBe('parlak,efsanevi')
    // Mapper itself remains syntax-preserving; generator/copy policy owns the PokeQuery no-pipe rule.
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
