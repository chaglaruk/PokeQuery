package com.caglar.pokequery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.4.2 safety patch (Fix 5) — version consistency.
 *
 * Audit finding (BUG-008/015): Settings/About hardcoded "v0.3.4" while the app was at
 * a different version, and docs were stale. The displayed version must derive from a
 * single source of truth (BuildConfig.VERSION_NAME), and must equal the current release.
 */
class AppVersionTest {

    @Test
    fun `display version matches the v0-dot-4-dot-3 release`() {
        assertEquals("0.4.3", AppVersion.versionName)
    }

    @Test
    fun `display version is never the stale v0-dot-3-dot-4 string`() {
        // Regression guard for BUG-008: About must never show the old hardcoded value.
        assertTrue(
            "About must not show stale 'v0.3.4'; got '${AppVersion.versionName}'",
            AppVersion.versionName != "0.3.4"
        )
    }

    @Test
    fun `about display string starts with PokeQuery and includes the version`() {
        val display = AppVersion.aboutDisplayString
        assertTrue("Expected 'PokeQuery' in: $display", display.contains("PokeQuery"))
        assertTrue("Expected '0.4.3' in: $display", display.contains("0.4.3"))
    }
}
