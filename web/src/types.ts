// Shared types — mirrors com.caglar.pokequery.data.model (GeneratedString, RiskLevel, etc.)

export type RiskLevel = 'Info' | 'Low' | 'Medium' | 'High'

export interface GeneratedString {
  rawSyntax: string
  plainLanguageExplanation: string
  protectedCategories: string[]
  includedHighRiskCategories: string[]
  riskLevel: RiskLevel
  warnings: string[]
  goalId: string
  title: string
  scopeBreadth: string
}

export type SearchStringLanguage =
  | 'Auto'
  | 'Match App Language'
  | 'English'
  | 'German'
  | 'Spanish'
  | 'French'
  | 'Italian'
  | 'Turkish'

export type AppLanguage =
  | 'System Default'
  | 'English'
  | 'Deutsch'
  | 'Español'
  | 'Français'
  | 'Italiano'
  | 'Türkçe'

export type EventStatus = 'CURRENT' | 'UPCOMING' | 'ENDED'

export type EventCategory =
  | 'MAJOR_GAMEPLAY'
  | 'LIMITED_GAMEPLAY'
  | 'ROUTINE_ROTATION'
  | 'SEASON_GBL'
  | 'RAID_ROTATION'
  | 'NEWS_PROMO'
  | 'REWARD_DROP'
  | 'ANNOUNCEMENT'

export type ImportanceTier = 'MAJOR' | 'STANDARD' | 'ROUTINE' | 'NEWS'

export interface EventPokemonEntry {
  name: string
  nameTr?: string | null
  nameDe?: string | null
  nameEs?: string | null
  nameFr?: string | null
  nameIt?: string | null
  source: string
  sourceTr?: string | null
  sourceDe?: string | null
  sourceEs?: string | null
  sourceFr?: string | null
  sourceIt?: string | null
  shinyAvailable?: boolean | null
  note?: string | null
  noteTr?: string | null
  noteDe?: string | null
  noteEs?: string | null
  noteFr?: string | null
  noteIt?: string | null
  badges?: string[] | null
  badgesTr?: string[] | null
  badgesDe?: string[] | null
  badgesEs?: string[] | null
  badgesFr?: string[] | null
  badgesIt?: string[] | null
  spriteKey?: string | null
}

export interface EventFeedEntry {
  id: string
  title: string
  titleTr?: string | null
  titleDe?: string | null
  titleEs?: string | null
  titleFr?: string | null
  titleIt?: string | null
  kind?: string
  status: EventStatus
  note: string
  noteTr?: string | null
  noteDe?: string | null
  noteEs?: string | null
  noteFr?: string | null
  noteIt?: string | null
  month?: number | null
  year?: number | null
  startDate?: string | null
  endDate?: string | null
  start?: string | null
  end?: string | null
  summary: string
  summaryTr?: string | null
  summaryDe?: string | null
  summaryEs?: string | null
  summaryFr?: string | null
  summaryIt?: string | null
  prep: string
  prepTr?: string | null
  prepDe?: string | null
  prepEs?: string | null
  prepFr?: string | null
  prepIt?: string | null
  suggestedSearch: string
  eventNotes: string
  eventNotesTr?: string | null
  eventNotesDe?: string | null
  eventNotesEs?: string | null
  eventNotesFr?: string | null
  eventNotesIt?: string | null
  themeKey: string
  eventCategory?: EventCategory | null
  importanceTier?: ImportanceTier | null
  sourceNotes?: string | null
  sourceName: string
  sourceUrl: string
  sourceType: 'official' | 'third-party'
  lastUpdated: string
  pokemon?: EventPokemonEntry[] | null
  featuredPokemon?: string | null
  featuredPokemonTr?: string | null
  featuredPokemonDe?: string | null
  featuredPokemonEs?: string | null
  featuredPokemonFr?: string | null
  featuredPokemonIt?: string | null
  boostedPokemon?: string | null
  boostedPokemonTr?: string | null
  boostedPokemonDe?: string | null
  boostedPokemonEs?: string | null
  boostedPokemonFr?: string | null
  boostedPokemonIt?: string | null
  bonuses?: string | null
  bonusesTr?: string | null
  bonusesDe?: string | null
  bonusesEs?: string | null
  bonusesFr?: string | null
  bonusesIt?: string | null
  raids?: string | null
  raidsTr?: string | null
  raidsDe?: string | null
  raidsEs?: string | null
  raidsFr?: string | null
  raidsIt?: string | null
  research?: string | null
  researchTr?: string | null
  researchDe?: string | null
  researchEs?: string | null
  researchFr?: string | null
  researchIt?: string | null
}

export interface EventFeed {
  schemaVersion: number
  lastUpdated: string
  notes?: string
  events: EventFeedEntry[]
}

export type LocaleCode = 'en' | 'tr' | 'de' | 'es' | 'fr' | 'it'
