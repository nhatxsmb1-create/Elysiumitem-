import re

with open('src/main/java/dev/elysium/item/ElysiumItem.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'import dev.elysium.item.mechanic.MechanicEngine;',
    'import dev.elysium.item.mechanic.MechanicEngine;\nimport dev.elysium.item.mechanic.MarkManager;'
)

content = content.replace(
    'private MechanicEngine mechanicEngine;',
    'private MechanicEngine mechanicEngine;\n    private MarkManager markManager;'
)

content = content.replace(
    'mechanicEngine = new MechanicEngine(this);',
    'mechanicEngine = new MechanicEngine(this);\n        markManager = new MarkManager(this);'
)

content = content.replace(
    'public MechanicEngine     getMechanicEngine() { return mechanicEngine; }',
    'public MechanicEngine     getMechanicEngine() { return mechanicEngine; }\n    public MarkManager        getMarkManager()    { return markManager; }'
)

with open('src/main/java/dev/elysium/item/ElysiumItem.java', 'w', encoding='utf-8') as f:
    f.write(content)
