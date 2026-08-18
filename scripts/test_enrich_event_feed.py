#!/usr/bin/env python3
import copy
import unittest

try:
    from scripts.enrich_event_feed import (
        clean_text,
        derive_turkish_title,
        enrich_feed,
        extract_enrichment,
        meaningful,
        normalize_event_title,
        parse_detail_html,
        validate_active_detail_quality,
    )
except ModuleNotFoundError:  # direct: python scripts/test_enrich_event_feed.py
    from enrich_event_feed import (
        clean_text,
        derive_turkish_title,
        enrich_feed,
        extract_enrichment,
        meaningful,
        normalize_event_title,
        parse_detail_html,
        validate_active_detail_quality,
    )


class EventFeedEnrichmentTest(unittest.TestCase):
    def test_normalizes_gbl_season_suffix_and_spacing(self):
        expected = "Master League and Evolution Cup: Great League Edition"
        self.assertEqual(
            expected,
            normalize_event_title("Master League and Evolution Cup: Great League Edition  -  Forever Forward"),
        )
        self.assertEqual(
            expected,
            normalize_event_title("Master League and Evolution Cup: Great League Edition | Forever Forward"),
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

    def test_turkish_structural_title_falls_back_when_base_contains_english_connector(self):
        self.assertIsNone(derive_turkish_title("Regirock, Regice, and Registeel — 5-Star Raids"))
        self.assertEqual(
            "Groudon — 5 Yıldızlı Akınlar",
            derive_turkish_title("Groudon — 5-Star Raids"),
        )

    def test_clean_text_repairs_inline_punctuation_artifacts(self):
        self.assertEqual(
            "August 20: The featured Pokémon is Magikarp and the bonus is 2× Catch XP.",
            clean_text("August 20 : The featured Pokémon is Magikarp and the bonus is 2× Catch XP ."),
        )
        self.assertEqual(
            "Shiny Shadow Giratina (Altered Forme) - if you’re lucky!",
            clean_text("Shiny Shadow Giratina (Altered Forme)-if you’re lucky!"),
        )

    def test_extracts_source_sections_and_replaces_generic_placeholders(self):
        source = """
        <html><body><p>Raids Current Raid Bosses Raid NOW</p><main>
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
        self.assertNotIn("Raid Bosses", enriched["summary"])
        self.assertIn("Shadow Giratina (Altered Forme)", enriched["raids"])
        self.assertIn("five-star Shadow Raids", enriched["raids"])
        self.assertIn("event weekends", enriched["bonuses"])
        self.assertIsNone(enriched["summaryTr"])
        self.assertEqual([], validate_active_detail_quality(result["events"]))

    def test_lead_in_only_detail_does_not_count_as_useful_content(self):
        self.assertFalse(meaningful("The following Pokémon will appear more frequently in the wild."))
        self.assertFalse(meaningful("Appearing in 1-Star Raids"))

        page = parse_detail_html(
            "<h1>Event</h1>"
            "<h2>Wild Encounters</h2><p>The following Pokémon will appear more frequently in the wild.</p>"
            "<h2>Raids</h2><p>Appearing in 1-Star Raids</p>"
        )
        extracted = extract_enrichment(page, "Event")
        self.assertNotIn("featuredPokemon", extracted)
        self.assertNotIn("raids", extracted)

    def test_section_is_classified_once_instead_of_duplicating_research_rewards(self):
        page = parse_detail_html(
            "<h1>Event</h1>"
            "<h2>Research and Rewards</h2>"
            "<p>Complete Timed Research to earn a Pikachu encounter.</p>"
        )
        extracted = extract_enrichment(page, "Event")
        self.assertIn("research", extracted)
        self.assertNotIn("bonuses", extracted)

    def test_article_intro_becomes_general_notes_when_no_gameplay_section_exists(self):
        source = """
        <html><body>
          <p>Site navigation content that must not become the event summary.</p>
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
        self.assertNotIn("Site navigation", event["summary"])
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
            "<p>Header junk that must be ignored.</p>"
            "<h1>Event</h1><p>Intro detail paragraph here.</p>"
            "<h2>Research</h2><h3>Timed Research</h3><p>Timed Research rewards encounters.</p>"
        )
        self.assertEqual("Event", page.title)
        self.assertEqual(["Intro detail paragraph here."], page.intro)
        self.assertIn("Timed Research", page.sections["Research"])
        self.assertIn("Timed Research rewards encounters.", page.sections["Research"])

    def test_parser_ignores_html_void_elements_inside_article_blocks(self):
        page = parse_detail_html(
            "<h1>Event</h1>"
            "<p>Intro<br>detail<img src='x'> remains intact.</p>"
            "<h2>Bonuses</h2>"
            "<p>Bonus<br>line remains intact.</p>"
        )
        self.assertEqual("Event", page.title)
        self.assertEqual(["Intro detail remains intact."], page.intro)
        self.assertEqual(["Bonus line remains intact."], page.sections["Bonuses"])


if __name__ == "__main__":
    unittest.main()
