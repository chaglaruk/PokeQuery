import os
from PIL import Image

def load(path):
    return Image.open(path).convert('RGB')

targets = [
    'docs/design_targets/target_onboarding.png',
    'docs/design_targets/target_home.png',
    'docs/design_targets/target_safe_cleanup_preview.png',
    'docs/design_targets/target_candy_prep_preview.png'
]
actuals = [
    'docs/screenshots/1_Onboarding.png',
    'docs/screenshots/2_Home.png',
    'docs/screenshots/4_SafeCleanup_Preview.png',
    'docs/screenshots/5_CandyPrep_Preview.png'
]

t_img = load(targets[0])
a_img = load(actuals[0])

tw, th = t_img.size
aw, ah = a_img.size

scale = aw / tw
new_th = int(th * scale)

comp_w = aw * 2
comp_h = new_th * 4

comp_img = Image.new('RGB', (comp_w, comp_h))

for i in range(4):
    t = load(targets[i]).resize((aw, new_th), Image.LANCZOS)
    a = load(actuals[i])
    
    # ensure a is exactly new_th high, crop from top
    if a.size[1] > new_th:
        a = a.crop((0, 0, aw, new_th))
    elif a.size[1] < new_th:
        a = a.resize((aw, new_th), Image.LANCZOS)
        
    comp_img.paste(t, (0, i * new_th))
    comp_img.paste(a, (aw, i * new_th))

comp_img.save('docs/screenshots/design_vs_actual.png')
print('Comparison created')
