import re

with open('src/main/java/dev/elysium/item/mechanic/MarkManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('Particle.REDSTONE', 'Particle.DUST')

with open('src/main/java/dev/elysium/item/mechanic/MarkManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
