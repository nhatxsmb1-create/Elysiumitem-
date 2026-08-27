package dev.elysium.item.command;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.gui.AdminGui;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminCommand implements CommandExecutor {

    private final ElysiumItem plugin;

    public AdminCommand(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("elysiumitem.admin")) {
            sender.sendMessage("§cBạn không có quyền!");
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                AdminGui gui = new AdminGui(plugin, player);
                dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, gui);
            } else {
                sender.sendMessage("§e/eitemadmin give <player> <item_id>");
                sender.sendMessage("§e/eitemadmin reload");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("give") && args.length == 3) {
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
        }

                if (args[0].equalsIgnoreCase("reload")) {
            plugin.getItemManager().loadAll();
            plugin.getGemManager().loadGems();
            
            // Auto update all online players
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                plugin.getItemManager().updatePlayerInventory(p);
            }
            
            sender.sendMessage("§a[!] Đã tải lại Config Vật Phẩm & Ngọc!");
            sender.sendMessage("§a[!] Đã tự động làm mới giao diện trang bị cho toàn bộ người chơi Online!");
            return true;
        }

        return true;
    }
}
