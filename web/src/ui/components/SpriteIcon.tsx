// Reusable sprite image component for PokeQuery sprites in public/sprites/.
// Supports both PNG and WebP (auto-detected from the sprite name).

interface SpriteIconProps {
  sprite: string
  alt?: string
  size?: number
  width?: number
  height?: number
  className?: string
}

const KNOWN_WEBP = new Set([
  'onboarding_hero',
  'onboarding_hero_scene',
  'onboarding_hero_wide',
  'home_header_bg',
  'safe_cleanup_header',
  'candy_prep_header',
  'detail_header_blue',
  'detail_header_gold',
  'lucky_trade_header',
  'nundo_header',
  'pvp_header',
  'trade_fodder_header',
  'goal_safe_cleanup_icon',
  'goal_hundo_icon',
  'goal_candy_prep_icon',
  'goal_expert_icon',
  'goal_tag_icon',
  'goal_trade_icon',
  'empty_favorites',
  'app_icon_source',
  'logo_wordmark_source',
])

export function SpriteIcon({ sprite, alt = '', size = 48, width, height, className }: SpriteIconProps) {
  const ext = KNOWN_WEBP.has(sprite) ? 'webp' : 'png'
  const src = `${import.meta.env.BASE_URL}sprites/${sprite}.${ext}`
  const w = width ?? size
  const h = height ?? size
  return (
    <img
      src={src}
      alt={alt}
      width={w}
      height={h}
      className={className}
      style={{ display: 'block', maxWidth: '100%' }}
    />
  )
}

const iconPaths: Record<string, string> = {
  safe_cleanup: 'M4 7h16M9 7V4h6v3M7 7l1 13h8l1-13M10 11v5M14 11v5',
  candy_prep: 'M8 8l8 8M7 3l4 3-5 5-3-4 4-4M17 21l-4-3 5-5 3 4-4 4',
  trade_fodder: 'M7 7h11l-3-3M17 17H6l3 3',
  hundo_check: 'M12 20s-7-4.5-7-10a4 4 0 0 1 7-2 4 4 0 0 1 7 2c0 5.5-7 10-7 10Z',
  nundo_finder: 'M11 18a7 7 0 1 1 0-14 7 7 0 0 1 0 14Zm5-2 5 5',
  pvp_candidates: 'M12 3 5 6v5c0 4.6 2.8 8 7 10 4.2-2 7-5.4 7-10V6l-7-3Z',
  lucky_trade: 'M12 12c-5-1-6-6-3-8 3 0 4 3 3 8Zm0 0c1-5 6-6 8-3 0 3-3 4-8 3Zm0 0c5 1 6 6 3 8-3 0-4-3-3-8Zm0 0c-1 5-6 6-8 3 0-3 3-4 8-3Z',
  untagged: 'M4 5v6l9 9 7-7-9-9H5a1 1 0 0 0-1 1Zm4 3h.01',
  expert: 'M4 6h10M18 6h2M4 12h2M10 12h10M4 18h7M15 18h5M14 4v4M6 10v4M11 16v4',
  events: 'M6 3v3M18 3v3M4 8h16M5 5h14a1 1 0 0 1 1 1v14H4V6a1 1 0 0 1 1-1Zm3 7h3v3H8v-3Z',
  explain: 'm9 7-5 5 5 5M15 7l5 5-5 5',
  assistant: 'm4 20 10-10M13 4l1 3 3 1-3 1-1 3-1-3-3-1 3-1 1-3ZM18 14l.7 2.3L21 17l-2.3.7L18 20l-.7-2.3L15 17l2.3-.7L18 14Z',
  home: 'm3 11 9-8 9 8M5 10v10h14V10M9 20v-6h6v6',
  settings: 'M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6Zm8 3 2-1-2-4-2 .5-1.5-1L16 4h-4l-.5 2.5-1.5 1L8 7 6 11l2 1v2l-2 1 2 4 2-.5 1.5 1L12 22h4l.5-2.5 1.5-1 2 .5 2-4-2-1v-2Z',
  favorite: 'M12 20s-7-4.4-7-10a4.2 4.2 0 0 1 7-2.7A4.2 4.2 0 0 1 19 10c0 5.6-7 10-7 10Z',
  history: 'M4 12a8 8 0 1 0 2.3-5.7L4 8.6M4 4v4.6h4.6M12 7v5l3 2',
  delete: 'M4 7h16M9 7V4h6v3M7 7l1 13h8l1-13M10 11v5M14 11v5',
  search: 'M11 18a7 7 0 1 1 0-14 7 7 0 0 1 0 14Zm5-2 5 5',
  chevron: 'm8 10 4 4 4-4',
  lock: 'M6 10h12v10H6V10Zm3 0V7a3 3 0 0 1 6 0v3',
  cloud_off: 'm3 3 18 18M7 17H6a4 4 0 0 1-.8-7.9A6 6 0 0 1 16 7.5a4.5 4.5 0 0 1 4 6.5v1M9 6.2A6 6 0 0 1 16 12l4 4',
  copy: 'M8 8h11v12H8V8ZM5 16H4V4h11v1',
  warning: 'M12 4 3 20h18L12 4Zm0 5v5m0 3h.01',
  info: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm0-11v6m0-10h.01',
  refresh: 'M20 6v5h-5M4 18v-5h5M18.5 9A7 7 0 0 0 6 6.5L4 9m2 6a7 7 0 0 0 12 2.5l2-2.5',
  check: 'm5 12 4 4L19 6',
  error: 'm7 7 10 10M17 7 7 17',
}

export function AppIcon({ name, size = 24, label, className }: { name: string; size?: number; label?: string; className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      width={size}
      height={size}
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      role={label ? 'img' : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
      focusable="false"
    >
      <path d={iconPaths[name] ?? 'M12 4a8 8 0 1 0 0 16 8 8 0 0 0 0-16Z'} />
    </svg>
  )
}

export function spriteKeyForGoal(goalId: string): string | null {
  const map: Record<string, string> = {
    safe_cleanup: 'event_mewtwo',
    candy_prep: 'event_eevee',
    trade_fodder: 'event_pikachu',
    hundo_check: 'event_zeraora',
    pvp_candidates: 'event_necrozma',
  }
  return map[goalId] ?? null
}
