package com.caglar.pokequery.ui.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.domain.qr.QrMatrix
import com.caglar.pokequery.domain.qr.QrPayload
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.ui.pq.PqPrimaryButton

/**
 * v0.6.1 — QR export panel.
 *
 * Renders a generated QR Code for a search string using the offline, dependency-free
 * [QrMatrix] encoder. The QR encodes ONLY the search string itself — no user identifiers, no
 * account data, no URLs (unless the user explicitly put one in an Expert string).
 *
 * Export-first policy: this is export only. There is no CAMERA permission and no live scanning.
 * A copy fallback is ALWAYS rendered alongside the QR (or in place of it when the string is too
 * long for a reliable scan), so a string is never unreachable.
 *
 * Privacy: no analytics, no network. The matrix is computed locally on the device.
 *
 * @param searchString the exact text to encode (already the final string the user would copy).
 * @param onCopy invoked when the user taps the copy fallback.
 */
@Composable
fun QrExportPanel(
    searchString: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val payload = QrPayload.of(searchString)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.QrCode2, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("QR export", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Encodes only the search string. No account data, no links unless you typed one. Export only — scanning is not built in.",
            color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp
        )
        Spacer(Modifier.height(10.dp))

        if (payload.reliability == QrPayload.Reliability.SAFE) {
            QrCanvas(payload.searchString)
        } else {
            TooLongNotice(payload.searchString.length, QrPayload.RELIABLE_MAX_CHARS)
        }

        Spacer(Modifier.height(12.dp))
        // Copy fallback is ALWAYS shown — export-first, so a string is never unreachable.
        PqPrimaryButton(
            text = "Copy string instead",
            onClick = onCopy,
            leadingIcon = Icons.Default.ContentCopy
        )
    }
}

/**
 * Renders the QR matrix as a square of black/white modules with a quiet zone, drawn from the
 * offline [QrMatrix] encoder. Pure Compose Canvas — no bitmaps, no network.
 */
@Composable
private fun QrCanvas(text: String) {
    val matrix = remember(text) { QrMatrix.encode(text) }
    val size = matrix.size
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            // Draw only dark modules (white is the canvas background). Module (col, row) maps to
            // (x, y); matrix is indexed [row][col] per the encoder convention.
            val module = size.toFloat()
            val cell = this.size.width / module
            for (row in 0 until size) {
                val cols = matrix[row]
                for (col in 0 until size) {
                    if (cols[col]) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(col * cell, row * cell),
                            size = Size(cell, cell)
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "Point any phone's QR camera at this code, or tap Copy string instead.",
        color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TooLongNotice(length: Int, max: Int) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(AmberWarning.copy(alpha = 0.08f))
            .border(1.dp, AmberWarning.copy(alpha = 0.35f), shape).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(6.dp).background(AmberWarning, CircleShape))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                "This string is too long for a reliable QR scan ($length chars; safe max ~$max).",
                color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp
            )
            Text(
                "Use Copy string instead to share it.",
                color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp
            )
        }
    }
}
