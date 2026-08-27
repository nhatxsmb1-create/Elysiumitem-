package dev.elysium.item.mechanic;

import dev.elysium.item.ElysiumItem;
import dev.elysium.core.event.ElysiumUltimateEvent;
import dev.elysium.item.item.GemData;
import dev.elysium.core.event.ElysiumDashEvent;
import dev.elysium.item.item.ElysiumItemData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MechanicEngine implements Listener {
    @EventHandler
    public void onUltimateCast(ElysiumUltimateEvent e) {
        Player player = e.getPlayer();
        List<org.bukkit.inventory.ItemStack> equipped = plugin.getItemManager().getEquippedItems(player);
        
        for (org.bukkit.inventory.ItemStack item : equipped) {
            String gemId = plugin.getItemManager().getSocket(item, 1);
            if (gemId != null) {
                GemData gem = plugin.getGemManager().getGem(gemId);
                if (gem != null) {
                    for (String mechanic : gem.getMechanics()) {
                        if (mechanic.equals("BLOOD_DOMAIN_ON_ULTIMATE")) {
                            executeBloodDomain(player);
                        } else if (mechanic.equals("FROST_NOVA_ON_ULTIMATE")) {
                            executeFrostNova(player);
                        }
                    }
                }
            }
        }
    }

    private void executeBloodDomain(Player player) {
        player.sendMessage("§4§l[!] §cKích hoạt Lãnh Địa Máu!");
        player.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, player.getLocation(), 200, 5, 0.1, 5, org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.5f);
        
        int totalMarksConsumed = 0;
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(10, 5, 10)) {
            if (entity instanceof LivingEntity target && target != player) {
                int marks = plugin.getMarkManager().getStacks(target, "BLOOD_MARK");
                if (marks > 0) {
                    totalMarksConsumed += marks;
                    plugin.getMarkManager().consumeMarks(target, "BLOOD_MARK");
                    target.damage(100.0 * marks, player); // Heavy damage
                }
            }
        }
        
        if (totalMarksConsumed > 0) {
            double heal = totalMarksConsumed * 20.0;
            player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
            player.sendMessage("§c❤ Hồi phục " + heal + " HP từ Huyết Ấn!");
        }
    }

    private void executeFrostNova(Player player) {
        player.sendMessage("§b§l[!] §fKích hoạt Sương Giá Cực Hàn!");
        player.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, player.getLocation(), 300, 8, 1, 8, 0.1);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
        
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(8, 5, 8)) {
            if (entity instanceof LivingEntity target && target != player) {
                target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 100, 9, false, false));
                target.damage(50.0, player); // Flat frost damage
            }
        }
    }

    
    private final ElysiumItem plugin;
    
    private final Map<UUID, Long> dashBuffs = new ConcurrentHashMap<>();

    public MechanicEngine(ElysiumItem plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onPlayerDash(ElysiumDashEvent event) {
        Player player = event.getPlayer();
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
        if (buffExpiry != null && System.currentTimeMillis() > buffExpiry) {
            dashBuffs.remove(player.getUniqueId());
            buffExpiry = null;
        }

        // ĐÂY LÀ ĐIỂM CÂN BẰNG TUYỆT ĐỐI: 
        // Lướt được tính khi: CÓ BUFF LƯỚT (Từ các skill gọi DashEvent như Florentino) HOẶC ĐANG CHẠY NHANH (Sprint của Minecraft)
        boolean isDashHit = (buffExpiry != null) || player.isSprinting();
        
        List<String> equipped = plugin.getItemManager().getEquippedItemIds(player);
        
        // 1. Tích Ấn
        boolean alreadyApplied = false;
        for (String id : equipped) {
            ElysiumItemData data = plugin.getItemManager().getItemData(id);
            if (data == null) continue;
            for (String mechanic : data.getMechanics()) {
                if (mechanic.startsWith("BLOOD_MARK_ON_DASH") && !alreadyApplied) {
                    if (isDashHit) {
                        plugin.getMarkManager().addMark(target, "BLOOD_MARK", 1, 5, 5000L, player);
                        alreadyApplied = true;
                    }
                }
            }
        }

        // 2. Tác dụng & Kích nổ
        int marks = plugin.getMarkManager().getStacks(target, "BLOOD_MARK");
        if (marks > 0) {
            boolean alreadyConsumed = false;
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
                    if (mechanic.startsWith("CONSUME_BLOOD_MARK_ON_DASH") && !alreadyConsumed) {
                        if (marks >= 5 && isDashHit) {
                            double boom = 50.0 * marks;
                            e.setDamage(e.getDamage() + boom);
                            player.sendMessage("§c§l💥 Huyết Ấn Nổ!");
                            plugin.getMarkManager().consumeMarks(target, "BLOOD_MARK");
                            alreadyConsumed = true;
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