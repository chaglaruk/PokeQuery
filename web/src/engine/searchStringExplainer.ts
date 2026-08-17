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

interface KnownTokenInfo {
  category: string
  description: string
}

const knownTokens: Record<string, KnownTokenInfo> = {
  shiny: { category: 'shiny', description: 'Filters for Shiny Pokémon' },
  legendary: { category: 'legendary', description: 'Filters for Legendary Pokémon' },
  mythical: { category: 'mythical', description: 'Filters for Mythical Pokémon (risky — cannot be re-obtained easily)' },
  shadow: { category: 'shadow', description: 'Filters for Shadow Pokémon' },
  purified: { category: 'purified', description: 'Filters for Purified Pokémon' },
  lucky: { category: 'lucky', description: 'Filters for Lucky Pokémon' },
  favorite: { category: 'favorite', description: 'Filters for Favorite (starred) Pokémon' },
  costume: { category: 'costume', description: 'Filters for Costume Pokémon' },
  traded: { category: 'traded', description: 'Filters for Pokémon that have been traded' },
  ultrabeast: { category: 'ultrabeast', description: 'Filters for Ultra Beasts' },
  background: { category: 'background', description: 'Filters for Special Background Pokémon' },
  locationbackground: { category: 'locationbackground', description: 'Filters for Location Card Background Pokémon' },
  specialbackground: { category: 'specialbackground', description: 'Filters for Event Background Pokémon' },
  defender: { category: 'defender', description: 'Filters for Pokémon currently defending a Gym' },
  '4*': { category: 'iv_band', description: 'Perfect IV (100% appraisal)' },
  '3*': { category: 'iv_band', description: 'High IV (80-99% appraisal)' },
  '2*': { category: 'iv_band', description: 'Mid IV (50-79% appraisal)' },
  '1*': { category: 'iv_band', description: 'Low IV (0-50% appraisal) — cleanup candidate' },
  '0*': { category: 'iv_band', description: 'Lowest IV band — may include 0% IV finds' },
  age0: { category: 'age_filter', description: 'Caught today' },
  age1: { category: 'age_filter', description: 'Caught yesterday or today' },
  'age365-': { category: 'age_filter', description: 'Caught at least 365 days ago' },
  'distance100-': { category: 'distance_filter', description: 'Traded from 100+ km away' },
  'distance1000-': { category: 'distance_filter', description: 'Traded from 1000+ km away' },
  hp: { category: 'iv_stat', description: 'HP IV filter' },
  attack: { category: 'iv_stat', description: 'Attack IV filter' },
  defense: { category: 'iv_stat', description: 'Defense IV filter' },
  cp: { category: 'cp_range', description: 'CP range filter' },
  '#': { category: 'tag', description: 'Tag filter' },
}

const riskyTokens = new Set(['shiny', 'legendary', 'mythical', 'lucky'])

function structuredCategory(clean: string): string | null {
  if (/^\d+\*$/.test(clean)) return 'iv_band'
  if (/^\d+(?:-\d+)?(?:hp|attack|defense)$/.test(clean)) return 'iv_stat'
  if (/^(?:hp|attack|defense)[<>]?\d*(?:-\d*)?$/.test(clean)) return 'iv_stat'
  if (/^cp-?\d+(?:-\d*)?$/.test(clean)) return 'cp_range'
  if (/^age\d+(?:-\d*)?$/.test(clean)) return 'age_filter'
  if (/^distance\d+(?:-\d*)?$/.test(clean)) return 'distance_filter'
  if (/^count\d*(?:-\d*)?$/.test(clean)) return 'count_filter'
  if (/^@[^&,:;]+$/.test(clean)) return 'special_move'
  return null
}

function descriptionFor(category: string): string {
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
  return descriptions[category] ?? descriptions.unknown
}

function computePrecision(tokens: ExplainedToken[]): SearchPrecision {
  const cleanTokens = tokens.filter(t => !t.isExclusion).map(t => t.token.replace(/^!/, ''))
  if (cleanTokens.some(t => exactTokens.has(t))) return 'EXACT'
  if (cleanTokens.some(t => shortlistTokens.has(t))) return 'SHORTLIST'
  const categories = new Set(tokens.map(t => t.category))
  if (['iv_band', 'iv_stat', 'age_filter', 'distance_filter', 'cp_range', 'count_filter'].some(c => categories.has(c))) return 'APPROXIMATE'
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
    const known = knownTokens[clean]
    const category = known?.category ?? structuredCategory(clean) ?? 'unknown'
    const riskHint: RiskLevel = riskyTokens.has(clean) ? 'Medium' : category === 'unknown' ? 'Low' : 'Info'

    return {
      token: part,
      category,
      isExclusion,
      description: known?.description ?? descriptionFor(category),
      riskHint,
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
