import json
import os

filepath = 'app/src/main/assets/knowledgebase.json'

updates = {
    "op_and": {
        "description_en": "The AND operator (&) combines multiple search terms. A Pokémon must match BOTH terms to appear.\n\nExample: 'shiny&legendary' finds only Pokémon that are both shiny AND legendary.\n\nCaution: Using conflicting terms (e.g., 'shiny&!shiny') will result in no matches."
    },
    "op_or": {
        "description_en": "The OR operator (,) allows either term to match. A Pokémon matching ANY of the terms will appear.\n\nExample: 'shiny,legendary' finds all shinies and all legendaries.\n\nCaution: Comma groups are evaluated within AND blocks. You cannot use parentheses in Pokémon GO."
    },
    "op_not": {
        "description_en": "The NOT operator (!) excludes a specific term. A Pokémon matching the term will NOT appear.\n\nExample: '!shiny' hides all shiny Pokémon.\n\nCaution: Must directly prefix a search term without spaces."
    },
    "prop_shiny": {
        "description_en": "Finds all Shiny Pokémon (differently colored rare variants).\n\nExample: 'shiny' finds all shinies. '!shiny' hides them.\n\nLanguage Note: Translates to 'parlak' in Turkish."
    },
    "prop_legendary": {
        "description_en": "Finds all Legendary Pokémon.\n\nExample: 'legendary' shows all your Legendary Pokémon.\n\nLanguage Note: Translates to 'efsanevi' in Turkish."
    },
    "prop_mythical": {
        "description_en": "Finds all Mythical Pokémon (e.g., Mew, Celebi, Meltan).\n\nExample: 'mythical' shows your Mythical Pokémon.\n\nLanguage Note: Translates to 'mistik' in Turkish."
    },
    "prop_traded": {
        "description_en": "Finds Pokémon received from a trade.\n\nExample: 'traded' shows traded Pokémon. '!traded' shows Pokémon you caught yourself.\n\nCaution: Traded Pokémon cannot be traded again.\n\nLanguage Note: Translates to 'takaslanan' in Turkish."
    },
    "prop_lucky": {
        "description_en": "Finds Lucky Pokémon, which cost 50% less Stardust to power up.\n\nExample: 'lucky' shows all lucky Pokémon.\n\nLanguage Note: Translates to 'şanslı' in Turkish."
    },
    "prop_shadow": {
        "description_en": "Finds Shadow Pokémon left behind by Team GO Rocket.\n\nExample: 'shadow' shows all shadow Pokémon.\n\nCaution: Shadow Pokémon have a 20% attack bonus but take 20% more damage.\n\nLanguage Note: Translates to 'gölge' in Turkish."
    },
    "prop_purified": {
        "description_en": "Finds Pokémon that have been purified from their Shadow state.\n\nExample: 'purified' shows all purified Pokémon.\n\nLanguage Note: Translates to 'arıtılmış' in Turkish."
    },
    "prop_favorite": {
        "description_en": "Finds Pokémon you have marked with a star (favorite).\n\nExample: 'favorite' shows starred Pokémon. '!favorite' is crucial for safe transfers.\n\nCaution: Favorited Pokémon cannot be transferred.\n\nLanguage Note: Translates to 'favori' in Turkish."
    },
    "numeric_count": {
        "description_en": "Finds Pokémon species that you have a certain number of.\n\nExample: 'count3-' finds Pokémon where you own 3 or more of that exact species.\n\nCaution: This counts all forms of the species together (e.g., Alolan and normal Vulpix count as the same species).\n\nLanguage Note: 'count' translates to 'toplam' in Turkish (e.g., 'toplam3-')."
    },
    "numeric_cp": {
        "description_en": "Finds Pokémon based on their Combat Power (CP).\n\nExample: 'cp1500' finds exactly 1500 CP. 'cp-1500' finds 1500 or lower. 'cp2500-' finds 2500 or higher."
    },
    "numeric_hp": {
        "description_en": "Finds Pokémon based on their current Hit Points (HP).\n\nExample: 'hp100-200' finds Pokémon with HP between 100 and 200.\n\nLanguage Note: 'hp' translates to 'can' in Turkish."
    },
    "numeric_distance": {
        "description_en": "Finds Pokémon caught at a certain distance (in km) from your current location.\n\nExample: 'distance100-' finds Pokémon caught 100km or further away (great for trading for 3 candy).\n\nLanguage Note: 'distance' translates to 'mesafe' in Turkish."
    },
    "numeric_age": {
        "description_en": "Finds Pokémon based on how many days ago they were caught.\n\nExample: 'age0' finds Pokémon caught today. 'age0-7' finds Pokémon caught in the last week. 'age365-' finds Pokémon caught over a year ago.\n\nLanguage Note: 'age' translates to 'yaş' in Turkish."
    },
    "iv_0_attack": {
        "description_en": "Finds Pokémon with exactly 0 IVs in Attack (0/15).\n\nExample: '0attack' finds 0 attack Pokémon. '0attack&0defense&0hp' finds perfect 0% IVs (Nundos).\n\nLanguage Note: 'attack' -> 'saldırı', 'defense' -> 'savunma', 'hp' -> 'can' in Turkish."
    },
    "iv_4_star": {
        "description_en": "Finds perfect 100% IV Pokémon (15/15/15).\n\nExample: '4*' shows all perfect Pokémon.\n\nCaution: Extremely rare, do not transfer!"
    },
    "iv_0_star": {
        "description_en": "Finds Pokémon with low IVs (below ~50%).\n\nExample: '0*' finds weak Pokémon. '0*,1*,2*' finds all Pokémon that are not 3* or 4*.\n\nCaution: Good PvP Pokémon (for Great/Ultra League) often fall into the 0* or 1* category because they prefer low Attack."
    }
}

with open(filepath, 'r', encoding='utf-8') as f:
    data = json.load(f)

for item in data:
    item_id = item.get("id")
    if item_id in updates:
        item["description_en"] = updates[item_id]["description_en"]

with open(filepath, 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

print("Updated knowledgebase.json successfully.")
