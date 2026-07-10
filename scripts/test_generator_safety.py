import json
import os
import sys
import unittest
from unittest.mock import patch, mock_open

# Adjust path to import generate_event_feed
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from generate_event_feed import (
    METADATA_ID_ALIASES,
    canonical_event_id,
    ensure_traded_exclusion,
    generate_feed,
    put_raw_event,
    resolve_event_metadata,
)


class TestGeneratorSafety(unittest.TestCase):
    @patch("os.path.exists", return_value=True)
    def test_fixture_mode_with_production_output_fails(self, mock_exists):
        # Setting output path to the production feed path should trigger sys.exit
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_dir = os.path.dirname(script_dir)
        prod_path = os.path.join(project_dir, "docs", "event-feed", "pokequery-events.json")

        with self.assertRaises(SystemExit) as cm:
            generate_feed(fixture_mode=True, output_path=prod_path)

        self.assertIn(
            "Fixture mode cannot write to docs/event-feed/pokequery-events.json",
            str(cm.exception),
        )

    @patch("os.path.exists", return_value=True)
    @patch("builtins.open", new_callable=mock_open, read_data='{"sources": []}')
    @patch("os.makedirs")
    def test_fixture_mode_with_test_output_succeeds(self, mock_makedirs, mock_file, mock_exists):
        # Setting output path to a test file should not trigger sys.exit
        test_path = "docs/event-feed/pokequery-events.generated.test.json"

        # This should execute and open files without raising SystemExit for path validation
        try:
            generate_feed(fixture_mode=True, output_path=test_path)
        except SystemExit:
            self.fail("generate_feed raised SystemExit unexpectedly in test mode!")
        except Exception:
            # We expect normal file parsing exceptions since we mocked open with empty data,
            # but it should not fail on the path safety check.
            pass

    def test_workflow_production_step_has_no_fixture_mode(self):
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_dir = os.path.dirname(script_dir)
        workflow = os.path.join(project_dir, ".github", "workflows", "update-event-feed.yml")
        self.assertTrue(os.path.exists(workflow), "workflow file missing")
        text = open(workflow, encoding="utf-8").read()
        self.assertNotIn("--fixture-mode", text)
        self.assertIn("python scripts/generate_event_feed.py", text)

    def test_production_feed_is_not_tiny_fixture(self):
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_dir = os.path.dirname(script_dir)
        prod_path = os.path.join(project_dir, "docs", "event-feed", "pokequery-events.json")
        self.assertTrue(os.path.exists(prod_path))
        data = json.load(open(prod_path, encoding="utf-8"))
        events = data.get("events", [])
        self.assertGreaterEqual(len(events), 20, "production feed must not be a tiny fixture feed")
        # Generated search strings must never contain pipe characters.
        for ev in events:
            search = ev.get("suggestedSearch", "")
            self.assertNotIn("|", search)
            self.assertEqual(1, search.split("&").count("!traded"))
            for value in ev.values():
                if isinstance(value, str):
                    self.assertNotIn("|", value, f"pipe found in event {ev.get('id')}")

    def test_metadata_aliases_resolve_go_fest(self):
        metadata = {
            "event-go-fest-global-2026": {
                "featuredPokemon": "Mewtwo",
                "startDate": "2026-07-11",
                "endDate": "2026-07-12",
            }
        }
        resolved = resolve_event_metadata(
            metadata, "event-pokemon-go-fest-2026-global", "Pokémon GO Fest 2026: Global"
        )
        self.assertEqual(resolved.get("featuredPokemon"), "Mewtwo")
        self.assertIn("event-pokemon-go-fest-2026-global", METADATA_ID_ALIASES)

    def test_go_fest_metadata_does_not_broad_match_news_titles(self):
        metadata = {
            "event-go-fest-global-2026": {
                "featuredPokemon": "Mewtwo",
                "eventCategory": "MAJOR_GAMEPLAY",
            }
        }
        resolved = resolve_event_metadata(
            metadata,
            "event-community-celebrations-go-fest-2026-details-en",
            "Get ready for Pokemon GO Fest 2026: Global Community Celebrations",
        )
        self.assertEqual({}, resolved)

    def test_raw_event_insert_canonicalizes_true_duplicate_sources(self):
        raw_events = {}
        put_raw_event(raw_events, {
            "id": "event-pokemon-go-fest-2026-global",
            "title": "Pokemon GO Fest 2026: Global",
            "kind": "GENERIC_EVENT",
            "sourceName": "Leek Duck Events",
        })
        put_raw_event(raw_events, {
            "id": "event-go-fest-2026-global-final-details",
            "title": "Pokemon GO Fest 2026: Global final details!",
            "kind": "GENERIC_EVENT",
            "sourceName": "Pokemon GO Live News",
        })

        self.assertEqual("event-pokemon-go-fest-2026-global", canonical_event_id("event-go-fest-2026-global-final-details"))
        self.assertEqual(["event-pokemon-go-fest-2026-global"], list(raw_events.keys()))
        self.assertEqual("Pokemon GO Live News", raw_events["event-pokemon-go-fest-2026-global"]["sourceName"])

    def test_traded_exclusion_is_added_once_and_preserved(self):
        self.assertEqual("age0&!favorite&!traded", ensure_traded_exclusion("age0&!favorite"))
        self.assertEqual("age0&!favorite&!traded", ensure_traded_exclusion("age0&!favorite&!traded"))

    def test_curated_metadata_searches_have_one_traded_exclusion(self):
        script_dir = os.path.dirname(os.path.abspath(__file__))
        metadata_path = os.path.join(os.path.dirname(script_dir), "docs", "event-feed", "event_metadata.json")
        with open(metadata_path, encoding="utf-8") as handle:
            metadata = json.load(handle)
        searches = [entry["suggestedSearch"] for entry in metadata.values() if entry.get("suggestedSearch")]
        self.assertTrue(searches)
        for search in searches:
            self.assertEqual(1, search.split("&").count("!traded"))
            self.assertNotIn("|", search)


if __name__ == "__main__":
    unittest.main()
