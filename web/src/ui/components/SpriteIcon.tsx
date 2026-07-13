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

// CSS-only icon glyphs — no emoji, no images. Simple geometric/SVG-like single char symbols.
const iconGlyphs: Record<string, string> = {
  safe_cleanup: '\u2727',       // ✧
  candy_prep: '\u2736',        // ✶
  trade_fodder: '\u21C4',      // ⇄
  hundo_check: '\u2606',       // ☆
  nundo_finder: '\u25CB',      // ○
  pvp_candidates: '\u265F',    // ♟
  lucky_trade: '\u2748',       // ❈
  untagged: '\u22A1',          // ⊡
  expert: '\u2692',            // ⚒
  events: '\u29D6',            // ⧖ (hourglass)
  explain: '\u2318',           // ⌘ (command)
  assistant: '\u2728',         // ✨
  home: '\u2302',              // ⌂ (house)
  settings: '\u2699',          // ⚙
  lock: '\u29BF',              // ⦿ circled bullet — NO emoji; geometric replacement for 🔒
  cloud_off: '\u2601',         // ☁
  copy: '\u29C9',              // ⧉
}

export function goalIcon(key: string): string {
  return iconGlyphs[key] ?? '\u25CF' // ● fallback
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