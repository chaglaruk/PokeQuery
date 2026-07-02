import os
import re

res_dir = 'C:/Users/Caglar/Desktop/PokeQuery/app/src/main/res'
for root, dirs, files in os.walk(res_dir):
    if 'values' in root and 'strings.xml' in files:
        filepath = os.path.join(root, 'strings.xml')
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # We need to escape apostrophes that are not already escaped.
        # But wait, <?xml version='1.0' encoding='utf-8'?> has single quotes!
        # So we only want to escape single quotes inside text content.
        # It's safer to just replace any (?<!\\)' with \', EXCEPT in the xml declaration.
        # A simpler way is to replace all ' with \', then fix the xml declaration.
        
        content = re.sub(r"(?<!\\)'", r"\'", content)
        # Fix the xml declaration
        content = content.replace("<?xml version=\\'1.0\\' encoding=\\'utf-8\\'?>", "<?xml version='1.0' encoding='utf-8'?>")
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {filepath}")
