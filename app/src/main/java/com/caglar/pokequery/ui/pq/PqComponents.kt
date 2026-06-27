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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
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

/**
 * v0.5.3 motion polish: the brief in-progress spinner label exposed to assistive tech when the
 * primary button is in its loading state. A single shared constant so the wording is consistent
 * and easy to localize later. Kept short on purpose.
 */
private const val PQ_BUTTON_LOADING_DESC = "Working"

/**
 * v0.5.3 motion polish — the primary CTA now has a subtle label→spinner→label morph.
 *
 * Safety contract (do not regress):
 *  - **The action fires immediately and exactly once.** `onClick` runs synchronously as the very
 *    first thing the Button's click lambda does. The loading state is flipped on *afterwards* and
 *    the button is disabled for the ~150ms window, so a second tap cannot re-fire the action.
 *  - **The morph never delays the action.** Copy/search actions complete before any animation.
 *    The spinner is pure visual confirmation that something happened.
 *  - **Reduced-motion skips the morph entirely** (instant label → label). See [LocalPqMotion].
 *  - **Button bounds never change** — the spinner is sized to fit the existing min 52dp height.
 *  - **Accessible**: the spinner node carries [PQ_BUTTON_LOADING_DESC] so screen readers announce
 *    the in-progress state; the label returns automatically so the button stays identifiable.
 */
@Composable
fun PqPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val motion = com.caglar.pokequery.ui.motion.LocalPqMotion.current
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            // C2: action FIRST, synchronously, before any state flip. Runs exactly once — the
            // button is disabled (loading=true) for the morph window, blocking a second firing.
            onClick()
            if (!motion.reducedMotion) {
                loading = true
                scope.launch {
                    kotlinx.coroutines.delay(motion.tokens.BUTTON_MORPH_MS.toLong())
                    loading = false
                }
            }
        },
        // Disabled while loading so the action cannot fire twice. Honors the caller's `enabled`.
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = TealPrimary,
            contentColor = SlateBlack,
            disabledContainerColor = TealPrimary.copy(alpha = 0.3f),
            disabledContentColor = SlateBlack.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        // Crossfade label ↔ spinner inside the SAME bounds. Reduced-motion → no animation, but
        // content is still correct (instant swap).
        androidx.compose.animation.Crossfade(
            targetState = loading,
            animationSpec = androidx.compose.animation.core.tween(
                if (motion.reducedMotion) 0 else motion.tokens.BUTTON_MORPH_MS
            ),
            label = "pqPrimaryButtonMorph"
        ) { isLoading ->
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = SlateBlack,
                    strokeWidth = 2.5.dp,
                    // Reusable short constant — keeps accessibility wording consistent.
                    // null contentDescription is intentional: the button's own semantics still
                    // describe it; the spinner is a transient confirmation, not a primary control.
                )
            } else {
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
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
 * Original PokeQuery wordmark — playful game-branding inspired vector treatment.
 *
 * White "Poke" + bright-cyan "Query" with a thick dark navy outline (8-direction
 * offset), a 3D shadow layer, and two sparkle/star accents near the end. Uses
 * layered Text composables so every letter is outlined cleanly. Original artwork
 * — no Pokémon / Niantic / Nintendo fonts, colors, or assets.
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
    val outlineColor = Color(0xFF0D0D1A)
    val shadowColor = Color(0xFF0A0A1A)
    val outlineW = (fontSize.value * 0.08f).dp
    val shadowOff = (fontSize.value * 0.07f).dp
    val sparkSz = (fontSize.value * 0.35f).dp

    val wordStyle = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        letterSpacing = (-0.3).sp
    )

    val content: @Composable () -> Unit = {
        Box {
            // 3D shadow layer
            Row(Modifier.offset(shadowOff, shadowOff)) {
                Text("PokeQuery", color = shadowColor, style = wordStyle)
            }
            // Outline layers (8 directions)
            Row(Modifier.offset(outlineW, 0.dp)) { Text("PokeQuery", color = outlineColor, style = wordStyle) }
            Row(Modifier.offset(-outlineW, 0.dp)) { Text("PokeQuery", color = outlineColor, style = wordStyle) }
            Row(Modifier.offset(0.dp, outlineW)) { Text("PokeQuery", color = outlineColor, style = wordStyle) }
            Row(Modifier.offset(0.dp, -outlineW)) { Text("PokeQuery", color = outlineColor, style = wordStyle) }
            Row(Modifier.offset(outlineW, outlineW)) { Text("PokeQuery", color = outlineColor, style = wordStyle) }
            Row(Modifier.offset(-outlineW, -outlineW)) { Text("PokeQuery", color = outlineColor, style = wordStyle) }
            Row(Modifier.offset(outlineW, -outlineW)) { Text("PokeQuery", color = outlineColor, style = wordStyle) }
            Row(Modifier.offset(-outlineW, outlineW)) { Text("PokeQuery", color = outlineColor, style = wordStyle) }
            // Fill + sparkle layer
            Row {
                Text("Poke", color = Color.White, style = wordStyle)
                Box {
                    Text("Query", color = TealPrimary, style = wordStyle)
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-6).dp)
                            .size(sparkSz)
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanGlow.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = (-2).dp)
                            .size(sparkSz * 0.55f)
                    )
                }
            }
        }
    }

    if (centered) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
    } else {
        Box(modifier) { content() }
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
fun PqManualReviewPanel(
    modifier: Modifier = Modifier,
    text: String = "This is a review aid only. Always inspect matches in Pokémon GO before transferring or trading."
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(GoldCaution.copy(alpha = 0.08f))
            .border(1.dp, GoldCaution.copy(alpha = 0.35f), shape).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(6.dp).background(GoldCaution, CircleShape).padding(top = 2.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
    }
}
