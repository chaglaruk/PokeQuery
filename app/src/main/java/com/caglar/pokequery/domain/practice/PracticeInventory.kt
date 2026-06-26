package com.caglar.pokequery.domain.practice

/**
 * v0.6.1 — Practice Mode (fake inventory sandbox).
 *
 * An entirely SYNTHETIC, fictional Pokémon-like record used to teach search-string concepts.
 * This is NOT connected to Pokémon GO: no API, no screenshots, no OCR, no account access.
 * There are no official sprites — only text rows. Real Pokémon GO results will differ.
 *
 * Flags mirror the protection tokens the app generates (shiny, legendary, traded, ...) so the
 * conceptual matcher can show why an item is matched, protected, or excluded.
 */
data class FakeInventoryPokemon(
    val id: String,
    val displayName: String,
    val cp: Int? = null,
    val ivTag: IvTag = IvTag.UNKNOWN,
    val shiny: Boolean = false,
    val legendary: Boolean = false,
    val mythical: Boolean = false,
    val costume: Boolean = false,
    val shadow: Boolean = false,
    val purified: Boolean = false,
    val favorite: Boolean = false,
    val traded: Boolean = false,
    val lucky: Boolean = false,
    val ultrabeast: Boolean = false,
    val background: Boolean = false,
    val locationbackground: Boolean = false,
    val specialbackground: Boolean = false,
    val tagged: Boolean = false,
    val tags: List<String> = emptyList()
)

enum class IvTag { HUNDO, NUNDO, PVP_CANDIDATE, LOW_IV, UNKNOWN }

/** Conceptual outcome for a fake item against a generated string. */
enum class PracticeStatus { MATCHED, EXCLUDED, PROTECTED, NOT_MATCHED }

data class PracticeMatchResult(
    val item: FakeInventoryPokemon,
    val status: PracticeStatus,
    val reasons: List<String>
)
