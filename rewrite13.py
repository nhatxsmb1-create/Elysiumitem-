import re

with open('src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('org.bukkit.Particle.EXPLOSION_LARGE', 'org.bukkit.Particle.EXPLOSION')

with open('src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'w', encoding='utf-8') as f:
    f.write(content)
