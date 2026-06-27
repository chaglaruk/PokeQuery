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
 * These protect the Play Data Safety claims against accidental regressions.
 * They parse the source AndroidManifest.xml that all variants merge from, so they run as
 * plain unit tests (no device, no merged-manifest dependency).
 *
 * v0.6.2: INTERNET permission is now declared for the optional daily event feed (opt-in only).
 * No other permissions are allowed. See the manifest comment for the rationale.
 *
 * If a future change adds other permissions (location / storage / camera / mic / contacts), or
 * changes the applicationId, or weakens backup policy, these fail loudly.
 */
class ManifestPrivacyRegressionTest {

    private val manifest: String by lazy {
        val candidates = listOf(
            File("app/src/main/AndroidManifest.xml"),
            File("src/main/AndroidManifest.xml")
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("AndroidManifest.xml not found at: ${candidates.map { it.path }}")
        file.readText()
    }

    @Test
    fun `only INTERNET permission is declared`() {
        // v0.6.2: INTERNET is allowed for the optional daily event feed (opt-in).
        // Verify it's present and that no OTHER permissions exist.
        val permissionCount = Regex("""<uses-permission""", RegexOption.IGNORE_CASE).findAll(manifest).count()
        assertEquals(
            "Expected exactly one <uses-permission> (INTERNET), found $permissionCount",
            1, permissionCount
        )
        assertTrue(
            "The one permission must be INTERNET",
            manifest.contains("android.permission.INTERNET", ignoreCase = true)
        )
    }

    @Test
    fun `internet permission has documented rationale`() {
        val lines = manifest.lines()
        val idx = lines.indexOfFirst { it.contains("android.permission.INTERNET", ignoreCase = true) }
        assertTrue("INTERNET permission line not found", idx >= 0)
        val preamble = lines.subList(0, idx).joinToString("\n")
        assertTrue(
            "INTERNET permission must have a rationale comment. Manifest preamble:\n$preamble",
            preamble.contains("v0.6.2", ignoreCase = true) || preamble.contains("event feed", ignoreCase = true)
        )
    }

    @Test
    fun `no dangerous location storage camera mic contacts or other permissions beyond INTERNET`() {
        val allowed = listOf("INTERNET")
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
        // v0.6.2: INTERNET is the only declared permission.
        val manifested = Regex("""android\.permission\.(\w+)""", RegexOption.IGNORE_CASE)
            .findAll(manifest).map { it.groupValues[1] }.toSet()
        manifested.forEach { perm ->
            assertTrue(
                "Permission $perm is not in the allowed list: $allowed",
                perm in allowed
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

