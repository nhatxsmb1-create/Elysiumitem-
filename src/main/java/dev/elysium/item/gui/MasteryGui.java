package dev.elysium.item.gui;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import dev.elysium.item.item.PlayerItemState;
import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class MasteryGui extends ElysiumGui {

    private final ElysiumItem plugin;
    private String            selectedItemId = null;

    public MasteryGui(ElysiumItem plugin) {
        super("&5&l✦ Mastery Collection", 54);
        this.plugin = plugin;
    }

    @Override
    public void build(Player player) {
        fill(ItemBuilder.filler());

        if (selectedItemId == null) buildOverview(player);
        else                        buildDetail(player, selectedItemId);
    }

    // ── Overview: hien thi tat ca item player dang co + mastery ──────────────

    private void buildOverview(Player player) {
        PlayerItemState state    = plugin.getItemManager().getState(player);
        List<String>    equipped = plugin.getItemManager().getEquippedItemIds(player);

        // Header stats (slot 4)
        int totalItems    = equipped.size();
        int maxedItems    = 0;
        for (String id : equipped) {
            ElysiumItemData data = plugin.getItemManager().getItemData(id);
            if (data == null) continue;
            int level = plugin.getItemMastery().getLevelFromExp(data, state.getExp(id));
            if (level >= data.getMastery().getMaxLevel()) maxedItems++;
        }

        fill(4, new ItemBuilder(Material.NETHER_STAR)
                .name(color("&5&l✦ Mastery Collection"))
                .lore(
                    "",
                    color("&7Item dang trang bi: &e" + totalItems),
                    color("&7Da Max Mastery: &a" + maxedItems + "&7/" + totalItems),
                    "",
                    color("&7Click item de xem chi tiet!")
                ).glow().build());

        // Category tabs
        setButton(0, new GuiButton(new ItemBuilder(Material.GOLD_NUGGET)
                .name(color("&6💍 Nhẫn")).build(),
                e -> { e.setCancelled(true); }));
        setButton(1, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name(color("&a📿 Dây Chuyền")).build(),
                e -> { e.setCancelled(true); }));
        setButton(2, new GuiButton(new ItemBuilder(Material.RABBIT_FOOT)
                .name(color("&e🍀 Bùa")).build(),
                e -> { e.setCancelled(true); }));
        setButton(3, new GuiButton(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                .name(color("&b🛡 Giáp")).build(),
                e -> { e.setCancelled(true); }));
        setButton(5, new GuiButton(new ItemBuilder(Material.GOLDEN_PICKAXE)
                .name(color("&6🏆 Cúp")).build(),
                e -> { e.setCancelled(true); }));

        // Hien thi tat ca item trong game (khong chi equipped)
        int[] slots = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34
        };

        List<Map.Entry<String, ElysiumItemData>> allItems =
                new ArrayList<>(plugin.getItemManager().getAllItems().entrySet());

        for (int i = 0; i < slots.length && i < allItems.size(); i++) {
            Map.Entry<String, ElysiumItemData> entry = allItems.get(i);
            String          id   = entry.getKey();
            ElysiumItemData data = entry.getValue();
            if (data.getMastery() == null) continue;

            int  level   = plugin.getItemMastery().getLevelFromExp(data, state.getExp(id));
            int  maxLevel= data.getMastery().getMaxLevel();
            long totalExp= state.getExp(id);
            long expNext = plugin.getItemMastery().getExpForNextLevel(data, level);
            long expCur  = totalExp - plugin.getItemMastery().getExpForLevel(data, level);
            double pct   = expNext > 0 ? (expCur / (double) expNext) * 100.0 : 100.0;

            boolean isEquipped = equipped.contains(id);
            boolean isMaxed    = level >= maxLevel;

            Material mat;
            try { mat = Material.valueOf(data.getMaterial()); }
            catch (Exception e) { mat = Material.STONE; }

            List<String> lore = new ArrayList<>();
            lore.add(color(buildTypeBadge(data)));
            lore.add("");
            lore.add(color("&6✦ Mastery: " + getLevelColor(level, maxLevel) + level + "&7/" + maxLevel));
            lore.add(buildMiniExpBar(expCur, expNext, pct));
            lore.add("");

            if (isMaxed) {
                lore.add(color("&6&l★ MAX MASTERY!"));
            } else {
                String next = plugin.getItemMastery().getNextUnlockDesc(data, level);
                if (next != null) lore.add(color("&7Next: " + next));
            }

            lore.add("");
            if (isEquipped) lore.add(color("&a✔ Đang trang bị"));
            else            lore.add(color("&7Chưa trang bị"));
            lore.add(color("&eClick để xem chi tiết!"));

            final String finalId = id;
            setButton(slots[i], new GuiButton(
                    new ItemBuilder(isMaxed ? Material.NETHER_STAR : mat)
                            .name(color(data.getDisplayName()))
                            .lore(lore)
                            .customModelData(data.getModelData())
                            .build(),
                    e -> {
                        e.setCancelled(true);
                        selectedItemId = finalId;
                        build(player);
                        player.openInventory(getInventory());
                    }
            ));
        }

        // Dong (slot 49)
        setButton(49, new GuiButton(
                new ItemBuilder(Material.BARRIER).name(color("&cĐóng")).build(),
                e -> { e.setCancelled(true); player.closeInventory(); }
        ));
    }

    // ── Detail: chi tiet mastery 1 item ──────────────────────────────────────

    private void buildDetail(Player player, String itemId) {
        ElysiumItemData data  = plugin.getItemManager().getItemData(itemId);
        if (data == null || data.getMastery() == null) {
            selectedItemId = null; build(player); return;
        }

        PlayerItemState state   = plugin.getItemManager().getState(player);
        int    level   = plugin.getItemMastery().getLevelFromExp(data, state.getExp(itemId));
        int    maxLevel= data.getMastery().getMaxLevel();
        long   totalExp= state.getExp(itemId);
        long   expNext = plugin.getItemMastery().getExpForNextLevel(data, level);
        long   expCur  = totalExp - plugin.getItemMastery().getExpForLevel(data, level);
        double pct     = expNext > 0 ? (expCur / (double) expNext) * 100.0 : 100.0;

        // Item icon (slot 4)
        Material mat;
        try { mat = Material.valueOf(data.getMaterial()); }
        catch (Exception e) { mat = Material.STONE; }

        fill(4, new ItemBuilder(mat)
                .name(color(data.getDisplayName()))
                .lore(
                    color(buildTypeBadge(data)),
                    "",
                    color("&6✦ Mastery: " + getLevelColor(level, maxLevel) + level + "/" + maxLevel),
                    buildMiniExpBar(expCur, expNext, pct),
                    color("&7EXP: &f" + totalExp + " &7tổng cộng")
                ).customModelData(data.getModelData()).glow().build());

        // Unlock milestones
        List<Map.Entry<Integer, ElysiumItemData.BonusUnlock>> bonuses =
                new ArrayList<>(data.getMastery().getBonuses().entrySet());

        int[] milestoneSlots = {19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

        for (int i = 0; i < milestoneSlots.length && i < bonuses.size(); i++) {
            Map.Entry<Integer, ElysiumItemData.BonusUnlock> entry = bonuses.get(i);
            int lv     = entry.getKey();
            var bonus  = entry.getValue();
            boolean unlocked = level >= lv;
            boolean isCurrent= (level < lv && (i == 0 || level >= bonuses.get(i-1).getKey()));

            Material mslot = unlocked ? Material.LIME_STAINED_GLASS_PANE
                           : isCurrent ? Material.YELLOW_STAINED_GLASS_PANE
                           : Material.GRAY_STAINED_GLASS_PANE;

            String prefix = unlocked ? "&a✔ " : isCurrent ? "&e◆ " : "&8○ ";

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(color(unlocked ? "&aDa mo khoa!" : isCurrent ? "&eGan mo khoa!" : "&8Chua mo"));
            lore.add(color("&7Can level: &e" + lv));
            lore.add("");
            lore.add(color("&f" + bonus.getDescription()));

            fill(milestoneSlots[i], new ItemBuilder(mslot)
                    .name(color(prefix + "Level " + lv))
                    .lore(lore)
                    .build());
        }

        // Progress bar lon (slot 13)
        fill(13, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(color("&6EXP Progress"))
                .lore(
                    "",
                    buildLargeExpBar(expCur, expNext, pct),
                    color("&7" + expCur + " / " + expNext + " EXP"),
                    color("&7(" + String.format("%.1f%%", pct) + ")")
                ).build());

        // EXP source info (slot 15)
        String expSource = switch (data.getCategory()) {
            case ACCESSORY -> color("&7Bi danh trung → &e+" + data.getMastery().getExpPerHit() + " EXP");
            case ARMOR     -> color("&7Bi danh trung → &e+" + data.getMastery().getExpPerHit() + " EXP");
            case TROPHY    -> color("&7Dao khoang san → &eEXP theo loai quy");
        };

        fill(15, new ItemBuilder(Material.BOOK)
                .name(color("&6Cach tang EXP"))
                .lore("", expSource, "").build());

        // Quay lai (slot 45)
        setButton(45, new GuiButton(
                new ItemBuilder(Material.ARROW).name(color("&7← Quay Lại")).build(),
                e -> {
                    e.setCancelled(true);
                    selectedItemId = null;
                    build(player);
                    player.openInventory(getInventory());
                }
        ));

        // Dong (slot 49)
        setButton(49, new GuiButton(
                new ItemBuilder(Material.BARRIER).name(color("&cĐóng")).build(),
                e -> { e.setCancelled(true); player.closeInventory(); }
        ));
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    private String getLevelColor(int level, int maxLevel) {
        double ratio = (double) level / maxLevel;
        if (ratio >= 1.0) return "&6&l";
        if (ratio >= 0.7) return "&a";
        if (ratio >= 0.4) return "&e";
        return "&f";
    }

    private String buildMiniExpBar(long cur, long max, double pct) {
        int bars   = 10;
        int filled = (int) (pct / 100.0 * bars);
        StringBuilder bar = new StringBuilder(color("&6["));
        for (int i = 0; i < bars; i++) bar.append(i < filled ? color("&e|") : color("&8|"));
        bar.append(color("&6] &f" + String.format("%.0f%%", pct)));
        return bar.toString();
    }

    private String buildLargeExpBar(long cur, long max, double pct) {
        int bars   = 20;
        int filled = (int) (pct / 100.0 * bars);
        StringBuilder bar = new StringBuilder(color("&6["));
        for (int i = 0; i < bars; i++) bar.append(i < filled ? color("&e█") : color("&8░"));
        bar.append(color("&6]"));
        return bar.toString();
    }

    private String buildTypeBadge(ElysiumItemData data) {
        return switch (data.getSubType()) {
            case "RING"       -> "&8[💍 &7Nhẫn&8]";
            case "NECKLACE"   -> "&8[📿 &7Dây Chuyền&8]";
            case "CHARM"      -> "&8[🍀 &7Bùa&8]";
            case "HELMET"     -> "&8[⛑ &7Mũ Giáp&8]";
            case "CHESTPLATE" -> "&8[🛡 &7Giáp Ngực&8]";
            case "LEGGINGS"   -> "&8[🩲 &7Quần Giáp&8]";
            case "BOOTS"      -> "&8[👟 &7Giày Giáp&8]";
            case "TROPHY"     -> "&8[🏆 &7Cúp&8]";
            default           -> "&8[&7" + data.getSubType() + "&8]";
        };
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
