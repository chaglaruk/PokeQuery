// SearchIntentParser — TypeScript port of Android SearchIntentParser.kt
// Parses natural-language search intent into Pokemon GO search strings.

import { parseCaughtDateIntent } from './caughtDateIntent'

export interface ParsedIntent {
  tokens: string[]
  exclusions: string[]
  rawQuery: string
  explanationKey: string
  explanation: string
  limitationKeys: string[]
  limitations: string[]
  canBuild: boolean
  hasAutoAdded: boolean
  pipeForbidden: boolean
  noteKeys: string[]
}

interface IntentPattern {
  keywords: string[]
  tokens: string[]
  exclusions?: string[]
  explanationKey: string
  explanation: string
  limitationKeys: string[]
  limitations: string[]
  canBuild?: boolean
}

function normalize(text: string): string {
  return text.toLowerCase().replace(/[\u2018\u2019\u02BC]/g, "'").trim().replace(/\s+/g, ' ')
}

function isKeywordWordChar(char: string | undefined): boolean {
  return char !== undefined && /[\p{L}\p{N}_]/u.test(char)
}

/**
 * Whole-keyword/phrase matching without JS \b semantics. This preserves punctuation-bearing
 * keywords such as 100% and 15/15/15 while blocking trade->traded, age->storage, all->small.
 */
function keywordIndex(text: string, keyword: string): number {
  if (!keyword) return -1
  let fromIndex = 0
  while (fromIndex <= text.length - keyword.length) {
    const index = text.indexOf(keyword, fromIndex)
    if (index === -1) return -1
    const end = index + keyword.length
    const beforeOk = index === 0 || !isKeywordWordChar(text[index - 1])
    const afterOk = end === text.length || !isKeywordWordChar(text[end])
    if (beforeOk && afterOk) return index
    fromIndex = index + 1
  }
  return -1
}

function containsKeyword(text: string, keyword: string): boolean {
  return keywordIndex(text, keyword) >= 0
}

type ControlPolarity = 'POSITIVE' | 'NEGATIVE'

const contrastRegex = /\b(?:but|ama|ancak|fakat|lakin)\b/gi

const negatorPrefix = `(?:don'?t|do\\s+not|doesn'?t|does\\s+not|isn'?t|is\\s+not|aren'?t|are\\s+not|can'?t|cannot|won'?t|wont|wouldn'?t|wouldnt)`
const negativeWords = `(?:hide|exclude|without|except|gizle|hariç|haric|dışında|disinda|no|not)`
const positiveWords = `(?:find|show|include|keep|want|get|with|bul|göster|goster|dahil|sakla|ile|birlikte)`

const combinedControlRegex = new RegExp(
  `\\b(?:(${negatorPrefix})\\s+(${negativeWords}|${positiveWords})|(${negativeWords})|(${positiveWords}))\\b`,
  'gi'
)

const negativeWordSet = new Set([
  'hide', 'exclude', 'without', 'except', 'gizle', 'hariç', 'haric', 'dışında', 'disinda', 'no', 'not',
])

const clauseBreak = /\b(?:and|or|ve|veya)\b|[,&;:]/
const suffixNegations = ['degil', 'değil', 'olmayan', 'yok', 'hariç', 'haric', 'disinda', 'dışında', 'excluded', 'hidden']

function isNegativeWord(word: string): boolean {
  return negativeWordSet.has(word.toLowerCase())
}

function lastControlMatch(text: string): RegExpExecArray | null {
  combinedControlRegex.lastIndex = 0
  let last: RegExpExecArray | null = null
  let m: RegExpExecArray | null
  while ((m = combinedControlRegex.exec(text)) !== null) {
    last = m
  }
  return last
}

function extractLastControl(text: string): ControlPolarity | null {
  const last = lastControlMatch(text)
  if (!last) return null

  const negator = last[1]
  const negatedWord = last[2]
  const standaloneNeg = last[3]
  const standalonePos = last[4]

  if (negator && negatedWord) {
    return isNegativeWord(negatedWord) ? 'POSITIVE' : 'NEGATIVE'
  }
  if (standaloneNeg) return 'NEGATIVE'
  if (standalonePos) return 'POSITIVE'
  return null
}

function polarityForPrefix(prefix: string): boolean {
  const trimmedPrefix = prefix.trimEnd()
  if (trimmedPrefix.endsWith('!')) return true

  contrastRegex.lastIndex = 0
  const contrastMatches: RegExpExecArray[] = []
  let cm: RegExpExecArray | null
  while ((cm = contrastRegex.exec(prefix)) !== null) {
    contrastMatches.push(cm)
  }

  if (contrastMatches.length > 0) {
    const lastContrast = contrastMatches[contrastMatches.length - 1]!
    const preContrast = prefix.substring(0, lastContrast.index)
    const postContrast = prefix.substring(lastContrast.index + lastContrast[0].length)

    const postControl = extractLastControl(postContrast)
    if (postControl !== null) {
      return postControl === 'NEGATIVE'
    }

    const preControl = extractLastControl(preContrast) ?? 'POSITIVE'
    return preControl === 'POSITIVE'
  }

  const lastControl = lastControlMatch(prefix)
  const standaloneNeg = lastControl?.[3]?.toLowerCase()
  if (lastControl && (standaloneNeg === 'not' || standaloneNeg === 'no')) {
    const afterControl = prefix.substring(lastControl.index + lastControl[0].length)
    if (clauseBreak.test(afterControl)) return false
  }

  const control = extractLastControl(prefix)
  return control === 'NEGATIVE'
}

function isPatternNegated(normalized: string, keyword: string): boolean {
  if (!keyword) return false
  const index = keywordIndex(normalized, keyword)
  if (index === -1) return false

  const prefix = normalized.substring(0, index)
  const suffix = normalized.substring(index + keyword.length)

  const prefixNegated = polarityForPrefix(prefix)

  const suffixClause = (suffix.split(clauseBreak)[0] ?? '').trim()
  const suffixNegated = suffixNegations.some(neg =>
    suffixClause === neg || suffixClause.startsWith(`${neg} `) || suffixClause.startsWith(neg)
  )
  return prefixNegated || suffixNegated
}

const patterns: IntentPattern[] = [
  {
    keywords: ['hundo', 'hundos', 'perfect', '100%', '15/15/15', '15 15 15', 'max iv', 'all 15', 'yüzde yüz', 'yuzde yuz', '100 iv', 'kusursuz', 'mükemmel', 'mukemmel', 'güçlü', 'guclu'],
    tokens: ['4*'], explanationKey: 'search_intent_expl_hundo',
    explanation: 'Finds Pokémon with perfect 15/15/15 IVs (exact 100% appraisal using 4*). Inspection only — does not filter or exclude anything.',
    limitationKeys: ['search_intent_lim_hundo_purified', 'search_intent_lim_iv_approx'],
    limitations: ['4* also matches purified Pokémon. Check manually if you want non-purified only.', 'IV appraisal is an approximation, not exact stats.'],
  },
  {
    keywords: ['nundo', 'nundos', '0%', '0/0/0', '0 0 0', 'zero iv', 'lowest', 'minimum iv', 'sıfır iv', 'sifir iv', '0 iv', 'en düşük', 'en dusuk'],
    tokens: ['0attack', '0defense', '0hp'], explanationKey: 'search_intent_expl_nundo',
    explanation: 'Finds Pokémon with 0/0/0 IVs. This is an exact match — only true 0% appraisal shows.',
    limitationKeys: ['search_intent_lim_nundo_floor'], limitations: ['IV floor events (trades, weather boost, raids) make 0% IV impossible.'],
  },
  {
    keywords: ['great league pvp', 'great league candidate', 'great league', 'büyük lig', 'buyuk lig'],
    tokens: ['0-1attack', '3-4defense', '3-4hp', 'cp-1500'], explanationKey: 'search_intent_expl_great_league',
    explanation: 'Finds Great League PvP candidates (CP <= 1500) using CP cap/shortlist logic.',
    limitationKeys: ['search_intent_lim_cp_cap_only', 'search_intent_lim_not_all_pvp'],
    limitations: ['CP cap filters by current CP only; exact PvP rank and level are not detectable via search strings.', 'Not all matches are PvP-relevant — species and moveset also matter.'],
  },
  {
    keywords: ['ultra league pvp', 'ultra league candidate', 'ultra league', 'ultra lig'],
    tokens: ['0-1attack', '3-4defense', '3-4hp', 'cp-2500'], explanationKey: 'search_intent_expl_ultra_league',
    explanation: 'Finds Ultra League PvP candidates (CP <= 2500) using CP cap/shortlist logic.',
    limitationKeys: ['search_intent_lim_cp_cap_only', 'search_intent_lim_not_all_pvp'],
    limitations: ['CP cap filters by current CP only; exact PvP rank and level are not detectable via search strings.', 'Not all matches are PvP-relevant — species and moveset also matter.'],
  },
  {
    keywords: ['pvp', 'pvp iv', 'pvp candidate', 'pvp adayı', 'pvp adayi', 'kapışma', 'kapisma', 'düello', 'duello'],
    tokens: ['0-1attack', '3-4defense', '3-4hp'], explanationKey: 'search_intent_expl_pvp_generic',
    explanation: 'Finds Pokémon with PvP-friendly IV spreads (low attack, high defense/HP). Suitable for Great League and Ultra League — exact PvP rank is not detectable via search strings; check CP manually in Pokémon GO.',
    limitationKeys: ['search_intent_lim_no_rank_via_search', 'search_intent_lim_not_all_pvp', 'search_intent_lim_no_league_cap'],
    limitations: ['Pokémon GO search cannot detect exact PvP rank or level — only IV floor/ceil values.', 'Not all matches are PvP-relevant — species and moveset also matter.', 'Does not apply a league CP cap; use specific league name for cap.'],
  },
  { keywords: ['shiny', 'shinies', 'parlak', 'şayni', 'sayni'], tokens: ['shiny'], explanationKey: 'search_intent_expl_shiny', explanation: 'Filters to show only Shiny Pokémon.', limitationKeys: ['search_intent_lim_shiny_no_variants', 'search_intent_lim_shiny_invertible'], limitations: ['Shiny search does not distinguish costume, event, or regional variants.', 'You can also use !shiny to search for non-Shiny Pokémon.'] },
  { keywords: ['legendary', 'legendaries', 'legend', 'efsane', 'efsanevi'], tokens: ['legendary'], explanationKey: 'search_intent_expl_legendary', explanation: 'Filters to show only Legendary Pokémon.', limitationKeys: ['search_intent_lim_mythical_excluded'], limitations: ['Mythical Pokémon are NOT included in this search.'] },
  { keywords: ['mythical', 'mythic', 'mitolojik', 'gizemli'], tokens: ['mythical'], explanationKey: 'search_intent_expl_mythical', explanation: 'Filters to show only Mythical Pokémon.', limitationKeys: ['search_intent_lim_mythical_risky'], limitations: ['This is a risky filter — mythical Pokémon are often valuable and cannot be re-obtained easily.'] },
  { keywords: ['shadow', 'shadows', 'gölge', 'golge', 'karanlık', 'karanlik'], tokens: ['shadow'], explanationKey: 'search_intent_expl_shadow', explanation: 'Filters to show only Shadow Pokémon.', limitationKeys: ['search_intent_lim_shadow_expensive', 'search_intent_lim_purified_excluded'], limitations: ['Shadow Pokémon are expensive to power up and cannot be traded.', 'Purified Pokémon are NOT included.'] },
  { keywords: ['purified', 'arınmış', 'arinmis', 'temizlenmiş', 'temizlenmis'], tokens: ['purified'], explanationKey: 'search_intent_expl_purified', explanation: 'Filters to show only Purified Pokémon.', limitationKeys: ['search_intent_lim_purified_dust', 'search_intent_lim_purified_tradeable', 'search_intent_lim_purified_no_reshadow'], limitations: ['Purified Pokémon cost 20% less stardust to power up.', 'Purified Pokémon can be traded — they are not blocked from trading.', 'Purified Pokémon cannot be re-shadowed.'] },
  { keywords: ['lucky', 'şanslı', 'sansli'], tokens: ['lucky'], explanationKey: 'search_intent_expl_lucky', explanation: 'Filters to show only Lucky Pokémon (received via trade with guaranteed higher IVs).', limitationKeys: ['search_intent_lim_lucky_dust', 'search_intent_lim_lucky_no_retrade', 'search_intent_lim_lucky_context_only'], limitations: ['Lucky Pokémon cost 50% less stardust to power up.', 'Lucky Pokémon cannot be traded again.', 'A Pokémon becoming Lucky is not guaranteed — it depends on trade context, not just age or distance.'] },
  { keywords: ['costume', 'event', 'hat', 'bow', 'crown', 'kostüm', 'kostum', 'şapka', 'sapka', 'etkinlik'], tokens: ['costume'], explanationKey: 'search_intent_expl_costume', explanation: 'Filters to show only Costume Pokémon.', limitationKeys: ['search_intent_lim_costume_no_evolve'], limitations: ['Costume Pokémon cannot evolve (with rare event exceptions).'] },
  { keywords: ['favorite', 'fav', 'starred', 'favourite', 'favourites', 'favorites', 'favori', 'yıldızlı', 'yildizli'], tokens: ['favorite'], explanationKey: 'search_intent_expl_favorite', explanation: 'Filters to show only your Favorite (starred) Pokémon.', limitationKeys: ['search_intent_lim_favorite_invertible'], limitations: ['You can also use !favorite to search for non-favorited Pokémon.'] },
  {
    keywords: ['cleanup', 'transfer', 'delete', 'junk', 'trash', 'bulk transfer', 'temizlik', 'çöp', 'cop', 'gönder', 'gonder'], tokens: ['1*'],
    explanationKey: 'search_intent_expl_cleanup', explanation: 'Finds low-appraisal Pokémon for cleanup or transfer. Safe Cleanup excludes protected categories by default.',
    limitationKeys: ['search_intent_lim_1star_band', 'search_intent_lim_cleanup_exclude_protected'], limitations: ['1* is an IV band (0-50%), not exact 1-star. Always review before transferring.', 'Exclude shiny, legendary, mythical, costume, shadow, lucky, and trade-relevant Pokémon.'],
  },
  {
    keywords: ['candy', 'candy prep', 'extra candy', 'transfer candy', 'şeker', 'seker', 'şeker için', 'seker icin'], tokens: ['count2-'],
    explanationKey: 'search_intent_expl_candy', explanation: 'Finds duplicate Pokémon (count >= 2) for candy generation via transfer.',
    limitationKeys: ['search_intent_lim_count_mandatory_exclusions', 'search_intent_lim_count_species'], limitations: ['Mandatory exclusions: shiny, legendary, mythical, shadow, purified, and 4*.', 'Count refers to species count, not candy. High count = many transfers needed.'],
  },
  {
    keywords: ['trade', 'trading', 'trade fodder', 'duplicate', 'extra', 'spare', 'takas', 'ticaret', 'takaslık', 'takaslik', 'fazla'], tokens: ['count2-'], exclusions: ['traded'],
    explanationKey: 'search_intent_expl_trade', explanation: 'Finds duplicate untraded Pokémon (count >= 2) for trade with friends.',
    limitationKeys: ['search_intent_lim_trade_stardust', 'search_intent_lim_special_trade_cap'], limitations: ['Trade eligibility depends on stardust cost (friendship level). High-value Pokémon still cost more.', 'Special trades (legendary, shiny, unregistered) are limited to one per day.'],
  },
  {
    keywords: ['old', 'older', 'age', '2016', '2017', '2018', 'vintage', 'eski', 'yıllık', 'yillik', 'yaşlı', 'yasli'], tokens: ['age365-'],
    explanationKey: 'search_intent_expl_old', explanation: 'Finds Pokémon you have caught/obtained at least 365 days ago (1+ year old).',
    limitationKeys: ['search_intent_lim_old_lucky_chance', 'search_intent_lim_age_catch_only'], limitations: ['Pokémon from 2016-2018 have a higher (but not guaranteed) Lucky Trade chance — a search string cannot prove Lucky eligibility.', 'Age is based on catch date, not hatch date.'],
  },
  {
    keywords: ['distance', 'far', 'far away', 'overseas', 'foreign', 'distant', 'uzak', 'mesafe', 'yurtdışı', 'yurtdisi', 'yurt dışı'], tokens: ['distance100-'],
    explanationKey: 'search_intent_expl_distance', explanation: 'Finds Pokémon traded from 100+ km away. These qualify for distance-based candy bonus on transfer.',
    limitationKeys: ['search_intent_lim_distance_no_retrade', 'search_intent_lim_distance_resets'], limitations: ['Not all distance Pokémon are tradeable again (already traded).', 'Distance resets on each trade — the last trade distance applies.'],
  },
  {
    keywords: ['untagged', 'no tag', 'not tagged', 'etiketsiz', 'etiketlenmemiş', 'etiketlenmemis', 'etiketlenmeyen', 'etiket yok'], tokens: [], exclusions: ['#'],
    explanationKey: 'search_intent_expl_untagged', explanation: 'Finds untagged Pokémon for tagging and organization. The search uses !# (NOT tag filter).',
    limitationKeys: ['search_intent_lim_untagged_none', 'search_intent_lim_untagged_first_use'], limitations: ['!# shows Pokémon WITHOUT any tags.', 'If you have never tagged, this matches everything.'],
  },
  {
    keywords: ['lucky trade', 'lucky friend', 'guaranteed lucky', 'şanslı takas', 'sansli takas', 'garanti şanslı', 'garanti sansli'], tokens: ['age365-'], exclusions: ['traded'],
    explanationKey: 'search_intent_expl_lucky_trade', explanation: 'Finds older untraded Pokémon that may qualify for Lucky Trades (12/12/12+ IV floor).',
    limitationKeys: ['search_intent_lim_lucky_2016_only', 'search_intent_lim_special_trade_cap'], limitations: ['Only Pokémon from 2016-2018 are guaranteed Lucky. Newer ones have a small chance — a search string cannot prove Lucky eligibility.', 'Can only make one Special Trade per day by default.'],
  },
  {
    keywords: ['all', 'everything', 'all pokemon', 'show all', 'hepsi', 'tümü', 'tumu', 'bütün', 'butun'], tokens: [],
    explanationKey: 'search_intent_expl_all', explanation: 'Shows all Pokémon. No filter is applied.',
    limitationKeys: ['search_intent_lim_all_slow'], limitations: ['In a large inventory, "all" may be slow to load. Use filters to narrow down.'], canBuild: false,
  },
]

function emptyIntent(explanationKey: string, text?: string): ParsedIntent {
  if (explanationKey === 'search_intent_could_not_understand') {
    return {
      tokens: [], exclusions: [], rawQuery: '', explanationKey,
      explanation: `Could not understand "${text ?? ''}". Try words like: shiny, hundo, cleanup, trade, pvp, lucky, shadow, old, costume. (Türkçe: parlak, efsanevi, temizlik, takas, gölge, eski...)`,
      limitationKeys: ['search_intent_lim_complex_use_expert'], limitations: ['PokeQuery understands common search intents. For complex queries, use the Expert Builder.'],
      canBuild: false, hasAutoAdded: false, pipeForbidden: false, noteKeys: [],
    }
  }
  return { tokens: [], exclusions: [], rawQuery: '', explanationKey: 'search_intent_empty', explanation: 'Enter a description of what you want to find.', limitationKeys: [], limitations: [], canBuild: false, hasAutoAdded: false, pipeForbidden: false, noteKeys: [] }
}

export function parseSearchIntent(text: string, today: Date = new Date()): ParsedIntent {
  const caughtMatch = parseCaughtDateIntent(text, today)
  if (caughtMatch !== null && !caughtMatch.canBuild) {
    return {
      tokens: [],
      exclusions: [],
      rawQuery: '',
      explanationKey: caughtMatch.explanationKey,
      explanation: caughtMatch.explanation,
      limitationKeys: [],
      limitations: [],
      canBuild: false,
      hasAutoAdded: false,
      pipeForbidden: text.includes('|'),
      noteKeys: [],
    }
  }

  const pipeForbidden = text.includes('|')
  const cleaned = pipeForbidden ? text.replace(/\|/g, ' ') : text
  const normalized = normalize(cleaned)
  if (!normalized && caughtMatch === null) return emptyIntent('search_intent_empty')

  const matched = patterns.filter(pattern => {
    const matchingKeywords = pattern.keywords.filter(keyword => containsKeyword(normalized, keyword))
    if (matchingKeywords.length === 0) return false

    // Suppress legacy "old" pattern if it only matched 2016/2017/2018 from caught year
    if (caughtMatch !== null && caughtMatch.canBuild && pattern.tokens.length === 1 && pattern.tokens[0] === 'age365-' && (!pattern.exclusions || pattern.exclusions.length === 0)) {
      const nonYearKeywords = pattern.keywords.filter(k => !['2016', '2017', '2018'].includes(k))
      const hasExplicitOldKeyword = nonYearKeywords.some(k => containsKeyword(normalized, k))
      if (!hasExplicitOldKeyword) {
        return false
      }
    }
    return true
  })

  if (matched.length === 0 && caughtMatch === null) return emptyIntent('search_intent_could_not_understand', text)

  const allTokens: string[] = []
  const allExclusions = new Set<string>()
  const explanationKeys: string[] = []
  const allExplanationText: string[] = []
  const limitKeys: string[] = []
  const allLimitations: string[] = []

  if (caughtMatch !== null && caughtMatch.canBuild) {
    caughtMatch.tokens.forEach(t => allTokens.push(t))
    explanationKeys.push(caughtMatch.explanationKey)
    allExplanationText.push(caughtMatch.explanation)
    limitKeys.push(...caughtMatch.limitationKeys)
    allLimitations.push(...caughtMatch.limitations)
  }

  for (const pattern of matched) {
    const matchedKeyword = pattern.keywords.find(k => containsKeyword(normalized, k)) ?? ''
    const negated = isPatternNegated(normalized, matchedKeyword)
    if (negated) {
      pattern.tokens.forEach(t => allExclusions.add(t))
      pattern.exclusions?.forEach(e => allExclusions.add(e))
    } else {
      pattern.tokens.forEach(t => allTokens.push(t))
      pattern.exclusions?.forEach(e => allExclusions.add(e))
    }
    explanationKeys.push(pattern.explanationKey)
    allExplanationText.push(pattern.explanation)
    limitKeys.push(...pattern.limitationKeys)
    allLimitations.push(...pattern.limitations)
  }

  const hasShiny = containsKeyword(normalized, 'shiny')
  const hasLegendary = containsKeyword(normalized, 'legendary')
  const hasMythical = containsKeyword(normalized, 'mythical')
  const extraTokens: string[] = []
  const tokenList = allTokens.map(t => t.toLowerCase())

  if (hasShiny && !tokenList.includes('shiny') && !Array.from(allExclusions).map(e => e.toLowerCase()).includes('shiny')) {
    if (isPatternNegated(normalized, 'shiny')) allExclusions.add('shiny')
    else { extraTokens.push('shiny'); limitKeys.push('search_intent_lim_shiny_added'); allLimitations.push('Shiny search added based on your input. Verify before transferring.') }
  }
  if (hasLegendary && !tokenList.includes('legendary') && !Array.from(allExclusions).map(e => e.toLowerCase()).includes('legendary')) {
    if (isPatternNegated(normalized, 'legendary')) allExclusions.add('legendary')
    else extraTokens.push('legendary')
  }
  if (hasMythical && !tokenList.includes('mythical') && !Array.from(allExclusions).map(e => e.toLowerCase()).includes('mythical')) {
    if (isPatternNegated(normalized, 'mythical')) allExclusions.add('mythical')
    else extraTokens.push('mythical')
  }
  extraTokens.forEach(t => allTokens.push(t))

  const noteKeys: string[] = []
  if (pipeForbidden) noteKeys.push('search_intent_pipe_forbidden')
  if (allExclusions.has('traded')) noteKeys.push('search_intent_traded_kept')

  if (allTokens.length === 0 && allExclusions.size === 0) {
    return {
      tokens: [], exclusions: [], rawQuery: '', explanationKey: explanationKeys[0] ?? 'search_intent_empty',
      explanation: allExplanationText.filter((v, i, a) => a.indexOf(v) === i).join(' '), limitationKeys: limitKeys,
      limitations: allLimitations.filter((v, i, a) => a.indexOf(v) === i), canBuild: false,
      hasAutoAdded: extraTokens.length > 0, pipeForbidden, noteKeys,
    }
  }

  const dateTokens = allTokens.filter(t => t.startsWith('year') || t.startsWith('age')).filter((v, i, a) => a.indexOf(v) === i)
  const otherTokens = allTokens.filter(t => !t.startsWith('year') && !t.startsWith('age')).filter((v, i, a) => a.indexOf(v) === i)
  const distinctTokens = [...dateTokens, ...otherTokens]

  const distinctExclusions = Array.from(allExclusions)
  const rawQuery = [...distinctTokens, ...distinctExclusions.map(e => `!${e}`)].join('&')

  return {
    tokens: distinctTokens, exclusions: distinctExclusions, rawQuery,
    explanationKey: explanationKeys[0] ?? 'search_intent_empty',
    explanation: allExplanationText.filter((v, i, a) => a.indexOf(v) === i).join(' '),
    limitationKeys: limitKeys, limitations: allLimitations.filter((v, i, a) => a.indexOf(v) === i),
    canBuild: distinctTokens.length > 0 || distinctExclusions.length > 0,
    hasAutoAdded: extraTokens.length > 0, pipeForbidden, noteKeys,
  }
}
