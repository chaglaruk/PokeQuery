"""Validate local links and publication field limits in the pre-release handoff."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FILES = [
    'README.md', 'CHANGELOG.md', 'docs/ROADMAP.md', 'docs/VALIDATION_MATRIX.md',
    'docs/audit/BUG_REPORT.md', 'docs/audit/FEATURE_GAP_ROADMAP.md',
    'docs/ai/AI_ASSISTANT_ROADMAP.md', 'docs/ai/AI_FEASIBILITY.md',
    'docs/growth/COMMUNITY_LAUNCH_PACK.md', 'docs/growth/EXECUTION_CHECKPOINT_2026-10-07.md',
    'docs/growth/PAID_ACQUISITION_GATE.md', 'docs/growth/PLAY_STORE_LISTING_PACK.md',
    'docs/release/RELEASE_READINESS.md', 'docs/release/V078_RELEASE_PLAN.md',
    'docs/release/PRERELEASE_EXECUTION_2026-10-08.md',
]

links = 0
for name in FILES:
    path = ROOT / name
    content = path.read_text(encoding='utf-8')
    for target in re.findall(r'!?\[[^\]\n]+\]\(([^)]+)\)', content):
        if target.startswith(('https://', 'http://', '#', 'mailto:')):
            continue
        destination = path.parent / target.split('#')[0]
        assert destination.exists(), f'{name}: missing local link {target}'
        links += 1

pack = (ROOT / 'docs/growth/PLAY_STORE_LISTING_PACK.md').read_text(encoding='utf-8')
locales = ['EN', 'TR', 'DE', 'ES', 'FR', 'IT']
for locale in locales:
    section = pack.split(f'## {locale}\n\n', 1)[1].split('\n## ', 1)[0]
    for label, limit in [('App name', 30), ('Short description', 80), ('Full description', 4000)]:
        match = re.search(rf'{label} \((\d+)/[\d,]+\):\n\n(.*?)(?=\n\n(?:Short description|Full description) \(|\Z)', section, re.S)
        assert match, f'{locale}: missing {label}'
        text = match.group(2).rstrip('\n')
        assert len(text) == int(match.group(1)), f'{locale}: stale {label} count'
        assert len(text) <= limit, f'{locale}: {label} exceeds {limit}'
        assert '|' not in text, f'{locale}: unexpected pipe in listing copy'

plan = (ROOT / 'docs/release/V078_RELEASE_PLAN.md').read_text(encoding='utf-8')
for locale in locales:
    section = plan.split(f'### {locale}\n\n', 1)[1].split('\n##', 1)[0]
    notes, addition = section.split('\n\nConditional addition: ', 1)
    assert len(notes + '\n' + addition.strip()) <= 500, f'{locale}: combined proposed notes exceed 500'
print(f'PASS: {len(FILES)} documents, {links} local links, six listing limits/counts and six release-note limits.')
