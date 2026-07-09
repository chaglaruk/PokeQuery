#!/usr/bin/env python3
import os
import json
import re
import argparse
import urllib.request
from datetime import datetime

MONTH_MAP = {
    "january": 1, "february": 2, "march": 3, "april": 4, "may": 5, "june": 6,
    "july": 7, "august": 8, "september": 9, "october": 10, "november": 11, "december": 12,
    "jan": 1, "feb": 2, "mar": 3, "apr": 4, "jun": 6, "jul": 7, "aug": 8, "sep": 9, "oct": 10, "nov": 11, "dec": 12
}

BANNED_TR_WORDS = ["arama dizgisi", "arama dizgisini", "dizgi", "dizgin"]

# HTML Parser implementations using Python's standard library HTMLParser
from html.parser import HTMLParser

class LeekDuckHTMLParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.events = []
        self.current_event = None
        self.current_field = None
        self.in_event = False

    def handle_starttag(self, tag, attrs):
        attrs_dict = dict(attrs)
        classes = attrs_dict.get('class', '')
        if tag == 'li' and 'event-card' in classes:
            self.in_event = True
            self.current_event = {
                'title': '',
                'date_range': '',
                'type': '',
                'href': ''
            }
        elif self.in_event:
            if tag == 'a':
                self.current_event['href'] = attrs_dict.get('href', '')
            elif tag == 'h2' and 'event-title' in classes:
                self.current_field = 'title'
            elif tag == 'span' and 'event-date' in classes:
                self.current_field = 'date_range'
            elif tag == 'span' and 'event-type' in classes:
                self.current_field = 'type'

    def handle_endtag(self, tag):
        if tag == 'li' and self.in_event:
            if self.current_event:
                self.events.append(self.current_event)
            self.current_event = None
            self.in_event = False
        self.current_field = None

    def handle_data(self, data):
        if self.in_event and self.current_field and self.current_event:
            self.current_event[self.current_field] += data

class PokemonGoNewsHTMLParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.events = []
        self.current_event = None
        self.current_field = None
        self.in_event = False

    def handle_starttag(self, tag, attrs):
        attrs_dict = dict(attrs)
        classes = attrs_dict.get('class', '')
        if tag == 'div' and 'news-list__item' in classes:
            self.in_event = True
            self.current_event = {
                'title': '',
                'date': '',
                'href': ''
            }
        elif self.in_event:
            if tag == 'a':
                self.current_event['href'] = attrs_dict.get('href', '')
            elif tag == 'h3' and 'news-list__item__title' in classes:
                self.current_field = 'title'
            elif tag == 'span' and 'news-list__item__date' in classes:
                self.current_field = 'date'

    def handle_endtag(self, tag):
        if tag == 'div' and self.in_event:
            if self.current_event:
                self.events.append(self.current_event)
            self.current_event = None
            self.in_event = False
        self.current_field = None

    def handle_data(self, data):
        if self.in_event and self.current_field and self.current_event:
            self.current_event[self.current_field] += data

def parse_date_range(date_str):
    """
    Parses start date, end date, start month, and year from standard date ranges.
    Returns: start_date_str (YYYY-MM-DD), end_date_str (YYYY-MM-DD), month (int), year (int)
    """
    date_str = date_str.replace("–", "-").replace("—", "-").strip()
    year = datetime.now().year
    year_match = re.search(r'\b(20\d{2})\b', date_str)
    if year_match:
        year = int(year_match.group(1))
    
    clean_str = re.sub(r',\s*20\d{2}', '', date_str).strip()
    
    if '-' in clean_str:
        parts = [p.strip() for p in clean_str.split('-')]
        start_part, end_part = parts[0], parts[1]
        
        # Parse end part (e.g. "July 6" or "6")
        end_m = re.match(r'([a-zA-Z]+)\s+(\d+)', end_part)
        if end_m:
            end_month_str, end_day_str = end_m.group(1).lower(), end_m.group(2)
            end_month = MONTH_MAP.get(end_month_str, 7)
            end_day = int(end_day_str)
        else:
            end_day_str = re.search(r'\d+', end_part)
            end_day = int(end_day_str.group(0)) if end_day_str else 1
            end_month = 7

        # Parse start part (e.g. "July 4" or "4")
        start_m = re.match(r'([a-zA-Z]+)\s+(\d+)', start_part)
        if start_m:
            start_month_str, start_day_str = start_m.group(1).lower(), start_m.group(2)
            start_month = MONTH_MAP.get(start_month_str, end_month)
            start_day = int(start_day_str)
        else:
            start_day_str = re.search(r'\d+', start_part)
            start_day = int(start_day_str.group(0)) if start_day_str else 1
            start_month = end_month
    else:
        m = re.match(r'([a-zA-Z]+)\s+(\d+)', clean_str)
        if m:
            month_str, day_str = m.group(1).lower(), m.group(2)
            start_month = MONTH_MAP.get(month_str, 7)
            start_day = int(day_str)
        else:
            start_day = 1
            start_month = 7
        end_month, end_day = start_month, start_day
        
    start_date_str = f"{year:04d}-{start_month:02d}-{start_day:02d}"
    end_date_str = f"{year:04d}-{end_month:02d}-{end_day:02d}"
    return start_date_str, end_date_str, start_month, year

def get_event_id(href, title):
    """Generates a stable unique event ID from href slug or title."""
    if href:
        slug = href.strip("/").split("/")[-1]
        if not slug.startswith("event-"):
            slug = f"event-{slug}"
        return slug
    slug = re.sub(r'[^a-z0-9]+', '-', title.lower()).strip('-')
    return f"event-{slug}"

def validate_safety_constraints(event):
    """Enforces Turkish copy restrictions and suggestedSearch requirements."""
    # Ensure suggestedSearch does not contain '|'
    search = event.get("suggestedSearch", "")
    if "|" in search:
        raise ValueError(f"Event {event['id']} has invalid suggestedSearch containing '|': {search}")
    
    # Check for Turkish banned words
    for field, val in event.items():
        if field.endswith("Tr") and isinstance(val, str):
            for banned in BANNED_TR_WORDS:
                if banned in val.lower():
                    raise ValueError(f"Event {event['id']} has banned Turkish word '{banned}' in field '{field}': {val}")

def parse_live_pokemongolive_news(html):
    events = []
    card_matches = list(re.finditer(r'<a\s+href="(/news/[^"]+)"[^>]*class="[^"]*_newsCard_[^"]*"[^>]*>', html))
    for m in card_matches:
        href = m.group(1)
        sub_html = html[m.end():m.end()+1500]
        
        ts_match = re.search(r'timestamp="(\d+)"', sub_html)
        if not ts_match:
            continue
        ts = int(ts_match.group(1)) / 1000.0
        dt = datetime.fromtimestamp(ts)
        start_date = dt.strftime("%Y-%m-%d")
        end_date = start_date
        
        title_match = re.search(r'class="[^"]*heading[^"]*">([^<]+)</div>', sub_html)
        if not title_match:
            continue
        title = title_match.group(1).strip()
        
        events.append({
            'title': title,
            'date': start_date,
            'href': href,
            'parsed_dates': (start_date, end_date, dt.month, dt.year)
        })
    return events

def parse_live_leekduck_events(html):
    events = []
    matches = list(re.finditer(r'<a\s+class="[^"]*event-item-link[^"]*"\s+href="(/events/[^"]+)"', html))
    for m in matches:
        href = m.group(1)
        window_start = max(0, m.start() - 600)
        window_end = min(len(html), m.start() + 1500)
        sub_html = html[window_start:window_end]
        
        start_match = re.search(r'data-event-start-date-check="([^"T\s]+)T', sub_html)
        end_match = re.search(r'data-event-end-date="([^"T\s]+)T', sub_html)
        if not start_match or not end_match:
            continue
            
        start_date = start_match.group(1)
        end_date = end_match.group(1)
        
        title_match = re.search(r'<h2>([^<]+)</h2>', sub_html)
        if not title_match:
            continue
        title = title_match.group(1).strip()
        
        badge_match = re.search(r'<span class="event-tag-badge">([^<]+)</span>', sub_html)
        ev_type = badge_match.group(1).strip() if badge_match else "Generic Event"
        
        parts = start_date.split('-')
        year = int(parts[0])
        month = int(parts[1])
        
        events.append({
            'title': title,
            'date_range': f"{start_date} - {end_date}",
            'startDate': start_date,
            'endDate': end_date,
            'month': month,
            'year': year,
            'href': href,
            'type': ev_type
        })
    return events

def get_importance_tier(title, kind):
    title_lower = title.lower()
    kind_lower = kind.lower()
    
    if any(w in title_lower for w in ["save the date", "save-the-date", "lego", "art", "partnership", "birthday", "wallpapers", "twitch drops"]):
        return "NEWS"
        
    if any(w in title_lower for w in ["spotlight hour", "raid hour", "max mondays", "league", "gbl", "season", "rotation", "5-star", "mega raid"]):
        return "ROUTINE"
        
    if any(w in title_lower for w in ["community day", "go fest", "raid day", "hatch day", "takeover", "anniversary party", "road of legends", "global"]):
        return "MAJOR"
    if kind_lower in ["community_day", "spotlight_hour"]:
        return "MAJOR" if kind_lower == "community_day" else "ROUTINE"
        
    return "STANDARD"

def generate_feed(fixture_mode, output_path):
    # Determine base directories
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_dir = os.path.dirname(script_dir)
    
    sources_path = os.path.join(project_dir, "docs", "event-feed", "sources.json")
    metadata_path = os.path.join(project_dir, "docs", "event-feed", "event_metadata.json")
    
    # Load sources config
    with open(sources_path, "r", encoding="utf-8") as f:
        sources_config = json.load(f)
        
    # Load enrichment metadata
    if os.path.exists(metadata_path):
        with open(metadata_path, "r", encoding="utf-8") as f:
            metadata = json.load(f)
    else:
        metadata = {}
        
    raw_events = {}
    successful_sources = []
    
    headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}
    
    for src in sources_config.get("sources", []):
        src_id = src["id"]
        src_url = src["url"]
        
        html_content = ""
        if fixture_mode:
            fixture_name = "pokemongolive_news.html" if src_id == "pokemongolive-news" else "leekduck_events.html"
            fixture_path = os.path.join(script_dir, "fixtures", fixture_name)
            if os.path.exists(fixture_path):
                with open(fixture_path, "r", encoding="utf-8") as f:
                    html_content = f.read()
                print(f"Loaded fixture for source {src_id} from {fixture_path}")
                successful_sources.append(src_id)
            else:
                print(f"Fixture not found for source {src_id} at {fixture_path}")
                continue
        else:
            try:
                req = urllib.request.Request(src_url, headers=headers)
                with urllib.request.urlopen(req, timeout=10) as response:
                    html_content = response.read().decode("utf-8")
                print(f"Successfully fetched {src_id} from {src_url}")
                successful_sources.append(src_id)
            except Exception as e:
                print(f"Failed to fetch {src_id} from {src_url}: {e}")
                continue
                
        if src_id == "pokemongolive-news":
            if fixture_mode:
                parser = PokemonGoNewsHTMLParser()
                parser.feed(html_content)
                parsed_events = parser.events
            else:
                parsed_events = parse_live_pokemongolive_news(html_content)
                
            for ev in parsed_events:
                title = ev["title"].strip()
                if not title:
                    continue
                try:
                    if 'parsed_dates' in ev:
                        start_date, end_date, month, year = ev['parsed_dates']
                    else:
                        start_date, end_date, month, year = parse_date_range(ev["date"])
                    ev_id = get_event_id(ev["href"], title)
                    
                    raw_events[ev_id] = {
                        "id": ev_id,
                        "title": title,
                        "kind": "GENERIC_EVENT",
                        "startDate": start_date,
                        "endDate": end_date,
                        "month": month,
                        "year": year,
                        "sourceUrl": ev["href"] if ev["href"].startswith("http") else "https://pokemongolive.com" + ev["href"],
                        "sourceName": "Pokémon GO Live News"
                    }
                except Exception as ex:
                    print(f"Skipping event '{title}' due to date parse failure: {ex}")
                    
        elif src_id == "leekduck-events":
            if fixture_mode:
                parser = LeekDuckHTMLParser()
                parser.feed(html_content)
                parsed_events = parser.events
            else:
                parsed_events = parse_live_leekduck_events(html_content)
                
            for ev in parsed_events:
                title = ev["title"].strip()
                if not title:
                    continue
                try:
                    if 'startDate' in ev:
                        start_date = ev['startDate']
                        end_date = ev['endDate']
                        month = ev['month']
                        year = ev['year']
                    else:
                        start_date, end_date, month, year = parse_date_range(ev["date_range"])
                    ev_id = get_event_id(ev["href"], title)
                    
                    kind_str = ev["type"].lower()
                    kind = "GENERIC_EVENT"
                    if "community day" in kind_str:
                        kind = "COMMUNITY_DAY"
                    elif "spotlight hour" in kind_str:
                        kind = "SPOTLIGHT_HOUR"
                        
                    existing = raw_events.get(ev_id)
                    if existing and existing.get("sourceName") == "Pokémon GO Live News":
                        if kind != "GENERIC_EVENT":
                            existing["kind"] = kind
                    else:
                        raw_events[ev_id] = {
                            "id": ev_id,
                            "title": title,
                            "kind": kind,
                            "startDate": start_date,
                            "endDate": end_date,
                            "month": month,
                            "year": year,
                            "sourceUrl": ev["href"] if ev["href"].startswith("http") else "https://leekduck.com" + ev["href"],
                            "sourceName": "Leek Duck Events"
                        }
                except Exception as ex:
                    print(f"Skipping Leek Duck event '{title}' due to date parse failure: {ex}")

    # Safety Check: If not in fixture_mode and all sources failed to fetch/parse, abort to prevent overwriting production JSON
    if not fixture_mode and (len(successful_sources) == 0 or len(raw_events) == 0):
        raise RuntimeError("Critical Error: All event sources failed to fetch or parse any events in live mode. Aborting generator to prevent empty/corrupted feed output.")

    # Build final event feed items
    final_events = []
    
    for ev_id, ev in raw_events.items():
        meta = metadata.get(ev_id, {})
        
        # Determine status dynamically based on current date (defaults to UPCOMING)
        today = datetime.now().strftime("%Y-%m-%d")
        start = ev["startDate"]
        end = ev["endDate"]
        if end < today:
            status = "ENDED"
        elif start <= today <= end:
            status = "CURRENT"
        else:
            status = "UPCOMING"
            
        event_entry = {
            "id": ev_id,
            "title": ev["title"],
            "titleTr": meta.get("titleTr", ev["title"]),
            "titleDe": meta.get("titleDe"),
            "titleEs": meta.get("titleEs"),
            "titleFr": meta.get("titleFr"),
            "titleIt": meta.get("titleIt"),
            "kind": ev["kind"],
            "status": status,
            "note": meta.get("note", f"Official event window: {start} to {end} local time."),
            "noteTr": meta.get("noteTr", f"Resmi etkinlik aralığı: {start} ile {end} yerel saat."),
            "noteDe": meta.get("noteDe"),
            "noteEs": meta.get("noteEs"),
            "noteFr": meta.get("noteFr"),
            "noteIt": meta.get("noteIt"),
            "month": ev["month"],
            "year": ev["year"],
            "startDate": start,
            "endDate": end,
            "start": meta.get("start", start),
            "end": meta.get("end", end),
            "summary": meta.get("summary", "Verify details in-game before acting."),
            "summaryTr": meta.get("summaryTr", "İşlem yapmadan önce oyun içi detayları kontrol edin."),
            "summaryDe": meta.get("summaryDe"),
            "summaryEs": meta.get("summaryEs"),
            "summaryFr": meta.get("summaryFr"),
            "summaryIt": meta.get("summaryIt"),
            "prep": meta.get("prep", "Prepare for event catches and inventory limits."),
            "prepTr": meta.get("prepTr", "Etkinlik yakalamaları ve envanter limitleri için hazırlık yapın."),
            "prepDe": meta.get("prepDe"),
            "prepEs": meta.get("prepEs"),
            "prepFr": meta.get("prepFr"),
            "prepIt": meta.get("prepIt"),
            "suggestedSearch": meta.get("suggestedSearch", "age0&!favorite"),
            "eventNotes": meta.get("eventNotes", "Review recent catches before transfer."),
            "eventNotesTr": meta.get("eventNotesTr", "Transferden önce son yakalamaları kontrol edin."),
            "eventNotesDe": meta.get("eventNotesDe"),
            "eventNotesEs": meta.get("eventNotesEs"),
            "eventNotesFr": meta.get("eventNotesFr"),
            "eventNotesIt": meta.get("eventNotesIt"),
            "themeKey": meta.get("themeKey", "generic_event"),
            "importanceTier": meta.get("importanceTier") or get_importance_tier(ev["title"], ev["kind"]),
            "sourceNotes": f"Generated from {ev['sourceName']}: {ev['sourceUrl']}",
            "sourceName": ev["sourceName"],
            "sourceUrl": ev["sourceUrl"],
            "sourceType": "official" if "News" in ev["sourceName"] else "third-party",
            "lastUpdated": datetime.now().strftime("%Y-%m-%d"),
            "pokemon": meta.get("pokemon", [])
        }
        
        # Safety assertions
        validate_safety_constraints(event_entry)
        final_events.append(event_entry)

    # Sort final list: CURRENT first, then UPCOMING by startDate
    final_events.sort(key=lambda x: (
        0 if x["status"] == "CURRENT" else (1 if x["status"] == "UPCOMING" else 2),
        x["startDate"] or "9999-12-31"
    ))

    # Construct complete feed
    feed_output = {
        "schemaVersion": 1,
        "lastUpdated": datetime.now().strftime("%Y-%m-%d"),
        "notes": "Repository-maintained public feed for PokeQuery. Entries are prepared from official Pokémon GO public event pages; the Android app consumes this PokeQuery-owned JSON and does not scrape third-party sites at runtime.",
        "events": final_events
    }
    
    # Save output
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(feed_output, f, indent=2, ensure_ascii=False)
        f.write("\n")
        
    print(f"Successfully generated event feed JSON containing {len(final_events)} events at {output_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate PokeQuery static event feed JSON")
    parser.add_argument("-f", "--fixture-mode", action="store_true", help="Run in offline mode using local HTML fixtures")
    parser.add_argument("-o", "--output", default="docs/event-feed/pokequery-events.json", help="Path to write the output JSON file")
    args = parser.parse_args()
    
    generate_feed(args.fixture_mode, args.output)
