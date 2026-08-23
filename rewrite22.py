import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'private void loadAll\(\) \{'
replacement = '''public void loadAll() {
        itemDataMap.clear();'''

content = re.sub(pattern, replacement, content)

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
