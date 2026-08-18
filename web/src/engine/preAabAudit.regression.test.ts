import { describe, expect, it } from 'vitest'
import { parseSearchIntent } from './searchIntentParser'
import { buildFinal } from './goalStringBuilder'
import { buildGoal, COUNT_MANDATORY_PROTECTIONS } from './stringBuilderEngine'
import { lint } from './linter'
import { explain } from './searchStringExplainer'

describe('pre-AAB audit regressions', () => {
  it('scopes negation to the matched clause', () => {
    const parsed = parseSearchIntent('Find hundos and exclude shinies')
    expect(parsed.rawQuery).toBe('4*&!shiny')
    expect(parsed.tokens).toContain('4*')
    expect(parsed.exclusions).toContain('shiny')
    expect(parsed.exclusions).not.toContain('4*')
  })

  it('does not duplicate localized protections', () => {
    const base = buildGoal('safe_cleanup', '', '', 'Turkish')
    const merged = buildFinal(base, ['shiny', 'legendary', 'traded'], 'Turkish')
    const tokens = merged.rawSyntax.split('&')
    expect(tokens.filter(t => t === '!parlak')).toHaveLength(1)
    expect(tokens.filter(t => t === '!efsanevi')).toHaveLength(1)
    expect(tokens.filter(t => t === '!takas edilen')).toHaveLength(1)
  })

  it('does not accept substring-spoofed count protections', () => {
    const other = COUNT_MANDATORY_PROTECTIONS
      .filter(p => p !== 'shiny')
      .map(p => `!${p}`)
      .join('&')
    const warnings = lint(`count2-&!shinyx&${other}`)
    expect(warnings.some(w => w.isError && w.message.includes('!shiny'))).toBe(true)
  })

  it('blocks the unsupported untraded token', () => {
    const warnings = lint('!untraded')
    expect(warnings.some(w => w.isError && w.message.includes('untraded'))).toBe(true)
  })

  it('recognizes normal count and IV range tokens', () => {
    const result = explain('count2-&0-1attack&3-4defense&3-4hp')
    expect(result.hasUnknownTokens).toBe(false)
    expect(result.tokens).toEqual(expect.arrayContaining([
      expect.objectContaining({ token: 'count2-', category: 'count_filter' }),
      expect.objectContaining({ token: '0-1attack', category: 'iv_stat' }),
      expect.objectContaining({ token: '3-4defense', category: 'iv_stat' }),
      expect.objectContaining({ token: '3-4hp', category: 'iv_stat' }),
    ]))
  })
})
