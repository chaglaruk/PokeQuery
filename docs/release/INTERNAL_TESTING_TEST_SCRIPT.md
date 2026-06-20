# Internal Testing Test Script

When inviting internal testers to PokeQuery via Google Play Console, ask them to verify the following flow:

1. **Install the App:** Download from the Play Console internal testing link.
2. **Onboarding:** Swipe through the onboarding cards and tap "Get Started".
3. **Builder/Home:** Tap "Safe Cleanup".
4. **Options:** Toggle "Include 0★ Candidates" on and off. Ensure the string updates dynamically.
5. **Copy:** Tap "Copy Search String". Verify a toast appears confirming the copy.
6. **External Verification:** Open Pokémon GO (or a text app) and paste the string to verify it copied correctly.
7. **Favorites:** Tap the Star icon on the top right. Navigate to the Favorites tab on the bottom bar and ensure the saved string appears.
8. **Settings:** Navigate to the Settings tab, switch output language to Turkish, and verify the Builder outputs Turkish localized strings.
