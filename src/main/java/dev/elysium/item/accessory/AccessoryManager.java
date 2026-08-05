package dev.elysium.item.accessory;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import dev.elysium.item.item.PlayerItemState;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccessoryManager {

    private final ElysiumItem plugin;

    // UUID -> AccessorySlotData
    private final Map<UUID, AccessorySlotData> playerSlots = new HashMap<>();

    // Namespace key cho attribute modifier
    private static final String MOD_PREFIX = "elysium_item_";

    public AccessoryManager(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    // ── Equip / Unequip ───────────────────────────────────────────────────────

    /**
     * Trang bi accessory vao slot.
     * Neu slot da co item thi tra item cu ve inventory truoc.
     */
    public boolean equip(Player player, String itemId) {
        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null || data.getCategory() != ElysiumItemData.ItemCategory.ACCESSORY) return false;

        AccessorySlotData slots = getSlots(player);
        AccessorySlotData.SlotType slotType = getSlotType(data.getSubType());
        if (slotType == null) return false;

        // Neu slot da co item -> thao ra truoc
        String currentId = slots.getEquipped(slotType);
        if (currentId != null) {
            unequip(player, slotType, true);
        }

        // Kiem tra item trong tay
        org.bukkit.inventory.ItemStack held = player.getInventory().getItemInMainHand();
        String heldId = plugin.getItemManager().getItemId(held);
        if (!itemId.equals(heldId)) {
            player.sendMessage(color("&cBan can cam &e" + data.getDisplayName() + " &ctren tay truoc!"));
            return false;
        }

        // Lay item khoi tay
        player.getInventory().setItemInMainHand(null);

        // Equip
        slots.equip(slotType, itemId);

        // Apply stats
        applyStats(player, itemId, true);

        player.sendMessage(color("&a✔ Trang bi: " + data.getDisplayName()));
        player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1.2f);

        // Save slot data
        saveSlots(player);
        return true;
    }

    /**
     * Thao accessory ra khoi slot.
     * returnToInventory: tra item ve inventory khong.
     */
    public boolean unequip(Player player, AccessorySlotData.SlotType slotType, boolean returnToInventory) {
        AccessorySlotData slots = getSlots(player);
        String itemId = slots.getEquipped(slotType);
        if (itemId == null) return false;

        // Remove stats
        applyStats(player, itemId, false);

        slots.unequip(slotType);

        // Tra item ve inventory
        if (returnToInventory) {
            org.bukkit.inventory.ItemStack item = plugin.getItemManager().createItemForPlayer(itemId, player);
            player.getInventory().addItem(item);
        }

        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data != null) {
            player.sendMessage(color("&c✖ Thao ra: " + data.getDisplayName()));
        }

        saveSlots(player);
        return true;
    }

    // ── Apply Stats ───────────────────────────────────────────────────────────

    /**
     * Apply hoac remove stats cua item len player.
     * apply = true: them vao. apply = false: xoa di.
     */
    public void applyStats(Player player, String itemId, boolean apply) {
        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null) return;

        PlayerItemState state = plugin.getItemManager().getState(player);
        int level = plugin.getItemMastery().getLevelFromExp(data, state.getExp(itemId));

        // Base stats + mastery bonus
        double bonusHp      = getScaledStat(data, level, "bonus-hp");
        double bonusDefense = getScaledStat(data, level, "bonus-defense");
        double bonusMana    = getScaledStat(data, level, "bonus-mana");
        double bonusSpeed   = getScaledStat(data, level, "bonus-speed");

        if (apply) {
            // HP
            if (bonusHp > 0) addModifier(player, Attribute.MAX_HEALTH,
                    MOD_PREFIX + itemId + "_hp", bonusHp);
            // Defense (armor)
            if (bonusDefense > 0) addModifier(player, Attribute.ARMOR,
                    MOD_PREFIX + itemId + "_def", bonusDefense);
            // Speed
            if (bonusSpeed > 0) addModifier(player, Attribute.MOVEMENT_SPEED,
                    MOD_PREFIX + itemId + "_spd", bonusSpeed);
            // Mana (qua CoreAPI)
            if (bonusMana > 0) {
                try { dev.elysium.core.api.CoreAPI.getPlayer(player).setMaxMana(dev.elysium.core.api.CoreAPI.getPlayer(player).getMaxMana() + (int) bonusMana); }
                catch (Exception ignored) {}
            }
        } else {
            removeModifier(player, Attribute.MAX_HEALTH, MOD_PREFIX + itemId + "_hp");
            removeModifier(player, Attribute.ARMOR,       MOD_PREFIX + itemId + "_def");
            removeModifier(player, Attribute.MOVEMENT_SPEED, MOD_PREFIX + itemId + "_spd");
            if (bonusMana > 0) {
                try { dev.elysium.core.api.CoreAPI.getPlayer(player).setMaxMana(Math.max(100, dev.elysium.core.api.CoreAPI.getPlayer(player).getMaxMana() - (int) bonusMana)); }
                catch (Exception ignored) {}
            }
        }
    }

    /** Lay stat co scale theo mastery level */
    private double getScaledStat(ElysiumItemData data, int level, String statKey) {
        double base = data.getStatDouble(statKey);

        // Cong them bonus tu mastery unlock
        double masteryBonus = 0;
        if (data.getMastery() != null) {
            for (Map.Entry<Integer, ElysiumItemData.BonusUnlock> entry : data.getMastery().getBonuses().entrySet()) {
                if (level >= entry.getKey()) {
                    masteryBonus += entry.getValue().getDouble(statKey);
                }
            }
        }
        return base + masteryBonus;
    }

    // ── Re-apply (khi len mastery) ────────────────────────────────────────────

    /** Goi khi player len mastery de cap nhat stats */
    public void reapplyStats(Player player) {
        AccessorySlotData slots = getSlots(player);
        for (AccessorySlotData.SlotType slot : AccessorySlotData.SlotType.values()) {
            String itemId = slots.getEquipped(slot);
            if (itemId == null) continue;
            // Remove cu -> apply moi
            applyStats(player, itemId, false);
            applyStats(player, itemId, true);
        }
    }

    /** Goi khi player join de restore stats */
    public void restoreStats(Player player) {
        AccessorySlotData slots = getSlots(player);
        for (AccessorySlotData.SlotType slot : AccessorySlotData.SlotType.values()) {
            String itemId = slots.getEquipped(slot);
            if (itemId != null) applyStats(player, itemId, true);
        }
    }

    /** Goi khi player quit de remove stats */
    public void removeAllStats(Player player) {
        AccessorySlotData slots = getSlots(player);
        for (AccessorySlotData.SlotType slot : AccessorySlotData.SlotType.values()) {
            String itemId = slots.getEquipped(slot);
            if (itemId != null) applyStats(player, itemId, false);
        }
    }

    // ── Attribute Utils ───────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void addModifier(Player player, Attribute attribute, String key, double value) {
        var attr = player.getAttribute(attribute);
        if (attr == null) return;
        // Xoa modifier cu neu co
        removeModifier(player, attribute, key);
        attr.addModifier(new AttributeModifier(
                new org.bukkit.NamespacedKey(plugin, key),
                value,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ANY
        ));
    }

    private void removeModifier(Player player, Attribute attribute, String key) {
        var attr = player.getAttribute(attribute);
        if (attr == null) return;
        var nsKey = new org.bukkit.NamespacedKey(plugin, key);
        attr.getModifiers().stream()
                .filter(m -> m.getKey().equals(nsKey))
                .findFirst()
                .ifPresent(attr::removeModifier);
    }

    // ── Armor Stats Apply ─────────────────────────────────────────────────────

    /** Apply stats cho giap khi player mac/thao */
    public void applyArmorStats(Player player, String itemId, boolean apply) {
        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null || data.getCategory() != ElysiumItemData.ItemCategory.ARMOR) return;

        PlayerItemState state = plugin.getItemManager().getState(player);
        int level = plugin.getItemMastery().getLevelFromExp(data, state.getExp(itemId));

        double bonusHp      = getScaledStat(data, level, "bonus-hp");
        double bonusDefense = getScaledStat(data, level, "bonus-defense");
        double bonusMana    = getScaledStat(data, level, "bonus-mana");
        double bonusSpeed   = getScaledStat(data, level, "bonus-speed");
        double kbResist     = getScaledStat(data, level, "knockback-resist");

        if (apply) {
            if (bonusHp > 0)      addModifier(player, Attribute.MAX_HEALTH,      MOD_PREFIX + itemId + "_hp",  bonusHp);
            if (bonusDefense > 0) addModifier(player, Attribute.ARMOR,            MOD_PREFIX + itemId + "_def", bonusDefense);
            if (bonusSpeed > 0)   addModifier(player, Attribute.MOVEMENT_SPEED,   MOD_PREFIX + itemId + "_spd", bonusSpeed);
            if (kbResist > 0)     addModifier(player, Attribute.KNOCKBACK_RESISTANCE, MOD_PREFIX + itemId + "_kb", kbResist);
            if (bonusMana > 0) {
                try { dev.elysium.core.api.CoreAPI.getPlayer(player).setMaxMana(dev.elysium.core.api.CoreAPI.getPlayer(player).getMaxMana() + (int) bonusMana); }
                catch (Exception ignored) {}
            }
        } else {
            removeModifier(player, Attribute.MAX_HEALTH,          MOD_PREFIX + itemId + "_hp");
            removeModifier(player, Attribute.ARMOR,                MOD_PREFIX + itemId + "_def");
            removeModifier(player, Attribute.MOVEMENT_SPEED,       MOD_PREFIX + itemId + "_spd");
            removeModifier(player, Attribute.KNOCKBACK_RESISTANCE, MOD_PREFIX + itemId + "_kb");
            if (bonusMana > 0) {
                try { dev.elysium.core.api.CoreAPI.getPlayer(player).setMaxMana(Math.max(100, dev.elysium.core.api.CoreAPI.getPlayer(player).getMaxMana() - (int) bonusMana)); }
                catch (Exception ignored) {}
            }
        }
    }

    // ── Trophy Mining Speed ───────────────────────────────────────────────────

    /** Lay mining speed bonus tu trophy dang cam */
    public double getTrophyMiningSpeed(Player player) {
        org.bukkit.inventory.ItemStack held = player.getInventory().getItemInMainHand();
        String itemId = plugin.getItemManager().getItemId(held);
        if (itemId == null) return 0;

        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null || data.getCategory() != ElysiumItemData.ItemCategory.TROPHY) return 0;

        PlayerItemState state = plugin.getItemManager().getState(player);
        int level = plugin.getItemMastery().getLevelFromExp(data, state.getExp(itemId));

        return getScaledStat(data, level, "mining-speed");
    }

    // ── Slots ─────────────────────────────────────────────────────────────────

    public AccessorySlotData getSlots(Player player) {
        return playerSlots.computeIfAbsent(player.getUniqueId(), uuid -> new AccessorySlotData());
    }

    public void loadSlots(Player player, String serialized) {
        playerSlots.put(player.getUniqueId(), AccessorySlotData.deserialize(serialized));
    }

    public void removeSlots(UUID uuid) { playerSlots.remove(uuid); }

    private void saveSlots(Player player) {
        AccessorySlotData slots = getSlots(player);
        plugin.getItemDatabase().saveAccessorySlots(player.getUniqueId(), slots.serialize());
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    public AccessorySlotData.SlotType getSlotType(String subType) {
        return switch (subType) {
            case "RING"     -> AccessorySlotData.SlotType.RING;
            case "NECKLACE" -> AccessorySlotData.SlotType.NECKLACE;
            case "CHARM"    -> AccessorySlotData.SlotType.CHARM;
            default         -> null;
        };
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
