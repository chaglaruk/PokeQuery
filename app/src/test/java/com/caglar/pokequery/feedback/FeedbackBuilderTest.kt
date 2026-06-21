package com.caglar.pokequery.feedback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Package 2 — tester feedback body builder (mailto-based, no network).
 *
 * The body must include app version + selected language + the structured tester prompts,
 * and must NOT include personal/account data beyond basic, non-identifying device/app context.
 */
class FeedbackBuilderTest {

    private val context = FeedbackContext(
        appVersion = "0.4.3",
        androidVersion = "14",
        deviceModel = "Pixel 8",
        gameLanguage = "English"
    )

    @Test
    fun `body includes app version`() {
        val body = FeedbackBuilder.buildBody(context)
        assertTrue("Expected app version '0.4.3' in body", body.contains("0.4.3"))
    }

    @Test
    fun `body includes selected search language`() {
        val body = FeedbackBuilder.buildBody(context.copy(gameLanguage = "Turkish"))
        assertTrue("Expected language 'Turkish' in body", body.contains("Turkish"))
    }

    @Test
    fun `body includes device and android version as basic context`() {
        val body = FeedbackBuilder.buildBody(context)
        assertTrue(body.contains("Pixel 8"))
        assertTrue(body.contains("Android 14"))
    }

    @Test
    fun `body includes the structured tester prompts`() {
        val body = FeedbackBuilder.buildBody(context)
        assertTrue(body.contains("Which screen was tested?"))
        assertTrue(body.contains("Did Safe Cleanup make sense?"))
        assertTrue(body.contains("Did Candy Prep make sense?"))
        assertTrue(body.contains("Did PvP Candidates make sense?"))
        assertTrue(body.contains("Did copying/pasting into Pokémon GO work?"))
        assertTrue(body.contains("Any bug or suggestion?"))
    }

    @Test
    fun `body does not include personal or account data markers`() {
        val body = FeedbackBuilder.buildBody(context)
        // The body is a template the USER fills in. It must not solicit or auto-include
        // account identifiers, emails, Pokémon GO trainer names, or location.
        assertFalse(body.contains("password"))
        assertFalse(body.contains("account"))
        assertFalse(body.contains("trainer name"))
        assertFalse(body.contains("email"))
    }

    @Test
    fun `mailto uri has recipient subject and encoded body`() {
        val mailto = FeedbackBuilder.buildMailtoUri(context)
        assertTrue(mailto.startsWith("mailto:caglar@caglardinc.com"))
        assertTrue(mailto.contains("subject="))
        assertTrue(mailto.lowercase().contains("pokequery"))
    }

    @Test
    fun `subject is the fixed closed-test subject`() {
        assertEquals("PokeQuery Closed Test Feedback", FeedbackBuilder.SUBJECT)
    }
}
