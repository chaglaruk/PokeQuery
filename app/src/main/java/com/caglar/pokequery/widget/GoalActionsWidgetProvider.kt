package com.caglar.pokequery.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.caglar.pokequery.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GoalActionsWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val resolvedLang = getResolvedLanguage(appContext)
                val safeCleanupSearch = com.caglar.pokequery.domain.engine.StringBuilderEngine.buildGoal("safe_cleanup", language = resolvedLang).rawSyntax
                val candyPrepSearch = com.caglar.pokequery.domain.engine.StringBuilderEngine.buildGoal("candy_prep", language = resolvedLang).rawSyntax

                appWidgetIds.forEach { id ->
                    val views = RemoteViews(appContext.packageName, R.layout.widget_goal_actions).apply {
                        setTextViewText(R.id.widget_safe_cleanup_preview, safeCleanupSearch)
                        setTextViewText(R.id.widget_candy_prep_preview, candyPrepSearch)
                        setOnClickPendingIntent(R.id.widget_action_safe_cleanup, openRouteIntent(appContext, ROUTE_SAFE_CLEANUP, 0x0621))
                        setOnClickPendingIntent(R.id.widget_action_safe_cleanup_copy, copySearchIntent(appContext, safeCleanupSearch, ROUTE_SAFE_CLEANUP, 0x0625))
                        setOnClickPendingIntent(R.id.widget_action_candy_prep, openRouteIntent(appContext, ROUTE_CANDY_PREP, 0x0622))
                        setOnClickPendingIntent(R.id.widget_action_candy_prep_copy, copySearchIntent(appContext, candyPrepSearch, ROUTE_CANDY_PREP, 0x0626))
                        setOnClickPendingIntent(R.id.widget_action_assistant, openRouteIntent(appContext, ROUTE_ASSISTANT, 0x0623))
                        setOnClickPendingIntent(R.id.widget_action_event_guide, openRouteIntent(appContext, ROUTE_EVENTS, 0x0624))
                    }
                    appWidgetManager.updateAppWidget(id, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
