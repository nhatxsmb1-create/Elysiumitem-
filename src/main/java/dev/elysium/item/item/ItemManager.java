package dev.elysium.item.item;

import dev.elysium.item.ElysiumItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class ItemManager {

    private final ElysiumItem plugin;

    private final Map<String, ElysiumItemData>   itemDataMap   = new LinkedHashMap<>();
    

    public static final String ITEM_ID_KEY = "elysium_item_id";

    public ItemManager(ElysiumItem plugin) {
        this.plugin = plugin;
        loadAll();
    }

    // ── Load Config ───────────────────────────────────────────────────────────

    private void loadAll() {
        loadFile("items.yml",    ElysiumItemData.ItemCategory.ACCESSORY);
        loadFile("armors.yml",   ElysiumItemData.ItemCategory.ARMOR);
        loadFile("trophies.yml", ElysiumItemData.ItemCategory.TROPHY);
        plugin.getLogger().info("Loaded " + itemDataMap.size() + " Elysium item(s).");
    }

    @SuppressWarnings("unchecked")
    private void loadFile(String fileName, ElysiumItemData.ItemCategory category) {
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

    public ItemStack createItem(String itemId) {
        ElysiumItemData data = itemDataMap.get(itemId);
        if (data == null) return null;

        Material mat;
        try { mat = Material.valueOf(data.getMaterial()); }
        catch (Exception e) { mat = Material.STONE; }

        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();

        meta.setDisplayName(color(data.getDisplayName()));
        meta.setCustomModelData(data.getModelData());
        meta.setUnbreakable(true);
        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE,
            org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES,
            org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS
        );

        // Lore
        List<String> lore = new ArrayList<>();
        // Type badge dep hon
        String typeBadge = buildTypeBadge(data);
        lore.add(color(typeBadge));
        lore.add("");
        data.getLore().forEach(l -> lore.add(color(l)));
        lore.add("");

        if (data.getCategory() == ElysiumItemData.ItemCategory.ACCESSORY) {
            lore.add("");
            lore.add(color("&e[Hướng dẫn] &fGõ lệnh &a/trangbi"));
            lore.add(color("&fđể gắn mảnh ghép này vào người."));
        }
        
        // Stats - format dep
        if (!data.getStats().isEmpty()) {
            lore.add(color("&8&m              "));
            for (Map.Entry<String, Object> e : data.getStats().entrySet()) {
                lore.add(color(formatStat(e.getKey(), e.getValue())));
            }
        }

        lore.add("");

        lore.add("");
        lore.add(color("&8ID: " + itemId));
        meta.setLore(lore);

        // NBT tag
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, ITEM_ID_KEY),
                org.bukkit.persistence.PersistentDataType.STRING,
                itemId
        );

        item.setItemMeta(meta);
        return item;
    }

    /** Tao item voi Mastery info cua player */
    public ItemStack createItemForPlayer(String itemId, Player player) {
        return createItem(itemId);
    }

    public void giveItem(Player player, String itemId) {
        ItemStack item = createItemForPlayer(itemId, player);
        if (item == null) { player.sendMessage(color("&cItem khong ton tai: " + itemId)); return; }
        player.getInventory().addItem(item);
        player.sendMessage(color("&aNhan duoc: " + itemDataMap.get(itemId).getDisplayName()));
    }

    // ── Detect Equipped ───────────────────────────────────────────────────────

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        var key = new org.bukkit.NamespacedKey(plugin, ITEM_ID_KEY);
        if (!pdc.has(key)) return null;
        return pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
    }

    /** Lay tat ca Elysium item dang duoc trang bi (giap + inventory) */
    public List<String> getEquippedItemIds(Player player) {
        List<String> result = new ArrayList<>();
        // Giap
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            String id = getItemId(armor);
            if (id != null) result.add(id);
        }
        // Tay chem + tay phu
        String mainId = getItemId(player.getInventory().getItemInMainHand());
        if (mainId != null) result.add(mainId);
        String offId  = getItemId(player.getInventory().getItemInOffHand());
        if (offId != null) result.add(offId);
        return result;
    }

    /** Kiem tra player co dang mac item nay khong */
    public boolean isEquipped(Player player, String itemId) {
        return getEquippedItemIds(player).contains(itemId);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public ElysiumItemData            getItemData(String id)      { return itemDataMap.get(id); }
    public Map<String, ElysiumItemData> getAllItems()              { return Collections.unmodifiableMap(itemDataMap); }
    public Set<String>                getItemIds()                { return itemDataMap.keySet(); }

    // ── Utils ─────────────────────────────────────────────────────────────────

    private String buildExpBar(long cur, long max, double pct) {
        int bars   = 12;
        int filled = (int) (pct / 100.0 * bars);
        StringBuilder bar = new StringBuilder(color("&7EXP &6["));
        for (int i = 0; i < bars; i++) bar.append(i < filled ? color("&e|") : color("&8|"));
        bar.append(color("&6] &f" + cur + "/" + max));
        return bar.toString();
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
            default                   -> "&7▸";
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
            default                   -> key.replace("-", " ");
        };
    }

    private String formatStatValue(String key, Object val) {
        if (val instanceof Boolean b) return b ? "&aYes" : "&cNo";
        if (val instanceof Double d || val instanceof Float) {
            double dv = ((Number) val).doubleValue();
            // Phan tram
            if (key.contains("chance") || key.contains("reduce") || key.contains("rate")
                    || key.contains("resist") || key.contains("speed")) {
                return String.format("&f%.0f%%", dv * 100);
            }
            return dv % 1 == 0 ? "&f" + (int) dv : String.format("&f%.2f", dv);
        }
        return "&f" + val;
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
// NOTE: refreshItemInInventory is defined in the main ItemManager class below
