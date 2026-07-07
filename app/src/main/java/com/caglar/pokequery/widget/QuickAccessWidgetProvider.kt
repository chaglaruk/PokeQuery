package com.caglar.pokequery.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.caglar.pokequery.MainActivity
import com.caglar.pokequery.R

/**
 * v0.7.1 — Quick Access home screen widget.
 *
 * A static (non-configurable) 2x1 widget. Shows Safe Cleanup search preview.
 * - Tap copy button: copies search to clipboard (if clipboard available), opens app with search ready as fallback
 * - Tap root: opens [MainActivity] routed to the Safe Cleanup review screen via `start_route` extra
 *
 * Safety contract (do not regress):
 *  - The widget NEVER silently writes to clipboard without user action. Copy is explicit tap.
 *  - No widget network work, no analytics, no background work, no periodic updates
 *    (`updatePeriodMillis = 0`). There is nothing for onUpdate to fetch.
 *  - The widget is registered only via the Android app-widget contract (the system
 *    AppWidgetHost binds it); it is not a privileged entry point.
 *
 * Why [onUpdate] still builds the views: the system calls it once when the widget instance is
 * first placed, and on reboot / locale change. Building the views locally (no remote fetch) keeps
 * the tap target wired to the launcher intent. It is a no-op when no instances exist.
 */
class QuickAccessWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val resolvedLang = getResolvedLanguage(context)
        val safeCleanupSearch = com.caglar.pokequery.domain.engine.StringBuilderEngine.buildGoal("safe_cleanup", language = resolvedLang).rawSyntax

        return RemoteViews(context.packageName, R.layout.widget_quick_access).apply {
            // Set dynamic preview text matching actual copied query
            setTextViewText(R.id.widget_search_preview, safeCleanupSearch)

            // Root click -> open Safe Cleanup screen
            setOnClickPendingIntent(
                R.id.widget_root,
                openSafeCleanupIntent(context)
            )
            // Copy button click -> copy search to clipboard, fallback open app
            setOnClickPendingIntent(
                R.id.widget_copy_btn,
                copySearchIntent(context, safeCleanupSearch, ROUTE_SAFE_CLEANUP, 0x0611)
            )
        }
    }

    /**
     * Builds the launcher intent that opens [MainActivity] with `start_route =
     * detail_safe_cleanup`. This mirrors the Safe Cleanup app shortcut, so the widget lands on the
     * same review screen (and Back returns to Home, as for every start route).
     */
    private fun openSafeCleanupIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(START_ROUTE_EXTRA, ROUTE_SAFE_CLEANUP)
        }
        // requestCode is stable per route so re-installing the widget does not stack duplicates.
        return PendingIntent.getActivity(
            context,
            ROUTE_SAFE_CLEANUP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val START_ROUTE_EXTRA = "start_route"
        const val ROUTE_SAFE_CLEANUP = "detail_safe_cleanup"
        private const val ROUTE_SAFE_CLEANUP_REQUEST_CODE = 0x0611
    }
}
