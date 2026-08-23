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
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cNgười chơi không online!");
                return true;
            }
            String itemId = args[2].toUpperCase();
            ItemStack item = plugin.getItemManager().createItem(itemId);
            if (item == null) {
                sender.sendMessage("§cItem không tồn tại!");
                return true;
            }
            target.getInventory().addItem(item);
            sender.sendMessage("§aĐã đưa " + itemId + " cho " + target.getName());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§aReload thành công!");
            return true;
        }

        return true;
    }
}