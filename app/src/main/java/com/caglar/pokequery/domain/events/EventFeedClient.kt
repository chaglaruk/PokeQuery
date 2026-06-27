package com.caglar.pokequery.domain.events

import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class EventFeed(
    val notes: List<EventFeedEntry> = emptyList(),
    val monthlyNote: MonthlyFeedEntry? = null,
    val fetchedAt: Long = 0L
)

data class EventFeedEntry(
    val id: String,
    val title: String,
    val contextType: String = "GENERIC_EVENT",
    val note: String
)

data class MonthlyFeedEntry(
    val month: Int,
    val year: Int,
    val title: String,
    val contextType: String = "COMMUNITY_DAY",
    val pokemonName: String? = null,
    val note: String
)

object EventFeedClient {
    private const val CACHE_FILENAME = "event_feed_cache.json"
    private const val CACHE_TTL_MS = 24L * 60 * 60 * 1000

    var feedUrl: String = "https://raw.githubusercontent.com/caglardinc/pokequery-events/main/events.json"

    fun isCachedFresh(cacheDir: File): Boolean {
        val cacheFile = File(cacheDir, CACHE_FILENAME)
        if (!cacheFile.exists()) return false
        val age = System.currentTimeMillis() - cacheFile.lastModified()
        return age < CACHE_TTL_MS
    }

    fun readCached(cacheDir: File): EventFeed? {
        val cacheFile = File(cacheDir, CACHE_FILENAME)
        if (!cacheFile.exists()) return null
        return try {
            parseFeed(cacheFile.readText())
        } catch (_: Exception) { null }
    }

    private const val MAX_BODY_BYTES = 512_000

    fun fetch(cacheDir: File): Result<EventFeed> {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(feedUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return Result.failure(RuntimeException("HTTP $responseCode from event feed"))
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            if (body.length > MAX_BODY_BYTES) {
                return Result.failure(RuntimeException("Event feed body too large (${body.length} bytes)"))
            }
            val feed = parseFeed(body).copy(fetchedAt = System.currentTimeMillis())
            val cacheFile = File(cacheDir, CACHE_FILENAME)
            cacheFile.parentFile?.mkdirs()
            cacheFile.writeText(encodeFeed(feed))
            Result.success(feed)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    fun clearCache(cacheDir: File) {
        File(cacheDir, CACHE_FILENAME).delete()
    }

    private fun parseFeed(json: String): EventFeed {
        val root = JSONObject(json)
        val notes = root.optJSONArray("notes")?.let { arr ->
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                EventFeedEntry(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    contextType = obj.optString("contextType", "GENERIC_EVENT"),
                    note = obj.getString("note")
                )
            }
        } ?: emptyList()
        val monthly = root.optJSONObject("monthlyNote")?.let { obj ->
            MonthlyFeedEntry(
                month = obj.getInt("month"),
                year = obj.getInt("year"),
                title = obj.getString("title"),
                contextType = obj.optString("contextType", "COMMUNITY_DAY"),
                pokemonName = if (obj.has("pokemonName") && !obj.isNull("pokemonName")) obj.getString("pokemonName") else null,
                note = obj.getString("note")
            )
        }
        val fetchedAt = root.optLong("fetchedAt", 0L)
        return EventFeed(notes = notes, monthlyNote = monthly, fetchedAt = fetchedAt)
    }

    private fun encodeFeed(feed: EventFeed): String {
        val root = JSONObject()
        root.put("fetchedAt", feed.fetchedAt)
        val notesArr = org.json.JSONArray()
        feed.notes.forEach { entry ->
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("title", entry.title)
            obj.put("contextType", entry.contextType)
            obj.put("note", entry.note)
            notesArr.put(obj)
        }
        root.put("notes", notesArr)
        feed.monthlyNote?.let { m ->
            val obj = JSONObject()
            obj.put("month", m.month)
            obj.put("year", m.year)
            obj.put("title", m.title)
            obj.put("contextType", m.contextType)
            m.pokemonName?.let { obj.put("pokemonName", it) }
            obj.put("note", m.note)
            root.put("monthlyNote", obj)
        }
        return root.toString()
    }
}
