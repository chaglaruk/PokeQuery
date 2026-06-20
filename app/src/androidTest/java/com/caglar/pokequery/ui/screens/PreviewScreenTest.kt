package com.caglar.pokequery.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel
import org.junit.Rule
import org.junit.Test

class PreviewScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersGeneratedStringFieldsWithoutReinferringThem() {
        val generated = GeneratedString(
            rawSyntax = "unique-query",
            plainLanguageExplanation = "engine-owned explanation",
            protectedCategories = listOf("engine-protection"),
            includedHighRiskCategories = listOf("engine-inclusion"),
            riskLevel = RiskLevel.Info,
            warnings = listOf("engine warning"),
            goalId = "test_goal",
            title = "Engine Title"
        )

        composeRule.setContent { PreviewScreen(generated, onCopy = {}, onSaveFavorite = {}, onBack = {}) }

        listOf(
            "unique-query", "engine-owned explanation", "engine-protection",
            "engine-inclusion", "engine warning", "Info", "Engine Title"
        ).forEach { composeRule.onNodeWithText(it, substring = true).assertExists() }
    }
}
