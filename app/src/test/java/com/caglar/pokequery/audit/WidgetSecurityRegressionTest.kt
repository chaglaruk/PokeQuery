package com.caglar.pokequery.audit

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WidgetSecurityRegressionTest {
    private fun read(vararg candidates: String): String =
        candidates.map(::File).first { it.exists() }.readText()

    @Test
    fun `widget copy target remains non exported`() {
        val manifest = read("app/src/main/AndroidManifest.xml", "src/main/AndroidManifest.xml")
        val widgetCopy = Regex(
            """<activity\s+android:name="\.widget\.WidgetCopyActivity"[\s\S]*?/>"""
        ).find(manifest)?.value.orEmpty()
        assertTrue("WidgetCopyActivity must remain declared", widgetCopy.isNotBlank())
        assertTrue("WidgetCopyActivity must remain non-exported", widgetCopy.contains("android:exported=\"false\""))
    }

    @Test
    fun `production launcher does not trust copy search extra`() {
        val mainActivity = read(
            "app/src/main/java/com/caglar/pokequery/MainActivity.kt",
            "src/main/java/com/caglar/pokequery/MainActivity.kt"
        )
        assertTrue(mainActivity.contains("if (BuildConfig.DEBUG) intent?.getStringExtra(\"copy_search\") else null"))
    }

    @Test
    fun `quick widget open and copy use distinct request codes`() {
        val provider = read(
            "app/src/main/java/com/caglar/pokequery/widget/QuickAccessWidgetProvider.kt",
            "src/main/java/com/caglar/pokequery/widget/QuickAccessWidgetProvider.kt"
        )
        assertTrue(provider.contains("ROUTE_SAFE_CLEANUP_REQUEST_CODE = 0x0611"))
        assertTrue(provider.contains("ROUTE_SAFE_CLEANUP_COPY_REQUEST_CODE = 0x0612"))
    }
}
