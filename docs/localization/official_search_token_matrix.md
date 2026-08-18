# Official Pokémon GO Search Token Matrix

- **Verification Date**: 2026-08-18
- **Source**: Official Pokémon GO Help Center FAQ 1486 ("Searching and Filtering your Pokémon Inventory")
- **Official Reference URLs**:
  - EN: `https://niantic.helpshift.com/hc/en/6-pokemon-go/faq/1486-searching-filtering-your-pokemon-inventory/`
  - TR: `https://niantic.helpshift.com/hc/tr/6-pokemon-go/faq/1486-searching-filtering-your-pokemon-inventory/`
  - DE: `https://niantic.helpshift.com/hc/de/6-pokemon-go/faq/1486-searching-filtering-your-pokemon-inventory/`
  - ES: `https://niantic.helpshift.com/hc/es/6-pokemon-go/faq/1486-searching-filtering-your-pokemon-inventory/`
  - FR: `https://niantic.helpshift.com/hc/fr/6-pokemon-go/faq/1486-searching-filtering-your-pokemon-inventory/`
  - IT: `https://niantic.helpshift.com/hc/it/6-pokemon-go/faq/1486-searching-filtering-your-pokemon-inventory/`

## Confidence & Invariants
- **BETA vs. VERIFIED**: In PokeQuery's registry confidence model, terms documented in official Help Center materials are classified as **BETA**. **VERIFIED** is reserved exclusively for terms confirmed independently in a live localized Pokémon GO game client.
- **Pipe Prohibition**: PokeQuery search strings **never** emit `|` (pipe) under any circumstances, preserving strict delimiter safety across Android and Web.
- **Intentional English Fallbacks**:
  - `count`: Numeric species count syntax (`countN-`) is parser-sensitive. Unverified across live localized clients; remains English fallback.
  - `specialbackground`: No official localized keyword published; remains English fallback.

## Canonical Keyword Mapping Table

| English Canonical | Turkish (TR) | German (DE) | Spanish (ES) | French (FR) | Italian (IT) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `cp` | `dg` | `wp` | `pc` | `pc` | `pl` |
| `hp` | `sp` | `kp` | `ps` | `pv` | `ps` |
| `distance` | `mesafe` | `entfernung` | `distancia` | `distance` | `distanza` |
| `attack` | `saldırı` | `angriff` | `ataque` | `attaque` | `attacco` |
| `defense` | `savunma` | `verteidigung` | `defensa` | `défense` | `difesa` |
| `age` | `yaş` | `alter` | `edad` | `âge` | `età` |
| `year` | `yıl` | `jahr` | `año` | `année` | `anno` |
| `shiny` | `parlak` | `schillernd` | `variocolor` | `chromatique` | `cromatico` |
| `legendary` | `efsanevi` | `legendär` | `legendario` | `légendaire` | `leggendario` |
| `mythical` | `mitolojik` | `mysteriös` | `singular` | `fabuleux` | `misterioso` |
| `shadow` | `gölge` | `crypto` | `oscuro` | `obscur` | `ombra` |
| `purified` | `arınmış` | `erlöst` | `purificado` | `purifié` | `purificato` |
| `favorite` | `favori` | `favorit` | `favorito` | `favoris` | `preferiti` |
| `lucky` | `şanslı` | `glücks` | `con suerte` | `chanceux` | `fortunato` |
| `costume` | `kostüm` | `kostümiert` | `disfraz` | `costume` | `costume` |
| `traded` | `takas edilen` | `getauscht` | `intercambiados` | `échangé` | `scambiato` |
| `defender` | `savunucu` | `verteidiger` | `defensor` | `défenseur` | `difensore` |
| `background` | `arkaplan` | `hintergrund` | `fondo` | `fond` | `sfondo` |
| `locationbackground` | `konumarkaplanı` | `ortshintergrund` | `fondolugar` | `fondlieu` | `sfondodiposizione` |
| `ultrabeast` | `ultracanavar` | `ultrabestie` | `ultraentes` | `ultra-chimère` | `ultracreatura` |
| `dynamax` | `dinamaks` | `dynamax` | `dinamax` | `dynamax` | `dynamax` |
| `gigantamax` | `gigantamaks` | `gigadynamax` | `gigamax` | `gigamax` | `gigamax` |
| `fusion` | `füzyon` | `fusion` | `fusión` | `fusion` | `fusione` |
| `mega` | `mega` | `mega` | `mega` | `méga` | `mega` |
| `megaevolve` | `megaevrim` | `megaentwicklung` | `megaevolucionar` | `mégaévolue` | `megaevoluto` |
| `buddy` | `dost` | `kumpel` | `compañero` | `copain` | `compagno` |
| `evolve` | `evrim` | `entwickeln` | `evolucionar` | `évoluer` | `fai evolvere` |
| `hypertraining` | `hipereğitim` | `superspezialtraining` | `entrenamiento extremo` | `entraînementultime` | `allenamentopro` |
| `item` | `eşya` | `item` | `objeto` | `objet` | `strumento` |
| `evolvenew` | `yenievrim` | `neueentwicklung` | `nuevaevolución` | `nouvelleévolution` | `nuovaevoluzione` |
| `evolvequest` | `evrimhedef` | `entwicklungsaufgabe` | `misión evolución` | `évolutionparquête` | `evoluzionetramitericerca` |
| `tradeevolve` | `takasevrim` | `tauschentwicklung` | `evoluciónintercambio` | `évolutionparéchange` | `evoluzionetramitescambio` |
| `@special` | `@özel` | `@spezial` | `@especial` | `@spécial` | `@speciale` |
| `@weather` | `@havadurumu` | `@wetter` | `@tiempo atmosférico` | `@météo` | `@meteo` |
| `eggsonly` | `sadeceyumurta` | `nurausEiern` | `huevosolo` | `oeufseulement` | `solouovo` |
| `hatched` | `yumurtadançıkmış` | `ausgebrütet` | `eclosionado` | `éclos` | `dauovo` |
