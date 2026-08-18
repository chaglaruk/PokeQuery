package com.caglar.pokequery.domain.assist

import com.caglar.pokequery.domain.locale.LocalizationModel
import com.caglar.pokequery.ui.screens.resolveAssistantOutputQuery
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchAssistantLanguageTest {

    @Test
    fun resolvesEnglishOutputWhenConfigured() {
        val output = resolveAssistantOutputQuery(
            rawQuery = "shiny&!traded",
            gameLanguage = LocalizationModel.SearchStringLanguage.ENGLISH,
            appLanguage = LocalizationModel.AppLanguage.ENGLISH
        )
        assertEquals("shiny&!traded", output)
    }

    @Test
    fun resolvesTurkishOutputWhenConfigured() {
        val output = resolveAssistantOutputQuery(
            rawQuery = "shiny&!traded",
            gameLanguage = LocalizationModel.SearchStringLanguage.TURKISH,
            appLanguage = LocalizationModel.AppLanguage.ENGLISH
        )
        assertEquals("parlak&!takas edilen", output)
    }

    @Test
    fun resolvesGermanOutputWhenConfigured() {
        val output = resolveAssistantOutputQuery(
            rawQuery = "shiny&!traded",
            gameLanguage = LocalizationModel.SearchStringLanguage.GERMAN,
            appLanguage = LocalizationModel.AppLanguage.ENGLISH
        )
        assertEquals("schillernd&!getauscht", output)
    }

    @Test
    fun preservesIndependenceBetweenAppLanguageAndSearchLanguage() {
        // App language Turkish, Search language German -> German search syntax
        val germanOutput = resolveAssistantOutputQuery(
            rawQuery = "shiny&!traded",
            gameLanguage = LocalizationModel.SearchStringLanguage.GERMAN,
            appLanguage = LocalizationModel.AppLanguage.TURKISH
        )
        assertEquals("schillernd&!getauscht", germanOutput)

        // App language Turkish, Search language Auto (Safe) -> English search syntax
        val autoOutput = resolveAssistantOutputQuery(
            rawQuery = "shiny&!traded",
            gameLanguage = LocalizationModel.SearchStringLanguage.AUTO_SAFE,
            appLanguage = LocalizationModel.AppLanguage.TURKISH
        )
        assertEquals("shiny&!traded", autoOutput)
    }
}
