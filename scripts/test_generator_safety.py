import unittest
import os
import sys
from unittest.mock import patch, mock_open

# Adjust path to import generate_event_feed
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from generate_event_feed import generate_feed

class TestGeneratorSafety(unittest.TestCase):
    @patch("os.path.exists", return_value=True)
    def test_fixture_mode_with_production_output_fails(self, mock_exists):
        # Setting output path to the production feed path should trigger sys.exit
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_dir = os.path.dirname(script_dir)
        prod_path = os.path.join(project_dir, "docs", "event-feed", "pokequery-events.json")
        
        with self.assertRaises(SystemExit) as cm:
            generate_feed(fixture_mode=True, output_path=prod_path)
            
        self.assertIn("Fixture mode cannot write to docs/event-feed/pokequery-events.json", str(cm.exception))

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

if __name__ == "__main__":
    unittest.main()
