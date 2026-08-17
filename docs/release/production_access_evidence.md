# PokeQuery — Google Play Production Access Evidence

**Package:** `com.caglar.pokequery`  
**Source status:** post-v0.7.4 master, before the next Android release/version bump  
**Purpose:** Current evidence pack for closed testing and the next Google Play production-access request.

---

## 1. What PokeQuery does

PokeQuery is a copy-only Pokémon GO search-string utility. It generates plain-text search strings for storage-review goals such as Safe Cleanup, Candy Prep, Trade Fodder, Lucky Trade, PvP candidates, Hundo/Nundo inspection, Untagged cleanup, Expert Builder, presets, and Event Guide preparation.

The user manually reviews, copies, and pastes a generated string into Pokémon GO. PokeQuery does not perform transfers, trades, taps, OCR, or other gameplay actions.

## 2. Privacy and network model

- **No Pokémon GO account access.** PokeQuery never logs into, authenticates with, or reads a Pokémon GO account or private game API.
- **No ads, analytics, or tracking.** No advertising, analytics, tracking, or crash-reporting SDK is included.
- **Core search generation is local.** Search generation, favorites, history, presets, and settings do not require a network connection.
- **Narrow Android Internet use.** The manifest declares `android.permission.INTERNET` so the Event Guide can retrieve a public read-only JSON feed from GitHub over HTTPS. The app caches that feed and has a bundled fallback.
- **No intentional user-data payload in Event Guide requests.** PokeQuery does not include Pokémon GO account data, local search history, favorites, presets, or a PokeQuery account identifier. Standard HTTP metadata such as IP address and User-Agent may be processed by GitHub/network providers when serving the feed.
- **Local backup disabled.** `android:allowBackup="false"` is set and backup/data-extraction rules provide defense in depth.

## 3. Search-safety controls

- Generated PokeQuery output never emits the `|` operator.
- Count/cleanup flows require conservative protection exclusions.
- `!traded` and other protected-category semantics are guarded by deterministic tests.
- Expert Builder copy is blocked on error-level linter findings.
- Medium/high-risk action-adjacent flows require a review warning before copy.
- App Language and Search String Language are separate so UI translation does not silently change search syntax.

## 4. Closed-testing flow

1. Tester joins the Google Play closed-test group/opt-in page.
2. Tester installs the current closed-test build.
3. Tester uses the app normally and verifies generated strings in Pokémon GO before acting.
4. Tester reports issues through **Settings → Send tester feedback**. This launches the tester's own email app using an `ACTION_SENDTO` `mailto:` intent; PokeQuery has no feedback backend and sends nothing automatically.
5. Feedback is reviewed and acted on through app updates before another production-access request.

## 5. Evidence of iteration from testing/audits

Recent corrective work includes:

- Android 11+ mail-app package-visibility support for tester feedback.
- Official Help Center reconciliation of localized search tokens for EN/TR/DE/ES/FR/IT, with BETA vs VERIFIED confidence semantics kept distinct.
- Web onboarding removal while Android onboarding remains intentional.
- Public privacy policy plus in-app Android/Web access.
- Canonical Event Guide/fallback freshness validation and platform-parity checks.
- Parser/token-boundary, localized protection-dedup, linter, widget, and offline-fallback hardening from pre-AAB independent audits.

## 6. Data Safety summary

- **Required user-data types collected/shared:** No.
- **Data collection required:** Not applicable.
- **Data encrypted in transit:** Yes — public Event Guide retrieval uses HTTPS/TLS.
- **Advertising ID:** Not used; no advertising SDK.
- **Local app data:** settings/favorites/history/presets remain local and can be deleted by clearing app storage or uninstalling.

## 7. Non-affiliation disclaimer

> PokeQuery is an independent helper app and is not affiliated with, endorsed by, or sponsored by Niantic, The Pokémon Company, Nintendo, or Game Freak.

The name “Pokémon GO” is used descriptively to explain compatibility with the game's user-operated search field.

## 8. Runtime visual-asset policy

Release runtime assets must be app-owned/generic UI artwork. Pokémon character sprites previously used for internal Event Guide testing are replaced before the next production candidate; see `IP_ASSET_AUDIT.md`.

## 9. Production-access readiness evidence

Before a new production-access request, the release candidate must have:

- a new, unique Android `versionCode`;
- green Android unit/lint/safety/feed/runtime-asset validation;
- green Web/PWA validation for shared engine/parity changes;
- physical-device smoke testing of copy, Search Assistant, Event Guide, shortcuts, and widgets;
- a signed AAB verified with the existing Play upload signing identity;
- Play Console Data Safety/store text matching the implementation described above;
- documented tester engagement and feedback acted on during the required closed-testing period.
