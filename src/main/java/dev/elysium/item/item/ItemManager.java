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
    private final Map<UUID, PlayerItemState>      playerStates  = new HashMap<>();

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
        File f = new File(plugin.getDataFolder(), fileName);
        if (!f.exists()) plugin.saveResource(fileName, false);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);

        // Root key: accessories / armors / trophies
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

            // Stats
            ConfigurationSection statsSec = sec.getConfigurationSection("stats");
            Map<String, Object> stats = statsSec != null ? new HashMap<>(statsSec.getValues(false)) : new HashMap<>();

            // Mastery
            ConfigurationSection mSec = sec.getConfigurationSection("mastery");
            ElysiumItemData.MasteryConfig mastery = null;
            if (mSec != null) {
                // Ore EXP (trophy only)
                Map<String, Integer> oreExp = new HashMap<>();
                ConfigurationSection oreSec = mSec.getConfigurationSection("ore-exp");
                if (oreSec != null) {
                    for (String mat : oreSec.getKeys(false)) {
                        oreExp.put(mat.toUpperCase(), oreSec.getInt(mat));
                    }
                }

                // Bonuses
                Map<Integer, ElysiumItemData.BonusUnlock> bonuses = new LinkedHashMap<>();
                ConfigurationSection bonusSec = mSec.getConfigurationSection("bonuses");
                if (bonusSec != null) {
                    for (String lvStr : bonusSec.getKeys(false)) {
                        try {
                            int lv = Integer.parseInt(lvStr);
                            ConfigurationSection bSec = bonusSec.getConfigurationSection(lvStr);
                            if (bSec == null) continue;
                            String desc = bSec.getString("description", "");
                            Map<String, Object> vals = new HashMap<>(bSec.getValues(false));
                            vals.remove("description");
                            bonuses.put(lv, new ElysiumItemData.BonusUnlock(desc, vals));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                mastery = new ElysiumItemData.MasteryConfig(
                        mSec.getInt("max-level", 30),
                        mSec.getInt("exp-per-hit", 3),
                        oreExp, bonuses
                );
            }

            itemDataMap.put(id, new ElysiumItemData(
                    id,
                    sec.getString("display-name", id),
                    category,
                    sec.getString("type", "UNKNOWN"),
                    sec.getString("material", "STONE"),
                    sec.getInt("model-data", 0),
                    sec.getStringList("lore"),
                    stats,
                    mastery
            ));
        }
    }

    // ── Create Item ───────────────────────────────────────────────────────────

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

        // Stats - format dep
        if (!data.getStats().isEmpty()) {
            lore.add(color("&8&m              "));
            for (Map.Entry<String, Object> e : data.getStats().entrySet()) {
                lore.add(color(formatStat(e.getKey(), e.getValue())));
            }
        }

        lore.add("");
        if (data.getMastery() != null) {
            lore.add(color("&8── Mastery ──"));
            lore.add(color("&6Mastery Level: &e0/" + data.getMastery().getMaxLevel()));
            lore.add(color("&7EXP: &f0/100"));
        }

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
        ItemStack item = createItem(itemId);
        if (item == null || player == null) return item;

        ElysiumItemData data = itemDataMap.get(itemId);
        if (data == null || data.getMastery() == null) return item;

        try {
            PlayerItemState state = getState(player);
            int    level   = plugin.getItemMastery().getLevelFromExp(data, state.getExp(itemId));
            long   totalExp= state.getExp(itemId);
            long   expNext = plugin.getItemMastery().getExpForNextLevel(data, level);
            long   expCur  = totalExp - plugin.getItemMastery().getExpForLevel(data, level);
            double pct     = expNext > 0 ? (expCur / (double) expNext) * 100.0 : 100.0;

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            // Xoa dong mastery cu
            lore.removeIf(l -> l.contains("Mastery Level:") || l.contains("EXP:") || l.contains("ID:") || l.contains("──"));

            lore.add(color("&8──────────────"));
            lore.add(color("&6✦ Mastery: &e" + level + "&7/" + data.getMastery().getMaxLevel()));
            lore.add(buildExpBar(expCur, expNext, pct));

            // Hien bonus da unlock
            for (Map.Entry<Integer, ElysiumItemData.BonusUnlock> entry : data.getMastery().getBonuses().entrySet()) {
                if (level >= entry.getKey()) {
                    lore.add(color("&a▸ " + entry.getValue().getDescription()));
                }
            }

            // Next unlock
            String next = plugin.getItemMastery().getNextUnlockDesc(data, level);
            if (next != null) lore.add(color("&7Next: " + next));

            lore.add(color("&8ID: " + itemId));
            meta.setLore(lore);
            item.setItemMeta(meta);
        } catch (Exception ignored) {}

        return item;
    }

    // ── Give ─────────────────────────────────────────────────────────────────

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

    // ── Player State ──────────────────────────────────────────────────────────

    public PlayerItemState getState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), PlayerItemState::new);
    }

    public void removeState(UUID uuid) { playerStates.remove(uuid); }

    /** Cap nhat lore tren item trong inventory sau khi EXP thay doi */
    public void refreshItemInInventory(Player player, String itemId) {
        // Main hand
        ItemStack held = player.getInventory().getItemInMainHand();
        if (itemId.equals(getItemId(held))) {
            player.getInventory().setItemInMainHand(createItemForPlayer(itemId, player));
            return;
        }
        // Giap
        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean changed = false;
        for (int i = 0; i < armor.length; i++) {
            if (itemId.equals(getItemId(armor[i]))) {
                armor[i] = createItemForPlayer(itemId, player);
                changed = true;
            }
        }
        if (changed) { player.getInventory().setArmorContents(armor); return; }
        // Off hand
        if (itemId.equals(getItemId(player.getInventory().getItemInOffHand()))) {
            player.getInventory().setItemInOffHand(createItemForPlayer(itemId, player));
        }
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
