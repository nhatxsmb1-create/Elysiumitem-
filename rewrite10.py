import re

with open('src/main/java/dev/elysium/item/listener/ArmorListener.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('Attribute.GENERIC_MAX_HEALTH', 'Attribute.MAX_HEALTH')

with open('src/main/java/dev/elysium/item/listener/ArmorListener.java', 'w', encoding='utf-8') as f:
    f.write(content)
