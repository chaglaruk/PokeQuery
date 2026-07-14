import { beforeEach, describe, expect, it } from 'vitest'
import { addFavorite, addHistory, findFavorite, readSavedSearches, removeFavorite } from '@ui/savedSearches'

const base = { name: 'Safe Cleanup', goalId: 'safe_cleanup', riskLevel: 'Medium' as const }

describe('device-local saved searches', () => {
  beforeEach(() => localStorage.clear())

  it('deduplicates favorites by syntax and supports removal', () => {
    addFavorite({ ...base, rawSyntax: '1*&!shiny' })
    const latest = addFavorite({ ...base, name: 'Updated', rawSyntax: '1*&!shiny' })
    expect(readSavedSearches('favorites')).toHaveLength(1)
    expect(findFavorite('1*&!shiny')?.name).toBe('Updated')
    removeFavorite(latest.id)
    expect(readSavedSearches('favorites')).toEqual([])
  })

  it('deduplicates history by syntax and caps it at 25 newest items', () => {
    for (let index = 0; index < 30; index += 1) {
      addHistory({ ...base, rawSyntax: `age${index}` })
    }
    addHistory({ ...base, name: 'Newest duplicate', rawSyntax: 'age10' })
    const history = readSavedSearches('history')
    expect(history).toHaveLength(25)
    expect(history.filter(item => item.rawSyntax === 'age10')).toHaveLength(1)
    expect(history[0]?.name).toBe('Newest duplicate')
  })

  it('fails closed on malformed storage', () => {
    localStorage.setItem('pq_history', '{broken')
    expect(readSavedSearches('history')).toEqual([])
  })
})
