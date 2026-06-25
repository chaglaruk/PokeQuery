package com.caglar.pokequery.domain.changelog

import com.caglar.pokequery.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogTest {

    @Test
    fun `current app version matches latest changelog entry`() {
        val latest = Changelog.entries.first()
        assertTrue(latest.isCurrent)
        assertEquals(AppVersion.versionName, latest.versionName)
        assertEquals(AppVersion.versionCode, latest.versionCode)
    }

    @Test
    fun `version codes are unique and descending`() {
        val codes = Changelog.entries.map { it.versionCode }
        assertEquals(codes.size, codes.distinct().size)
        assertEquals(codes.sortedDescending(), codes)
    }

    @Test
    fun `every entry has highlights and exactly one is current`() {
        assertEquals(1, Changelog.entries.count { it.isCurrent })
        Changelog.entries.forEach { entry ->
            assertTrue("Missing highlight for ${entry.versionName}", entry.highlights.isNotEmpty())
        }
    }
}
