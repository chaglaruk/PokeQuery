package com.caglar.pokequery.domain.qr

/**
 * v0.6.1 — Self-contained QR Code encoder (byte mode, ECC level M).
 *
 * Produces the module matrix the Compose layer renders. Original implementation of ISO/IEC 18004
 * (no third-party code). Offline and dependency-free. EXPORT-only: no CAMERA permission and no
 * live scanning; a copy fallback is always shown alongside.
 *
 * Correctness is pinned by QrMatrixTest (structural invariants) AND by an external cross-decode
 * (OpenCV cv2.QRCodeDetector round-trips the rendered matrix back to the exact input string).
 *
 * @param version QR version 1..40.
 * @param data    the payload bytes (UTF-8).
 */
internal class QrEncoder(private val version: Int, private val data: ByteArray) {

    val matrix: Array<BooleanArray> by lazy { build() }

    private fun build(): Array<BooleanArray> {
        val size = 17 + version * 4
        val m = Array(size) { BooleanArray(size) }       // true = dark
        val reserved = Array(size) { BooleanArray(size) } // function-pattern cells (not data)

        placeFinder(m, reserved, 0, 0)
        placeFinder(m, reserved, size - 7, 0)
        placeFinder(m, reserved, 0, size - 7)
        placeAlignments(m, reserved, size)
        placeTiming(m, reserved, size)
        reserveFormat(reserved, size)

        // Data + ECC codewords, interleaved into the final module bit stream.
        val codewords = buildCodewords()
        writeData(m, reserved, size, codewords)

        // Pick the lowest-penalty mask, apply it, and write format info for that mask.
        var bestMask = 0
        var bestPenalty = Int.MAX_VALUE
        var best: Array<BooleanArray>? = null
        for (mask in 0..7) {
            val candidate = applyMask(m, reserved, size, mask)
            writeFormat(candidate, size, mask)
            val p = penalty(candidate, size)
            if (p < bestPenalty) { bestPenalty = p; bestMask = mask; best = candidate }
        }
        return best!!
    }

    // ---- Function patterns ----
    private fun placeFinder(m: Array<BooleanArray>, reserved: Array<BooleanArray>, x0: Int, y0: Int) {
        for (dy in -1..7) for (dx in -1..7) {
            val x = x0 + dx; val y = y0 + dy
            if (x < 0 || y < 0 || x >= m.size || y >= m.size) continue
            reserved[y][x] = true
            m[y][x] = isFinderDark(dx, dy)
        }
    }

    private fun isFinderDark(dx: Int, dy: Int): Boolean {
        if (dx !in 0..6 || dy !in 0..6) return false
        val edge = dx == 0 || dx == 6 || dy == 0 || dy == 6
        val center = dx in 2..4 && dy in 2..4
        return edge || center
    }

    private fun placeAlignments(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int) {
        val centers = ALIGNMENT_CENTERS[version] ?: IntArray(0)
        for (cy in centers) for (cx in centers) {
            if (reserved[cy][cx]) continue // overlaps a finder
            for (dy in -2..2) for (dx in -2..2) {
                val x = cx + dx; val y = cy + dy
                if (x !in 0 until size || y !in 0 until size) continue
                val dark = (kotlin.math.abs(dx) == 2 && kotlin.math.abs(dy) == 2) || (dx == 0 && dy == 0)
                m[y][x] = dark
                reserved[y][x] = true
            }
        }
    }

    private fun placeTiming(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int) {
        for (i in 8 until size - 8) {
            val dark = i % 2 == 0
            m[6][i] = dark; m[i][6] = dark
            reserved[6][i] = true; reserved[i][6] = true
        }
    }

    private fun reserveFormat(reserved: Array<BooleanArray>, size: Int) {
        for (i in 0..8) { reserved[8][i] = true; reserved[i][8] = true }
        for (i in 0..7) { reserved[size - 1 - i][8] = true; reserved[8][size - 1 - i] = true }
    }

    // ---- Data + ECC codeword construction ----
    private fun buildCodewords(): ByteArray {
        val spec = ecSpec(version)
        // Build the bit stream: mode(4) + charcount(8/16) + data bytes + terminator + pad + align.
        val bits = ArrayList<Int>()
        addBits(bits, 0b0100, 4) // byte mode
        val ccBits = if (version <= 9) 8 else 16
        addBits(bits, data.size, ccBits)
        for (b in data) addBits(bits, b.toInt() and 0xFF, 8)

        val totalDataCodewords = spec.totalDataCodewords(version)
        val totalBits = totalDataCodewords * 8
        // Terminator: up to 4 zero bits, capped at capacity.
        var term = 0
        while (bits.size < totalBits && term < 4) { bits.add(0); term++ }
        // Byte-align.
        while (bits.size < totalBits && bits.size % 8 != 0) bits.add(0)
        // Pad with 0xEC, 0x11.
        val pad = intArrayOf(0xEC, 0x11)
        var pi = 0
        while (bits.size + 8 <= totalBits) {
            addBits(bits, pad[pi % 2], 8); pi++
        }
        while (bits.size < totalBits) bits.add(0)

        // Pack bits into data codeword bytes.
        val dataCodewords = ByteArray(totalDataCodewords)
        for (i in dataCodewords.indices) {
            var v = 0
            for (j in 0..7) v = (v shl 1) or bits[i * 8 + j]
            dataCodewords[i] = v.toByte()
        }

        // Split into blocks per the ECC-M spec, compute RS ECC per block, interleave.
        val blocks = ArrayList<ByteArray>()
        val eccBlocks = ArrayList<ByteArray>()
        var off = 0
        repeat(spec.g1Count) {
            val blk = dataCodewords.copyOfRange(off, off + spec.g1Words); off += spec.g1Words
            blocks.add(blk); eccBlocks.add(rsEncode(blk, spec.ecWords))
        }
        repeat(spec.g2Count) {
            val blk = dataCodewords.copyOfRange(off, off + spec.g2Words); off += spec.g2Words
            blocks.add(blk); eccBlocks.add(rsEncode(blk, spec.ecWords))
        }

        val out = ArrayList<Byte>()
        val maxData = maxOf(spec.g1Words, spec.g2Words)
        for (i in 0 until maxData) blocks.forEach { if (i < it.size) out.add(it[i]) }
        for (i in 0 until spec.ecWords) eccBlocks.forEach { out.add(it[i]) }
        return out.toByteArray()
    }

    private fun addBits(bits: ArrayList<Int>, value: Int, count: Int) {
        for (i in count - 1 downTo 0) bits.add((value ushr i) and 1)
    }

    // ---- Reed–Solomon over GF(256), primitive 0x11D ----
    private fun rsEncode(data: ByteArray, ecLen: Int): ByteArray {
        val gen = rsGenerator(ecLen)
        val buf = IntArray(data.size + ecLen)
        for (i in data.indices) buf[i] = data[i].toInt() and 0xFF
        for (i in data.indices) {
            val coef = buf[i]
            if (coef != 0) for (j in gen.indices) buf[i + j] = buf[i + j] xor gfMul(gen[j], coef)
        }
        return ByteArray(ecLen) { buf[data.size + it].toByte() }
    }

    private fun rsGenerator(degree: Int): IntArray {
        var g = intArrayOf(1)
        for (i in 0 until degree) {
            val ng = IntArray(g.size + 1)
            for (j in g.indices) {
                ng[j] = ng[j] xor g[j]
                ng[j + 1] = ng[j + 1] xor gfMul(g[j], gfPow(2, i))
            }
            g = ng
        }
        return g
    }

    private fun gfMul(a: Int, b: Int): Int =
        if (a == 0 || b == 0) 0 else GF_EXP[(GF_LOG[a] + GF_LOG[b]) % 255]

    private fun gfPow(a: Int, e: Int): Int = if (e == 0) 1 else GF_EXP[(GF_LOG[a] * e) % 255]

    // ---- Data placement (zig-zag) ----
    /**
     * Writes the codeword bits into non-reserved cells using the canonical QR zig-zag: a column
     * pair cursor moves right-to-left in steps of two; for each pair both columns are visited
     * (right then left) at every row, walking up then down alternately. The timing column (x=6)
     * is crossed naturally — only its single (6,6) cell is reserved, so column 6's other cells
     * still receive data via the (6,5) pair. (Earlier code skipped the whole column 6, dropping
     * ~22 bits and corrupting the stream.)
     */
    private fun writeData(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int, codewords: ByteArray) {
        var bitIndex = 0
        val totalBits = codewords.size * 8
        var upward = true
        var col = size - 1
        while (col >= 1) {
            // Skip timing column 6 by moving to column 5 (matching the Python qrcode library).
            var cx = col
            if (cx <= 6) cx--
            for (i in 0 until size) {
                val row = if (upward) size - 1 - i else i
                for (c in 0..1) {
                    val x = cx - c
                    if (x < 0) continue
                    if (!reserved[row][x]) {
                        val bit = bitIndex < totalBits &&
                            ((codewords[bitIndex / 8].toInt() ushr (7 - (bitIndex % 8))) and 1) == 1
                        m[row][x] = bit
                        bitIndex++
                    }
                }
            }
            col -= 2
            upward = !upward
        }
    }

    // ---- Masking ----
    private fun maskFor(x: Int, y: Int, mask: Int): Boolean = when (mask) {
        0 -> (x + y) % 2 == 0
        1 -> y % 2 == 0
        2 -> x % 3 == 0
        3 -> (x + y) % 3 == 0
        4 -> ((x / 3) + (y / 2)) % 2 == 0
        5 -> ((x * y) % 2 + (x * y) % 3) == 0
        6 -> ((x * y) % 2 + (x * y) % 3) % 2 == 0
        7 -> ((x + y) % 2 + (x * y) % 3) % 2 == 0
        else -> false
    }

    private fun applyMask(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int, mask: Int): Array<BooleanArray> {
        val copy = Array(size) { m[it].copyOf() }
        for (y in 0 until size) for (x in 0 until size) {
            if (!reserved[y][x] && maskFor(x, y, mask)) copy[y][x] = !copy[y][x]
        }
        return copy
    }

    /**
     * Format info (15 bits) for ECC level M (0b00) + mask, BCH-encoded, XOR 0x5412, written to
     * both standard locations + the fixed dark module at (col=8,row=size-8). m[row][col]==m[y][x].
     *
     * Per convention used by standard QR decoders, bit 0 (LSB of the BCH result) is placed at
     * (row=0, col=8), and bit 14 (MSB) at (row=8, col=0).
     */
    private fun writeFormat(m: Array<BooleanArray>, size: Int, mask: Int) {
        val data = (0 shl 3) or mask // ECC M = 0b00
        var r = data shl 10
        for (i in 4 downTo 0) if ((r ushr (10 + i)) and 1 == 1) r = r xor (0b10100110111 shl i)
        val bits = (data shl 10) or (r and 0x3FF)
        // fBch[0..14] = BCH result MSB-first (fBch[0] = bit14)
        val fBch = BooleanArray(15) { ((bits ushr (14 - it)) and 1) == 1 }
        // XOR with 0x5412 MSB-first
        val mask5412 = BooleanArray(15) { ((0x5412 ushr (14 - it)) and 1) == 1 }
        // fx[0..14] = final format bits MSB-first
        val fx = BooleanArray(15) { fBch[it] xor mask5412[it] }
        // Place LSB-first: fx[14] (LSB) at row 0, fx[0] (MSB) at row/col towards bottom
        for (i in 0..5) m[i][8] = fx[14 - i]
        m[7][8] = fx[8]; m[8][8] = fx[7]; m[8][7] = fx[6]
        for (i in 9..14) m[8][14 - i] = fx[14 - i]
        for (i in 0..6) m[size - 1 - i][8] = fx[14 - i]
        m[size - 8][8] = true // fixed dark module
        for (i in 7..14) m[8][size - 15 + i] = fx[14 - i]
    }

    // ---- Penalty (ISO/IEC 18004 §8.8.2) ----
    private fun penalty(m: Array<BooleanArray>, size: Int): Int {
        var p = 0
        for (y in 0 until size) {
            var run = 1
            for (x in 1 until size) { if (m[y][x] == m[y][x - 1]) run++ else { if (run >= 5) p += run - 2; run = 1 } }
            if (run >= 5) p += run - 2
        }
        for (x in 0 until size) {
            var run = 1
            for (y in 1 until size) { if (m[y][x] == m[y - 1][x]) run++ else { if (run >= 5) p += run - 2; run = 1 } }
            if (run >= 5) p += run - 2
        }
        for (y in 0 until size - 1) for (x in 0 until size - 1) {
            val v = m[y][x]
            if (v == m[y][x + 1] && v == m[y + 1][x] && v == m[y + 1][x + 1]) p += 3
        }
        var dark = 0
        for (y in 0 until size) for (x in 0 until size) if (m[y][x]) dark++
        val pct = dark * 100 / (size * size)
        p += kotlin.math.abs(pct - 50) / 5 * 10
        return p
    }

    private data class EcSpec(val g1Count: Int, val g1Words: Int, val g2Count: Int, val g2Words: Int, val ecWords: Int) {
        fun totalDataCodewords(version: Int): Int = g1Count * g1Words + g2Count * g2Words
    }

    private fun ecSpec(version: Int): EcSpec = EC_SPECS[version - 1]

    companion object {
        private val GF_EXP = IntArray(512)
        private val GF_LOG = IntArray(256)

        init {
            var x = 1
            for (i in 0..254) {
                GF_EXP[i] = x; GF_LOG[x] = i
                x = x shl 1
                if (x and 0x100 != 0) x = x xor 0x11D
            }
            for (i in 255..511) GF_EXP[i] = GF_EXP[i - 255]
        }

        // Alignment-pattern center coordinates per version (1..15 covers all PokeQuery payloads).
        private val ALIGNMENT_CENTERS: Map<Int, IntArray> = mapOf(
            1 to intArrayOf(), 2 to intArrayOf(6, 18), 3 to intArrayOf(6, 22), 4 to intArrayOf(6, 26),
            5 to intArrayOf(6, 30), 6 to intArrayOf(6, 34), 7 to intArrayOf(6, 22, 38),
            8 to intArrayOf(6, 24, 42), 9 to intArrayOf(6, 26, 46), 10 to intArrayOf(6, 28, 50),
            11 to intArrayOf(6, 30, 54), 12 to intArrayOf(6, 32, 58), 13 to intArrayOf(6, 34, 62),
            14 to intArrayOf(6, 26, 46, 66), 15 to intArrayOf(6, 26, 48, 70)
        )

        // ECC level M per-version block specs (versions 1..15).
        private val EC_SPECS: Array<EcSpec> = arrayOf(
            EcSpec(1, 16, 0, 0, 10),  // V1
            EcSpec(1, 28, 0, 0, 16),  // V2
            EcSpec(1, 44, 0, 0, 26),  // V3
            EcSpec(2, 32, 0, 0, 18),  // V4
            EcSpec(2, 43, 0, 0, 24),  // V5
            EcSpec(4, 27, 0, 0, 16),  // V6
            EcSpec(4, 31, 0, 0, 18),  // V7
            EcSpec(2, 38, 2, 39, 22), // V8
            EcSpec(3, 36, 2, 37, 22), // V9
            EcSpec(4, 43, 1, 44, 26), // V10
            EcSpec(1, 50, 4, 51, 30), // V11
            EcSpec(6, 36, 2, 37, 22), // V12
            EcSpec(8, 37, 1, 38, 22), // V13
            EcSpec(4, 40, 5, 41, 24), // V14
            EcSpec(5, 41, 1, 42, 24)  // V15
        )
    }
}
