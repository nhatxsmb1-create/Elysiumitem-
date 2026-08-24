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
                    // Cấp buff "sẵn sàng tích dấu ấn Huyết Chiến" trong vòng 1.5s sau khi lướt
                    dashBuffs.put(player.getUniqueId(), System.currentTimeMillis() + 1500L);
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;

        // Check if player has Blood Mark dash buff
        Long buffExpiry = dashBuffs.get(player.getUniqueId());
        if (buffExpiry != null) {
            if (System.currentTimeMillis() <= buffExpiry) {
                // Đang trong thời gian lướt -> Tích Huyết Ấn
                plugin.getMarkManager().addMark(target, "BLOOD_MARK", 1, 5, 5000L, player); // 5 max, 5s duration
                // Xóa buff sau khi chém trúng (mỗi lần lướt chỉ tích 1 lần)
                dashBuffs.remove(player.getUniqueId());
            } else {
                dashBuffs.remove(player.getUniqueId());
            }
        }
        
        // Handle DAMAGE_PER_BLOOD_MARK_5 (if target has blood marks, deal extra damage)
        int marks = plugin.getMarkManager().getStacks(target, "BLOOD_MARK");
        if (marks > 0) {
            List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);
            for (String id : equipped) {
                ElysiumItemData data = plugin.getItemManager().getItemData(id);
                if (data == null) continue;
                for (String mechanic : data.getMechanics()) {
                    if (mechanic.startsWith("DAMAGE_PER_BLOOD_MARK_5")) {
                        // Tăng 5% sát thương mỗi ấn
                        double extra = e.getDamage() * (0.05 * marks);
                        e.setDamage(e.getDamage() + extra);
                    }
                    if (mechanic.startsWith("CONSUME_BLOOD_MARK_ON_DASH") && buffExpiry != null) {
                        // Nếu lướt qua kẻ thù có huyết ấn, nổ dame và tiêu hao
                        // Damage nổ: 50 * số ấn
                        double boom = 50.0 * marks;
                        e.setDamage(e.getDamage() + boom);
                        
                        target.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, target.getLocation(), 1);
                        player.sendMessage("§c§l💥 Huyết Ấn Nổ!");
                        
                        plugin.getMarkManager().consumeMarks(target, "BLOOD_MARK");
                        // Vì đã tiêu hao nên set mark = 0 cho các check sau
                        marks = 0; 
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