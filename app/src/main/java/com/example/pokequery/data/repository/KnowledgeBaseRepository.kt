package com.example.pokequery.data.repository

import android.content.Context
import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.data.model.Term
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
                    knownQuirks = item.optString("knownQuirks").takeIf { it.isNotBlank() && it != "null" }
                )
            }
        }
    }
}
