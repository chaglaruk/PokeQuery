// SearchIntentParser — TypeScript port of Android SearchIntentParser.kt
// Parses natural-language search intent into Pokemon GO search strings.
// 1:1 parity with Android patterns, negation detection, multi-pattern combining.

export interface ParsedIntent {
  tokens: string[]
  exclusions: string[]
  rawQuery: string
  explanation: string
  limitations: string[]
  canBuild: boolean
}

interface IntentPattern {
  keywords: string[]
  tokens: string[]
  exclusions?: string[]
  explanation: string
  limitations?: string[]
  canBuild?: boolean // defaults to true
}

function normalize(text: string): string {
  return text.toLowerCase().trim().replace(/\s+/g, ' ')
}

function isPatternNegated(normalized: string, keyword: string): boolean {
  if (!keyword) return false
  const index = normalized.indexOf(keyword)
  if (index === -1) return false
  const prefix = normalized.substring(0, index)
  const suffix = normalized.substring(index + keyword.length)
  if (normalized.includes('hide') || normalized.includes('exclude') || normalized.includes('without') ||
      normalized.includes('gizle') || normalized.includes('hariç') || normalized.includes('haric') ||
      normalized.includes('dışında') || normalized.includes('disinda')) return true

  const prefixNegations = ['not', 'no', '!', 'non']
  const suffixNegations = ['değil', 'degil', 'olmayan', 'yok']

  const prefixMatch = prefixNegations.some(neg =>
    prefix.trim().endsWith(neg) || prefix.includes(`${neg} `)
  )
  const suffixMatch = suffixNegations.some(neg =>
    suffix.trim().startsWith(neg) || suffix.includes(` ${neg}`)
  )
  return prefixMatch || suffixMatch
}

const patterns: IntentPattern[] = [
  {
    keywords: ['hundo', 'perfect', '100%', '15/15/15', '15 15 15', 'max iv', 'all 15', 'yüzde yüz', 'yuzde yuz', '100 iv', 'kusursuz', 'mükemmel', 'mukemmel', 'güçlü', 'guclu'],
    tokens: ['4*'],
    explanation: 'Finds Pokémon with perfect 15/15/15 IVs (exact 100% appraisal using 4*). Inspection only — does not filter or exclude anything.',
    limitations: ['4* also matches purified Pokémon. Check manually if you want non-purified only.', 'IV appraisal is an approximation, not exact stats.'],
  },
  {
    keywords: ['nundo', '0%', '0/0/0', '0 0 0', 'zero iv', 'lowest', 'minimum iv', 'sıfır iv', 'sifir iv', '0 iv', 'en düşük', 'en dusuk'],
    tokens: ['0attack', '0defense', '0hp'],
    explanation: 'Finds Pokémon with 0/0/0 IVs. This is an exact match — only true 0% appraisal shows.',
    limitations: ['IV floor events (trades, weather boost, raids) make 0% IV impossible.'],
  },
  {
    keywords: ['great league pvp', 'great league candidate', 'great league', 'büyük lig', 'buyuk lig'],
    tokens: ['0-1attack', '3-4defense', '3-4hp', 'cp-1500'],
    explanation: 'Finds Great League PvP candidates (CP <= 1500) using CP cap/shortlist logic.',
    limitations: ['CP cap filters by current CP only; exact PvP rank and level are not detectable via search strings.', 'Not all matches are PvP-relevant — species and moveset also matter.'],
  },
  {
    keywords: ['ultra league pvp', 'ultra league candidate', 'ultra league', 'ultra lig'],
    tokens: ['0-1attack', '3-4defense', '3-4hp', 'cp-2500'],
    explanation: 'Finds Ultra League PvP candidates (CP <= 2500) using CP cap/shortlist logic.',
    limitations: ['CP cap filters by current CP only; exact PvP rank and level are not detectable via search strings.', 'Not all matches are PvP-relevant — species and moveset also matter.'],
  },
  {
    keywords: ['pvp', 'pvp iv', 'pvp candidate', 'pvp adayı', 'pvp adayi', 'kapışma', 'kapisma', 'düello', 'duello'],
    tokens: ['0-1attack', '3-4defense', '3-4hp'],
    explanation: 'Finds Pokémon with PvP-friendly IV spreads (low attack, high defense/HP). Suitable for Great League and Ultra League — exact PvP rank is not detectable via search strings; check CP manually in Pokémon GO.',
    limitations: ['Pokémon GO search cannot detect exact PvP rank or level — only IV floor/ceil values.', 'Not all matches are PvP-relevant — species and moveset also matter.', 'Does not apply a league CP cap; use specific league name for cap.'],
  },
  {
    keywords: ['shiny', 'shinies', 'parlak', 'şayni', 'sayni'],
    tokens: ['shiny'],
    explanation: 'Filters to show only Shiny Pokémon.',
    limitations: ['Shiny search does not distinguish costume, event, or regional variants.', 'You can also use !shiny to search for non-Shiny Pokémon.'],
  },
  {
    keywords: ['legendary', 'legendaries', 'legend', 'efsane', 'efsanevi'],
    tokens: ['legendary'],
    explanation: 'Filters to show only Legendary Pokémon.',
    limitations: ['Mythical Pokémon are NOT included in this search.'],
  },
  {
    keywords: ['mythical', 'mythic', 'mitolojik', 'gizemli'],
    tokens: ['mythical'],
    explanation: 'Filters to show only Mythical Pokémon.',
    limitations: ['This is a risky filter — mythical Pokémon are often valuable and cannot be re-obtained easily.'],
  },
  {
    keywords: ['shadow', 'shadows', 'gölge', 'golge', 'karanlık', 'karanlik'],
    tokens: ['shadow'],
    explanation: 'Filters to show only Shadow Pokémon.',
    limitations: ['Shadow Pokémon are expensive to power up and cannot be traded.', 'Purified Pokémon are NOT included.'],
  },
  {
    keywords: ['purified', 'arınmış', 'arinmis', 'temizlenmiş', 'temizlenmis'],
    tokens: ['purified'],
    explanation: 'Filters to show only Purified Pokémon.',
    limitations: ['Purified Pokémon cost 20% less stardust to power up.', 'Purified Pokémon can be traded — they are not blocked from trading.', 'Purified Pokémon cannot be re-shadowed.'],
  },
  {
    keywords: ['lucky', 'şanslı', 'sansli'],
    tokens: ['lucky'],
    explanation: 'Filters to show only Lucky Pokémon (received via trade with guaranteed higher IVs).',
    limitations: ['Lucky Pokémon cost 50% less stardust to power up.', 'Lucky Pokémon cannot be traded again.', 'A Pokémon becoming Lucky is not guaranteed — it depends on trade context, not just age or distance.'],
  },
  {
    keywords: ['costume', 'event', 'hat', 'bow', 'crown', 'kostüm', 'kostum', 'şapka', 'sapka', 'etkinlik'],
    tokens: ['costume'],
    explanation: 'Filters to show only Costume Pokémon.',
    limitations: ['Costume Pokémon cannot evolve (with rare event exceptions).'],
  },
  {
    keywords: ['favorite', 'fav', 'starred', 'favourite', 'favourites', 'favorites', 'favori', 'yıldızlı', 'yildizli'],
    tokens: ['favorite'],
    explanation: 'Filters to show only your Favorite (starred) Pokémon.',
    limitations: ['You can also use !favorite to search for non-favorited Pokémon.'],
  },
  {
    keywords: ['cleanup', 'transfer', 'delete', 'junk', 'trash', 'bulk transfer', 'temizlik', 'çöp', 'cop', 'gönder', 'gonder'],
    tokens: ['1*'],
    explanation: 'Finds low-appraisal Pokémon for cleanup or transfer. Safe Cleanup excludes protected categories by default.',
    limitations: ['1* is an IV band (0-50%), not exact 1-star. Always review before transferring.', 'Exclude shiny, legendary, mythical, costume, shadow, lucky, and trade-relevant Pokémon.'],
  },
  {
    keywords: ['candy', 'candy prep', 'extra candy', 'transfer candy', 'şeker', 'seker', 'şeker için', 'seker icin'],
    tokens: ['count2-'],
    explanation: 'Finds duplicate Pokémon (count >= 2) for candy generation via transfer.',
    limitations: ['Mandatory exclusions: shiny, legendary, mythical, shadow, purified, and 4*.', 'Count refers to species count, not candy. High count = many transfers needed.'],
  },
  {
    keywords: ['trade', 'trading', 'trade fodder', 'duplicate', 'extra', 'spare', 'takas', 'ticaret', 'takaslık', 'takaslik', 'fazla'],
    tokens: ['count2-'],
    exclusions: ['traded'],
    explanation: 'Finds duplicate untraded Pokémon (count >= 2) for trade with friends.',
    limitations: ['Trade eligibility depends on stardust cost (friendship level). High-value Pokémon still cost more.', 'Special trades (legendary, shiny, unregistered) are limited to one per day.'],
  },
  {
    keywords: ['old', 'older', 'age', '2016', '2017', '2018', 'vintage', 'eski', 'yıllık', 'yillik', 'yaşlı', 'yasli'],
    tokens: ['age365-'],
    explanation: 'Finds Pokémon you have caught/obtained at least 365 days ago (1+ year old).',
    limitations: ['Pokémon from 2016-2018 have a higher (but not guaranteed) Lucky Trade chance — a search string cannot prove Lucky eligibility.', 'Age is based on catch date, not hatch date.'],
  },
  {
    keywords: ['distance', 'far', 'far away', 'overseas', 'foreign', 'distant', 'uzak', 'mesafe', 'yurtdışı', 'yurtdisi', 'yurt dışı'],
    tokens: ['distance100-'],
    explanation: 'Finds Pokémon traded from 100+ km away. These qualify for distance-based candy bonus on transfer.',
    limitations: ['Not all distance Pokémon are tradeable again (already traded).', 'Distance resets on each trade — the last trade distance applies.'],
  },
  {
    keywords: ['untagged', 'no tag', 'not tagged', 'tagged', 'tag', 'etiketsiz', 'etiketlenmemiş', 'etiketlenmemis', 'etiketlenmeyen', 'etiket yok', 'etiket'],
    tokens: [],
    exclusions: ['#'],
    explanation: 'Finds untagged Pokémon for tagging and organization. The search uses !# (NOT tag filter).',
    limitations: ['!# shows Pokémon WITHOUT any tags.', 'If you have never tagged, this matches everything.'],
  },
  {
    keywords: ['lucky trade', 'lucky friend', 'guaranteed lucky', 'şanslı takas', 'sansli takas', 'garanti şanslı', 'garanti sansli'],
    tokens: ['age365-'],
    exclusions: ['traded'],
    explanation: 'Finds older untraded Pokémon that may qualify for Lucky Trades (12/12/12+ IV floor).',
    limitations: ['Only Pokémon from 2016-2018 are guaranteed Lucky. Newer ones have a small chance — a search string cannot prove Lucky eligibility.', 'Can only make one Special Trade per day by default.'],
  },
  {
    keywords: ['all', 'everything', 'all pokemon', 'show all', 'hepsi', 'tümü', 'tumu', 'bütün', 'butun'],
    tokens: [],
    explanation: 'Shows all Pokémon. No filter is applied.',
    limitations: ['In a large inventory, "all" may be slow to load. Use filters to narrow down.'],
    canBuild: false,
  },
]

export function parseSearchIntent(text: string): ParsedIntent {
  const normalized = normalize(text)
  if (!normalized) {
    return { tokens: [], exclusions: [], rawQuery: '', explanation: 'Enter a description of what you want to find.', limitations: [], canBuild: false }
  }

  const matched = patterns.filter(pattern =>
    pattern.keywords.some(keyword => normalized.includes(keyword))
  )

  if (matched.length === 0) {
    return {
      tokens: [],
      exclusions: [],
      rawQuery: '',
      explanation: `Could not understand "${text}". Try words like: shiny, hundo, cleanup, trade, pvp, lucky, shadow, old, costume. (Türkçe: parlak, efsanevi, temizlik, takas, gölge, eski...)`,
      limitations: ['PokeQuery understands common search intents. For complex queries, use the Expert Builder.'],
      canBuild: false,
    }
  }

  const allTokens = new Set<string>()
  const allExclusions = new Set<string>()
  const explanations: string[] = []
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
    explanations.push(pattern.explanation)
    pattern.limitations?.forEach(l => allLimitations.push(l))
  }

  // Extra shiny/legendary/mythical auto-add logic
  const hasShiny = normalized.includes('shiny')
  const hasLegendary = normalized.includes('legendary')
  const hasMythical = normalized.includes('mythical')

  const extraTokens: string[] = []
  const tokenList = Array.from(allTokens).map(t => t.toLowerCase())

  if (hasShiny && !tokenList.includes('shiny') && !Array.from(allExclusions).map(e => e.toLowerCase()).includes('shiny')) {
    if (isPatternNegated(normalized, 'shiny')) {
      allExclusions.add('shiny')
    } else {
      extraTokens.push('shiny')
      allLimitations.push('Shiny search added based on your input. Verify before transferring.')
    }
  }
  if (hasLegendary && !tokenList.includes('legendary') && !Array.from(allExclusions).map(e => e.toLowerCase()).includes('legendary')) {
    if (isPatternNegated(normalized, 'legendary')) allExclusions.add('legendary')
    else extraTokens.push('legendary')
  }
  if (hasMythical && !tokenList.includes('mythical') && !Array.from(allExclusions).map(e => e.toLowerCase()).includes('mythical')) {
    if (isPatternNegated(normalized, 'mythical')) allExclusions.add('mythical')
    else extraTokens.push('mythical')
  }
  extraTokens.forEach(t => allTokens.add(t))

  const extraLabel = extraTokens.length > 0 ? ` [Added: ${extraTokens.join(', ')}]` : ''

  if (allTokens.size === 0 && allExclusions.size === 0) {
    return {
      tokens: [],
      exclusions: [],
      rawQuery: '',
      explanation: explanations.filter((v, i, a) => a.indexOf(v) === i).join(' ') + extraLabel,
      limitations: allLimitations.filter((v, i, a) => a.indexOf(v) === i),
      canBuild: false,
    }
  }

  const canBuildResult = allTokens.size > 0 || allExclusions.size > 0
  const distinctTokens = Array.from(allTokens)
  const distinctExclusions = Array.from(allExclusions)
  const parts = [...distinctTokens, ...distinctExclusions.map(e => `!${e}`)]
  const rawQuery = parts.join('&')

  return {
    tokens: distinctTokens,
    exclusions: distinctExclusions,
    rawQuery,
    explanation: explanations.filter((v, i, a) => a.indexOf(v) === i).join(' ') + extraLabel,
    limitations: allLimitations.filter((v, i, a) => a.indexOf(v) === i),
    canBuild: canBuildResult,
  }
}
