# PokeQuery Event Feed Pipeline

`pokequery-events.json` is a PokeQuery-owned public static JSON feed. The Android app consumes this static feed via HTTPS, validates it, caches it locally, and falls back to a bundled raw JSON fixture if fetching fails.

## How the Feed is Generated

The feed is generated automatically by a Python script:
- **Generator Script**: [generate_event_feed.py](file:///C:/Temp/PokeQuery-v072-event-guide-feed/scripts/generate_event_feed.py)
- **Sources configuration**: [sources.json](file:///C:/Temp/PokeQuery-v072-event-guide-feed/docs/event-feed/sources.json)
- **Event enrichment metadata**: [event_metadata.json](file:///C:/Temp/PokeQuery-v072-event-guide-feed/docs/event-feed/event_metadata.json)

### Sources Used
1. **Official Pokémon GO Live News**: `https://pokemongolive.com/news/` (used for verified official announcements, dates, and event pages).
2. **Leek Duck Events**: `https://leekduck.com/events/` (used as fallback and enrichment for event categories/spawns).

### Event Enrichment & Review
To ensure high-quality details (such as pipe-free search strings, correct localizations, and verified Pokémon spawns/categories), the generator script:
1. Automatically parses the live lists from both sources.
2. Performs de-duplication, preferring official sources when conflicts occur.
3. Looks up the stable event ID in `event_metadata.json`. If found, it enriches the event entry with verified descriptions, translations, and spawning templates.
4. If not found, it generates a clean entry with safe fallback values and source attribution.
5. Sorts the output feed with `CURRENT` (live) events first, then `UPCOMING` events by start date.

## Commands

### Run Generator
To run the generator locally in offline mode using static mock fixtures:
```bash
python scripts/generate_event_feed.py --fixture-mode
```

To run the generator in online mode (fetches live pages):
```bash
python scripts/generate_event_feed.py
```

### Validate Feed
To run the schema and safety constraint validator on the generated JSON file:
```bash
python scripts/validate_event_feed.py docs/event-feed/pokequery-events.json
```

### Run Generator Tests
To run the Python test suite:
```bash
python -m unittest scripts/test_generator.py
```

## GitHub Actions Automation

A GitHub Actions workflow [update-event-feed.yml](file:///C:/Temp/PokeQuery-v072-event-guide-feed/.github/workflows/update-event-feed.yml) is set up to automate the feed generation:
- Triggered on manual run (`workflow_dispatch`) and on a recurring schedule (every 12 hours).
- The workflow generates the feed in fixture-mode, runs the validator, and automatically commits any new changes back to the master branch.
