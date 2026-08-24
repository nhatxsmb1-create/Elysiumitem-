package dev.elysium.item.item;

import dev.elysium.item.ElysiumItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.*;

public class ItemManager {

    private final ElysiumItem plugin;
    public static final String ITEM_ID_KEY = "elysium_item_id";

    private final Map<String, ElysiumItemData> itemDataMap = new HashMap<>();

    public ItemManager(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        itemDataMap.clear();
        loadFile("items.yml", ElysiumItemData.ItemCategory.ACCESSORY);
        loadFile("armors.yml", ElysiumItemData.ItemCategory.ARMOR);
        loadFile("trophies.yml", ElysiumItemData.ItemCategory.TROPHY);
        plugin.getLogger().info("Loaded " + itemDataMap.size() + " Elysium item(s).");
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            updatePlayerInventory(p);
        }
    }

    public void updatePlayerInventory(Player player) {
        org.bukkit.inventory.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            String id = getItemId(item);
            if (id != null && itemDataMap.containsKey(id)) {
                ItemStack updated = createItemForPlayer(id, player);
                if (updated != null) {
                    updated.setAmount(item.getAmount());
                    inv.setItem(i, updated);
                }
            }
        }
    }

    private void loadFile(String fileName, ElysiumItemData.ItemCategory category) {
        File f = new File(plugin.getDataFolder(), fileName);
        if (!f.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

        ConfigurationSection itemsSec = config.getConfigurationSection(category.name().toLowerCase() + "s");
        if (itemsSec == null) return;

        for (String id : itemsSec.getKeys(false)) {
            ConfigurationSection sec = itemsSec.getConfigurationSection(id);
            if (sec == null) continue;

            ConfigurationSection statsSec = sec.getConfigurationSection("stats");
            Map<String, Object> stats = new HashMap<>();
            if (statsSec != null) {
                for (String k : statsSec.getKeys(false)) {
                    stats.put(k, statsSec.get(k));
                }
            }

            ElysiumItemData data = new ElysiumItemData(
                id,
                sec.getString("display-name", id),
                category,
                sec.getString("type", "UNKNOWN"),
                sec.getString("material", "STONE"),
                sec.getString("color", null),
                sec.getInt("model-data", 0),
                sec.getStringList("lore"),
                stats,
                sec.getStringList("mechanics"),
                sec.getStringList("tradeoffs")
            );
            itemDataMap.put(id.toUpperCase(), data);
        }
    }

    public ElysiumItemData getItemData(String id) {
        return id == null ? null : itemDataMap.get(id.toUpperCase());
    }
    
    public Set<String> getItemIds() { return itemDataMap.keySet(); }
    public Map<String, ElysiumItemData> getAllItems() { return itemDataMap; }

    public ItemStack createItem(String itemId) {
        ElysiumItemData data = getItemData(itemId);
        if (data == null) return null;

        Material mat = Material.matchMaterial(data.getMaterial().toUpperCase());
        if (mat == null) mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(color(data.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add(color("&8« &7[&8" + buildTypeBadge(data) + "&7] &8»"));
        lore.add("");

        if (!data.getTradeoffs().isEmpty()) {
            lore.add(color("&c[!] ĐÁNH ĐỔI [!]"));
            for (String t : data.getTradeoffs()) {
                lore.add(color(" &8- &c" + formatTradeoff(t)));
            }
            lore.add("");
        }

        for (String l : data.getLore()) {
            lore.add(color(l));
        }

        lore.add("");
        lore.add(color("&a[!] Mặc vào người để kích hoạt"));
        lore.add(color("&8ID: " + data.getId().toUpperCase()));

        meta.setLore(lore);
        
        if (data.getModelData() > 0) {
            meta.setCustomModelData(data.getModelData());
        }

        if (data.getColor() != null && meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta lam) {
            try {
                java.awt.Color c = java.awt.Color.decode(data.getColor());
                lam.setColor(org.bukkit.Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
            } catch (Exception ignored) {}
        }
        
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, ITEM_ID_KEY);
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, data.getId().toUpperCase());

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createItemForPlayer(String itemId, Player player) {
        return createItem(itemId);
    }

    public void giveItem(Player player, String itemId) {
        ItemStack item = createItemForPlayer(itemId, player);
        if (item == null) { player.sendMessage(color("&cItem khong ton tai: " + itemId)); return; }
        player.getInventory().addItem(item);
        player.sendMessage(color("&aNhan duoc: " + itemDataMap.get(itemId).getDisplayName()));
    }

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, ITEM_ID_KEY);
        if (pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        return null;
    }

    public List<String> getEquippedItemIds(Player player) {
        List<String> list = new ArrayList<>();
        if (player.getInventory().getHelmet() != null) {
            String id = getItemId(player.getInventory().getHelmet());
            if (id != null) list.add(id);
        }
        if (player.getInventory().getChestplate() != null) {
            String id = getItemId(player.getInventory().getChestplate());
            if (id != null) list.add(id);
        }
        if (player.getInventory().getLeggings() != null) {
            String id = getItemId(player.getInventory().getLeggings());
            if (id != null) list.add(id);
        }
        if (player.getInventory().getBoots() != null) {
            String id = getItemId(player.getInventory().getBoots());
            if (id != null) list.add(id);
        }

        var slotData = plugin.getAccessoryManager().getSlotData(player.getUniqueId());
        if (slotData.getNecklaceId() != null) list.add(slotData.getNecklaceId());
        if (slotData.getRing1Id() != null) list.add(slotData.getRing1Id());
        if (slotData.getRing2Id() != null) list.add(slotData.getRing2Id());
        
        return list;
    }

    private String buildTypeBadge(ElysiumItemData data) {
        String icon = switch (data.getSubType()) {
            case "RING"       -> "💍 &7Nhẫn";
            case "NECKLACE"   -> "📿 &7Dây Chuyền";
            case "HELMET"     -> "🛡 Giáp Đầu";
            case "CHESTPLATE" -> "🛡 Giáp Ngực";
            case "LEGGINGS"   -> "🛡 Giáp Chân";
            case "BOOTS"      -> "🛡 Giày";
            case "TROPHY"     -> "🏆 Cúp Kỷ Niệm";
            default           -> "🔹 Kỹ Năng";
        };
        return icon;
    }

    private String formatTradeoff(String tradeoff) {
        if (tradeoff.startsWith("HEAL_REDUCTION:")) {
            return "Giảm " + tradeoff.split(":")[1] + "% Khả năng hồi máu";
        }
        if (tradeoff.startsWith("INCOMING_DAMAGE_INCREASE:")) {
            return "Nhận thêm " + tradeoff.split(":")[1] + "% Sát thương";
        }
        return tradeoff;
    }

    private String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
