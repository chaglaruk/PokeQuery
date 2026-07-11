#!/usr/bin/env node
// Generates PWA PNG icons from the SVG favicon.
// Produces: pwa-192x192.png, pwa-512x512.png, apple-touch-icon.png (180x180)

import sharp from 'sharp'
import { readFileSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const publicDir = join(__dirname, '..', 'public')
const svgPath = join(publicDir, 'favicon.svg')
const svgBuffer = readFileSync(svgPath)

const icons = [
  { name: 'pwa-192x192.png', size: 192 },
  { name: 'pwa-512x512.png', size: 512 },
  { name: 'apple-touch-icon.png', size: 180 },
]

for (const icon of icons) {
  await sharp(svgBuffer)
    .resize(icon.size, icon.size)
    .png()
    .toFile(join(publicDir, icon.name))
  console.log(`Generated ${icon.name} (${icon.size}x${icon.size})`)
}

console.log('All PWA icons generated.')
