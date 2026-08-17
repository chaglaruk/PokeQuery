package com.caglar.pokequery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Release-version consistency regression tests.
 *
 * The displayed version must derive from a single source of truth and stay aligned with the
 * current Android release declared in app/build.gradle.kts.
 */
class AppVersionTest {

    @Test
    fun `display version matches the v0-dot-7-dot-4 release`() {
        assertEquals("0.7.4", AppVersion.versionName)
        assertEquals(24, AppVersion.versionCode)
    }

    @Test
    fun `display version is never the stale v0-dot-3-dot-4 string`() {
        assertTrue(
            "About must not show stale 'v0.3.4'; got '${AppVersion.versionName}'",
            AppVersion.versionName != "0.3.4"
        )
    }

    @Test
    fun `about display string starts with PokeQuery and includes the version`() {
        val display = AppVersion.aboutDisplayString
        assertTrue("Expected 'PokeQuery' in: $display", display.contains("PokeQuery"))
        assertTrue("Expected '0.7.4' in: $display", display.contains("0.7.4"))
    }
}
