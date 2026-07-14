// Port of com.caglar.pokequery.domain.engine.SearchTermMapper
// Source: Niantic Help Center FAQ 1486, locale paths en/de/es/fr/it/tr.
// Conservative rule: only map single parser-safe tokens visible in the official pages.
// `count`, `specialbackground`, and multi-word terms such as Turkish/Spanish traded stay English.

const turkishMap: Record<string, string> = {
  shiny: 'parlak',
  legendary: 'efsanevi',
  mythical: 'mitolojik',
  shadow: 'gölge',
  purified: 'arınmış',
  favorite: 'favori',
  lucky: 'şanslı',
  costume: 'kostüm',
  attack: 'saldırı',
  defense: 'savunma',
  hp: 'sp',
  distance: 'mesafe',
  age: 'yaş',
  year: 'yıl',
  evolve: 'evrim',
  dynamax: 'dinamaks',
  gigantamax: 'gigantamaks',
  fusion: 'füzyon',
  cp: 'dg',
  defender: 'savunucu',
  background: 'arkaplan',
  locationbackground: 'konumarkaplanı',
  ultrabeast: 'ultracanavar',
}

const germanMap: Record<string, string> = {
  shiny: 'schillernd',
  legendary: 'legendär',
  mythical: 'mysteriös',
  shadow: 'crypto',
  purified: 'erlöst',
  favorite: 'favorit',
  lucky: 'glücks',
  costume: 'kostümiert',
  attack: 'angriff',
  defense: 'verteidigung',
  hp: 'kp',
  distance: 'entfernung',
  age: 'alter',
  year: 'jahr',
  evolve: 'entwickeln',
  dynamax: 'dynamax',
  gigantamax: 'gigadynamax',
  fusion: 'fusion',
  cp: 'wp',
  defender: 'verteidiger',
  background: 'hintergrund',
  locationbackground: 'ortshintergrund',
  ultrabeast: 'ultrabestie',
}

const spanishMap: Record<string, string> = {
  shiny: 'variocolor',
  legendary: 'legendario',
  mythical: 'singular',
  shadow: 'oscuro',
  purified: 'purificado',
  favorite: 'favorito',
  costume: 'disfraz',
  attack: 'ataque',
  defense: 'defensa',
  hp: 'ps',
  distance: 'distancia',
  age: 'edad',
  year: 'año',
  evolve: 'evolucionar',
  dynamax: 'dinamax',
  gigantamax: 'gigamax',
  fusion: 'fusión',
  cp: 'pc',
  defender: 'defensor',
  background: 'fondo',
  locationbackground: 'fondolugar',
  ultrabeast: 'ultraentes',
}

const frenchMap: Record<string, string> = {
  shiny: 'chromatique',
  legendary: 'légendaire',
  mythical: 'fabuleux',
  shadow: 'obscur',
  purified: 'purifié',
  favorite: 'favoris',
  lucky: 'chanceux',
  costume: 'costume',
  attack: 'attaque',
  defense: 'défense',
  hp: 'pv',
  distance: 'distance',
  age: 'âge',
  year: 'année',
  evolve: 'évoluer',
  dynamax: 'dynamax',
  gigantamax: 'gigamax',
  fusion: 'fusion',
  cp: 'pc',
  defender: 'défenseur',
  background: 'fond',
  locationbackground: 'fondlieu',
  ultrabeast: 'ultra-chimère',
}

const italianMap: Record<string, string> = {
  shiny: 'cromatico',
  legendary: 'leggendario',
  mythical: 'misterioso',
  shadow: 'ombra',
  purified: 'purificato',
  favorite: 'preferiti',
  lucky: 'fortunato',
  costume: 'costume',
  attack: 'attacco',
  defense: 'difesa',
  hp: 'ps',
  distance: 'distanza',
  age: 'età',
  year: 'anno',
  dynamax: 'dynamax',
  gigantamax: 'gigamax',
  fusion: 'fusione',
  cp: 'pl',
  defender: 'difensore',
  background: 'sfondo',
  locationbackground: 'sfondodiposizione',
  ultrabeast: 'ultracreatura',
}

const knownTokenKeys = new Set([
  'cp', 'hp', 'attack', 'defense', 'age', 'distance', 'year',
  'shiny', 'legendary', 'mythical', 'ultrabeast', 'shadow', 'purified',
  'favorite', 'lucky', 'traded', 'defender', 'costume',
  'background', 'locationbackground', 'specialbackground',
  'mega', 'evolve', 'dynamax', 'gigantamax', 'fusion', 'count',
])

export function getMapFor(language: string): Record<string, string> {
  switch (language) {
    case 'Turkish': return turkishMap
    case 'German': return germanMap
    case 'Spanish': return spanishMap
    case 'French': return frenchMap
    case 'Italian': return italianMap
    default: return {}
  }
}

export function resolveLanguage(language: string): string {
  if (!language || language.trim().length === 0 || language.toLowerCase() === 'auto') return 'English'
  return language
}

export function looksTurkish(rawSyntax: string): boolean {
  if (!rawSyntax || rawSyntax.trim().length === 0) return false
  const values = Object.values(turkishMap).filter(v => v && v.length > 0)
  if (values.some(v => rawSyntax.toLowerCase().includes(v.toLowerCase()))) return true
  const turkishChars = ['ı', 'ş', 'ğ', 'İ', 'Ş', 'Ğ']
  return [...rawSyntax].some(c => turkishChars.includes(c.toLowerCase()))
}

export function findUnverifiedTokens(query: string, language: string): string[] {
  const resolvedLanguage = resolveLanguage(language)
  if (resolvedLanguage === 'English' || !query || query.trim().length === 0) return []
  const map = getMapFor(resolvedLanguage)

  const tokens = query
    .split(/[&!,;:|\s]+/)
    .map(t => t.replace(/[0-9\-*]/g, '').trim())
    .filter(t => t.length > 0)

  return [...new Set(tokens.filter(token => knownTokenKeys.has(token) && !(token in map)))]
}

export function translateSyntax(rawSyntax: string, language: string): string {
  const resolvedLanguage = resolveLanguage(language)
  const map = getMapFor(resolvedLanguage)

  if (Object.keys(map).length === 0 || !rawSyntax || rawSyntax.trim().length === 0) return rawSyntax

  let translated = rawSyntax

  const keys = Object.keys(map).sort((a, b) => b.length - a.length)

  for (const key of keys) {
    const tr = map[key]
    const regex = new RegExp(`(^|[&!,])(${escapeRegex(key)})(?=[0-9\\-&,]|$)`, 'g')
    translated = translated.replace(regex, (_m, prefix, _token) => prefix + tr)
  }

  return translated
}

function escapeRegex(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
