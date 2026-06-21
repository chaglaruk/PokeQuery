# PokeQuery — Privacy & Security Audit

**Date:** 2026-06-21 · **Version:** 0.4.1 (versionCode 8) · **Mode:** Analysis only. No files modified.
**Secret-values policy:** This report names file paths and risk only. **No secret values are reproduced.**

---

## 1. Permissions & network

| Area | Finding | Status |
|---|---|---|
| Manifest permissions | **None requested** | ✅ |
| Network code | None (no OkHttp/HttpURLConnection/Retrofit/Firebase/etc.) | ✅ |
| Analytics / crash SDKs | None | ✅ |
| Ads SDKs | None | ✅ |
| Trackers | None | ✅ |
| Logging of user data | None (only UI `Toast` messages) | ✅ |

`PRIVACY_NOTES.md` and `MANIFEST_PERMISSION_AUDIT.md` claims are **accurate**.

---

## 2. Exported components & attack surface

- Only `MainActivity` is exported (launcher intent). ✅
- No deep-link intent filters beyond the internal `start_route` string extra consumed in `MainActivity.onCreate` (used for deterministic screenshot capture; not exported as a URI scheme).
- No services, receivers, or content providers. ✅

---

## 3. Local storage

- **DataStore** (`user_prefs`) via `preferencesDataStore` — app sandbox only. ✅
- `SavedTemplateCodec` uses a `length:value` framing that is robust to delimiter characters (round-trip tested). ✅
- No SQLCipher/Room; no files written outside the sandbox. ✅
- `resetSettings()` preserves favorites/history/firstUse while clearing preferences — sensible. ✅

---

## 4. Clipboard behavior

- Writes generated strings via `LocalClipboardManager.setText` (deprecated API — see housekeeping).
- **Never reads** from the clipboard. ✅ Privacy-safe.

---

## 5. Secret / signing-file scan

| Target | Tracked in git? | Risk | Note |
|---|---|---|---|
| `keystore.properties` | **No** (gitignored, confirmed via `git ls-files`) | Local-only | Contains plaintext store/key passwords. Acceptable for local dev; **rotate if machine is shared**. Values not reproduced here. |
| `release-keystore.jks` | **No** (gitignored, `*.jks` rule) | Local-only | ✅ |
| `*.keystore`, `*.p12`, `*.pem`, `*.key` | Covered by `.gitignore` | — | ✅ |
| `docs/release/KEYSTORE_LOCAL_SETUP.md` | **Yes** | None | Contains only `YOUR_KEYSTORE_PASSWORD` placeholders. ✅ |
| API keys / tokens in code | None present | — | No network layer exists. ✅ |
| Hardcoded passwords in build scripts | None | — | `app/build.gradle.kts` reads from properties file only. ✅ |

**Verdict:** No secrets are committed. Signing secrets are correctly gitignored.

---

## 6. Generated / accidental files in the working tree

| File | Tracked? | Recommendation |
|---|---|---|
| `jarsigner_verify.txt` (226 KB) | **No** (untracked, `??` in `git status`) | Add to `.gitignore` to prevent accidental commit. |
| `local.properties` | No (gitignored) | ✅ |
| `window_dump.xml` (root) | **Yes** | UI automator dump — dev artifact. Untrack. |
| `reference_full_mockup.png` (root) | **Yes** | Verify provenance (IP); untrack if dev-only. |
| `ChatGPT Image *.png` (root, ×2) | **Yes** | Dev artifacts; untrack. |
| `docs/design/*.png`, `docs/design_targets/*.png` | **Yes** | Not in APK; verify IP provenance; otherwise untrack. |

None of the above ship in the APK; they are repo-hygiene items only.

---

## 7. Dependencies

All from official sources (AndroidX, Compose BOM, Kotlin, Navigation3). No third-party analytics/ads/network libraries.

| Dependency | Note |
|---|---|
| `androidx.compose.material:material-icons-extended` | Pulled directly (not in version catalog). Increases footprint; acceptable. |
| `androidx.datastore:datastore-preferences:1.0.0` | Hardcoded version (not in catalog). Minor consistency nit. |
| AGP 9.0.1, Kotlin 2.3.20, Compose BOM 2026.03.01 | Very recent; ensure CI/toolchain matches. |

No known vulnerable/insecure dependencies identified at audit time.

---

## 8. Debug flags & release build behavior

| Flag | Setting | Note |
|---|---|---|
| `debuggable` | Not overridden (release = false) | ✅ |
| `isMinifyEnabled` (release) | **false** | ⚠️ No shrinking/obfuscation. Low security impact for an offline text app, but ships all symbol names and inflates size. Enable R8 + `proguard-android-optimize.txt` before production. |
| `proguardFiles` | Configured (`proguard-android-optimize.txt` + `proguard-rules.pro`) | Ready for when minify is enabled. |
| `buildConfig` | false | Note: reading version at runtime (BUG-008 fix) requires enabling BuildConfig or using `PackageManager`. |

---

## 9. Backup behavior

- `android:allowBackup="true"` with `backup_rules.xml` / `data_extraction_rules.xml` effectively empty (TODO stubs).
- Consequence: DataStore favorites/history are eligible for auto-cloud-backup and device-to-device transfer.
- **Privacy impact:** Low (no sensitive data), but contradicts the "private/offline-first" messaging.
- **Recommendation:** `allowBackup="false"`, or populate `<exclude>` rules for `user_prefs`; remove TODO stubs.

---

## 10. Privacy/security blockers

**None.** The app is genuinely offline, permission-free, analytics-free, and free of tracked secrets. All findings below are recommendations.

## 11. Recommendations (priority order)

1. Enable R8 `isMinifyEnabled = true` for release; convert drawable PNGs to WebP (cuts ~22 MB → a few MB).
2. Set `allowBackup="false"` or add DataStore exclusion rules; delete the TODO backup stubs.
3. Add `jarsigner_verify.txt` to `.gitignore`; untrack `window_dump.xml` and root dev PNGs.
4. Verify IP provenance of `docs/design/*` and root reference images.
5. Replace deprecated `LocalClipboardManager` with `LocalClipboard`; replace deprecated `statusBarColor`.
6. Rotate the keystore password if the dev machine is ever shared/compromised.

---

*Companion: `PLAY_COMPLIANCE_AUDIT.md`, `BUG_REPORT.md`.*
