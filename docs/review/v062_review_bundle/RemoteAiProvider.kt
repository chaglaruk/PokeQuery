package com.caglar.pokequery.domain.assist

data class AiSuggestion(
    val rawSyntax: String,
    val explanation: String,
    val limitations: List<String> = emptyList(),
    val requiresReview: Boolean = true
)

interface RemoteAiProvider {
    val isAvailable: Boolean
    val requiresApiKey: Boolean
    val displayName: String

    suspend fun suggest(text: String): Result<AiSuggestion>
}

object NoOpAiProvider : RemoteAiProvider {
    override val isAvailable = false
    override val requiresApiKey = false
    override val displayName = "Offline Only"
    override suspend fun suggest(text: String): Result<AiSuggestion> =
        Result.failure(UnsupportedOperationException("No remote AI provider configured."))
}

object AiProviderRegistry {
    var activeProvider: RemoteAiProvider = NoOpAiProvider
}
