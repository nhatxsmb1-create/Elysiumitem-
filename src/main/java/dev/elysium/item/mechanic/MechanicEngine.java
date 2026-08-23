package dev.elysium.item.mechanic;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MechanicEngine {
    
    private final ElysiumItem plugin;
    
    // Store simple triggers. In the future, this can be expanded.
    // For now, we will just parse mechanic strings directly on events.
    public MechanicEngine(ElysiumItem plugin) {
        this.plugin = plugin;
    }
    
    /** 
     * Executed when a player performs a Dash.
     * Searches all equipped items for DASH related mechanics.
     */
    public void onPlayerDash(Player player) {
        List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);
        for (String id : equipped) {
            ElysiumItemData data = plugin.getItemManager().getItemData(id);
            if (data == null) continue;
            
            for (String mechanic : data.getMechanics()) {
                if (mechanic.startsWith("BLOOD_MARK_ON_DASH")) {
                    // Logic for BLOOD_MARK_ON_DASH
                    // Wait for MarkManager implementation...
                }
            }
        }
    }
    
    /**
     * Get total stat bonus from items (including tradeoff penalties).
     */
    public double getTotalStat(Player player, String statKey) {
        double total = 0;
        List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);
        for (String id : equipped) {
            ElysiumItemData data = plugin.getItemManager().getItemData(id);
            if (data == null) continue;
            
            total += data.getStatDouble(statKey);
            
            // Check tradeoffs for stat reductions
            for (String tradeoff : data.getTradeoffs()) {
                if (tradeoff.startsWith("HEAL_REDUCTION") && statKey.equals("healing-bonus")) {
                    String[] parts = tradeoff.split(":");
                    if (parts.length > 1) {
                        try {
                            total -= Double.parseDouble(parts[1]) / 100.0;
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return total;
    }
}