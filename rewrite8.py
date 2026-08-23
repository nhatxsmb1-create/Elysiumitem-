import re

with open('src/main/java/dev/elysium/item/gui/AdminGui.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('build(viewer);', 'buttons.clear(); inventory.clear(); build(viewer);')

with open('src/main/java/dev/elysium/item/gui/AdminGui.java', 'w', encoding='utf-8') as f:
    f.write(content)
