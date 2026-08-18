# PokeQuery Event Feed Pipeline

`docs/event-feed/pokequery-events.json` is the canonical PokeQuery-owned public static Event Guide feed.

Android fetches the public feed via HTTPS, validates/caches it locally and falls back to `app/src/main/res/raw/event_context_fallback.json` if needed. Web/PWA uses `web/public/event-feed-fallback.json` as its bundled fallback.

The three feed files are kept synchronized by repository validation and the scheduled update workflow.

## Files

- Generator: `scripts/generate_event_feed.py`
- Source-page enrichment: `scripts/enrich_event_feed.py`
- Generator tests: `scripts/test_generator.py`
- Enrichment tests: `scripts/test_enrich_event_feed.py`
- Validator: `scripts/validate_event_feed.py`
- Source configuration: `docs/event-feed/sources.json`
- Manual metadata/override layer: `docs/event-feed/event_metadata.json`
- Canonical feed: `docs/event-feed/pokequery-events.json`
- Android bundled fallback: `app/src/main/res/raw/event_context_fallback.json`
- Web bundled fallback: `web/public/event-feed-fallback.json`

## Source policy

Configured sources currently include official Pokémon GO Live material and Leek Duck.

Rules:

1. Prefer official Pokémon GO Live information when available.
2. Third-party sources are discovery/enrichment/fallback evidence, not permission to fabricate data.
3. Date/status truth is mandatory.
4. Unknown Pokémon, bonuses, raids, research, rewards or dates remain unknown.
5. Runtime Android/Web code does not scrape Pokémon GO or authenticate to a game account.

## Pipeline

The current production pipeline has two distinct stages.

### 1. Discovery / base feed

```bash
python scripts/generate_event_feed.py
```

The generator discovers event candidates from configured public sources, normalizes/de-duplicates them and applies curated metadata where available.

`event_metadata.json` is a manual override/exception layer, not the only source of rich event content.

### 2. Source-page enrichment

```bash
python scripts/enrich_event_feed.py docs/event-feed/pokequery-events.json --strict
```

The enrichment pass fetches event source pages and fills missing/generic detail fields from source content. It also normalizes recurring title artifacts and excludes source-site navigation/chrome from event detail extraction.

`--strict` enforces content-quality requirements for CURRENT/UPCOMING gameplay events. A feed where active events collapse to generic “details are limited” placeholders must not silently publish.

### 3. Validation

```bash
python scripts/validate_event_feed.py docs/event-feed/pokequery-events.json
```

Validation checks schema/safety/date/content constraints.

### 4. Fallback synchronization

After successful enrichment/validation, copy the canonical feed byte-for-byte to both runtime fallbacks:

```bash
cp docs/event-feed/pokequery-events.json app/src/main/res/raw/event_context_fallback.json
cp docs/event-feed/pokequery-events.json web/public/event-feed-fallback.json
```

Repository CI checks these relationships.

## Local tests

Run deterministic tests without relying on a live-source refresh:

```bash
python scripts/test_generator_safety.py
python -m unittest scripts.test_generator scripts.test_enrich_event_feed scripts.test_check_web_fallback_fresh
python scripts/validate_event_feed.py docs/event-feed/pokequery-events.json
```

The generator also supports fixture-mode for local/offline generator work:

```bash
python scripts/generate_event_feed.py --fixture-mode
```

Fixture-mode is a test/development option. It is **not** what the scheduled production workflow uses.

## GitHub Actions automation

`.github/workflows/update-event-feed.yml` runs:

- every 12 hours; and
- via manual `workflow_dispatch`.

Current workflow order:

1. test Event Guide enrichment;
2. run the online generator;
3. enrich source pages with `--strict`;
4. validate the canonical feed;
5. synchronize Android and Web fallbacks;
6. stage exactly the three feed files;
7. commit/push to `master` only if content changed.

The bot commit is a **feed-data update**, not a new Android release. A newer `master` SHA caused only by this workflow must never be described as the immutable source of the latest published Android binary.

## Review expectations

Before accepting Event Guide data/pipeline changes, verify:

- dates and CURRENT/UPCOMING/ENDED state;
- source attribution and preference for official sources;
- event-specific content rather than generic placeholders;
- no source-navigation/footer leakage;
- normalized human-facing titles;
- no fabricated details;
- canonical/Android/Web fallback freshness;
- no generated PokeQuery search string containing `|`.
