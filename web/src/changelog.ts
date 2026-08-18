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
    versionName: '0.7.5',
    versionCode: 25,
    releaseLabel: 'Closed Testing',
    title: 'Pre-AAB Safety & Search Assistant',
    highlights: [],
    safetyNotes: [],
    testerNotes: [],
    isCurrent: true,
  },
  {
    versionName: '0.7.4',
    versionCode: 24,
    releaseLabel: 'Closed Testing',
    title: 'Bottom-Navigation Inset Fix',
    highlights: [
      'Fixed bottom-navigation inset for proper content spacing on devices with gesture navigation',
      'Google Play closed-testing release',
    ],
    safetyNotes: [
      'INTERNET is used only for public Event Guide notes with cache and safe fallback',
      'No login, no tracking, no ads, no analytics',
      'No Pokemon GO account access',
    ],
    testerNotes: [
      'Verify bottom-navigation padding on a device with gesture navigation',
      'Confirm content is not obscured behind navigation bar',
    ],
    isCurrent: false,
  },
  {
    versionName: '0.7.3',
    versionCode: 23,
    releaseLabel: 'Phase 5 Patch',
    title: 'Widget Value & Event Guide Polish',
    highlights: [],
    safetyNotes: [],
    testerNotes: [],
    isCurrent: false,
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
    versionName: '0.6.8',
    versionCode: 18,
    releaseLabel: 'Phase 4',
    title: 'Full Localization',
    highlights: [],
    safetyNotes: [],
    testerNotes: [],
    isCurrent: false,
  },
  {
    versionName: '0.6.7',
    versionCode: 17,
    releaseLabel: 'Phase 3',
    title: 'Search Intelligence',
    highlights: [],
    safetyNotes: [],
    testerNotes: [],
    isCurrent: false,
  },
  {
    versionName: '0.6.6',
    versionCode: 16,
    releaseLabel: 'Phase 2',
    title: 'Safety & Trust',
    highlights: [],
    safetyNotes: [],
    testerNotes: [],
    isCurrent: false,
  },
  {
    versionName: '0.6.5',
    versionCode: 15,
    releaseLabel: 'Release',
    title: 'Safety & Search Assistant',
    highlights: [],
    safetyNotes: [],
    testerNotes: [],
    isCurrent: false,
  },
]
