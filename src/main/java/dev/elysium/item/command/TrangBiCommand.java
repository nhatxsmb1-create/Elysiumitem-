package dev.elysium.item.command;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.gui.TrangBiGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrangBiCommand implements CommandExecutor {
    private final ElysiumItem plugin;

    public TrangBiCommand(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            TrangBiGui gui = new TrangBiGui(plugin);
            dev.elysium.core.ElysiumCore.getInstance().getGuiManager().open(player, gui);
        }
        return true;
    }
}