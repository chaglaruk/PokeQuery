package com.caglar.pokequery.growth

import android.content.Context

/**
 * Local-only state for the optional Play Store rating prompt.
 *
 * No analytics or telemetry is emitted. The first prompt is eligible after three
 * successful copy actions. If dismissed, it will not be eligible again until ten
 * more successful copies. Once the Play Store action is opened, the prompt stays off.
 */
class GrowthPromptStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun recordSuccessfulCopyAndShouldPrompt(): Boolean {
        val count = prefs.getInt(KEY_SUCCESSFUL_COPIES, 0) + 1
        val lastPromptCount = prefs.getInt(KEY_LAST_PROMPT_COUNT, 0)
        val ratingOpened = prefs.getBoolean(KEY_RATING_OPENED, false)
        prefs.edit().putInt(KEY_SUCCESSFUL_COPIES, count).apply()
        return GrowthPromptPolicy.shouldPrompt(count, lastPromptCount, ratingOpened)
    }

    fun markPromptShown() {
        prefs.edit()
            .putInt(KEY_LAST_PROMPT_COUNT, prefs.getInt(KEY_SUCCESSFUL_COPIES, 0))
            .apply()
    }

    fun markRatingOpened() {
        prefs.edit().putBoolean(KEY_RATING_OPENED, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "pokequery_growth_prompts"
        private const val KEY_SUCCESSFUL_COPIES = "successful_copies"
        private const val KEY_LAST_PROMPT_COUNT = "last_rating_prompt_count"
        private const val KEY_RATING_OPENED = "rating_opened"
    }
}
