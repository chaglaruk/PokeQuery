# PokeQuery organic growth execution checkpoint

**Reviewed:** 2026-10-07  
**Release boundary:** Android v0.7.7 / code 27 (published, immutable source `93bf178f942048e0fee8cbb47976b23f86ecde3f`); Web/PWA v0.7.3 independently versioned.  
**Scope:** store conversion, honest discovery, screenshots, and community feedback. No app SDKs, attribution, or public posting without review.

## What is verified vs. not yet verified

| Surface | Established evidence | Still needs verification |
|---|---|---|
| Android production | v0.7.7 published and Play-installed smoke passed | No new Android binary needed for listing-only work |
| Google Play listing | Existing six-locale title/short/full description proposals are in `PLAY_STORE_LISTING_PACK.md`; names 22–28 characters, short descriptions 72–78, full descriptions 962–1,139: all within Play's 30/80/4,000 limits | **Current live Play Console** listing fields, locales and screenshot order. Public search indexing can lag and is not Console truth |
| Visual assets | Earlier README/store screenshots exist in the repository, but many are historic captures and some show older layouts | Real installed v0.7.7 screenshots of all five chosen screen states, plus every supported locale and final overflow/IP review |
| Store performance | Play Console has aggregate store-listing metrics without an app-side tracking SDK | Baseline visitors, acquisitions, conversion, search terms and top languages for a comparable 28-day window; no figures have been supplied |
| Organic community | Four distinct Reddit developer/builder posts were reported published on 2026-09-06 | Their actual reply outcomes; status of previous r/pokemongo and r/TheSilphRoad moderator messages |
| Web/PWA | Version v0.7.3 and four static SEO guides are published via GitHub Pages workflow; routine Web tests are green | Independently open the deployed pages and check mobile sharing/indexing before calling a new SEO experiment effective |

## Next campaign: improve conversion before buying traffic

### Phase A — read-only store baseline

Before uploading anything, capture the **current** Play Console Main store listing for EN/TR/DE/ES/FR/IT, including title, short description, full description, feature graphic and ordered screenshots. Record which locales have a manually localized store listing rather than automatic translation.

Record aggregate Play Console `Store performance` for the previous completed 28 days and a comparison period: store-listing visitors, acquisitions and the Console's conversion rate. Break down by traffic source, country, language and available search terms. Small sample sizes should be reported as inconclusive, not evidence of a winner.

Do not assume Google search-result snippets are real-time publication state. Do not add analytics, tracking parameters or third-party attribution to the app.

### Phase B — capture a truthful v0.7.7 screenshot story

**Priority:** one complete EN baseline, then localized variants after the capture/layout gate. Reuse screenshots only when their underlying UI content actually matches the chosen language.

Each of the following five slots needs a real, non-synthetic Android app capture from the installed v0.7.7 build, taken at a repeatable clean state:

| Slot | Screen | What must be visibly true |
|---|---|---|
| 1 | Safe Cleanup goal/result | Exact query is legible, exclusions/protections visible; do not suggest every match is safe to transfer |
| 2 | Home or Search Assistant | A supported goal or query-to-search result, no fake AI capabilities or Pokémon GO account connection |
| 3 | Event Guide | An actually current or upcoming event with truthful status/date at capture time; if feed is stale, capture an evergreen Event Guide surface or defer |
| 4 | Goals (trade/PvP/candy) | Shipped options only, do not invent PvP rankings or raid recommendations |
| 5 | Settings language controls | App Language and Search String Language shown as independent settings |

**Localized short headlines (editorial options; review in a native speaker/layout pass):**

| Slot | EN | TR | DE |
|---|---|---|---|
| 1 | Review before you transfer | Aktarmadan önce kontrol et | Vor dem Verschicken prüfen |
| 2 | Build the search you need | Aradığın filtreyi oluştur | Passenden Suchfilter erstellen |
| 3 | Prepare for upcoming events | Etkinliklere önceden hazırlan | Auf Events vorbereitet sein |
| 4 | Find trade, PvP & candy candidates | Takas, PvP ve şeker adaylarını bul | Kandidaten für Tausch, PvP & Bonbons |
| 5 | Keep UI and search languages separate | Arayüz ve arama dilini ayrı seç | App- und Suchsprache getrennt wählen |

| Slot | ES | FR | IT |
|---|---|---|---|
| 1 | Revisa antes de transferir | Vérifie avant de transférer | Controlla prima di trasferire |
| 2 | Crea el filtro que necesitas | Crée la recherche qu'il te faut | Crea la ricerca che ti serve |
| 3 | Prepárate para los próximos eventos | Prépare les prochains événements | Preparati ai prossimi eventi |
| 4 | Busca candidatos para intercambios, PvP y caramelos | Trouve des candidats pour échanges, PvP et bonbons | Trova candidati per scambi, PvP e caramelle |
| 5 | Separa el idioma de la app y el de búsqueda | Sépare la langue de l'app et des recherches | Separa lingua dell'app e lingua di ricerca |

These are overlays for **real app screenshots**, not baked or fabricated interface mockups. Review every headline for naturalness, text fit, and consistency with the actual locale shown in the capture; shorten it if necessary.

**Output spec:** 1080 × 1920 portrait PNG, 24-bit/no alpha, each under Google Play limits. If source phone captures have a taller aspect ratio, fit them proportionally within the promotional canvas; do not distort, hide meaningful warnings, cover controls, or synthesize interface content. Distinguish any promotional headline visually from actual app UI. Use only PokeQuery original artwork. Preserve meaningful contrast and readable text at mobile preview sizes.

**Acceptance:** five EN graphics reviewed together, six languages checked for typography and UI correctness, no actual pixel crop concealing a gate, no fake feature, no trademarked third-party artwork. Store locally outside tracked source until approved.

### Phase C — one controlled Play Console experiment

1. Record the actual baseline screenshot order first.
2. Do **not** overwrite the original control just to begin an experiment. Use the existing published graphics as control and outcome-first graphics as a single variant when eligible.
3. Use Google's current `Default graphics experiment` or a localized experiment appropriate to the affected language. Primary goal: **Unique user install clicks**.
4. Check Play Console's sample-size/time estimate. With too little traffic, report **inconclusive**, do not declare an uplift or prematurely switch winners.
5. Keep icon, descriptions and screenshots from changing at once. Do not launch paid promotion until the separate paid-acquisition gate is satisfied.

### Phase D — substantive community acquisition

- The September Mega Finale hook in `COMMUNITY_LAUNCH_PACK.md` has **expired**. Never post its dated event claims as current advice.
- Revisit replies to the four September 6 creator/developer submissions before another standalone promotional post.
- Check the actual moderator replies for r/pokemongo and r/TheSilphRoad. The 2026-09-06 record of a pending request does not prove its status today.
- Next optional single experiment: a value-first r/IMadeThis creator story **only after verifying current rules and that it has not already been posted**. Include genuine screenshots and an honest disclosure that PokeQuery is the developer's app. Publishing stays an explicit human action.
- No repeated identical messages, no unverifiable string examples, no event-date guessing, no link tracking and no paid advertising.

## Why the store work comes before feature work

The app already ships goal-based searches, visible generated syntax, Search Assistant, Event Guide, share support and conservative review gates. A user arriving from a community should recognize those benefits in the **first one or two screenshots**. The existing screenshot inventory includes older onboarding and app layouts, so replacing the visual story with current genuine captures is a more testable near-term bet than adding new product features.

## Source hierarchy

- Current repo code/CI at exact ref is product truth.
- Google Play Console is live listing/distribution truth; public indexed search snippets may lag.
- Official Google Play listing/screenshot/experiment guidance governs Play limits and interface labels.
- Historical 2026-09-06 launch documents are evidence of plans/actions then, not proof of current moderator responses or store performance.

Official Google Play references:
- https://support.google.com/googleplay/android-developer/answer/9859152
- https://support.google.com/googleplay/android-developer/answer/9866151
- https://support.google.com/googleplay/android-developer/answer/12053285
- https://support.google.com/googleplay/android-developer/answer/9867158
- https://support.google.com/googleplay/android-developer/answer/9859173
