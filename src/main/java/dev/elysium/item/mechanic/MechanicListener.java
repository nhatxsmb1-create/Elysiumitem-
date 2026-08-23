package dev.elysium.item.mechanic;

import dev.elysium.core.event.ElysiumDashEvent;
import dev.elysium.item.ElysiumItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MechanicListener implements Listener {

    private final ElysiumItem plugin;

    public MechanicListener(ElysiumItem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDash(ElysiumDashEvent e) {
        plugin.getMechanicEngine().onPlayerDash(e.getPlayer());
    }
}