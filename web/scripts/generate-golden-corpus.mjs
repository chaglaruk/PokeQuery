#!/usr/bin/env node
// Generates the golden parity corpus — a shared JSON of inputs → expected outputs
// that both Vitest (web) and Android JUnit can consume to verify 1:1 engine parity.
//
// Output: web/src/parity/golden-corpus.json
// Also copies to: app/src/test/resources/golden-corpus.json (for Android)

import { writeFileSync, mkdirSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const webParityDir = join(__dirname, '..', 'src', 'parity')
const androidTestResDir = join(__dirname, '..', '..', 'app', 'src', 'test', 'resources')

// Import the engine functions directly as ES modules
// Note: This script must be run with --input-type=module or as .mjs

const corpus = {
  schemaVersion: 1,
  description: 'Golden parity corpus — shared between Vitest (web) and Android JUnit. Every test case must produce byte-identical output on both platforms.',
  generatedAt: new Date().toISOString(),
  testCases: [
    // --- buildGoal: English (default) ---
    {
      id: 'goal-hundo-check-en',
      category: 'buildGoal',
      input: { goalId: 'hundo_check', config: '', customQuery: '', language: 'English' },
      expected: {
        rawSyntax: '4*',
        riskLevel: 'Info',
        scopeBreadth: 'Very Narrow',
        warnings: [],
        protectedCategories: [],
        includedHighRiskCategories: [
          'shiny', 'legendary', 'mythical', 'ultrabeast', 'costume',
          'background', 'locationbackground', 'specialbackground',
          'shadow', 'purified', 'favorite', 'lucky', '#', 'traded', '4*',
        ],
        goalId: 'hundo_check',
        title: 'Hundo Check',
      },
    },
    {
      id: 'goal-nundo-finder-en',
      category: 'buildGoal',
      input: { goalId: 'nundo_finder', config: '', customQuery: '', language: 'English' },
      expected: {
        rawSyntax: '0attack&0defense&0hp',
        riskLevel: 'Info',
        scopeBreadth: 'Very Narrow',
        warnings: [],
        protectedCategories: [],
        goalId: 'nundo_finder',
        title: 'Nundo Finder',
      },
    },
    {
      id: 'goal-pvp-great-en',
      category: 'buildGoal',
      input: { goalId: 'pvp_candidates', config: 'great', customQuery: '', language: 'English' },
      expected: {
        rawSyntax: '0-1attack&3-4defense&3-4hp&cp-1500',
        riskLevel: 'Info',
        scopeBreadth: 'Narrow',
        warnings: [],
        protectedCategories: [],
        goalId: 'pvp_candidates',
        title: 'PvP IV Candidates',
      },
    },
    {
      id: 'goal-pvp-ultra-en',
      category: 'buildGoal',
      input: { goalId: 'pvp_candidates', config: 'ultra', customQuery: '', language: 'English' },
      expected: {
        rawSyntax: '0-1attack&3-4defense&3-4hp&cp-2500',
        riskLevel: 'Info',
        scopeBreadth: 'Narrow',
        warnings: [],
        protectedCategories: [],
        goalId: 'pvp_candidates',
        title: 'PvP IV Candidates',
      },
    },
    {
      id: 'goal-safe-cleanup-default-en',
      category: 'buildGoal',
      input: { goalId: 'safe_cleanup', config: '', customQuery: '', language: 'English' },
      expected: {
        rawSyntaxContains: ['1*', '!shiny', '!legendary', '!mythical', '!ultrabeast', '!costume', '!background', '!locationbackground', '!specialbackground', '!shadow', '!purified', '!favorite', '!lucky', '!#', '!4*'],
        rawSyntaxNotContains: ['!0*'],
        riskLevel: 'Medium',
        goalId: 'safe_cleanup',
        title: 'Safe Cleanup',
        warningsCount: 0,
      },
    },
    {
      id: 'goal-safe-cleanup-include0star-en',
      category: 'buildGoal',
      input: { goalId: 'safe_cleanup', config: 'include0Star', customQuery: '', language: 'English' },
      expected: {
        rawSyntaxContains: ['0*,1*', '!shiny', '!4*'],
        riskLevel: 'Medium',
        goalId: 'safe_cleanup',
      },
    },
    {
      id: 'goal-candy-prep-en',
      category: 'buildGoal',
      input: { goalId: 'candy_prep', config: '', customQuery: '', language: 'English' },
      expected: {
        rawSyntaxContains: ['count2-', '!shiny', '!lucky', '!legendary', '!mythical', '!shadow', '!purified', '!favorite', '!traded', '!costume', '!ultrabeast', '!background', '!locationbackground', '!specialbackground'],
        riskLevel: 'Medium',
        scopeBreadth: 'Broad',
        warningsContains: ['Count is based on Pokédex species number'],
        goalId: 'candy_prep',
        title: '2x Candy Prep',
      },
    },
    {
      id: 'goal-trade-fodder-en',
      category: 'buildGoal',
      input: { goalId: 'trade_fodder', config: '', customQuery: '', language: 'English' },
      expected: {
        rawSyntaxContains: ['count2-', '&!traded', '!shiny', '!lucky', '!legendary', '!costume'],
        riskLevel: 'Medium',
        warningsContains: [
          'Real trade eligibility depends on friendship level',
          'Count is based on Pokédex species number',
        ],
        goalId: 'trade_fodder',
        title: 'Trade Fodder',
      },
    },
    {
      id: 'goal-untagged-en',
      category: 'buildGoal',
      input: { goalId: 'untagged', config: '', customQuery: '', language: 'English' },
      expected: {
        rawSyntaxContains: ['!#', '!shiny'],
        riskLevel: 'Low',
        goalId: 'untagged',
        title: 'Untagged Cleanup',
      },
    },
    {
      id: 'goal-lucky-trade-age-en',
      category: 'buildGoal',
      input: { goalId: 'lucky_trade', config: 'age', customQuery: '', language: 'English' },
      expected: {
        rawSyntax: 'age365-&!traded',
        riskLevel: 'Medium',
        scopeBreadth: 'Moderate',
        goalId: 'lucky_trade',
        title: 'Lucky Trade Prep',
      },
    },
    {
      id: 'goal-lucky-trade-distance-en',
      category: 'buildGoal',
      input: { goalId: 'lucky_trade', config: 'distance', customQuery: '', language: 'English' },
      expected: {
        rawSyntax: 'distance100-&!traded',
        riskLevel: 'Medium',
        goalId: 'lucky_trade',
        title: 'Lucky Trade Prep',
      },
    },
    // --- buildGoal: localized (Turkish) ---
    {
      id: 'goal-hundo-check-tr',
      category: 'buildGoal',
      input: { goalId: 'hundo_check', config: '', customQuery: '', language: 'Turkish' },
      expected: {
        rawSyntax: '4*',
        riskLevel: 'Info',
        goalId: 'hundo_check',
      },
    },
    {
      id: 'goal-candy-prep-tr',
      category: 'buildGoal',
      input: { goalId: 'candy_prep', config: '', customQuery: '', language: 'Turkish' },
      expected: {
        rawSyntaxContains: ['parlak', 'efsanevi', 'count2-', '!traded'],
        rawSyntaxNotContains: ['!shiny', '!legendary'],
        riskLevel: 'Medium',
        goalId: 'candy_prep',
      },
    },
    {
      id: 'goal-lucky-trade-age-tr',
      category: 'buildGoal',
      input: { goalId: 'lucky_trade', config: 'age', customQuery: '', language: 'Turkish' },
      expected: {
        rawSyntax: 'yaş365-&!traded',
        riskLevel: 'Medium',
        goalId: 'lucky_trade',
      },
    },
    // --- buildGoal: localized (German) ---
    {
      id: 'goal-hundo-check-de',
      category: 'buildGoal',
      input: { goalId: 'hundo_check', config: '', customQuery: '', language: 'German' },
      expected: {
        rawSyntax: '4*',
        riskLevel: 'Info',
        goalId: 'hundo_check',
      },
    },
    {
      id: 'goal-candy-prep-de',
      category: 'buildGoal',
      input: { goalId: 'candy_prep', config: '', customQuery: '', language: 'German' },
      expected: {
        rawSyntaxContains: ['schillernd', 'legendär', 'count2-', '!traded'],
        rawSyntaxNotContains: ['!shiny', '!legendary'],
        riskLevel: 'Medium',
        goalId: 'candy_prep',
      },
    },
    // --- buildString: pipe replacement ---
    {
      id: 'buildstring-pipe-replaced',
      category: 'buildString',
      input: { baseQuery: 'test|query', protections: 'DEFAULT', explanation: 'test', riskLevel: 'Low', goalId: 'custom', title: 'Custom Search', language: 'English' },
      expected: {
        rawSyntaxNotContains: ['|'],
        rawSyntaxContains: [','],
        warningsContains: ["The '|' operator is unsupported and was replaced with ','."],
      },
    },
    // --- buildString: count mandatory protections ---
    {
      id: 'buildstring-count-mandatory-no-optional-protections',
      category: 'buildString',
      input: { baseQuery: 'count2-', protections: 'EMPTY', explanation: 'test', riskLevel: 'Low', goalId: 'custom', title: 'Custom Search', language: 'English' },
      expected: {
        rawSyntaxContains: ['!shiny', '!legendary', '!mythical', '!lucky', '!shadow', '!purified', '!favorite', '!traded', '!costume', '!ultrabeast', '!background', '!locationbackground', '!specialbackground'],
        riskLevel: 'Medium',
        warningsContains: ['Count is based on Pokédex species number'],
      },
    },
    {
      id: 'buildstring-traded-invariant-survives',
      category: 'buildString',
      input: { baseQuery: 'count2-', protections: 'EMPTY', explanation: 'test', riskLevel: 'Low', goalId: 'custom', title: 'Custom Search', language: 'English' },
      expected: {
        rawSyntaxContains: ['!traded'],
      },
    },
    // --- Linter tests ---
    {
      id: 'lint-pipe-error',
      category: 'lint',
      input: { query: 'shiny|lucky' },
      expected: {
        hasError: true,
        warningsContain: ['|'],
      },
    },
    {
      id: 'lint-unsafe-count-error',
      category: 'lint',
      input: { query: 'count3-' },
      expected: {
        hasError: true,
        warningsContain: ['Unsafe count usage', '!costume'],
      },
    },
    {
      id: 'lint-count-shortcut-risky-tag-collision',
      category: 'lint',
      input: { query: 'count&shiny&#shiny' },
      expected: {
        hasError: true,
        warningsContain: ['count2-', 'Risky inclusion of shiny', 'collides'],
      },
    },
    {
      id: 'lint-nundo-exact-no-0star-advisory',
      category: 'lint',
      input: { query: '0attack&0defense&0hp' },
      expected: {
        warningsNotContain: ['0* is an IV band'],
      },
    },
    {
      id: 'lint-0star-advisory',
      category: 'lint',
      input: { query: '0*' },
      expected: {
        warningsContain: ['0* is an IV band'],
        hasError: false,
      },
    },
    {
      id: 'lint-pvp-bypasses-risky-inclusion',
      category: 'lint',
      input: { query: '0-1attack&3-4defense&3-4hp&cp-1500&shiny' },
      expected: {
        warningsNotContain: ['Risky inclusion'],
      },
    },
    {
      id: 'lint-trade-prep-advisory',
      category: 'lint',
      input: { query: 'age365-&!traded' },
      expected: {
        warningsContain: ['Trade prep search'],
        hasError: false,
      },
    },
    {
      id: 'lint-lucky-traded-positive-no-error',
      category: 'lint',
      input: { query: 'lucky&traded' },
      expected: {
        hasError: false,
      },
    },
    {
      id: 'lint-lucky-traded-comma-no-error',
      category: 'lint',
      input: { query: 'lucky,traded' },
      expected: {
        hasError: false,
      },
    },
    // --- ExpertCopyPolicy tests ---
    {
      id: 'expert-pipe-blocks-copy',
      category: 'expertCopyPolicy',
      input: { rawQuery: 'shiny|lucky' },
      expected: { canCopy: false },
    },
    {
      id: 'expert-unsafe-count-blocks-copy',
      category: 'expertCopyPolicy',
      input: { rawQuery: 'count' },
      expected: { canCopy: false },
    },
    {
      id: 'expert-empty-allows-copy',
      category: 'expertCopyPolicy',
      input: { rawQuery: '' },
      expected: { canCopy: true },
    },
    {
      id: 'expert-advisory-0star-allows-copy',
      category: 'expertCopyPolicy',
      input: { rawQuery: '0*' },
      expected: { canCopy: true },
    },
    {
      id: 'expert-safe-query-allows-copy',
      category: 'expertCopyPolicy',
      input: { rawQuery: '4*&!shiny' },
      expected: { canCopy: true },
    },
    {
      id: 'expert-lucky-traded-allows-copy',
      category: 'expertCopyPolicy',
      input: { rawQuery: 'lucky&traded' },
      expected: { canCopy: true },
    },
    {
      id: 'expert-shiny-positive-allows-copy',
      category: 'expertCopyPolicy',
      input: { rawQuery: 'shiny' },
      expected: { canCopy: true },
    },
    {
      id: 'expert-count2-shiny-blocks-copy',
      category: 'expertCopyPolicy',
      input: { rawQuery: 'count2-&shiny' },
      expected: { canCopy: false },
    },
    // --- Spanish/French/Italian localized ---
    {
      id: 'goal-candy-prep-es',
      category: 'buildGoal',
      input: { goalId: 'candy_prep', config: '', customQuery: '', language: 'Spanish' },
      expected: {
        rawSyntaxContains: ['variocolor', 'legendario', 'count2-', '!traded'],
        rawSyntaxNotContains: ['!shiny', '!legendary'],
        riskLevel: 'Medium',
      },
    },
    {
      id: 'goal-candy-prep-fr',
      category: 'buildGoal',
      input: { goalId: 'candy_prep', config: '', customQuery: '', language: 'French' },
      expected: {
        rawSyntaxContains: ['chromatique', 'légendaire', 'count2-', '!traded'],
        rawSyntaxNotContains: ['!shiny', '!legendary'],
        riskLevel: 'Medium',
      },
    },
    {
      id: 'goal-candy-prep-it',
      category: 'buildGoal',
      input: { goalId: 'candy_prep', config: '', customQuery: '', language: 'Italian' },
      expected: {
        rawSyntaxContains: ['cromatico', 'leggendario', 'count2-', '!traded'],
        rawSyntaxNotContains: ['!shiny', '!legendary'],
        riskLevel: 'Medium',
      },
    },
    // --- GoalStringBuilder: !traded invariant ---
    {
      id: 'goalbuilder-trade-fodder-traded-invariant',
      category: 'goalStringBuilder',
      input: { goalId: 'trade_fodder', optionalProtections: [], language: 'English' },
      expected: {
        rawSyntaxContains: ['!traded', 'count2-'],
      },
    },
    {
      id: 'goalbuilder-lucky-trade-traded-invariant',
      category: 'goalStringBuilder',
      input: { goalId: 'lucky_trade', config: 'age', optionalProtections: [], language: 'English' },
      expected: {
        rawSyntaxContains: ['age365-', '!traded'],
      },
    },
    {
      id: 'goalbuilder-no-duplicate-traded',
      category: 'goalStringBuilder',
      input: { goalId: 'trade_fodder', optionalProtections: ['traded'], language: 'English' },
      expected: {
        rawSyntaxContains: ['!traded'],
        rawSyntaxNotContains: ['!traded&!traded'],
      },
    },
    {
      id: 'goalbuilder-passthrough-hundo',
      category: 'goalStringBuilder',
      input: { goalId: 'hundo_check', optionalProtections: [], language: 'English' },
      expected: {
        rawSyntax: '4*',
      },
    },
    {
      id: 'goalbuilder-passthrough-pvp',
      category: 'goalStringBuilder',
      input: { goalId: 'pvp_candidates', config: 'great', optionalProtections: [], language: 'English' },
      expected: {
        rawSyntax: '0-1attack&3-4defense&3-4hp&cp-1500',
      },
    },
  ],
}

mkdirSync(webParityDir, { recursive: true })
const json = JSON.stringify(corpus, null, 2)
writeFileSync(join(webParityDir, 'golden-corpus.json'), json, 'utf-8')
console.log(`Written golden-corpus.json (${corpus.testCases.length} test cases)`)

try {
  mkdirSync(androidTestResDir, { recursive: true })
  writeFileSync(join(androidTestResDir, 'golden-corpus.json'), json, 'utf-8')
  console.log(`Copied to ${androidTestResDir}`)
} catch {
  console.log('Android test resources dir not found — skipping copy')
}
