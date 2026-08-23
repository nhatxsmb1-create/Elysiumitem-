import re

with open('src/main/java/dev/elysium/item/ElysiumItem.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Add import
content = content.replace('import dev.elysium.item.mechanic.MechanicEngine;', 'import dev.elysium.item.mechanic.MechanicEngine;\nimport dev.elysium.item.mechanic.MarkManager;')

# Add field
content = content.replace('private MechanicEngine  mechanicEngine;', 'private MechanicEngine  mechanicEngine;\n    private MarkManager     markManager;')

# Initialize
content = content.replace('mechanicEngine  = new MechanicEngine(this);', 'mechanicEngine  = new MechanicEngine(this);\n        markManager     = new MarkManager(this);')

# Getter
getter_str = """    public MechanicEngine getMechanicEngine() {
        return mechanicEngine;
    }

    public MarkManager getMarkManager() {
        return markManager;
    }"""
content = content.replace('    public MechanicEngine getMechanicEngine() {\n        return mechanicEngine;\n    }', getter_str)

with open('src/main/java/dev/elysium/item/ElysiumItem.java', 'w', encoding='utf-8') as f:
    f.write(content)
