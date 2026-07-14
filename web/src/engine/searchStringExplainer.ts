// Port of com.caglar.pokequery.domain.assist.SearchStringExplainer
// 1:1 TypeScript port — token-by-token search string breakdown.

import type { RiskLevel } from '../types'

export interface ExplainedToken {
  token: string
  category: string
  isExclusion: boolean
  description: string
  riskHint: RiskLevel
}

export type SearchPrecision = 'EXACT' | 'SHORTLIST' | 'APPROXIMATE' | 'NEEDS_VERIFICATION' | 'UNKNOWN'

export interface ExplainedString {
  original: string
  tokens: ExplainedToken[]
  totalRisk: RiskLevel
  hasUnknownTokens: boolean
  summary: string
  precision: SearchPrecision
  precisionLabel: string
  scopeBreadth: string
}

const exactTokens = new Set(['4*', '0attack', '0defense', '0hp', 'nundo'])
const shortlistTokens = new Set(['shiny', 'legendary', 'shadow', 'purified', 'lucky', 'favorite', 'costume', 'traded'])
// approximateTokens is defined in the Kotlin source but unused (kept for parity documentation)
// const approximateTokens = new Set(['3*', '2*', '1*', '0*', 'age', 'distance', 'count', 'cp'])

const knownTokens: Record<string, string> = {
  shiny: 'Filters for Shiny Pokémon',
  legendary: 'Filters for Legendary Pokémon',
  mythical: 'Filters for Mythical Pokémon (risky — cannot be re-obtained easily)',
  shadow: 'Filters for Shadow Pokémon',
  purified: 'Filters for Purified Pokémon',
  lucky: 'Filters for Lucky Pokémon',
  favorite: 'Filters for Favorite (starred) Pokémon',
  costume: 'Filters for Costume Pokémon',
  traded: 'Filters for Pokémon that have been traded',
  ultrabeast: 'Filters for Ultra Beasts',
  background: 'Filters for Special Background Pokémon',
  locationbackground: 'Filters for Location Card Background Pokémon',
  specialbackground: 'Filters for Event Background Pokémon',
  defender: 'Filters for Pokémon currently defending a Gym',
  '4*': 'Perfect IV (100% appraisal)',
  '3*': 'High IV (80-99% appraisal)',
  '2*': 'Mid IV (50-79% appraisal)',
  '1*': 'Low IV (0-50% appraisal) — cleanup candidate',
  '0*': 'Lowest IV band — may include 0% IV finds',
  age0: 'Caught today',
  age1: 'Caught yesterday or today',
  'age365-': 'Caught at least 365 days ago',
  'distance100-': 'Traded from 100+ km away',
  'distance1000-': 'Traded from 1000+ km away',
  hp: 'HP IV filter',
  attack: 'Attack IV filter',
  defense: 'Defense IV filter',
  cp: 'CP range filter',
  '!': 'NOT / exclusion operator — must not have this tag',
  '&': 'AND operator — all conditions must match',
  '#': 'Tag filter',
}

const riskyTokens = new Set(['shiny', 'legendary', 'mythical', 'lucky'])

function computePrecision(tokens: ExplainedToken[]): SearchPrecision {
  const cleanTokens = tokens.filter(t => !t.isExclusion).map(t => t.token)
  if (cleanTokens.some(t => exactTokens.has(t))) return 'EXACT'
  if (cleanTokens.some(t => shortlistTokens.has(t))) return 'SHORTLIST'
  const categories = new Set(tokens.map(t => t.category))
  if (['iv_band', 'age_filter', 'distance_filter', 'cp_range', 'count_filter'].some(c => categories.has(c))) return 'APPROXIMATE'
  return 'NEEDS_VERIFICATION'
}

function computeScopeBreadth(tokens: ExplainedToken[]): string {
  const cleanCount = tokens.filter(t => !t.isExclusion).length
  if (cleanCount === 0) return 'All (no filter)'
  if (cleanCount <= 1) return 'Very Narrow'
  if (cleanCount <= 2) return 'Narrow'
  if (cleanCount <= 3) return 'Moderate'
  return 'Broad'
}

export function explain(input: string): ExplainedString {
  const raw = input.trim()
  if (raw.length === 0) {
    return { original: '', tokens: [], totalRisk: 'Info', hasUnknownTokens: false, summary: 'Empty search string', precision: 'NEEDS_VERIFICATION', precisionLabel: 'Needs verification', scopeBreadth: 'All (no filter)' }
  }

  const parts = raw.split('&').filter(p => p.trim().length > 0)

  const tokens: ExplainedToken[] = parts.map(part => {
    const isExclusion = part.startsWith('!')
    const clean = isExclusion ? part.slice(1) : part

    const knownKey = Object.keys(knownTokens).find(key => clean === key || clean.startsWith(key))

    if (knownKey) {
      const risk: RiskLevel = riskyTokens.has(knownKey) ? 'Medium' : 'Info'
      return { token: part, category: knownKey, isExclusion, description: knownTokens[knownKey], riskHint: risk }
    }

    let category = 'unknown'
    if (/^\d+\*$/.test(clean)) category = 'iv_band'
    else if (/^(hp|attack|defense)[<>]?\d*$/.test(clean)) category = 'iv_stat'
    else if (/^cp-?\d*$/.test(clean)) category = 'cp_range'
    else if (/^age\d+$/.test(clean)) category = 'age_filter'
    else if (/^distance\d+$/.test(clean)) category = 'distance_filter'
    else if (/^count\d*$/.test(clean)) category = 'count_filter'
    else if (/^@\w*$/.test(clean)) category = 'special_move'

    const descriptions: Record<string, string> = {
      iv_band: 'IV appraisal band filter',
      iv_stat: 'Individual IV stat filter',
      cp_range: 'CP range filter',
      age_filter: 'Age (days since caught) filter',
      distance_filter: 'Trade distance filter',
      count_filter: 'Species count filter',
      special_move: 'Special move / form filter',
      unknown: 'Unknown token — verify this works in Pokémon GO',
    }

    return {
      token: part,
      category,
      isExclusion,
      description: descriptions[category] ?? descriptions.unknown,
      riskHint: category === 'unknown' ? 'Low' : 'Info',
    }
  })

  const hasUnknown = tokens.some(t => t.category === 'unknown')
  const risks = tokens.map(t => t.riskHint)
  const totalRisk: RiskLevel = risks.includes('Medium') ? 'Medium' : risks.includes('Low') ? 'Low' : 'Info'

  const inclusions = tokens.filter(t => !t.isExclusion)
  const exclusions = tokens.filter(t => t.isExclusion)

  let summary = 'This search string'
  if (inclusions.length > 0) summary += ` looks for ${inclusions.map(t => t.token).join(', ')}`
  if (exclusions.length > 0) summary += ` and excludes ${exclusions.map(t => t.token).join(', ')}`
  if (inclusions.length === 0 && exclusions.length === 0) summary += ' has no recognized tokens'

  const precision = computePrecision(tokens)
  const precisionLabel = precision === 'EXACT' ? 'Exact search'
    : precision === 'SHORTLIST' ? 'Shortlist'
    : precision === 'APPROXIMATE' ? 'Approximate'
    : precision === 'NEEDS_VERIFICATION' ? 'Needs verification'
    : 'Unknown'

  const scope = computeScopeBreadth(tokens)

  return { original: raw, tokens, totalRisk, hasUnknownTokens: hasUnknown, summary, precision, precisionLabel, scopeBreadth: scope }
}
