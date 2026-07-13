import os
import glob
from PIL import Image

def create_contact_sheet(device_dir, output_path):
    """Create a single contact sheet from all PNGs in a device directory."""
    files = sorted(
        path for path in glob.glob(os.path.join(device_dir, "*.png"))
        if "contact_sheet" not in os.path.basename(path).lower()
        and "thumbnail" not in os.path.basename(path).lower()
        and not os.path.basename(path).lower().startswith(("thumb", "tmp", "temp"))
    )
    if not files:
        print(f"No screenshots found in {device_dir}")
        return False

    print(f"Creating contact sheet from {len(files)} screenshots in {device_dir}")

    # Open all images and thumbnail them to a consistent width
    thumb_w = 240
    thumbs = []
    for f in files:
        img = Image.open(f).convert("RGBA")
        ratio = thumb_w / img.width
        thumb_h = int(img.height * ratio)
        thumb = img.resize((thumb_w, thumb_h), Image.LANCZOS)
        thumbs.append(thumb)

    # Grid layout: 4 columns
    cols = min(4, len(thumbs))
    rows = (len(thumbs) + cols - 1) // cols

    padding = 12
    label_h = 20
    cell_w = thumb_w + padding
    sheet_w = cols * cell_w + padding
    row_heights = [
        max(thumb.height for thumb in thumbs[row * cols:(row + 1) * cols]) + padding + label_h
        for row in range(rows)
    ]
    sheet_h = sum(row_heights) + padding

    sheet = Image.new("RGBA", (sheet_w, sheet_h), (11, 15, 23, 255))

    from PIL import ImageDraw
    draw = ImageDraw.Draw(sheet)

    for idx, (thumb, f) in enumerate(zip(thumbs, files)):
        col = idx % cols
        row = idx // cols
        x = padding + col * cell_w
        y = padding + sum(row_heights[:row]) + label_h
        sheet.paste(thumb, (x, y))

        label = os.path.basename(f).replace(".png", "")
        draw.text((x, padding + sum(row_heights[:row])), label[:30], fill=(232, 234, 240, 255))

    sheet.save(output_path)
    print(f"  Saved: {output_path} ({sheet_w}x{sheet_h})")
    return True

def main():
    base_dir = os.path.join("docs", "screenshots", "pwa_initial_qa")
    if not os.path.isdir(base_dir):
        print(f"ERROR: Directory not found: {base_dir}")
        return

    # Create one contact sheet per device
    device_dirs = sorted([
        d for d in os.listdir(base_dir)
        if os.path.isdir(os.path.join(base_dir, d))
    ])

    for device in device_dirs:
        device_dir = os.path.join(base_dir, device)
        output_path = os.path.join(base_dir, device, "contact_sheet.png")
        create_contact_sheet(device_dir, output_path)

    # Create a combined contact sheet with one screenshot per device (Home screen)
    print("\nCreating cross-device comparison contact sheet...")
    state = "03-home-en.png"
    files = []
    for device in device_dirs:
        f = os.path.join(base_dir, device, state)
        if os.path.exists(f):
            files.append((device, f))

    if files:
        thumb_w = 200
        thumbs = []
        labels = []
        for device, f in files:
            img = Image.open(f).convert("RGBA")
            ratio = thumb_w / img.width
            thumb_h = int(img.height * ratio)
            thumb = img.resize((thumb_w, thumb_h), Image.LANCZOS)
            thumbs.append(thumb)
            labels.append(device)

        padding = 12
        label_h = 20
        cell_w = thumb_w + padding
        cell_h = max(t.height for t in thumbs) + padding + label_h
        cols = len(thumbs)
        rows = 1

        sheet_w = cols * cell_w + padding
        sheet_h = cell_h + padding
        sheet = Image.new("RGBA", (sheet_w, sheet_h), (11, 15, 23, 255))

        from PIL import ImageDraw
        draw = ImageDraw.Draw(sheet)

        for idx, (thumb, label) in enumerate(zip(thumbs, labels)):
            x = padding + idx * cell_w
            y = padding + label_h
            sheet.paste(thumb, (x, y))
            draw.text((x, padding), label, fill=(11, 140, 156, 255))

        output_path = os.path.join(base_dir, "contact_sheet_home_comparison.png")
        sheet.save(output_path)
        print(f"  Saved: {output_path} ({sheet_w}x{sheet_h})")

if __name__ == "__main__":
    main()
