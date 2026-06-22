# Security Policy

PokeQuery is a privacy-first, offline-only Android utility. This document explains
the project's security posture, what is in scope for reports, and how to disclose
responsibly.

## Privacy-first model

PokeQuery is built to collect **nothing** and talk to **no one**.

| | |
|---|---|
| Network permissions | **None.** The manifest declares zero `<uses-permission>` entries. |
| Accounts | None. No login, no sessions, no account access of any kind. |
| Pokémon GO access | **None.** The app never connects to Pokémon GO, Niantic, or any game service. |
| Analytics / telemetry | None. No crash SDKs, no analytics, no ads. |
| Local storage | Favorites, history, and settings are stored on-device in DataStore. `allowBackup="false"`. |
| Automation | None. The app generates text only; the user copies and pastes manually. |

These properties are enforced by regression tests:

- `ManifestPrivacyRegressionTest` — fails the build if any `<uses-permission>` is added.
- `BuildConfigRegressionTest` — fails the build if network/ads/analytics build flags are introduced.

If a contribution or a built APK ever violates one of these, that is a security issue.

## What is in scope

Please report security issues that affect this repository or the shipped app, for example:

- A path that causes the app to make a network call or load a remote resource.
- Inclusion of a permission, SDK, or dependency that breaks the privacy model above.
- A logic bug that could cause an **unsafe** search string to bypass the Risk Warning
  gate or the Expert Builder linter, potentially exposing protected Pokémon.
- Exposure of secrets, keystores, or signing material in the repo.
- Crashes or state corruption triggered by normal user input.

## What is out of scope

- Pokémon GO itself, Niantic's services, or any third-party game infrastructure —
  PokeQuery does not interact with them.
- "Attacks" that require physical access to an unlocked device.
- Reports asking the project to add network, account, automation, scanning, or OCR
  features. These are **by design** excluded and will not be fixed as security bugs.

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

PokeQuery is in Google Play Closed Testing. Security fixes are targeted at the
**latest** released version (currently **0.5.2**, versionCode 13). Older builds are
not maintained; testers should always be on the newest closed-testing build.

## Responsible use

By using PokeQuery you acknowledge that:

- Generated search strings are **suggestions**, not commands. You are responsible
  for what you do with them in Pokémon GO.
- PokeQuery is not affiliated with Niantic, The Pokémon Company, or Nintendo.
- You will not attempt to add automation, scraping, or account-access features to
  your own builds and represent them as PokeQuery.
