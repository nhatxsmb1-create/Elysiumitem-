package dev.elysium.item.command;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.gui.GuiListener;
import dev.elysium.item.gui.TrangBiGui;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TrangBiCommand implements CommandExecutor {

    private final ElysiumItem plugin;

    public TrangBiCommand(ElysiumItem plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Chi player dung duoc!"); return true;
        }
        TrangBiGui gui = new TrangBiGui(plugin);
        GuiListener.register(player.getUniqueId(), gui);
        gui.open(player);
        return true;
    }
}
