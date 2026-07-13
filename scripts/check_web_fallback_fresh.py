#!/usr/bin/env python3
"""
Deterministic freshness check for the bundled PWA event-feed fallback.

Compares `web/public/event-feed-fallback.json` against the canonical
`docs/event-feed/pokequery-events.json` and exits non-zero when the
fallback is stale (i.e. the file contents differ or the fallback's
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


def _sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def _load_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def main() -> int:
    if not FALLBACK_FEED.exists():
        print(f"error: fallback feed not found at {FALLBACK_FEED.relative_to(REPO_ROOT)}")
        return 1
    if not CANONICAL_FEED.exists():
        print(f"error: canonical feed not found at {CANONICAL_FEED.relative_to(REPO_ROOT)}")
        return 1

    canonical_hash = _sha256(CANONICAL_FEED)
    fallback_hash = _sha256(FALLBACK_FEED)

    canonical_json = _load_json(CANONICAL_FEED)
    fallback_json = _load_json(FALLBACK_FEED)

    canonical_updated = canonical_json.get("lastUpdated", "")
    fallback_updated = fallback_json.get("lastUpdated", "")

    print(f"canonical:  lastUpdated={canonical_updated} sha256={canonical_hash[:16]}...")
    print(f"fallback:   lastUpdated={fallback_updated} sha256={fallback_hash[:16]}...")

    if canonical_hash == fallback_hash:
        print("ok: fallback matches canonical feed")
        return 0

    if fallback_updated < canonical_updated:
        print(
            f"stale: fallback lastUpdated={fallback_updated} "
            f"< canonical lastUpdated={canonical_updated}"
        )
        return 3

    print("drift: fallback differs from canonical but lastUpdated is newer/equal")
    return 2


if __name__ == "__main__":
    sys.exit(main())
