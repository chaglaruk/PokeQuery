import type { LocaleCode } from '../types'

type GrowthStrings = {
  shareSearch: string
  shareFallbackCopied: string
  shareBuiltWith: string
  riskTitle: string
  riskBody: string
  continueCopy: string
  continueShare: string
  cancel: string
}

const strings: Record<LocaleCode, GrowthStrings> = {
  en: {
    shareSearch: 'Share search',
    shareFallbackCopied: 'Share text copied',
    shareBuiltWith: 'Built with PokeQuery',
    riskTitle: 'Check this search first',
    riskBody: 'Review the matches in Pokémon GO before you use or share this search. It can surface Pokémon you may not want to act on.',
    continueCopy: 'Review & copy',
    continueShare: 'Review & share',
    cancel: 'Back',
  },
  tr: {
    shareSearch: 'Aramayı paylaş',
    shareFallbackCopied: 'Paylaşım metni kopyalandı',
    shareBuiltWith: 'PokeQuery ile oluşturuldu',
    riskTitle: 'Önce bu aramayı kontrol et',
    riskBody: 'Bu aramayı kullanmadan veya paylaşmadan önce Pokémon GO’daki sonuçlara bak. İşlem yapmak istemeyeceğin Pokémonlar da görünebilir.',
    continueCopy: 'Kontrol et ve kopyala',
    continueShare: 'Kontrol et ve paylaş',
    cancel: 'Geri dön',
  },
  de: {
    shareSearch: 'Suche teilen',
    shareFallbackCopied: 'Text zum Teilen kopiert',
    shareBuiltWith: 'Mit PokeQuery erstellt',
    riskTitle: 'Diese Suche zuerst prüfen',
    riskBody: 'Prüfe die Treffer in Pokémon GO, bevor du diese Suche verwendest oder teilst. Es können Pokémon erscheinen, die du nicht bearbeiten möchtest.',
    continueCopy: 'Prüfen & kopieren',
    continueShare: 'Prüfen & teilen',
    cancel: 'Zurück',
  },
  es: {
    shareSearch: 'Compartir búsqueda',
    shareFallbackCopied: 'Texto para compartir copiado',
    shareBuiltWith: 'Creado con PokeQuery',
    riskTitle: 'Revisa esta búsqueda primero',
    riskBody: 'Revisa los resultados en Pokémon GO antes de usar o compartir esta búsqueda. Puede mostrar Pokémon sobre los que no quieras actuar.',
    continueCopy: 'Revisar y copiar',
    continueShare: 'Revisar y compartir',
    cancel: 'Volver',
  },
  fr: {
    shareSearch: 'Partager la recherche',
    shareFallbackCopied: 'Texte de partage copié',
    shareBuiltWith: 'Créé avec PokeQuery',
    riskTitle: 'Vérifiez d’abord cette recherche',
    riskBody: 'Vérifiez les résultats dans Pokémon GO avant d’utiliser ou de partager cette recherche. Elle peut afficher des Pokémon sur lesquels vous ne souhaitez pas agir.',
    continueCopy: 'Vérifier et copier',
    continueShare: 'Vérifier et partager',
    cancel: 'Retour',
  },
  it: {
    shareSearch: 'Condividi ricerca',
    shareFallbackCopied: 'Testo da condividere copiato',
    shareBuiltWith: 'Creato con PokeQuery',
    riskTitle: 'Controlla prima questa ricerca',
    riskBody: 'Controlla i risultati in Pokémon GO prima di usare o condividere questa ricerca. Potrebbero comparire Pokémon su cui non vuoi intervenire.',
    continueCopy: 'Controlla e copia',
    continueShare: 'Controlla e condividi',
    cancel: 'Indietro',
  },
}

export function growthStrings(locale: LocaleCode): GrowthStrings {
  return strings[locale] ?? strings.en
}
