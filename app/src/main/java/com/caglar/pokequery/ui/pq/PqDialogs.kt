package com.caglar.pokequery.ui.pq

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.SlateBlack
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.TextTertiary

/**
 * v0.5.0 Stitch dialogs + feedback sheet.
 *
 * Confirmation dialog: destructive-action guard (clear/reset).
 * Feedback sheet: mailto / share intent launcher — never sends automatically, no network.
 */

@Composable
fun PqConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = TextSecondary, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = CoralDanger, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = CardDark
    )
}

/**
 * Feedback sheet content (composed inside whatever container the caller uses).
 * Two explicit, offline actions: open the user's email app (mailto) or a share sheet.
 * The app sends nothing itself.
 */
@Composable
fun PqFeedbackSheet(
    appVersionLine: String,
    languageLine: String,
    onSendEmail: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(CardDark)
            .padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Tester Feedback", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.clickable { onDismiss() })
        }
        Spacer(Modifier.height(12.dp))
        InfoLine(appVersionLine)
        InfoLine(languageLine)
        Spacer(Modifier.height(8.dp))
        Text(
            "No personal data is tracked. You review and send this from your own email or share app.",
            color = TextTertiary, fontSize = 11.sp
        )
        Spacer(Modifier.height(16.dp))
        PqPrimaryButton(
            text = "Send via Email",
            onClick = onSendEmail,
            leadingIcon = Icons.Default.Email
        )
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onShare,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.width(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Share via other app", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InfoLine(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Box(Modifier.width(6.dp).height(6.dp).background(TealPrimary, RoundedCornerShape(50)))
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextSecondary, fontSize = 12.sp)
    }
}
