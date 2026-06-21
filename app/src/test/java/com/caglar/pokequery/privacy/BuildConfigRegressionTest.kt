package com.caglar.pokequery.privacy

import com.caglar.pokequery.AppVersion
import com.caglar.pokequery.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Package 7 — build-config / identity regression tests.
 *
 * Guards the production applicationId and the current version against accidental change.
 */
class BuildConfigRegressionTest {

    @Test
    fun `applicationId remains com dot caglar dot pokequery`() {
        assertEquals("com.caglar.pokequery", BuildConfig.APPLICATION_ID)
    }

    @Test
    fun `version name is accessible and current`() {
        // BuildConfig.VERSION_NAME is generated from defaultConfig.versionName.
        assertTrue("Version name should be non-empty", BuildConfig.VERSION_NAME.isNotBlank())
        assertEquals(BuildConfig.VERSION_NAME, AppVersion.versionName)
    }
}
