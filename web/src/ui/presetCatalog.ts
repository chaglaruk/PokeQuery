import { buildString } from '@engine/stringBuilderEngine'
import { lint, type LintWarning } from '@engine/linter'
import { canCopy } from '@engine/expertCopyPolicy'
import type { RiskLevel } from '@/types'

export type PresetRisk = 'low' | 'medium' | 'info'
export type PresetCategory = 'cleanup' | 'candy_event' | 'trading' | 'battle_iv' | 'collection'

export interface Preset {
  titleKey: string
  descKey: string
  syntax: string
  risk: PresetRisk
  category: PresetCategory
}

export const PRESETS: Preset[] = [
  { titleKey: 'preset_recent_catches', descKey: 'preset_desc_recent_catches', syntax: 'age0-7', risk: 'low', category: 'cleanup' },
  { titleKey: 'preset_low_iv_cleanup', descKey: 'preset_desc_low_iv_cleanup', syntax: '0*,1*&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground', risk: 'medium', category: 'cleanup' },
  { titleKey: 'preset_duplicate_cleanup', descKey: 'preset_desc_duplicate_cleanup', syntax: 'count2-&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground', risk: 'medium', category: 'cleanup' },
  { titleKey: 'preset_untagged_review', descKey: 'preset_desc_untagged_review', syntax: '!#&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!traded&!costume&!background&!locationbackground&!specialbackground', risk: 'low', category: 'cleanup' },
  { titleKey: 'preset_evolve_ready', descKey: 'preset_desc_evolve_ready', syntax: 'evolve', risk: 'low', category: 'candy_event' },
  { titleKey: 'preset_recent_event_review', descKey: 'preset_desc_recent_event_review', syntax: 'age0-3', risk: 'low', category: 'candy_event' },
  { titleKey: 'preset_untraded_duplicates', descKey: 'preset_desc_untraded_duplicates', syntax: 'count2-&!traded', risk: 'medium', category: 'trading' },
  { titleKey: 'preset_older_untraded', descKey: 'preset_desc_older_untraded', syntax: 'age365-&!traded', risk: 'medium', category: 'trading' },
  { titleKey: 'preset_distance_trade', descKey: 'preset_desc_distance_trade', syntax: 'distance100-&!traded', risk: 'medium', category: 'trading' },
  { titleKey: 'preset_special_trade', descKey: 'preset_desc_special_trade', syntax: 'shiny,legendary,mythical', risk: 'info', category: 'trading' },
  { titleKey: 'preset_hundo', descKey: 'preset_desc_hundo', syntax: '4*', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_nundo', descKey: 'preset_desc_nundo', syntax: '0attack&0defense&0hp', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_great_league', descKey: 'preset_desc_great_league', syntax: '0-1attack&3-4defense&3-4hp&cp-1500', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_ultra_league', descKey: 'preset_desc_ultra_league', syntax: '0-1attack&3-4defense&3-4hp&cp-2500', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_perfect_shadows', descKey: 'preset_desc_perfect_shadows', syntax: 'shadow&4*', risk: 'info', category: 'battle_iv' },
  { titleKey: 'preset_shiny_review', descKey: 'preset_desc_shiny_review', syntax: 'shiny', risk: 'info', category: 'collection' },
  { titleKey: 'preset_costume_review', descKey: 'preset_desc_costume_review', syntax: 'costume', risk: 'info', category: 'collection' },
  { titleKey: 'preset_lucky_review', descKey: 'preset_desc_lucky_review', syntax: 'lucky', risk: 'info', category: 'collection' },
]

export const PRESET_CATEGORIES: PresetCategory[] = ['cleanup', 'candy_event', 'trading', 'battle_iv', 'collection']

export interface PresetOutput {
  rawSyntax: string
  riskLevel: RiskLevel
  warnings: LintWarning[]
  copyBlocked: boolean
}

export function buildPresetOutput(preset: Preset, language: string): PresetOutput {
  const canonical = buildString(preset.syntax, [], '', preset.risk === 'info' ? 'Info' : preset.risk === 'medium' ? 'Medium' : 'Low', 'preset', '', 'English')
  const localized = buildString(preset.syntax, [], '', canonical.riskLevel, 'preset', '', language)
  const warnings = lint(preset.syntax.includes('|') ? preset.syntax : canonical.rawSyntax)
  return {
    rawSyntax: localized.rawSyntax,
    riskLevel: localized.riskLevel,
    warnings,
    copyBlocked: preset.syntax.includes('|') || localized.rawSyntax.includes('|') || !canCopy(canonical.rawSyntax),
  }
}
