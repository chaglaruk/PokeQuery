package com.caglar.pokequery.domain.expert

/**
 * Expert Builder query model (pure, deterministic and testable).
 *
 * [positiveTokens] preserves the original OR-group behavior for the primary include-status row.
 * [filterTokens] is an additive AND set used by the broader official syntax families (appraisal,
 * size, buddy/Mega level, region/type and other exact filters). This distinction prevents a
 * selection such as `shiny` + `kanto` from accidentally becoming `shiny,kanto`.
 *
 * Numeric/range fields remain explicit. Open-ended official families such as Pokémon names,
 * nicknames, moves, tags and arbitrary ranges are available through Raw mode and are documented
 * centrally by OfficialSearchSyntax.
 */
data class ExpertQueryModel(
    val positiveTokens: Set<String> = emptySet(),
    val filterTokens: Set<String> = emptySet(),
    val ivAttackFloor: Int? = null,
    val ivDefenseFloor: Int? = null,
    val ivHpFloor: Int? = null,
    val countFloor: Int? = null,
    val age365: Boolean = false,
    val distance100: Boolean = false,
    val exclusions: Set<String> = emptySet()
) {
    fun buildRawQuery(): String {
        val parts = mutableListOf<String>()
        if (positiveTokens.isNotEmpty()) {
            parts.add(positiveTokens.sorted().joinToString(","))
        }
        parts.addAll(filterTokens.sorted())
        ivAttackFloor?.let { parts.add("${it}attack") }
        ivDefenseFloor?.let { parts.add("${it}defense") }
        ivHpFloor?.let { parts.add("${it}hp") }
        countFloor?.let { parts.add("count$it-") }
        if (age365) parts.add("age365-")
        if (distance100) parts.add("distance100-")
        if (exclusions.isNotEmpty()) {
            parts.add(exclusions.sorted().joinToString("&") { "!$it" })
        }
        return parts.filter(String::isNotBlank).joinToString("&")
    }

    fun togglePositive(token: String): ExpertQueryModel =
        copy(positiveTokens = if (token in positiveTokens) positiveTokens - token else positiveTokens + token)

    fun toggleFilter(token: String): ExpertQueryModel =
        copy(filterTokens = if (token in filterTokens) filterTokens - token else filterTokens + token)

    fun toggleExclusion(token: String): ExpertQueryModel =
        copy(exclusions = if (token in exclusions) exclusions - token else exclusions + token)

    fun setIvAttack(floor: Int?): ExpertQueryModel = copy(ivAttackFloor = floor)
    fun setIvDefense(floor: Int?): ExpertQueryModel = copy(ivDefenseFloor = floor)
    fun setIvHp(floor: Int?): ExpertQueryModel = copy(ivHpFloor = floor)
    fun setCount(floor: Int?): ExpertQueryModel = copy(countFloor = floor)
    fun setAge(enabled: Boolean): ExpertQueryModel = copy(age365 = enabled)
    fun setDistance(enabled: Boolean): ExpertQueryModel = copy(distance100 = enabled)
}
