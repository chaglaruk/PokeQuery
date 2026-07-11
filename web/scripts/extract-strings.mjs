#!/usr/bin/env node
// Extracts string resources from Android strings.xml files into TypeScript modules.
// Usage: node scripts/extract-strings.mjs
// Reads:  ../app/src/main/res/values*/strings.xml
// Writes: ../src/i18n/locales/{en,tr,de,es,fr,it}.ts

import { readFileSync, writeFileSync, readdirSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const androidResDir = join(__dirname, '..', '..', 'app', 'src', 'main', 'res')
const localeDir = join(__dirname, '..', 'src', 'i18n', 'locales')

const localeDirs = {
  en: 'values',
  tr: 'values-tr',
  de: 'values-de',
  es: 'values-es',
  fr: 'values-fr',
  it: 'values-it',
}

function parseStringsXml(xml) {
  const strings = {}
  const regex = /<string\s+name="([^"]+)"\s*(?:[^>]*)>([\s\S]*?)<\/string>/g
  let match
  while ((match = regex.exec(xml)) !== null) {
    const key = match[1]
    let value = match[2]
    // Unescape XML entities
    value = value
      .replace(/&/g, '&')
      .replace(/</g, '<')
      .replace(/>/g, '>')
      .replace(/'/g, "'")
      .replace(/"/g, '"')
      // Android format specifiers
      .replace(/%1\$s/g, '{0}')
      .replace(/%2\$s/g, '{1}')
      .replace(/%3\$s/g, '{2}')
      .replace(/%1\$d/g, '{0}')
      .replace(/%2\$d/g, '{1}')
      .replace(/\\n/g, '\n')
      .replace(/\\'/g, "'")
      .replace(/\\"/g, '"')
    strings[key] = value
  }
  return strings
}

for (const [locale, dir] of Object.entries(localeDirs)) {
  const xmlPath = join(androidResDir, dir, 'strings.xml')
  try {
    const xml = readFileSync(xmlPath, 'utf-8')
    const strings = parseStringsXml(xml)
    const ts = `// Auto-generated from android/app/src/main/res/${dir}/strings.xml\n// Do not edit manually.\n\nexport const ${locale}: Record<string, string> = ${JSON.stringify(strings, null, 2)}\n`
    writeFileSync(join(localeDir, `${locale}.ts`), ts, 'utf-8')
    console.log(`Extracted ${Object.keys(strings).length} strings for ${locale}`)
  } catch (e) {
    console.error(`Could not read ${xmlPath}: ${e.message}`)
    // Write empty placeholder
    const ts = `// Placeholder — source XML not found\n\nexport const ${locale}: Record<string, string> = {}\n`
    writeFileSync(join(localeDir, `${locale}.ts`), ts, 'utf-8')
  }
}

console.log('Done.')
