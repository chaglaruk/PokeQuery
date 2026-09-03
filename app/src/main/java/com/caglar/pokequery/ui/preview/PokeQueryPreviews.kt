package com.caglar.pokequery.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.caglar.pokequery.theme.PokeQueryTheme
import com.caglar.pokequery.ui.screens.HomeScreen

/**
 * Reusable Android Studio preview set for the phone sizes that are most useful for
 * PokeQuery visual QA. Keep this list deliberately small enough that Grid mode remains fast.
 */
@Preview(name = "01 - Small 320x568", group = "Phone sizes", widthDp = 320, heightDp = 568)
@Preview(name = "02 - Compact 360x640", group = "Phone sizes", widthDp = 360, heightDp = 640)
@Preview(name = "03 - Standard 360x800", group = "Phone sizes", widthDp = 360, heightDp = 800)
@Preview(name = "04 - Tall 393x852", group = "Phone sizes", widthDp = 393, heightDp = 852)
@Preview(name = "05 - Large 412x915", group = "Phone sizes", widthDp = 412, heightDp = 915)
@Preview(name = "06 - Small tablet 600x960", group = "Phone sizes", widthDp = 600, heightDp = 960)
annotation class PokeQueryPhonePreviews

/**
 * Locale stress set. It intentionally uses one representative phone size instead of creating
 * a device x locale Cartesian product, which would make Android Studio render 36 previews.
 */
@Preview(name = "English", group = "UI locales", locale = "en", widthDp = 393, heightDp = 852)
@Preview(name = "Türkçe", group = "UI locales", locale = "tr", widthDp = 393, heightDp = 852)
@Preview(name = "Deutsch", group = "UI locales", locale = "de", widthDp = 393, heightDp = 852)
@Preview(name = "Español", group = "UI locales", locale = "es", widthDp = 393, heightDp = 852)
@Preview(name = "Français", group = "UI locales", locale = "fr", widthDp = 393, heightDp = 852)
@Preview(name = "Italiano", group = "UI locales", locale = "it", widthDp = 393, heightDp = 852)
annotation class PokeQueryLocalePreviews

@Composable
private fun HomePreviewContent() {
    PokeQueryTheme {
        HomeScreen(onGoalSelected = {})
    }
}

@PokeQueryPhonePreviews
@Composable
fun HomeScreenPhonePreviews() {
    HomePreviewContent()
}

@PokeQueryLocalePreviews
@Composable
fun HomeScreenLocalePreviews() {
    HomePreviewContent()
}
