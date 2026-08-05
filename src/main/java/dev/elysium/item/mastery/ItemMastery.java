package dev.elysium.item.mastery;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import dev.elysium.item.item.PlayerItemState;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public class ItemMastery {

    private final ElysiumItem plugin;

    public ItemMastery(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    // ── Add EXP ───────────────────────────────────────────────────────────────

    public void addExp(Player player, String itemId, long amount) {
        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null || data.getMastery() == null) return;

        PlayerItemState state = plugin.getItemManager().getState(player);
        int oldLevel = getLevelFromExp(data, state.getExp(itemId));

        state.addExp(itemId, amount);

        int newLevel = getLevelFromExp(data, state.getExp(itemId));
        long totalExp = state.getExp(itemId);
        long expNext  = getExpForNextLevel(data, newLevel);
        long expCur   = totalExp - getExpForLevel(data, newLevel);

        if (newLevel > oldLevel) handleLevelUp(player, data, itemId, oldLevel, newLevel);

        // Actionbar EXP notification
        player.sendActionBar(color("&6[" + stripColor(data.getDisplayName())
                + " Lv." + newLevel + "] &e+" + amount
                + " EXP &7| &f" + expCur + "/" + expNext));

        // Refresh item lore
        plugin.getItemManager().refreshItemInInventory(player, itemId);
    }

    private void handleLevelUp(Player player, ElysiumItemData data,
                                String itemId, int oldLevel, int newLevel) {
        for (int lv = oldLevel + 1; lv <= newLevel; lv++) {
            ElysiumItemData.BonusUnlock bonus = data.getMastery().getBonus(lv);

            player.sendTitle(
                    color("&6&l✦ Mastery Lv." + lv),
                    color("&f" + stripColor(data.getDisplayName())),
                    10, 50, 10
            );
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

            if (bonus != null) {
                player.sendMessage(color("&6[Mastery] &f" + stripColor(data.getDisplayName())
                        + " &elen " + lv + "! &a→ " + bonus.getDescription()));
            }
        }
    }

    // ── Level Calc ────────────────────────────────────────────────────────────

    /** EXP can de len cap: 100 * 1.25^(level-1) */
    public long getExpForNextLevel(ElysiumItemData data, int currentLevel) {
        int max = data.getMastery().getMaxLevel();
        if (currentLevel >= max) return 0;
        return (long) (100 * Math.pow(1.25, currentLevel - 1));
    }

    public long getExpForLevel(ElysiumItemData data, int level) {
        long cumulative = 0;
        for (int i = 1; i < level; i++) {
            cumulative += (long) (100 * Math.pow(1.25, i - 1));
        }
        return cumulative;
    }

    public int getLevelFromExp(ElysiumItemData data, long totalExp) {
        int max   = data.getMastery().getMaxLevel();
        int level = 1;
        long cumulative = 0;
        for (int i = 1; i <= max; i++) {
            long needed = (long) (100 * Math.pow(1.25, i - 1));
            if (totalExp >= cumulative + needed) {
                level = i + 1;
                cumulative += needed;
            } else break;
        }
        return Math.min(level, max);
    }

    // ── Unlock Check ──────────────────────────────────────────────────────────

    public boolean hasUnlock(Player player, String itemId, String bonusKey) {
        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null || data.getMastery() == null) return false;

        PlayerItemState state = plugin.getItemManager().getState(player);
        int level = getLevelFromExp(data, state.getExp(itemId));

        for (Map.Entry<Integer, ElysiumItemData.BonusUnlock> entry : data.getMastery().getBonuses().entrySet()) {
            if (level >= entry.getKey() && entry.getValue().has(bonusKey)) return true;
        }
        return false;
    }

    /** Lay gia tri bonus cong don tu tat ca unlock da dat duoc */
    public double getTotalBonus(Player player, String itemId, String bonusKey) {
        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null || data.getMastery() == null) return 0;

        PlayerItemState state = plugin.getItemManager().getState(player);
        int level = getLevelFromExp(data, state.getExp(itemId));

        double total = 0;
        for (Map.Entry<Integer, ElysiumItemData.BonusUnlock> entry : data.getMastery().getBonuses().entrySet()) {
            if (level >= entry.getKey()) {
                total += entry.getValue().getDouble(bonusKey);
            }
        }
        return total;
    }

    public int getLevel(Player player, String itemId) {
        ElysiumItemData data = plugin.getItemManager().getItemData(itemId);
        if (data == null) return 0;
        return getLevelFromExp(data, plugin.getItemManager().getState(player).getExp(itemId));
    }

    public String getNextUnlockDesc(ElysiumItemData data, int currentLevel) {
        if (data.getMastery() == null) return null;
        for (Map.Entry<Integer, ElysiumItemData.BonusUnlock> entry : data.getMastery().getBonuses().entrySet()) {
            if (entry.getKey() > currentLevel) {
                return color("&eLv." + entry.getKey() + " &7- " + entry.getValue().getDescription());
            }
        }
        return color("&6✦ MAX MASTERY!");
    }

    private String stripColor(String s) { return s.replaceAll("§.", "").replaceAll("&.", ""); }
    private String color(String s)      { return s.replace("&", "\u00a7"); }
}
