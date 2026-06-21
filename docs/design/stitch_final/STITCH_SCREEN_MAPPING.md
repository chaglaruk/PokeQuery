# Stitch UI → PokeQuery Screen Mapping (v0.5.0)

**Date:** 2026-06-21 · **Branch:** `feature/stitch-ui-overhaul-v050`
**Stitch references:** `docs/design/stitch_final/core/` + `supplemental/` (20 variant `screen.png` files)
**PRD source-of-truth:** `stitch_core_project_brief.txt` + `stitch_supplemental_project_brief.txt`

## Design tokens (from PRD — authoritative)

| Token | Value | Usage |
|---|---|---|
| Deep Navy background | `#0A0E1A` | App background (replaces current `#040A18`) |
| Slate Black | `#050709` | Cards / bottom nav |
| Electric Cyan primary | `#00E5FF` | Primary accent, safe/verified, CTAs |
| Gold caution | `#FFD700` | Warnings, Medium risk, Beta |
| Red high-risk | `#FF5252` | High risk |
| Green verified | `#00E676` | Verified status badges |
| High-contrast text | `#FFFFFF` / `#B0BEC5` | Primary / secondary text |
| Monospace | search-string hero boxes | `0attack&!shiny` etc. |

## Screens mapped (Stitch → current app)

| # | Stitch reference | Current app screen | Status |
|---|---|---|---|
| 1 | core _1 | **Knowledge Base** | Analyzed: search + expandable categories + Verified/Beta/Risky badges |
| 2 | core _5 | **Favorites** (empty + populated) | Analyzed: string cards + Safe shield + Copy/Remove + empty state |
| 3 | core _10 | **Home** | Analyzed: header + 3 trust chips (horizontal) + 2-col goal grid + bottom nav |
| 4 | supplemental _2 | **Settings + Feedback dialog** | Analyzed: General + Coming Later + Feedback dialog |
| 5 | core _2,_3,_4,_6,_7,_8,_9 | **Goal Detail screens** (Safe Cleanup, Candy Prep, Trade Fodder, Lucky Trade, PvP, Hundo, Nundo) | Mapped from PRD §3.2 + Home card vocabulary: string hero box + Copy CTA + risk badge + explanation + protections + manual-review panel |
| 6 | core _11,_12,_13 | **Expert Builder / Presets / Onboarding** | Mapped from PRD §3.1, §3.3, §3.4: modular token chips + live preview + linter assistant |
| 7 | supplemental _1,_3,_5,_6 | **Risk Warning / History / Presets / Copy success / Confirmation dialogs** | Mapped from PRD §3.5 + confirmed Settings dialog pattern |

## Ambiguous mappings (resolved)

- **"Home Builder" nav label:** Stitch bottom-nav shows "Home Builder" (not "Builder"). Adopt the Stitch label "Home".
- **"PokeQuery" logo header on KB/Settings:** Stitch repeats the logo+tagline header on non-Home screens. Adopt a compact `PqTopBar` with back nav + title; the full logo header stays on Home/Onboarding only.
- **Version strings in Stitch (`1.0.0 (8)`):** Stitch placeholders. App uses real `0.5.0 (11)` via `AppVersion` / `BuildConfig`. Never copy Stitch version numbers.
- **Search strings in Stitch:** Placeholders. All real strings come from `StringBuilderEngine` / `GoalStringBuilder` / `Linter` / `ExpertCopyPolicy` — never hardcoded.
- **"Coming Later" features (Cloud Sync, Community Presets, Import/Export, Auto DB Updates):** Stitch shows these as disabled. Implement as `PqComingLaterCard` (visually present, tap shows info, never active, no network).

## Unsupported Stitch features (kept disabled / omitted)

| Feature | Disposition |
|---|---|
| "Execute Cleanup" / one-tap transfer | **Omit entirely.** App is manual copy/paste only. |
| Account analysis / real risk % | **Omit.** No account access, no data. |
| Screen scan / OCR | **Omit.** App generates text only. |
| Cloud sync / online database | **Coming Later card (disabled).** Offline-first. |
| Community preset packs | **Coming Later card (disabled).** |
| Import/Export | **Coming Later card (disabled).** |
| Automatic database updates | **Coming Later card (disabled).** Local KB only. |

## Component → Stitch vocabulary

| Pq component | Stitch element |
|---|---|
| `PqBackground` | Deep navy gradient surface |
| `PqTopBar` | Back arrow + title (compact on non-Home) |
| `PqBottomNav` | Home / Favorites / History / Knowledge / Settings |
| `PqGlowCard` | Refined card with soft cyan glow border |
| `PqGoalCard` | 2-col grid card: icon chip + title + subtitle + accent rail |
| `PqStringBox` | Monospace hero box with the generated string (dark, cyan text) |
| `PqPrimaryButton` | Full-width cyan CTA (Copy String) |
| `PqRiskBadge` | Info=blue / Low=cyan / Medium=gold / High=red |
| `PqTrustChip` | Offline-First / No Login / No Tracking |
| `PqChip` | Filter/token chips for Expert Builder |
| `PqVerificationBadge` | Verified(green) / Beta(gold) / Risky(red) / Language-sensitive(cyan) |
| `PqEmptyState` | Large icon + title + subtitle + CTA |
| `PqComingLaterCard` | Disabled feature card with lock + "Coming later" |
| `PqFeedbackSheet` | Dialog: Send via Email/Share (mailto intent) |
| `PqConfirmationDialog` | Confirm destructive actions |
