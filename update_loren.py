import re

codeGui = """package dev.elysium.item.gui;

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
        
        // Slot 11: Trang bị
        if (selectedGear == null) {
            setButton(11, new GuiButton(new ItemBuilder(Material.ANVIL).name("§e[Chọn Trang Bị]").lore("§7Click để chọn trang bị", "§7cần khảm ngọc.").build(), e -> {
                e.setCancelled(true);
                player.sendMessage("§cTính năng chọn trang bị đang được phát triển!");
            }));
        } else {
            setButton(11, new GuiButton(selectedGear, e -> { e.setCancelled(true); setGear(null); build(player); }));
        }
        
        // Slot 15: Ngọc Khảm
        if (selectedGem == null) {
            setButton(15, new GuiButton(new ItemBuilder(Material.EMERALD).name("§a[Chọn Ngọc Đột Biến]").lore("§7Click để chọn ngọc", "§7từ túi đồ.").build(), e -> {
                e.setCancelled(true);
                player.sendMessage("§cTính năng chọn ngọc đang được phát triển!");
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
            player.sendMessage("§a[!] Hệ thống khảm đang được hoàn thiện!");
            player.closeInventory();
        }));
    }
}"""

codeCmd = """package dev.elysium.item.command;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.gui.LorenGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LorenCommand implements CommandExecutor {
    private final ElysiumItem plugin;

    public LorenCommand(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, new LorenGui(plugin));
        }
        return true;
    }
}"""

with open(r'src/main/java/dev/elysium/item/gui/LorenGui.java', 'w', encoding='utf-8') as f:
    f.write(codeGui)
with open(r'src/main/java/dev/elysium/item/command/LorenCommand.java', 'w', encoding='utf-8') as f:
    f.write(codeCmd)

with open(r'src/main/java/dev/elysium/item/ElysiumItem.java', 'r', encoding='utf-8') as f:
    text = f.read()
text = text.replace('getCommand("eitem").setExecutor(new ItemCommand(this));', 'getCommand("eitem").setExecutor(new ItemCommand(this));\n        getCommand("loren").setExecutor(new dev.elysium.item.command.LorenCommand(this));')
with open(r'src/main/java/dev/elysium/item/ElysiumItem.java', 'w', encoding='utf-8') as f:
    f.write(text)

with open(r'src/main/java/dev/elysium/item/command/AdminCommand.java', 'r', encoding='utf-8') as f:
    text = f.read()

text = re.sub(r'if \(args\[0\]\.equalsIgnoreCase\("give"\) && args\.length == 3\) \{.*?(?=return true;\n        })return true;\n        }', '''if (args[0].equalsIgnoreCase("give") && args.length == 3) {
            org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(args[1]);
            if (target == null) { sender.sendMessage("§cOffline!"); return true; }
            org.bukkit.inventory.ItemStack item = plugin.getItemManager().createItem(args[2].toUpperCase());
            if (item != null) target.getInventory().addItem(item);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("gem") && args.length == 3) {
            org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(args[1]);
            if (target == null) { sender.sendMessage("§cOffline!"); return true; }
            org.bukkit.inventory.ItemStack item = plugin.getGemManager().createGemItem(args[2].toUpperCase());
            if (item != null) target.getInventory().addItem(item);
            return true;
        }''', text, flags=re.DOTALL)

with open(r'src/main/java/dev/elysium/item/command/AdminCommand.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("Loren GUI and Admin gem command created!")