// Port of com.caglar.pokequery.domain.engine.GoalStringBuilder

import type { GeneratedString } from '../types'
import { translateSyntax } from './searchTermMapper'

const passthroughGoals = new Set(['hundo_check', 'nundo_finder', 'pvp_candidates'])

export function buildFinal(
  baseGoal: GeneratedString,
  optionalProtections: string[],
  language: string = 'English',
): GeneratedString {
  if (passthroughGoals.has(baseGoal.goalId)) return baseGoal

  const existing = baseGoal.rawSyntax
  const alreadyPresent = optionalProtections.filter(p => existing.includes(`!${p}`))
  const toAdd = optionalProtections
    .filter(p => !existing.includes(`!${p}`))
    .map(token => `!${translateSyntax(token, language)}`)
    .join('&')

  if (alreadyPresent.length === 0 && toAdd.length === 0) return baseGoal

  const merged = toAdd.length === 0 ? existing : `${existing}&${toAdd}`
  return { ...baseGoal, rawSyntax: merged }
}
