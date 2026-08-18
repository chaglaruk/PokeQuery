// Port of com.caglar.pokequery.domain.engine.SearchTermMapper
// Source: Niantic Help Center FAQ 1486, locale paths en/de/es/fr/it/tr.
// Conservative rule: map terms explicitly documented by official Help Center pages.
// Multi-word terms are allowed when the official page itself documents the phrase (e.g. Turkish "takas edilen", Spanish "con suerte").
// count and specialbackground stay English fallback.

const turkishMap: Record<string, string> = {
  cp: 'dg',
  hp: 'sp',
  distance: 'mesafe',
  attack: 'saldırı',
  defense: 'savunma',
  age: 'yaş',
  year: 'yıl',
  shiny: 'parlak',
  legendary: 'efsanevi',
  mythical: 'mitolojik',
  shadow: 'gölge',
  purified: 'arınmış',
  favorite: 'favori',
  lucky: 'şanslı',
  costume: 'kostüm',
  traded: 'takas edilen',
  defender: 'savunucu',
  background: 'arkaplan',
  locationbackground: 'konumarkaplanı',
  ultrabeast: 'ultracanavar',
  dynamax: 'dinamaks',
  gigantamax: 'gigantamaks',
  fusion: 'füzyon',
  mega: 'mega',
  megaevolve: 'megaevrim',
  buddy: 'dost',
  evolve: 'evrim',
  hypertraining: 'hipereğitim',
  item: 'eşya',
  evolvenew: 'yenievrim',
  evolvequest: 'evrimhedef',
  tradeevolve: 'takasevrim',
  '@special': '@özel',
  '@weather': '@havadurumu',
  eggsonly: 'sadeceyumurta',
  hatched: 'yumurtadançıkmış',
}

const germanMap: Record<string, string> = {
  cp: 'wp', hp: 'kp', distance: 'entfernung', attack: 'angriff', defense: 'verteidigung', age: 'alter', year: 'jahr',
  shiny: 'schillernd', legendary: 'legendär', mythical: 'mysteriös', shadow: 'crypto', purified: 'erlöst', favorite: 'favorit',
  lucky: 'glücks', costume: 'kostümiert', traded: 'getauscht', defender: 'verteidiger', background: 'hintergrund',
  locationbackground: 'ortshintergrund', ultrabeast: 'ultrabestie', dynamax: 'dynamax', gigantamax: 'gigadynamax', fusion: 'fusion',
  mega: 'mega', megaevolve: 'megaentwicklung', buddy: 'kumpel', evolve: 'entwickeln', hypertraining: 'superspezialtraining', item: 'item',
  evolvenew: 'neueentwicklung', evolvequest: 'entwicklungsaufgabe', tradeevolve: 'tauschentwicklung', '@special': '@spezial', '@weather': '@wetter',
  eggsonly: 'nurausEiern', hatched: 'ausgebrütet',
}

const spanishMap: Record<string, string> = {
  cp: 'pc', hp: 'ps', distance: 'distancia', attack: 'ataque', defense: 'defensa', age: 'edad', year: 'año',
  shiny: 'variocolor', legendary: 'legendario', mythical: 'singular', shadow: 'oscuro', purified: 'purificado', favorite: 'favorito',
  lucky: 'con suerte', costume: 'disfraz', traded: 'intercambiados', defender: 'defensor', background: 'fondo', locationbackground: 'fondolugar',
  ultrabeast: 'ultraentes', dynamax: 'dinamax', gigantamax: 'gigamax', fusion: 'fusión', mega: 'mega', megaevolve: 'megaevolucionar',
  buddy: 'compañero', evolve: 'evolucionar', hypertraining: 'entrenamiento extremo', item: 'objeto', evolvenew: 'nuevaevolución',
  evolvequest: 'misión evolución', tradeevolve: 'evoluciónintercambio', '@special': '@especial', '@weather': '@tiempo atmosférico',
  eggsonly: 'huevosolo', hatched: 'eclosionado',
}

const frenchMap: Record<string, string> = {
  cp: 'pc', hp: 'pv', distance: 'distance', attack: 'attaque', defense: 'défense', age: 'âge', year: 'année',
  shiny: 'chromatique', legendary: 'légendaire', mythical: 'fabuleux', shadow: 'obscur', purified: 'purifié', favorite: 'favoris',
  lucky: 'chanceux', costume: 'costume', traded: 'échangé', defender: 'défenseur', background: 'fond', locationbackground: 'fondlieu',
  ultrabeast: 'ultra-chimère', dynamax: 'dynamax', gigantamax: 'gigamax', fusion: 'fusion', mega: 'méga', megaevolve: 'mégaévolue',
  buddy: 'copain', evolve: 'évoluer', hypertraining: 'entraînementultime', item: 'objet', evolvenew: 'nouvelleévolution',
  evolvequest: 'évolutionparquête', tradeevolve: 'évolutionparéchange', '@special': '@spécial', '@weather': '@météo',
  eggsonly: 'oeufseulement', hatched: 'éclos',
}

const italianMap: Record<string, string> = {
  cp: 'pl', hp: 'ps', distance: 'distanza', attack: 'attacco', defense: 'difesa', age: 'età', year: 'anno',
  shiny: 'cromatico', legendary: 'leggendario', mythical: 'misterioso', shadow: 'ombra', purified: 'purificato', favorite: 'preferiti',
  lucky: 'fortunato', costume: 'costume', traded: 'scambiato', defender: 'difensore', background: 'sfondo', locationbackground: 'sfondodiposizione',
  ultrabeast: 'ultracreatura', dynamax: 'dynamax', gigantamax: 'gigamax', fusion: 'fusione', mega: 'mega', megaevolve: 'megaevoluto',
  buddy: 'compagno', evolve: 'fai evolvere', hypertraining: 'allenamentopro', item: 'strumento', evolvenew: 'nuovaevoluzione',
  evolvequest: 'evoluzionetramitericerca', tradeevolve: 'evoluzionetramitescambio', '@special': '@speciale', '@weather': '@meteo',
  eggsonly: 'solouovo', hatched: 'dauovo',
}

const knownTokenKeys = new Set([
  'cp', 'hp', 'attack', 'defense', 'age', 'distance', 'year',
  'shiny', 'legendary', 'mythical', 'ultrabeast', 'shadow', 'purified',
  'favorite', 'lucky', 'traded', 'defender', 'costume',
  'background', 'locationbackground', 'specialbackground',
  'mega', 'megaevolve', 'buddy', 'evolve', 'hypertraining', 'item',
  'evolvenew', 'evolvequest', 'tradeevolve', '@special', '@weather',
  'eggsonly', 'hatched', 'dynamax', 'gigantamax', 'fusion', 'count',
])

function escapeRegExp(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

const turkishChars = ['ı', 'ş', 'ğ', 'İ', 'Ş', 'Ğ']

const turkishDistinctTokens = Object.values(turkishMap)
  .filter(v => v && v !== 'mega')
  .sort((a, b) => b.length - a.length)

const turkishTokenRegexes = turkishDistinctTokens.map(token =>
  new RegExp(`(^|[&|!,;:\\s])${escapeRegExp(token)}([0-9\\-&|,;:\\s]|$)`, 'i')
)

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
  if (!language || !language.trim() || language.trim().toLowerCase() === 'auto') return 'English'
  return language
}

/** Token-boundary-aware heuristic: does this generated search string look like Turkish output? */
export function looksTurkish(rawSyntax: string): boolean {
  if (!rawSyntax || !rawSyntax.trim()) return false
  if ([...rawSyntax].some(c => turkishChars.includes(c))) return true
  return turkishTokenRegexes.some(rx => rx.test(rawSyntax))
}

export function findUnverifiedTokens(query: string, language: string): string[] {
  const resolved = resolveLanguage(language)
  if (resolved === 'English' || !query || !query.trim()) return []
  const map = getMapFor(resolved)

  const segments = query.split(/[&!,;:|\s]+/)
    .map(t => t.replace(/^[!]+/, '').replace(/[0-9\-*]+$/, '').trim())
    .filter(t => t.length > 0)

  return [...new Set(segments.filter(t => knownTokenKeys.has(t) && !(t in map)))]
}

export function translateSyntax(rawSyntax: string, language: string): string {
  const resolved = resolveLanguage(language)
  const map = getMapFor(resolved)
  if (Object.keys(map).length === 0 || !rawSyntax || !rawSyntax.trim()) return rawSyntax

  let translated = rawSyntax
  const keys = Object.keys(map).sort((a, b) => b.length - a.length)

  for (const key of keys) {
    const localized = map[key]
    // Segment boundary matching includes both officially documented multi-criteria separators
    // '&' and '|', plus the documented multi-search separators ',', ';', ':'.
    const escaped = escapeRegExp(key)
    const regex = new RegExp(`(^|[&|!,;:])(${escaped})(?=[0-9\\-&|,;:]|$)`, 'g')
    translated = translated.replace(regex, `$1${localized}`)
  }

  return translated
}
