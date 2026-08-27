import re

with open(r'src/main/java/dev/elysium/item/item/GemManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('public GemData getGem(String id) {', 'public Map<String, GemData> getAllGems() { return gems; }\n\n    public GemData getGem(String id) {')

with open(r'src/main/java/dev/elysium/item/item/GemManager.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("GemManager.java updated!")