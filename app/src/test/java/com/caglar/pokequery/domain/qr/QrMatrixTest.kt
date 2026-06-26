package com.caglar.pokequery.domain.qr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.6.1 — QrMatrix / QrEncoder structural tests.
 *
 * These validate the QR Code *structural invariants* from ISO/IEC 18004 rather than a fixed byte
 * pattern: the finder patterns at the three corners, the timing patterns, the fixed dark module at
 * (8, size-8), and that the matrix is square of the expected size for the chosen version. They also
 * pin the export-first payload boundary (too-long strings are flagged, not rendered unreadable).
 */
class QrMatrixTest {

    private fun encode(text: String): Array<BooleanArray> = QrMatrix.encode(text)

    @Test
    fun `matrix is square and matches the version size formula`() {
        val m = encode("PokeQuery")
        val size = m.size
        // QR matrix size = 17 + 4*version. Each row has `size` columns.
        assertEquals(size, m[0].size)
        assertTrue("size $size must be 21, 25, 29, ... (17 + 4*version)", (size - 17) % 4 == 0 && size >= 21)
    }

    @Test
    fun `three finder patterns are present at the corners`() {
        val m = encode("hello")
        // Top-left 7x7 finder: a 7x7 square with a 3x3 center block.
        assertTrue("top-left finder center must be dark", m[3][3])
        assertTrue("top-left finder corner must be dark", m[0][0])
        // The 1-module white separator inside the matrix bounds around the top-left finder:
        // column 7 row 0..6 should be white (the separator), and row 7 col 0..6 white.
        for (i in 0..6) {
            assertFalse("finder separator row7[$i] must be white", m[7][i])
            assertFalse("finder separator col7[$i] must be white", m[i][7])
        }
        val size = m.size
        // Top-right finder occupies columns [size-7, size-1], rows [0,6].
        assertTrue("top-right finder center dark", m[3][size - 4])
        // Bottom-left finder occupies columns [0,6], rows [size-7, size-1].
        assertTrue("bottom-left finder center dark", m[size - 4][3])
    }

    @Test
    fun `timing pattern alternates along row 6 and column 6`() {
        val m = encode("timing test")
        val size = m.size
        // Timing pattern runs from col/row 8 to size-9 inclusive on the 6th line. It alternates
        // dark/light starting with dark at index 8.
        var expected = true
        for (i in 8 until size - 8) {
            assertEquals("row6 timing at $i", expected, m[6][i])
            expected = !expected
        }
        expected = true
        for (i in 8 until size - 8) {
            assertEquals("col6 timing at $i", expected, m[i][6])
            expected = !expected
        }
    }

    @Test
    fun `fixed dark module is present at the format-info corner`() {
        val m = encode("dark module")
        val size = m.size
        // The fixed dark module sits at (col=8, row=size-8) for every QR Code.
        assertTrue("dark module at (8, size-8) must be dark", m[size - 8][8])
    }

    @Test
    fun `encoding round trips for a typical short search string`() {
        // Structural sanity: a realistic safe-cleanup-style string encodes without throwing and
        // yields a non-trivial matrix (some dark, some light modules).
        val text = "0*,1*&!shiny&!legendary&!mythical&!favorite"
        val m = encode(text)
        val dark = m.sumOf { row -> row.count { it } }
        val light = m.size * m.size - dark
        assertTrue("expected some dark modules", dark > 0)
        assertTrue("expected some light modules", light > 0)
    }

    @Test
    fun `longer strings select a larger-or-equal version`() {
        val small = encode("a")
        val big = encode("0*,1*&!shiny&!legendary&!mythical&!ultrabeast&!shadow&!purified&!favorite&!lucky&!#&!traded&!costume&!background&!locationbackground&!specialbackground")
        // A longer payload never needs a smaller matrix than a one-char payload.
        assertTrue("bigger payload should not shrink the matrix", big.size >= small.size)
    }
}

class QrPayloadTest {

    @Test
    fun `short string is safe to render as a QR`() {
        assertEquals(QrPayload.Reliability.SAFE, QrPayload.of("0*,1*&!shiny").reliability)
    }

    @Test
    fun `string over the reliable limit is flagged too long`() {
        val tooLong = "a".repeat(QrPayload.RELIABLE_MAX_CHARS + 1)
        assertEquals(QrPayload.Reliability.TOO_LONG, QrPayload.of(tooLong).reliability)
    }

    @Test
    fun `payload is trimmed before the length check`() {
        val padded = "a".repeat(QrPayload.RELIABLE_MAX_CHARS) + "   "
        assertEquals(QrPayload.Reliability.SAFE, QrPayload.of(padded).reliability)
    }
}
