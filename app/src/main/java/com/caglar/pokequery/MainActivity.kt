package com.caglar.pokequery

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
import androidx.compose.ui.Modifier
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.domain.locale.AppLocaleController
import com.caglar.pokequery.theme.PokeQueryTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    // v0.5.2 (Fix 7): one repository instance shared with MainNavigation for the App
    // Language preference. The v0.5.2 original applied the OS per-app locale from a
    // `SideEffect` (per-frame), which on Android 16 / Samsung One UI drove an Activity
    // recreation loop and a permanent black screen. v0.5.2.1 hotfix: AppLocaleController now
    // sets only the in-process default locale (no LocaleManager, no recreation), and we
    // invoke it per-change via LaunchedEffect(appLanguage) so neither the call cadence nor
    // the call itself can loop. Search String Language (Layer B) is unaffected.
    val repository = UserPreferencesRepository(applicationContext.dataStore)
    setContent {
      val startRoute = intent.getStringExtra("start_route")
      val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
      val appLanguage = userPrefs?.appLanguage ?: "System Default"
      LaunchedEffect(appLanguage) { AppLocaleController.apply(applicationContext, appLanguage) }
      PokeQueryTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation(startRoute) } }
    }
  }
}

