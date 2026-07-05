package com.caglar.pokequery.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.caglar.pokequery.R
import com.caglar.pokequery.domain.events.EventContextRepository
import com.caglar.pokequery.domain.events.selectMainEvent
import com.caglar.pokequery.domain.events.localizedTitle
import com.caglar.pokequery.domain.events.dateLabel
import com.caglar.pokequery.domain.events.localizedName

class EventGuideWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = buildViews(context)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val events = EventContextRepository.entries
        val mainEvent = selectMainEvent(events)

        return RemoteViews(context.packageName, R.layout.widget_event_guide).apply {
            // Event title
            setTextViewText(R.id.widget_event_title, mainEvent?.localizedTitle() ?: context.getString(R.string.widget_event_title))

            // Event date
            mainEvent?.dateLabel()?.let { date ->
                setTextViewText(R.id.widget_event_date, date)
                setViewVisibility(R.id.widget_event_date, android.view.View.VISIBLE)
            } ?: setViewVisibility(R.id.widget_event_date, android.view.View.GONE)

            // Featured Pokémon (first 3)
            mainEvent?.pokemon?.take(3)?.forEachIndexed { index, entry ->
                val resId = when (index) {
                    0 -> R.id.widget_event_poke1
                    1 -> R.id.widget_event_poke2
                    2 -> R.id.widget_event_poke3
                    else -> -1
                }
                if (resId != -1) {
                    val drawableRes = spriteRes(entry.spriteKey)
                    if (drawableRes != 0) {
                        setImageViewResource(resId, drawableRes)
                    }
                }
            }

            // Raid Pokémon
            val raidPokemon = mainEvent?.pokemon?.filter { it.badges?.contains("Raid", ignoreCase = true) == true }
                ?.map { it.localizedName() }
                ?.joinToString(", ")
                ?: mainEvent?.raidsText?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.widget_event_raid_placeholder)

            setTextViewText(R.id.widget_event_raid_names, raidPokemon)

            // Search preview
            mainEvent?.suggestedSearch?.let { search ->
                setTextViewText(R.id.widget_event_search_preview, search)
            }

            // Copy button
            mainEvent?.suggestedSearch?.let { search ->
                setOnClickPendingIntent(
                    R.id.widget_event_copy,
                    copySearchIntent(context, search, ROUTE_EVENTS, 0x0633)
                )
            }

            // Open button
            setOnClickPendingIntent(
                R.id.widget_event_open_action,
                openRouteIntent(context, ROUTE_EVENTS, 0x0631)
            )
            setOnClickPendingIntent(
                R.id.widget_event_root,
                openRouteIntent(context, ROUTE_EVENTS, 0x0632)
            )
        }
    }

    private fun spriteRes(key: String?): Int = when (key) {
        "unown" -> R.drawable.event_unown
        "kangaskhan" -> R.drawable.event_kangaskhan
        "mr_mime" -> R.drawable.event_mr_mime
        "heracross" -> R.drawable.event_heracross
        "corsola" -> R.drawable.event_corsola
        "gimmighoul" -> R.drawable.event_gimmighoul
        "pikachu" -> R.drawable.event_pikachu
        "necrozma" -> R.drawable.event_necrozma
        "eevee" -> R.drawable.event_eevee
        "zeraora" -> R.drawable.event_zeraora
        "wurmple" -> R.drawable.event_wurmple
        "mewtwo" -> R.drawable.event_mewtwo
        else -> 0
    }
}
