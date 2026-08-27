package dev.elysium.item.item;

import dev.elysium.item.ElysiumItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GemManager {
    private final ElysiumItem plugin;
    private final Map<String, GemData> gems = new HashMap<>();

    public static final String GEM_ID_KEY = "elysium_gem_id";

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

    public Map<String, GemData> getAllGems() { return gems; }

    public GemData getGem(String id) {
        return id == null ? null : gems.get(id.toUpperCase());
    }

    public String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    public ItemStack createGemItem(String gemId) {
        GemData data = getGem(gemId);
        if (data == null) return null;

        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(data.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add(color("&8« &7[&dNgọc Đột Biến&7] &8»"));
        lore.add("");
        for (String l : data.getLore()) {
            lore.add(color(l));
        }
        lore.add("");
        lore.add(color("&e[!] Dùng tại Lò Rèn để khảm vào trang bị"));
        
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, GEM_ID_KEY), PersistentDataType.STRING, gemId.toUpperCase());
        item.setItemMeta(meta);

        return item;
    }

    public String getGemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, GEM_ID_KEY);
        if (pdc.has(key, PersistentDataType.STRING)) {
            return pdc.get(key, PersistentDataType.STRING);
        }
        return null;
    }
}