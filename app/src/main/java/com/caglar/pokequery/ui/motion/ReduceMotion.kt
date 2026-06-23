package com.caglar.pokequery.ui.motion

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * v0.5.3 motion polish — reduced-motion detection.
 *
 * PokeQuery honors the user's Android Developer Options animation scale. When the user has
 * turned animations off (any of the three global scales == 0), every motion primitive in
 * [PqMotion] collapses to instant (no stagger, no bounce, no button morph).
 *
 * Safety contract (this is the whole reason this lives in its own file):
 *  - **Read-only.** Only reads [Settings.Global]. Never writes a setting.
 *  - **No side effects.** Does not change locale, recreate the Activity, or call any stateful
 *    system API. It performs a single read and caches the result for the composition.
 *  - **No permission required.** Reading the global animation scales is allowed without a
 *    manifest permission on every supported API level (minSdk 24 .. targetSdk 36).
 *  - **Failure-safe.** The entire read is wrapped in [runCatching]. On any failure (some OEM
 *    ROMs restrict Settings.Global reads), it returns `false` so animations stay ON — we
 *    never silently leave the app motionless because a system read was denied.
 *
 * This intentionally reads only the OS developer-option setting. A future release may add an
 * in-app motion toggle; for v0.5.3 the OS setting is mirrored with no new permission or
 * setting of our own.
 */
object ReduceMotion {

    /**
     * Reads the three global animation scales and returns `true` when the user has effectively
     * disabled animation (any scale == 0). Pure function over [Context]; cached by the caller.
     */
    fun systemReducedMotion(context: Context): Boolean = runCatching {
        val animator = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        val transition = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        val window = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            1f
        )
        // A scale of 0 means "off" in Developer Options. Treat any of the three as fully reduced.
        animator == 0f || transition == 0f || window == 0f
    }.getOrDefault(false)
}

/**
 * Remembers the reduced-motion state for the current composition.
 *
 * Resolved once per composition via [remember]; not re-read on every recomposition or frame, so
 * there is no repeated system API access (see the safety contract on [ReduceMotion]).
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) { ReduceMotion.systemReducedMotion(context) }
}
