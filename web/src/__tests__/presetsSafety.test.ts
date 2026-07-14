import { describe, expect, it } from 'vitest'
import androidPresetsSource from '../../../app/src/main/java/com/caglar/pokequery/ui/screens/PresetsScreen.kt?raw'
import { buildPresetOutput, PRESETS, type Preset } from '@ui/presetCatalog'

describe('Popular Presets parity and safety', () => {
  it('keeps every PWA preset syntax tied to the Android canonical catalogue', () => {
    expect(PRESETS).toHaveLength(18)
    for (const preset of PRESETS) {
      expect(androidPresetsSource, preset.titleKey).toContain(`"${preset.syntax}"`)
    }
  })

  it('uses the selected search language while retaining English fallback tokens', () => {
    const recent = PRESETS.find(preset => preset.titleKey === 'preset_recent_catches')!
    expect(buildPresetOutput(recent, 'Turkish').rawSyntax).toBe('yaş0-7')
    expect(buildPresetOutput(recent, 'English').rawSyntax).toBe('age0-7')
  })

  it('routes count presets through mandatory protections and preserves !traded', () => {
    const preset = PRESETS.find(item => item.titleKey === 'preset_untraded_duplicates')!
    const output = buildPresetOutput(preset, 'English')
    expect(output.rawSyntax).toContain('!traded')
    expect(output.rawSyntax).toContain('!shiny')
    expect(output.rawSyntax).not.toContain('|')
    expect(output.copyBlocked).toBe(false)
  })

  it('blocks a forbidden operator through the shared policy', () => {
    const unsafe: Preset = { titleKey: 'unsafe', descKey: 'unsafe', syntax: 'shiny|legendary', risk: 'info', category: 'collection' }
    const output = buildPresetOutput(unsafe, 'English')
    expect(output.rawSyntax).not.toContain('|')
    expect(output.warnings.some(warning => warning.isError)).toBe(true)
    expect(output.copyBlocked).toBe(true)
  })
})
