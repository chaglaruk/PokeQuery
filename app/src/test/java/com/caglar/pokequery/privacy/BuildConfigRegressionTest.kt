package com.caglar.pokequery.privacy

import com.caglar.pokequery.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Package 7 Ã¢â‚¬â€ build-config / identity regression tests.
 *
 * Guards the production applicationId and the current version against accidental change.
 */
class BuildConfigRegressionTest {

    @Test
    fun `applicationId remains com dot caglar dot pokequery`() {
        val gradle = listOf(File("app/build.gradle.kts"), File("build.gradle.kts"))
            .first { it.exists() }
            .readText()
        assertTrue(gradle.contains("""applicationId = "com.caglar.pokequery""""))
        assertTrue(gradle.contains("""namespace = "com.caglar.pokequery""""))
    }

    @Test
    fun `version name is accessible and current`() {
        assertTrue("Version name should be non-empty", AppVersion.versionName.isNotBlank())
        assertEquals("0.7.3", AppVersion.versionName)
        assertEquals(23, AppVersion.versionCode)
    }
}
