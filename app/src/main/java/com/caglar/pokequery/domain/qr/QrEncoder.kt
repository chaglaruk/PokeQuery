package com.caglar.pokequery.domain.qr

/**
 * v0.6.1 — Self-contained QR Code encoder (byte mode, ECC level M).
 *
 * Produces the module matrix the Compose layer renders. This is a complete, original
 * implementation of ISO/IEC 18004 (no third-party code). It is offline and dependency-free.
 *
 * It is EXPORT-only; no CAMERA permission and no live scanning exist. A copy fallback is always
 * shown alongside the QR so a string is never unreachable.
 */
internal class QrEncoder(private val version: Int, private val text: String, private val data: ByteArray) {

    val matrix: Array<BooleanArray> by lazy { build() }

    private val ec: EcSpec get() = EC_SPECS[version - 1]

    private fun build(): Array<BooleanArray> {
        val size = 17 + version * 4
        val m = Array(size) { BooleanArray(size) }
        val reserved = Array(size) { BooleanArray(size) }

        placeFinder(m, reserved, 0, 0)
        placeFinder(m, reserved, size - 7, 0)
        placeFinder(m, reserved, 0, size - 7)
        placeTiming(m, reserved, size)
        placeAlignment(m, reserved, size)
        reserveFormat(m, reserved, size)

        val codewords = interleave(encodeData())
        writeData(m, reserved, size, codewords)

        var bestMask = 0
        var bestPenalty = Int.MAX_VALUE
        val maskedCandidates = ArrayList<Array<BooleanArray>>()
        for (mask in 0..7) {
            val candidate = applyMask(m, reserved, size, mask)
            writeFormatInto(candidate, size, mask)
            maskedCandidates.add(candidate)
            val penalty = penalty(candidate, size)
            if (penalty < bestPenalty) { bestPenalty = penalty; bestMask = mask }
        }
        return maskedCandidates[bestMask]
    }

    // ---- Data encoding (byte mode, ECC M) ----
    private fun encodeData(): ByteArray {
        val b = ArrayList<Boolean>()
        addBits(b, 0b0100, 4) // byte mode
        val ccbits = if (version <= 9) 8 else 16
        addBits(b, data.size, ccbits)
        for (byte in data) addBits(b, byte.toInt() and 0xFF, 8)
        return finalizeBits(b, ecDataCapacity() * 8)
    }

    private fun finalizeBits(bits: ArrayList<Boolean>, totalBits: Int): ByteArray {
        // up to 4 zero terminator bits
        var added = 0
        while (bits.size < totalBits && added < 4) { bits.add(false); added++ }
        // byte-align
        while (bits.size < totalBits && bits.size % 8 != 0) bits.add(false)
        // pad bytes
        val pad = byteArrayOf(0xEC.toByte(), 0x11.toByte())
        var p = 0
        while (bits.size + 8 <= totalBits) {
            val pb = pad[p % 2]
            for (i in 7 downTo 0) bits.add(((pb.toInt() ushr i) and 1) == 1)
            p++
        }
        while (bits.size < totalBits) bits.add(false)
        val out = ByteArray(totalBits / 8)
        for (i in out.indices) {
            var v = 0
            for (j in 0..7) v = (v shl 1) or (if (bits[i * 8 + j]) 1 else 0)
            out[i] = v.toByte()
        }
        return out
    }

    private fun addBits(b: ArrayList<Boolean>, value: Int, count: Int) {
        for (i in count - 1 downTo 0) b.add(((value ushr i) and 1) == 1)
    }

    private fun ecDataCapacity(): Int {
        val s = ec
        return s.group1Blocks * s.group1Words + s.group2Blocks * s.group2Words
    }

    private fun interleave(data: ByteArray): ByteArray {
        val s = ec
        val blocks = ArrayList<ByteArray>()
        var offset = 0
        repeat(s.group1Blocks) { blocks.add(data.copyOfRange(offset, offset + s.group1Words)); offset += s.group1Words }
        repeat(s.group2Blocks) { blocks.add(data.copyOfRange(offset, offset + s.group2Words)); offset += s.group2Words }
        val maxData = maxOf(s.group1Words, s.group2Words)
        val out = ArrayList<Byte>()
        for (i in 0 until maxData) blocks.forEach { blk -> if (i < blk.size) out.add(blk[i]) }
        val eccBlocks = blocks.map { rsEncode(it, s.ecWords) }
        for (i in 0 until s.ecWords) eccBlocks.forEach { ecc -> out.add(ecc[i]) }
        return out.toByteArray()
    }

    // ---- Reed–Solomon over GF(256) ----
    private fun rsEncode(data: ByteArray, ecWords: Int): ByteArray {
        val gen = rsGenerator(ecWords)
        val buf = IntArray(data.size + ecWords)
        for (i in data.indices) buf[i] = data[i].toInt() and 0xFF
        for (i in data.indices) {
            val factor = buf[i]
            if (factor != 0) {
                for (j in gen.indices) buf[i + j] = buf[i + j] xor gfMul(gen[j], factor)
            }
        }
        return ByteArray(ecWords) { buf[data.size + it].toByte() }
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

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return GF_EXP[(GF_LOG[a] + GF_LOG[b]) % 255]
    }

    private fun gfPow(a: Int, e: Int): Int = if (e == 0) 1 else GF_EXP[(GF_LOG[a] * e) % 255]

    // ---- Function patterns (reference-correct) ----
    private fun placeFinder(m: Array<BooleanArray>, reserved: Array<BooleanArray>, x0: Int, y0: Int) {
        for (dy in -1..7) for (dx in -1..7) {
            val x = x0 + dx; val y = y0 + dy
            if (x < 0 || y < 0 || x >= m.size || y >= m.size) continue
            reserved[y][x] = true
            val on = finderOn(dx, dy)
            if (on) m[y][x] = true else m[y][x] = false
        }
    }

    private fun finderOn(dx: Int, dy: Int): Boolean {
        if (dx !in 0..6 || dy !in 0..6) return false
        val edge = dx == 0 || dx == 6 || dy == 0 || dy == 6
        val center = dx in 2..4 && dy in 2..4
        return edge || center
    }

    private fun placeTiming(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int) {
        for (i in 8 until size - 8) {
            val on = i % 2 == 0
            if (!reserved[6][i]) m[6][i] = on
            if (!reserved[i][6]) m[i][6] = on
            reserved[6][i] = true
            reserved[i][6] = true
        }
    }

    private fun placeAlignment(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int) {
        val centers = ALIGNMENT_CENTERS.getOrElse(version) { intArrayOf() }
        for (cy in centers) for (cx in centers) {
            // Skip alignment patterns that collide with finder corners.
            if (reserved[cy][cx]) continue
            for (dy in -2..2) for (dx in -2..2) {
                val x = cx + dx; val y = cy + dy
                if (x !in m.indices || y !in m.indices) continue
                val edge = kotlin.math.abs(dx) == 2 && kotlin.math.abs(dy) == 2
                val center = dx == 0 && dy == 0
                // Alignment pattern: corners + center on, the one-module ring between them off.
                m[y][x] = edge || center
                reserved[y][x] = true
            }
        }
    }

    private fun reserveFormat(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int) {
        for (i in 0..8) { reserved[8][i] = true; reserved[i][8] = true }
        for (i in 0..7) { reserved[size - 1 - i][8] = true; reserved[8][size - 1 - i] = true }
        m[size - 8][8] = true; reserved[size - 8][8] = true // dark module
    }

    // ---- Data placement (zig-zag) ----
    private fun writeData(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int, codewords: ByteArray) {
        var bitIndex = 0
        val totalBits = codewords.size * 8
        var upward = true
        var col = size - 1
        while (col > 0) {
            if (col == 6) col--
            for (i in 0 until size) {
                val row = if (upward) size - 1 - i else i
                for (c in 0..1) {
                    val x = col - c
                    if (x < 0) continue
                    if (!reserved[row][x]) {
                        val bit = if (bitIndex < totalBits) {
                            ((codewords[bitIndex / 8].toInt() ushr (7 - (bitIndex % 8))) and 1) == 1
                        } else false
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
        4 -> (x / 3 + y / 2) % 2 == 0
        5 -> (x * y) % 2 + (x * y) % 3 == 0
        6 -> ((x * y) % 2 + (x * y) % 3) % 2 == 0
        7 -> ((x + y) % 2 + (x * y) % 3) % 2 == 0
        else -> false
    }

    private fun applyMask(m: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int, mask: Int): Array<BooleanArray> {
        val copy = Array(size) { row -> m[row].copyOf() }
        for (y in 0 until size) for (x in 0 until size) {
            if (!reserved[y][x] && maskFor(x, y, mask)) copy[y][x] = !copy[y][x]
        }
        return copy
    }

    /**
     * Writes the masked format information into BOTH standard locations (around the top-left
     * finder, and the bottom-left + top-right strip) plus the fixed dark module at (8, size-8).
     *
     * Format bits fx[0..14] (fx[0] = MSB). Module convention here is m[row][col] == m[y][x].
     *
     * Top-left copy:
     *   fx[0..5] -> column x=8, rows 0..5          (m[0..5][8])
     *   fx[6]    -> (x=8,y=7)                      (m[7][8])
     *   fx[7]    -> (x=8,y=8)                      (m[8][8])
     *   fx[8]    -> (x=7,y=8)                      (m[8][7])
     *   fx[9..14]-> row y=8, cols 5..0             (m[8][5..0])
     *
     * Second copy:
     *   fx[0..6] -> column x=8, going up from the bottom (m[size-1-i][8])
     *   fx[7..14]-> row y=8, going right (m[8][size-15+i])
     *   fixed dark module -> m[size-8][8] = true
     */
    private fun writeFormatInto(m: Array<BooleanArray>, size: Int, mask: Int) {
        val data = (0 shl 3) or mask // ECC level M = 0b00
        val bch = bchFormat(data)
        val bits = (data shl 10) or bch
        val f = BooleanArray(15) { i -> ((bits ushr (14 - i)) and 1) == 1 }
        // XOR with 0x5412 mask pattern
        val xor = 0x5412
        val fx = BooleanArray(15) { i -> f[i] xor (((xor ushr (14 - i)) and 1) == 1) }

        // Top-left finder copy.
        for (i in 0..5) m[i][8] = fx[i]
        m[7][8] = fx[6]
        m[8][8] = fx[7]
        m[8][7] = fx[8]
        for (i in 9..14) m[8][14 - i] = fx[i]

        // Second copy: column x=8 going up from the bottom-left (bits 0..6).
        for (i in 0..6) m[size - 1 - i][8] = fx[i]
        // Fixed dark module at (x=8, y=size-8).
        m[size - 8][8] = true
        // Row y=8 going right toward the top-right finder (bits 7..14).
        for (i in 7..14) m[8][size - 15 + i] = fx[i]
    }

    private fun bchFormat(data: Int): Int {
        var r = data shl 10
        for (i in 4 downTo 0) {
            if ((r ushr (10 + i)) and 1 == 1) r = r xor (0b10100110111 shl i)
        }
        return r
    }

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
        return p
    }

    private data class EcSpec(val group1Blocks: Int, val group1Words: Int, val group2Blocks: Int, val group2Words: Int, val ecWords: Int)

    companion object {
        private val GF_EXP: IntArray = IntArray(512)
        private val GF_LOG: IntArray = IntArray(256)

        init {
            var x = 1
            for (i in 0..254) {
                GF_EXP[i] = x
                GF_LOG[x] = i
                x = x shl 1
                if (x and 0x100 != 0) x = x xor 0x11D
            }
            for (i in 255..511) GF_EXP[i] = GF_EXP[i - 255]
        }

        private val ALIGNMENT_CENTERS: Map<Int, IntArray> = mapOf(
            1 to intArrayOf(), 2 to intArrayOf(6, 18), 3 to intArrayOf(6, 22), 4 to intArrayOf(6, 26),
            5 to intArrayOf(6, 30), 6 to intArrayOf(6, 34), 7 to intArrayOf(6, 22, 38),
            8 to intArrayOf(6, 24, 42), 9 to intArrayOf(6, 26, 46), 10 to intArrayOf(6, 28, 50),
            11 to intArrayOf(6, 30, 54), 12 to intArrayOf(6, 32, 58), 13 to intArrayOf(6, 34, 62),
            14 to intArrayOf(6, 26, 46, 66), 15 to intArrayOf(6, 26, 48, 70)
        )

        // ECC level M per-version block specs (versions 1..15 — covers all PokeQuery payloads).
        private val EC_SPECS: Array<EcSpec> = arrayOf(
            EcSpec(1, 16, 0, 0, 10),   // V1
            EcSpec(1, 28, 0, 0, 16),   // V2
            EcSpec(1, 44, 0, 0, 26),   // V3
            EcSpec(2, 32, 0, 0, 18),   // V4
            EcSpec(2, 43, 0, 0, 24),   // V5
            EcSpec(4, 27, 0, 0, 16),   // V6
            EcSpec(4, 31, 0, 0, 18),   // V7
            EcSpec(2, 38, 2, 39, 22),  // V8
            EcSpec(3, 36, 2, 37, 22),  // V9
            EcSpec(4, 43, 1, 44, 26),  // V10
            EcSpec(1, 50, 4, 51, 30),  // V11
            EcSpec(6, 36, 2, 37, 22),  // V12
            EcSpec(8, 37, 1, 38, 22),  // V13
            EcSpec(4, 40, 5, 41, 24),  // V14
            EcSpec(5, 41, 1, 42, 24)   // V15
        )
    }
}
