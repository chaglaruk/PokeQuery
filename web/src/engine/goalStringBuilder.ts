// Port of com.caglar.pokequery.domain.engine.GoalStringBuilder

import type { GeneratedString } from '../types'
import { translateSyntax } from './searchTermMapper'

const passthroughGoals = new Set(['hundo_check', 'nundo_finder', 'pvp_candidates'])

function syntaxTokens(raw: string): Set<string> {
  return new Set(
    raw.split(/[&,;:]/)
      .map(token => token.trim())
      .filter(Boolean),
  )
}

export function buildFinal(
  baseGoal: GeneratedString,
  optionalProtections: string[],
  language: string = 'English',
): GeneratedString {
  if (passthroughGoals.has(baseGoal.goalId)) return baseGoal

  const existing = baseGoal.rawSyntax
  const existingTokens = syntaxTokens(existing)
  const translatedProtections = [...new Set(optionalProtections)]
    .map(token => `!${translateSyntax(token, language)}`)

  const toAdd = translatedProtections
    .filter(token => !existingTokens.has(token))
    .join('&')

  if (!toAdd) return baseGoal

  const merged = existing ? `${existing}&${toAdd}` : toAdd
  return { ...baseGoal, rawSyntax: merged }
}
