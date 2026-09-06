package com.caglar.pokequery.growth

object GrowthPromptPolicy {
    const val FIRST_PROMPT_COPY_COUNT = 3
    const val REPEAT_PROMPT_COPY_GAP = 10

    fun shouldPrompt(successfulCopies: Int, lastPromptCount: Int, ratingOpened: Boolean): Boolean {
        if (ratingOpened) return false
        return if (lastPromptCount == 0) {
            successfulCopies >= FIRST_PROMPT_COPY_COUNT
        } else {
            successfulCopies - lastPromptCount >= REPEAT_PROMPT_COPY_GAP
        }
    }
}
