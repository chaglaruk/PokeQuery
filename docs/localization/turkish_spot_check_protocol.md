# Turkish Spot-Check Protocol

This protocol governs how PokeQuery verifies Turkish Pokémon GO search terms **without** over-claiming, and **without** any automation, scraping, or account access.

## Core rules

1. **Set the game language to Turkish.** Open Pokémon GO → Settings → Language (if available on the build) → Türkçe. The client must display Turkish for the term to be valid.
2. **Search each token manually.** Type or paste the candidate Turkish token into the in-game storage search bar. Do **not** use any macro, automation, or third-party tool that touches the account.
3. **Compare to English behavior where possible.** Run the same query on an English client (or recall its known result) and confirm the Turkish result set matches in size and composition.
4. **Screenshots only if no personal info is exposed.** If you capture evidence, ensure no trainer name, friend list, location, or account identifier is visible. Blurring is preferred over cropping.
5. **Never transfer or trade during verification.** Verification is read-only: search and observe. Do not act on results.
6. **Mark a token `works` only after live confirmation.** A community source, a prior patch's behavior, or "it probably works" is **not** sufficient. Update `docs/localization/turkish_verification_matrix.md` with the date, tester, and device.
7. **Compound / no-space tokens must be tested exactly as written.** e.g. `toplam2-`, `0saldırı`, `konum arka planlı`. A token that works in isolation but fails as a compound (or vice versa) is `ambiguous`, not `works`.

## Per-token minimum check

For each token in the matrix:

1. Type the candidate Turkish token into the search bar.
2. Confirm the result count is non-zero (for positive queries) or correctly reduces results (for `!` exclusions).
3. Spot-check 2–3 results to confirm they match the intended category.
4. If it fails, try the alternative candidates listed in the matrix and record which one (if any) works.

## After verification

- Update the matrix row: set Status, Date tested, Tester/device, Notes.
- If the verified candidate differs from the current code map, open a change to `SearchTermMapper.kt` and the Knowledge Base `description_tr`/`commonMistake` fields together, with a test.
- Do **not** remove the in-app "Turkish (Beta — verify before use)" label until every token used by every goal is `works` and a separate sign-off decision is made.

## Out of scope (never do during verification)

- Logging into another person's account.
- Using emulators/spoofing to change location.
- Automating taps, swipes, or transfers.
- Scraping the game or its assets.
- Treating a single device's behavior as universal — record the device/OS/build.
