package com.caglar.pokequery.domain.locale

/**
 * Display/catalog view of the canonical search-token registry.
 *
 * This object used to duplicate Turkish translations independently from
 * [SearchTokenRegistry], which allowed stale values such as `mistik`, `arıtılmış`, `takaslanan`
 * and `can` to survive after the official Help Center-backed mapper had moved to `mitolojik`,
 * `arınmış`, `takas edilen` and `sp`. The registry is now the single source of truth.
 *
 * English remains the canonical safe syntax. Turkish candidates are emitted only through the
 * existing Search String Language setting and retain the verification status from the registry.
 */
data class LanguageToken(
    val language: String,
    val token: String,
    val notes: String = ""
)

object SearchTokenCatalog {
    val supportedLanguages = listOf("English", "Turkish")

    private fun statusNote(meta: SearchTokenMetadata): String = buildString {
        append(meta.status.name)
        meta.notes?.takeIf { it.isNotBlank() }?.let {
            append(" — ")
            append(it)
        }
    }

    /**
     * Catalog rows are generated from SearchTokenRegistry so UI/KB metadata cannot diverge from
     * the mapper's audited terminology again.
     */
    val tokens: Map<String, List<LanguageToken>> = SearchTokenRegistry.tokens.associate { meta ->
        val turkishToken = meta.turkish ?: meta.english
        meta.english to listOf(
            LanguageToken("English", meta.english, "CANONICAL — official syntax key"),
            LanguageToken(
                "Turkish",
                turkishToken,
                if (meta.turkish == null) {
                    "${meta.status.name} — English fallback; no supported Turkish candidate. ${meta.notes.orEmpty()}".trim()
                } else {
                    statusNote(meta)
                }
            )
        )
    }

    fun tokenFor(token: String, language: String): LanguageToken? =
        tokens[token]?.firstOrNull { it.language == language }

    fun isVerified(token: String, language: String): Boolean {
        if (language == "English") return tokens.containsKey(token)
        val meta = SearchTokenRegistry.byEnglish(token) ?: return false
        return meta.status == TokenVerification.VERIFIED
    }

    fun unverifiedTokens(language: String): List<String> {
        if (language == "English") return emptyList()
        return SearchTokenRegistry.tokens
            .filter { it.status != TokenVerification.VERIFIED }
            .map { it.english }
    }

    /** Turkish forms still awaiting live-client verification. */
    val betaTokensTurkish: List<String> = SearchTokenRegistry.tokens
        .filter { it.status == TokenVerification.BETA }
        .mapNotNull { it.turkish }
}
