package dev.elysium.item.mechanic;

import dev.elysium.item.ElysiumItem;
import dev.elysium.item.item.ElysiumItemData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MechanicEngine implements Listener {
    
    private final ElysiumItem plugin;
    
    // UUID -> Expiry time of "dash buff"
    private final Map<UUID, Long> dashBuffs = new ConcurrentHashMap<>();

    public MechanicEngine(ElysiumItem plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void onPlayerDash(Player player) {
        List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);
        for (String id : equipped) {
            ElysiumItemData data = plugin.getItemManager().getItemData(id);
            if (data == null) continue;
            
            for (String mechanic : data.getMechanics()) {
                if (mechanic.startsWith("BLOOD_MARK_ON_DASH")) {
                    dashBuffs.put(player.getUniqueId(), System.currentTimeMillis() + 1500L);
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;

        Long buffExpiry = dashBuffs.get(player.getUniqueId());
        if (buffExpiry != null) {
            if (System.currentTimeMillis() <= buffExpiry) {
                plugin.getMarkManager().addMark(target, "BLOOD_MARK", 1, 5, 5000L, player);
                dashBuffs.remove(player.getUniqueId());
            } else {
                dashBuffs.remove(player.getUniqueId());
            }
        }
        
        int marks = plugin.getMarkManager().getStacks(target, "BLOOD_MARK");
        if (marks > 0) {
            List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);
            for (String id : equipped) {
                ElysiumItemData data = plugin.getItemManager().getItemData(id);
                if (data == null) continue;
                for (String mechanic : data.getMechanics()) {
                    if (mechanic.startsWith("DAMAGE_PER_BLOOD_MARK:")) {
                        try {
                            String[] parts = mechanic.split(":");
                            double percent = Double.parseDouble(parts[1]) / 100.0;
                            double extra = e.getDamage() * (percent * marks);
                            e.setDamage(e.getDamage() + extra);
                        } catch (Exception ex) {}
                    }
                    if (mechanic.startsWith("CONSUME_BLOOD_MARK_ON_DASH") && buffExpiry != null) {
                        if (System.currentTimeMillis() <= buffExpiry) {
                            double boom = 50.0 * marks;
                            e.setDamage(e.getDamage() + boom);
                            player.sendMessage("§c§l💥 Huyết Ấn Nổ!");
                            plugin.getMarkManager().consumeMarks(target, "BLOOD_MARK");
                        }
                    }
                }
            }
        }
    }
    
    public double getTotalStat(Player player, String statKey) {
        double total = 0;
        List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);
        for (String id : equipped) {
            ElysiumItemData data = plugin.getItemManager().getItemData(id);
            if (data == null) continue;
            
            total += data.getStatDouble(statKey);
            
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