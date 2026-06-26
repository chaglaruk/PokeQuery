package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.data.model.CleaningJournalEntry
import com.caglar.pokequery.data.model.JournalAction
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
import com.caglar.pokequery.ui.motion.pqSpringPop
import com.caglar.pokequery.ui.motion.pqStaggeredItem
import com.caglar.pokequery.ui.pq.PqEmptyState
import com.caglar.pokequery.ui.pq.PqPrimaryButton
import kotlinx.coroutines.launch

/**
 * v0.6.1 — Cleaning Journal.
 *
 * A USER-ENTERED memory aid. The user keeps a note when they manually use a query in Pokémon GO.
 *
 * Critical honesty contract (do not regress): PokeQuery CANNOT and MUST NOT know what Pokémon were
 * deleted/transferred/traded. Copying a string never automatically creates an "applied" note. The
 * action types ("Applied manually", "Cleanup session", ...) are user-asserted memory only — never
 * auto-populated by the copy flow. The banner says this plainly.
 *
 * Local only: nothing here is synced, uploaded, or account-bound.
 */
@Composable
fun CleaningJournalScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { UserPreferencesRepository(context.dataStore) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val journal = userPrefs?.journal
    val density = currentDensity()

    var editing by remember { mutableStateOf<CleaningJournalEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    PqStaggeredEntrance { visible ->
        Box(Modifier.fillMaxSize().background(BackgroundDark)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(density.listGap),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
            ) {
                item {
                    ScreenTitleBar("Cleaning Journal", onBack, Modifier.pqStaggeredItem(visible, 0).padding(bottom = 4.dp))
                }
                item { HonestyBanner(Modifier.pqStaggeredItem(visible, 1)) }
                item {
                    // FAB-like primary button at the top of the list so it is reachable above the fold.
                    PqPrimaryButton(
                        text = "Add a journal note",
                        onClick = { editing = null; showEditor = true },
                        leadingIcon = Icons.Default.Add
                    )
                }
                when {
                    journal == null -> item {
                        Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TealPrimary)
                        }
                    }
                    journal.isEmpty() -> item {
                        Box(Modifier.pqStaggeredItem(visible, 2).pqSpringPop(visible)) {
                            PqEmptyState(
                                icon = Icons.Default.EditNote,
                                title = "No journal notes yet",
                                subtitle = "Keep a manual note when you use a query in Pokémon GO."
                            )
                        }
                    }
                    else -> items(journal, key = { it.id }) { entry ->
                        JournalEntryRow(
                            entry = entry,
                            onEdit = { editing = entry; showEditor = true },
                            onDelete = { scope.launch { repository.removeJournal(entry.id) } }
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {
        JournalEditorDialog(
            existing = editing,
            onDismiss = { showEditor = false; editing = null },
            onSave = { entry ->
                scope.launch {
                    if (editing == null) repository.addJournal(entry) else repository.updateJournal(entry)
                }
                showEditor = false
                editing = null
            }
        )
    }
}

@Composable
private fun HonestyBanner(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(AmberWarning.copy(alpha = 0.08f))
            .border(1.dp, AmberWarning.copy(alpha = 0.35f), shape).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(6.dp).background(AmberWarning, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(
            "This is your manual memory only. PokeQuery does not know what you deleted, traded, or " +
                "transferred — copying a string never records an \"applied\" action here. Choose the " +
                "action type yourself.",
            color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp
        )
    }
}

@Composable
private fun JournalEntryRow(
    entry: CleaningJournalEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(CardDark).border(1.dp, BorderSubtle, shape).padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                ActionChip(entry.actionType)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.EditNote, contentDescription = "Edit", tint = TextSecondary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralDanger) }
        }
        Spacer(Modifier.height(6.dp))
        Text(entry.queryString, color = TealPrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp)
        if (entry.note.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(entry.note, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun ActionChip(action: JournalAction) {
    val color = when (action) {
        JournalAction.COPIED, JournalAction.REVIEWED -> CyanGlow
        else -> AmberWarning
    }
    Row(
        Modifier.padding(top = 4.dp).clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(action.label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun JournalEditorDialog(
    existing: CleaningJournalEntry?,
    onDismiss: () -> Unit,
    onSave: (CleaningJournalEntry) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var queryString by remember { mutableStateOf(existing?.queryString ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var action by remember { mutableStateOf(existing?.actionType ?: JournalAction.REVIEWED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text(if (existing == null) "Add journal note" else "Edit note", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                LabeledField("Title", title) { title = it }
                Spacer(Modifier.height(10.dp))
                LabeledField("Search string", queryString, mono = true) { queryString = it }
                Spacer(Modifier.height(10.dp))
                Text("Note", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    placeholder = { Text("What did you do? (your words only)", color = TextTertiary) },
                    colors = outlineColors(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
                Spacer(Modifier.height(10.dp))
                Text("Action type (you choose)", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                JournalAction.entries.forEach { a ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { action = a }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(selected = action == a, onClick = { action = a }, colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = TealPrimary))
                        Text(a.label, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val finalTitle = title.ifBlank { "Journal note" }
                val finalQuery = queryString.ifBlank { "(no string)" }
                val entry = if (existing == null) {
                    CleaningJournalEntry.new(finalQuery, finalTitle, note, action)
                } else {
                    existing.copy(title = finalTitle, queryString = finalQuery, note = note, actionType = action)
                }
                onSave(entry)
            }) { Text("Save", color = TealPrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

@Composable
private fun LabeledField(label: String, value: String, mono: Boolean = false, onChange: (String) -> Unit) {
    Text(label, color = TextSecondary, fontSize = 12.sp)
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = TextPrimary,
            fontFamily = if (mono) androidx.compose.ui.text.font.FontFamily.Monospace else null,
            fontSize = 13.sp
        ),
        singleLine = true,
        colors = outlineColors(),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun outlineColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealPrimary,
    unfocusedBorderColor = BorderDark,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = TealPrimary
)
