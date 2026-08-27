import re

with open(r'src/main/resources/trophies.yml', 'r', encoding='utf-8') as f:
    text = f.read()

# Remove mastery blocks
# A mastery block starts with '    mastery:' and goes until the next property at the same indentation (or end of file/next item)
text = re.sub(r'\n    mastery:.*?(?=\n  [A-Z_]+:|\Z)', '', text, flags=re.DOTALL)

with open(r'src/main/resources/trophies.yml', 'w', encoding='utf-8') as f:
    f.write(text)

print("Trophies cleaned!")