#!/usr/bin/env node

// Golden corpus synchronization check.
// Fails if web/src/parity/golden-corpus.json and
// app/src/test/resources/golden-corpus.json differ byte-for-byte.
//
// Usage:
//   node scripts/check-golden-corpus.mjs          — verify
//   node scripts/check-golden-corpus.mjs --sync   — copy web → Android

import { readFileSync, writeFileSync, existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import { createHash } from 'crypto'

const __dirname = dirname(fileURLToPath(import.meta.url))
const webCorpus = resolve(__dirname, '../src/parity/golden-corpus.json')
const androidCorpus = resolve(__dirname, '../../app/src/test/resources/golden-corpus.json')

function md5(filePath) {
  const data = readFileSync(filePath)
  return createHash('md5').update(data).digest('hex')
}

if (!existsSync(webCorpus)) {
  console.error(`FAIL: web corpus not found: ${webCorpus}`)
  process.exit(1)
}

if (!existsSync(androidCorpus)) {
  console.error(`FAIL: Android corpus not found: ${androidCorpus}`)
  console.error('       Run: npm run sync:golden-corpus')
  process.exit(1)
}

const webMd5 = md5(webCorpus)
const androidMd5 = md5(androidCorpus)
const webSize = readFileSync(webCorpus).length
const androidSize = readFileSync(androidCorpus).length

console.log(`Web     MD5: ${webMd5}  (${webSize} bytes)  ${webCorpus}`)
console.log(`Android MD5: ${androidMd5}  (${androidSize} bytes)  ${androidCorpus}`)

if (process.argv.includes('--sync')) {
  writeFileSync(androidCorpus, readFileSync(webCorpus))
  console.log(`\nSynced: copied web corpus to Android (${webSize} bytes)`)
  process.exit(0)
}

if (webMd5 === androidMd5) {
  console.log('\nPASS: golden corpus files are byte-identical.')
  process.exit(0)
} else {
  console.error('\nFAIL: golden corpus files differ!')
  console.error('       Run: npm run sync:golden-corpus')
  process.exit(1)
}
