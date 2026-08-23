import re

with open('src/main/java/dev/elysium/item/command/AdminCommand.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("plugin.reloadConfig();", "plugin.reloadConfig();\n            plugin.getItemManager().loadAll();")

with open('src/main/java/dev/elysium/item/command/AdminCommand.java', 'w', encoding='utf-8') as f:
    f.write(content)
