import os
from PIL import Image


SCREENSHOT_DIR = os.path.join("docs", "screenshots", "v060_trust_education")
REQUIRED = [
    "01_home.png",
    "02_safe_cleanup_detail.png",
    "03_trade_fodder_detail.png",
    "04_settings.png",
    "05_knowledge.png",
    "06_knowledge_expanded.png",
    "07_changelog.png",
    "08_favorites.png",
    "09_history.png",
]


def main():
    files = []
    for name in REQUIRED:
        path = os.path.join(SCREENSHOT_DIR, name)
        if not os.path.exists(path):
            raise SystemExit(f"ERROR: Missing required screenshot: {path}")
        files.append(path)

    images = [Image.open(path) for path in files]
    cols = min(3, len(images))
    rows = (len(images) + cols - 1) // cols
    width, height = images[0].size
    padding = 20
    sheet = Image.new("RGBA", (cols * width + (cols + 1) * padding, rows * height + (rows + 1) * padding), (8, 16, 32, 255))

    for index, image in enumerate(images):
        x = padding + (index % cols) * (width + padding)
        y = padding + (index // cols) * (height + padding)
        sheet.paste(image, (x, y))

    target = os.path.join(SCREENSHOT_DIR, "contact_sheet.png")
    sheet.save(target)
    print(f"Created {target}")


if __name__ == "__main__":
    main()
