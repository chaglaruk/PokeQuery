package com.caglar.pokequery.domain.practice

/**
 * v0.6.1 — Practice Mode synthetic dataset.
 *
 * Entirely fictional (no species names, no copyrighted assets). The IDs/flags are designed so
 * the conceptual matcher can demonstrate: Safe Cleanup exclusions, Trade Fodder (!traded),
 * 2x Candy Prep, Hundo/Nundo, PvP candidates, and Background/Ultra Beast protection.
 */
object PracticeDataset {

    val items: List<FakeInventoryPokemon> = listOf(
        FakeInventoryPokemon(
            id = "f1", displayName = "Common duplicate A", cp = 312, ivTag = IvTag.LOW_IV,
            tagged = false
        ),
        FakeInventoryPokemon(
            id = "f2", displayName = "Shiny duplicate", cp = 880, ivTag = IvTag.LOW_IV,
            shiny = true, tagged = true, tags = listOf("keep")
        ),
        FakeInventoryPokemon(
            id = "f3", displayName = "Legendary transfer bait", cp = 1900, ivTag = IvTag.LOW_IV,
            legendary = true
        ),
        FakeInventoryPokemon(
            id = "f4", displayName = "Mythical keepsake", cp = 2100, ivTag = IvTag.PVP_CANDIDATE,
            mythical = true
        ),
        FakeInventoryPokemon(
            id = "f5", displayName = "Ultra Beast visitor", cp = 2600,
            ivTag = IvTag.PVP_CANDIDATE, ultrabeast = true
        ),
        FakeInventoryPokemon(
            id = "f6", displayName = "Costume event catch", cp = 410, ivTag = IvTag.LOW_IV,
            costume = true
        ),
        FakeInventoryPokemon(
            id = "f7", displayName = "Shadow project", cp = 1500, ivTag = IvTag.LOW_IV, shadow = true
        ),
        FakeInventoryPokemon(
            id = "f8", displayName = "Purified gem", cp = 1650, ivTag = IvTag.PVP_CANDIDATE, purified = true
        ),
        FakeInventoryPokemon(
            id = "f9", displayName = "Favorite buddy", cp = 2400, ivTag = IvTag.PVP_CANDIDATE,
            favorite = true, tagged = true, tags = listOf("buddy")
        ),
        FakeInventoryPokemon(
            id = "f10", displayName = "Lucky received", cp = 1200, ivTag = IvTag.PVP_CANDIDATE,
            lucky = true, traded = true
        ),
        FakeInventoryPokemon(
            id = "f11", displayName = "Traded duplicate", cp = 520, ivTag = IvTag.LOW_IV, traded = true
        ),
        FakeInventoryPokemon(
            id = "f12", displayName = "Background feature catch", cp = 980,
            ivTag = IvTag.LOW_IV, background = true
        ),
        FakeInventoryPokemon(
            id = "f13", displayName = "Location background scene", cp = 760,
            ivTag = IvTag.LOW_IV, locationbackground = true
        ),
        FakeInventoryPokemon(
            id = "f14", displayName = "Special background star", cp = 1340,
            ivTag = IvTag.PVP_CANDIDATE, specialbackground = true
        ),
        FakeInventoryPokemon(
            id = "f15", displayName = "Hundo prize", cp = 3000, ivTag = IvTag.HUNDO
        ),
        FakeInventoryPokemon(
            id = "f16", displayName = "Nundo oddity", cp = 42, ivTag = IvTag.NUNDO
        ),
        FakeInventoryPokemon(
            id = "f17", displayName = "Great League candidate", cp = 1480, ivTag = IvTag.PVP_CANDIDATE
        ),
        FakeInventoryPokemon(
            id = "f18", displayName = "Plain duplicate B", cp = 290, ivTag = IvTag.LOW_IV
        )
    )
}
