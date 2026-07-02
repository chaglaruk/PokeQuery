package com.caglar.pokequery.domain.events

import android.content.Context
import com.caglar.pokequery.BuildConfig
import com.caglar.pokequery.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object EventFeedConfig {
    const val PRODUCTION_URL = "https://raw.githubusercontent.com/chaglaruk/PokeQuery/master/docs/event-feed/pokequery-events.json"
    const val CACHE_FILE = "event_context_feed.json"
}

interface EventDataProvider {
    suspend fun fetch(): Result<String>
}

class HttpEventDataProvider(private val url: String = EventFeedConfig.PRODUCTION_URL) : EventDataProvider {
    override suspend fun fetch(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            if (code !in 200..299) error("Event feed unavailable")
            connection.inputStream.bufferedReader().use { it.readText() }
        }
    }
}

class RawEventDataProvider(private val context: Context) : EventDataProvider {
    override suspend fun fetch(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            context.resources.openRawResource(R.raw.event_context_fixture).bufferedReader().use { it.readText() }
        }
    }
}

data class EventFeed(
    val lastUpdated: String,
    val events: List<EventContext>
)

object EventFeedParser {
    fun parse(json: String): Result<EventFeed> = runCatching {
        val schema = intField(json, "schemaVersion")
        require(schema == 1) { "unsupported schema" }
        val lastUpdated = stringField(json, "lastUpdated")
        require(lastUpdated.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) { "bad lastUpdated" }
        val eventsBody = Regex(""""events"\s*:\s*\[(.*)]""", RegexOption.DOT_MATCHES_ALL)
            .find(json)?.groupValues?.get(1) ?: error("missing events")
        val events = Regex("""\{([^{}]+)}""").findAll(eventsBody).map { match ->
            val body = match.groupValues[1]
            EventContext(
                id = stringField(body, "id"),
                titleText = stringField(body, "title"),
                contextType = EventContextType.valueOf(stringField(body, "kind")),
                noteText = stringField(body, "note"),
                month = intField(body, "month"),
                year = intField(body, "year"),
                isManual = false
            )
        }.toList()
        require(events.isNotEmpty()) { "empty events" }
        require(events.all { it.id.isNotBlank() && it.titleText?.isNotBlank() == true && it.noteText?.isNotBlank() == true }) {
            "blank event field"
        }
        EventFeed(lastUpdated, events)
    }

    private fun stringField(json: String, name: String): String =
        Regex(""""$name"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)?.trim()
            ?: error("missing $name")

    private fun intField(json: String, name: String): Int =
        Regex(""""$name"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toInt()
            ?: error("missing $name")
}

object EventFeedCache {
    fun file(context: Context): File = File(context.cacheDir, EventFeedConfig.CACHE_FILE)

    suspend fun read(context: Context): EventFeed? = withContext(Dispatchers.IO) {
        val file = file(context)
        if (!file.exists()) return@withContext null
        EventFeedParser.parse(file.readText()).getOrNull()
    }

    suspend fun write(context: Context, json: String) = withContext(Dispatchers.IO) {
        file(context).writeText(json)
    }
}

object EventFeedLoader {
    fun defaultProvider(context: Context): EventDataProvider =
        HttpEventDataProvider()

    suspend fun load(
        context: Context,
        provider: EventDataProvider = defaultProvider(context),
        preferCachedOnFailure: Boolean = true
    ): ContextFeedState {
        val manualMonthly = MonthlyContextRepository.current
        val fetched = provider.fetch()
        if (fetched.isSuccess) {
            val json = fetched.getOrThrow()
            val parsed = EventFeedParser.parse(json)
            if (parsed.isSuccess) {
                EventFeedCache.write(context, json)
                val feed = parsed.getOrThrow()
                return ContextFeedState.Online(manualMonthly, feed.events, feed.lastUpdated)
            }
            val cached = if (preferCachedOnFailure) EventFeedCache.read(context) else null
            return decideAfterParseFailure(manualMonthly, cached)
        }
        val cached = if (preferCachedOnFailure) EventFeedCache.read(context) else null
        val bundled = if (cached == null) bundledFallback(context) else null
        return decideAfterFetchFailure(manualMonthly, cached, bundled)
    }

    /**
     * Pure state-machine decision after a successful fetch that failed to parse. Exposed so the
     * Invalid-vs-StaleCache branch is unit-testable without an Android `Context` (the disk cache
     * is passed in as data, not read inside this function).
     */
    fun decideAfterParseFailure(
        manualMonthly: MonthlyContext?,
        cached: EventFeed?
    ): ContextFeedState =
        if (cached != null) {
            ContextFeedState.StaleCache(manualMonthly, cached.events, cached.lastUpdated)
        } else {
            ContextFeedState.Invalid(manualMonthly)
        }

    /**
     * Pure state-machine decision after a network failure. Falls back to the cached feed when one
     * exists (StaleCache), otherwise the honest OfflineOnly state. Testable without `Context`.
     */
    fun decideAfterFetchFailure(
        manualMonthly: MonthlyContext?,
        cached: EventFeed?,
        bundled: EventFeed? = null
    ): ContextFeedState =
        if (cached != null) {
            ContextFeedState.StaleCache(manualMonthly, cached.events, cached.lastUpdated)
        } else if (bundled != null) {
            ContextFeedState.OfflineOnly(manualMonthly, bundled.events)
        } else {
            ContextFeedState.OfflineOnly(manualMonthly)
        }

    private suspend fun bundledFallback(context: Context): EventFeed? {
        val json = RawEventDataProvider(context).fetch().getOrNull() ?: return null
        return EventFeedParser.parse(json).getOrNull()
    }
}
