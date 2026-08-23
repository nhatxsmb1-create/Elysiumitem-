package dev.elysium.item.gui;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.accessory.AccessorySlotData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TrangBiGui implements InventoryHolder {
    private final Inventory inventory;
    private final ElysiumItem plugin;
    private final Player player;

    public TrangBiGui(ElysiumItem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, "Trang Bị Cá Nhân");
        build();
    }

    private void build() {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }

        AccessorySlotData slotData = plugin.getAccessoryManager().getSlotData(player.getUniqueId());
        
        // Slot 11: Dây chuyền
        inventory.setItem(11, getSlotItem(slotData.getNecklaceId(), "Dây Chuyền", Material.CHAIN));
        // Slot 13: Nhẫn 1
        inventory.setItem(13, getSlotItem(slotData.getRing1Id(), "Nhẫn 1", Material.GOLD_NUGGET));
        // Slot 15: Nhẫn 2
        inventory.setItem(15, getSlotItem(slotData.getRing2Id(), "Nhẫn 2", Material.GOLD_NUGGET));
    }

    private ItemStack getSlotItem(String itemId, String slotName, Material defaultMat) {
        if (itemId == null || itemId.isEmpty()) {
            ItemStack empty = new ItemStack(defaultMat);
            ItemMeta meta = empty.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7[Trống] §f" + slotName);
                empty.setItemMeta(meta);
            }
            return empty;
        }
        return plugin.getItemManager().createItem(itemId);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}