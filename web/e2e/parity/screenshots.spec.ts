import { expect, test, type Page } from '@playwright/test'
import * as fs from 'fs'
import * as path from 'path'
import { fileURLToPath } from 'url'
import { gotoRoute, skipOnboarding } from '../helpers'

const FEED_URL = 'https://raw.githubusercontent.com/chaglaruk/PokeQuery/master/docs/event-feed/pokequery-events.json'
const OUTPUT_DIR = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../../docs/screenshots/pwa_visual_parity_pass_20260714/iphone-13')

const parityFeed = {
  schemaVersion: 1,
  lastUpdated: '2026-07-14',
  events: [
    {
      id: 'parity-featured', title: 'GO Fest 2026: Global', titleTr: 'GO Fest 2026: Küresel',
      status: 'CURRENT', importanceTier: 'MAJOR', eventCategory: 'MAJOR_GAMEPLAY',
      startDate: '2026-07-11', endDate: '2026-07-22', themeKey: 'candy_bonus',
      note: 'Global GO Fest featuring Mewtwo and Zeraora.', noteTr: 'Mewtwo ve Zeraora odaklı küresel GO Fest etkinliği.',
      summary: 'Global event with featured encounters, raids, research, shiny checks, and storage preparation.',
      summaryTr: 'Öne çıkan karşılaşmalar, baskınlar, araştırma ve hazırlık önerileri içeren küresel etkinlik.',
      prep: 'Tag rare, shiny, costume, and background catches before cleanup.', prepTr: 'Temizlikten önce nadir, parlak, kostümlü ve arka planlı yakalamaları etiketleyin.',
      bonuses: 'Extra Candy, boosted shiny checks, and event backgrounds.', bonusesTr: 'Ekstra Şeker, artırılmış parlak kontrolleri ve etkinlik arka planları.',
      raids: 'Mewtwo and featured legendary raids.', raidsTr: 'Mewtwo ve öne çıkan efsanevi baskınlar.',
      research: 'Special Research leads to Zeraora. Do not mix research reward checks with bulk cleanup.',
      researchTr: 'Özel Araştırma Zeraora ödülüne götürür. Araştırma ödüllerini toplu temizlikle karıştırmayın.',
      suggestedSearch: 'age0-7&!traded', eventNotes: 'Visual parity fixture', eventNotesTr: 'Görsel eşlik test verisi',
      sourceName: 'PokeQuery visual fixture', sourceUrl: 'https://example.com', sourceType: 'official', lastUpdated: '2026-07-14',
      pokemon: [
        { name: 'Mewtwo', source: 'Raids', sourceTr: 'Baskınlar', badges: 'Shiny, Raid, Background', badgesTr: 'Parlak, Baskın, Özel arka plan', spriteKey: 'mewtwo' },
        { name: 'Zeraora', source: 'Research', sourceTr: 'Araştırma', badges: 'Research, Mythical check', badgesTr: 'Araştırma, Efsanevi kontrol', spriteKey: 'zeraora' },
        { name: 'Pikachu', source: 'Wild', sourceTr: 'Vahşi', badges: 'Shiny, Costume', badgesTr: 'Parlak, Kostümlü', spriteKey: 'pikachu' },
        { name: 'Necrozma', source: 'Raids', sourceTr: 'Baskınlar', badges: 'Raid, Trade, Storage', badgesTr: 'Baskın, Takas, Depo', spriteKey: 'necrozma' },
      ],
    },
    {
      id: 'parity-anniversary', title: '10th Anniversary Party', titleTr: '10. Yıl Dönümü Etkinliği',
      status: 'CURRENT', importanceTier: 'STANDARD', eventCategory: 'LIMITED_GAMEPLAY',
      startDate: '2026-07-04', endDate: '2026-07-18', themeKey: 'generic_event',
      note: 'Anniversary collection review.', noteTr: 'Yıl dönümü koleksiyon incelemesi.', summary: 'Anniversary featured catches.', summaryTr: 'Yıl dönümü öne çıkan yakalamaları.',
      prep: 'Review costumes before cleanup.', prepTr: 'Temizlikten önce kostümleri kontrol edin.', suggestedSearch: 'costume&age0-14', eventNotes: 'Visual fixture',
      sourceName: 'PokeQuery visual fixture', sourceUrl: 'https://example.com', sourceType: 'official', lastUpdated: '2026-07-14',
    },
    {
      id: 'parity-upcoming', title: 'Road of Legends', titleTr: 'Efsaneler Yolu',
      status: 'UPCOMING', importanceTier: 'STANDARD', eventCategory: 'MAJOR_GAMEPLAY',
      startDate: '2026-07-25', endDate: '2026-07-27', themeKey: 'raid',
      note: 'Prepare raid storage.', noteTr: 'Baskın deposunu hazırlayın.', summary: 'Upcoming raid event.', summaryTr: 'Yaklaşan baskın etkinliği.',
      prep: 'Make room safely.', prepTr: 'Güvenli biçimde yer açın.', suggestedSearch: 'age365-&!traded', eventNotes: 'Visual fixture',
      sourceName: 'PokeQuery visual fixture', sourceUrl: 'https://example.com', sourceType: 'official', lastUpdated: '2026-07-14',
    },
  ],
}

async function setLanguage(page: Page, language: 'English' | 'Türkçe') {
  await page.evaluate(value => localStorage.setItem('pq_app_language', value), language)
  await page.reload()
}

async function capture(page: Page, name: string) {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true })
  await page.waitForTimeout(600)
  await page.evaluate(() => window.scrollTo(0, 0))
  await page.screenshot({ path: path.join(OUTPUT_DIR, name), fullPage: false })
}

test('generate Android parity acceptance screenshots', async ({ page }) => {
  test.setTimeout(180000)
  await page.setViewportSize({ width: 390, height: 844 })
  await skipOnboarding(page)
  await page.route(FEED_URL, route => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(parityFeed) }))

  await gotoRoute(page, '/')
  await setLanguage(page, 'English')
  await expect(page.getByText('Safe Cleanup').first()).toBeVisible()
  await capture(page, 'home_en.png')
  await setLanguage(page, 'Türkçe')
  await expect(page.getByText('Güvenli Temizlik').first()).toBeVisible()
  await capture(page, 'home_tr.png')

  await gotoRoute(page, '/settings')
  await expect(page.getByRole('heading', { name: 'Ayarlar' })).toBeVisible()
  await capture(page, 'settings_tr.png')

  await gotoRoute(page, '/goal/safe_cleanup')
  await expect(page.locator('.search-string')).toBeVisible()
  await capture(page, 'goal_detail.png')
  await gotoRoute(page, '/goal/pvp_candidates')
  await expect(page.locator('.search-string')).toBeVisible()
  await capture(page, 'pvp_great.png')
  await page.getByRole('button', { name: 'Ultra Lig' }).first().click()
  await capture(page, 'pvp_ultra.png')

  await gotoRoute(page, '/presets')
  await expect(page.getByText('Son Yakalananlar')).toBeVisible()
  await capture(page, 'popular_presets_tr.png')
  await page.getByText('Son Yakalananlar').first().click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await capture(page, 'popular_presets_tr_popup.png')
  await page.keyboard.press('Escape')
  await setLanguage(page, 'English')
  await capture(page, 'popular_presets_en.png')
  await page.getByText('Recent Catches').first().click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await capture(page, 'popular_presets_en_popup.png')
  await page.keyboard.press('Escape')

  await setLanguage(page, 'Türkçe')
  await gotoRoute(page, '/events')
  await expect(page.locator('[data-event-id="parity-featured"]')).toBeVisible({ timeout: 20000 })
  await capture(page, 'event_guide_tr_main.png')
  await page.locator('[data-pokemon-name="Mewtwo"]').first().click()
  await expect(page.getByRole('dialog', { name: 'Mewtwo' })).toBeVisible()
  await capture(page, 'event_guide_tr_popup.png')
  await page.keyboard.press('Escape')
  await setLanguage(page, 'English')
  await capture(page, 'event_guide_en_main.png')
  await page.locator('[data-pokemon-name="Mewtwo"]').first().click()
  await expect(page.getByRole('dialog', { name: 'Mewtwo' })).toBeVisible()
  await capture(page, 'event_guide_en_popup.png')
})
