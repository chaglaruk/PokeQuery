package com.caglar.pokequery.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.caglar.pokequery.MainActivity
import com.caglar.pokequery.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuickAccessWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, buildViews(context.applicationContext)) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val resolvedLang = getResolvedLanguage(context)
        val search = com.caglar.pokequery.domain.engine.StringBuilderEngine.buildGoal("safe_cleanup", language = resolvedLang).rawSyntax
        return RemoteViews(context.packageName, R.layout.widget_quick_access).apply {
            setTextViewText(R.id.widget_search_preview, search)
            setOnClickPendingIntent(R.id.widget_root, openSafeCleanupIntent(context))
            setOnClickPendingIntent(R.id.widget_copy_btn, copySearchIntent(context, search, ROUTE_SAFE_CLEANUP, ROUTE_SAFE_CLEANUP_COPY_REQUEST_CODE))
        }
    }

    private fun openSafeCleanupIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(START_ROUTE_EXTRA, ROUTE_SAFE_CLEANUP)
        }
        return PendingIntent.getActivity(context, ROUTE_SAFE_CLEANUP_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        const val START_ROUTE_EXTRA = "start_route"
        const val ROUTE_SAFE_CLEANUP = "detail_safe_cleanup"
        private const val ROUTE_SAFE_CLEANUP_REQUEST_CODE = 0x0611
        private const val ROUTE_SAFE_CLEANUP_COPY_REQUEST_CODE = 0x0612
    }
}
