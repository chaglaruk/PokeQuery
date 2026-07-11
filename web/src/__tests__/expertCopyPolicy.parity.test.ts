// Parity test: mirrors ExpertCopyPolicyTest.kt (Android JUnit)

import { describe, it, expect } from 'vitest'
import { canCopy } from '../engine/expertCopyPolicy'

describe('ExpertCopyPolicy parity', () => {
  it('pipe operator blocks copy', () => {
    expect(canCopy('shiny|lucky')).toBe(false)
  })

  it('unsafe bare count blocks copy', () => {
    expect(canCopy('count')).toBe(false)
  })

  it('empty query does not block copy', () => {
    expect(canCopy('')).toBe(true)
  })

  it('advisory-only warnings do not block copy', () => {
    expect(canCopy('0*')).toBe(true)
  })

  it('clean safe query does not block copy', () => {
    expect(canCopy('4*&!shiny')).toBe(true)
  })

  it('lucky and traded positive filters do not block copy', () => {
    expect(canCopy('lucky,traded')).toBe(true)
    expect(canCopy('lucky&traded')).toBe(true)
  })

  it('advisory risky positive filter does not block copy', () => {
    expect(canCopy('shiny')).toBe(true)
    expect(canCopy('legendary')).toBe(true)
  })

  it('true error still blocks copy after fix 7', () => {
    expect(canCopy('shiny|lucky')).toBe(false)
    expect(canCopy('count2-')).toBe(false)
    expect(canCopy('count2-&shiny')).toBe(false)
  })
})
