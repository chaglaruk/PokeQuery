package com.caglar.pokequery.domain.qr

/**
 * v0.6.1 — Offline QR matrix generator (NO third-party dependency, NO network).
 *
 * A compact, dependency-free QR Code generator producing a boolean matrix (modules) that the
 * Compose layer renders as a square of black/white cells. Implementation is a faithful port of
 * the QR specification (ISO/IEC 18004): it produces a scannable QR at byte-mode, M ECC for the
 * payload sizes PokeQuery emits (search strings up to ~230 chars at version ~10).
 *
 * License: this is original code written for PokeQuery; it contains no third-party code. It
 * implements the publicly-documented QR algorithm (Reed–Solomon via a standard GF(256) table).
 *
 * Scope note: PokeQuery is EXPORT-FIRST. Import/live camera scanning is intentionally NOT
 * implemented (would require CAMERA permission). This generator is offline and pure-Kotlin.
 */
object QrMatrix {

    /** Build the QR module matrix for [text]. Caller should check length via QrPayload first. */
    fun encode(text: String): Array<BooleanArray> {
        val data = text.toByteArray(Charsets.UTF_8)
        val version = chooseVersion(data.size)
        return QrEncoder(version, text, data).matrix
    }

    /**
     * Choose the smallest version (1..40) whose byte-mode + M ECC capacity fits [dataLen] bytes.
     * Capacity table for byte mode at ECC level M (bytes), versions 1..40.
     */
    private fun chooseVersion(dataLen: Int): Int {
        // (byte-capacity @ ECC M per QR spec, versions 1..40)
        val capacities = intArrayOf(
            14, 26, 42, 62, 84, 106, 122, 152, 180, 213, 251, 287, 331, 367, 425, 458, 520, 586, 644,
            718, 792, 858, 929, 1003, 1091, 1171, 1273, 1367, 1465, 1528, 1628, 1732, 1840, 1952, 2068,
            2188, 2303, 2431, 2563, 2699
        )
        for (v in capacities.indices) {
            if (capacities[v] >= dataLen) return v + 1
        }
        return 40
    }
}
