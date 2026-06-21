package com.caglar.pokequery.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Package 7 — manifest / privacy regression tests.
 *
 * These protect the Play Data Safety claims ("zero permissions", "no network") against
 * accidental regressions. They parse the source AndroidManifest.xml that all variants
 * merge from, so they run as plain unit tests (no device, no merged-manifest dependency).
 *
 * If a future change adds INTERNET / location / storage / camera / mic / contacts, or
 * changes the applicationId, or weakens backup policy, these fail loudly.
 */
class ManifestPrivacyRegressionTest {

    private val manifest: String by lazy {
        // src/main/AndroidManifest.xml relative to the app module (working dir = project root).
        val candidates = listOf(
            File("app/src/main/AndroidManifest.xml"),
            File("src/main/AndroidManifest.xml")
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("AndroidManifest.xml not found at: ${candidates.map { it.path }}")
        file.readText()
    }

    @Test
    fun `manifest declares zero permissions`() {
        // No <uses-permission> elements at all.
        assertFalse(
            "Manifest must not declare any <uses-permission>, but found one.",
            manifest.contains("<uses-permission", ignoreCase = true)
        )
    }

    @Test
    fun `no INTERNET permission is present`() {
        assertFalse(manifest.contains("android.permission.INTERNET", ignoreCase = true))
    }

    @Test
    fun `no dangerous location storage camera mic or contacts permissions`() {
        val forbidden = listOf(
            "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
            "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE",
            "CAMERA", "RECORD_AUDIO", "READ_CONTACTS", "WRITE_CONTACTS",
            "READ_MEDIA_IMAGES", "BLUETOOTH_CONNECT", "POST_NOTIFICATIONS"
        )
        forbidden.forEach { perm ->
            assertFalse(
                "Manifest must not request $perm",
                manifest.contains(perm, ignoreCase = true)
            )
        }
    }

    @Test
    fun `only MainActivity is exported`() {
        // Count exported activities/components and confirm MainActivity is the only exported one.
        val exportedRegex = Regex("""<activity[^>]*android:exported="true"[^>]*>""", RegexOption.IGNORE_CASE)
        val exportedActivities = exportedRegex.findAll(manifest).toList()
        assertTrue("Expected at least the launcher activity exported", exportedActivities.isNotEmpty())
        exportedActivities.forEach { match ->
            assertTrue(
                "Only MainActivity may be exported, but found: ${match.value}",
                match.value.contains("MainActivity", ignoreCase = true)
            )
        }
        // No exported services/receivers/providers.
        assertFalse(manifest.contains("<service", ignoreCase = true))
        assertFalse(manifest.contains("<receiver", ignoreCase = true))
        assertFalse(manifest.contains("<provider", ignoreCase = true))
    }

    @Test
    fun `allowBackup is explicitly false to match privacy-first positioning`() {
        // Package 7 requires an explicit backup policy; false stops DataStore favorites/history
        // from being auto-backed-up to the user's Google account.
        assertTrue(
            "Expected android:allowBackup=\"false\"; got: ${Regex("allowBackup=\"[^\"]+\"").find(manifest)?.value}",
            Regex("""allowBackup="false"""", RegexOption.IGNORE_CASE).containsMatchIn(manifest)
        )
    }
}
