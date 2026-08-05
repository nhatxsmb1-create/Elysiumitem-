package dev.elysium.item.gui;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.accessory.AccessorySlotData;
import dev.elysium.item.accessory.AccessorySlotData.SlotType;
import dev.elysium.item.item.ElysiumItemData;
import dev.elysium.item.item.PlayerItemState;
import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TrangBiGui extends ElysiumGui {

    private final ElysiumItem plugin;

    public TrangBiGui(ElysiumItem plugin) {
        super("&5&l⚔ Trang Bị Phụ Kiện", 54);
        this.plugin = plugin;
    }

    @Override
    public void build(Player player) {
        fill(ItemBuilder.filler());

        AccessorySlotData slots = plugin.getAccessoryManager().getSlots(player);

        // ── Background viền ─────────────────────────────────────────────────

        // Vien ngoai
        for (int i = 0; i < 9; i++)   fill(i,  borderItem("&8"));
        for (int i = 45; i < 54; i++) fill(i,  borderItem("&8"));
        for (int i = 0; i < 54; i += 9) fill(i, borderItem("&8"));
        for (int i = 8; i < 54; i += 9) fill(i, borderItem("&8"));

        // ── Header ──────────────────────────────────────────────────────────

        fill(4, new ItemBuilder(Material.NETHER_STAR)
                .name(color("&5&l⚔ Trang Bị Phụ Kiện"))
                .lore(
                    "",
                    color("&7Cầm item → Click vào slot để trang bị"),
                    color("&7Click vào item đang đeo → Tháo ra"),
                    ""
                ).glow().build());

        // ── Slot RING (cột trái) ─────────────────────────────────────────────

        buildSlot(player, slots, SlotType.RING,
                19,  // Slot icon
                "💍", "&6Nhẫn",
                Material.GOLD_NUGGET,
                List.of(
                    color("&7Slot: &6Nhẫn"),
                    color("&7Chỉ có thể đeo &e1 &7nhẫn"),
                    "",
                    color("&7Bonus từ nhẫn:"),
                    color("  &c❤ HP  &b✦ Mana  &7🛡 Phòng Thủ")
                )
        );

        // ── Slot NECKLACE (cột giữa) ──────────────────────────────────────────

        buildSlot(player, slots, SlotType.NECKLACE,
                22,
                "📿", "&bDây Chuyền",
                Material.EMERALD,
                List.of(
                    color("&7Slot: &bDây Chuyền"),
                    color("&7Chỉ có thể đeo &e1 &7dây chuyền"),
                    "",
                    color("&7Bonus từ dây chuyền:"),
                    color("  &c❤ HP  &a🛡 Giảm Sát Thương")
                )
        );

        // ── Slot CHARM (cột phải) ─────────────────────────────────────────────

        buildSlot(player, slots, SlotType.CHARM,
                25,
                "🍀", "&aBùa",
                Material.RABBIT_FOOT,
                List.of(
                    color("&7Slot: &aBùa"),
                    color("&7Chỉ có thể mang &e1 &7bùa"),
                    "",
                    color("&7Bonus từ bùa:"),
                    color("  &6◈ Drop  &a★ EXP  &e⚡ Speed")
                )
        );

        // ── Stats tổng hiện tại (row dưới) ────────────────────────────────────

        buildStatsPanel(player, slots, 28, 29, 30, 31, 32);

        // ── Hướng dẫn ─────────────────────────────────────────────────────────

        fill(37, new ItemBuilder(Material.BOOK)
                .name(color("&eHướng Dẫn"))
                .lore(
                    "",
                    color("&e1. &fCầm accessory trên tay"),
                    color("&e2. &fMở &b/trangbi"),
                    color("&e3. &fClick vào slot để đeo"),
                    "",
                    color("&7Để tháo ra: click vào item đang đeo")
                ).build());

        // Mastery shortcut (slot 41)
        setButton(41, new GuiButton(
                new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                        .name(color("&6✦ Mastery Collection"))
                        .lore(color("&7Click để xem tiến trình mastery"))
                        .build(),
                e -> {
                    e.setCancelled(true);
                    MasteryGui gui = new MasteryGui(plugin);
                    GuiListener.register(player.getUniqueId(), gui);
                    gui.open(player);
                }
        ));

        // Đóng (slot 49)
        setButton(49, new GuiButton(
                new ItemBuilder(Material.BARRIER).name(color("&cĐóng")).build(),
                e -> { e.setCancelled(true); player.closeInventory(); }
        ));
    }

    // ── Build từng slot ───────────────────────────────────────────────────────

    private void buildSlot(Player player, AccessorySlotData slots, SlotType slotType,
                            int slotIndex, String icon, String slotName,
                            Material emptyMat, List<String> emptyLore) {
        String equippedId = slots.getEquipped(slotType);

        if (equippedId == null) {
            // Slot trống
            List<String> lore = new ArrayList<>(emptyLore);
            lore.add("");

            // Kiem tra item dang cam tren tay co phu hop khong
            String heldId = plugin.getItemManager().getItemId(
                    player.getInventory().getItemInMainHand());
            if (heldId != null) {
                ElysiumItemData heldData = plugin.getItemManager().getItemData(heldId);
                if (heldData != null && plugin.getAccessoryManager()
                        .getSlotType(heldData.getSubType()) == slotType) {
                    lore.add(color("&a▶ Click để đeo: " + heldData.getDisplayName()));
                } else {
                    lore.add(color("&7▶ Cầm accessory phù hợp để đeo"));
                }
            } else {
                lore.add(color("&7▶ Cầm accessory phù hợp để đeo"));
            }

            setButton(slotIndex, new GuiButton(
                    new ItemBuilder(emptyMat)
                            .name(color("&8[" + icon + " " + slotName + "&8] &7Trống"))
                            .lore(lore)
                            .build(),
                    e -> {
                        e.setCancelled(true);
                        // Thu trang bi item dang cam
                        String hId = plugin.getItemManager().getItemId(
                                player.getInventory().getItemInMainHand());
                        if (hId == null) {
                            player.sendMessage(color("&cCầm accessory trên tay trước!"));
                            return;
                        }
                        if (plugin.getAccessoryManager().equip(player, hId)) {
                            // Refresh GUI
                            build(player);
                            player.openInventory(getInventory());
                        }
                    }
            ));

            // Arrow indicator phia tren slot
            fill(slotIndex - 9, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .name(color("&a" + icon + " " + slotName + " &7(Trống)")).build());

        } else {
            // Slot co item
            ElysiumItemData data  = plugin.getItemManager().getItemData(equippedId);
            PlayerItemState state = plugin.getItemManager().getState(player);
            int level = data != null ? plugin.getItemMastery().getLevelFromExp(
                    data, state.getExp(equippedId)) : 0;

            Material mat;
            try { mat = Material.valueOf(data != null ? data.getMaterial() : "STONE"); }
            catch (Exception ex) { mat = Material.STONE; }

            List<String> lore = new ArrayList<>();
            lore.add(color("&8[" + icon + " " + slotName + "&8]"));
            lore.add("");
            if (data != null) {
                lore.add(color("&6✦ Mastery: &e" + level + "/" + data.getMastery().getMaxLevel()));
                // Hien stats dang duoc ap dung
                lore.add("");
                lore.add(color("&7Stats dang ap dung:"));
                addStatLine(lore, data, level, "bonus-hp",      "&c❤ HP",     true);
                addStatLine(lore, data, level, "bonus-defense",  "&7🛡 Phong Thu", false);
                addStatLine(lore, data, level, "bonus-mana",     "&b✦ Mana",   true);
                addStatLine(lore, data, level, "bonus-speed",    "&e⚡ Speed",  true);
                addStatLine(lore, data, level, "damage-reduce",  "&a🛡 Giam ST", true);
            }
            lore.add("");
            lore.add(color("&cClick để tháo ra!"));

            final String finalId  = equippedId;
            final SlotType finalSt= slotType;
            setButton(slotIndex, new GuiButton(
                    new ItemBuilder(mat)
                            .name(color(data != null ? data.getDisplayName() : equippedId))
                            .lore(lore)
                            .customModelData(data != null ? data.getModelData() : 0)
                            .glow()
                            .build(),
                    e -> {
                        e.setCancelled(true);
                        plugin.getAccessoryManager().unequip(player, finalSt, true);
                        build(player);
                        player.openInventory(getInventory());
                    }
            ));

            // Arrow indicator phia tren slot
            fill(slotIndex - 9, new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE)
                    .name(color("&5" + icon + " " + slotName + " &a✔")).build());
        }
    }

    // ── Stats Panel ───────────────────────────────────────────────────────────

    private void buildStatsPanel(Player player, AccessorySlotData slots,
                                  int... panelSlots) {
        // Tinh tong stats tu tat ca accessory dang deo
        double totalHp      = 0;
        double totalDef     = 0;
        double totalMana    = 0;
        double totalSpeed   = 0;
        double totalCrit    = 0;
        double totalDmgRed  = 0;

        for (SlotType st : SlotType.values()) {
            String id = slots.getEquipped(st);
            if (id == null) continue;
            ElysiumItemData data = plugin.getItemManager().getItemData(id);
            if (data == null) continue;
            int level = plugin.getItemMastery().getLevel(player, id);

            totalHp     += getScaled(data, level, "bonus-hp");
            totalDef    += getScaled(data, level, "bonus-defense");
            totalMana   += getScaled(data, level, "bonus-mana");
            totalSpeed  += getScaled(data, level, "bonus-speed");
            totalCrit   += getScaled(data, level, "bonus-crit-chance");
            totalDmgRed += getScaled(data, level, "damage-reduce");
        }

        List<String> statsLore = new ArrayList<>();
        statsLore.add("");
        statsLore.add(color("&7Tổng bonus từ phụ kiện:"));
        if (totalHp     > 0) statsLore.add(color("  &c❤ HP: &f+" + (int) totalHp));
        if (totalDef    > 0) statsLore.add(color("  &7🛡 Phòng Thủ: &f+" + (int) totalDef));
        if (totalMana   > 0) statsLore.add(color("  &b✦ Mana: &f+" + (int) totalMana));
        if (totalSpeed  > 0) statsLore.add(color("  &e⚡ Tốc Độ: &f+" + String.format("%.0f%%", totalSpeed * 100)));
        if (totalCrit   > 0) statsLore.add(color("  &c⚔ Crit: &f+" + String.format("%.0f%%", totalCrit * 100)));
        if (totalDmgRed > 0) statsLore.add(color("  &a🛡 Giảm ST: &f-" + String.format("%.0f%%", totalDmgRed * 100)));
        if (totalHp == 0 && totalDef == 0 && totalMana == 0)
            statsLore.add(color("  &7Chưa có bonus nào"));

        fill(31, new ItemBuilder(Material.COMPARATOR)
                .name(color("&e📊 Tổng Stats Phụ Kiện"))
                .lore(statsLore)
                .build());
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    private void addStatLine(List<String> lore, ElysiumItemData data, int level,
                              String key, String label, boolean isPercent) {
        double val = getScaled(data, level, key);
        if (val <= 0) return;
        String fval = isPercent && key.contains("reduce") || key.contains("chance") || key.contains("speed")
                ? String.format("%.0f%%", val * 100)
                : "+" + (int) val;
        lore.add(color("  " + label + ": &f" + fval));
    }

    private double getScaled(ElysiumItemData data, int level, String key) {
        double base = data.getStatDouble(key);
        double mastery = 0;
        if (data.getMastery() != null) {
            for (var entry : data.getMastery().getBonuses().entrySet()) {
                if (level >= entry.getKey()) mastery += entry.getValue().getDouble(key);
            }
        }
        return base + mastery;
    }

    private org.bukkit.inventory.ItemStack borderItem(String color) {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(color(color + " ")).build();
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
