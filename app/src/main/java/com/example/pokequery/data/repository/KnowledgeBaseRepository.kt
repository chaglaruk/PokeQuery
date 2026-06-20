package com.example.pokequery.data.repository

import android.content.Context
import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.data.model.Term
import org.json.JSONArray
import java.io.InputStreamReader

class KnowledgeBaseRepository(private val context: Context) {

    private val _terms = mutableListOf<Term>()
    val terms: List<Term> get() = _terms

    fun load() {
        if (_terms.isNotEmpty()) return
        
        context.assets.open("knowledgebase.json").use { inputStream ->
            val jsonString = InputStreamReader(inputStream).readText()
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                _terms.add(
                    Term(
                        id = obj.getString("id"),
                        syntax = obj.getString("syntax"),
                        category = obj.getString("category"),
                        tier = obj.getString("tier"),
                        descriptionTr = obj.getString("description_tr"),
                        descriptionEn = obj.getString("description_en"),
                        riskLevel = RiskLevel.valueOf(obj.getString("riskLevel")),
                        sourceUrl = obj.getString("sourceUrl"),
                        lastVerified = obj.getString("lastVerified"),
                        knownQuirks = if (obj.isNull("knownQuirks")) null else obj.getString("knownQuirks")
                    )
                )
            }
        }
    }
}
