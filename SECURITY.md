# Security Policy

PokeQuery is a privacy-first Android utility. Core search-string generation is local-only; the
Event Guide may use the network to fetch the project's public event-feed JSON. This document
explains the project's security posture, what is in scope for reports, and how to disclose
responsibly.

## Privacy-first model

PokeQuery is built to collect **no personal data** and to avoid account or game-service access.

| | |
|---|---|
| Network permission | The Android manifest declares `android.permission.INTERNET` for Event Guide public-feed retrieval. |
| Event Guide network use | May fetch public PokeQuery event-feed JSON. No Pokémon GO account or Niantic API access is used. |
| Accounts | None. No login, no sessions, no account access of any kind. |
| Pokémon GO access | **None.** The app never signs in to Pokémon GO, reads a player's collection, or connects to a Pokémon GO account. |
| Analytics / telemetry | None. No crash SDKs, analytics, ads, or tracking identifiers. |
| Local storage | Favorites, history, and settings are stored on-device in DataStore. `allowBackup="false"`. |
| Automation | None. The app generates text only; the user copies and pastes manually. |

These properties are protected by regression tests and review rules. Network access must remain
limited to explicitly documented public-data features; adding account access, telemetry, ads,
tracking, or undisclosed remote calls is a security/privacy-sensitive change.

## What is in scope

Please report security issues that affect this repository or the shipped app, for example:

- An unexpected or undisclosed network call outside the documented Event Guide public-feed path.
- Inclusion of an account-access, tracking, analytics, advertising, or other privacy-sensitive SDK.
- A logic bug that could cause an **unsafe** search string to bypass the Risk Warning
  gate or the Expert Builder linter, potentially exposing protected Pokémon.
- Exposure of secrets, keystores, or signing material in the repo.
- Crashes or state corruption triggered by normal user input.

## What is out of scope

- Pokémon GO itself, Niantic's services, or third-party game infrastructure — PokeQuery does not
  authenticate to or control those services.
- The public Event Guide feed being temporarily unavailable, provided the app fails safely and
  does not expose private data.
- "Attacks" that require physical access to an unlocked device.
- Requests to add account automation, scanning, OCR, scraping, or Pokémon GO credential access;
  those capabilities are intentionally excluded from the product.

## Reporting a vulnerability

Please report responsibly:

1. **Do not** open a public GitHub issue for a security vulnerability.
2. Email the maintainer via the address listed on the
   [GitHub profile](https://github.com/chaglaruk), or open a **private** security
   advisory on GitHub (`Security → Advisories → Report a vulnerability`).
3. Include:
   - PokeQuery version (from **Settings → About**)
   - Android version and device
   - Steps to reproduce
   - The impact you observed
4. Please **do not** include Pokémon GO account credentials, session tokens, or
   screenshots of private Pokémon collections.

We will acknowledge receipt as soon as possible and coordinate a fix and disclosure
timeline with you.

## Secrets and signing

- **Keystores, passwords, and private keys are never committed.** Release signing
  relies on a local `keystore.properties` + `release-keystore.jks` that are not in
  this repository and are covered by `.gitignore`.
- If you find **any** private key, keystore, or password committed to this repo,
  treat it as a critical security issue and report it privately immediately.
- Contributors must never paste real credentials, API keys, or signing material into
  issues, PRs, commits, or screenshots.

## Supported versions

Security fixes are targeted at the latest released Android version, currently **0.7.4**
(versionCode **24**). Older builds are not maintained; testers should use the newest available
closed-testing build.

## Responsible use

By using PokeQuery you acknowledge that:

- Generated search strings are **suggestions**, not commands. You are responsible
  for what you do with them in Pokémon GO.
- PokeQuery is not affiliated with Niantic, The Pokémon Company, or Nintendo.
- You will not attempt to add automation, scraping, or account-access features to
  your own builds and represent them as PokeQuery.
