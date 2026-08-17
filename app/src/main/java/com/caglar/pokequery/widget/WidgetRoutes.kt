package com.caglar.pokequery.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.caglar.pokequery.MainActivity
import com.caglar.pokequery.data.repository.dataStore
import kotlinx.coroutines.flow.first

internal const val START_ROUTE_EXTRA = "start_route"
internal const val ROUTE_SAFE_CLEANUP = "detail_safe_cleanup"
internal const val ROUTE_CANDY_PREP = "detail_candy_prep"
internal const val ROUTE_ASSISTANT = "assistant"
internal const val ROUTE_EVENTS = "events"

internal fun openRouteIntent(context: Context, route: String, requestCode: Int): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra(START_ROUTE_EXTRA, route)
    }
    return PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

/**
 * Clipboard writes use a dedicated non-exported Activity rather than MainActivity extras.
 * This keeps the widget action explicit while preventing another app from forging a
 * `copy_search` launch intent against the exported launcher activity.
 */
internal fun copySearchIntent(context: Context, search: String, route: String, requestCode: Int): PendingIntent {
    val intent = Intent(context, WidgetCopyActivity::class.java).apply {
        action = "com.caglar.pokequery.action.WIDGET_COPY"
        putExtra(START_ROUTE_EXTRA, route)
        putExtra(WidgetCopyActivity.EXTRA_COPY_SEARCH, search)
    }
    return PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

internal fun getResolvedLanguage(context: Context): String {
    return try {
        val repository = com.caglar.pokequery.data.repository.UserPreferencesRepository(context.dataStore)
        val userPrefs = kotlinx.coroutines.runBlocking { repository.userPreferencesFlow.first() }
        com.caglar.pokequery.domain.locale.LocalizationModel.SearchStringLanguage.resolve(userPrefs.gameLanguage, userPrefs.appLanguage)
    } catch (e: Exception) {
        "English"
    }
}
