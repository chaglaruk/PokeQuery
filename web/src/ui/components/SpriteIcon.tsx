// Reusable sprite image component for PokeQuery sprites in public/sprites/.

interface SpriteIconProps {
  sprite: string
  alt?: string
  size?: number
  className?: string
}

export function SpriteIcon({ sprite, alt = '', size = 48, className }: SpriteIconProps) {
  const src = `${import.meta.env.BASE_URL}sprites/${sprite}.png`
  return (
    <img
      src={src}
      alt={alt}
      width={size}
      height={size}
      className={className}
      style={{ display: 'block' }}
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
  lock: '\uD83D\uDD12',        // 🔒
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