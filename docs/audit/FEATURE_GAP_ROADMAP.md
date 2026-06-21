# PokeQuery — Feature Gap Analysis & Risk Register & Roadmap

**Date:** 2026-06-21 · **Version:** 0.4.1 · **Mode:** Analysis only.

This document combines (7) feature gap analysis, (10) risk register, and (11) prioritized roadmap as requested.

---

## Part A — Feature Gap Analysis

Each feature: **Value / Risk / Effort / Phase**. Phase key: **A** = before continuing closed testing; **B** = before production access request; **C** = nice-to-have after production; **D** = larger future ideas.

### A1. Turkish verification gate (or disable)
**Value:** Eliminates the single highest correctness/legal risk. **Risk:** Low (mostly removal/gating). **Effort:** S. **Phase:** **A.** Either clear the spot-check matrix in `turkish_localization_plan.md` or gate Turkish behind an explicit beta toggle with a visible "unverified" notice.

### A2. Preset safety hardening
**Value:** Closes the latent unsafe-string foot-gun (BUG-007). **Risk:** Low. **Effort:** S. **Phase:** **A.** Route presets through the engine's count-protection path or bake protections into syntax; add parameterized test.

### A3. Trade-goal `!traded` invariant fix
**Value:** Restores engine contract in the screen layer (BUG-001). **Risk:** Low. **Effort:** S. **Phase:** **A.**

### A4. Expert-builder linter enforcement
**Value:** Makes the displayed safety guard real (BUG-005). **Risk:** Low. **Effort:** S. **Phase:** **A.**

### A5. Version-string & doc reconciliation
**Value:** Tester/reviewer trust. **Risk:** None. **Effort:** XS. **Phase:** **A.**

### A6. Non-affiliation disclaimer
**Value:** IP/trademark risk reduction. **Risk:** None. **Effort:** XS. **Phase:** **B** (store listing) / **A** (in-app).

### A7. Confirmation dialogs for destructive actions
**Value:** Prevents accidental data loss (BUG-011). **Risk:** None. **Effort:** S. **Phase:** **B.**

### A8. R8 minify + WebP assets
**Value:** ~34 MB → single-digit MB AAB; faster installs; better store hygiene. **Risk:** Medium (need keep rules for serialization/nav). **Effort:** M. **Phase:** **B.**

### A9. "Why this is risky" explanations on RiskWarning
**Value:** Users currently see a generic warning; per-goal explanation (e.g., "this list may contain shinies") improves safe behavior. **Risk:** None. **Effort:** S. **Phase:** **B.**

### A10. Search-string validator/linter UI for Expert (richer)
**Value:** Power users get inline suggestions, not just warnings. **Risk:** Low. **Effort:** M. **Phase:** **C.**

### A11. User-defined presets / custom protected categories
**Value:** Repeatability for advanced users. **Risk:** Medium (must still pass safety rules). **Effort:** M. **Phase:** **C.**

### A12. Import/export favorites
**Value:** Device migration; backup. **Risk:** Medium (file I/O, URI safety). **Effort:** M. **Phase:** **C.**

### A13. Pokémon GO language packs (beyond EN/TR)
**Value:** Broader reach. **Risk:** High (each needs verification). **Effort:** L. **Phase:** **D.**

### A14. Versioned knowledge base
**Value:** Auditable, dated term changes without app releases. **Risk:** Low. **Effort:** M. **Phase:** **C.**

### A15. In-app feedback / tester feedback screen
**Value:** Closed-testing signal collection. **Risk:** Low (no network → use mailto/share intent). **Effort:** S. **Phase:** **B.**

### A16. Safer duplicate-cleanup builder
**Value:** A guided, multi-step "review before transfer" checklist flow. **Risk:** Low. **Effort:** L. **Phase:** **D.**

### A17. Event-mode presets
**Value:** Time-boxed community-day/raid strings. **Risk:** Low. **Effort:** S. **Phase:** **C.**

### A18. Onboarding improvements
**Value:** Explain *how* to paste into Pokémon GO; explain risk colors. **Risk:** None. **Effort:** S. **Phase:** **B.**

---

## Part B — Risk Register

| # | Risk | Impact | Likelihood | Mitigation | Priority |
|---|---|---|---|---|---|
| R1 | Turkish tokens wrong → unsafe cleanup string on tr-TR devices | High (data loss) | Medium | Gate/disable Turkish until verified; golden-table test (A1) | **P0** |
| R2 | Preset ships unsafe count/IV string in a future edit | High (data loss) | Low today | Parameterized preset-safety test (A2) | **P0** |
| R3 | Trade-goal `!traded` bypassed by screen layer | High (data loss) | Low (masked by default toggle) | Fix GoalDetail re-wrap (A3) | **P0** |
| R4 | Play rejection for missing non-affiliation disclaimer / IP in repo | Medium (rejection delay) | Low–Medium | Add disclaimer; verify `docs/design` provenance (A6) | **P1** |
| R5 | Users over-trust search strings and transfer valuables | High (user harm) | Medium | Per-goal RiskWarning copy; "review before acting" emphasis (A9) | **P1** |
| R6 | Stale version strings confuse testers/reviewers | Low | High | Dynamic version; delete stale doc (A5) | **P1** |
| R7 | Accidental data loss from one-tap clear/reset | Medium | Medium | Confirmation dialogs (A7) | **P1** |
| R8 | AAB bloat (34 MB) → slow installs, store perception | Low | High | R8 + WebP (A8) | **P1** |
| R9 | `allowBackup` leaks favorites/history to Google account | Low | Medium | allowBackup=false or exclusion rules | **P2** |
| R10 | UI regression after future polish (no screenshot tests) | Medium | Medium | Screenshot tests (S-T1) | **P2** |
| R11 | Release signing mis-configured in CI (unsigned release) | High (release block) | Low | Signing-config test (R-T5) | **P1** |
| R12 | Package-name confusion from stale `app_config_audit.md` | Low | Low | Delete/rewrite doc (A5) | **P1** |
| R13 | Linter bypass in Expert builder | Medium | Medium | Enforce linter errors (A4) | **P0** |
| R14 | Local keystore password exposure on shared machine | High (if realized) | Low | Rotate; keep gitignored (already) | **P2** |

---

## Part C — Prioritized Roadmap

### Phase A — Must fix before continuing closed testing
| Item | Effort | Impact | Files likely | Acceptance criteria |
|---|---|---|---|---|
| A1 Turkish gate | S | High | `SearchTermMapper.kt`, `UserPreferencesRepository.kt`, Settings UI | Turkish either verified-by-test or disabled/notice; default=English |
| A2 Preset safety | S | High | `PresetsScreen.kt`, `StringBuilderEngineTest` | All presets with count/IV tokens carry mandatory protections (test green) |
| A3 Trade `!traded` | S | High | `GoalDetailScreen.kt` | trade_fodder/lucky_trade final string always contains `!traded` |
| A4 Expert linter enforce | S | Medium | `ExpertBuilderScreen.kt` | Copy disabled when any `LintWarning.isError` |
| A5 Version/doc fix | XS | Medium | `MiscScreens.kt`, `app_config_audit.md` | About shows 0.4.1; stale doc removed/rewritten |

### Phase B — Should fix before production access request
| Item | Effort | Impact | Files likely | Acceptance criteria |
|---|---|---|---|---|
| A6 Disclaimer | XS | Medium | `STORE_LISTING_DRAFT.md`, About screen | Non-affiliation text present in listing + in-app |
| A7 Confirmation dialogs | S | Medium | `MiscScreens.kt` | All clear/reset actions show AlertDialog |
| A8 Minify + WebP | M | Medium | `app/build.gradle.kts`, `drawable-nodpi/*`, `proguard-rules.pro` | AAB < 10 MB; release smoke passes |
| A9 Per-goal risk copy | S | High | `RiskWarningScreen.kt`, `GeneratedString` | Warning text varies by goalId; mentions valuables for trade/lucky |
| A15 Tester feedback | S | Medium | new screen, Navigation | mailto/share intent works offline |
| A18 Onboarding polish | S | Medium | `OnboardingScreen.kt` | Paste-how-to + risk-color legend page |
| R-T1/R-T4/R-T5 tests | M | High | `app/src/...Test` | Manifest/version/signing tests green in CI |

### Phase C — Nice-to-have after production
| Item | Effort | Impact | Notes |
|---|---|---|---|
| A10 Richer linter UI | M | Medium | Inline suggestions |
| A11 User presets | M | Medium | Must pass safety rules |
| A12 Import/export | M | Medium | URI safety |
| A14 Versioned KB | M | Low | Auditable term updates |
| A17 Event presets | S | Low | Time-boxed |
| S-T1 Screenshot tests | M | Medium | Lock visual regressions |
| L-T1 Golden TR table | M | High (post-verify) | Requires verified tokens |

### Phase D — Larger future ideas
| Item | Effort | Impact | Notes |
|---|---|---|---|
| A13 More language packs | L | High reach | Each needs client verification |
| A16 Safer cleanup builder | L | High | Multi-step review-before-transfer flow |
| Community preset packs | L | Medium | Sharing/import flow |

---

## Final prioritized recommendation

Do **Phase A** now (5 small changes, all with existing test scaffolds). It removes the three High search-safety risks (Turkish, presets, trade `!traded`) and the linter-bypass, then run the new P0 tests. Only then cut the next closed-testing build. Phase B items are the gate to a production access request, with the non-affiliation disclaimer and minify/WebP being the most visible to Play reviewers.
