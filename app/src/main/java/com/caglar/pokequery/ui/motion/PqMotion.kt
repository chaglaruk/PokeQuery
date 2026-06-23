package com.caglar.pokequery.ui.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * v0.5.3 motion polish — PokeQuery's shared, subtle motion layer.
 *
 * Design goal: make the app feel **alive and premium**, not game-like. Everything here is
 * deliberately small and short. The two governing invariants are:
 *
 *  1. **Entrance runs once.** A screen's entrance animation is driven by a single hoisted
 *     `visible` flag owned by [PqStaggeredEntrance]. Item modifiers ([pqStaggeredItem],
 *     [pqSpringPop]) compute their *target* purely from `(visible, index)` — they never gate
 *     on their own first appearance. Therefore, when a `LazyColumn` recycles an offscreen item
 *     *after* the entrance is complete, that item's target is already at rest (alpha 1,
 *     offset 0, scale 1) and it simply appears with **no** animation. Scrolling never replays
 *     the entrance.
 *  2. **Reduced-motion collapses everything to instant.** When the OS animation scale is off,
 *     every primitive below renders at its final state with no animation (see [ReduceMotion]).
 *
 * All APIs are built on Compose's built-in animation primitives. No third-party libraries.
 */

// ------------------------------------------------------------------ tokens

/**
 * Centralized durations and specs. Nothing animates outside these numbers, which keeps the
 * motion consistent and lets the "reduce until premium" dial live in one place.
 */
object PqMotionTokens {
    /** Screen-to-screen crossfade (the navigation transition). */
    const val SCREEN_CROSSFADE_MS = 180
    const val CROSSFADE_FADE_MS = 120

    /** Staggered entrance. */
    const val STAGGER_STEP_MS = 65
    const val STAGGER_SLIDE_DP = 10
    const val STAGGER_DURATION_MS = 230
    const val MAX_STAGGER_INDEX = 6   // beyond this, items appear without further delay

    /** Icon spring-pop. Subtle overshoot — never cartoonish. */
    const val ICON_POP_FROM = 0.88f
    val iconSpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.72f,            // gently under-damped → one small overshoot, settles fast
        stiffness = Spring.StiffnessMediumLow
    )

    /** Primary-button loading morph (pure visual confirmation; never delays the action). */
    const val BUTTON_MORPH_MS = 150
}

// ---------------------------------------------------- motion configuration

/**
 * Resolved motion configuration for a composition: whether to reduce motion, plus the tokens.
 * Provided via [LocalPqMotion] at the navigation root.
 */
data class PqMotionConfig(
    val reducedMotion: Boolean,
    val tokens: PqMotionTokens = PqMotionTokens
) {
    /** Effective stagger step: 0 when reduced (→ all items appear at once, instantly). */
    val staggerStepMs: Int get() = if (reducedMotion) 0 else tokens.STAGGER_STEP_MS
}

/** The default config used when no provider is present (e.g. previews, tests). Animations OFF. */
val DefaultPqMotionConfig = PqMotionConfig(reducedMotion = true)

val LocalPqMotion = staticCompositionLocalOf { DefaultPqMotionConfig }

/**
 * Provides the resolved [PqMotionConfig] to descendants. Resolves reduced-motion once for the
 * composition (see [rememberReducedMotion] — single cached system read, no repeated access).
 */
@Composable
fun ProvidePqMotion(content: @Composable () -> Unit) {
    val config = PqMotionConfig(reducedMotion = rememberReducedMotion())
    CompositionLocalProvider(LocalPqMotion provides config) { content() }
}

// --------------------------------------------------- once-only entrance

/**
 * The single screen-level entrance driver.
 *
 * Hoists one `visible` flag that flips to `true` **exactly once** on first composition (via a
 * one-shot [LaunchedEffect]). Callers receive [visible] and tag their children with
 * [Modifier.pqStaggeredItem] / [Modifier.pqSpringPop]. Because each item's animation target is a
 * pure function of `(visible, index)` — never of the item's own first composition — a
 * `LazyColumn` item that first composes *after* the entrance is complete animates instantly from
 * rest (its target is already reached) → scrolling never replays the cascade (C1).
 *
 * Passing `visible` as a parameter (rather than via a scope receiver) is deliberate: it lets
 * callers build the stagger modifiers inside `LazyColumn`/`item { }` blocks, where a custom
 * receiver scope would be shadowed by `LazyListScope`. Top-level modifier extensions have no such
 * restriction.
 */
@Composable
fun PqStaggeredEntrance(content: @Composable (visible: Boolean) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    // One-shot: flip on once, never back. Reduced-motion still flips it (targets jump to rest);
    // the only difference is the per-item animation specs collapse to instant.
    LaunchedEffect(Unit) { visible = true }
    content(visible)
}

// ------------------------------------------------------- public modifiers

/**
 * Item entrance: a fade + small upward slide that settles at the at-rest position. The slide
 * offset animates from `+STAGGER_SLIDE_DP` to `0`, so the final layout is identical to the
 * non-animated layout (no post-animation shift).
 *
 * Safe inside `LazyColumn` items: the target depends only on [visible] and [index], so an item
 * that first composes after the entrance is done appears instantly. Pass the [visible] flag
 * provided by [PqStaggeredEntrance].
 */
fun Modifier.pqStaggeredItem(visible: Boolean, index: Int): Modifier = composed {
    val config = LocalPqMotion.current
    if (config.reducedMotion || !visible) {
        // Reduced-motion, or not yet started, or already past entrance: render at rest.
        this
    } else {
        val clampedIndex = index.coerceIn(0, config.tokens.MAX_STAGGER_INDEX)
        // A single per-item Animatable; target is at-rest once `visible` is true. When a recycled
        // item composes after entrance, target == current → no visible animation.
        val progress = remember { Animatable(0f) }
        LaunchedEffect(visible) {
            // `visible` is the only key. Once true it stays true, so this runs once per item.
            if (visible) {
                val delayMs = clampedIndex * config.staggerStepMs
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = config.tokens.STAGGER_DURATION_MS,
                        delayMillis = delayMs
                    )
                )
            }
        }
        val slideDp = config.tokens.STAGGER_SLIDE_DP
        val p = progress.value
        this
            .alpha(p)
            .offset { IntOffset(0, ((1f - p) * slideDp).dp.roundToPx()) }
    }
}

/**
 * Icon spring-pop: a one-shot scale `ICON_POP_FROM → 1` with a gentle overshoot. **Icons /
 * illustrations only** — never body text, search strings, settings rows, nav labels, or copy
 * buttons. Pass the [visible] flag provided by [PqStaggeredEntrance].
 */
fun Modifier.pqSpringPop(visible: Boolean): Modifier = composed {
    val config = LocalPqMotion.current
    if (config.reducedMotion || !visible) {
        this
    } else {
        val scale = remember { Animatable(config.tokens.ICON_POP_FROM) }
        LaunchedEffect(visible) {
            if (visible) {
                scale.animateTo(targetValue = 1f, animationSpec = config.tokens.iconSpring)
            }
        }
        this.scale(scale.value)
    }
}

// --------------------------------------------------- animated visibility

/**
 * Centralized enter/exit spec for expand/collapse animations (Knowledge Base term rows,
 * Preset preview, etc.). Existing call sites keep their own `AnimatedVisibility`, but new ones
 * should use this so the motion language stays consistent.
 */
@Composable
fun PqAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val config = LocalPqMotion.current
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(if (config.reducedMotion) 0 else 180)) +
            slideInVertically(tween(if (config.reducedMotion) 0 else 180)) { it / 6 },
        exit = fadeOut(tween(if (config.reducedMotion) 0 else 120)) +
            slideOutVertically(tween(if (config.reducedMotion) 0 else 120)) { it / 6 }
    ) { content() }
}
