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
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.locale.AppLocaleController
import com.caglar.pokequery.domain.locale.LocalizationModel
import com.caglar.pokequery.theme.PokeQueryTheme

class MainActivity : ComponentActivity() {

    // v0.6.1: the start_route from the launcher intent (app shortcut / Quick Access widget).
    // Held as observable state so re-entry via onNewIntent (the activity already running) also
    // re-routes instead of being ignored. `null` means "no specific route / go Home".
    private var startRoute by mutableStateOf<String?>(null)
    private var copySearch by mutableStateOf<String?>(null)
    private var debugAppLanguage by mutableStateOf<String?>(null)
    private var debugSearchLanguage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        startRoute = readStartRoute(intent)
        copySearch = intent?.getStringExtra("copy_search")
        debugAppLanguage = readDebugAppLanguage(intent)
        debugSearchLanguage = readDebugSearchLanguage(intent)
        // v0.5.2 (Fix 7): one repository instance shared with MainNavigation for the App
        // Language preference. The v0.5.2 original applied the OS per-app locale from a
        // `SideEffect` (per-frame), which on Android 16 / Samsung One UI drove an Activity
        // recreation loop and a permanent black screen. v0.5.2.1 hotfix: AppLocaleController now
        // sets only the in-process default locale (no LocaleManager, no recreation), and we
        // invoke it per-change via LaunchedEffect(appLanguage) so neither the call cadence nor
        // the call itself can loop. Search String Language (Layer B) is unaffected.
        val repository = UserPreferencesRepository(applicationContext.dataStore)
        setContent {
            val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
            if (userPrefs == null) {
                // Wait for preferences to load from DataStore to avoid resetting locale to System Default
                return@setContent
            }

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

            // Deterministic in-app localization layer
            val context = androidx.compose.ui.platform.LocalContext.current
            val locale = AppLocaleController.localeFor(appLanguage)
            val configuration = android.content.res.Configuration(androidx.compose.ui.platform.LocalConfiguration.current)
            configuration.setLocale(locale)
            val localizedContext = context.createConfigurationContext(configuration)

            LaunchedEffect(appLanguage) { AppLocaleController.apply(applicationContext, appLanguage) }

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localizedContext,
                androidx.compose.ui.platform.LocalConfiguration provides configuration
            ) {
                PokeQueryTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        // Keyed on startRoute so a new shortcut/widget intent recomposes navigation.
                        MainNavigation(
                            startRoute = startRoute,
                            copySearch = copySearch,
                            onCopyHandled = { copySearch = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // v0.6.1: pick up a fresh start_route when the activity is already running (e.g. the user
        // taps a different shortcut, or the Quick Access widget while PokeQuery is in the back
        // stack). setIntent so getIntent() also reflects the latest.
        setIntent(intent)
        startRoute = readStartRoute(intent)
        copySearch = intent.getStringExtra("copy_search")
        debugAppLanguage = readDebugAppLanguage(intent)
        debugSearchLanguage = readDebugSearchLanguage(intent)
    }

    private fun readStartRoute(intent: Intent?): String? =
        intent?.getStringExtra(START_ROUTE_EXTRA)

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
