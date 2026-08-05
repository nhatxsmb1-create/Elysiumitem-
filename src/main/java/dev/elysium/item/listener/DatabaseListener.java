package dev.elysium.item.listener;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.PlayerItemState;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

public class DatabaseListener implements Listener {

    private final ElysiumItem plugin;

    public DatabaseListener(ElysiumItem plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, Long> data   = plugin.getItemDatabase().load(uuid);
            String            slots  = plugin.getItemDatabase().loadAccessorySlots(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Load mastery EXP
                PlayerItemState state = plugin.getItemManager().getState(e.getPlayer());
                state.loadFromDb(data);
                // Load accessory slots
                plugin.getAccessoryManager().loadSlots(e.getPlayer(), slots);
                // Restore stats sau khi load xong (delay 1 tick cho player spawn)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getAccessoryManager().restoreStats(e.getPlayer());
                }, 5L);
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        // Remove stats truoc khi quit
        plugin.getAccessoryManager().removeAllStats(e.getPlayer());
        plugin.getAccessoryManager().removeSlots(uuid);
        // Save
        PlayerItemState state = plugin.getItemManager().getState(e.getPlayer());
        Map<String, Long> allExp = state.getAllExp();
        if (!allExp.isEmpty()) plugin.getItemDatabase().saveAsync(uuid, allExp);
        plugin.getItemManager().removeState(uuid);
    }
}
