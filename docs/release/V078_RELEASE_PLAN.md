# Android v0.7.8 release gates — 2026-10-10

**CODE29 REVISION PREPARED, NOT PUBLISHED.** Signed 0.7.8/code28 was accepted into a Play Console production **draft**, but no review submission or rollout occurred. Event Guide correctness/accessibility PR #49 merged at `64ba28687efe5fdd9a8bf71af2f625f949e91b6b`; the replacement Android **0.7.8/code29** candidate is on `release/v0.7.8-code29`. Published Android is still **0.7.7/code27**; Web/PWA remains **0.7.3**. Final source/CI, signing, Console version-code availability and draft replacement are separate gates.

## Source and changes

Published Android source: `93bf178f942048e0fee8cbb47976b23f86ecde3f` (peeled `v0.7.7`). The first 0.7.8/code28 version-bump source was `d6f0268fbe6a7e560b0161a1c6d01a1b87aac38e`; the **signed code28 draft** was built from `57b4f8d8d50630af6632ef5730f20978c18ba948`. The **code29 replacement baseline** is merged PR #49 SHA `64ba28687efe5fdd9a8bf71af2f625f949e91b6b`. Pin the final reviewed/merged code29 SHA (not the moving master) before signing.

Merged after the immutable release:

- PR #34/#38/#40: release/growth provenance documentation, including independently verified Play installation. These are evidence corrections, not new Android features.
- PR #35: Compose preview/UI development improvements. Do not advertise previews as a runtime feature.
- PR #36: PWA CI/Playwright workflow separation; independently versioned Web development.
- PR #37: growth/roadmap documentation; no public experiment was started by this preparation.
- PR #41: official-source TCG event feed correction; synchronized canonical, Android and Web fallbacks.
- PR #42: truthful Event Guide bonus cards and complete TCG conditions, localized detail dialogs, German Candy Prep title wrapping and correct relative-day labels. The source is merged; Play 0.7.7 does not contain the new rendering.
- PR #43: Knowledge Base reference copying guard; released query output never includes unverified reference placeholders or pipes.
- PR #44: Web/PWA SEO and mobile navigation improvements; Android/Web release versions remain separate.
- PR #45: pre-release documentation and six-language Play listing preparation, not Android runtime functionality.
- PR #46: verified Halloween Part II vs GO Pass source-date/feed classification corrections.
- PR #47: Event Guide editorial-news classification and timing, localized status, copy/risk/readability fixes, four local text sizes, equal bottom-nav icon alignment and accessible Settings targets.
- PR #49: localized neutral Featured badges remove event-independent costume claims; checked Event Guide checklist text has higher contrast.
- Scheduled feed-only changes are public event data updates, not Android releases.

The Knowledge Base guard **is included** in the selected release baseline. Do not describe it as conditional. Web/SEO changes do not imply an Android or Web version bump on their own.

## Final six-language What's New drafts

Use [the dedicated 0.7.8 release-note draft](V078_WHATS_NEW_DRAFT.md) for EN/TR/DE/ES/FR/IT; the initial proposal was superseded after PRs #43–#49 merged. The complete release note now includes Knowledge Base copy guards, optional text sizing and editorial Event Guide date handling. The publisher must confirm final Play Console locale previews. Never claim automatic Pokémon GO actions or guaranteed transfer safety.

## Exact release gates — perform only after separate authorization

1. Review and explicitly authorize the relevant PR merges. Fetch again, list all commits since the immutable 0.7.7 source and confirm the intended changes; do not use today's moving master blindly. Feed-only bot commits may advance it.
2. Create a clean isolated release checkout at the selected SHA. Verify origin is chaglaruk/PokeQuery, exact HEAD, clean status and immutable 0.7.7 ancestry. Record the resolved SHA, reviewed PRs and source diff.
3. In the new reviewed release PR, keep Android versionName **0.7.8** and increase versionCode from saved-draft 28 to **29**. Confirm that Play Console accepts 29. Use the updated six-language What's New draft and CHANGELOG; Web stays independently **0.7.3**. Require terminal CI at the final source.
4. Run Android unit tests, lint and debug assembly; engine corpus byte identity; canonical/both fallback equality; generator safety, feed validation and runtime-asset checks. Preserve mandatory protection/risk/count policy and no pipe in generated output. If a Web engine is changed, run golden corpus, typecheck, lint, unit tests, production build and relevant mobile/routing/offline Playwright gates as well.
5. Validate Home, two language controls, Search Assistant, goal generation, Risk Warning, Copy/Share, local favorites/history/presets, Knowledge Base and Event Guide. Confirm medium/high-risk flows cannot replace clipboard or open sharing before review. Check EN/TR/DE/ES/FR/IT, German Candy Prep wrapping and Zorua day labels against the actual device date.
6. Test Event Guide both online refresh and offline bundled fallback. Today's PR42 visual evidence covers the corrected bundled fallback in a separate QA package; it does not replace release-source validation or a Play-production capture gate. Do not silently use an old cached feed.
7. Repeat small-device/large-font review on affected screens at normal and enlarged font scales. Record widths/scales and any remaining clipping rather than treating the previous Samsung profile as all accessibility coverage.
8. Generate a signed **release AAB only after signing is separately authorized**. No debug package suffix may remain. Inspect AAB manifest using bundletool: package com.caglar.pokequery, versionName 0.7.8, **code 29**, non-debuggable, documented INTERNET exception and allowBackup=false. Verify AAB signature with jarsigner/keytool against the approved upload certificate. The upload certificate is for upload, not installed-APK acceptance. Record SHA-256, size, exact build/source/tool versions and delivery-copy hash equality without logging secrets.
9. Create/tag the release only under explicit permission. Any release tag must resolve to the verified final source SHA; never retarget v0.7.7. Recheck CI terminal success at that exact source.
10. Upload/publish only under an explicit Google Play gate. Confirm code **29** is available and accepted, and that the already saved **code28** draft is superseded safely in the same production track without touching the current live 0.7.7 release; review intended track/countries/notes/listing fields. Do not assume a successful upload means production is live. Preserve credentials/keystores outside evidence and Git.
11. Once genuinely available in Google Play, update the existing production installation normally, preserving user data unless a new explicit instruction permits otherwise. No uninstall/clear-data instruction is implied by this plan.
12. Verify the installed **base APK and every split APK**: correct package, versionName 0.7.8, versionCode **29**, DEBUGGABLE=false, Play installer; run apksigner verify and require Google Play App Signing SHA-256:

   `FE:9C:42:96:61:0D:1C:61:41:9E:54:7A:8E:90:10:05:72:2B:B0:76:6C:CC:40:C7:43:3E:A1:66:7F:7D:1A:2A`

   Reject upload-key/debug signatures. Save sanitized metadata and APK hashes; don't include device IDs or private account data.
13. Complete the production smoke and only then perform the six Event screenshot replacements below. Keep binary-provenance and visual/store gates separate.

## Event regression truth

For the TCG 30th Celebration example, distinguish event dates **27 September–20 October 2026** from eligible gift-card code redemption **27 September–31 October 2026**. Participating US retailers are **Target, GameStop and Best Buy**. Cards must explain eligible purchase/redemption conditions, one redemption per account and the equivalent reward for an avatar-item owner. Actual gameplay includes Mega Gengar Mega Raids and Timed Research. Follow the official Pokémon GO announcement linked in the card. Do not invent 4× XP/Stardust, an extra free Raid Pass, unlimited Remote Raid Passes, Premier Ball or Party Power bonuses.

Verify the [official Pokémon GO announcement](https://pokemongo.com/news/tcg-30th-celebration-event) again at release/capture time. Feed generation and the Android binary are different delivery mechanisms: corrected feed data alone cannot repair the 0.7.7 bonus rendering.

## Conditional store screenshot correction (only if the current Play listing is inaccurate)

1. Search only bounded known evidence/backups and existing Play Console listing images for the previously approved 24 assets. Their local approved bytes/manifest were **not recovered as of 2026-10-10**; do not claim hash verification. If recovered, verify against the original manifest; otherwise recapture and review all 30 cells. Retain rejection history for the six former Event examples.
2. Require the exact future Play-installed build/signature gate above. A debug or .visualqa capture never counts as a production store replacement.
3. Assess the event at the actual intended publication date. Before 20 October, TCG may be truthful if still active, with US and purchase conditions readable. Its separate code window to 31 October is not evidence that the whole event remains active. After the event expires or when publication would immediately outlive the example, use a fresh officially sourced current event or a genuinely evergreen Event Guide workflow. Do not edit dates in pixels or fabricate an active event.
4. Open Event Guide, use normal Refresh, verify feed/cache/source/date/region/bonuses, then choose EN/TR/DE/ES/FR/IT via App Language. Check language on summary, card conditions and detail heading. Keep Search String Language independent.
5. Capture exactly one new raw real Event screen per language, with full source evidence. If necessary keep supporting raw detail captures for QA; never cover warnings or hide contradictory content to fit the primary picture.
6. Compose only these six slots as 1080×1920 RGB/no-alpha PNGs with appropriate localized promotional headlines. Fit device UI proportionally. No Pokémon artwork, fictional capability, retouched UI text or generated query containing a pipe.
7. Inspect date/source/conditions, clipping, text density, headline fit, contrast, risk warnings and localization. Rebuild all six language contact sheets using verified restored images if available; otherwise include all 30 newly recaptured and reviewed cells. Never substitute unverified bytes for the lost 24 approved originals.
8. Update RESULT.md, qa-results.json, capture/asset manifests and growth ZIP. Verify every final hash, all archive members and CRC integrity. Report GROWTH ASSET GATE: PASS only when all 30 final cells pass. Until then report **historical 24/30 evidence only, zero locally reverified originals, six formerly rejected Event slots**.

## Remaining gates before publication

The **signed 0.7.8/code28** AAB and existing draft are already verified by previous device-agent evidence, but were **not published**. The **0.7.8/code29** replacement source is being reviewed. Remaining: independent release-PR merge and exact-head CI; repeat signed build/bundletool/certificate checks for **code29**; verify its new Play Console draft and native-language listing/policy fields; perform release-behavior offline cache/fallback test plus targeted Event Guide badge/contrast physical QA; verify current live Play listing visuals and update **only if genuinely inaccurate**. Lost local screenshot archives alone do not require recapturing the whole 30-cell pack. No rollout or tag without an explicit final publication gate.
