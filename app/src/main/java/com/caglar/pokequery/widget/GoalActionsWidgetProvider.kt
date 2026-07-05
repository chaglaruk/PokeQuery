package com.caglar.pokequery.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.caglar.pokequery.R

class GoalActionsWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_goal_actions).apply {
                setOnClickPendingIntent(
                    R.id.widget_action_safe_cleanup,
                    openRouteIntent(context, ROUTE_SAFE_CLEANUP, 0x0621)
                )
                setOnClickPendingIntent(
                    R.id.widget_action_candy_prep,
                    openRouteIntent(context, ROUTE_CANDY_PREP, 0x0622)
                )
                setOnClickPendingIntent(
                    R.id.widget_action_assistant,
                    openRouteIntent(context, ROUTE_ASSISTANT, 0x0623)
                )
                setOnClickPendingIntent(
                    R.id.widget_action_event_guide,
                    openRouteIntent(context, ROUTE_EVENTS, 0x0624)
                )
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
