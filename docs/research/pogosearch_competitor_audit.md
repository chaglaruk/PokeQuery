# Competitor Audit: pogosearch

## Overview
Repository: `https://github.com/its-snooze/pogosearch`

Pogosearch is a comprehensive, open-source tool for generating Pokémon GO search strings. It is highly advanced and supports extensive multi-language translation dictionaries.

## What pogosearch does better
- **Exhaustive Language Support**: Pogosearch has an incredibly rich set of translation tables mapped to various languages, enabling global string generation.
- **Deep Feature Set**: It allows highly granular selection of virtually every search parameter possible in Pokémon GO, acting as an advanced query constructor.

## What PokeQuery intentionally does differently
- **Positioning**: PokeQuery is a "safe, guided assistant" rather than a raw query constructor. We prioritize safety wrappers (like omitting 100% IVs from transfer strings automatically) over raw sandbox flexibility.
- **Scope limitation**: We are heavily restricting the tool to specific, curated goals (Cleanup, PvP candidates, Candy Prep) rather than building a generic search UI that requires the user to know exactly what they want to filter.

## Features worth borrowing conceptually
- **Language mapping architectures**: Using structured JSON for mapping English baseline queries to localized equivalents is a highly robust approach.
- **Pokedex number expansions**: For complex searches where text breaks down (like forms/costumes), expanding into specific Pokédex integers is a powerful technique.

## Features not worth cloning
- **The complex form UI**: Building a massive web of checkboxes for every possible Pokémon trait goes against our mobile-friendly, guided approach.
- **Over-translation of beta features**: Implementing untested string localizations that haven't been manually verified in the live game client.

## Risk of becoming too complex
Attempting to replicate pogosearch's full feature set would dilute PokeQuery's value proposition as a fast, safe "cleanup assistant" and turn it into a heavy power-user sandbox. We will maintain strict goal-oriented flows.
