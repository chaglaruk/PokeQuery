package com.caglar.pokequery.domain.qr

/**
 * v0.6.1 — QR export payload model.
 *
 * The QR encodes ONLY the generated search string — no user identifiers, no account data, no
 * tracking, no URLs (unless the user explicitly put one in an Expert string). This object
 * centralizes the export-first policy and the long-string safety boundary.
 *
 * A payload that is too long for a reliable QR (QR version capacity at M error-correction is
 * bounded) is reported as [Reliability.TOO_LONG]; callers show a "use copy/share instead" notice
 * rather than rendering an unreadable QR.
 */
data class QrPayload(
    val searchString: String,
    val reliability: Reliability
) {
    enum class Reliability { SAFE, TOO_LONG }

    companion object {
        /**
         * Conservative capacity for a numeric+alphanumeric search string at QR version ~10, M ECC.
         * Beyond this a phone camera scan becomes unreliable. The value is intentionally modest;
         * long Expert strings fall back to copy/share.
         */
        const val RELIABLE_MAX_CHARS = 230

        fun of(searchString: String): QrPayload {
            val trimmed = searchString.trim()
            val reliability = if (trimmed.length > RELIABLE_MAX_CHARS) Reliability.TOO_LONG else Reliability.SAFE
            return QrPayload(searchString = trimmed, reliability = reliability)
        }
    }
}
