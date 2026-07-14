import { useEffect, useState } from 'react'
import type { RiskLevel } from '@/types'

export type SavedSearchKind = 'favorites' | 'history'

export interface SavedSearch {
  id: string
  name: string
  rawSyntax: string
  goalId: string
  riskLevel: RiskLevel
  createdAt: number
}

const keys: Record<SavedSearchKind, string> = {
  favorites: 'pq_favorites',
  history: 'pq_history',
}
const changedEvent = 'pq-saved-searches-changed'

export function readSavedSearches(kind: SavedSearchKind): SavedSearch[] {
  if (typeof localStorage === 'undefined') return []
  try {
    const value: unknown = JSON.parse(localStorage.getItem(keys[kind]) ?? '[]')
    return Array.isArray(value) ? value.filter(isSavedSearch).sort((a, b) => b.createdAt - a.createdAt) : []
  } catch {
    return []
  }
}

function isSavedSearch(value: unknown): value is SavedSearch {
  if (!value || typeof value !== 'object') return false
  const item = value as Partial<SavedSearch>
  return typeof item.id === 'string' && typeof item.name === 'string' &&
    typeof item.rawSyntax === 'string' && typeof item.goalId === 'string' &&
    typeof item.riskLevel === 'string' && typeof item.createdAt === 'number'
}

function write(kind: SavedSearchKind, searches: SavedSearch[]) {
  localStorage.setItem(keys[kind], JSON.stringify(searches))
  window.dispatchEvent(new CustomEvent(changedEvent, { detail: kind }))
}

function create(search: Omit<SavedSearch, 'id' | 'createdAt'>): SavedSearch {
  const createdAt = Date.now()
  return { ...search, id: `${createdAt}-${Math.random().toString(36).slice(2, 8)}`, createdAt }
}

export function addFavorite(search: Omit<SavedSearch, 'id' | 'createdAt'>): SavedSearch {
  const favorite = create(search)
  write('favorites', [favorite, ...readSavedSearches('favorites').filter(item => item.rawSyntax !== search.rawSyntax)])
  return favorite
}

export function removeFavorite(id: string) {
  write('favorites', readSavedSearches('favorites').filter(item => item.id !== id))
}

export function findFavorite(rawSyntax: string): SavedSearch | undefined {
  return readSavedSearches('favorites').find(item => item.rawSyntax === rawSyntax)
}

export function addHistory(search: Omit<SavedSearch, 'id' | 'createdAt'>) {
  const historyItem = create(search)
  const history = [historyItem, ...readSavedSearches('history').filter(item => item.rawSyntax !== search.rawSyntax)].slice(0, 25)
  write('history', history)
}

export function useSavedSearches(kind: SavedSearchKind): SavedSearch[] {
  const [searches, setSearches] = useState(() => readSavedSearches(kind))

  useEffect(() => {
    const refresh = () => setSearches(readSavedSearches(kind))
    refresh()
    const onChanged = (event: Event) => {
      if ((event as CustomEvent<SavedSearchKind>).detail === kind) refresh()
    }
    window.addEventListener(changedEvent, onChanged)
    window.addEventListener('storage', refresh)
    return () => {
      window.removeEventListener(changedEvent, onChanged)
      window.removeEventListener('storage', refresh)
    }
  }, [kind])

  return searches
}
