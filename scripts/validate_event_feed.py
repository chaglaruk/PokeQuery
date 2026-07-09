#!/usr/bin/env python3
import sys
import json
import re
import os

BANNED_TR_WORDS = ["arama dizgisi", "arama dizgisini", "dizgi", "dizgin"]
ALLOWED_THEME_KEYS = {
    "electric", "dragon", "community_day", "candy_bonus",
    "trade_bonus", "raid", "spotlight_hour", "hatch", "research", "generic_event"
}

def validate_feed(file_path):
    print(f"Validating feed: {file_path}")
    if not os.path.exists(file_path):
        print(f"Error: file not found: {file_path}")
        return False
        
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            feed = json.load(f)
    except Exception as e:
        print(f"Error: Invalid JSON format: {e}")
        return False
        
    # Check top-level schema
    schema_version = feed.get("schemaVersion")
    if schema_version != 1:
        print(f"Error: Unsupported schema version: {schema_version}")
        return False
        
    last_updated = feed.get("lastUpdated")
    if not last_updated or not re.match(r'^\d{4}-\d{2}-\d{2}$', last_updated):
        print(f"Error: Invalid lastUpdated date format: {last_updated}")
        return False
        
    events = feed.get("events")
    if not isinstance(events, list) or len(events) == 0:
        print("Error: events must be a non-empty list")
        return False
        
    # Check each event
    for i, event in enumerate(events):
        event_id = event.get("id")
        if not event_id or not event_id.strip():
            print(f"Error: Event at index {i} is missing a stable ID")
            return False
            
        print(f"  Validating event: {event_id}")
        
        # Verify required non-blank fields
        required_fields = ["title", "note", "summary", "prep", "suggestedSearch", "eventNotes"]
        for field in required_fields:
            val = event.get(field)
            if not val or not val.strip():
                print(f"Error: Event {event_id} is missing required field: {field}")
                return False
                
        # Check no '|' in search
        suggested_search = event.get("suggestedSearch")
        if "|" in suggested_search:
            print(f"Error: Event {event_id} suggestedSearch contains banned '|': {suggested_search}")
            return False
            
        # Check themeKey
        theme_key = event.get("themeKey", "generic_event")
        if theme_key not in ALLOWED_THEME_KEYS:
            print(f"Error: Event {event_id} uses unsupported themeKey: {theme_key}")
            return False
            
        # Check Turkish terms and banned words
        for field, val in event.items():
            if field.endswith("Tr") and isinstance(val, str):
                for banned in BANNED_TR_WORDS:
                    if banned in val.lower():
                        print(f"Error: Event {event_id} contains banned Turkish word '{banned}' in field '{field}': {val}")
                        return False
                        
        # Check Pokémon entries
        pokemon = event.get("pokemon", [])
        if not isinstance(pokemon, list):
            print(f"Error: Event {event_id} pokemon field must be a list")
            return False
            
        for pk in pokemon:
            name = pk.get("name")
            if not name or not name.strip():
                print(f"Error: Event {event_id} has a pokemon entry missing a name")
                return False
                
            source = pk.get("source")
            if not source or not source.strip():
                print(f"Error: Event {event_id} pokemon {name} is missing a source")
                return False
                
            # Check Turkish translation suffixes for pokemon entries
            for field, val in pk.items():
                if field.endswith("Tr") and isinstance(val, str):
                    for banned in BANNED_TR_WORDS:
                        if banned in val.lower():
                            print(f"Error: Event {event_id} pokemon {name} contains banned Turkish word '{banned}' in field '{field}': {val}")
                            return False
                            
    print("Feed validation PASSED successfully!")
    return True

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python validate_event_feed.py <path_to_json>")
        sys.exit(1)
        
    success = validate_feed(sys.argv[1])
    if not success:
        sys.exit(1)
    sys.exit(0)
