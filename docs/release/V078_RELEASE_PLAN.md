# Android v0.7.8 preparation — 2026-10-08

READY BUT NOT PUBLISHED. Intended later values: versionName **0.7.8**, versionCode **28**. They are not set in source. Current Android is 0.7.7/code 27; Web/PWA remains independently versioned at 0.7.3. This document authorizes no merge, tag, signing, upload or production replacement.

## Source and changes

Published Android source: `93bf178f942048e0fee8cbb47976b23f86ecde3f`, the peeled `v0.7.7` tag. Fetched master for this preparation: `cd9778b663c777cb98c6c196f8d87a5314fd696d`. Moving master is not the Android release source.

Merged after the immutable release:

- PR #34/#38/#40: release/growth provenance documentation, including independently verified Play installation. These are evidence corrections, not new Android features.
- PR #35: Compose preview/UI development improvements. Do not advertise previews as a runtime feature.
- PR #36: PWA CI/Playwright workflow separation; independently versioned Web development.
- PR #37: growth/roadmap documentation; no public experiment was started by this preparation.
- PR #41: official-source TCG event feed correction; synchronized canonical, Android and Web fallbacks.
- PR #42: truthful Event Guide bonus cards and complete TCG conditions, localized detail dialogs, German Candy Prep title wrapping and correct relative-day labels. The source is merged; Play 0.7.7 does not contain the new rendering.
- Scheduled feed-only changes are public event data updates, not Android releases.

The new Knowledge Base copy guard is a separate review candidate. Include its release-note sentence only if its PR is reviewed, explicitly authorized for merge, and included in the final release source. The SEO and documentation PRs do not change Android version metadata.

## Proposed What's New — six locales

Use these as short release notes and Play What's New drafts. All are under 500 characters including newlines; publication must match the final approved source. Existing protections do not imply guaranteed safe transfer. A native-speaker review is recommended for DE/ES/FR/IT.

### EN

• Event Guide shows actual bonuses and retailer, purchase and redemption conditions on event cards.
• Event details follow the app language. German Candy Prep titles and relative event-day labels display correctly.
• Searches remain local, with manual review before any action in Pokémon GO.

Conditional addition: Knowledge Base reference templates can no longer be copied as complete searches.

### TR

• Etkinlik Rehberi, gerçek bonusları ve mağaza, satın alma ve kod kullanımı koşullarını etkinlik kartlarında gösterir.
• Etkinlik ayrıntıları uygulama dilini izler. Almanca Şeker Hazırlığı başlığı ve etkinliğe kalan gün etiketleri düzeltildi.
• Aramalar yerel kalır; Pokémon GO'da işlem yapmadan önce sonuçları kendin incelersin.

Conditional addition: Bilgi Bankası'ndaki açıklama şablonları artık tamamlanmış arama olarak kopyalanamaz.

### DE

• Der Event-Guide zeigt tatsächliche Boni sowie Händler-, Kauf- und Einlösebedingungen direkt auf den Event-Karten.
• Event-Details folgen der App-Sprache. Deutsche Titel zur Bonbon-Planung und relative Tagesangaben werden korrekt angezeigt.
• Suchen bleiben lokal. Prüfe die Ergebnisse vor jeder Aktion in Pokémon GO selbst.

Conditional addition: Referenzvorlagen der Wissensdatenbank lassen sich nicht mehr als vollständige Suchen kopieren.

### ES

• La guía muestra los bonus reales y las condiciones de tiendas, compra y canje en las tarjetas de eventos.
• Los detalles siguen el idioma de la app. Se corrigen los títulos alemanes de preparación de caramelos y las etiquetas de días relativos.
• Las búsquedas siguen siendo locales. Revisa los resultados antes de actuar en Pokémon GO.

Conditional addition: Las plantillas de referencia ya no se pueden copiar como búsquedas completas.

### FR

• Le guide affiche les bonus réels et les conditions liées aux magasins, achats et codes sur les cartes d’événements.
• Les détails suivent la langue de l’app. Les titres allemands de préparation des Bonbons et les indications de jours relatifs sont corrigés.
• Les recherches restent locales. Vérifiez les résultats avant toute action dans Pokémon GO.

Conditional addition: Les modèles de référence ne peuvent plus être copiés comme recherches complètes.

### IT

• La guida mostra i bonus reali e le condizioni dei negozi, degli acquisti e dei codici sulle schede degli eventi.
• I dettagli seguono la lingua dell’app. Sono corretti i titoli tedeschi della preparazione delle caramelle e le etichette dei giorni relativi.
• Le ricerche restano locali. Controlla i risultati prima di agire in Pokémon GO.

Conditional addition: I modelli di riferimento non si possono più copiare come ricerche complete.

## Exact release gates — perform only after separate authorization

1. Review and explicitly authorize the relevant PR merges. Fetch again, list all commits since the immutable 0.7.7 source and confirm the intended changes; do not use today's moving master blindly. Feed-only bot commits may advance it.
2. Create a clean isolated release checkout at the selected SHA. Verify origin is chaglaruk/PokeQuery, exact HEAD, clean status and immutable 0.7.7 ancestry. Record the resolved SHA, reviewed PRs and source diff.
3. In a dedicated authorized release change, set only planned Android versionName 0.7.8/versionCode 28, update bundled What's New in six locales and CHANGELOG to the actual release date. Web stays 0.7.3 unless separately gated. Re-review the release change and obtain terminal CI at the final source, including any post-review changes.
4. Run Android unit tests, lint and debug assembly; engine corpus byte identity; canonical/both fallback equality; generator safety, feed validation and runtime-asset checks. Preserve mandatory protection/risk/count policy and no pipe in generated output. If a Web engine is changed, run golden corpus, typecheck, lint, unit tests, production build and relevant mobile/routing/offline Playwright gates as well.
5. Validate Home, two language controls, Search Assistant, goal generation, Risk Warning, Copy/Share, local favorites/history/presets, Knowledge Base and Event Guide. Confirm medium/high-risk flows cannot replace clipboard or open sharing before review. Check EN/TR/DE/ES/FR/IT, German Candy Prep wrapping and Zorua day labels against the actual device date.
6. Test Event Guide both online refresh and offline bundled fallback. Today's PR42 visual evidence covers the corrected bundled fallback in a separate QA package; it does not replace release-source validation or a Play-production capture gate. Do not silently use an old cached feed.
7. Repeat small-device/large-font review on affected screens at normal and enlarged font scales. Record widths/scales and any remaining clipping rather than treating the previous Samsung profile as all accessibility coverage.
8. Generate a signed **release AAB only after signing is separately authorized**. No debug package suffix may remain. Inspect AAB manifest using bundletool: package com.caglar.pokequery, versionName 0.7.8, code 28, non-debuggable, documented INTERNET exception and allowBackup=false. Verify AAB signature with jarsigner/keytool against the approved upload certificate. The upload certificate is for upload, not installed-APK acceptance. Record SHA-256, size, exact build/source/tool versions and delivery-copy hash equality without logging secrets.
9. Create/tag the release only under explicit permission. Any release tag must resolve to the verified final source SHA; never retarget v0.7.7. Recheck CI terminal success at that exact source.
10. Upload/publish only under an explicit Google Play gate. Confirm code 28 is accepted and the intended track/countries/rollout/notes/listing fields are reviewed. Do not assume a successful upload means production is live. Preserve credentials/keystores outside evidence and Git.
11. Once genuinely available in Google Play, update the existing production installation normally, preserving user data unless a new explicit instruction permits otherwise. No uninstall/clear-data instruction is implied by this plan.
12. Verify the installed **base APK and every split APK**: correct package, versionName 0.7.8, versionCode 28, DEBUGGABLE=false, Play installer; run apksigner verify and require Google Play App Signing SHA-256:

   `FE:9C:42:96:61:0D:1C:61:41:9E:54:7A:8E:90:10:05:72:2B:B0:76:6C:CC:40:C7:43:3E:A1:66:7F:7D:1A:2A`

   Reject upload-key/debug signatures. Save sanitized metadata and APK hashes; don't include device IDs or private account data.
13. Complete the production smoke and only then perform the six Event screenshot replacements below. Keep binary-provenance and visual/store gates separate.

## Event regression truth

For the TCG 30th Celebration example, distinguish event dates **27 September–20 October 2026** from eligible gift-card code redemption **27 September–31 October 2026**. Participating US retailers are **Target, GameStop and Best Buy**. Cards must explain eligible purchase/redemption conditions, one redemption per account and the equivalent reward for an avatar-item owner. Actual gameplay includes Mega Gengar Mega Raids and Timed Research. Follow the official Pokémon GO announcement linked in the card. Do not invent 4× XP/Stardust, an extra free Raid Pass, unlimited Remote Raid Passes, Premier Ball or Party Power bonuses.

Verify the [official Pokémon GO announcement](https://pokemongo.com/news/tcg-30th-celebration-event) again at release/capture time. Feed generation and the Android binary are different delivery mechanisms: corrected feed data alone cannot repair the 0.7.7 bonus rendering.

## Final six-screenshot recapture sequence

1. Snapshot the existing growth manifest/hashes first. Verify all **24 PASS** assets plus their raw PNG/XML/JSON companions; do not rename, recompress, restyle or overwrite them. Keep all six rejected Event captures and rejection evidence in history.
2. Require the exact future Play-installed build/signature gate above. A debug or .visualqa capture never counts as a production store replacement.
3. Assess the event at the actual intended publication date. Before 20 October, TCG may be truthful if still active, with US and purchase conditions readable. Its separate code window to 31 October is not evidence that the whole event remains active. After the event expires or when publication would immediately outlive the example, use a fresh officially sourced current event or a genuinely evergreen Event Guide workflow. Do not edit dates in pixels or fabricate an active event.
4. Open Event Guide, use normal Refresh, verify feed/cache/source/date/region/bonuses, then choose EN/TR/DE/ES/FR/IT via App Language. Check language on summary, card conditions and detail heading. Keep Search String Language independent.
5. Capture exactly one new raw real Event screen per language, with full source evidence. If necessary keep supporting raw detail captures for QA; never cover warnings or hide contradictory content to fit the primary picture.
6. Compose only these six slots as 1080×1920 RGB/no-alpha PNGs with appropriate localized promotional headlines. Fit device UI proportionally. No Pokémon artwork, fictional capability, retouched UI text or generated query containing a pipe.
7. Inspect date/source/conditions, clipping, text density, headline fit, contrast, risk warnings and localization. Rebuild all six contact sheets with the **same 24 approved PNG bytes** plus six new Event PNGs.
8. Update RESULT.md, qa-results.json, capture/asset manifests and growth ZIP. Verify every final hash, all archive members and CRC integrity. Report GROWTH ASSET GATE: PASS only when all 30 final cells pass. Until then preserve **24/30 PASS, six rejected**.

## Remaining gates before publication

Explicit PR merge/release/signing/upload/publishing authorization; final version/source/CI and AAB gates; large-font/release-device regression; a genuine Play 0.7.8 installation; six fresh production Event captures and complete 30-cell QA; final publisher/native-language listing review. These are deliberately pending. No Android/Web version was bumped and no signed release was built during preparation.
