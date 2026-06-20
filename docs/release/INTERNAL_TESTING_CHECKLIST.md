# Internal Testing Checklist

Please execute this manual testing checklist on a physical device:

- [ ] Install the `app-debug.apk` successfully.
- [ ] Launch the app from the launcher.
- [ ] **Onboarding Screen**: Verify hero image is dominant, trust icons display properly, and CTA navigates to Home.
- [ ] **Home Screen**: Verify the 2-column grid and correct colored glowing accents on goal cards.
- [ ] **Safe Cleanup**: Tap through, read copy, generate string, verify expected output.
- [ ] **2x Candy Prep**: Tap through, verify warning about count limitation displays clearly, generate string.
- [ ] **Trade Fodder**: Verify disclaimer regarding trade eligibility, generate string.
- [ ] **Hundo Check**: Generate string and verify it returns precisely `4*`.
- [ ] **Untagged Cleanup**: Generate string and ensure it includes `!#`.
- [ ] **Knowledge Base**: Scroll through, check term formatting, ensure no loading errors.
- [ ] **Expert Builder Linter**: Enter `|` and ensure linter warning appears.
- [ ] **Expert Builder Linter**: Enter `count2-` alone and ensure unsafe protection warning appears.
- [ ] **Favorites**: Save a generated string, verify it appears in the Favorites tab, test the "Copy" button, and delete it.
- [ ] **Settings**: Toggle "First-use Guide Seen", restart app, and confirm it behaves correctly (skips onboarding if toggled).
- [ ] **Pokémon GO Integration**: Copy at least one complex string (e.g., Safe Cleanup) and paste it into the real Pokémon GO search bar. Verify results are accurate.
