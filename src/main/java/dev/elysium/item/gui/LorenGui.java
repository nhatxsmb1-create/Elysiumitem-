package dev.elysium.item.gui;

import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import dev.elysium.item.ElysiumItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LorenGui extends ElysiumGui {
    
    private final ElysiumItem plugin;
    private ItemStack selectedGear = null;
    private ItemStack selectedGem = null;

    public LorenGui(ElysiumItem plugin) {
        super("&8&l♨ Lò Rèn Thức Tỉnh", 45);
        this.plugin = plugin;
    }

    public void setGear(ItemStack gear) { this.selectedGear = gear; }
    public void setGem(ItemStack gem)   { this.selectedGem = gem; }

    @Override
    public void build(Player player) {
        fill(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
        
        // Check if items are still in inventory (anti-dupe)
        if (selectedGear != null && !player.getInventory().contains(selectedGear)) selectedGear = null;
        if (selectedGem != null && !player.getInventory().contains(selectedGem)) selectedGem = null;

        // Slot 11: Trang bị
        if (selectedGear == null) {
            setButton(11, new GuiButton(new ItemBuilder(Material.ANVIL).name("§e[Chọn Trang Bị]").lore("§7Click để chọn trang bị", "§7cần khảm ngọc.").build(), e -> {
                e.setCancelled(true);
                dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, new SelectLorenItemGui(plugin, this, false));
            }));
        } else {
            setButton(11, new GuiButton(selectedGear, e -> { e.setCancelled(true); setGear(null); build(player); }));
        }
        
        // Slot 15: Ngọc Khảm
        if (selectedGem == null) {
            setButton(15, new GuiButton(new ItemBuilder(Material.EMERALD).name("§a[Chọn Ngọc Đột Biến]").lore("§7Click để chọn ngọc", "§7từ túi đồ.").build(), e -> {
                e.setCancelled(true);
                dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, new SelectLorenItemGui(plugin, this, true));
            }));
        } else {
            setButton(15, new GuiButton(selectedGem, e -> { e.setCancelled(true); setGem(null); build(player); }));
        }

        // Slot 31: Nút Khảm
        setButton(31, new GuiButton(new ItemBuilder(Material.LAVA_BUCKET).name("§c§l[ KHẢM NGỌC ]").lore("§7Tiến hành dung hợp Ngọc vào Trang Bị.").build(), e -> {
            e.setCancelled(true);
            if (selectedGear == null || selectedGem == null) {
                player.sendMessage("§cBạn phải chọn đủ Trang Bị và Ngọc!");
                return;
            }
            
            // Anti-dupe check again
            if (!player.getInventory().contains(selectedGear) || !player.getInventory().contains(selectedGem)) {
                player.sendMessage("§cĐã có lỗi xảy ra! Vật phẩm không tồn tại trong túi.");
                player.closeInventory();
                return;
            }

            String gemId = plugin.getGemManager().getGemId(selectedGem);
            if (gemId == null) return;
            
            // Remove items
            selectedGear.setAmount(selectedGear.getAmount() - 1);
            selectedGem.setAmount(selectedGem.getAmount() - 1);

            // Create new socketed item
            ItemStack socketed = selectedGear.clone();
            socketed.setAmount(1);
            plugin.getItemManager().setSocket(socketed, 1, gemId);
            plugin.getItemManager().rebuildLore(socketed);
            
            player.getInventory().addItem(socketed);
            
            player.sendMessage("§a§l[!] §fKhảm thành công " + plugin.getGemManager().getGem(gemId).getDisplayName() + " §fvào trang bị!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1f);
            
            selectedGear = null;
            selectedGem = null;
            build(player);
        }));
    }
}