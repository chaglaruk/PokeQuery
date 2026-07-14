// Changelog data — ported from Changelog.kt
// The current version reads its highlights/safety notes/tester notes from
// i18n keys (what_changed_v066_*); past versions store English strings directly.

export interface ChangelogEntry {
  versionName: string
  versionCode: number
  releaseLabel: string
  title: string
  highlights: string[]
  safetyNotes: string[]
  testerNotes: string[]
  isCurrent: boolean
}

export const changelogEntries: ChangelogEntry[] = [
  {
    versionName: '0.7.3',
    versionCode: 23,
    releaseLabel: 'Phase 5 Patch',
    title: 'Widget Value & Event Guide Polish',
    highlights: [],
    safetyNotes: [],
    testerNotes: [],
    isCurrent: true,
  },
  {
    versionName: '0.7.0',
    versionCode: 20,
    releaseLabel: 'Phase 5',
    title: 'Home Screen Widgets',
    highlights: [
      'Goal Actions widget: Safe Cleanup, Candy Prep, Assistant, Event Guide quick actions',
      'Event Guide widget: event icons, status, category highlights, one-tap open',
      'Quick Access widget polish: card styling, consistent tap behavior',
    ],
    safetyNotes: [
      'INTERNET is used only for public Event Guide notes with cache and safe fallback',
      'No login, no tracking, no ads, no analytics',
      'No Pokemon GO account access',
      'Widgets open app via standard start_route — no silent clipboard writes',
    ],
    testerNotes: [
      'Add Goal Actions widget (4x2) and tap each action',
      'Add Event Guide widget (4x3) and tap Open Event Guide',
      'Verify Quick Access widget still opens Safe Cleanup',
      'Verify widgets work after reboot and locale change',
    ],
    isCurrent: false,
  },
  {
    versionName: '0.6.2',
    versionCode: 19,
    releaseLabel: 'Phase 4',
    title: 'Final Polish & Turkish Localization',
    highlights: [
      'Full Turkish UI localization (Event Guide, Onboarding, Goal Detail, Home)',
      'Search Assistant promoted to a primary Home card',
      'Nundo Finder moved to More Tools',
      'Public Event Guide notes with cache and safe bundled fallback',
    ],
    safetyNotes: [
      'INTERNET is used only for public Event Guide notes with cache and safe fallback',
      'No login, no tracking, no ads, no analytics',
      'No Pokemon GO account access',
      'Turkish search tokens remain beta — verify results in Pokemon GO',
    ],
    testerNotes: [
      'Verify Turkish localization applies to all visible UI',
      'Check Event Guide refresh and fallback notes',
      "Verify Search Assistant accepts Turkish input (e.g. 'parlak')",
    ],
    isCurrent: false,
  },
  {
    versionName: '0.6.1',
    versionCode: 18,
    releaseLabel: 'Phase 2',
    title: 'Workflows, Surface & Context',
    highlights: [
      'Personal Presets from favorites and history',
      'Cleaning Journal notes (local-only)',
      'Practice Mode with fake inventory sandbox',
      'Android App Shortcuts',
      'Quick Access home screen widget',
      'Offline manual Event Guide context',
    ],
    safetyNotes: [
      'No login, no tracking, no ads, no analytics',
      'No Pokemon GO account access',
      'No INTERNET permission, no CAMERA permission',
      'Journal is user-entered memory only — the app never knows what changed in-game',
      'Practice Mode uses a fake sandbox; it never reads your real inventory',
    ],
    testerNotes: [
      'Save a favorite as a Personal Preset',
      'Add a journal note from History',
      'Open Practice Mode and inspect matches/exclusions',
      'Long-press the app icon for shortcuts',
      'Add the Quick Access widget',
    ],
    isCurrent: false,
  },
  {
    versionName: '0.6.0',
    versionCode: 17,
    releaseLabel: 'Phase 1',
    title: 'Trust & Education',
    highlights: [
      'Risk explanations',
      'Common misconceptions',
      'Changelog screen',
      'Inventory size context',
    ],
    safetyNotes: [
      'No login',
      'No tracking',
      'No ads',
      'No analytics',
      'No Pokemon GO account access',
    ],
    testerNotes: [
      'Check Why this risk?',
      'Try Common Misconceptions',
      'Change inventory size context',
    ],
    isCurrent: false,
  },
  {
    versionName: '0.5.5',
    versionCode: 16,
    releaseLabel: 'Hardening',
    title: 'Audit hardening',
    highlights: [
      'Density consumption',
      'Turkish safety fallback',
      'Locale regression guard',
    ],
    safetyNotes: [
      'Count/background/Ultra Beast protections hardened',
    ],
    testerNotes: [
      'Regression-test Turkish fallback',
    ],
    isCurrent: false,
  },
  {
    versionName: '0.5.4',
    versionCode: 15,
    releaseLabel: 'Polish',
    title: 'Onboarding and layout polish',
    highlights: [
      'Onboarding fixes',
      'Layout fixes',
      'Radio polish',
    ],
    safetyNotes: [
      'No safety model changes',
    ],
    testerNotes: [
      'Verify settings selections',
    ],
    isCurrent: false,
  },
  {
    versionName: '0.5.3',
    versionCode: 14,
    releaseLabel: 'Motion',
    title: 'Motion polish',
    highlights: [
      'Subtle screen motion',
      'Reduced primitive transitions',
    ],
    safetyNotes: [
      'No tracking or analytics added',
    ],
    testerNotes: [
      'Check back navigation',
    ],
    isCurrent: false,
  },
  {
    versionName: '0.5.2',
    versionCode: 13,
    releaseLabel: 'Localization foundation',
    title: 'App Language black-screen fix',
    highlights: [
      'Locale black-screen fix',
      'Language foundation labels',
    ],
    safetyNotes: [
      'No OS LocaleManager recreation path',
    ],
    testerNotes: [
      'Switch App Language safely',
    ],
    isCurrent: false,
  },
]
