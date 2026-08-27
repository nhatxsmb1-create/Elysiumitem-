package dev.elysium.item.gui;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import dev.elysium.item.item.GemData;
import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class AdminGui extends ElysiumGui {

    private final ElysiumItem plugin;
    private final Player      target;
    private int               page = 0;

    private static final int PAGE_SIZE = 28;
    private static final int[] ITEM_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };

    private String filter = "ALL"; // ALL, ACCESSORY, ARMOR, GEM

    public AdminGui(ElysiumItem plugin, Player target) {
        super("&5&l[Admin] Kho Trang Bị", 54);
        this.plugin = plugin;
        this.target = target;
    }

    @Override
    public void build(Player viewer) {
        fill(ItemBuilder.filler());

        List<Object[]> items = new ArrayList<>(); // [id, type, object]

        if (!filter.equals("GEM")) {
            for (Map.Entry<String, ElysiumItemData> entry : plugin.getItemManager().getAllItems().entrySet()) {
                if (filter.equals("ALL") || entry.getValue().getCategory().name().equals(filter)) {
                    items.add(new Object[]{entry.getKey(), "ITEM", entry.getValue()});
                }
            }
        }
        
        if (filter.equals("ALL") || filter.equals("GEM")) {
            for (Map.Entry<String, GemData> entry : plugin.getGemManager().getAllGems().entrySet()) {
                items.add(new Object[]{entry.getKey(), "GEM", entry.getValue()});
            }
        }

        int start = page * PAGE_SIZE;
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= items.size()) break;

            Object[] entry = items.get(idx);
            String id = (String) entry[0];
            String type = (String) entry[1];

            if (type.equals("ITEM")) {
                ElysiumItemData data = (ElysiumItemData) entry[2];
                Material mat;
                try { mat = Material.valueOf(data.getMaterial()); }
                catch (Exception e) { mat = Material.STONE; }

                List<String> lore = new ArrayList<>();
                lore.add(color("&8" + data.getCategory() + " | " + data.getSubType()));
                lore.add("");
                data.getLore().forEach(l -> lore.add(color(l)));
                lore.add("");
                lore.add(color("&7ID: &f" + id));
                lore.add("");
                lore.add(color("&a[Click] &fđể lấy 1 cái vào túi."));

                setButton(ITEM_SLOTS[i], new GuiButton(
                        new ItemBuilder(mat)
                                .name(color(data.getDisplayName()))
                                .lore(lore)
                                .customModelData(data.getModelData())
                                .build(),
                        e -> {
                            e.setCancelled(true);
                            org.bukkit.inventory.ItemStack item = plugin.getItemManager().createItem(id);
                            target.getInventory().addItem(item);
                            viewer.sendMessage(color("&aĐã lấy &f" + data.getDisplayName()));
                        }
                ));
            } else if (type.equals("GEM")) {
                GemData data = (GemData) entry[2];
                List<String> lore = new ArrayList<>();
                lore.add(color("&8NGỌC ĐỘT BIẾN"));
                lore.add("");
                data.getLore().forEach(l -> lore.add(color(l)));
                lore.add("");
                lore.add(color("&7ID: &f" + id));
                lore.add("");
                lore.add(color("&a[Click] &fđể lấy 1 cái vào túi."));

                setButton(ITEM_SLOTS[i], new GuiButton(
                        new ItemBuilder(Material.EMERALD)
                                .name(color(data.getDisplayName()))
                                .lore(lore)
                                .build(),
                        e -> {
                            e.setCancelled(true);
                            org.bukkit.inventory.ItemStack item = plugin.getGemManager().createGemItem(id);
                            target.getInventory().addItem(item);
                            viewer.sendMessage(color("&aĐã lấy &f" + data.getDisplayName()));
                        }
                ));
            }
        }

        // Filters
        setButton(0, new GuiButton(
                new ItemBuilder(filter.equals("ALL") ? Material.NETHER_STAR : Material.GRAY_DYE)
                        .name(color(filter.equals("ALL") ? "&e&lTẤT CẢ" : "&7Tất Cả")).build(),
                e -> { e.setCancelled(true); filter = "ALL"; page = 0; buttons.clear(); inventory.clear(); build(viewer); }
        ));

        setButton(1, new GuiButton(
                new ItemBuilder(filter.equals("ACCESSORY") ? Material.GOLD_NUGGET : Material.GRAY_DYE)
                        .name(color(filter.equals("ACCESSORY") ? "&a&lPHỤ KIỆN" : "&7Phụ Kiện")).build(),
                e -> { e.setCancelled(true); filter = "ACCESSORY"; page = 0; buttons.clear(); inventory.clear(); build(viewer); }
        ));

        setButton(2, new GuiButton(
                new ItemBuilder(filter.equals("ARMOR") ? Material.DIAMOND_CHESTPLATE : Material.GRAY_DYE)
                        .name(color(filter.equals("ARMOR") ? "&b&lGIÁP" : "&7Giáp")).build(),
                e -> { e.setCancelled(true); filter = "ARMOR"; page = 0; buttons.clear(); inventory.clear(); build(viewer); }
        ));
        
        setButton(3, new GuiButton(
                new ItemBuilder(filter.equals("GEM") ? Material.EMERALD : Material.GRAY_DYE)
                        .name(color(filter.equals("GEM") ? "&d&lNGỌC" : "&7Ngọc")).build(),
                e -> { e.setCancelled(true); filter = "GEM"; page = 0; buttons.clear(); inventory.clear(); build(viewer); }
        ));

        // Stats
        int totalItems = items.size();
        int maxPage    = Math.max(0, (totalItems - 1) / PAGE_SIZE);

        if (page > 0) {
            setButton(45, new GuiButton(
                    new ItemBuilder(Material.ARROW).name(color("&7← Trang trước")).build(),
                    e -> { e.setCancelled(true); page--; buttons.clear(); inventory.clear(); build(viewer); }
            ));
        } else {
            fill(45, ItemBuilder.filler());
        }

        fill(49, new ItemBuilder(Material.PAPER)
                .name(color("&fTrang &e" + (page + 1) + "&f/" + (maxPage + 1)))
                .lore(color("&7Tổng: &f" + totalItems + " item"))
                .build());

        if (page < maxPage) {
            setButton(53, new GuiButton(
                    new ItemBuilder(Material.ARROW).name(color("&7Trang sau →")).build(),
                    e -> { e.setCancelled(true); page++; buttons.clear(); inventory.clear(); build(viewer); }
            ));
        } else {
            fill(53, ItemBuilder.filler());
        }
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}