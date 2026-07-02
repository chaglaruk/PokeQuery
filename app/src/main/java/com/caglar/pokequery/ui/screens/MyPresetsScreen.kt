package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.caglar.pokequery.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.PersonalPreset
import com.caglar.pokequery.data.repository.UserPreferencesRepository
import com.caglar.pokequery.data.repository.dataStore
import com.caglar.pokequery.theme.AmberWarning
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.BorderDark
import com.caglar.pokequery.theme.BorderSubtle
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CyanGlow
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.theme.TextTertiary
import com.caglar.pokequery.theme.density.currentDensity
import com.caglar.pokequery.ui.components.ScreenTitleBar
import com.caglar.pokequery.ui.motion.PqStaggeredEntrance
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.pq.PqEmptyState
import com.caglar.pokequery.ui.pq.PqPrimaryButton
import com.caglar.pokequery.ui.pq.PqRiskBadge
import com.caglar.pokequery.ui.pq.PqStringBox
import com.caglar.pokequery.ui.pq.PqSectionHeader
import kotlinx.coroutines.launch

/**
 * v0.6.1 — Personal Presets ("My Presets").
 *
 * Personal presets are LOCAL ONLY: never synced, never uploaded, never account-bound. They are
 * created from a Favorite, a History entry, or a freshly generated string (via
 * [PersonalPreset.fromFavorite]/[PersonalPreset.fromGenerated]).
 *
 * Risk gating is preserved: a Medium/High personal preset still routes through RiskWarning before
 * copy. Saving a string as a preset never downgrades its risk level.
 */
@Composable
fun MyPresetsScreen(
    onBack: () -> Unit,
    onCopy: (GeneratedString) -> Unit,
    onNavigateRisk: (GeneratedString) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val presets = userPrefs?.personalPresets
    val density = currentDensity()

    var renaming by remember { mutableStateOf<PersonalPreset?>(null) }

    PqStaggeredEntrance { visible ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(density.listGap),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item {
                ScreenTitleBar(stringResource(R.string.goal_my_presets), onBack, Modifier.pqStaggeredItem(visible, 0).padding(bottom = 4.dp))
            }
            item { LocalOnlyBanner(Modifier.pqStaggeredItem(visible, 1)) }
            when {
                presets == null -> item {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TealPrimary)
                    }
                }
                presets.isEmpty() -> item {
                    Box(Modifier.pqStaggeredItem(visible, 2)) {
                        PqEmptyState(
                            icon = Icons.Default.Bookmark,
                            title = stringResource(R.string.my_presets_empty_title),
                            subtitle = stringResource(R.string.goal_my_presets_desc)
                        )
                    }
                }
                else -> {
                    item { PqSectionHeader(stringResource(R.string.goal_my_presets).uppercase()) }
                    items(presets, key = { it.id }) { preset ->
                        PersonalPresetRow(
                            preset = preset,
                            onCopy = {
                                val generated = preset.asGeneratedString()
                                if (requiresRiskWarningInline(generated.riskLevel)) onNavigateRisk(generated) else onCopy(generated)
                            },
                            onRename = { renaming = preset },
                            onDelete = { scope.launch { repository.removePersonalPreset(preset.id) } }
                        )
                    }
                }
            }
        }
    }

    renaming?.let { preset ->
        RenameDialog(
            currentTitle = preset.title,
            onDismiss = { renaming = null },
            onConfirm = { newTitle ->
                scope.launch { repository.renamePersonalPreset(preset.id, newTitle) }
                renaming = null
            }
        )
    }
}

@Composable
private fun LocalOnlyBanner(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(CyanGlow.copy(alpha = 0.08f))
            .border(1.dp, CyanGlow.copy(alpha = 0.35f), shape).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(6.dp).background(CyanGlow, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.my_presets_local_banner),
            color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun PersonalPresetRow(
    preset: PersonalPreset,
    onCopy: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val density = currentDensity()
    val shape = RoundedCornerShape(14.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(CardDark).border(1.dp, BorderSubtle, shape).padding(density.cardPadding)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(preset.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    PqBadge(text = stringResource(R.string.my_presets_local), color = CyanGlow)
                    PqBadge(text = preset.riskLevel.name, color = AmberWarning)
                }
            }
            IconButton(onClick = onRename) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.my_presets_rename), tint = TextSecondary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = CoralDanger) }
        }
        Spacer(Modifier.height(density.innerElementGap))
        PqStringBox(preset.queryString)
        Spacer(Modifier.height(12.dp))
        PqPrimaryButton(text = stringResource(R.string.action_use_preset), onClick = onCopy)
    }
}

@Composable
private fun PqBadge(text: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun RenameDialog(currentTitle: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var title by remember { mutableStateOf(currentTitle) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text(stringResource(R.string.my_presets_rename), color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = TealPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title.ifBlank { currentTitle }) }) {
                Text(stringResource(R.string.action_save), color = TealPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = TextSecondary) } }
    )
}

/** Mirrors com.caglar.pokequery.requiresRiskWarning without an extra import cycle. */
private fun requiresRiskWarningInline(riskLevel: com.caglar.pokequery.data.model.RiskLevel): Boolean =
    riskLevel == com.caglar.pokequery.data.model.RiskLevel.Medium ||
        riskLevel == com.caglar.pokequery.data.model.RiskLevel.High
