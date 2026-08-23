import re

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace Mastery references in loadFile
load_file_pattern = r'private void loadFile\(.*?\)\s*\{.*?(?=public ItemStack createItem)public ItemStack createItem'
load_file_replacement = """private void loadFile(String fileName, ElysiumItemData.ItemCategory category) {
        java.io.File f = new java.io.File(plugin.getDataFolder(), fileName);
        if (!f.exists()) plugin.saveResource(fileName, false);
        org.bukkit.configuration.file.YamlConfiguration cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(f);

        String rootKey = switch (category) {
            case ACCESSORY -> "accessories";
            case ARMOR     -> "armors";
            case TROPHY    -> "trophies";
        };

        org.bukkit.configuration.ConfigurationSection root = cfg.getConfigurationSection(rootKey);
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;

            org.bukkit.configuration.ConfigurationSection statsSec = sec.getConfigurationSection("stats");
            java.util.Map<String, Object> stats = statsSec != null ? new java.util.HashMap<>(statsSec.getValues(false)) : new java.util.HashMap<>();

            java.util.List<String> mechanics = sec.getStringList("mechanics");
            java.util.List<String> tradeoffs = sec.getStringList("tradeoffs");

            ElysiumItemData data = new ElysiumItemData(
                    id,
                    sec.getString("display-name", id),
                    category,
                    sec.getString("type", ""),
                    sec.getString("material", "STONE"),
                    sec.getString("color", ""),
                    sec.getInt("model-data", 0),
                    sec.getStringList("lore"),
                    stats,
                    mechanics,
                    tradeoffs
            );
            itemDataMap.put(id, data);
        }
    }

    public ItemStack createItem"""

content = re.sub(load_file_pattern, load_file_replacement, content, flags=re.DOTALL)

with open('src/main/java/dev/elysium/item/item/ItemManager.java', 'w', encoding='utf-8') as f:
    f.write(content)
