# PokeQuery — Google Play Compliance Audit

**Date:** 2026-06-21 · **Version:** 0.4.1 (versionCode 8) · **Package:** `com.caglar.pokequery`
**Mode:** Analysis only. No source/docs were modified.

---

## 1. Application identity

| Item | Value | Status |
|---|---|---|
| `applicationId` | `com.caglar.pokequery` | ✅ |
| `namespace` | `com.caglar.pokequery` | ✅ matches |
| `versionCode` | 8 | ✅ monotonically increasing (prior: per git log) |
| `versionName` | "0.4.1" | ✅ matches `RELEASE_READINESS.md`, `v0.4.1_release_audit.md` |
| `minSdk` / `targetSdk` / `compileSdk` | 24 / 36 / 36 | ✅ current (target 36 satisfies Play target-API requirements for 2026) |

**No `poke.cglr` package anywhere** — the rename to `com.caglar.pokequery` (commit `c55c689`) is complete and consistent across code, manifest, and release docs (except the stale `app_config_audit.md` — see §6).

---

## 2. Permissions & exported components

- **Permissions requested:** **NONE.** No `INTERNET`, no storage, no location. ✅ Matches `MANIFEST_PERMISSION_AUDIT.md` and `PRIVACY_NOTES.md`.
- **Exported components:** only `MainActivity` (launcher). No services, receivers, or additional exported activities. ✅
- `android:allowBackup="true"` with empty rules — see PRIVACY_SECURITY_AUDIT.md (BUG-012). Not a Play blocker.

---

## 3. Data Safety form accuracy

| Play Data Safety claim | Code reality | Verdict |
|---|---|---|
| "No data collected" | DataStore favorites/history only, local, never transmitted | ✅ Accurate |
| "No network traffic / encryption N/A" | No network code, no analytics/ads SDKs | ✅ Accurate |
| "Data stored locally via DataStore" | Yes | ✅ |

The Data Safety draft (`docs/release/DATA_SAFETY_DRAFT.md`) is **accurate**. No corrections required to the form itself.

---

## 4. Store listing review

Source: `docs/release/STORE_LISTING_DRAFT.md`.

| Claim | Verdict | Note |
|---|---|---|
| "100% offline. Zero permissions. No ads." | ✅ Accurate | |
| "Does not connect to your Pokémon GO account" | ✅ Accurate | |
| "Supports both English and Turkish string output natively." | ⚠️ **Overstatement** | Turkish is enabled but **unverified** vs the live client; `turkish_localization_plan.md` says "Do not enable Turkish runtime mode in this phase." Either remove "natively" / qualify as "beta/community-sourced" or complete verification first. |
| App name "PokeQuery: Safe Search Strings" | ✅ | Descriptive; does not imply affiliation. |
| Use of "Pokémon GO" | ⚠️ | Descriptive/nominative. **No explicit non-affiliation disclaimer present** in the listing or in-app. Add: "Not affiliated with, endorsed by, or sponsored by Nintendo, Niantic, or The Pokémon Company." |

---

## 5. IP / trademark exposure

- **Runtime (in APK):** Abstract Canvas art (mascot silhouette, shield, map lines, generic icons). `IP_ASSET_AUDIT.md` confirms no official Pokémon/Niantic/Nintendo/Game Freak art loads at runtime. ✅
- **Repository (not in APK):** Tracked images under `docs/design/`, `docs/design_targets/`, plus root `reference_full_mockup.png` and two `ChatGPT Image *.png`. **Provenance of these reference images needs verification** — if any depict official IP they should not remain in the public repo even as docs. They do not ship in the APK, so not a Play rejection risk, but a legal-hygiene risk.
- **Launcher icon:** generic magnifier/grid/shield per audit. ✅

**Recommendation:** Verify `docs/design/*` and root reference PNGs' provenance; add the non-affiliation disclaimer to the store listing, About screen, and a "Legal" line.

---

## 6. Release/signing configuration

- Release signing loaded from gitignored `keystore.properties` only when present ✅ (safe pattern).
- `keystore.properties` and `release-keystore.jks` confirmed **not tracked** by git.
- `docs/release/KEYSTORE_LOCAL_SETUP.md` is tracked but contains only placeholder passwords ✅.
- AAB signs when properties present (`bundleRelease`). `AAB_BUILD_REPORT.md` records a signed ~34 MB bundle. ⚠️ Size is high — see BUG-017 (minify off + uncompressed PNGs). Not a Play blocker, but a quality signal.

---

## 7. Documentation inconsistencies (must reconcile)

| Doc | Problem | Action |
|---|---|---|
| `docs/release/app_config_audit.md` | Says versionName `0.1.3`, versionCode `2`; calls the package a "BLOCKER placeholder" while stating it is already `com.caglar.pokequery`. Self-contradictory and stale (predates rename). | Delete or rewrite for 0.4.1. |
| In-app Settings About (`MiscScreens.kt:246`) | Displays `v0.3.4`. | Read version dynamically. |
| `turkish_localization_plan.md` vs code | Plan says don't enable Turkish; code enables it (Auto→Turkish on tr-TR). | Reconcile (see BUG-002/003). |
| `search_language_mapping.md` vs `turkish_localization_plan.md` vs KB | Three different Turkish words for `count`/`traded`. | Verify and lock one. |

---

## 8. Content rating & target audience

- No violence, user-generated content, online interaction, gambling, or ads → "Everyone" is defensible.
- `PLAY_UPLOAD_CHECKLIST.md` checkboxes (Data Safety, Privacy Policy link, Content Rating questionnaire, Target Audience) are **unchecked** — they still need completing in Play Console before production. Not an audit failure, a process reminder.

---

## 9. Closed-testing readiness verdict

**Ready for continued closed testing** with the soft fixes below. **No hard Play Console blockers identified.**

### Strongly recommended before requesting production access
1. Add a non-affiliation disclaimer (Nintendo/Niantic/The Pokémon Company) to the store listing and in-app About.
2. Soften or verify the "Turkish native support" claim (highest reputational risk if a Turkish reviewer tests it).
3. Reconcile/delete `app_config_audit.md`; fix the in-app About version string.
4. (Quality) Enable R8 minify + convert PNGs to WebP to bring AAB size down from ~34 MB.
5. Confirm the 16 store screenshots are recaptured at 0.4.1 (commit `3a03f03` suggests recent; verify currency).

### Optional / lower priority
- Set `allowBackup=false` or add exclusion rules to match "privacy-first" messaging.
- Verify provenance of `docs/design/*` and root reference PNGs.

---

## 10. Play Console blockers

**None (hard).** The app meets the structural requirements: valid/current target SDK, consistent package identity, zero permissions matching an accurate Data Safety form, signed release bundle, no runtime third-party IP, no analytics/ads.

## 11. Privacy/security blockers

**None.** See `PRIVACY_SECURITY_AUDIT.md` for recommendations only.
