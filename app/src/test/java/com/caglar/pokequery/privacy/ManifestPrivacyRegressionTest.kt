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
    fun `only MainActivity is exported and no services providers or extra receivers`() {
        // Count exported activities/components and confirm MainActivity is the only exported
        // activity. The v0.6.1 Quick Access widget receiver is the ONE allowed exported
        // receiver: the Android app-widget contract requires exported="true" so the system
        // AppWidgetHost can bind it. It is allowlisted below and must carry NO permission.
        val exportedActivities = Regex(
            """<activity[^>]*android:exported="true"[^>]*>""", RegexOption.IGNORE_CASE
        ).findAll(manifest).toList()
        assertTrue("Expected at least the launcher activity exported", exportedActivities.isNotEmpty())
        exportedActivities.forEach { match ->
            assertTrue(
                "Only MainActivity may be exported, but found: ${match.value}",
                match.value.contains("MainActivity", ignoreCase = true)
            )
        }

        // No services or providers ever.
        assertFalse(manifest.contains("<service", ignoreCase = true))
        assertFalse(manifest.contains("<provider", ignoreCase = true))

        // Receivers: only the v0.6.1 Quick Access widget AppWidgetProvider is allowed. The
        // receiver opening tag spans multiple lines, so use [^>]* (which matches newlines) like
        // the activity regex above rather than `.*?`.
        val receivers = Regex("""<receiver\b[^>]*>""", RegexOption.IGNORE_CASE)
            .findAll(manifest).map { it.value }.toList()
        assertTrue(
            "Expected exactly one receiver (the Quick Access widget), found ${receivers.size}: $receivers",
            receivers.size == 1
        )
        val receiver = receivers.single()
        assertTrue(
            "The only receiver must be the Quick Access widget provider, got: $receiver",
            receiver.contains("QuickAccessWidgetProvider", ignoreCase = true)
        )
        // The widget must register the appwidget system intent-filter and must NOT declare any
        // android:permission (so it cannot be used as a privileged entry point).
        assertTrue(
            "Widget must register the APPWIDGET_UPDATE system action",
            manifest.contains("android.appwidget.action.APPWIDGET_UPDATE", ignoreCase = true)
        )
        assertFalse(
            "Widget receiver must not declare any android:permission",
            manifest.contains("android:permission", ignoreCase = true)
        )
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

    @Test
    fun `dataExtractionRules is linked in manifest`() {
        // API 31+ cloud-backup and device-transfer rules must be wired so the system reads them.
        assertTrue(
            "Expected android:dataExtractionRules=\"@xml/data_extraction_rules\" in manifest",
            manifest.contains("dataExtractionRules", ignoreCase = true)
        )
    }

    @Test
    fun `fullBackupContent is linked in manifest`() {
        // Pre-API-31 backup rules must be wired so the system reads them.
        assertTrue(
            "Expected android:fullBackupContent=\"@xml/backup_rules\" in manifest",
            manifest.contains("fullBackupContent", ignoreCase = true)
        )
    }
}

