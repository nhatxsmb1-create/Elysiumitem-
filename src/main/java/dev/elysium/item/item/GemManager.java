package dev.elysium.item.item;

import dev.elysium.item.ElysiumItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GemManager {
    private final ElysiumItem plugin;
    private final Map<String, GemData> gems = new HashMap<>();

    public GemManager(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    public void loadGems() {
        gems.clear();
        File file = new File(plugin.getDataFolder(), "gems.yml");
        if (!file.exists()) {
            plugin.saveResource("gems.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("gems");
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec != null) {
                GemData data = new GemData(
                    id.toUpperCase(),
                    sec.getString("display-name", "&aNgọc Đột Biến"),
                    sec.getStringList("lore"),
                    sec.getStringList("mechanics")
                );
                gems.put(id.toUpperCase(), data);
            }
        }
        plugin.getLogger().info("Loaded " + gems.size() + " gems.");
    }

    public GemData getGem(String id) {
        return id == null ? null : gems.get(id.toUpperCase());
    }
}