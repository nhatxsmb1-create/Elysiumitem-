import re

with open('src/main/java/dev/elysium/item/ElysiumItem.java', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'getServer\(\)\.getPluginManager\(\)\.registerEvents\(new GuiListener\(\),          this\);'
replacement = """getServer().getPluginManager().registerEvents(new GuiListener(),          this);
        getServer().getPluginManager().registerEvents(new dev.elysium.item.mechanic.MechanicListener(this), this);"""
content = re.sub(pattern, replacement, content)

with open('src/main/java/dev/elysium/item/ElysiumItem.java', 'w', encoding='utf-8') as f:
    f.write(content)
