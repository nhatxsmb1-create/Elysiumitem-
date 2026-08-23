import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'\s*if \(data\.getMastery\(\) != null\) \{.*?\n\s*\}'
content = re.sub(pattern, '', content, flags=re.DOTALL)

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
