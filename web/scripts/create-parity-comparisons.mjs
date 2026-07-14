import fs from 'node:fs/promises'
import path from 'node:path'
import sharp from 'sharp'

const repoRoot = path.resolve(import.meta.dirname, '../..')
const referenceDir = process.env.ANDROID_REFERENCE_DIR || 'C:/Temp/PokeQuery-android-ui-reference'
const outputDir = path.join(repoRoot, 'docs/screenshots/pwa_android_parity_comparison_20260714')
const pwaDir = path.join(outputDir, 'pwa_iphone13')

const pairs = [
  ['Home EN', null, 'home_en.png'],
  ['Home TR', 'home_tr.png', 'home_tr.png'],
  ['Settings TR', 'settings_tr.png', 'settings_tr.png'],
  ['Safe Cleanup TR', 'goal_detail_tr_one_box.png', 'safe_cleanup_tr.png'],
  ['PvP Great TR', 'pvp_candidate_tr_great.png', 'pvp_great.png'],
  ['PvP Ultra TR', 'pvp_candidate_tr_ultra.png', 'pvp_ultra.png'],
  ['Presets EN', 'popular_presets_en.png', 'popular_presets_en.png'],
  ['Presets EN dialog', 'popular_presets_en_popup.png', 'popular_presets_en_popup.png'],
  ['Presets TR', 'popular_presets_tr.png', 'popular_presets_tr.png'],
  ['Presets TR dialog', 'popular_presets_tr_popup.png', 'popular_presets_tr_popup.png'],
  ['Event Guide EN', 'event_guide_en_main.png', 'event_guide_en_main.png'],
  ['Event Guide EN dialog', 'event_guide_en_popup.png', 'event_guide_en_popup.png'],
  ['Event Guide TR', 'event_guide_tr_main.png', 'event_guide_tr_main.png'],
  ['Event Guide TR dialog', 'event_guide_tr_popup.png', 'event_guide_tr_popup.png'],
  ['Five-tab Home TR', 'home_tr.png', 'five_tab_home.png'],
  ['Favorites TR', null, 'favorites_tr.png'],
  ['History TR', null, 'history_tr.png'],
  ['Knowledge TR', null, 'knowledge_tr.png'],
]

const escapeXml = value => value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
const label = (title, subtitle) => Buffer.from(`<svg width="390" height="52"><rect width="390" height="52" fill="#0a0e1a"/><text x="14" y="22" fill="#fff" font-family="Arial" font-size="15" font-weight="700">${escapeXml(title)}</text><text x="14" y="41" fill="#9aa7b6" font-family="Arial" font-size="11">${escapeXml(subtitle)}</text></svg>`)
const placeholder = title => Buffer.from(`<svg width="390" height="844"><rect width="390" height="844" fill="#0a0e1a"/><rect x="24" y="330" width="342" height="150" rx="18" fill="#0f1422" stroke="#1a2238"/><text x="195" y="385" text-anchor="middle" fill="#fff" font-family="Arial" font-size="16" font-weight="700">${escapeXml(title)}</text><text x="195" y="416" text-anchor="middle" fill="#9aa7b6" font-family="Arial" font-size="12">No supplied Android screenshot</text><text x="195" y="438" text-anchor="middle" fill="#9aa7b6" font-family="Arial" font-size="12">Behavior matched from Android source</text></svg>`)

async function screen(file, missingTitle) {
  if (!file) return placeholder(missingTitle)
  return sharp(path.join(referenceDir, file)).resize(390, 844, { fit: 'contain', background: '#050709' }).png().toBuffer()
}

async function createPair([title, androidFile, pwaFile]) {
  const android = await screen(androidFile, title)
  const pwa = await sharp(path.join(pwaDir, pwaFile)).resize(390, 844, { fit: 'contain', background: '#050709' }).png().toBuffer()
  const out = path.join(outputDir, 'pairs', pwaFile)
  await fs.mkdir(path.dirname(out), { recursive: true })
  await sharp({ create: { width: 792, height: 896, channels: 4, background: '#050709' } })
    .composite([
      { input: label(title, androidFile ? 'Android reference' : 'Android reference unavailable'), left: 0, top: 0 },
      { input: label(title, 'PWA · WebKit iPhone 13 · 390×844'), left: 402, top: 0 },
      { input: android, left: 0, top: 52 },
      { input: pwa, left: 402, top: 52 },
    ])
    .png().toFile(out)
  return out
}

async function stack(name, indexes, width = 792) {
  const images = await Promise.all(indexes.map(index => sharp(path.join(outputDir, 'pairs', pairs[index][2])).resize({ width }).png().toBuffer()))
  const metadata = await Promise.all(images.map(image => sharp(image).metadata()))
  const heights = metadata.map(item => item.height ?? 0)
  const canvas = sharp({ create: { width, height: heights.reduce((sum, height) => sum + height, 0), channels: 4, background: '#050709' } })
  let top = 0
  const composites = images.map((input, index) => {
    const item = { input, left: 0, top }
    top += heights[index]
    return item
  })
  await canvas.composite(composites).png().toFile(path.join(outputDir, name))
}

await fs.mkdir(outputDir, { recursive: true })
await Promise.all(pairs.map(createPair))
await stack('full_comparison_sheet.png', pairs.map((_, index) => index), 600)
await stack('home_navigation_sheet.png', [0, 1, 14, 15, 16, 17])
await stack('goal_pvp_sheet.png', [3, 4, 5])
await stack('presets_sheet.png', [6, 7, 8, 9])
await stack('event_guide_sheet.png', [10, 11, 12, 13])

console.log(`Created comparison sheets in ${outputDir}`)
