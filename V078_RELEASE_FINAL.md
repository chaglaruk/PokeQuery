# PokeQuery Android 0.7.8 release status

Date: 2026-10-10
Decision: **SOURCE PREPARATION — REVIEW IN PROGRESS**. Initial technical checks passed, but final post-review source/CI and independent merge must be checked afresh.

## Source and merge gate

- Verified merged master: `005a81a9effa8c56720b34dd334d9c9753de1784` (PR #47, merged).
- Release preparation branch: `release/v0.7.8`.
- Initial Android version-bump commit: `d6f0268fbe6a7e560b0161a1c6d01a1b87aac38e` (this **includes** the 0.7.8/code28 change; `005a81a9...` is its 0.7.7/code27 baseline).
- Release PR: https://github.com/chaglaruk/PokeQuery/pull/48
- PR #48 is open for independent review. Its head may advance for review fixes: fetch the current PR HEAD and exact-head CI immediately before merge, rather than using an earlier report SHA as the release source.
- Verified immutable Android v0.7.7 source: `93bf178f942048e0fee8cbb47976b23f86ecde3f` (`v0.7.7`, code 27).
- Web/PWA remains independently versioned at 0.7.3.

## Source change and validation

- Android package remains `com.caglar.pokequery`.
- Android `versionName` is `0.7.8`; `versionCode` is `28`.
- `AppVersion`, the app Changelog, six localized Changelog resource sets, release notes, and listing preparation docs now describe the actual 0.7.8 source candidate.
- Android unit tests and debug assembly passed locally.
- Android release bundle configuration passed locally without signing credentials; this produced an unsigned local AAB only.
- GitHub Android CI run [38006484206](https://github.com/chaglaruk/PokeQuery/actions/runs/38006484206) passed all unit, lint, debug build, golden corpus, Event fallback, generator safety, feed, and runtime asset jobs at **initial version-bump commit** `d6f0268f...`. Later commits require their own exact-head CI.
- GitHub documentation validation run [38006484346](https://github.com/chaglaruk/PokeQuery/actions/runs/38006484346) passed at the initial version-bump commit; newer commits require revalidation.
- CodeRabbit status on PR #48 is SUCCESS.
- Local golden corpus and canonical/Android/Web Event fallback SHA-256 parity passed.
- Local generator safety, enrichment, feed validation, and runtime asset checks passed.
- PWA typecheck, lint, and production build passed. PWA unit tests were not green in this Windows Node 26 environment because the test setup reads `localStorage.clear` when `localStorage` is undefined; this is a pre-existing environment/setup issue and was not changed.
- Local Windows Android lint was blocked by the ignored checkout `local.properties` `PropertyEscape` diagnostic; the exact source passed the GitHub Android lint job.
- Manifest inspection confirms `allowBackup="false"` and only the documented `INTERNET` permission.

## AAB and signing

- Local unsigned configuration-check AAB: `app/build/outputs/bundle/release/app-release.aab`.
- Local AAB SHA-256: `F92590CCDE17C5F2A6C8C6ACAC33C6D3C1EDCBA3A4D6670DE70DA546D45D8808`.
- No signing secret, keystore, password, or private signing value was read or logged.
- This AAB is not a release deliverable: it has no approved upload-key or Play App Signing verification.
- `bundletool` was not available in the local environment, so bundletool manifest verification and signer verification remain pending.

## Play Console and Pixel gates

- Play Console access/signing state was not verified in this task.
- No Play Console draft release was created or uploaded.
- No rollout, review submission, publication, or release tag was performed.
- Pixel F1-F5 evidence supplied before this task is pre-release QA evidence; no 0.7.8 Play-signed production APK was installed for this release gate.
- Play-installed acceptance, split-APK signer verification, and release-package Pixel smoke testing remain pending.

## Store visual recovery

- The bounded search of the known PokeQuery Desktop QA/evidence locations did not recover the original 24 approved store-image bytes or a complete prior growth ZIP/manifest pair.
- The prior 24/30 acceptance is not re-claimed as locally hash-verified.
- Six Event images remain rejected/pending.
- Current status: **24/30 previous evidence only; 30/30 new release QA NOT DONE**.
- The required 30-cell recapture plan remains: EN/TR/DE/ES/FR/IT, real release UI, current truthful Event Guide data, 1080x1920 RGB PNG output, source provenance, SHA-256 manifest, ZIP integrity, and complete QA before any production rollout.

## Post-review corrections

- Independent review identified five P2 issues: baseline SHA mislabeled as 0.7.8 source, v0.7.7 history provenance overwritten, stale release-plan status, archived 0.7.7 locale copy falling back to English, and missing PR #43–#47 scope in release plan.
- The release branch now preserves `what_changed_v077_*` translations for archived 0.7.7 Changelog entries and tests that the archived resources remain in all six locales. Release documentation distinguishes the **0.7.7 baseline**, **first version-bump SHA**, and **final merged release SHA** instead of hardcoding a moving candidate HEAD.
- These fixes require new final-head CI and independent verification before merge. Do not mark review comments resolved until their exact corrective changes have been inspected.

## Protection and remaining blockers

- Original dirty checkout, IDE files, unrelated worktrees, and local signing material were preserved.
- No production rollout is allowed until PR #48 is independently merged, terminal CI is rechecked on the merged source, Play signing/version state is verified, a correctly signed AAB is built and inspected, Play draft upload is accepted, and Play-installed Pixel evidence is obtained.
- Store visual recovery/recapture and final six-language listing review remain independent release blockers.
