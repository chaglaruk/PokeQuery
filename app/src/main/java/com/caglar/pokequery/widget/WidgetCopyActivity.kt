package com.caglar.pokequery.widget

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.caglar.pokequery.R
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.SavedTemplate
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import kotlinx.coroutines.launch

/**
 * Internal-only widget copy target.
 *
 * AppWidget PendingIntents can launch this non-exported activity using the creator app's
 * identity, while third-party apps cannot start it directly. Production MainActivity therefore
 * does not need to trust an externally supplied `copy_search` extra.
 */
class WidgetCopyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val search = intent?.getStringExtra(EXTRA_COPY_SEARCH)?.trim().orEmpty()
        val route = intent?.getStringExtra(START_ROUTE_EXTRA).orEmpty()
        if (search.isBlank()) {
            finish()
            return
        }

        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("PokeQuery search", search))
        Toast.makeText(this, getString(R.string.goal_detail_copied), Toast.LENGTH_SHORT).show()

        val repository = UserPreferencesRepository(applicationContext.dataStore)
        val explanation = when (route) {
            ROUTE_SAFE_CLEANUP -> "Safe Cleanup search string copied from widget"
            ROUTE_CANDY_PREP -> "Candy Prep search string copied from widget"
            ROUTE_EVENTS -> "Event Guide search string copied from widget"
            else -> "Search string copied from widget"
        }

        lifecycleScope.launch {
            runCatching {
                repository.addHistory(
                    SavedTemplate.from(
                        GeneratedString(
                            rawSyntax = search,
                            plainLanguageExplanation = explanation,
                            protectedCategories = emptyList(),
                            includedHighRiskCategories = emptyList(),
                            riskLevel = RiskLevel.Low
                        )
                    )
                )
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_COPY_SEARCH = "copy_search"
    }
}
