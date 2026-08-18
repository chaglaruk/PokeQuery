#!/usr/bin/env python3
"""
Deterministic freshness check for the bundled PWA event-feed fallback.

Compares `web/public/event-feed-fallback.json` against the canonical
`docs/event-feed/pokequery-events.json` and exits non-zero when the
fallback is stale (i.e. the parsed JSON differs or the fallback's
`lastUpdated` field is older than the canonical one).

Designed to run both locally and in CI to prevent shipping a stale
fallback snapshot to PWA users.

Exit codes:
    0 - fallback is fresh and matches the canonical feed
    1 - fallback file is missing
    2 - fallback content differs from the canonical feed
    3 - fallback `lastUpdated` is older than the canonical `lastUpdated`
"""
from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
CANONICAL_FEED = REPO_ROOT / "docs" / "event-feed" / "pokequery-events.json"
FALLBACK_FEED = REPO_ROOT / "web" / "public" / "event-feed-fallback.json"


def _canonical_json(value: object) -> str:
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def _load_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8-sig") as f:
        return json.load(f)


def _semantic_hash(value: object) -> str:
    return hashlib.sha256(_canonical_json(value).encode("utf-8")).hexdigest()


def _sync(canonical_json: dict) -> None:
    FALLBACK_FEED.write_text(
        json.dumps(canonical_json, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
        newline="\n",
    )


def _comparison_exit_code(canonical_json: dict, fallback_json: dict) -> int:
    if canonical_json == fallback_json:
        return 0
    if fallback_json.get("lastUpdated", "") < canonical_json.get("lastUpdated", ""):
        return 3
    return 2


def main() -> int:
    if not FALLBACK_FEED.exists():
        print(f"error: fallback feed not found at {FALLBACK_FEED.relative_to(REPO_ROOT)}")
        return 1
    if not CANONICAL_FEED.exists():
        print(f"error: canonical feed not found at {CANONICAL_FEED.relative_to(REPO_ROOT)}")
        return 1

    canonical_json = _load_json(CANONICAL_FEED)

    if sys.argv[1:] == ["--sync"]:
        _sync(canonical_json)
        print(f"synced: {FALLBACK_FEED.relative_to(REPO_ROOT)}")
        return 0
    if sys.argv[1:]:
        print("usage: check_web_fallback_fresh.py [--sync]")
        return 64

    fallback_json = _load_json(FALLBACK_FEED)
    canonical_hash = _semantic_hash(canonical_json)
    fallback_hash = _semantic_hash(fallback_json)

    canonical_updated = canonical_json.get("lastUpdated", "")
    fallback_updated = fallback_json.get("lastUpdated", "")

    print(f"canonical:  lastUpdated={canonical_updated} sha256={canonical_hash[:16]}...")
    print(f"fallback:   lastUpdated={fallback_updated} sha256={fallback_hash[:16]}...")

    result = _comparison_exit_code(canonical_json, fallback_json)
    if result == 0:
        print("ok: fallback matches canonical feed")
        return 0

    if result == 3:
        print(
            f"stale: fallback lastUpdated={fallback_updated} "
            f"< canonical lastUpdated={canonical_updated}"
        )
        return 3

    print("drift: fallback differs from canonical but lastUpdated is newer/equal")
    return 2


if __name__ == "__main__":
    sys.exit(main())
