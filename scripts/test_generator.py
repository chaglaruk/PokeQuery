#!/usr/bin/env python3
import unittest
import unittest.mock
import os
import sys
import tempfile
import json

# Ensure scripts directory is on sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from generate_event_feed import parse_date_range, get_event_id, generate_feed, validate_safety_constraints, canonical_event_id, put_raw_event

class TestEventFeedGenerator(unittest.TestCase):

    def test_date_range_parsing(self):
        # Multi-day month transition
        start, end, m, y = parse_date_range("July 4 – July 6, 2026")
        self.assertEqual(start, "2026-07-04")
        self.assertEqual(end, "2026-07-06")
        self.assertEqual(m, 7)
        self.assertEqual(y, 2026)

        # Multi-day same month abbreviated
        start, end, m, y = parse_date_range("July 6 – 10, 2026")
        self.assertEqual(start, "2026-07-06")
        self.assertEqual(end, "2026-07-10")

        # Single day
        start, end, m, y = parse_date_range("July 21, 2026")
        self.assertEqual(start, "2026-07-21")
        self.assertEqual(end, "2026-07-21")

    def test_event_id_generation(self):
        self.assertEqual(get_event_id("/events/road-of-legends-2026/", "Road of Legends"), "event-road-of-legends-2026")
        self.assertEqual(get_event_id("", "July Community Day"), "event-july-community-day")

    def test_safety_constraints_valid(self):
        valid_event = {
            "id": "event-test",
            "suggestedSearch": "age0-2&!favorite&!traded",
            "titleTr": "Test Etkinliği",
            "noteTr": "Test Notu"
        }
        # Should not raise any exception
        validate_safety_constraints(valid_event)

    def test_safety_constraints_banned_word(self):
        invalid_event = {
            "id": "event-test",
            "suggestedSearch": "age0-2&!favorite&!traded",
            "titleTr": "Yeni arama dizgisi",
            "noteTr": "Test Notu"
        }
        with self.assertRaises(ValueError):
            validate_safety_constraints(invalid_event)

    def test_safety_constraints_pipe(self):
        invalid_event = {
            "id": "event-test",
            "suggestedSearch": "age0-2|!favorite",
            "titleTr": "Test",
            "noteTr": "Test Notu"
        }
        with self.assertRaises(ValueError):
            validate_safety_constraints(invalid_event)

    def test_generator_feed_output(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            output_file = os.path.join(tmpdir, "events.json")
            # Run generator in fixture-mode
            generate_feed(fixture_mode=True, output_path=output_file)
            
            self.assertTrue(os.path.exists(output_file))
            with open(output_file, "r", encoding="utf-8") as f:
                feed = json.load(f)
                
            self.assertEqual(feed["schemaVersion"], 1)
            self.assertTrue(len(feed["events"]) >= 3)
            
            # Check de-duplication and merging
            go_fest = next((e for e in feed["events"] if e["id"] == "event-go-fest-global-2026"), None)
            self.assertIsNotNone(go_fest)
            self.assertEqual(go_fest["titleTr"], "GO Fest 2026: Küresel")
            self.assertEqual(go_fest["themeKey"], "raid")
            self.assertTrue(len(go_fest["pokemon"]) >= 4)
            
            # Ensure no pipe in suggestedSearch
            for ev in feed["events"]:
                self.assertFalse("|" in ev["suggestedSearch"])
                self.assertEqual(ev["suggestedSearch"].split("&").count("!traded"), 1)
                # Assert source fields exist and match schema
                self.assertIn("sourceName", ev)
                self.assertIn("sourceUrl", ev)
                self.assertIn("sourceType", ev)
                self.assertIn("lastUpdated", ev)
                self.assertIn(ev["sourceType"], ["official", "third-party"])
                self.assertTrue(ev["sourceUrl"].startswith("http"))

    def test_known_article_ids_resolve_to_curated_event_ids(self):
        self.assertEqual(
            canonical_event_id("event-tcg-30th-celebration-event"),
            "event-pokemon-tcg-30th-celebration"
        )
        self.assertEqual(
            canonical_event_id("event-communityday-october-2026-zorua"),
            "event-october-communityday2026"
        )
        self.assertEqual(
            canonical_event_id("event-halloween-part-2-2026"),
            "event-halloween-2026-part-2"
        )

    def test_calendar_dates_win_over_news_publication_timestamp(self):
        raw = {}
        put_raw_event(raw, {
            "id": "event-communityday-october-2026-zorua",
            "kind": "GENERIC_EVENT",
            "startDate": "2026-09-15",
            "endDate": "2026-09-15",
            "sourceName": "Pokémon GO Live News",
        })
        put_raw_event(raw, {
            "id": "event-october-communityday2026",
            "kind": "COMMUNITY_DAY",
            "startDate": "2026-10-10",
            "endDate": "2026-10-10",
            "sourceName": "Leek Duck Events",
        })
        self.assertEqual(len(raw), 1)
        event = raw["event-october-communityday2026"]
        self.assertEqual(event["startDate"], "2026-10-10")
        self.assertEqual(event["endDate"], "2026-10-10")
        self.assertEqual(event["kind"], "COMMUNITY_DAY")

    def test_halloween_part2_calendar_dates_win_in_both_discovery_orders(self):
        news = {
            "id": "event-halloween-part-2-2026",
            "kind": "GENERIC_EVENT",
            "startDate": "2026-10-08",
            "endDate": "2026-10-08",
            "sourceName": "Pokémon GO Live News",
        }
        calendar = {
            "id": "event-halloween-2026-part-2",
            "kind": "GENERIC_EVENT",
            "startDate": "2026-11-01",
            "endDate": "2026-11-05",
            "sourceName": "Leek Duck Events",
        }
        for first, second in ((news, calendar), (calendar, news)):
            with self.subTest(first_source=first["sourceName"]):
                raw = {}
                put_raw_event(raw, dict(first))
                put_raw_event(raw, dict(second))
                self.assertEqual(len(raw), 1)
                row = raw["event-halloween-2026-part-2"]
                self.assertEqual((row["startDate"], row["endDate"]), ("2026-11-01", "2026-11-05"))

    def test_gameplay_update_is_news_not_a_timed_gameplay_event(self):
        root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        with open(os.path.join(root, "docs", "event-feed", "event_metadata.json"), encoding="utf-8") as f:
            metadata = json.load(f)
        row = metadata["event-pgo-gameplay-update-oct-2026"]
        self.assertEqual(row["eventCategory"], "ANNOUNCEMENT")
        self.assertEqual(row["importanceTier"], "NEWS")
        self.assertEqual(row["sourceType"], "official")
        self.assertEqual(row["sourceUrl"], "https://pokemongo.com/news/pgo-gameplay-update-oct-2026")
        self.assertEqual((row["startDate"], row["endDate"]), ("2026-10-08", "2026-10-08"))
        for suffix in ("", "Tr", "De", "Es", "Fr", "It"):
            with self.subTest(locale=suffix or "En"):
                self.assertTrue(row.get("summary" + suffix))
                self.assertTrue(row.get("note" + suffix))

    def test_halloween_part2_official_metadata_is_complete(self):
        root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        with open(os.path.join(root, "docs", "event-feed", "event_metadata.json"), encoding="utf-8") as f:
            metadata = json.load(f)
        row = metadata["event-halloween-2026-part-2"]
        self.assertEqual((row["startDate"], row["endDate"]), ("2026-11-01", "2026-11-05"))
        self.assertEqual(row["sourceType"], "official")
        self.assertEqual(row["sourceUrl"], "https://pokemongo.com/news/halloween-part-2-2026")
        for field in ("summary", "featuredPokemon", "bonuses", "raids", "research", "eventNotes"):
            for suffix in ("", "Tr", "De", "Es", "Fr", "It"):
                with self.subTest(field=field, locale=suffix or "En"):
                    self.assertTrue(row.get(field + suffix))

    def test_tcg_and_zorua_official_metadata_has_valid_windows(self):
        root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        with open(os.path.join(root, "docs", "event-feed", "event_metadata.json"), encoding="utf-8") as f:
            metadata = json.load(f)
        for key, start, end in (
            ("event-pokemon-tcg-30th-celebration", "2026-09-27", "2026-10-20"),
            ("event-october-communityday2026", "2026-10-10", "2026-10-10"),
        ):
            row = metadata[key]
            self.assertEqual(row["startDate"], start)
            self.assertEqual(row["endDate"], end)
            self.assertEqual(row["sourceType"], "official")
            self.assertTrue(row["sourceUrl"].startswith("https://pokemongo.com/news/"))
            for suffix in ("Tr", "De", "Es", "Fr", "It"):
                self.assertTrue(row.get("summary" + suffix))
                self.assertTrue(row.get("bonuses" + suffix))

    @unittest.mock.patch('urllib.request.urlopen')
    def test_live_mode_all_sources_fail(self, mock_urlopen):
        # Setup mock to fail for all requests
        mock_urlopen.side_effect = Exception("Network Connection Refused")
        
        with tempfile.TemporaryDirectory() as tmpdir:
            output_file = os.path.join(tmpdir, "events.json")
            # Running live mode should raise RuntimeError because all sources fail
            with self.assertRaises(RuntimeError):
                generate_feed(fixture_mode=False, output_path=output_file)
            
            # File must not be created
            self.assertFalse(os.path.exists(output_file))

    @unittest.mock.patch('urllib.request.urlopen')
    def test_live_mode_partial_source_failure(self, mock_urlopen):
        # Mock one success (returns simple HTML structure) and one failure
        class MockResponse:
            def __init__(self, data):
                self.data = data
            def read(self):
                return self.data.encode('utf-8')
            def __enter__(self):
                return self
            def __exit__(self, exc_type, exc_val, exc_tb):
                pass

        # Return mock html for official, but raise exception for third-party
        def urlopen_side_effect(req, timeout=None):
            url = req.full_url if hasattr(req, 'full_url') else str(req)
            if "pokemongolive" in url:
                html = """
                <div>
                  <a href="/news/event-10th-anniversary-party-2026" class="_newsCard_1stmd_22">
                    <pg-date-format timestamp="1783443540000"></pg-date-format>
                    <div class="_size:heading_ovqdr_19">10th Anniversary Party</div>
                  </a>
                </div>
                """
                return MockResponse(html)
            else:
                raise Exception("Third-party site blocked")

        mock_urlopen.side_effect = urlopen_side_effect

        with tempfile.TemporaryDirectory() as tmpdir:
            output_file = os.path.join(tmpdir, "events.json")
            # Should not raise any error since at least one source succeeded
            generate_feed(fixture_mode=False, output_path=output_file)
            
            self.assertTrue(os.path.exists(output_file))
            with open(output_file, "r", encoding="utf-8") as f:
                feed = json.load(f)
            
            # Should have processed events from the successful source
            self.assertTrue(len(feed["events"]) > 0)

if __name__ == "__main__":
    unittest.main()
