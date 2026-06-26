import os
from PIL import Image


SCREENSHOT_DIR = os.path.join("docs", "screenshots", "v061_workflows_surface_context")

# Each entry maps a screenshot file to the critical test case it documents. Order = sheet order.
REQUIRED = [
    ("00_onboarding.png", "Onboarding still routes (v0.5.2 black-screen guard)"),
    ("01_home.png", "Home: trust chips + goal grid (regression)"),
    ("13_home_workflows_cards.png", "Home: v0.6.1 My Presets / Practice / Journal / Event Context cards"),
    ("02_my_presets_empty.png", "Personal Presets: local-only banner + empty state"),
    ("03_practice_mode.png", "Practice Mode: conceptual sandbox + matcher (matched/protected)"),
    ("04_journal_with_note.png", "Cleaning Journal: user-entered note persisted (local only)"),
    ("14_journal_editor.png", "Cleaning Journal: add-note editor with user-chosen action type"),
    ("05_event_context.png", "Event Context: offline manual notes + monthly Community Day"),
    ("06_safe_cleanup_detail.png", "Safe Cleanup detail (shortcut + widget target)"),
    ("08_qr_export.png", "QR export: dependency-free encoder, export-first, copy fallback"),
    ("07_changelog.png", "What Changed: v0.6.1 Phase 2 entry is Current"),
    ("09_shortcut_candy_prep.png", "App Shortcut target: 2x Candy Prep"),
    ("10_shortcut_trade_fodder.png", "App Shortcut target: Trade Fodder"),
    ("11_shortcut_expert_builder.png", "App Shortcut target: Expert Builder"),
    ("12_settings.png", "Settings (privacy + language foundation regression)"),
]


def main():
    files = []
    for name, _ in REQUIRED:
        path = os.path.join(SCREENSHOT_DIR, name)
        if not os.path.exists(path):
            raise SystemExit(f"ERROR: Missing required screenshot: {path}")
        files.append(path)

    # Normalize to a consistent cell size so phones of differing capture heights tile evenly.
    target_w = 540
    target_h = 1170
    images = []
    for path in files:
        img = Image.open(path).convert("RGB")
        images.append(img.resize((target_w, target_h), Image.LANCZOS))

    cols = 3
    rows = (len(images) + cols - 1) // cols
    padding = 16
    label_h = 34
    cell_w = target_w + 2 * padding
    cell_h = target_h + 2 * padding + label_h
    sheet = Image.new(
        "RGB",
        (cols * cell_w, rows * cell_h),
        (8, 16, 32),
    )

    from PIL import ImageDraw
    draw = ImageDraw.Draw(sheet)

    for index, (image, (_, caption)) in enumerate(zip(images, REQUIRED)):
        col = index % cols
        row = index // cols
        x = col * cell_w + padding
        y = row * cell_h + padding
        sheet.paste(image, (x, y))
        draw.text((x, y + target_h + 6), caption, fill=(20, 184, 166))

    target = os.path.join(SCREENSHOT_DIR, "contact_sheet.png")
    sheet.save(target)
    print(f"Created {target} ({len(images)} screenshots, {cols}x{rows} grid)")


if __name__ == "__main__":
    main()
