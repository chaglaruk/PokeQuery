package com.caglar.pokequery.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.caglar.pokequery.MainActivity

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
