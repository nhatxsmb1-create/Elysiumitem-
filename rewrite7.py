import re

with open('src/main/java/dev/elysium/item/ElysiumItem.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(r'import dev\.elysium\.item\.gui\.GuiListener;\s+', '', content)
content = re.sub(r'getServer\(\)\.getPluginManager\(\)\.registerEvents\(new GuiListener\(\),          this\);\s+', '', content)

with open('src/main/java/dev/elysium/item/ElysiumItem.java', 'w', encoding='utf-8') as f:
    f.write(content)
