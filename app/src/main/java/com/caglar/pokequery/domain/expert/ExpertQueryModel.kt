package com.caglar.pokequery.domain.expert

/**
 * v0.5.0 / v0.5.1 Expert Builder — modular query model (pure, testable).
 *
 * The chip-based builder composes a raw query from:
 *   - positive tokens (joined with ',' = OR within a group)
 *   - IV floors (e.g. 0attack, 15defense)
 *   - a count floor (countN-)
 *   - age / distance filters (age365-, distance100-)
 *   - exclusions (joined with '&' = AND, each prefixed '!')
 *
 * The produced string is fed to the EXISTING Linter / ExpertCopyPolicy /
 * StringBuilderEngine — safety behavior is unchanged. This model never weakens the
 * linter; it only produces text.
 *
 * Order in the output is deterministic: positives (sorted) -> iv floors -> count ->
 * age -> distance -> exclusions.
 *
 * v0.5.1 (Fix 6/7): expanded with age, distance, and IV defense/hp floors so the
 * Expert Builder can offer the richer grouped option set without horizontal scrolling.
 * Defense/hp setters and the new fields are strictly additive.
 */
data class ExpertQueryModel(
    val positiveTokens: Set<String> = emptySet(),
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
        ivAttackFloor?.let { parts.add("${it}attack") }
        ivDefenseFloor?.let { parts.add("${it}defense") }
        ivHpFloor?.let { parts.add("${it}hp") }
        countFloor?.let { parts.add("count$it-") }
        if (age365) parts.add("age365-")
        if (distance100) parts.add("distance100-")
        if (exclusions.isNotEmpty()) {
            parts.add(exclusions.sorted().joinToString("&") { "!$it" })
        }
        return parts.joinToString("&")
    }

    fun togglePositive(token: String): ExpertQueryModel =
        copy(positiveTokens = if (token in positiveTokens) positiveTokens - token else positiveTokens + token)

    fun toggleExclusion(token: String): ExpertQueryModel =
        copy(exclusions = if (token in exclusions) exclusions - token else exclusions + token)

    fun setIvAttack(floor: Int?): ExpertQueryModel = copy(ivAttackFloor = floor)
    fun setIvDefense(floor: Int?): ExpertQueryModel = copy(ivDefenseFloor = floor)
    fun setIvHp(floor: Int?): ExpertQueryModel = copy(ivHpFloor = floor)
    fun setCount(floor: Int?): ExpertQueryModel = copy(countFloor = floor)
    fun setAge(enabled: Boolean): ExpertQueryModel = copy(age365 = enabled)
    fun setDistance(enabled: Boolean): ExpertQueryModel = copy(distance100 = enabled)
}
