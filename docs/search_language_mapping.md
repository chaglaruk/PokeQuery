# Search Language Mapping

PokeQuery strictly generates strings in the syntax format matching the user's selected `Search Language` setting (Auto / English / Turkish).

## Core Rules

* Valid search engine components (e.g. `distance100-`, `shiny`, `costume`) are translated directly into their specific language equivalent while keeping integers and numeric thresholds strictly intact.
* Logical operators (`&`, `,`, `!`) stay unchanged as they are uniform across supported Pokémon GO languages.

## English vs Turkish Reference Table

| English Target String         | Turkish Target String         | Mapping Rule                 |
|-------------------------------|-------------------------------|------------------------------|
| `legendary`                   | `efsanevi`                    | Direct exact match           |
| `mythical`                    | `mistik`                      | Direct exact match           |
| `shiny`                       | `parlak`                      | Direct exact match           |
| `traded`                      | `takaslanan`                  | Direct exact match           |
| `favorite`                    | `favori`                      | Direct exact match           |
| `lucky`                       | `şanslı`                      | Direct exact match           |
| `shadow`                      | `gölge`                       | Direct exact match           |
| `purified`                    | `arıtılmış`                   | Direct exact match           |
| `costume`                     | `kostümlü`                    | Direct exact match           |
| `count[N]-` (e.g. `count2-`)  | `toplam[N]-` (e.g. `toplam2-`)| Prefix swap, digits intact   |
| `[N]attack` (e.g. `0attack`)  | `[N]saldırı`                  | Suffix swap, digits intact   |
| `[N]defense`                  | `[N]savunma`                  | Suffix swap, digits intact   |
| `[N]hp`                       | `[N]can`                      | Suffix swap, digits intact   |
| `distance[N]-`                | `mesafe[N]-`                  | Prefix swap, digits intact   |
| `age[N]-[N]`                  | `yaş[N]-[N]`                  | Prefix swap, digits intact   |
| `4*`, `0*`                    | `4*`, `0*`                    | Intact across languages      |

## Uncertainties
* `hp` mapping in game translates variably depending on client localization, we map to `can` per explicit user requirement. 
* Age mapping translates to `yaş`, tested to work in most cases.
* If a string token fails to match the language prefix matching rules, it defaults to retaining the English base token.
