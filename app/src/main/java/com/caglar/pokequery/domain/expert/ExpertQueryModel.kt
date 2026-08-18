package com.caglar.pokequery.domain.expert

/**
 * Expert Builder query model (pure, deterministic and testable).
 *
 * [positiveTokens] preserves the original OR-group behavior for the primary include-status row.
 * [filterGroups] models the broader finite official syntax correctly: values inside one family
 * are OR'ed (for example `kanto,johto`) while different families are AND'ed (for example
 * `kanto,johto&shiny&xxl`). This avoids both accidental broadening and impossible ANDs such as
 * `kanto&johto`.
 *
 * Numeric/range fields remain explicit. Open-ended official families such as Pokémon names,
 * nicknames, moves, tags and arbitrary ranges are available through Raw mode and are documented
 * centrally by OfficialSearchSyntax.
 */
data class ExpertQueryModel(
    val positiveTokens: Set<String> = emptySet(),
    val filterGroups: Map<String, Set<String>> = emptyMap(),
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
        filterGroups.toSortedMap().values.forEach { values ->
            if (values.isNotEmpty()) parts.add(values.sorted().joinToString(","))
        }
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

    fun toggleFilter(group: String, token: String): ExpertQueryModel {
        val current = filterGroups[group].orEmpty()
        val updated = if (token in current) current - token else current + token
        val groups = filterGroups.toMutableMap()
        if (updated.isEmpty()) groups.remove(group) else groups[group] = updated
        return copy(filterGroups = groups)
    }

    fun isFilterSelected(group: String, token: String): Boolean = token in filterGroups[group].orEmpty()

    fun toggleExclusion(token: String): ExpertQueryModel =
        copy(exclusions = if (token in exclusions) exclusions - token else exclusions + token)

    fun setIvAttack(floor: Int?): ExpertQueryModel = copy(ivAttackFloor = floor)
    fun setIvDefense(floor: Int?): ExpertQueryModel = copy(ivDefenseFloor = floor)
    fun setIvHp(floor: Int?): ExpertQueryModel = copy(ivHpFloor = floor)
    fun setCount(floor: Int?): ExpertQueryModel = copy(countFloor = floor)
    fun setAge(enabled: Boolean): ExpertQueryModel = copy(age365 = enabled)
    fun setDistance(enabled: Boolean): ExpertQueryModel = copy(distance100 = enabled)
}
