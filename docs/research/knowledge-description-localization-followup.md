# Knowledge Base description localization follow-up

Scope: DE, ES, FR and IT catalog explanations still contain English fallback prose.
PR #47 corrects goal-specific risk warnings and Event Guide status copy; it does not
claim that the full Knowledge Base catalog is translated.

Complete this as a separate localization task:

- Inventory every catalog explanation, example and reference-only hint in Android
  and Web, including intentionally shared English token syntax.
- Translate explanatory prose with native-language review. Preserve Pokémon GO
  search tokens, punctuation and examples byte-for-byte unless independently
  confirmed official syntax requires a separate, tested change.
- Keep unconfirmed localized tokens BETA/unverified. Translation of a description
  is not evidence that a token works in the live game.
- Preserve reference-only copy restrictions for templates such as `cp[N]` and
  unsupported operators such as `|`; confirm that concrete supported tokens remain
  usable.
- Validate Android/Web catalog parity, all six app languages, and genuine device
  readability at normal and enlarged font scales before closing the follow-up.

Acceptance requires a complete locale inventory with no unintended English prose
fallback, reviewed translations, unchanged token verification levels, passing
catalog/safety tests and new UI evidence. This follow-up is not a release, signing
or Play publication authorization.
