package dev.elysium.item.gui;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class AdminGui extends ElysiumGui {

    private final ElysiumItem plugin;
    private final Player      target;     // Player se duoc give item
    private int               page = 0;

    private static final int PAGE_SIZE = 28; // Slot hien thi item
    private static final int[] ITEM_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };

    // Filter
    private ElysiumItemData.ItemCategory filterCategory = null;

    public AdminGui(ElysiumItem plugin, Player target) {
        super("&5&l[Admin] Elysium Items", 54);
        this.plugin = plugin;
        this.target = target;
    }

    @Override
    public void build(Player viewer) {
        fill(ItemBuilder.filler());

        // Lay danh sach item theo filter
        List<Map.Entry<String, ElysiumItemData>> items = new ArrayList<>();
        for (Map.Entry<String, ElysiumItemData> entry : plugin.getItemManager().getAllItems().entrySet()) {
            if (filterCategory == null || entry.getValue().getCategory() == filterCategory) {
                items.add(entry);
            }
        }

        // Hien item theo page
        int start = page * PAGE_SIZE;
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= items.size()) break;

            Map.Entry<String, ElysiumItemData> entry = items.get(idx);
            String          id   = entry.getKey();
            ElysiumItemData data = entry.getValue();

            Material mat;
            try { mat = Material.valueOf(data.getMaterial()); }
            catch (Exception e) { mat = Material.STONE; }

            List<String> lore = new ArrayList<>();
            lore.add(color("&8" + data.getCategory() + " | " + data.getSubType()));
            lore.add("");
            data.getLore().forEach(l -> lore.add(color(l)));
            lore.add("");
            lore.add(color("&7ID: &f" + id));
            lore.add(color("&7Give cho: &e" + target.getName()));
            lore.add("");
            lore.add(color("&aLeft Click: &fGive 1 cai"));
            lore.add(color("&6Right Click: &fGive voi Mastery info"));

            setButton(ITEM_SLOTS[i], new GuiButton(
                    new ItemBuilder(mat)
                            .name(color(data.getDisplayName()))
                            .lore(lore)
                            .customModelData(data.getModelData())
                            .build(),
                    e -> {
                        e.setCancelled(true);
                        if (e.isRightClick()) {
                            plugin.getItemManager().giveItem(target, id);
                            viewer.sendMessage(color("&aGive &f" + data.getDisplayName()
                                    + " &acho &e" + target.getName()));
                        } else {
                            // Left click: give item thuan tuy khong co mastery info
                            org.bukkit.inventory.ItemStack item = plugin.getItemManager().createItem(id);
                            target.getInventory().addItem(item);
                            viewer.sendMessage(color("&aGive &f" + data.getDisplayName()
                                    + " &acho &e" + target.getName()));
                        }
                    }
            ));
        }

        // ── Filter buttons (row 1) ────────────────────────────────────────────

        // ALL
        setButton(0, new GuiButton(
                new ItemBuilder(filterCategory == null ? Material.NETHER_STAR : Material.GRAY_DYE)
                        .name(color(filterCategory == null ? "&e&lTẤT CẢ" : "&7Tất Cả")).build(),
                e -> { e.setCancelled(true); filterCategory = null; page = 0; build(viewer); player(viewer); }
        ));

        // ACCESSORY
        setButton(1, new GuiButton(
                new ItemBuilder(filterCategory == ElysiumItemData.ItemCategory.ACCESSORY
                        ? Material.EMERALD : Material.GRAY_DYE)
                        .name(color(filterCategory == ElysiumItemData.ItemCategory.ACCESSORY
                                ? "&a&lPHỤ KIỆN" : "&7Phụ Kiện")).build(),
                e -> { e.setCancelled(true); filterCategory = ElysiumItemData.ItemCategory.ACCESSORY; page = 0; build(viewer); player(viewer); }
        ));

        // ARMOR
        setButton(2, new GuiButton(
                new ItemBuilder(filterCategory == ElysiumItemData.ItemCategory.ARMOR
                        ? Material.DIAMOND_CHESTPLATE : Material.GRAY_DYE)
                        .name(color(filterCategory == ElysiumItemData.ItemCategory.ARMOR
                                ? "&b&lGIÁP" : "&7Giáp")).build(),
                e -> { e.setCancelled(true); filterCategory = ElysiumItemData.ItemCategory.ARMOR; page = 0; build(viewer); player(viewer); }
        ));

        // TROPHY
        setButton(3, new GuiButton(
                new ItemBuilder(filterCategory == ElysiumItemData.ItemCategory.TROPHY
                        ? Material.GOLDEN_PICKAXE : Material.GRAY_DYE)
                        .name(color(filterCategory == ElysiumItemData.ItemCategory.TROPHY
                                ? "&6&lCÚP" : "&7Cúp")).build(),
                e -> { e.setCancelled(true); filterCategory = ElysiumItemData.ItemCategory.TROPHY; page = 0; build(viewer); player(viewer); }
        ));

        // Target info (slot 8)
        fill(8, new ItemBuilder(Material.PLAYER_HEAD)
                .name(color("&eGive cho: &f" + target.getName()))
                .lore(color("&7Left Click = item thuần"),
                      color("&7Right Click = item + Mastery info"))
                .build());

        // Stats
        int totalItems = items.size();
        int maxPage    = Math.max(0, (totalItems - 1) / PAGE_SIZE);

        // Prev page (slot 45)
        if (page > 0) {
            setButton(45, new GuiButton(
                    new ItemBuilder(Material.ARROW).name(color("&7← Trang trước")).build(),
                    e -> { e.setCancelled(true); page--; build(viewer); player(viewer); }
            ));
        }

        // Page info (slot 49)
        fill(49, new ItemBuilder(Material.PAPER)
                .name(color("&fTrang &e" + (page + 1) + "&f/" + (maxPage + 1)))
                .lore(color("&7Tổng: &f" + totalItems + " item"))
                .build());

        // Next page (slot 53)
        if (page < maxPage) {
            setButton(53, new GuiButton(
                    new ItemBuilder(Material.ARROW).name(color("&7Trang sau →")).build(),
                    e -> { e.setCancelled(true); page++; build(viewer); player(viewer); }
            ));
        }

        // Dong (slot 47)
        setButton(47, new GuiButton(
                new ItemBuilder(Material.BARRIER).name(color("&cĐóng")).build(),
                e -> { e.setCancelled(true); viewer.closeInventory(); }
        ));
    }

    private void player(Player viewer) {
        viewer.openInventory(getInventory());
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
