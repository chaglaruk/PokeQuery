#!/usr/bin/env python3
import unittest
import unittest.mock
import os
import sys
import tempfile
import json

# Ensure scripts directory is on sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from generate_event_feed import parse_date_range, get_event_id, generate_feed, validate_safety_constraints

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
            "suggestedSearch": "age0-2&!favorite",
            "titleTr": "Test Etkinliği",
            "noteTr": "Test Notu"
        }
        # Should not raise any exception
        validate_safety_constraints(valid_event)

    def test_safety_constraints_banned_word(self):
        invalid_event = {
            "id": "event-test",
            "suggestedSearch": "age0-2&!favorite",
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
                # Assert source fields exist and match schema
                self.assertIn("sourceName", ev)
                self.assertIn("sourceUrl", ev)
                self.assertIn("sourceType", ev)
                self.assertIn("lastUpdated", ev)
                self.assertIn(ev["sourceType"], ["official", "third-party"])
                self.assertTrue(ev["sourceUrl"].startswith("http"))

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
