package dev.elysium.item.command;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.gui.AdminGui;
import dev.elysium.item.gui.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {

    private final ElysiumItem plugin;

    public AdminCommand(ElysiumItem plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("elysium.item.admin")) {
            sender.sendMessage(color("&cKhong co quyen!")); return true;
        }

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            // /eia gui [player] - mo admin gui
            case "gui", "g" -> {
                Player viewer  = sender instanceof Player p ? p : null;
                Player target  = args.length >= 2 ? Bukkit.getPlayerExact(args[1]) : viewer;

                if (viewer == null) { sender.sendMessage("Chi player dung duoc!"); return true; }
                if (target == null) { sender.sendMessage(color("&cPlayer khong online!")); return true; }

                AdminGui gui = new AdminGui(plugin, target);
                GuiListener.register(viewer.getUniqueId(), gui);
                gui.open(viewer);
            }

            // /eia give <player> <item_id>
            case "give", "g2" -> {
                if (args.length < 3) {
                    sender.sendMessage(color("&cDung: /eia give <player> <item_id>")); return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(color("&cPlayer khong online!")); return true; }
                plugin.getItemManager().giveItem(target, args[2].toUpperCase());
                sender.sendMessage(color("&aDa give &f" + args[2] + " &acho &e" + target.getName()));
            }

            // /eia list - hien tat ca item
            case "list", "l" -> {
                sender.sendMessage(color("&5=== Danh sach Item Elysium ==="));
                for (var entry : plugin.getItemManager().getAllItems().entrySet()) {
                    sender.sendMessage(color("  &e" + entry.getKey()
                            + " &7- " + entry.getValue().getDisplayName()
                            + " &8[" + entry.getValue().getCategory() + "]"));
                }
            }

            // /eia addexp <player> <item_id> <amount>
            case "addexp" -> {
                if (args.length < 4) {
                    sender.sendMessage(color("&cDung: /eia addexp <player> <item_id> <amount>")); return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(color("&cPlayer khong online!")); return true; }
                try {
                    long amount = Long.parseLong(args[3]);
                    plugin.getItemMastery().addExp(target, args[2].toUpperCase(), amount);
                    sender.sendMessage(color("&aDa them &e" + amount + " &aEXP cho &f"
                            + target.getName() + " &avoi item &f" + args[2]));
                } catch (NumberFormatException e) {
                    sender.sendMessage(color("&cAmount phai la so!"));
                }
            }

            // /eia mas [player] - mo GUI mastery
            case "mas", "mastery" -> {
                Player viewer = sender instanceof Player p ? p : null;
                if (viewer == null) { sender.sendMessage("Chi player dung duoc!"); return true; }
                Player target = args.length >= 2 ? org.bukkit.Bukkit.getPlayerExact(args[1]) : viewer;
                if (target == null) { sender.sendMessage(color("&cPlayer khong online!")); return true; }

                dev.elysium.item.gui.MasteryGui gui = new dev.elysium.item.gui.MasteryGui(plugin);
                dev.elysium.item.gui.GuiListener.register(viewer.getUniqueId(), gui);
                gui.open(viewer);
            }

            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(color("&aReloaded!"));
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(color("&5=== ElysiumItem Admin ==="));
        s.sendMessage(color("  &7/eia gui [player] &f- Mo GUI admin"));
        s.sendMessage(color("  &7/eia give <player> <id> &f- Give item"));
        s.sendMessage(color("  &7/eia list &f- Danh sach item"));
        s.sendMessage(color("  &7/eia addexp <player> <id> <amount> &f- Them EXP"));
        s.sendMessage(color("  &7/eia reload &f- Reload config"));
        s.sendMessage(color("  &7/eia mas [player] &f- Mo GUI Mastery"));
    }

    private String color(String s) { return s.replace("&", "\u00a7"); }
}
