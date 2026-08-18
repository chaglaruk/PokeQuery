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

  it('PQ-ZAI-FINAL-01: parses "dont hide shiny", "do not hide shiny", "don\'t exclude shiny"', () => {
    const r1 = parseSearchIntent('dont hide shiny')
    expect(r1.tokens).toContain('shiny')
    expect(r1.exclusions).not.toContain('shiny')
    expect(r1.rawQuery).toBe('shiny')

    const r2 = parseSearchIntent('do not hide shiny')
    expect(r2.tokens).toContain('shiny')
    expect(r2.exclusions).not.toContain('shiny')
    expect(r2.rawQuery).toBe('shiny')

    const r3 = parseSearchIntent("don't exclude shiny")
    expect(r3.tokens).toContain('shiny')
    expect(r3.exclusions).not.toContain('shiny')
    expect(r3.rawQuery).toBe('shiny')
  })

  it('PQ-ZAI-FINAL-01: parses "don\'t include shiny" -> negative !shiny', () => {
    const r = parseSearchIntent("don't include shiny")
    expect(r.canBuild).toBe(true)
    expect(r.exclusions).toContain('shiny')
    expect(r.tokens).not.toContain('shiny')
    expect(r.rawQuery).toBe('!shiny')
    expect(r.rawQuery).not.toContain('|')

    const r2 = parseSearchIntent("don't show shiny")
    expect(r2.canBuild).toBe(true)
    expect(r2.exclusions).toContain('shiny')
    expect(r2.tokens).not.toContain('shiny')
    expect(r2.rawQuery).toBe('!shiny')
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

  it('PQ-ZAI-FINAL-03: parses "show all but shiny and hundos" -> !4*&!shiny', () => {
    const r = parseSearchIntent('show all but shiny and hundos')
    expect(r.canBuild).toBe(true)
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).toContain('4*')
    expect(r.tokens).toEqual([])
    expect(r.rawQuery === '!4*&!shiny' || r.rawQuery === '!shiny&!4*').toBe(true)
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

  it('PQ-ZAI-FINAL-03: parses "find hundo but exclude shiny" -> 4*&!shiny', () => {
    const r = parseSearchIntent('find hundo but exclude shiny')
    expect(r.canBuild).toBe(true)
    expect(r.tokens).toContain('4*')
    expect(r.tokens).not.toContain('shiny')
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).not.toContain('4*')
    expect(r.rawQuery).toBe('4*&!shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  it('PQ-ZAI-FINAL-03: parses "exclude shiny but find hundo" -> 4*&!shiny', () => {
    const r = parseSearchIntent('exclude shiny but find hundo')
    expect(r.canBuild).toBe(true)
    expect(r.tokens).toContain('4*')
    expect(r.tokens).not.toContain('shiny')
    expect(r.exclusions).toContain('shiny')
    expect(r.exclusions).not.toContain('4*')
    expect(r.rawQuery).toBe('4*&!shiny')
    expect(r.rawQuery).not.toContain('|')
  })

  // ==========================================
  // CAUGHT DATE INTENT TESTS (Fixed today: 2026-08-18)
  // ==========================================
  const fixedToday = new Date(2026, 7, 18) // Month 7 = August

  it('parses "find pokemon caught in April 2025" and "caught in April 2025"', () => {
    const r1 = parseSearchIntent('find pokemon caught in April 2025', fixedToday)
    expect(r1.canBuild).toBe(true)
    expect(r1.rawQuery).toBe('year2025&age475-504')
    expect(r1.rawQuery).not.toContain('|')
    expect(r1.tokens).toEqual(['year2025', 'age475-504'])
    expect(r1.limitations.some(l => l.includes('rolling 24-hour windows'))).toBe(true)

    const r2 = parseSearchIntent('caught in April 2025', fixedToday)
    expect(r2.canBuild).toBe(true)
    expect(r2.rawQuery).toBe('year2025&age475-504')
  })

  it('parses Turkish "nisan 2025te yakalanan pokemonları bul" and "nisan 2025\'te yakalanan pokemonları bul"', () => {
    const r1 = parseSearchIntent('nisan 2025te yakalanan pokemonları bul', fixedToday)
    expect(r1.canBuild).toBe(true)
    expect(r1.rawQuery).toBe('year2025&age475-504')
    expect(r1.rawQuery).not.toContain('|')

    const r2 = parseSearchIntent("nisan 2025'te yakalanan pokemonları bul", fixedToday)
    expect(r2.canBuild).toBe(true)
    expect(r2.rawQuery).toBe('year2025&age475-504')
  })

  it('parses Month only: "caught in April", "nisanda yakalanan pokemonları bul", "nisan ayında yakalanan pokemonları bul"', () => {
    const rEn = parseSearchIntent('caught in April', fixedToday)
    expect(rEn.canBuild).toBe(true)
    expect(rEn.rawQuery).toBe('year2026&age110-139')
    expect(rEn.rawQuery).not.toContain('|')

    const rTr1 = parseSearchIntent('nisanda yakalanan pokemonları bul', fixedToday)
    expect(rTr1.canBuild).toBe(true)
    expect(rTr1.rawQuery).toBe('year2026&age110-139')

    const rTr2 = parseSearchIntent('nisan ayında yakalanan pokemonları bul', fixedToday)
    expect(rTr2.canBuild).toBe(true)
    expect(rTr2.rawQuery).toBe('year2026&age110-139')
  })

  it('parses Year only: "caught in 2025", "2025te yakalanan pokemonları bul", "2025\'te yakalanan pokemonları bul"', () => {
    const rEn = parseSearchIntent('caught in 2025', fixedToday)
    expect(rEn.canBuild).toBe(true)
    expect(rEn.rawQuery).toBe('year2025')
    expect(rEn.rawQuery).not.toContain('|')

    const rTr1 = parseSearchIntent('2025te yakalanan pokemonları bul', fixedToday)
    expect(rTr1.canBuild).toBe(true)
    expect(rTr1.rawQuery).toBe('year2025')

    const rTr2 = parseSearchIntent("2025'te yakalanan pokemonları bul", fixedToday)
    expect(rTr2.canBuild).toBe(true)
    expect(rTr2.rawQuery).toBe('year2025')
  })

  it('parses bare caught requests with helpful guidance: "find caught pokemon", "yakalanan pokemonları bul"', () => {
    const rEn = parseSearchIntent('find caught pokemon', fixedToday)
    expect(rEn.canBuild).toBe(false)
    expect(rEn.rawQuery).toBe('')
    expect(rEn.explanation).toContain('caught in April 2025')

    const rTr = parseSearchIntent('yakalanan pokemonları bul', fixedToday)
    expect(rTr.canBuild).toBe(false)
    expect(rTr.rawQuery).toBe('')
    expect(rTr.explanation).toContain("Nisan 2025'te yakalanan")
  })

  it('handles caught date edge cases: current month, Dec past-year inference, future date, leap year', () => {
    // Current month: August 2026 (clamped to today: Aug 18)
    const rCurrent = parseSearchIntent('caught in August 2026', fixedToday)
    expect(rCurrent.canBuild).toBe(true)
    expect(rCurrent.rawQuery).toBe('year2026&age0-17')

    // Month-only December (inferred past year: Dec 2025)
    const rDec = parseSearchIntent('caught in December', fixedToday)
    expect(rDec.canBuild).toBe(true)
    expect(rDec.rawQuery).toBe('year2025&age230-260')

    // Future year
    const rFuture = parseSearchIntent('caught in 2030', fixedToday)
    expect(rFuture.canBuild).toBe(false)
    expect(rFuture.rawQuery).toBe('')

    // Leap year: Feb 2024 (29 days)
    const rLeap = parseSearchIntent('caught in February 2024', fixedToday)
    expect(rLeap.canBuild).toBe(true)
    expect(rLeap.rawQuery).toBe('year2024&age901-929')
  })

  it('recognizes all 12 EN and TR months in caught context', () => {
    const enMonths = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
    for (const m of enMonths) {
      const res = parseSearchIntent(`caught in ${m} 2024`, fixedToday)
      expect(res.canBuild).toBe(true)
      expect(res.rawQuery.startsWith('year2024&age')).toBe(true)
      expect(res.rawQuery).not.toContain('|')
    }

    const trMonths = ['Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran', 'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık']
    for (const m of trMonths) {
      const res = parseSearchIntent(`${m} 2024'te yakalanan`, fixedToday)
      expect(res.canBuild).toBe(true)
      expect(res.rawQuery.startsWith('year2024&age')).toBe(true)
      expect(res.rawQuery).not.toContain('|')
    }
  })

  it('verifies DST safety in days calculation', () => {
    // Cross US Daylight Saving Time start (March 8, 2026)
    const dstDay = new Date(2026, 2, 15) // March 15, 2026
    const r = parseSearchIntent('caught in March 2026', dstDay)
    expect(r.canBuild).toBe(true)
    // start 2026-03-01 to 2026-03-15 = 14 days; end clamped to 2026-03-15 = 0 days
    expect(r.rawQuery).toBe('year2026&age0-14')
  })
})