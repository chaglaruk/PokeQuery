package com.caglar.pokequery.ui.pq

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.VerificationStatus
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.BorderDark
import com.caglar.pokequery.theme.BorderSubtle
import com.caglar.pokequery.theme.BottomNavBackground
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CardPremium
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.GreenVerified
import com.caglar.pokequery.theme.SlateBlack
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.TextTertiary
import com.caglar.pokequery.theme.density.currentDensity

/**
 * v0.5.0 Stitch design system — reusable Pq* primitives.
 *
 * Single source for the premium dark-navy / electric-cyan visual language so every
 * screen shares one vocabulary. Tokens come from theme/Color.kt (Stitch PRD §2).
 * Components keep 44dp+ touch targets and AA-contrast text.
 */

// ---------- Surfaces ----------

@Composable
fun PqBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) { content() }
}

@Composable
fun PqTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }
        Text(
            title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.weight(1f).padding(start = if (onBack == null) 12.dp else 0.dp)
        )
        actions()
    }
}

@Composable
fun PqCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderSubtle,
    glow: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    // v0.5.1 (Fixes 2/3/4): Stack children vertically. The previous Box laid callers'
    // Spacers/Rows/cards on top of each other (z-axis), causing the text overlap, copy
    // button covering the query string, and Trade Fodder notes overlap observed on S25.
    // v0.5.2 (Fix 6): card padding now follows the Visual Density tokens.
    val density = currentDensity()
    Column(
        modifier
            .clip(shape)
            .background(CardDark)
            .then(if (glow) Modifier.border(1.dp, TealPrimary.copy(alpha = 0.35f), shape) else Modifier.border(1.dp, borderColor, shape))
            .padding(density.cardPadding)
    ) { content() }
}

@Composable
fun PqGlowCard(
    modifier: Modifier = Modifier,
    accent: Color = TealPrimary,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    // v0.5.1 (Fix 3): vertical stacking so the result block hierarchy (badge -> string box
    // -> copy button) lays out top-to-bottom instead of overlapping.
    // v0.5.2 (Fix 6): card padding now follows the Visual Density tokens.
    val density = currentDensity()
    Column(
        modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(CardPremium, CardDark)))
            .border(1.dp, accent.copy(alpha = 0.45f), shape)
            .padding(density.glowCardPadding)
    ) { content() }
}

// ---------- Buttons ----------

@Composable
fun PqPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = TealPrimary,
            contentColor = SlateBlack,
            disabledContainerColor = TealPrimary.copy(alpha = 0.3f),
            disabledContentColor = SlateBlack.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun PqSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
        border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

// ---------- Chips & badges ----------

@Composable
fun PqChip(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    accent: Color = TealPrimary
) {
    // v0.5.2 (Fix 6): chip padding follows the Visual Density tokens.
    val density = currentDensity()
    val shape = RoundedCornerShape(50)
    val mod = Modifier
        .clip(shape)
        .background(if (selected) accent.copy(alpha = 0.18f) else CardPremium)
        .border(1.dp, if (selected) accent else BorderSubtle, shape)
        .let { if (onClick != null) it.clickable { onClick() } else it }
        .padding(horizontal = density.chipPaddingHorizontal, vertical = density.chipPaddingVertical)
    Box(mod) {
        Text(text, color = if (selected) accent else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * v0.5.1 (Fix 5): Segmented control for mutually-exclusive modes.
 *
 * Used by PvP Candidates (Great / Ultra League) so the visible generated string is
 * clearly tied to the selected league. Fills the available width, no horizontal scroll.
 */
@Composable
fun <T> PqSegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = TealPrimary
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SlateBlack)
            .border(1.dp, BorderSubtle, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            val segmentShape = RoundedCornerShape(10.dp)
            Box(
                Modifier
                    .weight(1f)
                    .clip(segmentShape)
                    .background(if (isSelected) accent.copy(alpha = 0.22f) else Color.Transparent)
                    .then(if (isSelected) Modifier.border(1.dp, accent, segmentShape) else Modifier)
                    .let { if (options.size > 1) it.clickable { onSelect(value) } else it }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) accent else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PqRiskBadge(riskLevel: RiskLevel) {
    val (bg, fg, label) = when (riskLevel) {
        RiskLevel.High -> Triple(CoralDanger.copy(alpha = 0.18f), CoralDanger, "HIGH")
        RiskLevel.Medium -> Triple(GoldCaution.copy(alpha = 0.18f), GoldCaution, "MEDIUM")
        RiskLevel.Low -> Triple(GreenVerified.copy(alpha = 0.18f), GreenVerified, "LOW")
        RiskLevel.Info -> Triple(TealPrimary.copy(alpha = 0.18f), TealPrimary, "INFO")
    }
    PqBadge(label, fg, bg)
}

@Composable
fun PqVerificationBadge(status: VerificationStatus) {
    val (color, label) = when (status) {
        VerificationStatus.VERIFIED -> GreenVerified to "Verified"
        VerificationStatus.BETA -> GoldCaution to "Beta"
        VerificationStatus.NEEDS_VERIFICATION -> TextTertiary to "Needs check"
    }
    PqBadge(label, color)
}

@Composable
fun PqBadge(label: String, color: Color, bg: Color = color.copy(alpha = 0.16f)) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun PqTrustChip(label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(CardPremium)
            .border(1.dp, TealPrimary.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(GreenVerified, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---------- String hero box ----------

@Composable
fun PqStringBox(text: String, accent: Color = TealPrimary, modifier: Modifier = Modifier) {
    // v0.5.2 (Fix 6): the generated-string box scales its font slightly with Visual Density
    // (Compact shrinks to ~0.96x, clamped to a readable floor) so Compact shows more text.
    val density = currentDensity()
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlateBlack)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text,
            color = TealPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = density.bodySize(14f),
            lineHeight = density.bodySize(20f)
        )
    }
}

// ---------- Section header ----------

/**
 * v0.5.2 (Fix 2 + Fix 3): the canonical PokeQuery wordmark, shared by onboarding and Home.
 *
 * Replaces the raster `logo_wordmark_source.webp` (which rendered as an opaque black block
 * because the WebP carried a solid background) with a pure vector treatment drawn in the
 * brand language: white "Poke" + electric-cyan "Query", heavy weight, a soft shadow for logo
 * depth, and a spark accent over the 'Q'. Original artwork — no Pokémon font, colors, layout,
 * Poké Ball, or creatures. Both screens now share ONE definition, so the wordmark is
 * consistent everywhere instead of diverging between onboarding and Home.
 *
 * @param fontSize  wordmark size.
 * @param centered  horizontally center the wordmark (used by the onboarding hero).
 */
@Composable
fun PqWordmark(
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 30.sp,
    centered: Boolean = false
) {
    val sparkSize = (fontSize.value * 0.55f).dp
    val wordmark: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "Poke",
                color = TextPrimary,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.shadow(elevation = 4.dp, spotColor = Color.Black, ambientColor = Color.Black)
            )
            Box {
                Text(
                    "Query",
                    color = TealPrimary,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.shadow(elevation = 4.dp, spotColor = Color.Black, ambientColor = Color.Black)
                )
                // Spark accent floating over the top-right of "Query".
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-6).dp)
                        .size(sparkSize)
                )
            }
        }
    }
    if (centered) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { wordmark() }
    } else {
        Box(modifier) { wordmark() }
    }
}

@Composable
fun PqSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

/**
 * v0.5.1 (Fix 9): Branded major-heading style.
 *
 * Uses an extra-heavy weight + tight letter-spacing for a display feel on hero/major
 * screen titles only (the same brand language as the Home wordmark). Intentionally NOT
 * applied to body text — readability stays on the default sans-serif. Keep usage limited
 * to top-of-screen titles so the app does not feel childish.
 */
@Composable
fun PqBrandTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp
) {
    Text(
        text,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.3).sp,
        modifier = modifier
    )
}

// ---------- Empty state ----------

@Composable
fun PqEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    cta: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(96.dp).clip(CircleShape).background(CardPremium)
                .border(1.dp, BorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        if (cta != null) { Spacer(Modifier.height(20.dp)); cta() }
    }
}

// ---------- Coming Later (disabled feature) ----------

@Composable
fun PqComingLaterCard(title: String, description: String) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardDark.copy(alpha = 0.6f))
            .border(1.dp, BorderSubtle, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = TextTertiary, fontSize = 11.sp)
        }
        PqBadge("Coming later", TextTertiary)
    }
}

// ---------- Manual review panel ----------

@Composable
fun PqManualReviewPanel(text: String = "This is a review aid only. Always inspect matches in Pokémon GO before transferring or trading.") {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(GoldCaution.copy(alpha = 0.08f))
            .border(1.dp, GoldCaution.copy(alpha = 0.35f), shape).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(6.dp).background(GoldCaution, CircleShape).padding(top = 2.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
    }
}
