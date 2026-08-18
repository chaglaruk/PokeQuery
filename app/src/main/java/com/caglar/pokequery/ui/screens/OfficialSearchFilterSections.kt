package com.caglar.pokequery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    OfficialFilterSection("region", OfficialSearchSyntax.regions),
    OfficialFilterSection("type", OfficialSearchSyntax.pokemonTypes)
)

@Composable
internal fun OfficialSearchFilterSections(
    model: ExpertQueryModel,
    onModelChange: (ExpertQueryModel) -> Unit
) {
    val lang = LocalConfiguration.current.locales[0].language
    extraOfficialSections.forEach { section ->
        Spacer(Modifier.height(16.dp))
        PqSectionHeader(officialSectionTitle(section.key, lang))
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
        text = parameterizedFamilyHint(lang),
        color = TextSecondary,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
}

@Composable
internal fun OfficialRawSyntaxHint() {
    val lang = LocalConfiguration.current.locales[0].language
    Spacer(Modifier.height(10.dp))
    Text(
        text = rawModeHint(lang),
        color = TextSecondary,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
}

private fun officialSectionTitle(key: String, lang: String): String = when (lang) {
    "tr" -> when (key) {
        "official" -> "Diğer Resmi Filtreler"
        "appraisal" -> "Değerlendirme"
        "size" -> "Boyut"
        "buddy" -> "Dost Seviyesi"
        "mega" -> "Mega Seviyesi"
        "region" -> "Bölge"
        "type" -> "Pokémon Türü"
        else -> "Resmi Filtreler"
    }
    "de" -> when (key) {
        "official" -> "Weitere offizielle Filter"
        "appraisal" -> "Bewertung"
        "size" -> "Größe"
        "buddy" -> "Kumpel-Level"
        "mega" -> "Mega-Level"
        "region" -> "Region"
        "type" -> "Pokémon-Typ"
        else -> "Offizielle Filter"
    }
    "es" -> when (key) {
        "official" -> "Más filtros oficiales"
        "appraisal" -> "Valoración"
        "size" -> "Tamaño"
        "buddy" -> "Nivel de compañero"
        "mega" -> "Nivel Mega"
        "region" -> "Región"
        "type" -> "Tipo de Pokémon"
        else -> "Filtros oficiales"
    }
    "fr" -> when (key) {
        "official" -> "Autres filtres officiels"
        "appraisal" -> "Évaluation"
        "size" -> "Taille"
        "buddy" -> "Niveau Copain"
        "mega" -> "Niveau Méga"
        "region" -> "Région"
        "type" -> "Type de Pokémon"
        else -> "Filtres officiels"
    }
    "it" -> when (key) {
        "official" -> "Altri filtri ufficiali"
        "appraisal" -> "Valutazione"
        "size" -> "Taglia"
        "buddy" -> "Livello compagno"
        "mega" -> "Livello Mega"
        "region" -> "Regione"
        "type" -> "Tipo Pokémon"
        else -> "Filtri ufficiali"
    }
    else -> when (key) {
        "official" -> "More official filters"
        "appraisal" -> "Appraisal"
        "size" -> "Size"
        "buddy" -> "Buddy level"
        "mega" -> "Mega level"
        "region" -> "Region"
        "type" -> "Pokémon type"
        else -> "Official filters"
    }
}

private fun parameterizedFamilyHint(lang: String): String = when (lang) {
    "tr" -> "Açık uçlu resmi aramalar Raw modunda da desteklenir: Pokémon adı/takma ad/Dex no, +evrim ailesi, CP-SP-mesafe-yaş-yıl değerleri ve aralıkları, @hareket, @tür, @1/@2/@3 hareket türü ve #etiket."
    "de" -> "Offene offizielle Suchfamilien sind im Raw-Modus verfügbar: Name/Spitzname/Pokédex-Nr., +Entwicklungsfamilie, WP/KP/Entfernung/Alter/Jahr, @Attacke, @Typ, @1/@2/@3 und #Tag."
    "es" -> "Las familias oficiales abiertas están disponibles en modo Raw: nombre/apodo/n.º Pokédex, +familia evolutiva, PC/PS/distancia/edad/año, @movimiento, @tipo, @1/@2/@3 y #etiqueta."
    "fr" -> "Les familles officielles ouvertes restent disponibles en mode Raw : nom/surnom/n° Pokédex, +famille d’évolution, PC/PV/distance/âge/année, @attaque, @type, @1/@2/@3 et #étiquette."
    "it" -> "Le famiglie ufficiali aperte sono disponibili in modalità Raw: nome/soprannome/n. Pokédex, +famiglia evolutiva, PL/PS/distanza/età/anno, @mossa, @tipo, @1/@2/@3 e #tag."
    else -> "Open-ended official families are available in Raw mode: Pokémon name/nickname/Dex number, +evolution family, CP/HP/distance/age/year values and ranges, @move, @type, @1/@2/@3 move type, and #tag."
}

private fun rawModeHint(lang: String): String = when (lang) {
    "tr" -> "Resmi parametrik syntax örnekleri: cp-1500 · hp100- · age0-7 · year2026 · +Pikachu · @shadow ball · @3ghost · #etiket. AND=&, NOT=!, OR için , : ; kullanılabilir."
    "de" -> "Offizielle parametrische Beispiele: wp-1500 · kp100- · alter0-7 · jahr2026 · +Pikachu · @Spukball · @3geist · #tag. AND=&, NOT=!, OR mit , : ;."
    "es" -> "Ejemplos paramétricos oficiales: pc-1500 · ps100- · edad0-7 · año2026 · +Pikachu · @bola sombra · @3fantasma · #etiqueta. AND=&, NOT=!, OR con , : ;."
    "fr" -> "Exemples paramétriques officiels : pc-1500 · pv100- · âge0-7 · année2026 · +Pikachu · @ball’ombre · @3spectre · #étiquette. AND=&, NOT=!, OR avec , : ;."
    "it" -> "Esempi parametrici ufficiali: pl-1500 · ps100- · età0-7 · anno2026 · +Pikachu · @palla ombra · @3spettro · #tag. AND=&, NOT=!, OR con , : ;."
    else -> "Official parameterized examples: cp-1500 · hp100- · age0-7 · year2026 · +Pikachu · @shadow ball · @3ghost · #tag. AND=&, NOT=!, and OR may use , : ;."
}
