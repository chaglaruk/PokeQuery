package com.caglar.pokequery.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.caglar.pokequery.R

class EventGuideWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_event_guide).apply {
                setOnClickPendingIntent(
                    R.id.widget_event_root,
                    openRouteIntent(context, ROUTE_EVENTS, 0x0631)
                )
                setOnClickPendingIntent(
                    R.id.widget_event_open_action,
                    openRouteIntent(context, ROUTE_EVENTS, 0x0632)
                )
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
