#!/usr/bin/env python3
import copy
import unittest

try:
    from scripts.enrich_event_feed import (
        enrich_feed,
        normalize_event_title,
        parse_detail_html,
        validate_active_detail_quality,
    )
except ModuleNotFoundError:  # direct: python scripts/test_enrich_event_feed.py
    from enrich_event_feed import (
        enrich_feed,
        normalize_event_title,
        parse_detail_html,
        validate_active_detail_quality,
    )


class EventFeedEnrichmentTest(unittest.TestCase):
    def test_normalizes_gbl_season_suffix_and_spacing(self):
        self.assertEqual(
            "Master League and Evolution Cup: Great League Edition",
            normalize_event_title("Master League and Evolution Cup: Great League Edition  -  Forever Forward"),
        )

    def test_normalizes_rotation_titles_without_changing_pokemon_form(self):
        self.assertEqual(
            "Shadow Giratina (Altered Forme) — Shadow Raids",
            normalize_event_title("Shadow Giratina (Altered Forme) in Shadow Raids"),
        )
        self.assertEqual(
            "Groudon — 5-Star Raids",
            normalize_event_title("Groudon in 5-star Raid Battles"),
        )

    def test_extracts_source_sections_and_replaces_generic_placeholders(self):
        source = """
        <html><body><main>
          <h1>Shadow Giratina (Altered Forme) in Shadow Raids - August 2026</h1>
          <p>Shadow Giratina (Altered Forme) makes its Shadow Raid debut during this rotation.</p>
          <h2>Raids</h2>
          <h3>Shadow Giratina (Altered Forme)</h3>
          <p>Shadow Giratina (Altered Forme) appears in five-star Shadow Raids on weekends.</p>
          <p>If you are lucky, you may encounter a Shiny one.</p>
          <h2>Bonuses</h2><ul><li>Five-star Shadow Raids are available on event weekends.</li></ul>
        </main></body></html>
        """
        event = {
            "id": "event-shadow-giratina",
            "title": "Shadow Giratina (Altered Forme) in Shadow Raids",
            "titleTr": None,
            "status": "CURRENT",
            "eventCategory": "RAID_ROTATION",
            "sourceUrl": "https://example.test/event",
            "summary": "Verify details in-game before acting.",
            "summaryTr": "İşlem yapmadan önce oyun içi detayları kontrol edin.",
            "prep": "Prepare for event catches and inventory limits.",
            "eventNotes": "Review recent catches before transfer.",
            "eventNotesTr": "Transferden önce son yakalamaları kontrol edin.",
            "pokemon": [],
            "featuredPokemon": None,
            "boostedPokemon": None,
            "bonuses": None,
            "raids": None,
            "research": None,
        }
        feed = {"events": [copy.deepcopy(event)]}
        result = enrich_feed(feed, fetcher=lambda _url: source, strict=True)
        enriched = result["events"][0]
        self.assertEqual("Shadow Giratina (Altered Forme) — Shadow Raids", enriched["title"])
        self.assertEqual("Shadow Giratina (Altered Forme) — Gölge Akınları", enriched["titleTr"])
        self.assertIn("Shadow Raid debut", enriched["summary"])
        self.assertIn("Shadow Giratina (Altered Forme)", enriched["raids"])
        self.assertIn("five-star Shadow Raids", enriched["raids"])
        self.assertIn("event weekends", enriched["bonuses"])
        self.assertIsNone(enriched["summaryTr"])
        self.assertEqual([], validate_active_detail_quality(result["events"]))

    def test_article_intro_becomes_general_notes_when_no_gameplay_section_exists(self):
        source = """
        <html><body>
          <h1>LEGO Stores and Pokémon GO</h1>
          <p>Pokémon GO activities are coming to participating LEGO Stores during the campaign.</p>
        </body></html>
        """
        feed = {
            "events": [{
                "id": "event-lego",
                "title": "LEGO Stores and Pokémon GO",
                "status": "CURRENT",
                "eventCategory": "NEWS_PROMO",
                "sourceUrl": "https://example.test/lego",
                "summary": "Verify details in-game before acting.",
                "eventNotes": "Review recent catches before transfer.",
                "pokemon": [],
            }]
        }
        result = enrich_feed(feed, fetcher=lambda _url: source, strict=True)
        event = result["events"][0]
        self.assertIn("participating LEGO Stores", event["summary"])
        self.assertEqual(event["summary"], event["eventNotes"])

    def test_strict_quality_gate_rejects_active_gameplay_without_detail(self):
        feed = {
            "events": [{
                "id": "event-empty",
                "title": "Example Event",
                "status": "CURRENT",
                "eventCategory": "LIMITED_GAMEPLAY",
                "sourceUrl": "",
                "summary": "Verify details in-game before acting.",
                "prep": "Prepare for event catches and inventory limits.",
                "eventNotes": "Review recent catches before transfer.",
                "pokemon": [],
            }]
        }
        with self.assertRaises(RuntimeError):
            enrich_feed(feed, fetcher=lambda _url: "", strict=True)

    def test_parser_groups_intro_and_nested_subhead_under_parent_section(self):
        page = parse_detail_html(
            "<h1>Event</h1><p>Intro detail paragraph here.</p>"
            "<h2>Research</h2><h3>Timed Research</h3><p>Timed Research rewards encounters.</p>"
        )
        self.assertEqual("Event", page.title)
        self.assertIn("Intro detail paragraph here.", page.intro)
        self.assertIn("Timed Research", page.sections["Research"])
        self.assertIn("Timed Research rewards encounters.", page.sections["Research"])


if __name__ == "__main__":
    unittest.main()
