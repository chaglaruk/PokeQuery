package com.caglar.pokequery.ui.components

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties

/** Keep app language and text scale inside the separate Android dialog window. */
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) {
    val appContext = LocalContext.current
    val appConfiguration = LocalConfiguration.current
    val appDensity = LocalDensity.current

    @Composable
    fun AppContent(content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalContext provides appContext,
            LocalConfiguration provides appConfiguration,
            LocalDensity provides appDensity,
            content = content
        )
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { AppContent(confirmButton) },
        modifier = modifier,
        dismissButton = if (dismissButton != null) { { AppContent(dismissButton) } } else null,
        icon = if (icon != null) { { AppContent(icon) } } else null,
        title = if (title != null) { { AppContent(title) } } else null,
        text = if (text != null) { { AppContent(text) } } else null,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties
    )
}
