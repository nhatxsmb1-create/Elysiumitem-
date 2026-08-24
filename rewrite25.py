import re

with open('src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'@EventHandler\s*public void onPlayerDash\(dev\.elysium\.core\.event\.ElysiumDashEvent event\) \{\s*Player player = event\.getPlayer\(\);'
replacement = '''public void onPlayerDash(Player player) {'''

content = re.sub(pattern, replacement, content)

with open('src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'w', encoding='utf-8') as f:
    f.write(content)
