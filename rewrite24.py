import re

with open('src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'public void onPlayerDash\(Player player\) \{'
replacement = '''@EventHandler
    public void onPlayerDash(dev.elysium.core.event.ElysiumDashEvent event) {
        Player player = event.getPlayer();'''

content = re.sub(pattern, replacement, content)

with open('src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'w', encoding='utf-8') as f:
    f.write(content)
