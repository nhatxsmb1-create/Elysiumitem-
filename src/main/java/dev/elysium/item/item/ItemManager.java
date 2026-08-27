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
                    // Copy socket
                    String gemId = getSocket(item, 1);
                    if (gemId != null) {
                        setSocket(updated, 1, gemId);
                    }
                    // Rebuild lore based on socket
                    rebuildLore(updated);
                    inv.setItem(i, updated);
                }
            }
        }
    }

    private void loadFile(String fileName, ElysiumItemData.ItemCategory category) {
        File f = new File(plugin.getDataFolder(), fileName);
        if (!f.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

                String rootKey = switch (category) {
            case ACCESSORY -> "accessories";
            case ARMOR     -> "armors";
            case TROPHY    -> "trophies";
        };
        ConfigurationSection itemsSec = config.getConfigurationSection(rootKey);
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

    
    public void rebuildLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String itemId = getItemId(item);
        if (itemId == null) return;
        ElysiumItemData data = getItemData(itemId);
        if (data == null) return;

        var meta = item.getItemMeta();
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
        
        String gemId = getSocket(item, 1);
        if (gemId != null) {
            GemData gem = plugin.getGemManager().getGem(gemId);
            if (gem != null) {
                lore.add("");
                lore.add(color("&8=====[ &6LỖ KHẢM &8]====="));
                lore.add(color("&7Đang khảm: " + gem.getDisplayName()));
                for (String gl : gem.getLore()) {
                    lore.add(color(gl));
                }
                lore.add(color("&8====================="));
            }
        }

        lore.add("");
        lore.add(color("&a[!] Dùng lệnh &f/trangbi &ađể mặc vào người"));
        lore.add(color("&8ID: " + data.getId().toUpperCase()));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

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
        lore.add(color("&a[!] Dùng lệnh &f/trangbi &ađể mặc vào người"));
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

    
    public String getSocket(ItemStack item, int slot) {
        if (item == null || !item.hasItemMeta()) return null;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, slot == 1 ? SOCKET_1_KEY : SOCKET_1_KEY);
        if (pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        return null;
    }

    public void setSocket(ItemStack item, int slot, String gemId) {
        if (item == null || !item.hasItemMeta()) return;
        var meta = item.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, slot == 1 ? SOCKET_1_KEY : SOCKET_1_KEY);
        if (gemId == null) {
            pdc.remove(key);
        } else {
            pdc.set(key, org.bukkit.persistence.PersistentDataType.STRING, gemId.toUpperCase());
        }
        item.setItemMeta(meta);
    }

    public List<org.bukkit.inventory.ItemStack> getEquippedItems(Player player) {
        List<org.bukkit.inventory.ItemStack> list = new ArrayList<>();
        if (player.getInventory().getHelmet() != null) list.add(player.getInventory().getHelmet());
        if (player.getInventory().getChestplate() != null) list.add(player.getInventory().getChestplate());
        if (player.getInventory().getLeggings() != null) list.add(player.getInventory().getLeggings());
        if (player.getInventory().getBoots() != null) list.add(player.getInventory().getBoots());
        
        var slotData = plugin.getAccessoryManager().getSlotData(player.getUniqueId());
        if (slotData.getNecklace() != null) list.add(slotData.getNecklace());
        if (slotData.getRing1() != null) list.add(slotData.getRing1());
        if (slotData.getRing2() != null) list.add(slotData.getRing2());
        
        return list;
    }

    public List<String> getEquippedItemIds(Player player) {
        List<String> ids = new ArrayList<>();
        for (org.bukkit.inventory.ItemStack item : getEquippedItems(player)) {
            String id = getItemId(item);
            if (id != null) ids.add(id);
        }
        return ids;
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
