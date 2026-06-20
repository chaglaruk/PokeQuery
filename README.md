# PokeQuery

PokeQuery is an offline-first Android application that helps Pokémon GO players generate safe, copyable Pokémon GO search strings for storage management.

## Features
- **Offline-first**: Works entirely without internet. No login required.
- **Safe Defaults**: Protects shiny, legendary, mythical, ultrabeast, shadow, purified, favorite, lucky, traded, and special IV Pokémon by default.
- **Generates Safe Search Strings**: Converts user intentions into Pokemon GO search syntax.
- **Pure Kotlin Domain Engine**: Tested, rule-based string builder.

## Safety Rules
- Does NOT log into Pokémon GO
- Does NOT use unofficial APIs
- Does NOT scrape account data
- Does NOT automate gameplay
- Does NOT spoof or bot

## Tech Stack
- Kotlin
- Jetpack Compose
- DataStore (for settings/favorites)
