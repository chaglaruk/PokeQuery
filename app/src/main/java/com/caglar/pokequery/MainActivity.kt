package com.caglar.pokequery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
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
    // Language preference. We do NOT block on a synchronous DataStore read — on API 33+
    // the platform LocaleManager persists the per-app locale across launches, so the OS
    // has already restored it before this frame. The SideEffect below keeps it in sync if
    // the user changes App Language at runtime.
    val repository = UserPreferencesRepository(applicationContext.dataStore)
    setContent {
      val startRoute = intent.getStringExtra("start_route")
      val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
      val appLanguage = userPrefs?.appLanguage ?: "System Default"
      SideEffect { AppLocaleController.apply(applicationContext, appLanguage) }
      PokeQueryTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation(startRoute) } }
    }
  }
}

