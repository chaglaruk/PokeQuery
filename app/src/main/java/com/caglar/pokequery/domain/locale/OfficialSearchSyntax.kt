package com.caglar.pokequery.domain.locale

/**
 * Canonical inventory of the Pokémon GO inventory-search syntax documented by the official
 * Pokémon GO Help Center.
 *
 * Source: https://niantic.helpshift.com/hc/en/6-pokemon-go/faq/1486-searching-filtering-your-pokemon-inventory/
 * Audited: 2026-08-18.
 *
 * This is deliberately a syntax-family registry rather than an impossible list of every search
 * string. Pokémon names, nicknames, move names, tag names and numeric values are open-ended, so
 * those capabilities are represented as parameterized families. Finite official values are kept
 * explicitly so tests and UI surfaces can prove they have not silently gone missing.
 */
data class OfficialSearchFamily(
    val id: String,
    val pattern: String,
    val example: String,
    val finiteValues: List<String> = emptyList(),
    val parameterized: Boolean = false
)

object OfficialSearchSyntax {
    const val SOURCE_URL =
        "https://niantic.helpshift.com/hc/en/6-pokemon-go/faq/1486-searching-filtering-your-pokemon-inventory/"
    const val AUDITED_DATE = "2026-08-18"

    val appraisalValues = listOf("0*", "1*", "2*", "3*", "4*")
    val sizeValues = listOf("xxs", "xs", "xl", "xxl")
    val buddyValues = (0..5).map { "buddy$it" }
    val megaLevelValues = (1..3).map { "mega$it" }
    val regions = listOf("kanto", "johto", "hoenn", "sinnoh", "unova", "kalos", "alola", "galar", "hisui", "paldea")
    val pokemonTypes = listOf(
        "normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison",
        "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
    )

    /** Finite, directly selectable canonical filters. */
    val statusValues = listOf(
        "shiny", "legendary", "mythical", "ultrabeast", "shadow", "purified", "costume",
        "lucky", "traded", "favorite", "defender", "dynamax", "gigantamax", "fusion",
        "megaevolve", "evolve", "hypertraining", "item", "evolvenew", "evolvequest",
        "tradeevolve", "eggsonly", "hatched", "background", "locationbackground", "@special",
        "@weather"
    )

    val families: List<OfficialSearchFamily> = listOf(
        OfficialSearchFamily("pokemon-name", "<Pokémon name>", "Pikachu", parameterized = true),
        OfficialSearchFamily("nickname", "<nickname>", "Sparky", parameterized = true),
        OfficialSearchFamily("pokedex-number", "<Pokédex number>", "25", parameterized = true),
        OfficialSearchFamily("pokemon-type", "<type>", "fire", finiteValues = pokemonTypes),
        OfficialSearchFamily("region", "<region>", "kanto", finiteValues = regions),
        OfficialSearchFamily("evolution-family", "+<Pokémon>", "+Pikachu", parameterized = true),
        OfficialSearchFamily("cp", "cp<value/range>", "cp200-300", parameterized = true),
        OfficialSearchFamily("hp", "hp<value/range>", "hp100-", parameterized = true),
        OfficialSearchFamily("distance", "distance<value/range>", "distance100-", parameterized = true),
        OfficialSearchFamily("dynamax", "dynamax", "dynamax", finiteValues = listOf("dynamax")),
        OfficialSearchFamily("fusion", "fusion", "fusion", finiteValues = listOf("fusion")),
        OfficialSearchFamily("gigantamax", "gigantamax", "gigantamax", finiteValues = listOf("gigantamax")),
        OfficialSearchFamily("size", "xxs/xs/xl/xxl", "xxl", finiteValues = sizeValues),
        OfficialSearchFamily("mega-evolvable", "megaevolve", "megaevolve", finiteValues = listOf("megaevolve")),
        OfficialSearchFamily("mega-level", "mega1..mega3", "mega2", finiteValues = megaLevelValues),
        OfficialSearchFamily("move-name", "@<move>", "@scratch", parameterized = true),
        OfficialSearchFamily("move-type", "@<type>", "@grass", parameterized = true),
        OfficialSearchFamily("special-move", "@special", "@special", finiteValues = listOf("@special")),
        OfficialSearchFamily("move-slot", "@1<type>/@2<type>/@3<type>", "@3ghost", parameterized = true),
        OfficialSearchFamily("age", "age<days/range>", "age0-7", parameterized = true),
        OfficialSearchFamily("buddy-level", "buddy0..buddy5", "buddy5", finiteValues = buddyValues),
        OfficialSearchFamily("can-evolve", "evolve", "evolve", finiteValues = listOf("evolve")),
        OfficialSearchFamily("favorite", "favorite", "favorite", finiteValues = listOf("favorite")),
        OfficialSearchFamily("gym-defender", "defender", "defender", finiteValues = listOf("defender")),
        OfficialSearchFamily("hyper-training", "hypertraining", "hypertraining", finiteValues = listOf("hypertraining")),
        OfficialSearchFamily("item-evolution", "item", "item", finiteValues = listOf("item")),
        OfficialSearchFamily("new-evolution", "evolvenew", "evolvenew", finiteValues = listOf("evolvenew")),
        OfficialSearchFamily("evolution-quest", "evolvequest", "evolvequest", finiteValues = listOf("evolvequest")),
        OfficialSearchFamily("trade-evolution", "tradeevolve", "tradeevolve", finiteValues = listOf("tradeevolve")),
        OfficialSearchFamily("weather-boosted-move", "@weather", "@weather", finiteValues = listOf("@weather")),
        OfficialSearchFamily("year", "year<year/range>", "year2020", parameterized = true),
        OfficialSearchFamily("appraisal", "0*..4*", "4*", finiteValues = appraisalValues),
        OfficialSearchFamily("attack-appraisal", "0..4attack", "3-4attack", parameterized = true),
        OfficialSearchFamily("defense-appraisal", "0..4defense", "3-4defense", parameterized = true),
        OfficialSearchFamily("hp-appraisal", "0..4hp", "3-4hp", parameterized = true),
        OfficialSearchFamily("egg-exclusive", "eggsonly", "eggsonly", finiteValues = listOf("eggsonly")),
        OfficialSearchFamily("hatched", "hatched", "hatched", finiteValues = listOf("hatched")),
        OfficialSearchFamily("lucky", "lucky", "lucky", finiteValues = listOf("lucky")),
        OfficialSearchFamily("legendary", "legendary", "legendary", finiteValues = listOf("legendary")),
        OfficialSearchFamily("mythical", "mythical", "mythical", finiteValues = listOf("mythical")),
        OfficialSearchFamily("background", "background", "background", finiteValues = listOf("background")),
        OfficialSearchFamily("location-background", "locationbackground", "locationbackground", finiteValues = listOf("locationbackground")),
        OfficialSearchFamily("purified", "purified", "purified", finiteValues = listOf("purified")),
        OfficialSearchFamily("shadow", "shadow", "shadow", finiteValues = listOf("shadow")),
        OfficialSearchFamily("shiny", "shiny", "shiny", finiteValues = listOf("shiny")),
        OfficialSearchFamily("costume", "costume", "costume", finiteValues = listOf("costume")),
        OfficialSearchFamily("tag", "#<tag>", "#battle", parameterized = true),
        OfficialSearchFamily("traded", "traded", "traded", finiteValues = listOf("traded")),
        OfficialSearchFamily("ultra-beast", "ultrabeast", "ultrabeast", finiteValues = listOf("ultrabeast")),
        OfficialSearchFamily("and", "&", "shiny&legendary"),
        OfficialSearchFamily("or", ", / : / ;", "shiny,legendary"),
        OfficialSearchFamily("not", "!", "!shiny"),
        OfficialSearchFamily("range", "<min>-<max>", "cp200-300", parameterized = true)
    )

    val familyIds: Set<String> = families.mapTo(linkedSetOf()) { it.id }

    fun byId(id: String): OfficialSearchFamily? = families.firstOrNull { it.id == id }
}
