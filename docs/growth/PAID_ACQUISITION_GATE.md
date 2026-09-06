# PokeQuery paid acquisition gate

Prepared: 2026-09-06

## Decision

Do not spend on paid acquisition yet.

PokeQuery has no in-app analytics or attribution SDK by design. Paid traffic can still be evaluated with Google Play aggregate acquisition/store-listing metrics, but buying traffic before the store message and organic positioning are proven would mostly pay to learn basic copy lessons that can be learned for free.

## Conditions before the first paid test

All must be true:

1. Outcome-first Play Store screenshots and localized listing copy are live.
2. At least one store-listing experiment has reached a stable result or enough traffic to reject a clearly weaker variant.
3. At least one organic message has demonstrated real interest through community response plus a visible Play Store acquisition lift.
4. The app's share loop is live and validated on a physical Android device.
5. Rating prompt behavior is validated and does not interrupt first-use or risk-review flows.
6. No growth change has introduced analytics, tracking, ad SDKs or unrelated networking.

## First paid test when the gate opens

Budget cap: **£50 total**, not a recurring campaign.

Audience/message: people searching for a concrete Pokémon GO storage/search problem, not broad Pokémon interests.

Creative hypothesis A:
`Storage full? Build a safer cleanup search in seconds.`

Creative hypothesis B:
`Stop memorising Pokémon GO search strings.`

Landing target: a matching Google Play custom store listing, not the generic listing.

Primary success signal: Google Play's own unique acquisition/install-click metrics and resulting installs. Do not add attribution SDKs just to optimize a £50 test.

## Stop rules

Stop rather than increase spend if:
- paid traffic does not outperform the baseline store conversion directionally;
- the creative gets clicks but weak install intent;
- users misunderstand PokeQuery as a scanner, account-connected tool or automation app;
- a campaign depends on misleading Pokémon imagery or official assets;
- measurement would require breaking the no-tracking product boundary.

## What to optimize first

1. Store screenshots.
2. Store title/short description.
3. Community positioning.
4. Share loop.
5. SEO landing pages.
6. Only then paid traffic.
