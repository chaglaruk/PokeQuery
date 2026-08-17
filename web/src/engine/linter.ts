// Port of com.caglar.pokequery.domain.lint.Linter

import { COUNT_MANDATORY_PROTECTIONS } from '../engine/stringBuilderEngine'

export interface LintWarning {
  message: string
  isError: boolean
}

const reservedTerms = new Set([
  'shiny', 'legendary', 'mythical', 'ultrabeast', 'shadow', 'purified',
  'favorite', 'favourite', 'costume', 'background', 'locationbackground',
  'specialbackground', 'lucky', 'traded', 'defender', 'raid', 'remoteraid',
  'hatched', 'research', 'gbl', 'rocket', 'snapshot', 'evolve', 'evolvenew',
  'megaevolve', 'tradeevolve', 'dynamax', 'gigantamax', 'adventureeffect',
])

const riskyCategories = new Set(['shiny', 'legendary', 'mythical', 'lucky'])

function tokenize(query: string): string[] {
  return query.toLowerCase().split(/[&,;:]/).map(t => t.trim()).filter(Boolean)
}

export function lint(query: string): LintWarning[] {
  const warnings: LintWarning[] = []
  const lower = query.toLowerCase()
  const tokens = tokenize(query)

  if (query.includes('|')) {
    warnings.push({ message: "T3 uncertain operator '|'. Do not use it; use '&' or ',' instead.", isError: true })
  }

  if (tokens.includes('!untraded') || tokens.includes('untraded')) {
    warnings.push({
      message: "Unsupported token 'untraded'. Use 'traded' to include traded Pokémon or '!traded' to exclude them.",
      isError: true,
    })
  }

  const hasCount = tokens.some(t => /^count\d*-?$/.test(t))
  if (hasCount) {
    const missing = COUNT_MANDATORY_PROTECTIONS.filter(p => !tokens.includes(`!${p}`))
    if (missing.length > 0) {
      warnings.push({
        message: `Unsafe count usage; missing mandatory exclusions: ${missing.map(p => `!${p}`).join(', ')}.`,
        isError: true,
      })
    }
    warnings.push({ message: 'Count uses Pokédex number and does not distinguish shiny, form, gender, or costume.', isError: false })
  }

  if (tokens.some(t => t === '0*') && lower !== '0attack&0defense&0hp') {
    warnings.push({ message: '0* is an IV band, not exact 0% IV.', isError: false })
  }

  const isPvP = tokens.some(t => t === '0-1attack' || t === '3-4defense')
  const isTradePrep = tokens.some(t => t === 'age365-' || t === 'distance100-')
  const cleanupOrCount =
    (hasCount || tokens.some(t => t === '0*' || t === '1*' || t === '2*')) &&
    !isPvP && !isTradePrep

  if (cleanupOrCount) {
    for (const cat of riskyCategories) {
      if (tokens.includes(cat)) {
        warnings.push({ message: `Risky inclusion of ${cat} in a cleanup/count search.`, isError: true })
      }
    }
  }

  if (isTradePrep) {
    warnings.push({ message: 'Trade prep search. Review manually. Valuable Pokémon may appear.', isError: false })
  }

  if (!cleanupOrCount && !isPvP && !isTradePrep) {
    for (const cat of riskyCategories) {
      if (tokens.includes(cat)) {
        warnings.push({ message: `Includes ${cat} as a positive filter. Review matches before acting.`, isError: false })
      }
    }
  }

  const tagRegex = /#([a-z0-9]+)/g
  let match: RegExpExecArray | null
  while ((match = tagRegex.exec(lower)) !== null) {
    const tag = match[1]
    if (reservedTerms.has(tag)) {
      warnings.push({ message: `Tag '#${tag}' collides with the reserved search keyword '${tag}'.`, isError: false })
    }
  }

  const shortcutMap: Record<string, string> = {
    mega: 'Mega0-',
    count: 'count2-',
    dynamax: 'dynamax1-',
    gigantamax: 'gigantamax1-',
  }
  for (const [shortcut, expansion] of Object.entries(shortcutMap)) {
    if (tokens.includes(shortcut)) {
      warnings.push({ message: `Shortcut '${shortcut}' expands to '${expansion}'. Use an explicit term.`, isError: false })
    }
  }

  if ([...query].some(c => c.charCodeAt(0) > 127)) {
    warnings.push({
      message: 'Localized search terms detected. Officially documented localized terms may still be beta until independently verified in the live game client.',
      isError: false,
    })
  }

  const seen = new Set<string>()
  return warnings.filter(w => {
    if (seen.has(w.message)) return false
    seen.add(w.message)
    return true
  })
}
