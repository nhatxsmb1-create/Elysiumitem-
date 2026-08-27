package dev.elysium.item.command;

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
}