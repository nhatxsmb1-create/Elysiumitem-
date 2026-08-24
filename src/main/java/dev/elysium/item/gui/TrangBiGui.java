package dev.elysium.item.gui;

import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import dev.elysium.item.ElysiumItem;
import dev.elysium.item.accessory.AccessorySlotData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class TrangBiGui extends ElysiumGui {
    private final ElysiumItem plugin;

    public TrangBiGui(ElysiumItem plugin) {
        super("&8&lTrang Bị Cá Nhân &f(Nhẫn & Dây chuyền)", 36);
        this.plugin = plugin;
    }

    @Override
    public void build(Player player) {
        ItemStack glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        fill(glass);
        
        ItemStack highlightGlass = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build();
        fill(10, highlightGlass);
        fill(12, highlightGlass);
        fill(14, highlightGlass);
        fill(16, highlightGlass);
        fill(19, highlightGlass);
        fill(21, highlightGlass);
        fill(23, highlightGlass);
        fill(25, highlightGlass);

        AccessorySlotData slotData = plugin.getAccessoryManager().getSlotData(player.getUniqueId());
        
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§d§lChỉ Số Của §f" + player.getName());
            
            double totalHp = plugin.getMechanicEngine().getTotalStat(player, "bonus-hp");
            double totalSpeed = plugin.getMechanicEngine().getTotalStat(player, "bonus-speed");
            
            meta.setLore(Arrays.asList(
                "§7Tổng hợp chỉ số từ các mảnh ghép cơ chế:",
                "",
                "§8▪ §c❤ HP Cộng Thêm: §f" + totalHp,
                "§8▪ §e⚡ Tốc Độ: §f" + totalSpeed + "%",
                "",
                "§a(Hệ thống đang tự động tối ưu sức mạnh)"
            ));
            head.setItemMeta(meta);
        }
        fill(4, head);

        // Slot 20: Dây chuyền
        setButton(20, new GuiButton(getSlotItem(slotData.getNecklaceId(), "Dây Chuyền", Material.CHAIN), e -> {
            e.setCancelled(true);
            handleSlotClick(player, AccessorySlotData.SlotType.NECKLACE, "NECKLACE", slotData);
        }));
        
        // Slot 22: Nhẫn 1
        setButton(22, new GuiButton(getSlotItem(slotData.getRing1Id(), "Nhẫn 1", Material.GOLD_NUGGET), e -> {
            e.setCancelled(true);
            handleSlotClick(player, AccessorySlotData.SlotType.RING_1, "RING", slotData);
        }));
        
        // Slot 24: Nhẫn 2
        setButton(24, new GuiButton(getSlotItem(slotData.getRing2Id(), "Nhẫn 2", Material.GOLD_NUGGET), e -> {
            e.setCancelled(true);
            handleSlotClick(player, AccessorySlotData.SlotType.RING_2, "RING", slotData);
        }));
        
        setButton(31, new GuiButton(new ItemBuilder(Material.BARRIER).name("§cĐóng").build(), e -> {
            e.setCancelled(true);
            player.closeInventory();
        }));
    }

    private void handleSlotClick(Player player, AccessorySlotData.SlotType slotType, String requiredSubType, AccessorySlotData slotData) {
        String currentEquipped = slotData.getEquipped(slotType);
        
        if (currentEquipped != null) {
            // Unequip
            player.getInventory().addItem(plugin.getItemManager().createItem(currentEquipped));
            slotData.unequip(slotType);
            player.sendMessage("§aĐã tháo trang bị vào túi đồ!");
            
            buttons.clear();
            inventory.clear();
            build(player);
        } else {
            // Open Selection GUI
            dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, new SelectAccessoryGui(plugin, slotType, requiredSubType));
        }
    }

    private ItemStack getSlotItem(String itemId, String slotName, Material defaultMat) {
        if (itemId == null || itemId.isEmpty()) {
            return new ItemBuilder(defaultMat).name("§7[Trống] §f" + slotName)
                .lore(
                    "",
                    "§7Bạn chưa trang bị món đồ nào ở đây.",
                    "§7Món đồ này dùng để kích hoạt các cơ chế ẩn.",
                    "",
                    "§e[Click] §fđể mở danh sách trang bị trong túi."
                )
                .build();
        }
        
        ItemStack item = plugin.getItemManager().createItem(itemId);
        if (item == null) {
            // Item ID da duoc luu nhung khong con ton tai trong config -> hien slot rong
            return new ItemBuilder(defaultMat).name("§7[Trống] §f" + slotName)
                .lore(
                    "",
                    "§cMón đồ §e" + itemId + " §ckhông còn tồn tại.",
                    "§7Hãy tháo ra và trang bị lại.",
                    "",
                    "§e[Click] §fđể tháo."
                )
                .build();
        }
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        lore.add("");
        lore.add("§c[Click] §fđể tháo xuống túi đồ.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
}