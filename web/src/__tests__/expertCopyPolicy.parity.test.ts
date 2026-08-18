// Parity test: mirrors ExpertCopyPolicyTest.kt (Android JUnit)

import { describe, it, expect } from 'vitest'
import { canCopy } from '../engine/expertCopyPolicy'

describe('ExpertCopyPolicy parity', () => {
  it('official pipe criteria operator allows copy', () => {
    expect(canCopy('shiny|lucky')).toBe(true)
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

  it('true safety errors still block copy', () => {
    expect(canCopy('shiny|lucky')).toBe(true)
    expect(canCopy('count2-')).toBe(false)
    expect(canCopy('count2-&shiny')).toBe(false)
  })
})
