package com.caglar.pokequery.privacy

import com.caglar.pokequery.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Build-config, identity, and privacy configuration regression tests.
 *
 * Guards the production applicationId, current version, configured privacy URL, and narrowly scoped
 * package visibility needed by the tester-feedback email intent against accidental change.
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
        assertEquals("0.7.4", AppVersion.versionName)
        assertEquals(24, AppVersion.versionCode)
    }

    @Test
    fun `privacy policy URL matches configured public HTTPS URL`() {
        assertEquals("https://chaglaruk.github.io/PokeQuery/privacy.html", PrivacyPolicyConfig.URL)
        assertTrue(PrivacyPolicyConfig.URL.startsWith("https://"))
    }

    @Test
    fun `tester feedback mailto handler remains query visible`() {
        val manifest = File("app/src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("<queries>"))
        assertTrue(manifest.contains("""<action android:name="android.intent.action.SENDTO" />"""))
        assertTrue(manifest.contains("""<data android:scheme="mailto" />"""))
    }
}
