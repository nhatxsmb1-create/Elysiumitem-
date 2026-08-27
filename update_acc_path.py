import re

with open(r'src/main/java/dev/elysium/item/accessory/AccessoryManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

old_logic = '''    public AccessoryManager(ElysiumItem plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "accessories_data.yml");
        loadData();
    }'''

new_logic = '''    public AccessoryManager(ElysiumItem plugin) {
        this.plugin = plugin;
        File coreFolder = new File(plugin.getDataFolder().getParentFile(), "ElysiumCore/data");
        if (!coreFolder.exists()) {
            coreFolder.mkdirs();
        }
        this.dataFile = new File(coreFolder, "accessories_data.yml");
        loadData();
    }'''

text = text.replace(old_logic, new_logic)

with open(r'src/main/java/dev/elysium/item/accessory/AccessoryManager.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("AccessoryManager path updated to ElysiumCore/data!")