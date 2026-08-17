// SearchIntentParser - TypeScript port of Android SearchIntentParser.kt
// Parses natural-language search intent into Pokemon GO search strings.

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
  return text.toLowerCase().trim().replace(/\s+/g, ' ')
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

function extractLastControl(text: string): ControlPolarity | null {
  combinedControlRegex.lastIndex = 0
  const matches: RegExpExecArray[] = []
  let m: RegExpExecArray | null
  while ((m = combinedControlRegex.exec(text)) !== null) {
    matches.push(m)
  }
  const last = matches[matches.length - 1]
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

  const control = extractLastControl(prefix)
  return control === 'NEGATIVE'
}

function isPatternNegated(normalized: string, keyword: string): boolean {
  if (!keyword) return false
  const index = normalized.indexOf(keyword)
  if (index === -1) return false

  const prefix = normalized.substring(0, index)
  const suffix = normalized.substring(index + keyword.length)

  const prefixNegated = polarityForPrefix(prefix)

  const suffixClause = suffix.split(clauseBreak)[0]?.trim() ?? ''
  const suffixNegated = suffixNegations.some(neg =>
    suffixClause === neg || suffixClause.startsWith(`${neg} `) || suffixClause.startsWith(neg)
  )
  return prefixNegated || suffixNegated
}

const patterns: IntentPattern[] = [
  {
    keywords: ['hundo', 'perfect', '100%', '15/15/15', '15 15 15', 'max iv', 'all 15', 'yüzde yüz', 'yuzde yuz', '100 iv', 'kusursuz', 'mükemmel', 'mukemmel', 'güçlü', 'guclu'],
    tokens: ['4*'], explanationKey: 'search_intent_expl_hundo', explanation: 'Finds Pokémon with perfect 15/15/15 IVs (exact 100% appraisal using 4*). Inspection only — does not filter or exclude anything.',
    limitationKeys: ['search_intent_lim_hundo_purified', 'search_intent_lim_iv_approx'], limitations: ['4* also matches purified Pokémon. Check manually if you want non-purified only.', 'IV appraisal is an approximation, not exact stats.'],
  },
  {
    keywords: ['nundo', '0%', '0/0/0', '0 0 0', 'zero iv', 'lowest', 'minimum iv', 'sıfır iv', 'sifir iv', '0 iv', 'en düşük', 'en dusuk'],
    tokens: ['0attack', '0defense', '0hp'], explanationKey: 'search_intent_expl_nundo', explanation: 'Finds Pokémon with 0/0/0 IVs. This is an exact match — only true 0% appraisal shows.',
    limitationKeys: ['search_intent_lim_nundo_floor'], limitations: ['IV floor events (trades, weather boost, raids) make 0% IV impossible.'],
  },
  {
    keywords: ['great league pvp', 'great league candidate', 'great league', 'büyük lig', 'buyuk lig'],
    tokens: ['0-1attack', '3-4defense', '3-4hp', 'cp-1500'], explanationKey: 'search_intent_expl_great_league', explanation: 'Finds Pokémon with low attack and high defense/HP IVs, capped at CP 1500 for Great League PvP.',
    limitationKeys: ['search_intent_lim_pvp_rank_varies', 'search_intent_lim_pvp_evolved'], limitations: ['Rank 1 PvP IV spreads vary per species (some prefer 0/15/15, others 0/14/13). Use an external PvP ranker for exact ranks.', 'Does not check evolved forms — a 0/15/15 base form may need CP checking.'],
  },
  {
    keywords: ['ultra league pvp', 'ultra league candidate', 'ultra league', 'ultra lig'],
    tokens: ['0-1attack', '3-4defense', '3-4hp', 'cp-2500'], explanationKey: 'search_intent_expl_ultra_league', explanation: 'Finds Pokémon with low attack and high defense/HP IVs, capped at CP 2500 for Ultra League PvP.',
    limitationKeys: ['search_intent_lim_pvp_rank_varies', 'search_intent_lim_pvp_evolved'], limitations: ['Rank 1 PvP IV spreads vary per species. Use an external PvP ranker for exact ranks.', 'Does not check evolved forms.'],
  },
  {
    keywords: ['pvp', 'pvp iv', 'gbl', 'battle league', 'go battle league', 'pvp adayı', 'pvp adayi'],
    tokens: ['0-1attack', '3-4defense', '3-4hp'], explanationKey: 'search_intent_expl_pvp_general', explanation: 'Finds Pokémon with low attack and high defense/HP IVs (stat product optimization for Great/Ultra League).',
    limitationKeys: ['search_intent_lim_pvp_rank_varies', 'search_intent_lim_pvp_master_15'], limitations: ['Different species have different rank 1 IV spreads. This is a shortlist, not a guarantee.', 'Master League requires 15/15/15 (use Hundo search instead).'],
  },
  {
    keywords: ['shiny', 'shinies', 'parlak', 'schillernd', 'chromatique', 'cromatico', 'brillante'],
    tokens: ['shiny'], explanationKey: 'search_intent_expl_shiny', explanation: 'Filters to show only Shiny Pokémon.',
    limitationKeys: ['search_intent_lim_shiny_variants', 'search_intent_lim_not_shiny'], limitations: ['Shiny search does not distinguish costume, event, or regional variants.', 'You can also use !shiny to search for non-Shiny Pokémon.'],
  },
  {
    keywords: ['legendary', 'legendaries', 'legend', 'efsanevi', 'legendaer', 'leggendario'],
    tokens: ['legendary'], explanationKey: 'search_intent_expl_legendary', explanation: 'Filters to show only Legendary Pokémon.',
    limitationKeys: ['search_intent_lim_mythical_not_included'], limitations: ['Mythical Pokémon are NOT included in this search.'],
  },
  {
    keywords: ['mythical', 'mythicals', 'mitik', 'mytisch', 'mítico', 'mitico'],
    tokens: ['mythical'], explanationKey: 'search_intent_expl_mythical', explanation: 'Filters to show only Mythical Pokémon.',
    limitationKeys: ['search_intent_lim_mythical_no_trade'], limitations: ['Mythical Pokémon cannot be traded (except Meltan/Melmetal).'],
  },
  {
    keywords: ['shadow', 'shadows', 'gölge', 'golge', 'erloest', 'obscur', 'ombra'],
    tokens: ['shadow'], explanationKey: 'search_intent_expl_shadow', explanation: 'Filters to show only Shadow Pokémon.',
    limitationKeys: ['search_intent_lim_shadow_no_trade', 'search_intent_lim_shadow_damage'], limitations: ['Shadow Pokémon cannot be traded.', 'Shadow Pokémon deal +20% damage but take +20% defense penalty.'],
  },
  {
    keywords: ['purified', 'arındırılmış', 'arindirilmis', 'purifie', 'purificato'],
    tokens: ['purified'], explanationKey: 'search_intent_expl_purified', explanation: 'Filters to show only Purified Pokémon.',
    limitationKeys: ['search_intent_lim_purified_discount'], limitations: ['Purified Pokémon cost less candy and stardust to power up.'],
  },
  {
    keywords: ['costume', 'costumes', 'kostüm', 'kostum', 'costumato'],
    tokens: ['costume'], explanationKey: 'search_intent_expl_costume', explanation: 'Filters to show only Costume/Event Pokémon.',
    limitationKeys: ['search_intent_lim_costume_no_evolve'], limitations: ['Some costume Pokémon cannot be evolved.'],
  },
  {
    keywords: ['favorite', 'favorites', 'favourite', 'favourites', 'fav', 'favori', 'favorit', 'favorito'],
    tokens: ['favorite'], explanationKey: 'search_intent_expl_favorite', explanation: 'Filters to show only Favorited Pokémon.',
    limitationKeys: ['search_intent_lim_favorites_no_transfer'], limitations: ['Favorites cannot be transferred.'],
  },
  {
    keywords: ['lucky', 'şanslı', 'sansli', 'gluecklich', 'chanceux', 'fortunato'],
    tokens: ['lucky'], explanationKey: 'search_intent_expl_lucky', explanation: 'Filters to show only Lucky Pokémon.',
    limitationKeys: ['search_intent_lim_lucky_discount'], limitations: ['Lucky Pokémon cost 50% less stardust to power up.'],
  },
  {
    keywords: ['cleanup', 'clean up', 'transfer', 'trash', 'junk', 'clear space', 'box cleanup', 'temizlik', 'temizle', 'silme', 'sil'],
    tokens: ['0*', '1*'], exclusions: ['shiny', 'legendary', 'mythical', 'ultrabeast', 'costume', 'background', 'locationbackground', 'specialbackground', 'shadow', 'purified', 'favorite', 'lucky', '#', 'traded', '4*'],
    explanationKey: 'search_intent_expl_cleanup', explanation: 'Builds a safe transfer candidate search (0* & 1* IV bands). Excludes all protected categories.',
    limitationKeys: ['search_intent_lim_cleanup_bands', 'search_intent_lim_cleanup_spot_check'], limitations: ['0* and 1* are IV bands (0-65%), not exact appraisals.', 'Always spot-check results before mass-transferring.'],
  },
  {
    keywords: ['0*', '0 star', 'zero star', '0 yıldız', '0 yildiz'], tokens: ['0*'],
    explanationKey: 'search_intent_expl_0star', explanation: 'Finds Pokémon in the 0-star appraisal band (0-49% total IVs).',
    limitationKeys: ['search_intent_lim_0star_nundo'], limitations: ['0* includes 0/0/0 (nundo) — lock/tag rare 0% Pokémon before transferring.'],
  },
  {
    keywords: ['1*', '1 star', 'one star', '1 yıldız', '1 yildiz'], tokens: ['1*'],
    explanationKey: 'search_intent_expl_1star', explanation: 'Finds Pokémon in the 1-star appraisal band (50-64% total IVs).',
    limitationKeys: ['search_intent_lim_1star_band', 'search_intent_lim_1star_exclusions'], limitations: ['1* is an IV band (0-50%), not exact 1-star. Always review before transferring.', 'Exclude shiny, legendary, mythical, costume, shadow, lucky, and trade-relevant Pokémon.'],
  },
  {
    keywords: ['candy', 'candy prep', 'extra candy', 'transfer candy', 'şeker', 'seker', 'şeker için', 'seker icin'], tokens: ['count2-'],
    explanationKey: 'search_intent_expl_candy', explanation: 'Finds duplicate Pokémon (count >= 2) for candy generation via transfer.',
    limitationKeys: ['search_intent_lim_candy_exclusions', 'search_intent_lim_candy_count_meaning'], limitations: ['Mandatory exclusions: shiny, legendary, mythical, shadow, purified, and 4*.', 'Count refers to species count, not candy. High count = many transfers needed.'],
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
    keywords: ['untagged', 'no tag', 'not tagged', 'tagged', 'tag', 'etiketsiz', 'etiketlenmemiş', 'etiketlenmemis', 'etiketlenmeyen', 'etiket yok', 'etiket'], tokens: [], exclusions: ['#'],
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

export function parseSearchIntent(text: string): ParsedIntent {
  const pipeForbidden = text.includes('|')
  const cleaned = pipeForbidden ? text.replace(/\|/g, ' ') : text
  const normalized = normalize(cleaned)
  if (!normalized) return emptyIntent('search_intent_empty')

  const matched = patterns.filter(pattern => pattern.keywords.some(keyword => normalized.includes(keyword)))
  if (matched.length === 0) return emptyIntent('search_intent_could_not_understand', text)

  const allTokens = new Set<string>()
  const allExclusions = new Set<string>()
  const explanationKeys: string[] = []
  const allExplanationText: string[] = []
  const limitKeys: string[] = []
  const allLimitations: string[] = []

  for (const pattern of matched) {
    const matchedKeyword = pattern.keywords.find(k => normalized.includes(k)) ?? ''
    const negated = isPatternNegated(normalized, matchedKeyword)
    if (negated) {
      pattern.tokens.forEach(t => allExclusions.add(t))
      pattern.exclusions?.forEach(e => allExclusions.add(e))
    } else {
      pattern.tokens.forEach(t => allTokens.add(t))
      pattern.exclusions?.forEach(e => allExclusions.add(e))
    }
    explanationKeys.push(pattern.explanationKey)
    allExplanationText.push(pattern.explanation)
    limitKeys.push(...pattern.limitationKeys)
    allLimitations.push(...pattern.limitations)
  }

  const noteKeys: string[] = []
  if (pipeForbidden) noteKeys.push('search_intent_pipe_forbidden')
  if (allExclusions.has('traded')) noteKeys.push('search_intent_traded_kept')

  if (allTokens.size === 0 && allExclusions.size === 0) {
    return {
      tokens: [], exclusions: [], rawQuery: '', explanationKey: explanationKeys[0] ?? 'search_intent_empty',
      explanation: allExplanationText.filter((v, i, a) => a.indexOf(v) === i).join(' '), limitationKeys: limitKeys,
      limitations: allLimitations.filter((v, i, a) => a.indexOf(v) === i), canBuild: false,
      hasAutoAdded: false, pipeForbidden, noteKeys,
    }
  }

  const distinctTokens = Array.from(allTokens)
  const distinctExclusions = Array.from(allExclusions)
  const rawQuery = [...distinctTokens, ...distinctExclusions.map(e => `!${e}`)].join('&')

  return {
    tokens: distinctTokens, exclusions: distinctExclusions, rawQuery,
    explanationKey: explanationKeys[0] ?? 'search_intent_empty',
    explanation: allExplanationText.filter((v, i, a) => a.indexOf(v) === i).join(' '),
    limitationKeys: limitKeys, limitations: allLimitations.filter((v, i, a) => a.indexOf(v) === i),
    canBuild: distinctTokens.length > 0 || distinctExclusions.length > 0,
    hasAutoAdded: false, pipeForbidden, noteKeys,
  }
}
