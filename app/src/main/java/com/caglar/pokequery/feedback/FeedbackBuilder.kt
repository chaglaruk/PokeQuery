package com.caglar.pokequery.feedback

import java.net.URLEncoder

/**
 * Package 2 — tester feedback body builder (mailto-based, no network).
 *
 * Builds a feedback email body the user reviews and sends manually from their own email
 * app. No backend, no auto-send, no analytics. The body carries only basic, non-identifying
 * app/device context (version, Android release, model, selected search language) plus a
 * structured template the tester fills in. It never solicits or includes account data,
 * trainer names, emails, or location.
 */
data class FeedbackContext(
    val appVersion: String,
    val androidVersion: String,
    val deviceModel: String,
    val gameLanguage: String
)

object FeedbackBuilder {
    const val RECIPIENT = "caglar@caglardinc.com"
    const val SUBJECT = "PokeQuery Closed Test Feedback"

    fun buildBody(c: FeedbackContext): String = buildString {
        appendLine("PokeQuery closed-test feedback")
        appendLine()
        appendLine("App version: ${c.appVersion}")
        appendLine("Android ${c.androidVersion}")
        appendLine("Device: ${c.deviceModel}")
        appendLine("Search language: ${c.gameLanguage}")
        appendLine()
        appendLine("Which screen was tested?")
        appendLine()
        appendLine("What felt confusing?")
        appendLine()
        appendLine("Did Safe Cleanup make sense?")
        appendLine()
        appendLine("Did Candy Prep make sense?")
        appendLine()
        appendLine("Did PvP Candidates make sense?")
        appendLine()
        appendLine("Did copying/pasting into Pokémon GO work?")
        appendLine()
        appendLine("Any bug or suggestion?")
    }

    fun buildMailtoUri(c: FeedbackContext): String {
        val body = buildBody(c)
        val encodedSubject = URLEncoder.encode(SUBJECT, "UTF-8")
            .replace("+", "%20")
        val encodedBody = URLEncoder.encode(body, "UTF-8")
            .replace("+", "%20")
        return "mailto:$RECIPIENT?subject=$encodedSubject&body=$encodedBody"
    }
}
