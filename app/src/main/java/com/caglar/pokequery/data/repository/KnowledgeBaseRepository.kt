package com.caglar.pokequery.data.repository

import android.content.Context
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.Term
import com.caglar.pokequery.data.model.VerificationStatus
import org.json.JSONArray

class KnowledgeBaseRepository(private val context: Context) {
    fun load(): Result<List<Term>> = runCatching {
        context.assets.open("knowledgebase.json").bufferedReader().use { parse(it.readText()) }
    }

    companion object {
        fun parse(json: String): List<Term> {
            val array = JSONArray(json)
            return List(array.length()) { index ->
                val item = array.getJSONObject(index)
                // Package 8: optional metadata. Missing verification status defaults to
                // NEEDS_VERIFICATION (never fake VERIFIED). Invalid values also fall back.
                val verificationStatus = item.optString("verificationStatus")
                    .takeIf { it.isNotBlank() && it != "null" }
                    ?.let { raw ->
                        runCatching { VerificationStatus.valueOf(raw.uppercase()) }
                            .getOrDefault(VerificationStatus.NEEDS_VERIFICATION)
                    } ?: VerificationStatus.NEEDS_VERIFICATION

                Term(
                    id = item.getString("id"),
                    syntax = item.getString("syntax"),
                    category = item.getString("category"),
                    tier = item.getString("tier"),
                    descriptionTr = item.optString("description_tr"),
                    descriptionEn = item.getString("description_en"),
                    riskLevel = RiskLevel.valueOf(item.getString("riskLevel")),
                    sourceUrl = item.getString("sourceUrl"),
                    lastVerified = item.getString("lastVerified"),
                    knownQuirks = item.optString("knownQuirks").takeIf { it.isNotBlank() && it != "null" },
                    verificationStatus = verificationStatus,
                    safetyLevel = item.optString("safetyLevel").takeIf { it.isNotBlank() && it != "null" },
                    languageSensitive = if (item.has("languageSensitive") && !item.isNull("languageSensitive"))
                        item.getBoolean("languageSensitive") else null,
                    example = item.optString("example").takeIf { it.isNotBlank() && it != "null" },
                    commonMistake = item.optString("commonMistake").takeIf { it.isNotBlank() && it != "null" }
                )
            }
        }
    }
}

