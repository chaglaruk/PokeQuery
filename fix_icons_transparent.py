from pathlib import Path
from PIL import Image

src = Path.home() / "Downloads" / "a"
dst = Path(r"app/src/main/res/drawable-nodpi")

mapping = {
    "ChatGPT Image 20 Haz 2026 14_54_51 (5).png": "goal_safe_cleanup_icon.png",
    "ChatGPT Image 20 Haz 2026 14_54_51 (6).png": "goal_candy_prep_icon.png",
    "ChatGPT Image 20 Haz 2026 14_54_51 (7).png": "goal_trade_icon.png",
    "ChatGPT Image 20 Haz 2026 14_54_51 (8).png": "goal_hundo_icon.png",
    "ChatGPT Image 20 Haz 2026 14_54_51 (9).png": "goal_tag_icon.png",
    "ChatGPT Image 20 Haz 2026 14_54_51 (10).png": "goal_expert_icon.png",
}

def white_to_alpha(img, threshold=245):
    img = img.convert("RGBA")
    data = []
    for r, g, b, a in img.getdata():
        if r >= threshold and g >= threshold and b >= threshold:
            data.append((255, 255, 255, 0))
        else:
            data.append((r, g, b, a))
    img.putdata(data)
    return img

for src_name, dst_name in mapping.items():
    in_file = src / src_name
    out_file = dst / dst_name
    img = Image.open(in_file)
    img = white_to_alpha(img, threshold=245)
    img.save(out_file)

print("Done: icon backgrounds made transparent.")
