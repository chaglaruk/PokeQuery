package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.domain.expert.ExpertQueryModel
import com.caglar.pokequery.domain.lint.ExpertCopyPolicy
import com.caglar.pokequery.domain.lint.Linter
import com.caglar.pokequery.theme.BackgroundDark
import com.caglar.pokequery.theme.CardDark
import com.caglar.pokequery.theme.CoralDanger
import com.caglar.pokequery.theme.GoldCaution
import com.caglar.pokequery.theme.TealPrimary
import com.caglar.pokequery.theme.TextPrimary
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.ui.pq.PqChip
import com.caglar.pokequery.ui.pq.PqPrimaryButton
import com.caglar.pokequery.ui.pq.PqSectionHeader
import com.caglar.pokequery.ui.pq.PqStringBox

// v0.5.0 Stitch Expert Builder — modular chip editor (not a code editor).
// Live preview + linter assistant. Raw query is an optional advanced mode.
// ExpertCopyPolicy still blocks copy on linter errors (safety unchanged from v0.4.2).

@Composable
fun ExpertBuilderScreen(
    onGenerate: (String) -> Unit,
    onBack: () -> Unit
) {
    var model by remember { mutableStateOf(ExpertQueryModel()) }
    var advancedMode by remember { mutableStateOf(false) }
    var rawOverride by remember { mutableStateOf("") }

    val rawQuery = if (advancedMode) rawOverride else model.buildRawQuery()
    val warnings = Linter.lint(rawQuery)
    val copyBlocked = !ExpertCopyPolicy.canCopy(rawQuery)

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text("Expert Builder", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { advancedMode = !advancedMode; if (!advancedMode) rawOverride = "" }) {
                Icon(Icons.Default.Code, contentDescription = null, tint = if (advancedMode) TealPrimary else TextSecondary, modifier = Modifier.padding(end = 6.dp))
                Text(if (advancedMode) "Chips" else "Raw", color = if (advancedMode) TealPrimary else TextSecondary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (advancedMode) {
            PqSectionHeader("RAW QUERY")
            OutlinedTextField(
                value = rawOverride,
                onValueChange = { rawOverride = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, color = TealPrimary, fontSize = 14.sp),
                placeholder = { Text("e.g. 4*&!shiny", color = TextSecondary) }
            )
        } else {
            PqSectionHeader("ADD CONDITION")
            ChipRow(
                options = listOf("shiny", "legendary", "shadow", "lucky", "traded", "costume"),
                selected = model.positiveTokens,
                onToggle = { model = model.togglePositive(it) }
            )
            Spacer(Modifier.height(14.dp))

            PqSectionHeader("IV FLOOR (ATTACK)")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2, 3, 4).forEach { floor ->
                    PqChip(
                        text = "${floor}attack",
                        selected = model.ivAttackFloor == floor,
                        onClick = { model = model.setIvAttack(if (model.ivAttackFloor == floor) null else floor) }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            PqSectionHeader("DUPLICATE COUNT")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(2, 3, 4).forEach { floor ->
                    PqChip(
                        text = "count$floor-",
                        selected = model.countFloor == floor,
                        onClick = { model = model.setCount(if (model.countFloor == floor) null else floor) }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            PqSectionHeader("EXCLUDE (PROTECT)")
            ChipRow(
                options = listOf("shiny", "legendary", "traded", "favorite", "lucky", "costume", "4*"),
                selected = model.exclusions,
                isExclusion = true,
                onToggle = { model = model.toggleExclusion(it) }
            )
        }

        Spacer(Modifier.height(18.dp))

        PqSectionHeader("LIVE PREVIEW")
        PqStringBox(rawQuery.ifEmpty { "—" })

        if (warnings.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            PqSectionHeader("LINTER ASSISTANT")
            val shape = RoundedCornerShape(14.dp)
            Column(
                Modifier.fillMaxWidth().clip(shape).background(CardDark)
                    .border(1.dp, if (copyBlocked) CoralDanger.copy(alpha = 0.4f) else GoldCaution.copy(alpha = 0.3f), shape)
                    .padding(14.dp)
            ) {
                warnings.forEach { w ->
                    Text(
                        "• ${w.message}",
                        color = if (w.isError) CoralDanger else GoldCaution,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                if (copyBlocked) {
                    Spacer(Modifier.height(6.dp))
                    Text("Fix the errors above before copying.", color = CoralDanger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        PqPrimaryButton(
            text = "Copy Custom String",
            onClick = { if (!copyBlocked) onGenerate(rawQuery) },
            enabled = !copyBlocked
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChipRow(
    options: List<String>,
    selected: Set<String>,
    isExclusion: Boolean = false,
    onToggle: (String) -> Unit
) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            val isSelected = opt in selected
            PqChip(
                text = if (isExclusion && isSelected) "!$opt" else opt,
                selected = isSelected,
                onClick = { onToggle(opt) }
            )
        }
    }
}
