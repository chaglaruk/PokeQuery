# Visual Gap Audit

**Target:** `ChatGPT Image 20 Haz 2026 01_53_00 (1).png` (Pokémon-themed mockup)
**Current:** Plain Compose UI with basic color tweaks.

## Comparison Checklist

### 1. Onboarding Screen
- **Mockup**: Full-screen rich night-map background with glowing routes. Large central illustration (glowing shield with magnifying glass, Pokémon characters flanking it). Trust indicators are presented as three distinct columns/rows with custom icons (Shield, Cloud, Lock) and descriptive text, integrated seamlessly into the background. Large blue CTA with an arrow icon.
- **Current**: Solid dark navy background (`Color(0xFF0F172A)`). Basic canvas circles. Trust indicators are trapped inside a single opaque rounded card. No large central hero illustration.
- **Gap**: Missing the full-screen map background, the large central hero visual (shield/magnifier), the multi-column/row icon layout for trust badges, and the gradient/sparkle typography on the title.

### 2. Home Screen
- **Mockup**: The top 40% of the screen is a seamless extension of the dark map background with glowing pins and routes. "What do you want to find?" is overlaid directly on this map. Goal cards have premium styling with subtle gradients, outer glows, and feature prominent character illustrations or high-quality icons. 
- **Current**: A tiny 140dp solid header box with three basic canvas circles. Cards are plain rounded boxes with standard Material icons.
- **Gap**: Missing the seamless map header background. Cards lack the premium gradient/glow borders and rich illustrations. 

### 3. Preview Screens (Safe Cleanup & Candy Prep)
- **Mockup**: Risk headers are rich cards featuring a glowing shield/warning icon and a faint topographical/radar map background *inside* the card. Search string card has a subtle border and monospace colored text. The "Copy" CTA is prominent (blue or amber). "What does this do?" and "About count" are premium panels with left-aligned stylized icons. Protected categories use pill-shaped chips with a distinct glowing border and teal checkmark icon.
- **Current**: Risk headers are solid color boxes (`riskColor.copy(alpha = 0.1f)`). Search string is just a box. Chips are plain text boxes.
- **Gap**: Missing the radar/map textures in the risk cards. Missing the premium layout for explanation panels. Chips lack the checkmark icon and border styling.

### 4. Bottom Navigation
- **Mockup**: Deep dark background, translucent or with a subtle top border. Unselected icons are muted; selected state features bright blue coloring.
- **Current**: Standard Material 3 `NavigationBar` with default pill-shaped indicators.
- **Gap**: The default M3 pill indicator clashes with the mockup's sleek, icon-focused selected state.

## Conclusion
The current UI is structurally functional but visually basic. A complete rebuild of the Compose layout hierarchies and custom drawing (or placeholder image loading) is required to achieve the layered, premium, "Pokémon GO-adjacent" aesthetic demanded by the mockup.
