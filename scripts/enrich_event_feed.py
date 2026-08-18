#!/usr/bin/env python3
"""Post-process the generated Event Guide feed with source-page details.

The base generator discovers events from stable listing pages. This pass follows each event's
already-recorded source URL at build time (never at Android runtime), extracts concise factual
sections, normalizes source/SEO title artifacts, and refuses to publish a detail-empty active
gameplay feed in strict mode. Curated event_metadata.json values always win.
"""

from __future__ import annotations

import argparse
import html
import json
import re
import sys
import time
import urllib.error
import urllib.request
from dataclasses import dataclass, field
from html.parser import HTMLParser
from pathlib import Path
from typing import Dict, Iterable, List, Optional
from urllib.parse import urljoin, urlparse

DEFAULT_FEED = Path("docs/event-feed/pokequery-events.json")
USER_AGENT = "PokeQuery-EventFeed/0.7.6 (+https://github.com/chaglaruk/PokeQuery)"

# Event discovery currently records only these two configured sources. Pokémon GO Live now
# redirects pokemongolive.com article URLs to pokemongo.com, so both official hostnames are
# explicitly allowed. Exact hosts (plus their conventional www aliases) keep build-time source
# enrichment from becoming a generic URL fetcher.
ALLOWED_SOURCE_HOSTS = frozenset({
    "pokemongolive.com",
    "www.pokemongolive.com",
    "pokemongo.com",
    "www.pokemongo.com",
    "leekduck.com",
    "www.leekduck.com",
})

GENERIC_FACTS = {
    "verify details in-game before acting.",
    "prepare for event catches and inventory limits.",
    "review recent catches before transfer.",
    "işlem yapmadan önce oyun içi detayları kontrol edin.",
    "etkinlik yakalamaları ve envanter limitleri için hazırlık yapın.",
    "transferden önce son yakalamaları kontrol edin.",
}

DETAIL_FIELDS = ("featuredPokemon", "boostedPokemon", "bonuses", "raids", "research", "eventNotes")
LOCALIZED_SUFFIXES = ("Tr", "De", "Es", "Fr", "It")
GAMEPLAY_CATEGORIES = {
    "MAJOR_GAMEPLAY",
    "LIMITED_GAMEPLAY",
    "ROUTINE_ROTATION",
    "RAID_ROTATION",
    "SEASON_GBL",
}


def clean_text(value: str) -> str:
    value = html.unescape(value or "")
    value = value.replace("\xa0", " ").replace("\u200b", "")
    value = re.sub(r"\s+", " ", value).strip()
    value = re.sub(r"\s+([,.;:!?])", r"\1", value)
    value = re.sub(r"(?<=[)\]])-(?=[^\W\d_])", " - ", value, flags=re.UNICODE)
    return value


def normalized_fact(value: str) -> str:
    # Python lower/casefold turns capital Turkish İ into i + COMBINING DOT ABOVE. Remove only that
    # artifact so localized generator placeholders compare reliably without transliterating text.
    return clean_text(value).casefold().replace("\u0307", "")


def is_lead_in_only(value: object) -> bool:
    if not isinstance(value, str) or not value.strip():
        return False
    normalized = normalized_fact(value)
    patterns = (
        r"^the following pok[eé]mon will appear(?: more frequently)? in the wild[.:]?$",
        r"^the following pok[eé]mon will appear in raids?[.:]?$",
        r"^appearing in (?:one|three|five|1|3|5)[ -]star raids?[.:]?$",
    )
    return any(re.fullmatch(pattern, normalized, flags=re.IGNORECASE) for pattern in patterns)


def is_generic(value: object) -> bool:
    if not isinstance(value, str) or not value.strip():
        return True
    return normalized_fact(value) in {normalized_fact(item) for item in GENERIC_FACTS} or is_lead_in_only(value)


def meaningful(value: object) -> bool:
    return isinstance(value, str) and bool(value.strip()) and not is_generic(value)


def normalize_event_title(title: str) -> str:
    """Clean listing/SEO artifacts without renaming branded event names."""
    value = clean_text(title)
    value = re.sub(r"\s*[|]\s*Pokémon GO\s*$", "", value, flags=re.IGNORECASE)
    value = re.sub(
        r"\s*[-–—]\s*(January|February|March|April|May|June|July|August|September|October|November|December)\s+20\d{2}\s*$",
        "",
        value,
        flags=re.IGNORECASE,
    )
    if re.search(r"\b(?:league|cup)\b", value, flags=re.IGNORECASE):
        value = re.sub(r"\s*[-–—|]\s*Forever Forward\s*$", "", value, flags=re.IGNORECASE)

    raid_patterns = (
        (r"^(.+?)\s+in\s+5[- ]star\s+Raid Battles$", r"\1 — 5-Star Raids"),
        (r"^(.+?)\s+in\s+Mega Raids$", r"\1 — Mega Raids"),
        (r"^(.+?)\s+in\s+Shadow Raids$", r"\1 — Shadow Raids"),
        (r"^(.+?)\s+in\s+Max Battles$", r"\1 — Max Battles"),
    )
    for pattern, replacement in raid_patterns:
        if re.match(pattern, value, flags=re.IGNORECASE):
            value = re.sub(pattern, replacement, value, flags=re.IGNORECASE)
            break

    value = re.sub(r"\s+[-–—]\s+", " — ", value)
    value = re.sub(r"\s{2,}", " ", value).strip(" -–—|")
    return value


def derive_turkish_title(title: str) -> Optional[str]:
    """Translate only deterministic structural suffixes; never machine-translate event brands."""
    suffixes = (
        (" — 5-Star Raids", " — 5 Yıldızlı Akınlar"),
        (" — Mega Raids", " — Mega Akınlar"),
        (" — Shadow Raids", " — Gölge Akınları"),
        (" — Max Battles", " — Max Savaşları"),
    )
    for source, target in suffixes:
        if title.endswith(source):
            base = title[: -len(source)]
            if re.search(r"\b(?:and|or)\b", base, flags=re.IGNORECASE):
                return None
            return base + target
    return None


@dataclass
class ParsedPage:
    title: Optional[str] = None
    intro: List[str] = field(default_factory=list)
    sections: Dict[str, List[str]] = field(default_factory=dict)


class DetailPageParser(HTMLParser):
    """Dependency-free event-article extractor.

    Source sites can have navigation cards, raid widgets and footer resources before/after the
    actual event article. Event content begins at the first H1. Text blocks before that H1 are
    discarded so site chrome cannot become an event summary. H2 starts a semantic section and
    H3/H4 remain inside the current H2 when possible.
    """

    BLOCKS = {"h1", "h2", "h3", "h4", "p", "li"}
    VOID_TAGS = {
        "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta",
        "param", "source", "track", "wbr",
    }

    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.page = ParsedPage()
        self._root_tag: Optional[str] = None
        self._depth = 0
        self._parts: List[str] = []
        self._section = "intro"
        self._skip_depth = 0
        self._seen_h1 = False

    def handle_starttag(self, tag: str, attrs) -> None:
        tag = tag.lower()
        if tag in {"script", "style", "svg", "noscript"}:
            self._skip_depth += 1
            return
        if self._skip_depth:
            return
        if self._root_tag is None and tag in self.BLOCKS:
            self._root_tag = tag
            self._depth = 1
            self._parts = []
        elif self._root_tag is not None and tag not in self.VOID_TAGS:
            # HTML void elements such as <br> and <img> never receive a closing tag. Counting
            # them as nested depth would leave the current paragraph/list item permanently open
            # and swallow every later section on the page.
            self._depth += 1

    def handle_endtag(self, tag: str) -> None:
        tag = tag.lower()
        if tag in {"script", "style", "svg", "noscript"} and self._skip_depth:
            self._skip_depth -= 1
            return
        if tag in self.VOID_TAGS:
            return
        if self._skip_depth or self._root_tag is None:
            return
        self._depth -= 1
        if self._depth > 0:
            return

        text = clean_text(" ".join(self._parts))
        root = self._root_tag
        self._root_tag = None
        self._parts = []
        self._depth = 0
        if not text:
            return

        if root == "h1":
            if self.page.title is None:
                self.page.title = text
                self._seen_h1 = True
                self._section = "intro"
            return

        if not self._seen_h1:
            return

        if root == "h2":
            self._section = text
            self.page.sections.setdefault(self._section, [])
            return

        if root in {"h3", "h4"}:
            if self._section == "intro":
                self._section = text
                self.page.sections.setdefault(self._section, [])
            else:
                self.page.sections.setdefault(self._section, []).append(text)
            return

        if self._section == "intro":
            self.page.intro.append(text)
        else:
            self.page.sections.setdefault(self._section, []).append(text)

    def handle_data(self, data: str) -> None:
        if not self._skip_depth and self._root_tag is not None:
            self._parts.append(data)


def parse_detail_html(raw_html: str) -> ParsedPage:
    parser = DetailPageParser()
    parser.feed(raw_html)
    return parser.page


def _useful_lines(lines: Iterable[str]) -> List[str]:
    result: List[str] = []
    seen = set()
    boilerplate = (
        "add to calendar",
        "event starts",
        "event ends",
        "starts:",
        "ends:",
        "local time",
        "share this",
        "advertisement",
        "privacy policy",
        "cookie",
    )
    for raw in lines:
        line = clean_text(raw)
        lower = line.lower()
        if len(line) < 12 or any(lower.startswith(prefix) for prefix in boilerplate) or is_lead_in_only(line):
            continue
        if lower in seen:
            continue
        seen.add(lower)
        result.append(line)
    return result


def _compact(lines: Iterable[str], max_items: int = 7, max_chars: int = 900) -> Optional[str]:
    chosen = _useful_lines(lines)[:max_items]
    if not chosen:
        return None
    output = " • ".join(chosen)
    if len(output) > max_chars:
        output = output[: max_chars - 1].rstrip(" ,;:•-") + "…"
    return output


def _classified_section_lines(page: ParsedPage) -> Dict[str, List[str]]:
    buckets: Dict[str, List[str]] = {
        "featured": [],
        "bonuses": [],
        "raids": [],
        "research": [],
        "notes": [],
    }
    for heading, lines in page.sections.items():
        lower = heading.lower()
        if any(key in lower for key in ("research", "collection challenge", "challenge", "ticket", "timed")):
            buckets["research"].extend(lines)
        elif any(key in lower for key in ("raid", "max battle", "max battles")):
            buckets["raids"].extend(lines)
        elif any(key in lower for key in ("featured", "wild", "encounter", "egg", "spawn", "incense", "lure", "showcase", "shiny")):
            buckets["featured"].extend(lines)
        elif any(key in lower for key in ("bonus", "bonuses", "reward", "rewards")):
            buckets["bonuses"].extend(lines)
        elif any(key in lower for key in ("additional", "note", "notes", "important", "remember")):
            buckets["notes"].extend(lines)
    return buckets


def extract_enrichment(page: ParsedPage, event_title: str) -> Dict[str, str]:
    _ = event_title
    intro = _compact(page.intro, max_items=2, max_chars=650)
    sections = _classified_section_lines(page)
    featured = _compact(sections["featured"])
    bonuses = _compact(sections["bonuses"])
    raids = _compact(sections["raids"])
    research = _compact(sections["research"])
    notes = _compact(sections["notes"], max_items=4, max_chars=600)

    result: Dict[str, str] = {}
    if intro:
        result["summary"] = intro
    if featured:
        result["featuredPokemon"] = featured
    if bonuses:
        result["bonuses"] = bonuses
    if raids:
        result["raids"] = raids
    if research:
        result["research"] = research
    if notes:
        result["eventNotes"] = notes
    if not any(key in result for key in DETAIL_FIELDS) and intro:
        result["eventNotes"] = intro
    return result


def validate_source_url(url: str) -> str:
    """Accept only configured public Event Guide sources over standard HTTPS."""
    parsed = urlparse(url)
    host = (parsed.hostname or "").lower().rstrip(".")
    if parsed.scheme.lower() != "https":
        raise ValueError(f"Event source URL must use HTTPS: {url}")
    if parsed.username or parsed.password:
        raise ValueError(f"Event source URL must not contain credentials: {url}")
    if parsed.port not in (None, 443):
        raise ValueError(f"Event source URL must use the standard HTTPS port: {url}")
    if host not in ALLOWED_SOURCE_HOSTS:
        raise ValueError(f"Event source host is not approved: {host or '<missing>'}")
    return url


class ApprovedSourceRedirectHandler(urllib.request.HTTPRedirectHandler):
    """Refuse redirects that leave the approved HTTPS Event Guide source set."""

    def redirect_request(self, req, fp, code, msg, headers, newurl):
        validated = validate_source_url(urljoin(req.full_url, newurl))
        return super().redirect_request(req, fp, code, msg, headers, validated)


def fetch_html(url: str, timeout: int = 15, attempts: int = 3, backoff_seconds: float = 0.75) -> str:
    validated_url = validate_source_url(url)
    request = urllib.request.Request(
        validated_url,
        headers={"User-Agent": USER_AGENT, "Accept": "text/html,application/xhtml+xml"},
    )
    opener = urllib.request.build_opener(ApprovedSourceRedirectHandler())
    last_error: Optional[BaseException] = None
    for attempt in range(attempts):
        try:
            with opener.open(request, timeout=timeout) as response:
                # Defense in depth: the redirect handler validates each hop, and the final URL is
                # checked again in case a custom response handler ever changes redirect behavior.
                validate_source_url(response.geturl())
                content_type = response.headers.get("Content-Type", "")
                charset_match = re.search(r"charset=([^;]+)", content_type, flags=re.IGNORECASE)
                charset = charset_match.group(1).strip() if charset_match else "utf-8"
                return response.read().decode(charset, errors="replace")
        except (urllib.error.URLError, TimeoutError, OSError) as exc:
            last_error = exc
            if attempt + 1 >= attempts:
                raise
            time.sleep(backoff_seconds * (2 ** attempt))
    if last_error is not None:
        raise last_error
    raise RuntimeError("source fetch failed without an exception")


def _clear_generic_localizations(event: dict, base_field: str) -> None:
    for suffix in LOCALIZED_SUFFIXES:
        field = f"{base_field}{suffix}"
        if is_generic(event.get(field)):
            event[field] = None


def apply_enrichment(event: dict, enrichment: Dict[str, str]) -> None:
    for field, value in enrichment.items():
        if not value:
            continue
        if is_generic(event.get(field)):
            event[field] = value
            _clear_generic_localizations(event, field)


def has_useful_detail(event: dict) -> bool:
    if event.get("pokemon"):
        return True
    return any(meaningful(event.get(field)) for field in DETAIL_FIELDS)


def has_useful_summary(event: dict) -> bool:
    return meaningful(event.get("summary"))


def validate_active_detail_quality(events: List[dict]) -> List[str]:
    failures: List[str] = []
    for event in events:
        status = str(event.get("status", "")).upper()
        if status not in {"CURRENT", "UPCOMING"}:
            continue
        category = str(event.get("eventCategory", "LIMITED_GAMEPLAY")).upper()
        if category in GAMEPLAY_CATEGORIES:
            if not has_useful_detail(event):
                failures.append(f"{event.get('id')}: active gameplay event has no useful detail fields")
        elif not (has_useful_detail(event) or has_useful_summary(event)):
            failures.append(f"{event.get('id')}: active event has neither useful detail nor summary")
    return failures


def enrich_feed(feed: dict, fetcher=fetch_html, strict: bool = False) -> dict:
    events = feed.get("events")
    if not isinstance(events, list):
        raise ValueError("feed.events must be a list")

    fetch_errors: List[str] = []
    for event in events:
        original_title = str(event.get("title") or "")
        normalized_title = normalize_event_title(original_title)
        if normalized_title:
            event["title"] = normalized_title
        if not event.get("titleTr"):
            derived_tr = derive_turkish_title(normalized_title)
            if derived_tr:
                event["titleTr"] = derived_tr

        source_url = str(event.get("sourceUrl") or "").strip()
        if not source_url:
            continue
        try:
            page = parse_detail_html(fetcher(source_url))
            page_title = normalize_event_title(page.title or "")
            # The discovery/catalog title is the event identity. Source-page H1 text is useful
            # only as a fallback for a genuinely title-less entry; never let an unrelated/error
            # page silently rename an existing event across all generated feed copies.
            if not normalized_title and page_title and len(page_title) <= 140:
                event["title"] = page_title
                if not event.get("titleTr"):
                    derived_tr = derive_turkish_title(page_title)
                    if derived_tr:
                        event["titleTr"] = derived_tr
            apply_enrichment(event, extract_enrichment(page, event.get("title", normalized_title)))
        except Exception as exc:
            fetch_errors.append(f"{event.get('id')}: {exc}")

    quality_failures = validate_active_detail_quality(events)
    if strict and quality_failures:
        message = "\n".join(f" - {failure}" for failure in quality_failures)
        if fetch_errors:
            message += "\nSource fetch errors:\n" + "\n".join(f" - {failure}" for failure in fetch_errors)
        raise RuntimeError("Event feed enrichment quality gate failed:\n" + message)

    feed["enrichment"] = {
        "pipeline": "source-page-sections-v3",
        "qualityFailures": len(quality_failures),
        "fetchErrors": len(fetch_errors),
    }
    return feed


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("feed", nargs="?", default=str(DEFAULT_FEED))
    parser.add_argument("--strict", action="store_true", help="Fail if an active event remains detail-empty")
    parser.add_argument("--timeout", type=int, default=15)
    args = parser.parse_args(argv)

    path = Path(args.feed)
    feed = json.loads(path.read_text(encoding="utf-8"))

    def configured_fetcher(url: str) -> str:
        return fetch_html(url, timeout=args.timeout)

    try:
        enriched = enrich_feed(feed, fetcher=configured_fetcher, strict=args.strict)
    except Exception as exc:
        print(str(exc), file=sys.stderr)
        return 1

    path.write_text(json.dumps(enriched, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Enriched {len(enriched.get('events', []))} Event Guide entries -> {path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())