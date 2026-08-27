package dev.elysium.item.gui;

import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import dev.elysium.item.ElysiumItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SelectLorenItemGui extends ElysiumGui {
    private final ElysiumItem plugin;
    private final LorenGui parent;
    private final boolean isGem;

    public SelectLorenItemGui(ElysiumItem plugin, LorenGui parent, boolean isGem) {
        super(isGem ? "&8&lChọn Ngọc Đột Biến" : "&8&lChọn Trang Bị (Phôi)", 54);
        this.plugin = plugin;
        this.parent = parent;
        this.isGem = isGem;
    }

    @Override
    public void build(Player player) {
        fill(ItemBuilder.filler());
        
        List<ItemStack> found = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            if (isGem) {
                String gemId = plugin.getGemManager().getGemId(item);
                if (gemId != null) found.add(item);
            } else {
                String itemId = plugin.getItemManager().getItemId(item);
                if (itemId != null) found.add(item);
            }
        }
        
        if (found.isEmpty()) {
            setButton(22, new GuiButton(
                new ItemBuilder(Material.BARRIER).name("§cTrống").lore("§7Bạn không có vật phẩm phù hợp trong túi!").build(),
                e -> {
                    e.setCancelled(true);
                    dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, parent);
                }
            ));
        } else {
            int slot = 10;
            for (ItemStack item : found) {
                if (slot % 9 == 8) slot += 2;
                if (slot > 43) break;
                
                ItemStack display = item.clone();
                org.bukkit.inventory.meta.ItemMeta meta = display.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add("§a[Click] §fđể đưa vào Lò Rèn");
                meta.setLore(lore);
                display.setItemMeta(meta);
                
                setButton(slot, new GuiButton(display, e -> {
                    e.setCancelled(true);
                    if (isGem) {
                        parent.setGem(item);
                    } else {
                        parent.setGear(item);
                    }
                    dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, parent);
                }));
                slot++;
            }
        }
        
        setButton(49, new GuiButton(new ItemBuilder(Material.ARROW).name("§cQuay lại").build(), e -> {
            e.setCancelled(true);
            dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, parent);
        }));
    }
}