package com.caglar.pokequery.domain.scope

enum class InventorySizeProfile(val label: String, val description: String) {
    NOT_SET("Not set", "Use generic scope explanations."),
    SMALL("Small collection", "Approx. 0–500 Pokémon."),
    MEDIUM("Medium collection", "Approx. 500–1500 Pokémon."),
    LARGE("Large collection", "Approx. 1500–3000 Pokémon."),
    VERY_LARGE("Very large collection", "Approx. 3000+ Pokémon.");

    companion object {
        fun fromStored(value: String?): InventorySizeProfile =
            entries.firstOrNull { it.name == value } ?: NOT_SET
    }
}

object ScopeBreadthExplainer {
    fun explain(scopeBreadth: String, profile: InventorySizeProfile): String {
        val base = "Result breadth: $scopeBreadth. PokeQuery cannot see your Pokémon GO inventory; this is educational only."
        return when (profile) {
            InventorySizeProfile.NOT_SET -> "$base Set inventory size context in Settings for a more useful explanation."
            InventorySizeProfile.SMALL -> "$base Broad queries may still be manageable for a smaller collection, but always review before acting."
            InventorySizeProfile.MEDIUM -> "$base Medium and broad queries can produce a meaningful review list; spot-check before transfer, trade, or cleanup."
            InventorySizeProfile.LARGE -> "$base Broad queries may match many Pokémon in a larger collection. Review carefully before action."
            InventorySizeProfile.VERY_LARGE -> "$base Broad queries may match many Pokémon in a very large collection. Use narrow filters and review carefully before transfer, trade, or cleanup."
        }
    }
}
