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
            val resolvedLang = getResolvedLanguage(context)
            val safeCleanupSearch = com.caglar.pokequery.domain.engine.StringBuilderEngine.buildGoal("safe_cleanup", language = resolvedLang).rawSyntax
            val candyPrepSearch = com.caglar.pokequery.domain.engine.StringBuilderEngine.buildGoal("candy_prep", language = resolvedLang).rawSyntax

            val views = RemoteViews(context.packageName, R.layout.widget_goal_actions).apply {
                // Set dynamic previews matching the actual copied searches
                setTextViewText(R.id.widget_safe_cleanup_preview, safeCleanupSearch)
                setTextViewText(R.id.widget_candy_prep_preview, candyPrepSearch)

                // Safe Cleanup - open action
                setOnClickPendingIntent(
                    R.id.widget_action_safe_cleanup,
                    openRouteIntent(context, ROUTE_SAFE_CLEANUP, 0x0621)
                )
                // Safe Cleanup - copy action
                setOnClickPendingIntent(
                    R.id.widget_action_safe_cleanup_copy,
                    copySearchIntent(context, safeCleanupSearch, ROUTE_SAFE_CLEANUP, 0x0625)
                )
                // Candy Prep - open action
                setOnClickPendingIntent(
                    R.id.widget_action_candy_prep,
                    openRouteIntent(context, ROUTE_CANDY_PREP, 0x0622)
                )
                // Candy Prep - copy action
                setOnClickPendingIntent(
                    R.id.widget_action_candy_prep_copy,
                    copySearchIntent(context, candyPrepSearch, ROUTE_CANDY_PREP, 0x0626)
                )
                // Assistant - open action
                setOnClickPendingIntent(
                    R.id.widget_action_assistant,
                    openRouteIntent(context, ROUTE_ASSISTANT, 0x0623)
                )
                // Event Guide - open action
                setOnClickPendingIntent(
                    R.id.widget_action_event_guide,
                    openRouteIntent(context, ROUTE_EVENTS, 0x0624)
                )
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
