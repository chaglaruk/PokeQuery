package com.caglar.pokequery.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caglar.pokequery.R
import com.caglar.pokequery.domain.expert.ExpertQueryModel
import com.caglar.pokequery.domain.locale.OfficialSearchSyntax
import com.caglar.pokequery.theme.TextSecondary
import com.caglar.pokequery.ui.pq.PqChip
import com.caglar.pokequery.ui.pq.PqSectionHeader

private data class OfficialFilterSection(
    val key: String,
    val values: List<String>
)

private val commonStatusTokens = setOf(
    "shiny", "legendary", "mythical", "ultrabeast", "shadow", "purified", "costume",
    "lucky", "traded", "defender"
)

private val extraOfficialSections = listOf(
    OfficialFilterSection(
        "official",
        OfficialSearchSyntax.statusValues.filterNot { it in commonStatusTokens }
    ),
    OfficialFilterSection("appraisal", OfficialSearchSyntax.appraisalValues),
    OfficialFilterSection("size", OfficialSearchSyntax.sizeValues),
    OfficialFilterSection("buddy", OfficialSearchSyntax.buddyValues),
    OfficialFilterSection("mega", OfficialSearchSyntax.megaLevelValues),
    OfficialFilterSection("region", OfficialSearchSyntax.regions)
    // Pokémon type remains a fully documented OfficialSearchSyntax family but is deliberately
    // entered in Raw mode. Type names are language-sensitive values, not canonical keyword
    // prefixes, and SearchTermMapper must never emit an English type into a localized client.
)

@Composable
internal fun OfficialSearchFilterSections(
    model: ExpertQueryModel,
    onModelChange: (ExpertQueryModel) -> Unit
) {
    extraOfficialSections.forEach { section ->
        Spacer(Modifier.height(16.dp))
        PqSectionHeader(stringResource(officialSectionTitleRes(section.key)))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            section.values.forEach { token ->
                PqChip(
                    text = token,
                    selected = model.isFilterSelected(section.key, token),
                    onClick = { onModelChange(model.toggleFilter(section.key, token)) }
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))
    Text(
        text = stringResource(R.string.official_filter_parameterized_hint),
        color = TextSecondary,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
}

@Composable
internal fun OfficialRawSyntaxHint() {
    Spacer(Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.official_filter_raw_hint),
        color = TextSecondary,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
}

@StringRes
private fun officialSectionTitleRes(key: String): Int = when (key) {
    "official" -> R.string.official_filter_more
    "appraisal" -> R.string.official_filter_appraisal
    "size" -> R.string.official_filter_size
    "buddy" -> R.string.official_filter_buddy_level
    "mega" -> R.string.official_filter_mega_level
    "region" -> R.string.official_filter_region
    else -> R.string.official_filter_default
}
