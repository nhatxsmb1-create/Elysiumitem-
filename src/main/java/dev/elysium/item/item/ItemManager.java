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
    private final Map<String, ElysiumItemData> itemDataMap = new HashMap<>();

    public static final String ITEM_ID_KEY = "elysium_item_id";

    public ItemManager(ElysiumItem plugin) {
        this.plugin = plugin;
        loadAll();
    }

    public void loadAll() {
        itemDataMap.clear();
        loadFile("items.yml",    ElysiumItemData.ItemCategory.ACCESSORY);
        loadFile("armors.yml",   ElysiumItemData.ItemCategory.ARMOR);
        loadFile("trophies.yml", ElysiumItemData.ItemCategory.TROPHY);
        plugin.getLogger().info("Loaded " + itemDataMap.size() + " Elysium item(s).");
    }

    private void loadFile(String fileName, ElysiumItemData.ItemCategory category) {
        File f = new File(plugin.getDataFolder(), fileName);
        if (!f.exists()) plugin.saveResource(fileName, false);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);

        String rootKey = switch (category) {
            case ACCESSORY -> "accessories";
            case ARMOR     -> "armors";
            case TROPHY    -> "trophies";
        };

        ConfigurationSection root = cfg.getConfigurationSection(rootKey);
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
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

            itemDataMap.put(id, data);
        }
    }

    public ElysiumItemData getItemData(String id) { return itemDataMap.get(id); }
    public Map<String, ElysiumItemData> getAllItems() { return Collections.unmodifiableMap(itemDataMap); }
    public Set<String> getItemIds() { return itemDataMap.keySet(); }

    public ItemStack createItem(String itemId) {
        ElysiumItemData data = itemDataMap.get(itemId);
        if (data == null) return null;

        Material mat;
        try { mat = Material.valueOf(data.getMaterial()); }
        catch (Exception e) { mat = Material.STONE; }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(color(data.getDisplayName()));

        if (data.getModelData() > 0) meta.setCustomModelData(data.getModelData());

        if (meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta lam && data.getColor() != null) {
            try {
                java.awt.Color c = java.awt.Color.decode(data.getColor());
                lam.setColor(org.bukkit.Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
            } catch (Exception ignored) {}
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);

        List<String> lore = new ArrayList<>();
        String typeBadge = buildTypeBadge(data);
        lore.add(color("&f&l« " + typeBadge + "&f&l »"));
        lore.add("");

        if (!data.getStats().isEmpty()) {
            lore.add(color("&e&l✦ &6&lCHỈ SỐ TĂNG THÊM &e&l✦"));
            for (Map.Entry<String, Object> e : data.getStats().entrySet()) {
                lore.add(color("  &8▪ " + formatStat(e.getKey(), e.getValue())));
            }
            lore.add("");
        }

        if (!data.getTradeoffs().isEmpty()) {
            lore.add(color("&4&l[!] &c&lĐÁNH ĐỔI &4&l[!]"));
            for (String t : data.getTradeoffs()) {
                lore.add(color("  &8▪ &c" + translateTradeoff(t)));
            }
            lore.add("");
        }

        data.getLore().forEach(l -> lore.add(color(l)));
        lore.add("");

        if (data.getCategory() == ElysiumItemData.ItemCategory.ACCESSORY) {
            lore.add(color("&8&m                                  "));
            lore.add(color("&a&l[!] &aGõ lệnh &f/trangbi &ađể sử dụng"));
            lore.add(color("&8&m                                  "));
        } else if (data.getCategory() == ElysiumItemData.ItemCategory.ARMOR) {
            lore.add(color("&8&m                                  "));
            lore.add(color("&a&l[!] &aMặc vào người để kích hoạt"));
            lore.add(color("&8&m                                  "));
        }
        
        lore.add(color("&8ID: " + itemId));
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, ITEM_ID_KEY),
                org.bukkit.persistence.PersistentDataType.STRING,
                itemId
        );

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
            case "CHARM"      -> "🍀 &7Bùa";
            case "HELMET"     -> "⛑ &7Mũ Giáp";
            case "CHESTPLATE" -> "🛡 &7Giáp Ngực";
            case "LEGGINGS"   -> "🩲 &7Quần Giáp";
            case "BOOTS"      -> "👟 &7Giày Giáp";
            case "TROPHY"     -> "🏆 &7Cúp Đặc Biệt";
            default           -> "&7" + data.getSubType();
        };
        return "&8[" + icon + "&8]";
    }

    private String formatStat(String key, Object val) {
        String icon  = getStatIcon(key);
        String name  = getStatDisplayName(key);
        String value = formatStatValue(key, val);
        return icon + " &7" + name + ": &f" + value;
    }

    private String getStatIcon(String key) {
        return switch (key) {
            case "bonus-hp"           -> "&c❤";
            case "bonus-defense"      -> "&7🛡";
            case "bonus-mana"         -> "&b✦";
            case "bonus-mana-regen"   -> "&b⟳";
            case "bonus-speed"        -> "&e⚡";
            case "bonus-jump"         -> "&a↑";
            case "bonus-drop-rate"    -> "&6◈";
            case "bonus-exp"          -> "&a★";
            case "bonus-crit-chance"  -> "&c⚔";
            case "bonus-crit-damage"  -> "&c💥";
            case "damage-reduce"      -> "&a🛡";
            case "knockback-resist"   -> "&7↩";
            case "dodge-chance"       -> "&5◎";
            case "mining-speed"       -> "&6⛏";
            case "fall-damage-reduce" -> "&aV";
            case "fire-resist"        -> "&c🔥";
            default                   -> "&7▪";
        };
    }

    private String getStatDisplayName(String key) {
        return switch (key) {
            case "bonus-hp"           -> "HP";
            case "bonus-defense"      -> "Phòng Thủ";
            case "bonus-mana"         -> "Mana";
            case "bonus-mana-regen"   -> "Mana Regen";
            case "bonus-speed"        -> "Tốc Độ";
            case "bonus-jump"         -> "Nhảy";
            case "bonus-drop-rate"    -> "Drop Rate";
            case "bonus-exp"          -> "EXP Bonus";
            case "bonus-crit-chance"  -> "Crit Chance";
            case "bonus-crit-damage"  -> "Crit Damage";
            case "damage-reduce"      -> "Giảm Sát Thương";
            case "knockback-resist"   -> "Kháng Knockback";
            case "dodge-chance"       -> "Tỉ Lệ Né";
            case "mining-speed"       -> "Tốc Độ Đào";
            case "fall-damage-reduce" -> "Giảm Dame Rơi";
            case "fire-resist"        -> "Kháng Lửa";
            default                   -> key;
        };
    }

    private String formatStatValue(String key, Object val) {
        if (key.contains("rate") || key.contains("chance") || key.contains("reduce") || key.contains("resist")) {
            return "&a+" + val + "%";
        }
        if (val instanceof Number n) {
            double dv = n.doubleValue();
            if (dv > 0) return "&a+" + (dv % 1 == 0 ? (int)dv : String.format("%.2f", dv));
            return dv % 1 == 0 ? "&f" + (int) dv : String.format("&f%.2f", dv);
        }
        return "&f" + val;
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }

    private String translateTradeoff(String t) {
        if (t.startsWith("HEAL_REDUCTION")) {
            String[] parts = t.split(":");
            return "Giảm " + (parts.length > 1 ? parts[1] : "0") + "% Khả năng hồi máu";
        }
        if (t.startsWith("INCOMING_DAMAGE_INCREASE")) {
            String[] parts = t.split(":");
            return "Nhận thêm " + (parts.length > 1 ? parts[1] : "0") + "% Sát thương từ kẻ địch";
        }
        return t;
    }
}