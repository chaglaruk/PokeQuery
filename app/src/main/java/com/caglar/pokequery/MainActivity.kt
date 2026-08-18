package com.caglar.pokequery

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.events.EventFeedLoader
import com.caglar.pokequery.domain.locale.AppLocaleController
import com.caglar.pokequery.domain.locale.LocalizationModel
import com.caglar.pokequery.theme.PokeQueryTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var startRoute by mutableStateOf<String?>(null)
    private var copySearch by mutableStateOf<String?>(null)
    private var debugAppLanguage by mutableStateOf<String?>(null)
    private var debugSearchLanguage by mutableStateOf<String?>(null)
    private var debugEventFeedUrl by mutableStateOf<String?>(null)
    private var navigationIntentVersion by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        startRoute = readStartRoute(intent)
        copySearch = readDebugCopySearch(intent)
        debugAppLanguage = readDebugAppLanguage(intent)
        debugSearchLanguage = readDebugSearchLanguage(intent)
        debugEventFeedUrl = intent?.getStringExtra("event_feed_url")

        lifecycleScope.launch {
            runCatching { EventFeedLoader.load(applicationContext) }
        }

        val repository = UserPreferencesRepository(applicationContext.dataStore)
        setContent {
            val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
            if (userPrefs == null) return@setContent

            val appLanguage = userPrefs!!.appLanguage
            val requestedDebugLanguage by rememberUpdatedState(debugAppLanguage)
            val requestedDebugSearchLanguage by rememberUpdatedState(debugSearchLanguage)

            LaunchedEffect(requestedDebugLanguage) {
                val requested = normalizeDebugLanguage(requestedDebugLanguage)
                if (BuildConfig.DEBUG && requested != null && requested in AppLocaleController.OPTIONS) {
                    repository.setSetting(UserPreferencesRepository.APP_LANGUAGE, requested)
                }
            }
            LaunchedEffect(requestedDebugSearchLanguage) {
                val requested = normalizeDebugSearchLanguage(requestedDebugSearchLanguage)
                if (BuildConfig.DEBUG && requested != null && requested in LocalizationModel.SearchStringLanguage.OPTIONS) {
                    repository.setSetting(UserPreferencesRepository.GAME_LANGUAGE, requested)
                }
            }

            val context = androidx.compose.ui.platform.LocalContext.current
            // Read the Activity configuration before installing PokeQuery's localized context.
            // This value updates when Android changes the device language while the process stays
            // alive, which keeps System Default truly live without LocaleManager recreation.
            val baseConfiguration = androidx.compose.ui.platform.LocalConfiguration.current
            val deviceLocale = baseConfiguration.locales[0]
            val deviceLocaleTag = deviceLocale.toLanguageTag()
            val locale = AppLocaleController.localeFor(appLanguage, deviceLocale)
            val configuration = android.content.res.Configuration(baseConfiguration)
            configuration.setLocale(locale)
            val localizedContext = context.createConfigurationContext(configuration)

            LaunchedEffect(appLanguage, deviceLocaleTag) {
                AppLocaleController.apply(applicationContext, appLanguage, deviceLocale)
            }

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localizedContext,
                androidx.compose.ui.platform.LocalConfiguration provides configuration
            ) {
                PokeQueryTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        MainNavigation(
                            startRoute = startRoute,
                            copySearch = copySearch,
                            debugEventFeedUrl = if (BuildConfig.DEBUG) debugEventFeedUrl else null,
                            navigationIntentVersion = navigationIntentVersion,
                            onCopyHandled = { copySearch = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startRoute = readStartRoute(intent)
        copySearch = readDebugCopySearch(intent)
        debugAppLanguage = readDebugAppLanguage(intent)
        debugSearchLanguage = readDebugSearchLanguage(intent)
        debugEventFeedUrl = intent.getStringExtra("event_feed_url")
        navigationIntentVersion += 1
    }

    private fun readStartRoute(intent: Intent?): String? =
        intent?.getStringExtra(START_ROUTE_EXTRA)

    /**
     * Production widget copy no longer travels through exported MainActivity. Keep this legacy
     * hook debug-only for local/E2E tooling so a foreign release app cannot overwrite clipboard
     * or pollute History by forging an explicit launcher intent.
     */
    private fun readDebugCopySearch(intent: Intent?): String? =
        if (BuildConfig.DEBUG) intent?.getStringExtra("copy_search") else null

    private fun readDebugAppLanguage(intent: Intent?): String? =
        intent?.getStringExtra(DEBUG_APP_LANGUAGE_EXTRA)

    private fun readDebugSearchLanguage(intent: Intent?): String? =
        intent?.getStringExtra(DEBUG_SEARCH_LANGUAGE_EXTRA)

    private fun normalizeDebugLanguage(value: String?): String? = when (value?.trim()?.lowercase()) {
        "system", "default", "system_default" -> AppLocaleController.SYSTEM_DEFAULT
        "en" -> AppLocaleController.ENGLISH
        "tr" -> AppLocaleController.TURKISH
        "de" -> AppLocaleController.DEUTSCH
        "es" -> AppLocaleController.ESPANOL
        "fr" -> AppLocaleController.FRANCAIS
        "it" -> AppLocaleController.ITALIANO
        else -> value
    }

    private fun normalizeDebugSearchLanguage(value: String?): String? = when (value?.trim()?.lowercase()) {
        "auto" -> LocalizationModel.SearchStringLanguage.AUTO_SAFE
        "match" -> LocalizationModel.SearchStringLanguage.MATCH_APP
        "en" -> LocalizationModel.SearchStringLanguage.ENGLISH
        "de" -> LocalizationModel.SearchStringLanguage.GERMAN
        "es" -> LocalizationModel.SearchStringLanguage.SPANISH
        "fr" -> LocalizationModel.SearchStringLanguage.FRENCH
        "it" -> LocalizationModel.SearchStringLanguage.ITALIAN
        "tr" -> LocalizationModel.SearchStringLanguage.TURKISH
        else -> value
    }

    companion object {
        const val START_ROUTE_EXTRA = "start_route"
        const val DEBUG_APP_LANGUAGE_EXTRA = "app_language"
        const val DEBUG_SEARCH_LANGUAGE_EXTRA = "search_language"
    }
}
