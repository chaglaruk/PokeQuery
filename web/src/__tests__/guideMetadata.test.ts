import { describe, expect, it } from 'vitest'
import appHtml from '../../index.html?raw'
const pages = Object.entries(import.meta.glob<string>('../../public/guides/*.html', { query: '?raw', import: 'default', eager: true }))

describe('Static guide discoverability', () => {
  it('keeps the four guides and their index distinct and crawlable', () => {
    expect(pages).toHaveLength(5)
    const titles = new Set<string>()
    const descriptions = new Set<string>()
    for (const [path, content] of pages) {
      const name = path.split('/').pop()!
      const doc = new DOMParser().parseFromString(content, 'text/html')
      titles.add(doc.title)
      const description = doc.querySelector('meta[name="description"]')?.getAttribute('content')
      expect(description).toBeTruthy()
      descriptions.add(description!)
      const url = `https://chaglaruk.github.io/PokeQuery/guides/${name === 'index.html' ? '' : name}`
      expect(doc.querySelector('link[rel="canonical"]')?.getAttribute('href')).toBe(url)
      expect(doc.querySelectorAll('main h1')).toHaveLength(1)
      expect(doc.querySelector('meta[property="og:title"]')?.getAttribute('content')).toBe(doc.title)
      expect(doc.querySelector('meta[property="og:description"]')?.getAttribute('content')).toBe(description)
      expect(doc.querySelector('meta[property="og:url"]')?.getAttribute('content')).toBe(url)
      expect(doc.querySelector('meta[property="og:image"]')?.getAttribute('content')).toBe('https://chaglaruk.github.io/PokeQuery/pwa-512x512.png')
      expect(doc.querySelector('meta[name="twitter:card"]')?.getAttribute('content')).toBe('summary')
      expect(doc.querySelector('meta[name="twitter:title"]')?.getAttribute('content')).toBe(doc.title)
      expect(doc.querySelector('meta[name="twitter:description"]')?.getAttribute('content')).toBe(description)
      expect(doc.querySelector('meta[name="twitter:image"]')?.getAttribute('content')).toBe(doc.querySelector('meta[property="og:image"]')?.getAttribute('content'))
      expect(doc.querySelector('meta[name="twitter:image:alt"]')?.getAttribute('content')).toBe('PokeQuery app icon')
      expect(doc.querySelectorAll('script')).toHaveLength(0)
      for (const code of doc.querySelectorAll('code')) expect(code.textContent).not.toContain('|')
    }
    expect(titles.size).toBe(5)
    expect(descriptions.size).toBe(5)
  })
  it('uses the existing same-origin icon for the app social preview', () => {
    const doc = new DOMParser().parseFromString(appHtml, 'text/html')
    const image = 'https://chaglaruk.github.io/PokeQuery/pwa-512x512.png'
    expect(doc.querySelector('meta[property="og:image"]')?.getAttribute('content')).toBe(image)
    expect(doc.querySelector('meta[name="twitter:image"]')?.getAttribute('content')).toBe(image)
    expect(doc.querySelector('meta[property="og:image:alt"]')?.getAttribute('content')).toBe('PokeQuery app icon')
  })
})
