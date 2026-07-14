// Port of com.caglar.pokequery.domain.engine.StringBuilderEngine
// 1:1 parity with the Kotlin implementation.

import type { GeneratedString, RiskLevel } from '../types'
import { findUnverifiedTokens, translateSyntax } from './searchTermMapper'

export const DEFAULT_PROTECTIONS: string[] = [
  'shiny', 'legendary', 'mythical', 'ultrabeast', 'costume',
  'background', 'locationbackground', 'specialbackground',
  'shadow', 'purified', 'favorite', 'lucky', '#', 'traded', '4*',
]

export const COUNT_MANDATORY_PROTECTIONS: string[] = [
  'shiny', 'lucky', 'legendary', 'mythical', 'shadow', 'purified',
  'favorite', 'traded', 'costume',
  'ultrabeast', 'background', 'locationbackground', 'specialbackground',
]

interface GoalSpec {
  query: string
  explanation: string
  risk: RiskLevel
  title: string
  protections: string[]
}

export function buildString(
  baseQuery: string,
  protections: string[] = DEFAULT_PROTECTIONS,
  explanation: string,
  riskLevel: RiskLevel = 'Low',
  goalId: string = 'custom',
  title: string = 'Custom Search',
  language: string = 'English',
): GeneratedString {
  let query = baseQuery
  const generatedWarnings: string[] = []

  if (query.includes('|')) {
    query = query.replace(/\|/g, ',')
    generatedWarnings.push("The '|' operator is unsupported and was replaced with ','.")
  }

  const protectionsToAdd = protections.filter(p => !baseQuery.includes(`!${p}`))
  if (protectionsToAdd.length > 0) {
    const protectionStr = protectionsToAdd.map(p => `!${p}`).join('&')
    query = query.length === 0 ? protectionStr : `${query}&${protectionStr}`
  }

  if (query.includes('count')) {
    const missingExclusions = COUNT_MANDATORY_PROTECTIONS.filter(p => !query.includes(`!${p}`))
    if (missingExclusions.length > 0) {
      const protectionStr = missingExclusions.map(p => `!${p}`).join('&')
      query = `${query}&${protectionStr}`
    }
    generatedWarnings.push('Count output: Count is based on Pokédex species number and may not distinguish shiny/form/costume differences.')
  }

  if (goalId === 'trade_fodder') {
    generatedWarnings.push('Trade disclaimer: Real trade eligibility depends on friendship level and cannot be guaranteed by search strings.')
  }

  const unverified = findUnverifiedTokens(query, language)
  if (unverified.length > 0) {
    generatedWarnings.push('Some search terms are unverified and will fall back to English.')
  }

  const protectedCategories = [...DEFAULT_PROTECTIONS, ...COUNT_MANDATORY_PROTECTIONS]
    .filter((v, i, a) => a.indexOf(v) === i)
    .filter(p => query.includes(`!${p}`))

  const rawSyntax = translateSyntax(query, language)

  return {
    rawSyntax,
    plainLanguageExplanation: explanation,
    protectedCategories,
    includedHighRiskCategories: DEFAULT_PROTECTIONS.filter(p => !query.includes(`!${p}`)),
    riskLevel: query.includes('count') && riskLevel === 'Low' ? 'Medium' : riskLevel,
    warnings: generatedWarnings,
    goalId,
    title,
    scopeBreadth: calculateScopeBreadth(query),
  }
}

function calculateScopeBreadth(query: string): string {
  const lower = query.toLowerCase()
  if (lower === '4*' || lower === '0attack&0defense&0hp') return 'Very Narrow'
  if (lower.includes('0-1attack') || lower.includes('3-4defense') || lower.includes('cp-1500') || lower.includes('cp-2500')) return 'Narrow'
  if (lower.includes('count2-') && lower.includes('!traded')) return 'Broad'
  if (lower.includes('count2-')) return 'Broad'
  if (lower.includes('age365-') || lower.includes('distance100-')) return 'Moderate'
  if (lower.includes('!shiny') && lower.includes('!legendary')) return 'Moderate'
  if (lower.length === 0 || (lower.split('&').length === 1 && !lower.includes('!'))) return 'Very Broad'
  return 'Moderate'
}

export function buildGoal(
  goalId: string,
  config: string = '',
  customQuery: string = '',
  language: string = 'English',
): GeneratedString {
  let spec: GoalSpec

  switch (goalId) {
    case 'safe_cleanup':
      spec = {
        query: config === 'include0Star' ? '0*,1*' : '1*',
        explanation: 'This is a REVIEW string targeting low-value candidates. It is not an automatic transfer command.',
        risk: 'Medium',
        title: 'Safe Cleanup',
        protections: DEFAULT_PROTECTIONS,
      }
      break
    case 'candy_prep':
      spec = {
        query: 'count2-',
        explanation: 'Finds extras. Count is based on Pokédex species number and may not distinguish shiny/form/costume differences.',
        risk: 'Medium',
        title: '2x Candy Prep',
        protections: DEFAULT_PROTECTIONS,
      }
      break
    case 'trade_fodder':
      spec = {
        query: 'count2-&!traded',
        explanation: 'Finds candidates for trading. Real trade eligibility depends on friendship level and cannot be guaranteed by search strings.',
        risk: 'Medium',
        title: 'Trade Fodder',
        protections: DEFAULT_PROTECTIONS,
      }
      break
    case 'hundo_check':
      spec = {
        query: '4*',
        explanation: 'Finds all perfect IV / hundo Pokémon. 4★ means 15/15/15.',
        risk: 'Info',
        title: 'Hundo Check',
        protections: [],
      }
      break
    case 'untagged':
      spec = {
        query: '!#',
        explanation: 'Finds Pokémon without any tags.',
        risk: 'Low',
        title: 'Untagged Cleanup',
        protections: DEFAULT_PROTECTIONS,
      }
      break
    case 'nundo_finder':
      spec = {
        query: '0attack&0defense&0hp',
        explanation: 'Finds exact 0/0/0 IV Pokémon (Nundos).',
        risk: 'Info',
        title: 'Nundo Finder',
        protections: [],
      }
      break
    case 'pvp_candidates': {
      const pvpQuery = config === 'ultra' ? '0-1attack&3-4defense&3-4hp&cp-2500' : '0-1attack&3-4defense&3-4hp&cp-1500'
      spec = {
        query: pvpQuery,
        explanation: 'Candidate search only. Final PvP rank depends on species, level, and IV spread.',
        risk: 'Info',
        title: 'PvP IV Candidates',
        protections: [],
      }
      break
    }
    case 'lucky_trade': {
      const tradeQuery = config === 'distance' ? 'distance100-&!traded' : 'age365-&!traded'
      spec = {
        query: tradeQuery,
        explanation: 'Finds older or distance-relevant Pokémon to review for trades. Review manually. Valuable Pokémon may appear. Trade eligibility and Lucky chance are not guaranteed by search strings.',
        risk: 'Medium',
        title: 'Lucky Trade Prep',
        protections: [],
      }
      break
    }
    case 'expert':
      spec = {
        query: customQuery,
        explanation: 'Custom search string. Review all matches in the game before acting.',
        risk: 'Medium',
        title: 'Custom Search',
        protections: DEFAULT_PROTECTIONS,
      }
      break
    default:
      spec = {
        query: customQuery,
        explanation: 'Custom search string.',
        risk: 'Medium',
        title: 'Custom Search',
        protections: DEFAULT_PROTECTIONS,
      }
      break
  }

  return buildString(
    spec.query,
    spec.protections,
    spec.explanation,
    spec.risk,
    goalId,
    spec.title,
    language,
  )
}
