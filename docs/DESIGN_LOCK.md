# Design Lock & Rules

## GLOBAL FAIL CONDITIONS
* If the same giant shield hero appears on more than one screen, the UI fails.
* If Home does not show a 2-column grid of goal cards, the UI fails.
* If Preview does not show a search string panel, copy button, risk header, explanation panel, and protected chips, the UI fails.
* If Knowledge, Expert, Favorites, and Settings look like onboarding, the UI fails.
* If contact_sheet.png still visually resembles the old shield-only sheet, the UI fails.
* If only colors changed, the UI fails.

## BANNED PATTERNS
* Reusing HeroIllustrationPlaceholder across all screens
* One generic shield as the main visual for every route
* Same layout for all bottom-nav tabs
* Placeholder-only cards
* Plain Compose Column with one trust card
* Documentation-only "visual alignment"

## REQUIRED COMPONENTS
1. **OnboardingHero**: Only used on Onboarding. Large hero visual allowed here.
2. **HomeMapHeader**: Used only on Home. Horizontal map/exploration header.
3. **GoalCardGrid**: Home must show 6 cards in 2 columns (Safe Cleanup, 2x Candy Prep, Trade Fodder, Hundo Check, Untagged Cleanup, Expert Builder).
4. **RiskHeaderCard**: Used in preview screens. Small/medium horizontal header.
5. **SearchStringPanel**: Large monospace final string box.
6. **CopyCTA**: Large blue button for Low Risk. Large amber/yellow button for Medium Risk.
7. **ExplanationCard**: "What does this do?" panel.
8. **ProtectedChipGrid**: Shows protected categories as chips.
9. **WarningInfoPanel**: For count/trade warnings.
10. **KnowledgeTermCard**: For Knowledge Base.
11. **ExpertEditorPanel**: For Expert Builder.
12. **EmptyFavoritesPanel**: For Favorites.
13. **SettingsCard**: For Settings.
