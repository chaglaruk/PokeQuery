# PokeQuery Event Feed

`pokequery-events.json` is a PokeQuery-owned public JSON feed. The app may refresh this URL, validate it, cache it, and fall back to the bundled raw JSON if refresh fails.

## How to add/update events
- PokeQuery developers must manually update the `pokequery-events.json` file.
- The JSON file is then published to a public hosting URL (e.g. GitHub Pages or a CDN) that the PokeQuery Android app consumes.
- Only real, confirmed events should be added to the live feed.
- If live feed publishing is not ready, the app falls back to the bundled `event_context_fixture.json` and labels that state honestly in the UI.

Rules:

- Do not scrape event sites from the app.
- Do not use Pokémon GO login, private APIs, OCR, camera, or analytics.
- Keep `suggestedSearch` free of the vertical-bar OR operator.
- Use localized event fields with `Tr`, `De`, `Es`, `Fr`, and `It` suffixes when visible in the UI.
- Use `pokemon[]` for template rows: `name`, localized `source*`, `shinyAvailable`, localized `note*`, localized `badges*`, `spriteKey`.
- If exact event tables are not confirmed, say so in the event copy and use `check-in-game` sources.
