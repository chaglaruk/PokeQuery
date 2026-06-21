package com.caglar.pokequery.theme

import androidx.compose.ui.graphics.Color

// v0.5.0 Stitch design tokens (PRD §2):
//   Deep Navy (#0A0E1A) + Slate Black background; Electric Cyan (#00E5FF) primary;
//   Gold (#FFD700) caution; Green verified; Red high-risk.
// Names kept stable so existing screens keep compiling; values aligned to Stitch.

val BackgroundDark = Color(0xFF0A0E1A)  // Deep navy background (Stitch #0A0E1A)
val SlateBlack = Color(0xFF050709)      // Slate black for cards / nav
val CardDark = Color(0xFF0F1422)        // Premium dark card
val CardPremium = Color(0xFF121A2E)     // Slightly elevated card surface
val TealPrimary = Color(0xFF00E5FF)     // Electric cyan primary accent
val CyanGlow = Color(0xFF1DE9FF)        // Brighter cyan for glows
val BlueCTA = Color(0xFF00E5FF)         // Primary CTA now cyan (Stitch direction)
val GoldCaution = Color(0xFFFFD700)     // Gold caution / Beta / Medium risk (Stitch #FFD700)
val AmberWarning = Color(0xFFFFD700)    // Alias kept for back-compat = GoldCaution
val CoralDanger = Color(0xFFFF5252)     // High risk
val RedHigh = Color(0xFFFF5252)         // Alias = CoralDanger
val GreenVerified = Color(0xFF00E676)   // Verified status badge
val PurpleIV = Color(0xFFB14BFF)        // Hundo accent (lightened for contrast)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0BEC5)   // High-contrast slate (Stitch: readable grey)
val TextTertiary = Color(0xFF6B7A8F)    // Dimmed hint text
val BorderDark = Color(0xFF1E3A5F)      // Glow border
val BorderSubtle = Color(0xFF1A2238)    // Subtle card border
val BottomNavBackground = Color(0xFF050709) // Slate black nav (Stitch)
val BottomNavSelected = Color(0xFF00E5FF)   // Cyan selection
