# Security Policy

PokeQuery is a privacy-first companion utility. Core search-string generation is local-only; the Event Guide may use the network to fetch the project's public static event-feed JSON. This document describes the current security/privacy boundary and responsible disclosure process.

## Privacy-first model

PokeQuery is designed to collect no personal data and to avoid Pokémon GO account/service access.

| | |
|---|---|
| Network permission | Android declares `android.permission.INTERNET` for the documented public Event Guide feed path. |
| Event Guide network use | May fetch PokeQuery's public static feed. Feed generation may research configured public event sources. Build-time enrichment is restricted to approved HTTPS Pokémon GO/Leek Duck hosts and validates redirects/final destinations. No Pokémon GO account/private API access is used. |
| Accounts | None. No login, sessions or cloud user account. |
| Pokémon GO access | None. The app never signs in to Pokémon GO, reads a player's collection or controls the game. |
| Analytics / telemetry | None. No crash SDKs, analytics, ads, tracking identifiers or remote profiling. |
| Local storage | Preferences/history/content used by the app are stored locally. `allowBackup="false"`. |
| Automation | None. PokeQuery generates text; the user decides whether to copy/paste/use it. |

Network access must remain limited to explicitly documented public-data features. Adding account access, telemetry, ads, tracking or unrelated remote calls is a security/privacy-sensitive product change.

## What is in scope

Please report security issues affecting this repository or shipped PokeQuery surfaces, for example:

- an unexpected or undisclosed network call outside documented public-data paths;
- Event Guide source/redirect validation that permits an unapproved host, insecure scheme, embedded credentials or non-standard port;
- inclusion of account-access, tracking, analytics, advertising or other privacy-sensitive SDKs;
- a search/linter/risk bug that allows an unsafe generated string or risky copy path to bypass established safety gates;
- exposure of secrets, keystores or signing material;
- malicious or malformed Event Guide data causing unsafe behavior, code execution or persistent corruption;
- crashes/state corruption triggered by normal user input.

## What is out of scope

- Pokémon GO, Niantic services or third-party infrastructure that PokeQuery does not control;
- temporary unavailability of the public Event Guide feed when the app fails safely to cache/bundled fallback;
- attacks requiring physical access to an unlocked device unless they expose a PokeQuery-specific vulnerability;
- requests to add Pokémon GO account automation, OCR/screen reading or credential access; those capabilities are intentionally outside the product boundary.

## Reporting a vulnerability

Do not open a public GitHub issue for a security vulnerability.

Use either:
1. the maintainer contact listed on the GitHub profile; or
2. a private GitHub Security Advisory (`Security → Advisories → Report a vulnerability`).

Include the affected PokeQuery version/ref, platform/device, reproduction steps and observed impact. Do not include Pokémon GO credentials, session tokens or private collection data.

## Secrets and signing

- Keystores, passwords and private keys must never be committed.
- Release signing relies on local signing material excluded from the repository.
- Never paste real credentials, API keys or signing material into issues, PRs, commits or screenshots.
- If signing/private material is found in Git history, treat it as a critical issue and rotate/revoke it as appropriate.

## Supported versions and release identity

Security fixes target the latest released Android build, currently **v0.7.5 / versionCode 25**.

Immutable Android v0.7.5 release source SHA:
`b19c3b150468318a71da6c4763266cf4aba10cdd`

Web/PWA is independently versioned (currently v0.7.3).

Do not infer released Android source from moving `master`: the scheduled Event Guide workflow can create feed-only commits after a binary release. Release claims must use the immutable tag/release SHA.

## Responsible use

- Generated search strings are suggestions, not commands.
- PokeQuery is not affiliated with Niantic, The Pokémon Company or Nintendo.
- PokeQuery's generated-output policy intentionally forbids `|` even if external Pokémon GO documentation accepts it.
