import re

with open('src/main/java/dev/elysium/item/gui/TrangBiGui.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('import java.util.Arrays;', 'import java.util.Arrays;\nimport java.util.List;\nimport java.util.ArrayList;')

with open('src/main/java/dev/elysium/item/gui/TrangBiGui.java', 'w', encoding='utf-8') as f:
    f.write(content)
