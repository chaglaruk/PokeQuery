// i18n context for the PWA — app language switch (UI) + search string language (output)

import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { en } from './locales/en'
import { tr } from './locales/tr'
import { de } from './locales/de'
import { es } from './locales/es'
import { fr } from './locales/fr'
import { it } from './locales/it'
import type { LocaleCode, AppLanguage, SearchStringLanguage } from '../types'

const localeMaps: Record<LocaleCode, Record<string, string>> = { en, tr, de, es, fr, it }

const appLanguageToLocale: Record<AppLanguage, LocaleCode> = {
  'System Default': 'en',
  English: 'en',
  Deutsch: 'de',
  Español: 'es',
  Français: 'fr',
  Italiano: 'it',
  Türkçe: 'tr',
}

const searchLanguageToEngineName: Record<SearchStringLanguage, string> = {
  Auto: 'Auto',
  'Match App Language': 'Match App Language',
  English: 'English',
  German: 'German',
  Spanish: 'Spanish',
  French: 'French',
  Italian: 'Italian',
  Turkish: 'Turkish',
}

function detectSystemLocale(): LocaleCode {
  if (typeof navigator !== 'undefined') {
    const lang = navigator.language?.toLowerCase() ?? 'en'
    if (lang.startsWith('tr')) return 'tr'
    if (lang.startsWith('de')) return 'de'
    if (lang.startsWith('es')) return 'es'
    if (lang.startsWith('fr')) return 'fr'
    if (lang.startsWith('it')) return 'it'
  }
  return 'en'
}

function resolveSearchLanguage(ssl: SearchStringLanguage, al: AppLanguage): string {
  if (ssl === 'Auto') return 'English'
  if (ssl === 'Match App Language') {
    const locale = appLanguageToLocale[al]
    const map: Record<LocaleCode, string> = { en: 'English', tr: 'Turkish', de: 'German', es: 'Spanish', fr: 'French', it: 'Italian' }
    return map[locale]
  }
  return searchLanguageToEngineName[ssl]
}

interface I18nContextValue {
  t: (key: string, ...args: (string | number)[]) => string
  appLanguage: AppLanguage
  setAppLanguage: (lang: AppLanguage) => void
  searchLanguage: SearchStringLanguage
  setSearchLanguage: (lang: SearchStringLanguage) => void
  resolvedSearchLanguage: string
  locale: LocaleCode
}

const I18nContext = createContext<I18nContextValue | null>(null)

export function I18nProvider({ children }: { children: ReactNode }) {
  const [appLanguage, setAppLanguageState] = useState<AppLanguage>(() => {
    const saved = localStorage.getItem('pq_app_language') as AppLanguage | null
    return saved ?? 'System Default'
  })
  const [searchLanguage, setSearchLanguageState] = useState<SearchStringLanguage>(() => {
    const saved = localStorage.getItem('pq_search_language') as SearchStringLanguage | null
    return saved ?? 'Auto'
  })

  const locale: LocaleCode = appLanguage === 'System Default'
    ? detectSystemLocale()
    : appLanguageToLocale[appLanguage]

  const stringMap = localeMaps[locale] ?? en

  const t = useCallback((key: string, ...args: (string | number)[]) => {
    let value = stringMap[key] ?? en[key] ?? key
    for (let i = 0; i < args.length; i++) {
      value = value.replace(new RegExp(`\\{${i}\\}`, 'g'), String(args[i]))
    }
    return value
  }, [stringMap])

  const setAppLanguage = useCallback((lang: AppLanguage) => {
    setAppLanguageState(lang)
    localStorage.setItem('pq_app_language', lang)
  }, [])

  const setSearchLanguage = useCallback((lang: SearchStringLanguage) => {
    setSearchLanguageState(lang)
    localStorage.setItem('pq_search_language', lang)
  }, [])

  const resolvedSearchLanguage = resolveSearchLanguage(searchLanguage, appLanguage)

  return (
    <I18nContext.Provider value={{ t, appLanguage, setAppLanguage, searchLanguage, setSearchLanguage, resolvedSearchLanguage, locale }}>
      {children}
    </I18nContext.Provider>
  )
}

export function useI18n(): I18nContextValue {
  const ctx = useContext(I18nContext)
  if (!ctx) throw new Error('useI18n must be used within I18nProvider')
  return ctx
}
