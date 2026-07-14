import { describe, expect, it } from 'vitest'
import { en } from '../i18n/locales/en'
import { de } from '../i18n/locales/de'
import { es } from '../i18n/locales/es'
import { fr } from '../i18n/locales/fr'
import { it as italian } from '../i18n/locales/it'
import { tr } from '../i18n/locales/tr'

const locales = { en, de, es, fr, it: italian, tr }
const entity = /&(amp|quot|apos|lt|gt|#\d+|#x[\da-f]+);/i
const tsxSources = import.meta.glob('../**/*.tsx', { query: '?raw', import: 'default', eager: true }) as Record<string, string>

describe('visual localization hygiene', () => {
  it('contains no escaped HTML or XML entities', () => {
    for (const [locale, values] of Object.entries(locales)) {
      expect(Object.entries(values).filter(([, value]) => entity.test(value)), locale).toEqual([])
    }
  })

  it('defines every established Home, Settings, Search Assistant and onboarding key', () => {
    const screenNames = [
      'HomeScreen.tsx', 'SettingsScreen.tsx', 'SearchAssistantScreen.tsx', 'OnboardingScreen.tsx',
      'GoalDetailScreen.tsx', 'PresetsScreen.tsx', 'EventsScreen.tsx', 'ExplainScreen.tsx',
      'SavedSearchesScreen.tsx', 'KnowledgeScreen.tsx',
    ]
    const screenText = screenNames.map(name => tsxSources[`../ui/screens/${name}`]).join('\n')
    const keys = [...new Set([...screenText.matchAll(/['"]([a-z][a-z0-9_]+)['"]/g)].map(match => match[1]).filter(key => key in en))]

    for (const [locale, values] of Object.entries(locales)) {
      expect(keys.filter(key => !(key in values)), locale).toEqual([])
    }
  })

  it('uses the approved Turkish Home copy', () => {
    expect(tr.trust_home_no_access).toBe('Hesap erişimi yok')
    expect(tr.trust_home_review_first).toBe('Transferden önce incele')
    expect(tr.goal_pvp_candidates_desc).toBe('Büyük ve Ultra Lig')
    expect(tr.goal_lucky_trade).toBe('Şanslı Takas Hazırlığı')
  })

  it('contains no platform-dependent emoji or icon glyphs in production TSX or locales', () => {
    const emoji = /\p{Emoji_Presentation}|[✨⚙⚒♟☁ℹ⚠]|\\u(?:2728|2699|2692|265f|2601|2139|26a0)/iu
    const sources = Object.entries(tsxSources).filter(([path]) => !path.includes('/__tests__/'))
    const localeValues = Object.entries(locales).flatMap(([locale, values]) => Object.values(values).map(value => [`locale:${locale}`, value] as const))
    expect([...sources, ...localeValues].filter(([, source]) => emoji.test(source)).map(([path]) => path)).toEqual([])
  })
})
