// Golden corpus parity test — reads and validates all test cases from golden-corpus.json
// This test file is the web side of the cross-platform parity verification.
// The Android side reads the same JSON from app/src/test/resources/golden-corpus.json.

import { describe, it, expect } from 'vitest'
import corpus from '../parity/golden-corpus.json'
import { buildString, buildGoal, DEFAULT_PROTECTIONS } from '../engine/stringBuilderEngine'
import { buildFinal } from '../engine/goalStringBuilder'
import { lint } from '../engine/linter'
import { canCopy } from '../engine/expertCopyPolicy'

interface TestCase {
  id: string
  category: string
  input: Record<string, unknown>
  expected: Record<string, unknown>
}

function checkExpected(actual: Record<string, unknown>, expected: Record<string, unknown>) {
  if ('rawSyntax' in expected) {
    expect(actual.rawSyntax).toBe(expected.rawSyntax)
  }
  if ('rawSyntaxContains' in expected) {
    const contains = expected.rawSyntaxContains as string[]
    for (const sub of contains) {
      expect(actual.rawSyntax).toContain(sub)
    }
  }
  if ('rawSyntaxNotContains' in expected) {
    const notContains = expected.rawSyntaxNotContains as string[]
    for (const sub of notContains) {
      expect(actual.rawSyntax).not.toContain(sub)
    }
  }
  if ('riskLevel' in expected) {
    expect(actual.riskLevel).toBe(expected.riskLevel)
  }
  if ('scopeBreadth' in expected) {
    expect(actual.scopeBreadth).toBe(expected.scopeBreadth)
  }
  if ('goalId' in expected) {
    expect(actual.goalId).toBe(expected.goalId)
  }
  if ('title' in expected) {
    expect(actual.title).toBe(expected.title)
  }
  if ('warningsCount' in expected) {
    expect((actual.warnings as string[]).length).toBe(expected.warningsCount)
  }
  if ('warningsContains' in expected) {
    const warnings = actual.warnings as string[]
    for (const sub of (expected.warningsContains as unknown as string[])) {
      expect(warnings.some(w => w.includes(sub))).toBe(true)
    }
  }
  if ('protectedCategories' in expected) {
    expect(actual.protectedCategories).toEqual(expected.protectedCategories)
  }
  if ('includedHighRiskCategories' in expected) {
    expect(actual.includedHighRiskCategories).toEqual(expected.includedHighRiskCategories)
  }
}

describe('Golden corpus parity', () => {
  const testCases = (corpus as typeof corpus & { testCases: TestCase[] }).testCases

  for (const tc of testCases) {
    it(tc.id, () => {
      const input = tc.input

      switch (tc.category) {
        case 'buildGoal': {
          const result = buildGoal(
            input.goalId as string,
            (input.config as string) || '',
            (input.customQuery as string) || '',
            (input.language as string) || 'English',
          )
          checkExpected(result as unknown as Record<string, unknown>, tc.expected)
          break
        }
        case 'buildString': {
          const protections =
            input.protections === 'EMPTY' ? [] :
            input.protections === 'DEFAULT' ? DEFAULT_PROTECTIONS :
            (input.protections as unknown as string[]) || DEFAULT_PROTECTIONS
          const result = buildString(
            input.baseQuery as string,
            protections,
            (input.explanation as string) || '',
            (input.riskLevel as 'Info' | 'Low' | 'Medium' | 'High') || 'Low',
            (input.goalId as string) || 'custom',
            (input.title as string) || 'Custom Search',
            (input.language as string) || 'English',
          )
          checkExpected(result as unknown as Record<string, unknown>, tc.expected)
          break
        }
        case 'lint': {
          const warnings = lint(input.query as string)
          if ('hasError' in tc.expected) {
            expect(warnings.some(w => w.isError)).toBe(tc.expected.hasError)
          }
          if ('warningsContain' in tc.expected) {
            for (const sub of tc.expected.warningsContain as string[]) {
              expect(warnings.some(w => w.message.includes(sub))).toBe(true)
            }
          }
          if ('warningsNotContain' in tc.expected) {
            for (const sub of tc.expected.warningsNotContain as string[]) {
              expect(warnings.some(w => w.message.includes(sub))).toBe(false)
            }
          }
          break
        }
        case 'expertCopyPolicy': {
          const result = canCopy(input.rawQuery as string)
          expect(result).toBe(tc.expected.canCopy)
          break
        }
        case 'goalStringBuilder': {
          const base = buildGoal(
            input.goalId as string,
            (input.config as string) || '',
            '',
            (input.language as string) || 'English',
          )
          const result = buildFinal(
            base,
            (input.optionalProtections as string[]) || [],
            (input.language as string) || 'English',
          )
          checkExpected(result as unknown as Record<string, unknown>, tc.expected)
          break
        }
      }
    })
  }
})
