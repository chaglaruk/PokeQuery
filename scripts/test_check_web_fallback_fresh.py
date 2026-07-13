import json
import tempfile
import unittest
from pathlib import Path

from scripts.check_web_fallback_fresh import (
    _comparison_exit_code,
    _load_json,
    _semantic_hash,
)


class FallbackFreshnessTest(unittest.TestCase):
    def test_formatting_and_bom_do_not_change_semantics(self) -> None:
        value = {"lastUpdated": "2026-07-13", "events": [{"id": "one", "value": 1}]}
        variants = (
            json.dumps(value) + "\n",
            json.dumps(value, indent=2).replace("\n", "\r\n") + "\r\n",
            "\ufeff" + json.dumps(value, indent=4),
        )
        with tempfile.TemporaryDirectory() as directory:
            paths = []
            for index, content in enumerate(variants):
                path = Path(directory) / f"feed-{index}.json"
                path.write_text(content, encoding="utf-8", newline="")
                paths.append(path)
            loaded = [_load_json(path) for path in paths]

        self.assertTrue(all(item == value for item in loaded))
        self.assertEqual(len({_semantic_hash(item) for item in loaded}), 1)
        self.assertTrue(all(_comparison_exit_code(value, item) == 0 for item in loaded))

    def test_changed_value_is_detected(self) -> None:
        original = {"events": [{"id": "one", "value": 1}]}
        changed = {"events": [{"id": "one", "value": 2}]}
        self.assertEqual(_comparison_exit_code(original, changed), 2)
        self.assertNotEqual(_semantic_hash(original), _semantic_hash(changed))


if __name__ == "__main__":
    unittest.main()
